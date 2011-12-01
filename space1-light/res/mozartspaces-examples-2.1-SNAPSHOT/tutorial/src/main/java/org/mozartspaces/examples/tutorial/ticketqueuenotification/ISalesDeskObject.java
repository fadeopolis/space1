/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.tutorial.ticketqueuenotification;

import java.io.Serializable;

/*
 * TicketQueue with Notifications.
 * This is an interface for objects placed in the "SalesDesk"-container
 *
 * @author Formanek, Keszthelyi
 */
public interface ISalesDeskObject extends Serializable {
    public static final String SALES_DESK_CONTAINER = "SalesDesk";

    public Person getOwner();
}
