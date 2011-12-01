/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.tutorial.formula1race;

import java.io.Serializable;

/*
 * Formula 1 Race (VectorCoordinator).
 * This class stores all needed information of a Racing-Car.
 *
 * @author Formanek, Keszthelyi
 */
public class RacingCar implements Serializable {

    private static final long serialVersionUID = -7125953750753501321L;

    private final String driver;

    /**
     * Constructor
     *
     * @param driver
     */
    public RacingCar(final String driver) {
        this.driver = driver;
    }

    /**
     * @return driver
     */
    public String getDriver() {
        return driver;
    }
}
