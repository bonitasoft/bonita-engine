package org.bonitasoft.engine;

import java.lang.reflect.Method;

import org.junit.runners.model.Statement;

final class WithGlobalBefore extends Statement {

    private final Statement classBlock;

    private final BonitaRunner testRunner;

    private final Class<?> initializer;

    WithGlobalBefore(final Statement classBlock, final BonitaRunner testRunner, final Class<?> annotatedInitializer) {
        this.classBlock = classBlock;
        this.testRunner = testRunner;
        this.initializer = annotatedInitializer;
    }

    @Override
    public void evaluate() throws Throwable {
        if (testRunner.isRoot()) {
            Method declaredMethod = initializer.getDeclaredMethod("beforeAll");
            declaredMethod.invoke(null);
        }
        classBlock.evaluate();
    }
}
