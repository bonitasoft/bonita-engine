package org.bonitasoft.engine.tracking;

import static org.junit.Assert.assertEquals;

public class AbstractTimeTrackerTest {

    protected void checkRecord(final Record expected, final Record actual) {
        assertEquals(expected.getTimestamp(), actual.getTimestamp());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getDuration(), actual.getDuration());
    }

}
