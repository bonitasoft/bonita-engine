/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.mockito.Mockito;

public class ConfigurableEventServiceImplTest extends EventServiceTest {

    @Override
    protected EventService instantiateEventServiceImplementation() {
        Map<String, SHandler<SEvent>> handlers = new HashMap<String, SHandler<SEvent>>();
        TechnicalLoggerService logger = mockTechnicalLoggerService();
        try {
            return new ConfigurableEventServiceImpl(handlers, logger);
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
