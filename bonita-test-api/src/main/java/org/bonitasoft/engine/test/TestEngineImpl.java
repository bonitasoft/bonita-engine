package org.bonitasoft.engine.test;

import org.bonitasoft.engine.test.internal.EngineCommander;
import org.bonitasoft.engine.test.internal.EngineStarter;

/**
 * @author Baptiste Mesta
 */
public class TestEngineImpl implements TestEngine {

    private static TestEngineImpl INSTANCE = createTestEngine();

    private static TestEngineImpl createTestEngine() {
        return new TestEngineImpl(new EngineStarter(), new EngineCommander());
    }

    private final EngineStarter engineStarter;
    private final EngineCommander engineCommander;
    private boolean started = false;

    public static TestEngine getInstance() {
        return INSTANCE;
    }

    protected TestEngineImpl(EngineStarter engineStarter, EngineCommander engineCommander) {
        this.engineStarter = engineStarter;
        this.engineCommander = engineCommander;
    }

    protected static void replaceInstance(TestEngineImpl newTestEngine) {
        if (INSTANCE.started) {
            throw new IllegalStateException("trying to replace an already started instance");
        }
        INSTANCE = newTestEngine;
    }

    protected EngineStarter getEngineStarter() {
        return engineStarter;
    }

    /**
     * start the engine and return if it was effectively started
     *
     * @return
     * @throws Exception
     */
    @Override
    public boolean start() throws Exception {
        if (!started) {
            doStart();
            started = true;
            return true;
        }
        return false;
    }

    protected synchronized void doStart() throws Exception {
        engineStarter.start();

    }

    @Override
    public void stop() throws Exception {
        if (started) {
            doStop();
            started = false;
        }
    }

    private void doStop() throws Exception {
        engineStarter.stop();
    }

    @Override
    public void clearData() throws Exception {
        engineCommander.clearData();
    }

    @Override
    public void setDropOnStart(boolean dropOnStart) {
        engineStarter.setDropOnStart(dropOnStart);
    }

    @Override
    public void setDropOnStop(boolean dropOnStop) {
        engineStarter.setDropOnStop(dropOnStop);
    }
}
