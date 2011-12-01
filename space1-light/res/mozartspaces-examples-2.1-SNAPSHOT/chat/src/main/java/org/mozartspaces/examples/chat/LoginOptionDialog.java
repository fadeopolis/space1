/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.chat;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * The login dialog.
 *
 * @author Christian Schreiber (original version)
 * @author Tobias Doenz (adapted for MozartSpaces 2)
 */
public class LoginOptionDialog extends JDialog {

	private static final long serialVersionUID = -792676733059191486L;

	private final JTextField nicknameTextField = new JTextField("chris");
	private final JTextField urlTextField = new JTextField("xvsm://localhost:4321");

	private URI url = null;
	private String nickname = null;

	public LoginOptionDialog() {
	    this.setTitle("XVSM Demo Chat: Login");
		this.setSize(320, 180);
		this.setMinimumSize(new Dimension(320, 180));
		this.setLayout(new GridBagLayout());

		GridBagConstraints a = new GridBagConstraints();
		a.fill = GridBagConstraints.HORIZONTAL;
		a.insets = new Insets(2, 2, 2, 2);

		a.gridx = 0;
		a.gridy = 0;
		a.gridwidth = 2;
		a.gridheight = 3;
		JLabel txtLabel = new JLabel(
				"<html>Please specify your nickname and the URI<br>"
						+ "of the MozartSpaces instance.<br>"
						+ "To use the embedded core leave the URI blank."
						+ "<br><br></html>");

		this.add(txtLabel, a);
		a.gridwidth = 1;
		a.gridheight = 1;
		a.gridx = 0;
		a.gridy = 3;
		this.add(new JLabel("Nickname:"), a);

		a.gridx = 1;
		a.weightx = 100;
		this.add(this.nicknameTextField, a);

		a.gridx = 0;
		a.gridy = 4;
		a.weightx = 0;
		this.add(new JLabel("Space URI:"), a);

		a.gridx = 1;
		a.weightx = 100;
		this.add(this.urlTextField, a);

		// login Button
		JButton loginButton = new JButton();
		Action action = new AbstractAction("Login") {
			private static final long serialVersionUID = 1520434980280739188L;

			public void actionPerformed(final ActionEvent evt) {
				if (!urlTextField.getText().equals("")) {
					try {
						url = new URI(urlTextField.getText());
					} catch (URISyntaxException e) {
						JOptionPane.showMessageDialog(null,
								"The url is invalid.\n"
										+ "Please enter a valid URL e.g.\n"
										+ "\"xvsm://localhost:9876\"",
								"Ivalid URL!", JOptionPane.OK_OPTION);
						return;
					}
				}
				if (nicknameTextField.getText().equals("")) {
					JOptionPane.showMessageDialog(null,
							"Please enter a nick name.", "Nickname required!",
							JOptionPane.OK_OPTION);
					return;
				}
				nickname = nicknameTextField.getText();
				setVisible(false);
			}
		};
		loginButton.setAction(action);

		a.fill = 0;
		a.anchor = GridBagConstraints.EAST;
		a.gridx = 1;
		a.gridy = 5;
		a.weightx = 0;
		this.add(loginButton, a);
		this.getRootPane().setDefaultButton(loginButton);
	}

	public URI getUrl() {
		return this.url;
	}

	public String getNickname() {
		return this.nickname;
	}
}
