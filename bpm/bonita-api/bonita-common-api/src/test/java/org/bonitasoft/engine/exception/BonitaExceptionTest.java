package org.bonitasoft.engine.exception;

import org.junit.Test;

/**
 * @author Emmanuel Duchastenier
 */
public class BonitaExceptionTest {

    @Test
    public void newBonitaExceptionWithNullCauseShouldNotThrowNPE() {
        new BonitaException("any message", null);
    }

}
