package org.bonitasoft.engine.core.process.instance.model.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class SUserTaskInstanceImplTest {

    private SUserTaskInstanceImpl task;

    @Before
    public void setUp() {
        task = new SUserTaskInstanceImpl();
    }

    @Test
    public void mustExecuteOnAbortOrCancelProcess_returns_true_if_stable_state() {
        task.setStable(true);

        assertTrue(task.mustExecuteOnAbortOrCancelProcess());
    }

    @Test
    public void mustExecuteOnAbortOrCancelProcess_returns_false_if_stable_state() {
        task.setStable(false);

        assertFalse(task.mustExecuteOnAbortOrCancelProcess());
    }

}
