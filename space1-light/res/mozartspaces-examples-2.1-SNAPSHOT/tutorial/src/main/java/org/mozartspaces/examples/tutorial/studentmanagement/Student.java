/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.tutorial.studentmanagement;

import java.io.Serializable;

/*
 * The Student management (KeyCoordinator).
 * This class stores all needed information of a student.
 *
 * @author Formanek, Keszthelyi
 */
public class Student implements Serializable {

    private static final long serialVersionUID = -2105419236282162888L;

    private int matNr = 0;
    private String forename = null;
    private String surname = null;
    private int age = 0;

    /**
     * Constructor, throws exception if: matNr, age <= 0; forename, surename is
     * either null or it's length is 0.
     *
     * @param matNr
     * @param forename
     * @param surname
     * @param age
     * @throws Exception
     */
    public Student(final int matNr, final String forename, final String surname, final int age) {
        this.matNr = matNr;
        this.forename = forename;
        this.surname = surname;
        this.age = age;
    }

    /**
     * @return matNr
     */
    public int getMatNr() {
        return matNr;
    }

    /**
     * @return forename
     */
    public String getForename() {
        return forename;
    }

    /**
     * @return surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * @return age
     */
    public int getAge() {
        return age;
    }
}
