/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.service.platform;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.platform.exception.SPlatformUpdateException;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.builder.SPlatformBuilderFactory;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.services.UpdateDescriptor;

/**
 * Updates the platform information using directly the {@link PersistenceService}
 * @author Elias Ricken de Medeiros
 */
public class PlatformInformationServiceImpl implements PlatformInformationService {

    private PersistenceService persistenceService;

    public PlatformInformationServiceImpl(final PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    @Override
    public void updatePlatformInfo(final SPlatform platform, final String platformInfo) throws SPlatformUpdateException {

        SPlatformBuilderFactory factory = BuilderFactory.get(SPlatformBuilderFactory.class);

        UpdateDescriptor desc = new UpdateDescriptor(platform);
        desc.addField(factory.getInformationKey(), platformInfo);

        try {
            persistenceService.update(desc);
        } catch (SPersistenceException e) {
            throw new SPlatformUpdateException(e);
        }
    }

}
