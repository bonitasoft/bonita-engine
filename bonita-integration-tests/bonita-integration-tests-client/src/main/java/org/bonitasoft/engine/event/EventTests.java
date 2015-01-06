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
