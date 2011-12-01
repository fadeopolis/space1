/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.tutorial.ticketqueue;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;

/*
 * Ticket Queue (FifoCoordinator).
 * This example writes a couple of persons to the container.
 * Every person in the ticket queue has to pay the ticket and
 * wait for it to be printed.
 *
 * @author Formanek, Keszthelyi, Efler
 */
public class TicketQueue {

    private static int CUSTOMERS = 5;

    public static void main(final String[] args) throws MzsCoreException, URISyntaxException {
        /* Create new Capi instance */
        MzsCore core = DefaultMzsCore.newInstance();
        Capi capi = new Capi(core);

        /*
         * Create new Container using FifoCoordinator, FifoSelector and an infinite container-size
         */
        ContainerReference cref = capi.createContainer("queue", null, MzsConstants.Container.UNBOUNDED,
                Arrays.asList(new FifoCoordinator()), null, null);

        /* Fill the queue with a couple of persons */
        for (int i = 1; i <= CUSTOMERS; i++) {
            /* Create a new entry */
            Entry entry = new Entry(new Person(i), FifoCoordinator.newCoordinationData());
            /* Write the entry to the container */
            capi.write(cref, 0, null, entry);
        }

        /* Handle the persons of the queue */
        for (int i = 1; i <= CUSTOMERS; i++) {
            /* Take an entry from the container using the FifoSelector */
            ArrayList<Serializable> readEntries = capi.take(cref, FifoCoordinator.newSelector(),
                    MzsConstants.RequestTimeout.INFINITE, null);
            if (readEntries.get(0).getClass().isAssignableFrom(Person.class)) {
                Person actualPerson = (Person) readEntries.get(0);
                /* start paying the Ticket */
                actualPerson.payTicket();
                /* start printing the Ticket */
                actualPerson.waitForTicket();
            }
        }

        /* Remove container from space */
        capi.destroyContainer(cref, null);

        capi.shutdown(null);
    }
}
