package org.bonitasoft.engine.events.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.EventServiceTest;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 *
 * @author Laurent Vaills
 *
 */
public class EventServiceImplTest extends EventServiceTest {

    @Override
    protected EventService instantiateEventServiceImplementation() {
        final TechnicalLoggerService logger = mockTechnicalLoggerService();
        return new EventServiceImpl(logger);
    }

    protected TechnicalLoggerService mockTechnicalLoggerService() {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        when(logger.isLoggable(any(Class.class), any(TechnicalLogSeverity.class))).thenReturn(false);
        return logger;
    }

}
