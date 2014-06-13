package com.bonitasoft.engine.bdm.proxy;

import java.lang.reflect.Method;

import org.bonitasoft.engine.session.APISession;

public class LazyLoader {

    private APISession apiSession;

    public LazyLoader(APISession apiSession) {
        this.apiSession = apiSession;
    }

    public Object load(Method method, long persistenceId) {
        return null;
    }
}
