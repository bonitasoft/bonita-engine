package org.bonitasoft.engine.repair;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.RepairAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by Vincent Elcrin
 * Date: 11/12/13
 * Time: 09:45
 */
public class RepairAPITest extends CommonAPITest {

    RepairAPI repairAPI;

    @Before
    public void setUp() throws Exception {
        login();
        repairAPI = TenantAPIAccessor.getRepairAPI(getSession());
    }

    @After
    public void tearDown() throws Exception {
        logout();
    }

    @Test
    // start -> task 1 -> task 2 -> task 3 -> end
    public void should_start_a_process_giving_an_activity_name_to_start_from() throws Exception {
        repairAPI.startProcess(1, Arrays.asList("task 2"), null, null);
    }
}
