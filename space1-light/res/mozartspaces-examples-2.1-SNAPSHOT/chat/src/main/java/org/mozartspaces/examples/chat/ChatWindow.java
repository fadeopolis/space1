/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.chat;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.FifoCoordinator.FifoSelector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.CapiUtil;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsConstants.Selecting;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

/**
 * A small chat application demonstrating the FIFO (First In First Out)
 * coordinator and the notifications mechanism of MozartSpaces.
 *
 * @author Christian Schreiber (original version)
 * @author Tobias Doenz (adapted for MozartSpaces 2)
 */
public class ChatWindow extends JFrame {

    /**
     * Auto generated serial version uid.
     */
    private static final long serialVersionUID = 1L;

    private static final String CONTAINER_NAME = "chat";

    /**
     * This text area displays the chat messages.
     */
    private JTextArea outputTextArea;

    /**
     * This text area is used for entring text.
     */
    private JTextArea inputTextArea;

    /**
     * The button which can be used the write the content of
     * {@link ChatWindow#inputTextArea} into the container.
     */
    private JButton sendButton;

    /**
     * The nick name of the logged in user.
     */
    private String nickName;

    /**
     * The used MozartSpaces instance.
     */
    private Capi capi;

    private NotificationManager notifMgr;

    /**
     * The container which is used for communication.
     */
    private ContainerReference cref;

    /**
     * The uri of MozartSpaces. <code>null</code> if we use the embedded core.
     */
    private URI spaceURI = null;

    /**
     * Creates a new ChatWindow.
     *
     */
    public ChatWindow() {
        this.init();
    }

    /**
     * Creates a new ChatWindow.
     *
     * @param cref
     *            The container which shall be used for communication.
     * @param capi
     *            The MozartSpaces instance which shall be used.
     */
    public ChatWindow(final ContainerReference cref, final Capi capi) {
        this.cref = cref;
        this.capi = capi;
        this.init();
    }

    /**
     * Initialize the chat window.<br>
     * The method asks the user for the nick name to use, calls
     * {@link ChatWindow#initSpace()} and {@link ChatWindow#initNotifications()}
     * and enables the content of the window.
     *
     */
    public void init() {
        this.initChatWindow();
        this.inputTextArea.setEnabled(false);
        this.sendButton.setEnabled(false);

        LoginOptionDialog loginDialog = new LoginOptionDialog();
        loginDialog.setLocationRelativeTo(this);
        loginDialog.setModal(true);
        loginDialog.setVisible(true);
        this.nickName = loginDialog.getNickname();
        if (this.nickName == null || this.nickName.equals("")) {
            System.exit(0);
        }
        this.setTitle(this.getTitle() + " -- " + this.nickName);
        this.spaceURI = loginDialog.getUrl();

        this.initSpace();
        this.initNotifications();

        this.inputTextArea.setEnabled(true);
        this.sendButton.setEnabled(true);
    }

