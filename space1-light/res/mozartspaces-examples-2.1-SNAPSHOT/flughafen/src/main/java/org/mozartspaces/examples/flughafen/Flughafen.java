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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URI;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.mozartspaces.capi3.ContainerNotFoundException;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.RandomCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.MzsConstants.RequestTimeout;

public class Flughafen extends JFrame implements ActionListener, WindowListener {

    private static final long serialVersionUID = 1L;

    // GUI
    private final JButton oeffnenButton;
    private final JButton sperrenButton;
    private final JButton exitButton;
    private final Container container;

    // Flughafen
    private final FlughafenData flughafen;

    // XVSM
    private Capi capi;
    private MzsCore core;
    private ContainerReference flughafenInfo;
    private ContainerReference landebahnen;
    private URI spaceURI;

    public Flughafen(final String flughafenName, final int landebahnenAnzahl) {

        if (landebahnenAnzahl <= 0) {
            throw new IllegalArgumentException("number of runways: " + landebahnenAnzahl);
        }

        flughafen = new FlughafenData(landebahnenAnzahl, CourseUtil.GESPERRT, flughafenName);

        System.out.println(flughafen.getFlughafenName() + " with " + flughafen.getNLandebahnen() + " Landebahnen");
        container = getContentPane();
        setTitle("Tower Flughafen " + flughafenName);
        setLocation(0, 0);
        setSize(260, 200);
        container.setLayout(new GridLayout(3, 1));

        oeffnenButton = new JButton();
        oeffnenButton.setFont(new Font(null, Font.BOLD, 24));
        oeffnenButton.setText("OEFFNEN");
        oeffnenButton.setBackground(Color.GREEN);
        container.add(oeffnenButton);
        oeffnenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                oeffnenButton_actionPerformed(e);
            }
        });

        sperrenButton = new JButton();
        sperrenButton.setText("SPERREN");
        sperrenButton.setFont(new Font(null, Font.BOLD, 24));
        sperrenButton.setBackground(Color.RED);
        container.add(sperrenButton);
        sperrenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                sperrenButton_actionPerformed(e);
            }
        });

        exitButton = new JButton();
        exitButton.setText("EXIT");
        exitButton.setFont(new Font(null, Font.BOLD, 24));
        container.add(exitButton);
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                exitButton_actionPerformed(e);
            }
        });

        addWindowListener(this);
        setVisible(true);

        try {
            // Create an embedded space and construct a Capi instance for it
            core = DefaultMzsCore.newInstance(0);
            this.capi = new Capi(core);

            spaceURI = new URI(CourseUtil.SITE_URI);

            // lookup/create container with info about all airports and this
            // airport
            try {
                this.flughafenInfo = capi.lookupContainer(CourseUtil.FLUGHAFEN_INFO, spaceURI,
                        RequestTimeout.TRY_ONCE, null);
            } catch (MzsCoreException e) {
                System.out.println(CourseUtil.FLUGHAFEN_INFO + " not found and will be created.");
                ArrayList<Coordinator> obligatoryCoords = new ArrayList<Coordinator>();
                obligatoryCoords.add(new KeyCoordinator());
                this.flughafenInfo = capi.createContainer(CourseUtil.FLUGHAFEN_INFO, spaceURI,
                        MzsConstants.Container.UNBOUNDED, obligatoryCoords, new ArrayList<Coordinator>(), null);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }

            TransactionReference tx = capi.createTransaction(RequestTimeout.INFINITE, this.spaceURI);
            CourseUtil.writeFlughafenInfo(capi, flughafenInfo, tx, flughafen);

            try {
                this.landebahnen = capi.lookupContainer(flughafen.getFlughafenName(), spaceURI, RequestTimeout.ZERO,
                        null);
                if (this.landebahnen != null)
                    capi.destroyContainer(this.landebahnen, tx);
            } catch (ContainerNotFoundException e) {
                System.out.println("Container " + flughafen.getFlughafenName() + " not found and will be created.");
            } catch (MzsCoreException e) {
                e.printStackTrace();
            }

            ArrayList<Coordinator> obligatoryCoords = new ArrayList<Coordinator>();
            obligatoryCoords.add(new KeyCoordinator());

            ArrayList<Coordinator> optionalCoords = new ArrayList<Coordinator>();
            optionalCoords.add(new RandomCoordinator());
            this.landebahnen = capi.createContainer(flughafen.getFlughafenName(), spaceURI,
                    MzsConstants.Container.UNBOUNDED, obligatoryCoords, optionalCoords, tx);

            for (int i = 0; i < flughafen.getNLandebahnen(); i++) {
                CourseUtil.writeLandebahnInfo(capi, landebahnen, tx, new LandebahnData(i, CourseUtil.FREI, ""));
            }

            capi.commitTransaction(tx);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    // ActionListener
    public void actionPerformed(final ActionEvent e) {
        System.out.println("GUI: actionPerformed: Event Fired: " + e.getActionCommand());
    }

    // WindowListener methods below
    public void windowClosing(final WindowEvent e) {
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

    void oeffnenButton_actionPerformed(final ActionEvent e) {
        TransactionReference tx = null;
        System.out.println("OEFFNEN BUTTON PRESSED");
        flughafen.setStatus(CourseUtil.GEOEFFNET);

        try {
            tx = capi.createTransaction(MzsConstants.RequestTimeout.INFINITE, this.spaceURI);
            CourseUtil.writeFlughafenInfo(capi, flughafenInfo, tx, flughafen);
            capi.commitTransaction(tx);
        } catch (Exception ex) {
            try {
                capi.rollbackTransaction(tx);
            } catch (Exception ex2) {

            }
        }
    }

    void sperrenButton_actionPerformed(final ActionEvent e) {
        TransactionReference tx = null;
        System.out.println("SPERREN BUTTON PRESSED");
        flughafen.setStatus(CourseUtil.GESPERRT);

        try {
            tx = capi.createTransaction(MzsConstants.RequestTimeout.INFINITE, this.spaceURI);
            CourseUtil.writeFlughafenInfo(capi, flughafenInfo, tx, flughafen);
            capi.commitTransaction(tx);
        } catch (Exception ex) {
            try {
                capi.rollbackTransaction(tx);
            } catch (Exception ex2) {

            }
        }
    }

    void exitButton_actionPerformed(final ActionEvent e) {
        System.out.println("EXIT BUTTON PRESSED: bye bye");
        System.exit(0);
    }

    public static void main(final String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: Flughafen <name> <nLandebahnen>");
            System.exit(-1);
        }
        new Flughafen(args[0], Integer.parseInt(args[1]));
    }

}
