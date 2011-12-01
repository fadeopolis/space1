/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.chat;

import java.io.Serializable;

/**
 * Tuple of nick name and chat message, written to the space.
 *
 * @author Tobias Doenz
 */
public final class ChatEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nickName;
    private final String message;

    public ChatEntry(final String nickName, final String message) {
        this.nickName = nickName;
        this.message = message;
    }

    /**
     * @return the user
     */
    public String getNickName() {
        return nickName;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }


}
