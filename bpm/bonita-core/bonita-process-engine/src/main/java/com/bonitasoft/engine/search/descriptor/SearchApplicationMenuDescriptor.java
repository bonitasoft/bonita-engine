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

import com.bonitasoft.engine.business.application.ApplicationMenuSearchDescriptor;
import com.bonitasoft.engine.business.application.model.SApplicationMenu;
import com.bonitasoft.engine.business.application.model.builder.SApplicationMenuBuilderFactory;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SearchApplicationMenuDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> keys;

    private final Map<Class<? extends PersistentObject>, Set<String>> allFields;

    SearchApplicationMenuDescriptor() {
        final SApplicationMenuBuilderFactory appMenuKeyProvider = BuilderFactory.get(SApplicationMenuBuilderFactory.class);
        keys = new HashMap<String, FieldDescriptor>(6);
        keys.put(ApplicationMenuSearchDescriptor.ID, new FieldDescriptor(SApplicationMenu.class, appMenuKeyProvider.getIdKey()));
        keys.put(ApplicationMenuSearchDescriptor.APPLICATION_PAGE_ID, new FieldDescriptor(SApplicationMenu.class, appMenuKeyProvider.getApplicationPageIdKey()));
        keys.put(ApplicationMenuSearchDescriptor.DISPLAY_NAME, new FieldDescriptor(SApplicationMenu.class, appMenuKeyProvider.getDisplayNameKey()));
        keys.put(ApplicationMenuSearchDescriptor.INDEX, new FieldDescriptor(SApplicationMenu.class, appMenuKeyProvider.getIndexKey()));
        keys.put(ApplicationMenuSearchDescriptor.APPLICATION_ID, new FieldDescriptor(SApplicationMenu.class, appMenuKeyProvider.getApplicationIdKey()));
        keys.put(ApplicationMenuSearchDescriptor.PARENT_ID, new FieldDescriptor(SApplicationMenu.class, appMenuKeyProvider.getParentIdKey()));

        allFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);

        final Set<String> pageFields = new HashSet<String>(1);
        pageFields.add(appMenuKeyProvider.getDisplayNameKey());
        allFields.put(SApplicationMenu.class, pageFields);
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
