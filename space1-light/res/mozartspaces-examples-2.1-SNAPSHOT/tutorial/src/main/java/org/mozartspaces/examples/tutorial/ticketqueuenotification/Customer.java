/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.tutorial.ticketqueuenotification;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

/*
 * TicketQueue with Notifications.
 * This class represents the customer who places
 * his payment on the salesdesk and waits until
 * he receives his ticket.
 *
 * @author Formanek, Keszthelyi, Efler
 */
public class Customer implements NotificationListener {

    private int ID = 0;
    private Capi capi = null;
    private final boolean quit = false;
    private ContainerReference salesDesk = null;

    /**
     * Constructor
     *
     * @param ID
     *            customer ID
     */
    public Customer(final int ID) {
        this.ID = ID;
    }

    /**
     * create/lookup container, register as a notificationlistener, place
     * payment into the salesdesk-container and wait until the ticket has been
     * received.
     */
    public void run() {
        try {
            MzsCore core = DefaultMzsCore.newInstance(0);
            capi = new Capi(core);
            NotificationManager notifManager = new NotificationManager(core);

            try {
                /* try to create container */
                capi.createContainer(ISalesDeskObject.SALES_DESK_CONTAINER, new URI("xvsm://localhost:9876"),
                        MzsConstants.Container.UNBOUNDED, Arrays.asList(new FifoCoordinator()), null, null);
            } catch (Exception ex) {
                /* nothing to do here */
            }

            /* lookup container */
            salesDesk = capi.lookupContainer(ISalesDeskObject.SALES_DESK_CONTAINER, new URI("xvsm://localhost:9876"),
                    MzsConstants.RequestTimeout.INFINITE, null);

            /* register to receive notifications */
            HashSet<Operation> operation = new HashSet<Operation>();
            operation.add(Operation.WRITE);
            notifManager.createNotification(salesDesk, this, operation, null, null);
            System.out.println("Registered to the Container");

            /* create payment */
            Payment payment = new Payment(new Person(ID));
            Entry entry = new Entry(payment, Arrays.asList(FifoCoordinator.newCoordinationData()));
            /* write payment */
            capi.write(salesDesk, entry);
            System.out.println("Placed Payement");

            /* wait for ticket */
            while (!quit) {
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /*
     * on notification verify if a ticket is the first entry in the
     * salesdesk-container, in case it is taken from the container and exit.
     */
    @Override
    public void entryOperationFinished(final Notification source, final Operation operation,
            final List<? extends Serializable> entries) {
        System.out.println("Received Notification!");

        try {
            /* take first entry from container using the fifo-selector */
            ArrayList<Serializable> read;
            try {
                read = capi.read(salesDesk, Arrays.asList(FifoCoordinator.newSelector(1)),
                        MzsConstants.RequestTimeout.INFINITE, null);
            } catch (Exception e) {
                /* someone was faster, nothing to do */
                return;
            }
            ISalesDeskObject sdo = ((ISalesDeskObject) read.get(0));

            System.out.println("class :: " + sdo.getClass());
            /* Check if the object is a payment and this customer is its owner */
            if ((sdo instanceof Ticket) && (sdo.getOwner().getID() == ID)) {
                /* take the ticket */
                read = capi.take(salesDesk, Arrays.asList(FifoCoordinator.newSelector(1)),
                        MzsConstants.RequestTimeout.INFINITE, null);
                System.out.println("Customer: Received my Ticket!" + read.size());

                /* nothing to do anymore, exit */
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {
        /* check if an ID has been provided */
        if ((args.length != 1)) {
            printUsage();
        }

        int ID = 0;

        /* parseInt, printUsage on fail */
        try {
            ID = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            printUsage();
        }

        /* ID must be > 0 */
        if (ID > 0) {
            Customer customer = new Customer(Integer.parseInt(args[0]));
            customer.run();
        } else {
            printUsage();
        }
    }

    /**
     * Print correct usage and exit.
     */
    public static void printUsage() {
        System.out.println("Usage: customer-id\n" + "id ... Integer > 0");
        System.exit(1);
    }
}
