/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.test;

import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.LocalServerTestsInitializer;
import org.bonitasoft.engine.api.impl.CommandAPIImpl;
import org.bonitasoft.engine.api.impl.IdentityAPIImpl;
import org.bonitasoft.engine.api.impl.PlatformAPIImpl;
import org.bonitasoft.engine.api.impl.PlatformCommandAPIImpl;
import org.bonitasoft.engine.api.impl.ProcessAPIImpl;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner;
import org.bonitasoft.engine.test.runner.BonitaTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BonitaTestRunner.class)
@BonitaSuiteRunner.Initializer(LocalServerTestsInitializer.class)
public class APIMethodLocalIT {

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
    public void checkAllMethodsOfProcessAPIContainsSerializableParameters() {
        checkAllParametersAreSerializable(ProcessAPIImpl.class);
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
        final List<String> defaultMethods = Arrays.asList("wait", "equals", "toString", "hashCode", "getClass", "notify", "notifyAll");
        return defaultMethods.contains(method.getName());
    }

}
