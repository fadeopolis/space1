/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.flughafen;

import java.awt.Color;
import java.awt.TextField;
import java.io.Serializable;
import java.util.List;

import org.mozartspaces.core.Entry;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.Operation;

public final class FlughafenNotificationListener implements NotificationListener {

    private final TextField towerTextField;
    private final String flughafenName;

    FlughafenNotificationListener(final TextField towerTextField, final String flughafenName) {
        this.towerTextField = towerTextField;
        this.flughafenName = flughafenName;
    }

    @Override
    public void entryOperationFinished(final Notification source, final Operation operation,
            final List<? extends Serializable> entries) {

        System.out.println("Notification fired - Flughafenstatus geaendert");

        FlughafenData data = (FlughafenData) ((Entry) entries.get(0)).getValue();
        String flughafenName = data.getFlughafenName();
        String flughafenStatus = data.getStatus();

        if (this.flughafenName.equals(flughafenName)) {
            if (flughafenStatus.equals(CourseUtil.GESPERRT)) {
                towerTextField.setForeground(Color.RED);
            } else {
                towerTextField.setForeground(Color.GREEN);
            }

            towerTextField.setText("Flughafen " + flughafenName + " ist " + flughafenStatus);
        }
    }

}
