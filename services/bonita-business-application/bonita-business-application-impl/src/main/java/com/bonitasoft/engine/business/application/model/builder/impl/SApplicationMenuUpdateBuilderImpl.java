/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.model.builder.impl;

import com.bonitasoft.engine.business.application.model.builder.SApplicationMenuUpdateBuilder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Elias Ricken de Medeiros
 */
public class SApplicationMenuUpdateBuilderImpl implements SApplicationMenuUpdateBuilder {

    private EntityUpdateDescriptor descriptor;

    public SApplicationMenuUpdateBuilderImpl() {
        descriptor = new EntityUpdateDescriptor();
    }

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

    @Override
    public SApplicationMenuUpdateBuilder updateDisplayName(String displayName) {
        descriptor.addField(SApplicationMenuFields.DISPLAY_NAME, displayName);
        return this;
    }

    @Override
    public SApplicationMenuUpdateBuilder updateApplicationPageId(Long applicationPageId) {
        descriptor.addField(SApplicationMenuFields.APPLICATION_PAGE_ID, applicationPageId);
        return this;
    }

    @Override
    public SApplicationMenuUpdateBuilder updateIndex(int index) {
        descriptor.addField(SApplicationMenuFields.INDEX, index);
        return this;
    }

    @Override
    public SApplicationMenuUpdateBuilder updateParentId(Long parentId) {
        descriptor.addField(SApplicationMenuFields.PARENT_ID, parentId);
        return this;
    }
}
