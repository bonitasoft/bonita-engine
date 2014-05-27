package org.bonitasoft.engine.core.operation.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.impl.SLeftOperandImpl;
import org.junit.Before;
import org.junit.Test;

public class ExternalDataLeftOperandHandlerTest {

    private ExternalDataLeftOperandHandler handler;

    @Before
    public void setUp() {
        handler = new ExternalDataLeftOperandHandler();
    }

    private SLeftOperandImpl createLeftOperand(final String name) {
        final SLeftOperandImpl sLeftOperand = new SLeftOperandImpl();
        sLeftOperand.setName(name);
        return sLeftOperand;
    }

    @Test(expected = SOperationExecutionException.class)
    public void deleteThrowsAnExceptionNotYetSupported() throws Exception {
        handler.delete(createLeftOperand("myData"), 45l, "container");
    }

    @Test
    public void handlerSupportsBatchUpdate() throws Exception {
        assertThat(handler.supportBatchUpdate()).isTrue();
    }

}
