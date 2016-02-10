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
package org.bonitasoft.engine.bdm.proxy.assertion;

import static javassist.util.proxy.ProxyFactory.isProxyClass;

import org.assertj.core.api.AbstractAssert;

public class ProxyAssert extends AbstractAssert<ProxyAssert, Object> {

    protected ProxyAssert(Object actual) {
        super(actual, ProxyAssert.class);
    }

    public static ProxyAssert assertThat(Object entity) {
        return new ProxyAssert(entity);
    }

    public ProxyAssert isAProxy() {
        isNotNull();

        if (!isProxyClass(actual.getClass())) {
            failWithMessage("Expected <%s> to be a proxy", actual);
        }
        return this;
    }

    public ProxyAssert isNotAProxy() {
        isNotNull();

        if (isProxyClass(actual.getClass())) {
            failWithMessage("Expected <%s> to not be a proxy", actual);
        }
        return this;
    }
}
