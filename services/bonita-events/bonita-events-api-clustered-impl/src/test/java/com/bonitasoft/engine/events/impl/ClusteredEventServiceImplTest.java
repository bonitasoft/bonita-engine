package com.bonitasoft.engine.events.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.EventServiceTest;
import org.bonitasoft.engine.events.model.HandlerRegistrationException;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;
import org.bonitasoft.engine.events.model.builders.SEventBuilders;
import org.bonitasoft.engine.events.model.builders.impl.SEventBuildersImpl;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.After;
import org.junit.BeforeClass;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class ClusteredEventServiceImplTest extends EventServiceTest {

    private static HazelcastInstance hazelcastInstance;

    private ClusteredEventServiceImpl eventService;

    @BeforeClass
    public static void setupBeforeClass() {
        hazelcastInstance = Hazelcast.newHazelcastInstance();
    }

    @After
    public void afterEachTest() {
        eventService.removeAllHandlers();
    }

    @Override
    protected EventService instantiateEventServiceImplementation() {
        SEventBuilders eventBuilders = new SEventBuildersImpl();
        Map<String, SHandler<SEvent>> handlers = new HashMap<String, SHandler<SEvent>>();
        TechnicalLoggerService logger = mockTechnicalLoggerService();
        Manager manager = mock(Manager.class);
        when(manager.isFeatureActive(Features.ENGINE_CLUSTERING)).thenReturn(true);
        try {
            eventService = new ClusteredEventServiceImpl("PLATFORM", eventBuilders, handlers, logger, hazelcastInstance, manager);
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

}
