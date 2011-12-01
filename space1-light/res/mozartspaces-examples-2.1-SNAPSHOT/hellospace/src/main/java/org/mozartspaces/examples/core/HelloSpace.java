/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.core;

import java.util.ArrayList;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCore;

/**
 * A simple "Hello, space!" example with the MozartSpaces core. First a core
 * instance with an embedded space is created and a container in that space,
 * then an entry is written into that container and read afterwards, before the
 * container is destroyed and the core is shut down.
 */
public class HelloSpace {

    public static void main(final String[] args) throws Exception {
        System.out.println();
        System.out.println("MozartSpaces: simple 'Hello, space!' with synchronous core interface");

        // create an embedded space and construct a Capi instance for it
        MzsCore core = DefaultMzsCore.newInstance();
        Capi capi = new Capi(core);
        // create a container
        ContainerReference container = capi.createContainer();
        // write an entry to the container
        capi.write(container, new Entry("Hello, space!"));
        System.out.println("Entry written");
        // read an entry from the container
        ArrayList<String> resultEntries = capi.read(container);
        System.out.println("Entry read: " + resultEntries.get(0));
        // destroy the container
        capi.destroyContainer(container, null);

        // shutdown the core
        core.shutdown(true);
    }
}
