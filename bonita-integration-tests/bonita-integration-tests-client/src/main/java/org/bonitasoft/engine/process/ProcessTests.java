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
package org.bonitasoft.engine.process;

import org.bonitasoft.engine.process.actor.ActorTests;
import org.bonitasoft.engine.process.comment.CommentIT;
import org.bonitasoft.engine.process.data.ActivityDataDefinitionIT;
import org.bonitasoft.engine.process.data.ActivityDataInstanceIT;
import org.bonitasoft.engine.process.data.ProcessDataDefinitionIT;
import org.bonitasoft.engine.process.data.ProcessDataInstanceIT;
import org.bonitasoft.engine.process.document.DocumentIT;
import org.bonitasoft.engine.process.instance.ProcessInstanceTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        ProcessResolutionIT.class,
        ProcessParameterIT.class,
        ProcessExecutionIT.class,
        ProcessManagementIT.class,
        ProcessDeploymentIT.class,
        ProcessDescriptionIT.class,
        GetProcessDefinitionIT.class,
        GatewayExecutionIT.class,
        FlowPatternsIT.class,
        ProcessCategoryIT.class,
        ProcessInstanceTests.class,
        ProcessWithExpressionIT.class,
        StartProcessWithOperationsIT.class,
        ProcessDeletionIT.class,
        EvaluateExpressionIT.class,
        ExecutionContextIT.class,
        CommentIT.class,
        DocumentIT.class,
        ActorTests.class,
        ActivityDataInstanceIT.class,
        ActivityDataDefinitionIT.class,
        ProcessDataInstanceIT.class,
        ProcessDataDefinitionIT.class
})
public class ProcessTests {

}
