/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.FifoCoordinator.FifoSelector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.MzsConstants.RequestTimeout;

/**
 * A simple "Hello, space!" example with the MozartSpaces core. First a core
 * instance with an embedded space is created and a container in that space,
 * then an entry is written into that container and read afterwards, before the
 * container is destroyed and the core is shut down.
 */
public class HelloSpaceTx {

    public static void main(final String[] args) throws Exception {
        // log only warnings and errors (better use logback.xml for logging configuration)
//        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
//        root.setLevel(Level.WARN); // default is DEBUG

        System.out.println();
        System.out.println("MozartSpaces: transactional 'Hello, space!' with synchronous core interface");

        // create an embedded space and construct a Capi instance for it
        MzsCore core = DefaultMzsCore.newInstance();
        Capi capi = new Capi(core);
        // create a transaction with a timeout of 5000 milliseconds
        TransactionReference transaction = capi.createTransaction(5000, null);

        // create a container "c1" with the maximum size of 10 entries and FIFO
        // coordination inside the transaction
        List<FifoCoordinator> coords = Collections.singletonList(new FifoCoordinator());
        ContainerReference container = capi.createContainer("c1", null, 10, coords, null, transaction);

        // write an entry to the container using the default timeout and the
        // transaction
        capi.write(container, RequestTimeout.DEFAULT, transaction, new Entry("Hello, space!"));
        System.out.println("Entry written");

        // commit the transaction
        capi.commitTransaction(transaction);

        // read all entries from the container using the FIFO selector and a
        // 1000 milliseconds timeout
        List<FifoSelector> selectors = Collections.singletonList(FifoCoordinator.newSelector());
        ArrayList<String> resultEntries = capi.read(container, selectors, 1000, null);
        System.out.println("Entry read: " + resultEntries.get(0));

        // destroy the container
        capi.destroyContainer(container, null);

        // shutdown the core
        core.shutdown(true);
    }
}
