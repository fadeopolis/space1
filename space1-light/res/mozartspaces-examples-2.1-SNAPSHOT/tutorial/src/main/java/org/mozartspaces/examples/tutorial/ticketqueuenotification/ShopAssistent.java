/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.tutorial.ticketqueuenotification;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

/*
 * TicketQueue with Notifications.
 * This class represents the shop-assistent who
 * waits for payments from customers and places
 * the ticket.
 *
 * @author Formanek, Keszthelyi
 */
public class ShopAssistent implements NotificationListener {

    private Capi capi = null;
    private boolean quit = false;
    private ContainerReference salesDesk = null;

    /**
     * Start shop-assistent. Create or lookup container, register as a
     * notificationlistener, check if there are already payments, proceed first
     * and wait then for notifications.
     */
    public void run() {
        try {
            MzsCore core = DefaultMzsCore.newInstance();
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

            /*
             * check if there are already payments available after proceeding
             * the first payment we will be notified about new writes and don't
             * have to take care about other payments here anymore.
             *
             * We assume that clients don't leave without taken their ticket!
             */
            ArrayList<Serializable> entries;
            try {
                entries = capi.read(salesDesk, Arrays.asList(FifoCoordinator.newSelector(1)),
                        MzsConstants.RequestTimeout.INFINITE, null);
                ISalesDeskObject sdo = ((ISalesDeskObject) entries.get(0));

                /* is the entry an instance of payment */
                if (sdo instanceof Payment) {
                    proceedPayment(salesDesk);
                }
            } catch (Exception e) {

            }
            System.out.println("Waiting for customers ...");

            /* wait for correct user input to quit */
            while (!quit) {
                BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

                if (stdin.readLine().compareTo("q") == 0) {
                    quit = true;
                }
            }
        } catch (Exception e) {
            /* exit on error */
            e.printStackTrace();
            System.exit(1);
        }
    }

    /*
     * check notification if it is an instance of payment, take it from the
     * container if it is and write back a new ticket for the payment's owner.
     * Ignore all other notifications.
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

            /* Check if the object is a payment */
            if (sdo instanceof Payment) {
                proceedPayment(salesDesk);
            } else {
                System.out.println("Received Notification! -- IGNORING");
            }
        } catch (Exception e) {
            /* print any exception, we don't expect one */
            e.printStackTrace();
        }
        System.out.println("Received Notification! -- END");
    }

    /**
     * take the payment from the container and write back a ticket for the
     * payment's owner
     *
     *
     * @param salesDesk
     * @throws XCoreException
     */
    public void proceedPayment(final ContainerReference salesDesk) throws MzsCoreException {
        /* take first entry using the fifo-selector */
        ArrayList<Serializable> entries = capi.take(salesDesk, Arrays.asList(FifoCoordinator.newSelector()),
                MzsConstants.RequestTimeout.INFINITE, null);

        Payment payment = (Payment) ((ISalesDeskObject) entries.get(0));
        System.out.println("ShopAssistent: Received Payment from ID: " + payment.getOwner().getID());

        /* create a new ticket */
        Ticket ticket = new Ticket(payment.getOwner());
        Entry entry = new Entry(ticket, Arrays.asList(FifoCoordinator.newCoordinationData()));
        /* write ticket into the container */
        capi.write(salesDesk, MzsConstants.RequestTimeout.INFINITE, null, entry);
        System.out.println("ShopAssistent: Placed Ticket for ID: " + ticket.getOwner().getID() + " :: "
                + ticket.getClass());
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {
        ShopAssistent assi = new ShopAssistent();
        assi.run();

        System.out.println("Exiting!");
        System.exit(0);
    }
}
