package org.mozartspaces.examples.tutorial.transactions;

import java.io.Serializable;

/*
 * Exercise 3.1: Transactions (Formula 1 extension)
 * This class is used to coordinate the server and its clients.
 *
 * @author Formanek, Keszthelyi
 */
public class Message implements Serializable {

    private static final long serialVersionUID = -1160296483016366320L;

    public static final int START_RACE = 0;
    public static final int CAR_FINISHED = 1;

    private int messageType = -1;

    /**
     * Constructor
     *
     * @param messageType
     */
    public Message(final int messageType) {
        this.messageType = messageType;
    }

    /**
     * Return message-type
     *
     * @return messageType
     */
    public int getMessageType() {
        return messageType;
    }
}
