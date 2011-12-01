package org.mozartspaces.examples.tutorial.transactions;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.mozartspaces.capi3.VectorCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsTimeoutException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

/*
 * Exercise 3.1: Transactions (Formula 1 extension)
 * This class represents the car (client) that registers to
 * the server by placing its carinfo into the \"racingPositions\"
 * container. After receiving a notification from the server about
 * starting the race, the simulation is started. Each round the carInfo
 * is taken from the container, updated and written back to the container.
 * This is done by using one transaction and on error retried until the
 * transaction could successfully be committed.
 *
 * @author Formanek, Keszthelyi
 */
public class RacingCar implements NotificationListener {

    private Capi capi = null;
    private ContainerReference racingPositions = null;
    private ContainerReference coordination = null;
    private URI serverURI = null;

    private final boolean running = true;
    private final String driver;

    /**
     * Constructor
     *
     * @param driver
     *            Name of driver
     */
    public RacingCar(final String driver) {
        this.driver = driver;
    }

    /**
     * Register car by placing its carinfo into the \"racingPositions\" container.
     *
     *
     * @throws XCoreException
     * @throws URISyntaxException
     * @throws InterruptedException
     */
    public void registerCar() throws MzsCoreException, URISyntaxException, InterruptedException {
        MzsCore core = DefaultMzsCore.newInstance();
        capi = new Capi(core);
        serverURI = new URI("xvsm://localhost:9876");

        NotificationManager notifManager = new NotificationManager(core);

        /* lookup containers */
        racingPositions = capi.lookupContainer(Formula1RaceTransaction.RACING_POSITIONS_CONTAINER, serverURI,
                MzsConstants.RequestTimeout.INFINITE, null);
        coordination = capi.lookupContainer(Formula1RaceTransaction.COORDINATION_CONTAINER, serverURI,
                MzsConstants.RequestTimeout.INFINITE, null);
        System.out.println("Found Container");

        /*
         * register to receive notification about the race-start. because we don't expect other messages at the
         * beginning, we only need a notification once.
         */
        HashSet<Operation> operations = new HashSet<Operation>();
        operations.add(Operation.WRITE);
        notifManager.createNotification(coordination, this, operations, null, null);

        /* create transaction */
        TransactionReference tx = capi.createTransaction(MzsConstants.TransactionTimeout.INFINITE, serverURI);
        /* create new carinfo-entry appending it to vectorcontainer */
        Entry carInfo = new Entry(new CarInfo(driver), Arrays.asList(VectorCoordinator
                .newCoordinationData(VectorCoordinator.APPEND)));
        /* write carinfo */
        capi.write(racingPositions, MzsConstants.RequestTimeout.INFINITE, null, carInfo);
        /* commit transaction */
        capi.commitTransaction(tx);

        System.out.println("Registered Car!");

        /* wait until race has finished */
        while (running)
            ;
    }

