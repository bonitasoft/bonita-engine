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
package org.bonitasoft.engine.core.process.definition.model.bindings;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.HashMap;

import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.definition.model.SCallableElementType;
import org.bonitasoft.engine.core.process.definition.model.builder.SProcessDefinitionBuilderFactory;
import org.bonitasoft.engine.core.process.definition.model.impl.SCallActivityDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SFlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.xml.XMLNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XMLSProcessDefinitionTest {

    @Mock
    private XMLSProcessDefinition xmlsProcessDefinition;

    @Test(expected = InvalidProcessDefinitionException.class)
    public void nullOperationShouldBeDeleted() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("Process with null operation", "1.0");

        processDefinitionBuilder.addAutomaticTask("task").addOperation(new LeftOperandBuilder().createNewInstance("myData").done(), OperatorType.ASSIGNMENT,
                "=", null, null);

        BuilderFactory.get(SProcessDefinitionBuilderFactory.class).createNewInstance(processDefinitionBuilder.done()).done();
    }

    @Test
    public void shouldGenerateCallActivityContractInputExpessions() throws Exception {
        final SProcessDefinitionImpl definition = new SProcessDefinitionImpl("TEST", "7.0.0");
        final SCallActivityDefinitionImpl callActivity = new SCallActivityDefinitionImpl(41L, "callActivity");
        final SExpressionImpl expression = new SExpressionImpl("expr", "return 'toto' ", "TYPE_READ_ONLY_SCRIPT", String.class.getName(), "GROOVY", null);
        callActivity.addProcessStartContractInput("someInput", expression);
        callActivity.setCallableElementType(SCallableElementType.PROCESS);
        ((SFlowElementContainerDefinitionImpl) definition.getProcessContainer()).addActivity(callActivity);
        final XMLSProcessDefinition xmlProcessDefinition = spy(new XMLSProcessDefinition());
        xmlProcessDefinition.getXMLProcessDefinition(definition);

        final HashMap<String, SExpression> inputs = new HashMap<>();
        inputs.put("someInput", expression);
        verify(xmlProcessDefinition).createAndfillContractInputs(any(XMLNode.class), eq(inputs));

    }

}
