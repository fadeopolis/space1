/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.tutorial.studentmanagement;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;

/*
 * The Student management (KeyCoordinator).
 * This example writes 5 Student objects to the container using its
 * matriculation number (MatNr) as key. Finally all 5 objects are
 * retrieved from the container by their keys.
 *
 * @author Formanek, Keszthelyi, Efler
 */
public class StudentManagement {

    public static void main(final String[] args) throws MzsCoreException, URISyntaxException {
        /* Create new Capi instance */
        MzsCore core = DefaultMzsCore.newInstance();
        Capi capi = new Capi(core);

        /* Create new Container using KeyCoordinator */
        ContainerReference cref = capi.createContainer("students", null,
                MzsConstants.Container.UNBOUNDED, Arrays.asList(new KeyCoordinator()), null, null);

        Student students[] = { new Student(1000, "Max", "Muster", 20), new Student(1001, "Ernst", "Mueller", 28),
                new Student(1002, "Nora", "Maier", 19), new Student(1003, "Indiana", "Jones", 26),
                new Student(1004, "John", "Constantine", 24) };

        /* Create 5 Student instances and write them into the container */
        for (Student writeStudent : students) {
            /* Create new AtomicEntry using KeySelector */
            Entry entry = new Entry(writeStudent, Arrays.asList(KeyCoordinator.newCoordinationData(Integer
                    .toString(writeStudent.getMatNr()))));
            /* Write entry to the container */
            capi.write(cref, 0, null, entry);
        }

        /* Read entries */
        for (int i = 1000; i < 1005; i++) {
            /* Get entry using KeySelector from the container */
            ArrayList<Serializable> readEntries = capi.read(cref, Arrays.asList(KeyCoordinator.newSelector(Integer
                    .toString(i))), 0, null);

            if (readEntries.get(0).getClass().isAssignableFrom(Student.class)) {
                Student readStudent = ((Student) readEntries.get(0));
                System.out.println("*** Beginning of entry ***");
                System.out.println("MatNr.:   " + readStudent.getMatNr());
                System.out.println("Forename: " + readStudent.getForename());
                System.out.println("Surname:  " + readStudent.getSurname());
                System.out.println("Age:      " + readStudent.getAge());
                System.out.println("*** End of entry ***\n");
            }
        }

        /* Remove container from space */
        capi.destroyContainer(cref, null);

        capi.shutdown(null);
    }
}
