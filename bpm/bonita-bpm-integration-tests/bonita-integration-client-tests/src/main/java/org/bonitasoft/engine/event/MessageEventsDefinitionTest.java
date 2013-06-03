/**
 * Copyright (C) 2013 BonitaSoft S.A.
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

import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.impl.CatchMessageEventTriggerDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class MessageEventsDefinitionTest {

    @Cover(classes = { CatchMessageEventTriggerDefinitionBuilder.class }, concept = BPMNConcept.EVENTS, keywords = { "Correlation", "Message start event" })
    @Test(expected = InvalidProcessDefinitionException.class)
    public void testCannotHaveCorrelationInStartMessageOfRootProcess() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("proc", "1.0");
        final CatchMessageEventTriggerDefinitionBuilder messageEventTrigger = builder.addStartEvent("startMessage").addMessageEventTrigger("m1");
        messageEventTrigger.addCorrelation(new ExpressionBuilder().createConstantStringExpression("key"),
                new ExpressionBuilder().createConstantStringExpression("v1"));
        builder.addAutomaticTask("auto1");
        builder.addEndEvent("end");
        builder.addTransition("startMessage", "auto1");
        builder.addTransition("auto1", "end");
        builder.done();
    }
}
