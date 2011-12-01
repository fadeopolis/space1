package org.mozartspaces.examples.tutorial.transactions;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.mozartspaces.capi3.VectorCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

/*
 * Exercise 3.1: Transactions (Formula 1 extension)
 * This class represents the server and coordinates
 * the clients so they can start all at the same time.
 *
 * @author Formanek, Keszthelyi, Efler
 */
public class Formula1RaceTransaction implements NotificationListener {

    public static final String RACING_POSITIONS_CONTAINER = "RacingPositions";
    public static final String COORDINATION_CONTAINER = "Coordination";
    public static final int MAX_LAPS = 2;
    public static final int MAX_CARS = 10;
    public static final int WAIT_FOR_CLIENTS = 15;

    private Capi capi = null;
    private URI serverURI = null;
    private ContainerReference racingPositions = null;
    private ContainerReference coordination = null;
    private volatile boolean running = false;
    private int registeredCars = 0;

    /**
     * Create a new race and wait X sec (= WAIT_FOR_CLIENTS) for clients to register. After the expiration of this term
     * place a message to the coordination-container, telling the clients to start the race. After the last client has
     * finished print the results.
     *
     * @throws XCoreException
     * @throws URISyntaxException
     * @throws InterruptedException
     */
    public void openRace() throws MzsCoreException, URISyntaxException, InterruptedException {
        /* Create new Capi instance */
        MzsCore core = DefaultMzsCore.newInstance();
        capi = new Capi(core);
        serverURI = new URI("xvsm://localhost:9876");

        NotificationManager notifManager = new NotificationManager(core);

        /* Create new Container using VectorCoordinator */
        racingPositions = capi.createContainer(RACING_POSITIONS_CONTAINER, serverURI, MAX_CARS,
                Arrays.asList(new VectorCoordinator()), null, null);
        coordination = capi.createContainer(COORDINATION_CONTAINER, serverURI, MzsConstants.Container.UNBOUNDED,
                Arrays.asList(new VectorCoordinator()), null, null);

        /* Wait for clients */
        for (int i = WAIT_FOR_CLIENTS; i > 0; i--) {
            System.out.println("Waiting " + i + " seconds for Clients");
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /* read how many cars have registered */
        registeredCars = capi.read(racingPositions,
                Arrays.asList(VectorCoordinator.newSelector(0, MzsConstants.Selecting.COUNT_ALL)),
                MzsConstants.RequestTimeout.INFINITE, null).size();
        System.out.println("Found " + registeredCars + " registered cars");

        /* start race if one or more cars have registered */
        if (registeredCars > 0) {
            System.out.println("Starting Race!");
            running = true;

            /* register to be notified on WRITEs in the coordination-container */
            HashSet<Operation> operations = new HashSet<Operation>();
            operations.add(Operation.WRITE);
            notifManager.createNotification(coordination, this, operations, null, null);

            /* place message telling clients to start race */
            /* create transaction */
            TransactionReference tx = capi.createTransaction(MzsConstants.TransactionTimeout.INFINITE, serverURI);
            /* create new entry appending to vectorcontainer */
            Entry startMessage = new Entry(new Message(Message.START_RACE), Arrays.asList(VectorCoordinator
                    .newCoordinationData(VectorCoordinator.APPEND)));
            /* write entry with transaction */
            capi.write(coordination, MzsConstants.RequestTimeout.INFINITE, tx, startMessage);
            /* commit transaction */
            capi.commitTransaction(tx);

            /* wait until the race has finished */
            while (running)
                ;
        }
    }

    @Override
    public void entryOperationFinished(final Notification source, final Operation operation,
            final List<? extends Serializable> entries) {
        try {
            /* lookup how many entries are already in the container */
            int numOfEntries = capi.read(coordination,
                    Arrays.asList(VectorCoordinator.newSelector(MzsConstants.Selecting.COUNT_ALL, 0)),
                    MzsConstants.RequestTimeout.INFINITE, null).size();
            /*
             * if there are registeredCars + 1 entries in the coordination-container then all clients have finished.
             * print the results and quit.
             */
            if (numOfEntries == registeredCars + 1) {
                running = false;

                System.out.println("Race finished!\n Printing result:");
                /* Read the current ranking */
                ArrayList<Serializable> readEntries = capi.read(racingPositions,
                        Arrays.asList(VectorCoordinator.newSelector(MzsConstants.Selecting.COUNT_ALL, 0)),
                        MzsConstants.RequestTimeout.INFINITE, null);

                int i = 1;
                for (Serializable readEntry : readEntries) {
                    if (readEntry.getClass().isAssignableFrom(CarInfo.class)) {
                        CarInfo car = (CarInfo) readEntry;
                        System.out.println("Pos " + i++ + " - " + car.getDriver() + ", Time: " + car.getRunTime());
                    }
                }

                System.out.println("*** *** *** *** *** ***\n\nShuting down now!");

                /* Destroy containers */
                capi.destroyContainer(racingPositions, null);
                capi.destroyContainer(coordination, null);

                System.out.println("Bye!");
                capi.shutdown(null);
            }
        } catch (MzsCoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @param args
     * @throws URISyntaxException
     * @throws XCoreException
     */
    public static void main(final String[] args) {
        Formula1RaceTransaction f1Race = new Formula1RaceTransaction();

        try {
            /* start a new race */
            f1Race.openRace();
        } catch (MzsCoreException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
