/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.tutorial.ticketqueue;

import java.io.Serializable;

/*
 * Example 2.5.2: Ticket Queue (FifoCoordinator)
 * This class provides all needed actions of a Person.
 *
 * @author Formanek, Keszthelyi
 */
public class Person implements Serializable {

    private static final long serialVersionUID = 4639391314689106800L;

    final int waitingPosition;

    /**
     * Constructor
     *
     * @param waitingPosition
     */
    public Person(final int waitingPosition) {
        this.waitingPosition = waitingPosition;
    }

    /**
     * Simulate payment
     */
    public void payTicket() {
        try {
            System.out.println("The " + waitingPosition + ". person pays the ticket.");
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Simulate waiting for the ticket
     */
    public void waitForTicket() {
        try {
            System.out.println("The " + waitingPosition + ". person waits for the ticket.");
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
