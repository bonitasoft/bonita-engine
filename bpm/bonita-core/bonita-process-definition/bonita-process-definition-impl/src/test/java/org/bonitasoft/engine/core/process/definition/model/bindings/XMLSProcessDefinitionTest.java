package org.bonitasoft.engine.core.process.definition.model.bindings;

import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.definition.model.builder.SProcessDefinitionBuilderFactory;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.junit.Test;

public class XMLSProcessDefinitionTest {

    @Test(expected = InvalidProcessDefinitionException.class)
    public void nullOperationShouldBeDeleted() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("Process with null operation", "1.0");

        processDefinitionBuilder.addAutomaticTask("task").addOperation(new LeftOperandBuilder().createNewInstance("myData").done(), OperatorType.ASSIGNMENT,
                "=", null, null);

        BuilderFactory.get(SProcessDefinitionBuilderFactory.class).createNewInstance(processDefinitionBuilder.done()).done();
    }
}
