/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.flughafen;

import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.mozartspaces.capi3.RandomCoordinator;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsConstants.RequestTimeout;

public class Flugzeug extends JFrame implements ActionListener, WindowListener {

    private static final long serialVersionUID = 1L;

    // GUI
    // private JLabel flugNrLabel;
    // private TextField flugNrTextField;

    private final JButton landenButton;
    private final JButton startenButton;
    private final JButton fertigButton;
    // private JButton exitButton;
    private final TextField textFieldOben1;
    private final JTextArea messageTextArea;
    private final JScrollPane messageScrolli;
    private final Container container;

    // Flughafen
    private FlughafenData flughafen;

    // Flugzeug
    private String flugzeugZustand = CourseUtil.AM_BODEN;

    private int actualLandebahnNr;
    private final String flugzeugID;

    // XVSM
    private Capi capi;
    private MzsCore core;
    private ContainerReference flughafenInfo;
    private ContainerReference landebahnen;

    public Flugzeug(final String flughafenName, final String flugzeugID) {
        URI spaceURI = null;

        flughafen = new FlughafenData(flughafenName);
        this.flugzeugID = flugzeugID;

        container = getContentPane();
        setTitle("Flugzeug " + flugzeugID + " @ Flughafen" + flughafenName);
        setLocation(0, 0);
        setSize(600, 300);
        container.setLayout(new GridLayout(2, 1));

        JPanel panelOben = new JPanel();
        panelOben.setLayout(new GridLayout(3, 1));

        textFieldOben1 = new TextField();
        textFieldOben1.setText("AM BODEN");
        textFieldOben1.setFont(new Font(null, Font.PLAIN, 24));

        JPanel panelOben2 = new JPanel();
        panelOben2.setLayout(new GridLayout(1, 2));

        startenButton = new JButton();
        startenButton.setText("STARTEN");
        startenButton.setFont(new Font(null, Font.BOLD, 24));
        panelOben2.add(startenButton);
        startenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                startenButton_actionPerformed(e);
            }
        });

        landenButton = new JButton();
        landenButton.setText("LANDEN");
        landenButton.setFont(new Font(null, Font.BOLD, 24));
        panelOben2.add(landenButton);
        landenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                landenButton_actionPerformed(e);
            }
        });

        fertigButton = new JButton();
        fertigButton.setText("FERTIG");
        fertigButton.setFont(new Font(null, Font.BOLD, 24));
        fertigButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                fertigButton_actionPerformed(e);
            }
        });

        panelOben.add(textFieldOben1);
        panelOben.add(panelOben2);
        panelOben.add(fertigButton);

        container.add(panelOben);

        messageTextArea = new JTextArea();
        messageTextArea.setFont(new Font(null, Font.PLAIN, 20));

        messageScrolli = new JScrollPane();
        messageScrolli.getViewport().add(messageTextArea);

        container.add(messageScrolli);

        addWindowListener(this);
        setVisible(true);

        try {
            // Create an embedded space and construct a Capi instance for it
            core = DefaultMzsCore.newInstance(0);
            this.capi = new Capi(core);

            spaceURI = new URI(CourseUtil.SITE_URI);
        } catch (Exception e) {
            System.out.println("Fehler beim aufbauen der Connection");
            System.exit(1);
        }

        // get container with info about all airports and the plane's airport
        try {
            this.flughafenInfo = capi.lookupContainer(CourseUtil.FLUGHAFEN_INFO, spaceURI,
                    MzsConstants.RequestTimeout.ZERO, null);
        } catch (MzsCoreException e) {
            System.out.println("Fehler: Container " + CourseUtil.FLUGHAFEN_INFO + " existiert nicht.");
            System.exit(1);
        }

        try {
            this.landebahnen = capi.lookupContainer(flughafen.getFlughafenName(), spaceURI,
                    MzsConstants.RequestTimeout.ZERO, null);
            System.out.println("Container " + flughafen.getFlughafenName() + " found.");
        } catch (MzsCoreException e) {
            System.out.println("Fehler: Container " + flughafen.getFlughafenName() + " existiert nicht.");
            System.exit(1);
        }

        msg("Flugzeug " + flugzeugID + " ist am Boden und kann starten");
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
        System.out.println("ciao");
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

    void startenButton_actionPerformed(final ActionEvent e) {
        String info = flugzeugID;
        System.out.println("STARTEN BUTTON PRESSED: " + info);
        if (info == null || info.length() == 0)
            return;

        if (!flugzeugZustand.equals(CourseUtil.AM_BODEN)) {
            msg("Flugzeug ist nicht am Boden; kann nicht starten");
            return;
        }

        msg("Versuche zu Starten");

        try {
            // read all entries in the Landebahnen container
            ArrayList<Selector> selectors = new ArrayList<Selector>();
            selectors.add(RandomCoordinator.newSelector(MzsConstants.Selecting.COUNT_ALL));
            ArrayList<Serializable> entries = this.capi.read(landebahnen, selectors, RequestTimeout.ZERO, null);
            System.out.println(entries.size());

            for (Serializable entry : entries) {
                LandebahnData data = (LandebahnData) entry;
                int landebahnNr = data.getLandebahnNr();
                String landebahnStatus = data.getStatus();
                String flugNr = data.getFlugNr();
                System.out.println(data.getLandebahnNr() + " " + data.getStatus());

                if (!landebahnStatus.equals(CourseUtil.FREI)) {
                    // Landebahn ist nicht frei
                    msg("Landebahn " + landebahnNr + " ist besetzt von " + flugNr + "/" + landebahnStatus);
                    continue;
                } else {
                    // Landebahn ist frei
                    msg("Landebahn ist frei");
                    flughafen = CourseUtil.readFlughafenInfo(capi, flughafenInfo, null, flughafen.getFlughafenName());
                    msg("Tower Info = " + flughafen.getStatus());
                    if (!flughafen.getStatus().equals(CourseUtil.GEOEFFNET)) {
                        // Tower nicht OK
                        msg("Flughafen gesperrt");
                        continue;
                    } else {
                        // Tower OK
                        try {
                            CourseUtil.writeLandebahnInfo(capi, landebahnen, null, new LandebahnData(landebahnNr,
                                    CourseUtil.START, this.flugzeugID));

                            msg("Erfolgreich gestartet");

                            this.flugzeugZustand = new String(CourseUtil.STARTEND);
                            actualLandebahnNr = landebahnNr;

                            textFieldOben1.setText("STARTEND AUF BAHN " + landebahnNr);

                            return; // alles ok
                        } catch (Exception e3) {
                            msg("Darf jetzt nicht starten");
                            continue;
                        }
                    }
                }

            }

            msg("bitte nocheinmal probieren!!");
            return;

        } catch (Exception e6) {
            System.out.println("Exception = " + e6);
        }
    }

    void landenButton_actionPerformed(final ActionEvent e) {
        String info = flugzeugID;
        System.out.println("LANDEN BUTTON PRESSED:" + info);
        if (info == null || info.length() == 0)
            return;

        if (!flugzeugZustand.equals(CourseUtil.IN_DER_LUFT)) {
            msg("Flugzeug ist nicht in der Luft; kann nicht landen\n");
            return;
        }

        msg("Versuche zu Landen");

        try {
            // read all entries in the Landebahnen container
            ArrayList<Selector> selectors = new ArrayList<Selector>();
            selectors.add(RandomCoordinator.newSelector(MzsConstants.Selecting.COUNT_ALL));
            ArrayList<Serializable> entries = this.capi.read(landebahnen, selectors, RequestTimeout.ZERO, null);

            msg("Anzahl der gelesenen Entries: " + entries.size());

            for (Serializable entry : entries) {
                LandebahnData data = (LandebahnData) entry;

                int landebahnNr = data.getLandebahnNr();
                String landebahnStatus = data.getStatus();
                String flugNr = data.getFlugNr();

                if (!landebahnStatus.equals(CourseUtil.FREI)) {
                    // Landebahn ist nicht frei
                    msg("Landebahn " + landebahnNr + " ist besetzt von " + flugNr + "/" + landebahnStatus);
                    continue;
                } else {
                    // Landebahn ist frei
                    msg("Landebahn ist frei");
                    flughafen = CourseUtil.readFlughafenInfo(capi, flughafenInfo, null, flughafen.getFlughafenName());
                    msg("Tower Info = " + flughafen.getStatus());
                    if (!flughafen.getStatus().equals(CourseUtil.GEOEFFNET)) {
                        // Tower nicht OK
                        msg("Flughafen gesperrt");
                        continue;
                    } else {
                        // Tower OK
                        try {
                            CourseUtil.writeLandebahnInfo(capi, landebahnen, null, new LandebahnData(landebahnNr,
                                    CourseUtil.LANDUNG, this.flugzeugID));

                            msg("Erfolgreich gelandet");

                            this.flugzeugZustand = new String(CourseUtil.LANDEND);
                            actualLandebahnNr = landebahnNr;

                            textFieldOben1.setText("LANDEND AUF BAHN " + landebahnNr);

                            return; // alles ok
                        } catch (Exception e3) {
                            msg("Darf jetzt nicht landen");
                            continue;
                        }
                    }
                }
            }

            msg("bitte nocheinmal probieren!!");
            return;
        } catch (Exception e6) {
            System.out.println("Exception = " + e6);
        }
    }

    void fertigButton_actionPerformed(final ActionEvent e) {
        System.out.println("FERTIG BUTTON PRESSED");

        if (flugzeugZustand.equals(CourseUtil.STARTEND) || flugzeugZustand.equals(CourseUtil.LANDEND)) {
            try {
                // clear runway
                CourseUtil.writeLandebahnInfo(capi, landebahnen, null, new LandebahnData(this.actualLandebahnNr,
                        CourseUtil.FREI, ""));

                msg("Landebahn " + actualLandebahnNr + " freigegeben");
            } catch (Exception e8) {
                System.out.println("Exception = " + e8);
                return;
            }

            if (flugzeugZustand.equals(CourseUtil.STARTEND)) {
                flugzeugZustand = new String(CourseUtil.IN_DER_LUFT);
                textFieldOben1.setText(CourseUtil.IN_DER_LUFT);
            } else {
                flugzeugZustand = new String(CourseUtil.AM_BODEN);
                textFieldOben1.setText(CourseUtil.AM_BODEN);
            }
        } else {
            msg("Flugzeug ist weder startend noch landend");
        }
    }

    void exitButton_actionPerformed(final ActionEvent e) {
        System.out.println("EXIT BUTTON PRESSED: bye bye");
        System.exit(0);
    }

    private void msg(final String m) {
        messageTextArea.append(m);
        messageTextArea.append("\n");
    }

    public static void main(final String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: Flugzeug <FlugzeugID> <FlughafenName>");
            System.exit(-1);
        }
        new Flugzeug(args[1], args[0]);
    }
}
