/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application.model.builder;

import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public interface SApplicationUpdateBuilder {

    EntityUpdateDescriptor done();

    SApplicationUpdateBuilder updateHomePageId(long applicationPageId);

    public SApplicationUpdateBuilder updateName(final String name);

    public SApplicationUpdateBuilder updateDisplayName(final String displayName);

    public SApplicationUpdateBuilder updateVersion(final String version);

    public SApplicationUpdateBuilder updatePath(final String path);

    public SApplicationUpdateBuilder updateDescription(final String description);

    public SApplicationUpdateBuilder updateIconPath(final String iconPath);

    public SApplicationUpdateBuilder updateState(final String state);

    public SApplicationUpdateBuilder updateProfileId(final Long profileId);

}
