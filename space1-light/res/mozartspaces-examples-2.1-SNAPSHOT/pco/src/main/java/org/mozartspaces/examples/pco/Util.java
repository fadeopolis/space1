/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.pco;

import java.net.URI;
import java.util.ArrayList;

import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsConstants.Container;
import org.mozartspaces.core.MzsConstants.RequestTimeout;

/**
 * Utility class.
 *
 * @author unascribed (original version from 2007-10-02)
 * @author Filip Hianik (adapted for MozartSpaces 2)
 * @author Tobias Doenz (adapted for MozartSpaces 2)
 */
public abstract class Util {

    /**
     * Gets or creates a named container (unbounded, FIFO coordination).
     *
     * @param space
     *            the space to use
     * @param containerName
     *            the name of the container
     * @param capi
     *            the interface to access the space (Core API)
     * @return the reference of the container
     * @throws MzsCoreException
     *             if getting or creating the container failed
     */
    public static ContainerReference getOrCreateNamedContainer(final URI space, final String containerName,
            final Capi capi) throws MzsCoreException {

        ContainerReference cref;
        try {
            // Get the Container
            System.out.println("Lookup container");
            cref = capi.lookupContainer(containerName, space, RequestTimeout.DEFAULT, null);
            System.out.println("Container found");
            // If it is unknown, create it
        } catch (MzsCoreException e) {
            System.out.println("Container not found, creating it ...");
            // Create the Container
            ArrayList<Coordinator> obligatoryCoords = new ArrayList<Coordinator>();
            obligatoryCoords.add(new FifoCoordinator());
            cref = capi.createContainer(containerName, space, Container.UNBOUNDED, obligatoryCoords, null, null);
            System.out.println("Container created");
        }
        return cref;
    }

}