    /**
     * Simulate the race
     *
     * @throws XCoreException
     */
    public void simulateRace() throws MzsCoreException {
        System.out.println("Simulating!");

        for (int lap = 1; lap <= Formula1RaceTransaction.MAX_LAPS; lap++) {
            Random rnd = new Random();

            int time = rnd.nextInt(2) + 3;

            System.out.println("Lap: " + lap + "waiting " + time + "sec.");
            try {
                /* simulate lap */
                Thread.sleep(time * 1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            boolean reposition = true;

            /* retry until transaction has successfully committed */
            while (reposition) {
                /* create transaction */
                TransactionReference tx = capi.createTransaction(MzsConstants.TransactionTimeout.INFINITE, serverURI);

                try {
                    /* take current positions */
                    ArrayList<Serializable> entries = capi
                            .take(racingPositions,
                                    Arrays.asList(VectorCoordinator.newSelector(MzsConstants.Selecting.COUNT_ALL, 0)),
                                    2500, tx);

                    /* search for the carinfo matching this driver */
                    for (Serializable entry : entries) {
                        CarInfo car = (CarInfo) entry;

                        if (car.getDriver().compareTo(driver) == 0) {
                            /* update carinfo */
                            car.addTime(time);
                            car.incrementLap();
                            break;
                        }
                    }

                    /* new vector for easier sorting */
                    Vector<CarInfo> newPositions = new Vector<CarInfo>();

                    /* resort entries */
                    for (Serializable entry : entries) {
                        int i = 0;
                        /* find position for entry */
                        for (; i < newPositions.size(); i++) {
                            CarInfo car = newPositions.get(i);

                            if (car.getRunTime() > ((CarInfo) entry).getRunTime()) {
                                newPositions.add(i, ((CarInfo) entry));
                                break;
                            }
                        }

                        /*
                         * in case the vector is empty or we reached the end of vector in the iteration above, append
                         * entry
                         */
                        if (i == newPositions.size()) {
                            newPositions.add((CarInfo) entry);
                        }
                    }

                    /* write entries */
                    for (CarInfo car : newPositions) {
                        /* create new entry for carinfo */
                        Entry carInfo = new Entry(car, Arrays.asList(VectorCoordinator
                                .newCoordinationData(VectorCoordinator.APPEND)));
                        /* write carinfo */
                        capi.write(racingPositions, 2500, tx, carInfo);
                    }

                    /* commit transaction */
                    capi.commitTransaction(tx);

                    /* leave loop */
                    reposition = false;
                } catch (MzsCoreException e) {
                    /* is exception an InvalidTransactionException? */
                    if (e instanceof MzsTimeoutException) {
                        capi.rollbackTransaction(tx);
                    }
                    /* is exception an TimeoutExpiredException? */
                    else if (e instanceof MzsTimeoutException) {
                        /* try to rollback transaction */
                        try {
                            capi.rollbackTransaction(tx);
                        } catch (MzsCoreException ex) {
                            // discard any XCoreExceptions here
                        }
                    }
                    /* in all other cases print error and exit */
                    else {
                        e.printStackTrace();
                        System.exit(1);
                    }

                    /*
                     * Penalty in case of transaction/timeout-exception This is needed to get a better propability that
                     * one client can commit its transaction and minimize concurrency problems.
                     */
                    int penalty = rnd.nextInt(10) * 100;
                    System.out.println("Timeout while waiting!\nPenalty waiting " + penalty + "ms");

                    try {
                        Thread.sleep(penalty);
                    } catch (InterruptedException e1) {
                    }
                }
            }
        }

        /* Simulation ended. Place Finished-message into coordination-container */
        System.out.println("Finished! Placing message...");
        /* create transaction */
        TransactionReference tx = capi.createTransaction(MzsConstants.TransactionTimeout.INFINITE, serverURI);
        /* create new entry appending to vectorcontainer */
        Entry carInfo = new Entry(new Message(Message.CAR_FINISHED), Arrays.asList(VectorCoordinator
                .newCoordinationData(VectorCoordinator.APPEND)));
        /* write entry with transaction */
        capi.write(coordination, MzsConstants.RequestTimeout.INFINITE, tx, carInfo);
        /* commit transaction */
        capi.commitTransaction(tx);

        System.out.println("Simulation done! Bye!");
        System.exit(0);
    }

    /*
     * wait for notification about race-start
     */
    @Override
    public void entryOperationFinished(final Notification source, final Operation operation,
            final List<? extends Serializable> entries) {

        /* inspect first entry */
        if (((Message) entries.get(0)).getMessageType() == Message.START_RACE) {
            try {
                simulateRace();
            } catch (MzsCoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * @param args
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws XCoreException
     */
    public static void main(final String[] args) throws MzsCoreException, URISyntaxException, InterruptedException {
        if ((args.length != 1)) {
            System.out.println("Usage: RacingCar name\n" + "name ... Driver-Name");
            System.exit(1);
        }

        RacingCar racingCar = new RacingCar(args[0]);

        /* register car for race */
        racingCar.registerCar();
    }

}
