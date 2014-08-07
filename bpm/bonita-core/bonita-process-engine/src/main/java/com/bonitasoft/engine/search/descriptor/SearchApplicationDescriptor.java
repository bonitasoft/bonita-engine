/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package com.bonitasoft.engine.search.descriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.search.descriptor.FieldDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;

import com.bonitasoft.engine.business.application.ApplicationSearchDescriptor;
import com.bonitasoft.engine.business.application.SApplication;
import com.bonitasoft.engine.business.application.SApplicationBuilderFactory;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SearchApplicationDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> keys;

    private final Map<Class<? extends PersistentObject>, Set<String>> allFields;

    SearchApplicationDescriptor() {
        final SApplicationBuilderFactory keyProvider = BuilderFactory.get(SApplicationBuilderFactory.class);
        keys = new HashMap<String, FieldDescriptor>(10);
        keys.put(ApplicationSearchDescriptor.ID, new FieldDescriptor(SApplication.class, keyProvider.getIdKey()));
        keys.put(ApplicationSearchDescriptor.NAME, new FieldDescriptor(SApplication.class, keyProvider.getNameKey()));
        keys.put(ApplicationSearchDescriptor.DISPLAY_NAME, new FieldDescriptor(SApplication.class, keyProvider.getDisplayNameKey()));
        keys.put(ApplicationSearchDescriptor.VERSION, new FieldDescriptor(SApplication.class, keyProvider.getVersionKey()));
        keys.put(ApplicationSearchDescriptor.PATH, new FieldDescriptor(SApplication.class, keyProvider.getPathKey()));
        keys.put(ApplicationSearchDescriptor.ICON_PATH, new FieldDescriptor(SApplication.class, keyProvider.getIconPathKey()));
        keys.put(ApplicationSearchDescriptor.CREATION_DATE, new FieldDescriptor(SApplication.class, keyProvider.getCreationDateKey()));
        keys.put(ApplicationSearchDescriptor.CREATED_BY, new FieldDescriptor(SApplication.class, keyProvider.getCreatedByKey()));
        keys.put(ApplicationSearchDescriptor.LAST_UPDATE_DATE, new FieldDescriptor(SApplication.class, keyProvider.getLastUpdatedDateKey()));
        keys.put(ApplicationSearchDescriptor.UPDATED_BY, new FieldDescriptor(SApplication.class, keyProvider.getUpdatedByKey()));
        keys.put(ApplicationSearchDescriptor.STATE, new FieldDescriptor(SApplication.class, keyProvider.getStateKey()));

        allFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);

        final Set<String> pageFields = new HashSet<String>(5);
        pageFields.add(keyProvider.getNameKey());
        pageFields.add(keyProvider.getDisplayNameKey());
        pageFields.add(keyProvider.getVersionKey());
        pageFields.add(keyProvider.getPathKey());
        pageFields.add(keyProvider.getIconPathKey());
        pageFields.add(keyProvider.getStateKey());
        allFields.put(SApplication.class, pageFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return keys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return allFields;
    }

}
