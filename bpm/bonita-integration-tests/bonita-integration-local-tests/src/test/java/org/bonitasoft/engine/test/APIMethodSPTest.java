package org.bonitasoft.engine.test;

import org.bonitasoft.engine.api.impl.LogAPIImpl;
import org.junit.Test;

/**
 * @author Matthieu Chaffotte
 */
public class APIMethodSPTest extends APIMethodTest {

    @Test
    public void checkAllMethodsOfLogAPIThrowInvalidSessionException() {
        checkThrowsInvalidSessionException(LogAPIImpl.class);
    }

    @Test
    public void checkAllMethodsOfLogAPIContainsSerializableParameters() {
        checkAllParametersAreSerializable(LogAPIImpl.class);
    }

}
