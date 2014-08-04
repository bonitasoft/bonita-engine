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
