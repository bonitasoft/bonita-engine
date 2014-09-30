package org.bonitasoft.engine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.Statement;

public class BonitaSuiteRunner extends Suite implements BonitaRunner {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface Initializer {

        public Class<?> value();
    }

    static Class<?> getAnnotatedInitializer(final Class<?> klass) throws InitializationError {
        Initializer annotation = klass.getAnnotation(Initializer.class);
        if (annotation == null) {
            throw new InitializationError(String.format("class '%s' must have a Initializer annotation", klass.getName()));
        }
        return annotation.value();
    }

    private boolean isRoot;

    private Class<?> annotatedInitializer;

    public BonitaSuiteRunner(final Class<?> klass, final Class<?>[] suiteClasses) throws InitializationError {
        super(klass, suiteClasses);
        isRoot = true;
    }

    public BonitaSuiteRunner(final Class<?> klass, final List<Runner> runners) throws InitializationError {
        super(klass, runners);
        isRoot = true;
    }

    public BonitaSuiteRunner(final Class<?> klass, final RunnerBuilder builder) throws InitializationError {
        super(klass, builder);
        annotatedInitializer = getAnnotatedInitializer(klass);
        isRoot = true;
    }

    public BonitaSuiteRunner(final RunnerBuilder builder, final Class<?> klass, final Class<?>[] suiteClasses) throws InitializationError {
        super(builder, klass, suiteClasses);
        isRoot = true;
    }

    public BonitaSuiteRunner(final RunnerBuilder builder, final Class<?>[] classes) throws InitializationError {
        super(builder, classes);
        isRoot = true;
    }

    @Override
    protected Statement childrenInvoker(final RunNotifier notifier) {
        // TODO Auto-generated method stub
        return super.childrenInvoker(notifier);
    }

    @Override
    protected List<Runner> getChildren() {
        // TODO Auto-generated method stub
        return super.getChildren();
    }

    @Override
    public void run(final RunNotifier notifier) {
        initializeClasses();
        super.run(notifier);
    }

    private void initializeClasses() {
        List<Runner> children = getChildren();
        setRootToFalse(children);
    }

    private void setRootToFalse(final List<Runner> children) {
        for (Runner runner : children) {
            if (runner instanceof BonitaRunner) {
                BonitaRunner bonitaRunner = (BonitaRunner) runner;
                bonitaRunner.setRoot(false);
            } else if (runner instanceof Suite) {
                Suite suite = (Suite) runner;
                try {
                    Method method = Suite.class.getDeclaredMethod("getChildren");
                    method.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    List<Runner> invoke = (List<Runner>) method.invoke(suite);
                    setRootToFalse(invoke);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void setRoot(final boolean isRoot) {
        this.isRoot = isRoot;
        setRootToFalse(getChildren());
    }

    @Override
    protected Statement classBlock(final RunNotifier notifier) {

        Statement statement = super.classBlock(notifier);
        statement = withGlobalBefore(statement);
        statement = withGlobalAfter(statement);
        return statement;
    }

    @Override
    public boolean isRoot() {
        return isRoot;
    }

    private Statement withGlobalBefore(final Statement statement) {
        return new WithGlobalBefore(statement, this, annotatedInitializer);
    }

    private Statement withGlobalAfter(final Statement statement) {
        return new WithGlobalAfter(statement, this, annotatedInitializer);
    }

}
