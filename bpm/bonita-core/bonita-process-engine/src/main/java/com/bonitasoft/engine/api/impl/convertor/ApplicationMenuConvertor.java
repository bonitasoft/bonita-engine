/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.convertor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bonitasoft.engine.business.application.model.builder.SApplicationMenuUpdateBuilder;
import com.bonitasoft.engine.business.application.model.builder.SApplicationMenuUpdateBuilderFactory;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

import com.bonitasoft.engine.business.application.ApplicationMenu;
import com.bonitasoft.engine.business.application.ApplicationMenuCreator;
import com.bonitasoft.engine.business.application.ApplicationMenuField;
import com.bonitasoft.engine.business.application.ApplicationMenuUpdater;
import com.bonitasoft.engine.business.application.impl.ApplicationMenuImpl;
import com.bonitasoft.engine.business.application.model.SApplicationMenu;
import com.bonitasoft.engine.business.application.model.builder.SApplicationMenuBuilder;
import com.bonitasoft.engine.business.application.model.builder.SApplicationMenuBuilderFactory;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationMenuConvertor {

    public SApplicationMenu buildSApplicationMenu(final ApplicationMenuCreator creator, int menuIndex) {
        final Map<ApplicationMenuField, Serializable> fields = creator.getFields();
        final String displayName = (String) fields.get(ApplicationMenuField.DISPLAY_NAME);
        final Long applicationId = (Long) fields.get(ApplicationMenuField.APPLICATION_ID);
        final Long applicationPageId = (Long) fields.get(ApplicationMenuField.APPLICATION_PAGE_ID);
        final Long parentId = (Long) fields.get(ApplicationMenuField.PARENT_ID);

        final SApplicationMenuBuilder builder = BuilderFactory.get(SApplicationMenuBuilderFactory.class).createNewInstance(displayName, applicationId,
                applicationPageId,
                menuIndex);
        if (parentId != null) {
            builder.setParentId(parentId);
        }
        return builder.done();
    }

    public ApplicationMenu toApplicationMenu(final SApplicationMenu sApplicationMenu) {
        final ApplicationMenuImpl menu = new ApplicationMenuImpl(sApplicationMenu.getDisplayName(), sApplicationMenu.getApplicationId(),
                sApplicationMenu.getApplicationPageId(),
                sApplicationMenu.getIndex());
        menu.setId(sApplicationMenu.getId());
        menu.setParentId(sApplicationMenu.getParentId());
        return menu;
    }

    public List<ApplicationMenu> toApplicationMenu(final List<SApplicationMenu> sApplicationMenus) throws SBonitaException {
        final List<ApplicationMenu> menus = new ArrayList<ApplicationMenu>(sApplicationMenus.size());
        for (final SApplicationMenu sMenu : sApplicationMenus) {
            menus.add(toApplicationMenu(sMenu));
        }
        return menus;
    }

    public EntityUpdateDescriptor toApplicationMenuUpdateDescriptor(final ApplicationMenuUpdater updater) {
        SApplicationMenuUpdateBuilder builder = BuilderFactory.get(SApplicationMenuUpdateBuilderFactory.class).createNewInstance();

        for (Map.Entry<ApplicationMenuField, Serializable> entry : updater.getFields().entrySet()) {
            switch (entry.getKey()) {
                case APPLICATION_PAGE_ID:
                    builder.updateApplicationPageId((Long) entry.getValue());
                    break;
                case DISPLAY_NAME:
                    builder.updateDisplayName((String) entry.getValue());
                    break;
                case INDEX:
                    builder.updateIndex((Integer) entry.getValue());
                    break;
                case PARENT_ID:
                    builder.updateParentId((Long) entry.getValue());
                    break;
            }
        }

        return builder.done();
    }

}
