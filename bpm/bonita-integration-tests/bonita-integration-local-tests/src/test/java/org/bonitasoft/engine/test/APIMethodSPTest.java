package org.bonitasoft.engine.test;

import org.junit.Test;

import com.bonitasoft.engine.api.impl.LogAPIExt;

public class APIMethodSPTest extends APIMethodTest {

    @Test
    public void checkAllMethodsOfLogAPIThrowInvalidSessionException() {
        checkThrowsInvalidSessionException(LogAPIExt.class);
    }

    @Test
    public void checkAllMethodsOfLogAPIContainsSerializableParameters() {
        checkAllParametersAreSerializable(LogAPIExt.class);
    }

}
