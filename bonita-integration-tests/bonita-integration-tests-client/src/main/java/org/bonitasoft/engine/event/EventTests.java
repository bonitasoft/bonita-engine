/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.event;

import org.bonitasoft.engine.activity.SendTaskIT;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        StartEventIT.class,
        EndEventIT.class,
        SignalEventIT.class,
        SendTaskIT.class,
        SignalBoundaryEventIT.class,
        ErrorBoundaryEventIT.class,
        SignalEventSubProcessIT.class,
        ErrorEventSubProcessIT.class,
        EventTriggerIT.class
})
public class EventTests {

    private static final String everySeconds = "0/1 * * * * ?";

    private static final String EVENT_JOB_FREQUENCY = "event.job.frequency";

    private static String originalFrequency;

    @BeforeClass
    public static void setEventJobCron() {
        originalFrequency = System.getProperty(EVENT_JOB_FREQUENCY);
        System.setProperty(EVENT_JOB_FREQUENCY, everySeconds);
    }

    @AfterClass
    public static void resetEventJobCron() {
        if (originalFrequency == null) {
            System.clearProperty(EVENT_JOB_FREQUENCY);
        } else {
            System.setProperty(EVENT_JOB_FREQUENCY, originalFrequency);
        }
    }

}
