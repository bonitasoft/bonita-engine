/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.cache.impl;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;

import com.bonitasoft.manager.Manager;
import com.hazelcast.core.HazelcastInstance;

/**
 * @author Baptiste Mesta
 */
public class ClusteredCacheServiceExt extends ClusteredCacheService {

    /**
     * @param manager
     * @param logger
     * @param sessionAccessor
     * @param hazelcastInstance
     */
    public ClusteredCacheServiceExt(final Manager manager, final TechnicalLoggerService logger, final ReadSessionAccessor sessionAccessor,
            final HazelcastInstance hazelcastInstance) {
        super(manager, logger, sessionAccessor, hazelcastInstance);
    }

}
