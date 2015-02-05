/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.scheduler.impl;

import java.util.Map.Entry;
import java.util.Properties;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.impl.BonitaSchedulerFactory;
import org.quartz.SchedulerException;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;

/**
 * @author Baptiste Mesta
 */
public class BonitaSchedulerFactoryExtended extends BonitaSchedulerFactory {

    public BonitaSchedulerFactoryExtended(final Properties props, final Properties additionalProperties, final TechnicalLoggerService logger)
            throws SchedulerException {
        super(merge(props, additionalProperties), logger);
    }

    private static Properties merge(final Properties props, final Properties additionalProperties) {
        if (!Manager.getInstance().isFeatureActive(Features.ENGINE_CLUSTERING)) {
            throw new IllegalStateException("The clustering is not an active feature.");
        }
        final Properties properties = new Properties();
        for (final Entry<Object, Object> entry : props.entrySet()) {
            properties.put(entry.getKey(), entry.getValue());
        }
        for (final Entry<Object, Object> entry : additionalProperties.entrySet()) {
            properties.put(entry.getKey(), entry.getValue());
        }
        return properties;
    }

}
