package org.mozartspaces.examples.tutorial.transactions;

import java.io.Serializable;

/*
 * Exercise 3.1: Transactions (Formula 1 extension)
 * This class stores all needed information for a Racing-Car.
 *
 * @author Formanek, Keszthelyi
 */
public class CarInfo implements Serializable {

    private static final long serialVersionUID = 2718352430273021574L;

    private String driver = null;
    private int runTime = 0;
    private int lapNumber = 0;

    /**
     * Constructor
     *
     * @param driver
     *            Name of driver
     */
    public CarInfo(final String driver) {
        this.driver = driver;
    }

    /**
     * Return the driver's name
     *
     * @return driver
     */
    public String getDriver() {
        return driver;
    }

    /**
     * Return the current runtime
     *
     * @return runTime
     */
    public int getRunTime() {
        return runTime;
    }

    /**
     * Return the current lapnumber
     *
     * @return lapNumber
     */
    public int getLapNumber() {
        return lapNumber;
    }

    /**
     * Add time to runTime
     *
     * @param time
     */
    public void addTime(final int time) {
        runTime += time;
    }

    /**
     * Increment lapnumber
     *
     */
    public void incrementLap() {
        lapNumber++;
    }
}
