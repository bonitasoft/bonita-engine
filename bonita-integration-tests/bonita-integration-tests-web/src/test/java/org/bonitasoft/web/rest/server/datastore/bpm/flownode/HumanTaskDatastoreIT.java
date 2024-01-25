/**
 * Copyright (C) 2022 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.web.rest.server.datastore.bpm.flownode;

import static org.bonitasoft.web.toolkit.client.data.APIID.makeAPIID;
import static org.junit.Assert.assertEquals;

import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.test.toolkit.bpm.TestHumanTask;
import org.bonitasoft.test.toolkit.bpm.TestProcessFactory;
import org.bonitasoft.web.rest.model.bpm.flownode.HumanTaskItem;
import org.bonitasoft.web.test.AbstractConsoleTest;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ValidatorEngine;
import org.junit.Test;

public class HumanTaskDatastoreIT extends AbstractConsoleTest {

    private HumanTaskDatastore humanTaskDatastore;

    @Override
    public void consoleTestSetUp() throws Exception {
        humanTaskDatastore = new HumanTaskDatastore(getInitiator().getSession());
    }

    private HumanTaskItem fetchHumanTask(final long taskId) throws Exception {
        final HumanTaskInstance humanTaskInstance = (HumanTaskInstance) TenantAPIAccessor
                .getProcessAPI(getInitiator().getSession()).getActivityInstance(taskId);
        return HumanTaskDatastore.fillConsoleItem(new HumanTaskItem(), humanTaskInstance);
    }

    @Test
    public void task_priority_can_be_changed() throws Exception {
        final TestHumanTask humanTask = TestProcessFactory.getDefaultHumanTaskProcess().addActor(getInitiator())
                .startCase().getNextHumanTask();
        final HumanTaskItem humanTaskItem = new HumanTaskItem();
        humanTaskItem.setId(makeAPIID(humanTask.getId()));
        humanTaskItem.setPriority(HumanTaskItem.VALUE_PRIORITY_ABOVE_NORMAL);
        ValidatorEngine.validate(humanTaskItem);

        humanTaskDatastore.update(humanTaskItem.getId(), humanTaskItem.getAttributes());

        final HumanTaskItem fetchedTask = fetchHumanTask(humanTask.getId());
        assertEquals(HumanTaskItem.VALUE_PRIORITY_ABOVE_NORMAL, fetchedTask.getPriority());
    }

}
