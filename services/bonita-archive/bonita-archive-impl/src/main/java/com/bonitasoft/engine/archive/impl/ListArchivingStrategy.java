/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.archive.impl;

import java.util.Map;

import org.bonitasoft.engine.archive.impl.AbstractArchivingStrategy;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class ListArchivingStrategy extends AbstractArchivingStrategy {

    public ListArchivingStrategy(final Map<String, Boolean> archives) {
        super(archives);
        if (!Manager.getInstance().isFeatureActive(Features.ENGINE_ARCHIVE_CONFIG)) {
            throw new IllegalStateException("The archive configuration is not an active feature.");
        }
    }

}
