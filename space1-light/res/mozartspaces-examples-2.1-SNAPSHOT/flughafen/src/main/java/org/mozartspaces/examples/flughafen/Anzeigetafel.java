/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.flughafen;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URI;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

public class Anzeigetafel extends JFrame implements ActionListener, WindowListener {

    private static final long serialVersionUID = 1L;

    // GUI
    private final TextField towerTextField;
    private final TextField landebahnNrTextFields[];
    private final TextField statusTextFields[];
    private final TextField flugNrTextFields[];
    private final Container container;

    // Flughafen
    private FlughafenData flughafen;

    // XVSM
    private Capi capi;
    private MzsCore core;
    private NotificationManager notifManager;
    private ContainerReference flughafenInfo;
    private ContainerReference landebahnen;

    public Anzeigetafel(final String flughafenName) {
        URI spaceURI = null;

        flughafen = new FlughafenData(flughafenName);

        container = getContentPane();
        setTitle("Anzeigetafel " + flughafen.getFlughafenName());
        setLocation(0, 0);
        setSize(510, 300);

        try {
            // Create an embedded space and construct a Capi instance for it
            core = DefaultMzsCore.newInstance(0);
            this.capi = new Capi(core);
            spaceURI = new URI(CourseUtil.SITE_URI);
        } catch (Exception e) {
            System.out.println("Fehler beim aufbauen der Connection");
            System.exit(1);
        }

        // get container with info about all airports and airport this display is for
        try {
            this.flughafenInfo = capi.lookupContainer(CourseUtil.FLUGHAFEN_INFO, spaceURI, RequestTimeout.ZERO, null);
        } catch (MzsCoreException e) {
            System.out.println("Fehler: Container " + CourseUtil.FLUGHAFEN_INFO + " existiert nicht.");
            System.exit(1);
        }

        try {
            this.landebahnen = capi.lookupContainer(flughafen.getFlughafenName(), spaceURI, RequestTimeout.ZERO, null);
        } catch (MzsCoreException e) {
            System.out.println("Fehler: Container " + flughafen.getFlughafenName() + " existiert nicht.");
            System.exit(1);
        }

        // get airport info
        System.out.println("Suche Flughafen " + flughafen.getFlughafenName());

        flughafen = CourseUtil.readFlughafenInfo(capi, flughafenInfo, null, flughafen.getFlughafenName());

        System.out.println("# Landebahnen = " + flughafen.getNLandebahnen());

        landebahnNrTextFields = new TextField[flughafen.getNLandebahnen()];
        statusTextFields = new TextField[flughafen.getNLandebahnen()];
        flugNrTextFields = new TextField[flughafen.getNLandebahnen()];

        container.setLayout(new GridLayout(flughafen.getNLandebahnen() + 1, 1));

        // Anzeige der Tower Info
        JPanel towerPanel = new JPanel();
        towerTextField = new TextField("Flughafen " + flughafen.getFlughafenName() + " ist " + flughafen.getStatus());
        towerTextField.setFont(new Font(null, Font.BOLD, 24));
        towerPanel.setBackground(Color.LIGHT_GRAY);
        towerPanel.add(towerTextField);
        container.add(towerPanel);

        if (flughafen.getStatus().equals(CourseUtil.GESPERRT)) {
            towerTextField.setForeground(Color.RED);
        } else {
            towerTextField.setForeground(Color.GREEN);
        }

        // Create notification for runways and airport info
        try {
            notifManager = new NotificationManager(core);
            notifManager.createNotification(this.landebahnen, new LandebahnenNotificationListener(statusTextFields,
                    flugNrTextFields), Operation.WRITE);
            notifManager.createNotification(this.flughafenInfo, new FlughafenNotificationListener(this.towerTextField,
                    flughafen.getFlughafenName()), Operation.WRITE);
        } catch (Exception e) {
            System.out.println("Failed to create Notification");
            System.exit(-1);
        }

        // read and display Landebahnen info
        for (int i = 0; i < flughafen.getNLandebahnen(); i++) {
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(1, 3));

            LandebahnData landebahn = CourseUtil.readLandebahnInfo(capi, landebahnen, null, i);

            // landebahnNr Text Field
            landebahnNrTextFields[i] = new TextField("" + i);
            landebahnNrTextFields[i].setFont(new Font(null, Font.PLAIN, 24));
            panel.add(landebahnNrTextFields[i]);

            // status Text Field
            statusTextFields[i] = new TextField(landebahn.getStatus());
            statusTextFields[i].setFont(new Font(null, Font.PLAIN, 24));
            panel.add(statusTextFields[i]);

            // flugNr Text Field
            flugNrTextFields[i] = new TextField(landebahn.getFlugNr());
            flugNrTextFields[i].setFont(new Font(null, Font.PLAIN, 24));
            panel.add(flugNrTextFields[i]);

            container.add(panel);
        }

        addWindowListener(this);
        setVisible(true);
    }

    // ActionListener
    public void actionPerformed(final ActionEvent e) {
    }

    // WindowListener methods below
    public void windowClosing(final WindowEvent e) {
        notifManager.shutdown();
        core.shutdown(true);
        System.exit(0);
    }

    public void windowClosed(final WindowEvent e) {
    }

    public void windowOpened(final WindowEvent e) {
    }

    public void windowIconified(final WindowEvent e) {
    }

    public void windowDeiconified(final WindowEvent e) {
    }

    public void windowActivated(final WindowEvent e) {
    }

    public void windowDeactivated(final WindowEvent e) {
    }

    public static void main(final String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: Anzeigetafel <flughafenName>");
            System.exit(-1);
        }
        new Anzeigetafel(args[0]);
    }

}

