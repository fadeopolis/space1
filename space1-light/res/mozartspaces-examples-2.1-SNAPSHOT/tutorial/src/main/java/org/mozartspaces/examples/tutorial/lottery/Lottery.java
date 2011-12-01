/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.tutorial.lottery;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import org.mozartspaces.capi3.RandomCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;

/*
 * Lottery (RandomCoordinator).
 * This example writes the numbers (balls) from 1 to 45 to the container
 * and then takes 6 + 1 numbers (balls) out of the container randomly.
 *
 * @author Formanek, Keszthelyi, Efler
 */
public class Lottery {

    public static void main(final String[] args) throws MzsCoreException, URISyntaxException {
        /* Create new Capi instance */
        MzsCore core = DefaultMzsCore.newInstance();
        Capi capi = new Capi(core);

        /*
         * Create new Container using RandomCoordinator, RandomSelector and a
         * container-size limit of 45
         */
        ContainerReference cref = capi.createContainer("lottery", null, 45, Arrays.asList(new RandomCoordinator()),
                null, null);

        /* Write the 45 numbers to the container */
        for (int i = 1; i < 46; i++) {
            /* Create a new Entry */
            Entry entry = new Entry(i, RandomCoordinator.newCoordinationData());
            /* Write the entry to the container */
            capi.write(cref, 0, null, entry);
        }

        /* Take 6 + 1 numbers from the container */
        System.out.print("Regular Numbers: ");
        for (int j = 0; j < 7; j++) {
            /* Take an entry from the container using the RandomSelector */
            ArrayList<Serializable> readEntries = capi.take(cref, Arrays.asList(RandomCoordinator.newSelector()),
                    MzsConstants.RequestTimeout.INFINITE, null);

            System.out.print(readEntries.get(0));
            if (j == 5) {
                System.out.print("\nBonus: ");
            } else if (j < 6) {
                System.out.print(", ");
            }
        }

        /* Remove container from space */
        capi.destroyContainer(cref, null);

        capi.shutdown(null);
    }
}
