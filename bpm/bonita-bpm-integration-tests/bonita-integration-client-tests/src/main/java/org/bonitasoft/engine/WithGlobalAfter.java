package org.bonitasoft.engine;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.runners.model.Statement;

final class WithGlobalAfter extends Statement {

    private final Statement classBlock;

    private final BonitaRunner testRunner;

    private final Class<?> initializer;

    WithGlobalAfter(final Statement classBlock, final BonitaRunner testRunner, final Class<?> initializer) {
        this.classBlock = classBlock;
        this.testRunner = testRunner;
        this.initializer = initializer;
    }

    @Override
    public void evaluate() throws Throwable {
        classBlock.evaluate();
        if (testRunner.isRoot()) {
            try {
                Method declaredMethod = initializer.getDeclaredMethod("afterAll");
                declaredMethod.invoke(null);
            } catch (final InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }
}
