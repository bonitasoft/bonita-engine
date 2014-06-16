package com.bonitasoft.engine.bdm.proxy;

import static javassist.util.proxy.ProxyFactory.isProxyClass;

import org.assertj.core.api.AbstractAssert;

import com.bonitasoft.engine.bdm.Entity;

public class ProxyAssert extends AbstractAssert<ProxyAssert, Entity> {

    protected ProxyAssert(Entity actual) {
        super(actual, ProxyAssert.class);
    }

    public static ProxyAssert assertThat(Entity entity) {
        return new ProxyAssert(entity);
    }

    public ProxyAssert isAProxy() {
        isNotNull();

        if (!isProxyClass(actual.getClass())) {
            failWithMessage("Expected <%s> to be a proxy", actual);
        }
        return this;
    }
}
