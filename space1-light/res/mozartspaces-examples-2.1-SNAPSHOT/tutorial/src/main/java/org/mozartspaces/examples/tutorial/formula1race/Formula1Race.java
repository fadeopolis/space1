/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.tutorial.formula1race;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.mozartspaces.capi3.VectorCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants.Container;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsConstants.Selecting;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;

/*
 * Formula 1 Race (VectorCoordinator).
 * This example writes 10 RacingCar objects to the container. In the first lap
 * the 6th positioned car has an accident and is removed from the container.
 * In lap 2 car number 4 overtakes number 3.
 *
 * @author Formanek, Keszthelyi, Efler
 */
public class Formula1Race {

    // only needed for nice printing
    final protected static int MAX_LAPS = 2;

    public static void main(final String[] args) throws MzsCoreException, URISyntaxException {
        String driver[] = { "Lewis Hamilton", "Fernando Alonso", "Kimi Räikkönen", "Felipe Massa", "Nick Heidfeld",
                "Robert Kubica", "Heikki Kovalainen", "Giancarlo Fisichella", "Nico Rosberg", "Alexander Wurz" };

        /* Create new Capi instance */
        MzsCore core = DefaultMzsCore.newInstance();
        Capi capi = new Capi(core);

        /* Create new Container using VectorCoordinator and VectorSelector */
        ContainerReference racingPositions = capi.createContainer("positions", null, Container.UNBOUNDED, null,
                new VectorCoordinator());
        ContainerReference retiredCars = capi.createContainer("retired", null, Container.UNBOUNDED, null,
                new VectorCoordinator());

        /* Create 10 RacingCar instances and write them to the container */
        for (int i = 0; i < driver.length; i++) {
            RacingCar writeCar = new RacingCar(driver[i]);
            /* Create a new entry */
            Entry entry = new Entry(writeCar, VectorCoordinator.newCoordinationData(VectorCoordinator.APPEND));
            /* Write entry to the container */
            capi.write(racingPositions, 0, null, entry);
        }

        printCurrentPositioning(capi, racingPositions, retiredCars, 0);

        /*
         * LAP 1 Car number 6 has an accident and is removed from the list
         */

        /* Read and destroy entry at index 5 in the container */
        ArrayList<Serializable> takenEntries = capi.take(racingPositions, VectorCoordinator.newSelector(5),
                RequestTimeout.INFINITE, null);

        /* Write the read entry to the retiredCars container */
        if (takenEntries.get(0).getClass().equals(RacingCar.class)) {
            RacingCar readCar = (RacingCar) takenEntries.get(0);
            Entry entry = new Entry(readCar, VectorCoordinator.newCoordinationData(0));
            capi.write(retiredCars, 0, null, entry);
        }

        printCurrentPositioning(capi, racingPositions, retiredCars, 1);

        /*
         * LAP 2 Car number 4 overtakes number 3
         */

        /* Read and destroy entry with index 3 in the container */
        takenEntries = capi.take(racingPositions, VectorCoordinator.newSelector(3), RequestTimeout.INFINITE, null);

        if (takenEntries.get(0).getClass().equals(RacingCar.class)) {
            /* Write back previously read entry at index 2 */
            RacingCar readCar = ((RacingCar) takenEntries.get(0));
            Entry entry = new Entry(readCar, VectorCoordinator.newCoordinationData(2));
            capi.write(racingPositions, 0, null, entry);
        }

        printCurrentPositioning(capi, racingPositions, retiredCars, 2);

        /* Remove container from space */
        capi.destroyContainer(racingPositions, null);
        capi.destroyContainer(retiredCars, null);

        capi.shutdown(null);
    }

    /*
     * Print current positioning
     */
    public static void printCurrentPositioning(final Capi capi, final ContainerReference racingPositions,
            final ContainerReference retiredCars, final int lap) throws MzsCoreException {
        // only some nice print-formatting
        if (lap == 0) {
            System.out.println("*** Starting Grid ***");
        } else if (lap == MAX_LAPS) {
            System.out.println("*** Lap " + lap + " - Final Lap ***");
        } else {
            System.out.println("*** Lap " + lap + " ***");
        }

        /* Read the current positioning */
        ArrayList<Serializable> readEntries = capi.read(racingPositions,
                VectorCoordinator.newSelector(0, Selecting.COUNT_ALL), RequestTimeout.INFINITE, null);

        int i = 1;
        for (Serializable readEntry : readEntries) {
            if (readEntry.getClass().isAssignableFrom(RacingCar.class)) {
                RacingCar car = ((RacingCar) readEntry);
                System.out.println("Pos " + i++ + " - " + car.getDriver());
            }
        }

        /* Read all retired cars */
        readEntries = capi.read(retiredCars, VectorCoordinator.newSelector(0, Selecting.COUNT_ALL),
                RequestTimeout.INFINITE, null);

        for (Serializable readEntry : readEntries) {
            if (readEntry.getClass().isAssignableFrom(RacingCar.class)) {
                RacingCar retiredCar = ((RacingCar) readEntry);
                System.out.println("Retired " + retiredCar.getDriver());
            }
        }

        System.out.println("*** ****** ****** ***\n");
    }
}
