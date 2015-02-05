/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.api.impl.converter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.business.application.ApplicationMenu;
import org.bonitasoft.engine.business.application.ApplicationMenuCreator;
import org.bonitasoft.engine.business.application.ApplicationMenuField;
import org.bonitasoft.engine.business.application.ApplicationMenuUpdater;
import org.bonitasoft.engine.business.application.impl.ApplicationMenuImpl;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.model.builder.SApplicationMenuBuilder;
import org.bonitasoft.engine.business.application.model.builder.SApplicationMenuBuilderFactory;
import org.bonitasoft.engine.business.application.model.builder.SApplicationMenuUpdateBuilder;
import org.bonitasoft.engine.business.application.model.builder.SApplicationMenuUpdateBuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationMenuModelConverter {

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
