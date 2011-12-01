/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.pco;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URI;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsConstants.RequestTimeout;

/**
 * The Producer.
 *
 * @author unascribed (original version from 2001-10-02)
 * @author Filip Hianik (adapted for MozartSpaces 2)
 * @author Tobias Doenz (adapted for MozartSpaces 2)
 */
public class Producer extends Frame implements ActionListener, WindowListener {

    private static final long serialVersionUID = 1L;

    private final Label l1;
    private final TextField tf1;
    private Capi capi;
    private ContainerReference cref;

    public Producer(final URI space) {

        // GUI
        setLayout(new FlowLayout());
        l1 = new Label("Your Message:");
        l1.setFont(new Font(null, Font.BOLD | Font.ITALIC, 24));
        add(l1);
        tf1 = new TextField("", 32);
        tf1.addActionListener(this);
        tf1.setFont(new Font(null, Font.BOLD, 24)); // use default font
        add(tf1);
        addWindowListener(this);
        setSize(600, 160);
        setTitle("Producer");

        try {
            // Create an embedded space and construct a Capi instance for it
            MzsCore core = DefaultMzsCore.newInstance();
            capi = new Capi(core);

            // Ensure that the container "products" exists
            cref = Util.getOrCreateNamedContainer(space, "products", capi);
        } catch (MzsCoreException ex) {
            ex.printStackTrace();
        }

        setVisible(true);
    }

    public static void main(final String[] args) {
        System.out.println("Welcome to the XVSM Producer");
        if (args.length != 1) {
            System.out.println("Usage: java Producer <space-URI>");
            return;
        }
        URI space = URI.create(args[0]);
        new Producer(space);
    }

    // ActionListener
    public void actionPerformed(final ActionEvent e) {
        String msg = tf1.getText();
        tf1.invalidate();
        tf1.setText("");
        this.validate();

        try {
            Entry entry = new Entry(msg);
            capi.write(cref, RequestTimeout.TRY_ONCE, null, entry);
        } catch (MzsCoreException ex) {
            ex.printStackTrace();
        }

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
