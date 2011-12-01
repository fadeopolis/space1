/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.tutorial.ticketqueuenotification;

/*
 * TicketQueue with Notifications.
 * This class implements ISalesDeskObject and represents a payment placed by customer.
 *
 * @author Formanek, Keszthelyi
 */
public class Payment implements ISalesDeskObject {

    private static final long serialVersionUID = -5581489245914673387L;

    private final Person person; // Customer who placed this payment

    /**
     * Create a payment
     *
     * @param person
     */
    public Payment(final Person person) {
        super();
        this.person = person;
    }

    /**
     * return this payment's owner
     *
     * @return person
     */
    public Person getOwner() {
        return person;
    }
}
