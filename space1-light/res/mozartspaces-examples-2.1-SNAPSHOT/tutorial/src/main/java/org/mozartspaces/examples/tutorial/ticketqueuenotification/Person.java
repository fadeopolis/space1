/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.tutorial.ticketqueuenotification;

import java.io.Serializable;

/*
 * TicketQueue with Notifications.
 * This class is used to identify the customer who
 * pays and waits for his ticket.
 *
 * @author Formanek, Keszthelyi
 */
public class Person implements Serializable {

    private static final long serialVersionUID = -8458051209789762141L;

    /* customer-ID */
    private final int ID;

    /**
     * Constructor
     *
     * @param ID
     *            customer-ID
     */
    public Person(final int ID) {
        this.ID = ID;
    }

    /**
     * Return customer's ID
     *
     * @return ID
     */
    public int getID() {
        return ID;
    }
}
