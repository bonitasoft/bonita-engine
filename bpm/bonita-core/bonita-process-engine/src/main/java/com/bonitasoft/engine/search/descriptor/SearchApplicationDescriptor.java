/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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
import com.bonitasoft.engine.business.application.model.SApplication;
import com.bonitasoft.engine.business.application.model.builder.SApplicationBuilderFactory;

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
        keys.put(ApplicationSearchDescriptor.TOKEN, new FieldDescriptor(SApplication.class, keyProvider.getTokenKey()));
        keys.put(ApplicationSearchDescriptor.DISPLAY_NAME, new FieldDescriptor(SApplication.class, keyProvider.getDisplayNameKey()));
        keys.put(ApplicationSearchDescriptor.VERSION, new FieldDescriptor(SApplication.class, keyProvider.getVersionKey()));
        keys.put(ApplicationSearchDescriptor.ICON_PATH, new FieldDescriptor(SApplication.class, keyProvider.getIconPathKey()));
        keys.put(ApplicationSearchDescriptor.CREATION_DATE, new FieldDescriptor(SApplication.class, keyProvider.getCreationDateKey()));
        keys.put(ApplicationSearchDescriptor.CREATED_BY, new FieldDescriptor(SApplication.class, keyProvider.getCreatedByKey()));
        keys.put(ApplicationSearchDescriptor.LAST_UPDATE_DATE, new FieldDescriptor(SApplication.class, keyProvider.getLastUpdatedDateKey()));
        keys.put(ApplicationSearchDescriptor.UPDATED_BY, new FieldDescriptor(SApplication.class, keyProvider.getUpdatedByKey()));
        keys.put(ApplicationSearchDescriptor.STATE, new FieldDescriptor(SApplication.class, keyProvider.getStateKey()));

        allFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);

        final Set<String> pageFields = new HashSet<String>(5);
        pageFields.add(keyProvider.getTokenKey());
        pageFields.add(keyProvider.getDisplayNameKey());
        pageFields.add(keyProvider.getVersionKey());
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
