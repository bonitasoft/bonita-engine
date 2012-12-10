package org.bonitasoft.engine.test;

import org.junit.Test;

import com.bonitasoft.engine.api.impl.LogAPIImpl;

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
