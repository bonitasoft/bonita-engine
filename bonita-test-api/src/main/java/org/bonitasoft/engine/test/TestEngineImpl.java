package org.bonitasoft.engine.test;

import org.bonitasoft.engine.BonitaDatabaseConfiguration;
import org.bonitasoft.engine.test.internal.EngineCommander;
import org.bonitasoft.engine.test.internal.EngineStarter;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Baptiste Mesta
 */
@Slf4j
public class TestEngineImpl implements TestEngine {

    private static TestEngineImpl INSTANCE = createTestEngine();
    private BonitaDatabaseConfiguration bonitaDatabaseConfiguration;
    private BonitaDatabaseConfiguration businessDataDatabaseConfiguration;
    private TestDatabaseConfigurator testDatabaseConfigurator = new TestDatabaseConfigurator();

    private static TestEngineImpl createTestEngine() {
        return new TestEngineImpl(EngineStarter.create(), new EngineCommander());
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
     * @throws Exception if Engine cannot be started
     */
    @Override
    public boolean start() throws Exception {
        initializeDatabaseConfigurations();
        if (!started) {
            doStart();
            started = true;
            return true;
        }
        return false;
    }

    private void initializeDatabaseConfigurations() {
        if (bonitaDatabaseConfiguration == null || businessDataDatabaseConfiguration == null) {
            BonitaDatabaseConfiguration configuration = testDatabaseConfigurator.getDatabaseConfiguration();
            if (bonitaDatabaseConfiguration == null) {
                bonitaDatabaseConfiguration = configuration;
                engineStarter.setBonitaDatabaseConfiguration(bonitaDatabaseConfiguration);
            }
            if (businessDataDatabaseConfiguration == null) {
                businessDataDatabaseConfiguration = configuration;
                engineStarter.setBusinessDataDatabaseConfiguration(businessDataDatabaseConfiguration);
            }
        }
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

    @Override
    public void setBonitaDatabaseProperties(BonitaDatabaseConfiguration configuration) {
        bonitaDatabaseConfiguration = configuration;
    }

    @Override
    public void setBusinessDataDatabaseProperties(BonitaDatabaseConfiguration database) {
        this.businessDataDatabaseConfiguration = database;
    }

    public BonitaDatabaseConfiguration getBonitaDatabaseConfiguration() {
        return bonitaDatabaseConfiguration;
    }

    public BonitaDatabaseConfiguration getBusinessDataDatabaseConfiguration() {
        return businessDataDatabaseConfiguration;
    }
}
