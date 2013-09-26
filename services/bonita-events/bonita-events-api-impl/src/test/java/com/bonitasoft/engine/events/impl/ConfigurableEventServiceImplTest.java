package com.bonitasoft.engine.events.impl;

import static org.mockito.Matchers.any;
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
import org.mockito.Mockito;

public class ConfigurableEventServiceImplTest extends EventServiceTest {

    @Override
    protected EventService instantiateEventServiceImplementation() {
        SEventBuilders eventBuilders = new SEventBuildersImpl();
        Map<String, SHandler<SEvent>> handlers = new HashMap<String, SHandler<SEvent>>();
        TechnicalLoggerService logger = mockTechnicalLoggerService();
        try {
            return new ConfigurableEventServiceImpl(eventBuilders, handlers, logger);
        } catch (HandlerRegistrationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return
     */
    protected TechnicalLoggerService mockTechnicalLoggerService() {
        TechnicalLoggerService logger = Mockito.mock(TechnicalLoggerService.class);
        when(logger.isLoggable(any(Class.class), any(TechnicalLogSeverity.class))).thenReturn(false);
        return logger;
    }

}
