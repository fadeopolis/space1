/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.pco;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsConstants.Selecting;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

/**
 * The Observer.
 *
 * @author unascribed (original version from 2007-10-02)
 * @author Filip Hianik (adapted for MozartSpaces 2)
 * @author Tobias Doenz (adapted for MozartSpaces 2)
 */
public class Observer extends Frame implements ActionListener, WindowListener, NotificationListener {

    private static final long serialVersionUID = 1L;

    private final TextArea textArea;

    public Observer(final URI space) {

        // GUI
        setLayout(new FlowLayout());
        textArea = new TextArea("", 8, 32, TextArea.SCROLLBARS_VERTICAL_ONLY);
        textArea.setFont(new Font(null, Font.BOLD, 24)); // use default font
        add(textArea);
        setSize(600, 330);
        addWindowListener(this);
        setVisible(true);
        setTitle("Observer");

        try {
            // Create an embedded space and construct a Capi instance for it
            MzsCore core = DefaultMzsCore.newInstance();
            Capi capi = new Capi(core);

            // Ensure that the container "products" exists
            ContainerReference cref = Util.getOrCreateNamedContainer(space, "products", capi);

            // Create notification
            NotificationManager notifManager = new NotificationManager(core);
            Set<Operation> operations = new HashSet<Operation>();
            operations.add(Operation.WRITE);
            notifManager.createNotification(cref, this, operations, null, null);

            // Read all existing entries
            ArrayList<Selector> selectors = new ArrayList<Selector>();
            selectors.add(FifoCoordinator.newSelector(Selecting.COUNT_ALL));
            ArrayList<String> resultEntries = capi.read(cref, selectors, RequestTimeout.INFINITE, null);

            // output
            for (String entry : resultEntries) {
                System.out.println(entry);
                textArea.append(entry);
                textArea.append("\n");
            }

        } catch (MzsCoreException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    // Callback method (NotificationListener)
    public void entryOperationFinished(final Notification arg0, final Operation arg1,
            final List<? extends Serializable> entries) {
        for (Serializable entry : entries) {
            String message = ((Entry) entry).getValue().toString();
            System.out.println(message);
            textArea.append(message);
            textArea.append("\n");
        }

    }

    public static void main(final String[] args) {
        System.out.println("Welcome to the XVSM Consumer");
        if (args.length != 1) {
            System.out.println("Usage: java Observer <space-URI>");
            return;
        }
        URI space = URI.create(args[0]);
        new Observer(space);
    }

    // ActionListener
    public void actionPerformed(final ActionEvent e) {
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

}
