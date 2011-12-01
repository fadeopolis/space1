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
import java.net.URI;
import java.util.ArrayList;

import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsTimeoutException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.MzsConstants.RequestTimeout;

/**
 * The Consumer.
 *
 * @author unascribed (original version from 2007-10-02)
 * @author Filip Hianik (adapted for MozartSpaces 2)
 * @author Tobias Doenz (adapted for MozartSpaces 2)
 */
public class Consumer extends Frame implements ActionListener, WindowListener {

    private static final long serialVersionUID = 1L;

    public Consumer(final URI space) {

        // GUI
        setLayout(new FlowLayout());
        TextArea textArea = new TextArea("", 8, 32, TextArea.SCROLLBARS_VERTICAL_ONLY);
        textArea.setFont(new Font(null, Font.BOLD, 24)); // use default font
        add(textArea);
        setSize(600, 330);
        addWindowListener(this);
        setVisible(true);
        setTitle("Consumer");

        try {
            // Create an embedded space and construct a Capi instance for it
            MzsCore core = DefaultMzsCore.newInstance();
            Capi capi = new Capi(core);

            // Ensure that the container "products" exists
            ContainerReference cref = Util.getOrCreateNamedContainer(space, "products", capi);

            System.out.println("entering consumption loop");

            ArrayList<String> entries;
            String message = null;
            for (;;) {
                // explicit TX to prevent possible loss of taken message when consumer is offline
                TransactionReference tx = capi.createTransaction(10000, space);

                // Take one entry
                try {
                    entries = capi.take(cref, FifoCoordinator.newSelector(), RequestTimeout.INFINITE, tx);
                } catch (MzsTimeoutException ex) {
                    System.out.println("transaction timeout. retry.");
                    continue;
                }
                message = entries.get(0);

                // output
                System.out.println(message);
                textArea.append(message);
                textArea.append("\n");

                capi.commitTransaction(tx);
                Thread.sleep(1000);
            }
        } catch (MzsCoreException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(final String[] args) {
        System.out.println("Welcome to the XVSM Consumer");
        if (args.length != 1) {
            System.out.println("Usage: java Consumer <space-URI>");
            return;
        }
        URI space = URI.create(args[0]);
        new Consumer(space);
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