    /**
     * Initializes the MozartSpaces instance.<br>
     * If {@link ChatWindow#capi} is <code>null</code> a new instance will be
     * created otherwise the existing one will be used. If
     * {@link ChatWindow#cref} is <code>null</code> a new container will be
     * created otherwise the existing container will be used. After the
     * initialization the content of the container will be read and displayed in
     * the {@link ChatWindow#outputTextArea}.
     */
    public void initSpace() {
        try {
            MzsCore core = DefaultMzsCore.newInstance(0);
            this.capi = new Capi(core);
            this.notifMgr = new NotificationManager(core);
            if (this.cref == null) {
                List<FifoCoordinator> coords = Arrays.asList(new FifoCoordinator());
                this.cref = CapiUtil.lookupOrCreateContainer(CONTAINER_NAME, this.spaceURI, coords, null, this.capi);
            }
            // Read the current content from the space
            List<FifoSelector> selectors = Arrays.asList(FifoCoordinator.newSelector(Selecting.COUNT_ALL));
            ArrayList<ChatEntry> entries = capi.read(this.cref, selectors, RequestTimeout.TRY_ONCE, null);
            for (ChatEntry entry : entries) {
                addMessageToOutput(entry.getNickName(), entry.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes a message into the container, together with the nick name in a
     * <code>ChatEntry</code> instance.
     *
     * @param msg
     *            the message to write.
     */
    private void sendMessage(final String msg) {
        try {
            ChatEntry entry = new ChatEntry(nickName, msg);
            capi.write(cref, new Entry(entry));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Registers a {@link NotificationListener} to the container.<br>
     * If the notification fires the new new entry will be displayed in
     * {@link ChatWindow#outputTextArea}.
     */
    public void initNotifications() {
        NotificationListener notifListener = new NotificationListener() {
            @Override
            public void entryOperationFinished(final Notification notification, final Operation operation, final List<? extends Serializable> entries) {
                Entry entry = (Entry) CapiUtil.getSingleEntry(entries);
                ChatEntry chatEntry = (ChatEntry) entry.getValue();
                addMessageToOutput(chatEntry.getNickName(), chatEntry.getMessage());
            }
        };
        try {
            notifMgr.createNotification(cref, notifListener, Operation.WRITE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Appends <code>name</code> and <code>message</code> to
     * {@link ChatWindow#outputTextArea}.<br>
     * The information will be appended with the following format:
     * <code>name + ":  " + message + "\n"</code>.
     *
     * @param name
     *            the nick name of the sender of <code>message</code>
     * @param message
     *            the sent message.
     */
    private void addMessageToOutput(final String name, final String message) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                // TODO Format the output nicer (colors, html, ...)
                outputTextArea.append(name + ":  " + message + "\n");
            }
        });
    }

    /**
     * Initializes the chat window.<br>
     * This method creates and configures all GUI components.
     */
    private void initChatWindow() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("XVSM Demo Chat");
        this.setSize(500, 300);
        this.setMinimumSize(new Dimension(500, 300));

        JPanel inputPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        inputPanel.setLayout(gbl);

        // configure the input text area
        GridBagConstraints gbcl = new GridBagConstraints();
        gbcl.fill = GridBagConstraints.BOTH;
        gbcl.gridx = 0;
        gbcl.gridy = 0;
        gbcl.gridwidth = 3;
        gbcl.gridheight = 1;
        gbcl.weightx = 1.0;
        gbcl.weighty = 1.0;
        this.inputTextArea = new JTextArea();

        // create the action for sending the message.
        AbstractAction sendMessage = new AbstractAction() {
            private static final long serialVersionUID = -446129333599863205L;
            public void actionPerformed(final ActionEvent e) {
                String txt = inputTextArea.getText();
                if (txt.length() > 0) {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            inputTextArea.setText("");
                        }
                    });
                    sendMessage(txt);
                }
            }
        };

        // control + enter sends the message
        this.inputTextArea.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK), "sendInput");
        this.inputTextArea.getActionMap().put("sendInput", sendMessage);

        JScrollPane inputScrollPane = new JScrollPane(this.inputTextArea);
        gbl.setConstraints(inputScrollPane, gbcl);
        inputPanel.add(inputScrollPane);

        // configure the send button
        GridBagConstraints gbcr = new GridBagConstraints();
        gbcr.fill = GridBagConstraints.BOTH;
        gbcr.gridx = 3;
        gbcr.gridy = 0;
        gbcr.gridwidth = 1;
        gbcr.gridheight = 1;
        gbcr.weightx = 0;
        gbcr.weighty = 0;
        this.sendButton = new JButton("Send");
        gbl.setConstraints(sendButton, gbcr);
        inputPanel.add(sendButton);
        sendButton.addActionListener(sendMessage);

        // configure the output text area
        this.outputTextArea = new JTextArea();
        this.outputTextArea.setEditable(false);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, new JScrollPane(this.outputTextArea),
                inputPanel);

        splitPane.setDividerLocation(200);
        this.getContentPane().add(splitPane);

        // Menu
        JMenuBar menuBar = new JMenuBar();
        JMenu chatMenu = new JMenu("Chat");
        menuBar.add(chatMenu);
        this.setJMenuBar(menuBar);

        Action exitAction = new AbstractAction("Close") {
            private static final long serialVersionUID = 5552593467961007638L;
            public void actionPerformed(final ActionEvent e) {
                try {
                    notifMgr.shutdown();
                    capi.shutdown(spaceURI);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                System.exit(0);
            }
        };
        chatMenu.add(exitAction);

        // calc the position
        this.setLocation((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 - this.getWidth() / 2,
                (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2 - this.getHeight() / 2);

        this.setVisible(true);
    }

    /**
     * The main entry point of the program.
     *
     * @param args
     *            Command line arguments.
     */
    public static void main(final String[] args) {
        new ChatWindow();
    }
}
