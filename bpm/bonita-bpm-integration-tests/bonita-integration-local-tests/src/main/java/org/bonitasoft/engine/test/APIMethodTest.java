package org.bonitasoft.engine.test;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.bonitasoft.engine.api.impl.CommandAPIImpl;
import org.bonitasoft.engine.api.impl.IdentityAPIImpl;
import org.bonitasoft.engine.api.impl.PlatformAPIImpl;
import org.bonitasoft.engine.api.impl.PlatformCommandAPIImpl;
import org.bonitasoft.engine.api.impl.ProcessAPIImpl;
import org.bonitasoft.engine.api.impl.ReportingAPIImpl;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class APIMethodTest {

    @Test
    public void checkAllMethodsOfCommandAPIContainsSerializableParameters() {
        checkAllParametersAreSerializable(CommandAPIImpl.class);
    }

    @Test
    public void checkAllMethodsOfPlatformCommandAPIContainsSerializableParameters() {
        checkAllParametersAreSerializable(PlatformCommandAPIImpl.class);
    }

    @Test
    public void checkAllMethodsOfPlatformAPIContainsSerializableParameters() {
        checkAllParametersAreSerializable(PlatformAPIImpl.class);
    }

    @Test
    public void checkAllMethodsOfIdentityAPIContainsSerializableParameters() {
        checkAllParametersAreSerializable(IdentityAPIImpl.class);
    }

    @Test
    @Ignore
    public void checkAllMethodsOfProcessAPIContainsSerializableParameters() {
        checkAllParametersAreSerializable(ProcessAPIImpl.class);
    }

    @Test
    public void checkAllMethodsOfReportAPIContainsSerializableParameters() {
        checkAllParametersAreSerializable(ReportingAPIImpl.class);
    }

    protected void checkAllParametersAreSerializable(final Class<?> api) {
        final Method[] methods = api.getMethods();
        for (final Method method : methods) {
            if (!isADefaultMethod(method)) {
                final Class<?>[] parameterTypes = method.getParameterTypes();
                for (final Class<?> parameterType : parameterTypes) {
                    if (!parameterType.isPrimitive() && !Collection.class.isAssignableFrom(parameterType) && !Map.class.isAssignableFrom(parameterType)) {
                        final boolean assignableFrom = Serializable.class.isAssignableFrom(parameterType);
                        assertTrue("Method: " + method.getName() + " of API: " + api.getName() + " contains an unserializable parameter " + parameterType,
                                assignableFrom);
                    }
                }
                final Class<?> returnType = method.getReturnType();
                if (!returnType.isPrimitive() && !Collection.class.isAssignableFrom(returnType) && !Map.class.isAssignableFrom(returnType)) {
                    final boolean assignableFrom = Serializable.class.isAssignableFrom(returnType);
                    assertTrue("Method: " + method.getName() + " of API: " + api.getName() + " contains an unserializable return type " + returnType,
                            assignableFrom);
                }
            }
        }
    }

    private boolean isADefaultMethod(final Method method) {
        final String methodName = method.getName();
        return "wait".equals(methodName) || "equals".equals(methodName) || "toString".equals(methodName) || "hashCode".equals(methodName)
                || "getClass".equals(methodName) || "notify".equals(methodName) || "notifyAll".equals(methodName);
    }

}
