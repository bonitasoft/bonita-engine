/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.events.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.EventServiceTest;
import org.bonitasoft.engine.events.model.HandlerRegistrationException;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class ClusteredEventServiceImplTest extends EventServiceTest {

    private static HazelcastInstance hazelcastInstance;

    private ClusteredEventServiceImpl eventService;

    @BeforeClass
    public static void beforeClass() {
        Config config = new Config();
        // disable all networking
        JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getTcpIpConfig().setEnabled(false);
        joinConfig.getAwsConfig().setEnabled(false);

        hazelcastInstance = Hazelcast.newHazelcastInstance(config);
    }

    @AfterClass
    public static void afterClass() {
        Hazelcast.shutdownAll();
    }

    @After
    public void afterEachTest() {
        eventService.removeAllHandlers();
    }

    @Override
    protected EventService instantiateEventServiceImplementation() {
        return instantiateEventServiceImplementation(new HashMap<String, SHandler<SEvent>>());
    }

    protected EventService instantiateEventServiceImplementation(final Map<String, SHandler<SEvent>> handlers) {
        TechnicalLoggerService logger = mockTechnicalLoggerService();
        Manager manager = mock(Manager.class);
        when(manager.isFeatureActive(Features.ENGINE_CLUSTERING)).thenReturn(true);
        try {
            eventService = new ClusteredEventServiceImpl("PLATFORM", handlers, logger, hazelcastInstance, manager);
            return eventService;
        } catch (HandlerRegistrationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return
     */
    protected TechnicalLoggerService mockTechnicalLoggerService() {
        TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        when(logger.isLoggable(any(Class.class), any(TechnicalLogSeverity.class))).thenReturn(false);
        return logger;
    }

    @Test
    public void constructInstanceWithDefaultHandlers() {
        SEvent event = new DummyEvent("DUMMY");
        SHandler<SEvent> handler = new DummyHandler(event.getType());
        Map<String, SHandler<SEvent>> defaultHandlers = Collections.singletonMap(event.getType(), handler);

        ClusteredEventServiceImpl eventService1 = (ClusteredEventServiceImpl) instantiateEventServiceImplementation(defaultHandlers);
        assertEquals(1, eventService1.getHandlers(event.getType()).size());
    }

    @Test
    public void constructTwoInstances() throws Exception {
        SEvent event = new DummyEvent("DUMMY");
        // Add the handler on #1
        ClusteredEventServiceImpl eventService1 = (ClusteredEventServiceImpl) instantiateEventServiceImplementation();
        SHandler<SEvent> handler = new DummyHandler(event.getType());
        eventService1.addHandler(event.getType(), handler);

        // Fire the event on #2
        ClusteredEventServiceImpl eventService2 = (ClusteredEventServiceImpl) instantiateEventServiceImplementation();
        assertTrue(eventService2.containsHandlerFor(event.getType()));
        // eventService2.fireEvent(event);
        // How to assert the handler was executed correctly ?
        // (-> Write on the filesystem in the handler then read on the assert ?)
    }

    @Test
    public void constructTwoInstancesWithDefaultHandlers() {
        SEvent event = new DummyEvent("DUMMY");
        SHandler<SEvent> handler = new DummyHandler(event.getType());
        Map<String, SHandler<SEvent>> defaultHandlers = Collections.singletonMap(event.getType(), handler);

        ClusteredEventServiceImpl eventService1 = (ClusteredEventServiceImpl) instantiateEventServiceImplementation(defaultHandlers);
        assertEquals(1, eventService1.getHandlers(event.getType()).size());

        ClusteredEventServiceImpl eventService2 = (ClusteredEventServiceImpl) instantiateEventServiceImplementation(defaultHandlers);
        assertEquals(1, eventService2.getHandlers(event.getType()).size());
    }

    @Test
    public void fireEventWithTwoInstances() throws Exception {
        SEvent event = new DummyEvent("DUMMY");
        // Add the handler on #1
        ClusteredEventServiceImpl eventService1 = (ClusteredEventServiceImpl) instantiateEventServiceImplementation();
        SHandler<SEvent> handler = new DummyHandler(event.getType());
        eventService1.addHandler(event.getType(), handler);

        // Fire the event on #2
        ClusteredEventServiceImpl eventService2 = (ClusteredEventServiceImpl) instantiateEventServiceImplementation();
        assertTrue(eventService2.containsHandlerFor(event.getType()));
        // eventService2.fireEvent(event);
        // How to assert the handler was executed correctly ?
        // (-> Write on the filesystem in the handler then read on the assert ?)
    }

    private static class DummyHandler implements SHandler<SEvent> {

        private static final long serialVersionUID = 1L;

        private final String interestedEventType;

        public DummyHandler(final String eventType) {
            this.interestedEventType = eventType;
        }

        @Override
        public void execute(final SEvent event) {
            System.out.println("Executing on event type : " + event.getType() + " " + event);
        }

        @Override
        public boolean isInterested(final SEvent event) {
            return interestedEventType.equals(event.getType());
        }

        @Override
        public String getIdentifier() {
            return getClass().getName();
        }

    }

    private static class DummyEvent implements SEvent {

        private final String type;

        private Object object;

        public DummyEvent(final String type) {
            this.type = type;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public Object getObject() {
            return object;
        }

        @Override
        public void setObject(final Object object) {
            this.object = object;
        }

    }

}
