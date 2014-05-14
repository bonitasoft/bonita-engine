package org.bonitasoft.engine.core.operation.impl;

import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.impl.SLeftOperandImpl;
import org.junit.Test;

public class ExternalDataLeftOperandHandlerTest {

    private ExternalDataLeftOperandHandler handler;

    private SLeftOperandImpl createLeftOperand(final String name) {
        final SLeftOperandImpl sLeftOperand = new SLeftOperandImpl();
        sLeftOperand.setName(name);
        return sLeftOperand;
    }

    @Test(expected = SOperationExecutionException.class)
    public void deleteThrowsAnExceptionNotYetSupported() throws Exception {
        handler = new ExternalDataLeftOperandHandler();
        handler.delete(createLeftOperand("myData"), 45l, "container");
    }

}
