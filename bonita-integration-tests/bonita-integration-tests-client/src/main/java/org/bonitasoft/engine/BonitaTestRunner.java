package org.bonitasoft.engine;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public class BonitaTestRunner extends BlockJUnit4ClassRunner implements BonitaRunner {

    private boolean isRoot;

    private final Class<?> annotatedInitializer;

    public BonitaTestRunner(final Class<?> klass) throws InitializationError {
        super(klass);
        annotatedInitializer = BonitaSuiteRunner.getAnnotatedInitializer(klass);
        isRoot = true;
    }

    @Override
    public void setRoot(final boolean isRoot) {
        this.isRoot = isRoot;
    }

    @Override
    public boolean isRoot() {
        return isRoot;
    }

    @Override
    protected Statement classBlock(final RunNotifier notifier) {

        Statement statement = super.classBlock(notifier);
        statement = withGlobalBefore(statement);
        statement = withGlobalAfter(statement);
        return statement;
    }

    private Statement withGlobalBefore(final Statement statement) {
        return new WithGlobalBefore(statement, this, annotatedInitializer);
    }

    private Statement withGlobalAfter(final Statement statement) {
        return new WithGlobalAfter(statement, this, annotatedInitializer);
    }

}
