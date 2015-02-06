/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.events.impl;

import java.util.Map;

import org.bonitasoft.engine.events.impl.EventServiceImpl;
import org.bonitasoft.engine.events.model.HandlerRegistrationException;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Elias Ricken de Medeiros
 */
public class ConfigurableEventServiceImpl extends EventServiceImpl {

    public ConfigurableEventServiceImpl(final Map<String, SHandler<SEvent>> handlers, final TechnicalLoggerService logger)
            throws HandlerRegistrationException {
        super(logger);
        // register the handlers with their associated event type
        for (final Map.Entry<String, SHandler<SEvent>> entry : handlers.entrySet()) {
            addHandler(entry.getKey(), entry.getValue());
        }
    }

}
