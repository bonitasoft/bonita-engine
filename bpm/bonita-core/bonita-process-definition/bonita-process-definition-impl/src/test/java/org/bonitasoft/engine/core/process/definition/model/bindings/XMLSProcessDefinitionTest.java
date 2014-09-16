package org.bonitasoft.engine.core.process.definition.model.bindings;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.bpm.contract.SimpleInputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.SimpleInputDefinitionImpl;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.builder.SProcessDefinitionBuilderFactory;
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
    public void complexInputtest() throws Exception {
        xmlsProcessDefinition = new XMLSProcessDefinition();
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("Process with contract", "1.0");
        builder.addActor("actor");
        final SimpleInputDefinition expenseType = new SimpleInputDefinitionImpl("expenseType", Type.TEXT, "describe expense type");
        final SimpleInputDefinition expenseAmount = new SimpleInputDefinitionImpl("amount", Type.DECIMAL, "expense amount");
        final SimpleInputDefinition expenseDate = new SimpleInputDefinitionImpl("date", Type.DATE, "expense date");
        final List<SimpleInputDefinition> inputs = Arrays.asList(expenseType, expenseDate, expenseAmount);

        builder.addUserTask("task1", "actor").addContract().addComplexInput("expenseLine", "expense report line", inputs, null);

        //when
        final SProcessDefinition sProcessDefinition = BuilderFactory.get(SProcessDefinitionBuilderFactory.class).createNewInstance(builder.done()).done();
        final XMLNode xmlNode = xmlsProcessDefinition.getXMLProcessDefinition(sProcessDefinition);

        //then
        final XMLNode expectedInputNode = new XMLNode(XMLSProcessDefinition.CONTRACT_INPUT_NODE);
        expectedInputNode.addAttribute(XMLSProcessDefinition.NAME, "expenseType");
        expectedInputNode.addAttribute(XMLSProcessDefinition.TYPE, Type.TEXT.toString());
        expectedInputNode.addAttribute(XMLSProcessDefinition.DESCRIPTION, "describe expense type");

    }
}
