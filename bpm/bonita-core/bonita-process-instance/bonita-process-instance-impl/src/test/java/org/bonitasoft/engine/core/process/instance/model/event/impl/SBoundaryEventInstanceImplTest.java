package org.bonitasoft.engine.core.process.instance.model.event.impl;

import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

public class SBoundaryEventInstanceImplTest {

    private SBoundaryEventInstanceImpl boundary;

    @Before
    public void setUp() {
        boundary = new SBoundaryEventInstanceImpl();
    }

    @Test
    public void mustExecutOnAbortOrCancelProcess_return_false_if_stable() {
        boundary.setStable(true);
        assertFalse(boundary.mustExecuteOnAbortOrCancelProcess());
    }

    // for a boundary we never must execute the flow node on abort the process instance because it will be aborted by the related activity
    @Test
    public void mustExecutOnAbortOrCancelProcess_return_false_if_not_stable() {
        boundary.setStable(false);
        assertFalse(boundary.mustExecuteOnAbortOrCancelProcess());
    }

}
