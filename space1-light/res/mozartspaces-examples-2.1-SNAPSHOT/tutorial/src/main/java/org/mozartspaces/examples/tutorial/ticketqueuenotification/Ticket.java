/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.tutorial.ticketqueuenotification;

/*
 * TicketQueue with Notifications.
 * This class implements ISalesDeskObject and represents a ticket placed by a shopassistant.
 *
 * @author Formanek, Keszthelyi
 */
public class Ticket implements ISalesDeskObject {

    private static final long serialVersionUID = -5396117582531479113L;

    private final Person person; // Customer who placed this payment

    /**
     * Create a payment.
     *
     * @param person
     *            The person who has payed for the Ticket
     */
    public Ticket(final Person person) {
        this.person = person;
    }

    /**
     * Takes a person as parameter, compares it with the owner of this ticket
     * and returns true if the comparison was true.
     *
     * @param person
     *
     * @return boolean
     */
    public boolean isOwner(final Person person) {
        return this.person.equals(person);
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
