package com.bonitasoft.engine.test;

import com.bonitasoft.engine.test.internal.EngineStarterSP;
import org.bonitasoft.engine.test.TestEngineImpl;
import org.bonitasoft.engine.test.internal.EngineCommander;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Baptiste Mesta
 */
public class TestEngineSP extends TestEngineImpl {


    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngineSP.class.getName());

    private static TestEngineSP INSTANCE = new TestEngineSP();

    public static TestEngineSP getInstance() {
        return INSTANCE;
    }

    protected TestEngineSP() {
        super(new EngineStarterSP(), new EngineCommander());
        replaceInstance(this);
    }

    @Override
    protected synchronized void doStart() throws Exception {
        LOGGER.info("Starting SP version of the engine");
        super.doStart();
    }


    public long getDefaultTenantId() {
        return ((EngineStarterSP) getEngineStarter()).getDefaultTenantId();
    }


}
