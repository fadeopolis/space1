/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.flughafen;

import java.awt.TextField;
import java.io.Serializable;
import java.util.List;

import org.mozartspaces.core.Entry;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.Operation;

public final class LandebahnenNotificationListener implements NotificationListener {

    private final TextField statusTextFields[];
    private final TextField flugNrTextFields[];

    LandebahnenNotificationListener(final TextField statusTextFields[], final TextField flugNrTextFields[]) {
        this.statusTextFields = statusTextFields;
        this.flugNrTextFields = flugNrTextFields;
    }

    @Override
    public void entryOperationFinished(final Notification source, final Operation operation,
            final List<? extends Serializable> entries) {
        System.out.println("Notification fired - Landebahnenstatus geaendert");

        LandebahnData data = (LandebahnData) ((Entry) entries.get(0)).getValue();
        int landebahnNr = data.getLandebahnNr();
        String landebahnStatus = data.getStatus();
        String flugNr = data.getFlugNr();

        statusTextFields[landebahnNr].setText(landebahnStatus);
        flugNrTextFields[landebahnNr].setText(flugNr);

    }
}
