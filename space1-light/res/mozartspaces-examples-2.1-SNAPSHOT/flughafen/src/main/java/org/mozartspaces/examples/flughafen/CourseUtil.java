/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.flughafen;

import java.io.Serializable;
import java.util.ArrayList;

import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.RandomCoordinator;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;

public final class CourseUtil {

    public static final String SITE_URI = "xvsm://localhost:4242";
    public static final String FLUGHAFEN_INFO = "FlughafenInfo";
    public static final String FLUGHAFEN_SELECTOR = "FlughafenName";
    public static final String LANDEBAHN_SELECTOR = "LandebahnID";

    // Flughafen Status
    public static final String GESPERRT = "GESPERRT";
    public static final String GEOEFFNET = "GEOEFFNET";

    // Landebahn Status
    public static final String FREI = "FREI";
    public static final String LANDUNG = "LANDUNG";
    public static final String START = "START";

    public static final String AM_BODEN = "AM_BODEN";
    public static final String IN_DER_LUFT = "IN_DER_LUFT";
    public static final String LANDEND = "LANDEND";
    public static final String STARTEND = "STARTEND";

    /**
     * Schreiben von Informationen ueber einen Flughafen in einen Container.
     */
    public static void writeFlughafenInfo(final Capi capi, final ContainerReference flughafenInfo,
            final TransactionReference tx, final FlughafenData flughafen) throws Exception {

        try {
            ArrayList<Selector> selectors = new ArrayList<Selector>();
            selectors.add(KeyCoordinator.newSelector(flughafen.getFlughafenName(), MzsConstants.Selecting.COUNT_ALL));
            capi.delete(flughafenInfo, selectors, MzsConstants.RequestTimeout.TRY_ONCE, tx);
        } catch (MzsCoreException e) {
            e.printStackTrace();
        }
        Entry entry = new Entry(flughafen, KeyCoordinator.newCoordinationData(flughafen.getFlughafenName()));
        capi.write(flughafenInfo, MzsConstants.RequestTimeout.ZERO, tx, entry);
    }

    public static FlughafenData readFlughafenInfo(final Capi capi, final ContainerReference flughafenInfo,
            final TransactionReference tx, final String flughafenName) {

        try {
            ArrayList<Selector> selectors = new ArrayList<Selector>();
            selectors.add(KeyCoordinator.newSelector(flughafenName, 1));
            ArrayList<Serializable> resultEntries = capi.read(flughafenInfo, selectors,
                    MzsConstants.RequestTimeout.ZERO, tx);
            return (FlughafenData) resultEntries.get(0);
            // TODO: what if empty arraylist
        } catch (MzsCoreException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    /**
     * Schreiben von Informationen ueber eine bestimmte Landebahn in einen Container.
     */
    public static void writeLandebahnInfo(final Capi capi, final ContainerReference landebahnen,
            final TransactionReference tx, final LandebahnData landebahn) throws Exception {

        try {
            ArrayList<Selector> selectors = new ArrayList<Selector>();
            selectors.add(KeyCoordinator.newSelector(String.valueOf(landebahn.getLandebahnNr()),
                    MzsConstants.Selecting.COUNT_ALL));
            capi.delete(landebahnen, selectors, MzsConstants.RequestTimeout.ZERO, tx);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Entry entry = new Entry(landebahn, KeyCoordinator.newCoordinationData(String.valueOf(landebahn
                .getLandebahnNr())), RandomCoordinator.newCoordinationData());
        capi.write(landebahnen, MzsConstants.RequestTimeout.ZERO, tx, entry);
        System.out.println("Written landebahn.");
    }

    /**
     * Liest Informationen ueber eine bestimmte Landebahn aus dem Container.
     *
     * @param capi
     * @param landebahnen
     * @param tx
     * @param landebahn
     * @return
     */
    public static LandebahnData readLandebahnInfo(final Capi capi, final ContainerReference landebahnen,
            final TransactionReference tx, final int landebahn) {

        try {
            ArrayList<Selector> selectors = new ArrayList<Selector>();
            selectors.add(KeyCoordinator.newSelector(String.valueOf(landebahn), 1));
            ArrayList<Serializable> resultEntries = capi.read(landebahnen, selectors,
                    MzsConstants.RequestTimeout.ZERO, tx);

            return (LandebahnData) resultEntries.get(0);
        }
        // TODO: what if empty result list returned.
        catch (MzsCoreException e) {
            e.printStackTrace();
        }
        return null;
    }

}
