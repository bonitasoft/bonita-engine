package org.bonitasoft.engine.operation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * @author Emmanuel Duchastenier
 */
public class OperationImplTest {

    @Test
    public void setOperatorInputTypeShouldConcatenateWithExistingOperator() {
        // given:
        OperationImpl operation = new OperationImpl();
        operation.setOperator("Some#@");

        // when:
        operation.setOperatorInputType("java.lang.Integer");

        // then:
        assertThat(operation.getOperator()).isEqualTo("Some#@:java.lang.Integer");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setOperatorInputTypeShouldThrowExceptionIfOperatorNotAlreadySet() {
        // given:
        OperationImpl operation = new OperationImpl();

        // when:
        operation.setOperatorInputType("tata");

        // then:
        fail("Should not pass through.");
    }

    @Test
    public void setOperatorInputTypeShouldLeaveUnchangedIfNull() {
        // given:
        OperationImpl operation = new OperationImpl();
        operation.setOperator("notNullOperator");

        // when:
        operation.setOperatorInputType(null);

        // then:
        assertThat(operation.getOperator()).isEqualTo("notNullOperator");
    }

}
