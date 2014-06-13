package com.bonitasoft.engine.bdm.proxy;

import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;


public class LazyMethodHandler implements MethodHandler {

    private LazyLoader lazyloader;

    public LazyMethodHandler(LazyLoader lazyloader) {
        this.lazyloader = lazyloader;
    }

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        // TODO Auto-generated method stub
        return null;
    }

}
