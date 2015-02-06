/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.proxy.assertion;

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
