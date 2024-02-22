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
package org.bonitasoft.web.rest.server.api.bpm.flownode;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.test.toolkit.bpm.TestHumanTask;
import org.bonitasoft.test.toolkit.bpm.TestProcess;
import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.web.rest.model.bpm.flownode.TaskItem;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.test.AbstractConsoleTest;
import org.junit.Test;

public class APITaskIT extends AbstractConsoleTest {

    private APITask apiTask;

    @Override
    public void consoleTestSetUp() throws Exception {
        apiTask = new APITask();
        apiTask.setCaller(getAPICaller(TestUserFactory.getJohnCarpenter().getSession(), "API/bpm/task"));
    }

    @Override
    protected TestUser getInitiator() {
        return TestUserFactory.getJohnCarpenter();
    }

    private TestHumanTask createActivityWithVariables() throws InvalidExpressionException {
        final ProcessDefinitionBuilder processDefinitionBuidler = new ProcessDefinitionBuilder()
                .createNewInstance("processName", "1.0");
        processDefinitionBuidler.addActor("Employees", true)
                .addDescription("This a default process")
                .addStartEvent("Start")
                .addUserTask("Activity 1", "Employees")

                .addData("variable1", String.class.getName(),
                        new ExpressionBuilder().createConstantStringExpression("defaultValue"))
                .addData("variable2", Long.class.getName(), new ExpressionBuilder().createConstantLongExpression(1))
                .addData("variable3", Date.class.getName(),
                        new ExpressionBuilder().createConstantDateExpression("428558400000"))

                .addEndEvent("Finish");
        return new TestProcess(processDefinitionBuidler).addActor(getInitiator()).setEnable(getInitiator(), true)
                .startCase().getNextHumanTask()
                .assignTo(getInitiator());
    }

    @Test
    public void api_can_search_with_default_search_order() throws Exception {
        //given
        createActivityWithVariables();

        //when
        final ItemSearchResult<TaskItem> searchResultWithNoOrder = apiTask.runSearch(0, 1, null, null, null, null,
                null);

        //then
        assertThat(searchResultWithNoOrder).as("should be able to search with default search order").isNotNull();

    }

}
