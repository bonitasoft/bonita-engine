/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.test;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.BonitaDatabaseConfiguration;
import org.bonitasoft.engine.execution.ProcessStarterVerifier;
import org.bonitasoft.engine.test.http.BonitaHttpServer;
import org.bonitasoft.engine.test.internal.EngineCommander;
import org.bonitasoft.engine.test.internal.EngineStarter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Baptiste Mesta
 */
@Getter
@Slf4j
@ContextConfiguration(classes = TestEngineImpl.TestConfiguration.class)
public class TestEngineImpl implements TestEngine {

    private static TestEngineImpl INSTANCE = createTestEngine();

    private BonitaDatabaseConfiguration bonitaDatabaseConfiguration;

    private BonitaDatabaseConfiguration businessDataDatabaseConfiguration;

    private final TestDatabaseConfigurator testDatabaseConfigurator = new TestDatabaseConfigurator();
    BonitaHttpServer httpServer = new BonitaHttpServer();

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
            // Starting HTTP server must be done AFTER engine start, as the API Type will be overridden to HTTP
            // (See EngineStarter logic):
            httpServer.startIfNeeded();
            return true;
        }
        return false;
    }

    private void initializeDatabaseConfigurations() {
        if (bonitaDatabaseConfiguration == null || businessDataDatabaseConfiguration == null) {
            if (bonitaDatabaseConfiguration == null) {
                bonitaDatabaseConfiguration = testDatabaseConfigurator.getDatabaseConfiguration();
                engineStarter.setBonitaDatabaseConfiguration(bonitaDatabaseConfiguration);
            }
            if (businessDataDatabaseConfiguration == null) {
                businessDataDatabaseConfiguration = testDatabaseConfigurator.getBDMDatabaseConfiguration();
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
            httpServer.stopIfNeeded();
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
    public void setBonitaDatabaseProperties(BonitaDatabaseConfiguration configuration) {
        bonitaDatabaseConfiguration = configuration;
    }

    @Override
    public void setBusinessDataDatabaseProperties(BonitaDatabaseConfiguration database) {
        this.businessDataDatabaseConfiguration = database;
    }

    /**
     * Configuration class used to override bean definitions for test purposes.
     */
    @Configuration
    static class TestConfiguration {

        @Bean
        ProcessStarterVerifier processStarterVerifierImpl() {
            return processInstance -> {
                // Override this bean to disable the process starter verifier
            };
        }
    }
}
