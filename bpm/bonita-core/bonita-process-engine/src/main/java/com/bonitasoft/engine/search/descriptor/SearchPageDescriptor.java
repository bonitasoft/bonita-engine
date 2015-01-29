/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
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
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.page.SPageBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.search.descriptor.FieldDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;

import com.bonitasoft.engine.page.PageSearchDescriptor;

/**
 * @author Baptiste Mesta
 */
public class SearchPageDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> pageKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> pageAllFields;

    SearchPageDescriptor() {
        final SPageBuilderFactory keyProvider = BuilderFactory.get(SPageBuilderFactory.class);
        pageKeys = new HashMap<String, FieldDescriptor>(6);
        pageKeys.put(PageSearchDescriptor.ID, new FieldDescriptor(SPage.class, keyProvider.getIdKey()));
        pageKeys.put(PageSearchDescriptor.NAME, new FieldDescriptor(SPage.class, keyProvider.getNameKey()));
        pageKeys.put(PageSearchDescriptor.PROVIDED, new FieldDescriptor(SPage.class, keyProvider.getProvidedKey()));
        pageKeys.put(PageSearchDescriptor.INSTALLATION_DATE, new FieldDescriptor(SPage.class, keyProvider.getInstallationDateKey()));
        pageKeys.put(PageSearchDescriptor.LAST_MODIFICATION_DATE, new FieldDescriptor(SPage.class, keyProvider.getLastModificationDateKey()));
        pageKeys.put(PageSearchDescriptor.INSTALLED_BY, new FieldDescriptor(SPage.class, keyProvider.getInstalledByKey()));
        pageKeys.put(PageSearchDescriptor.DISPLAY_NAME, new FieldDescriptor(SPage.class, keyProvider.getDisplayNameKey()));

        pageAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);

        final Set<String> pageFields = new HashSet<String>(2);
        pageFields.add(keyProvider.getNameKey());
        pageFields.add(keyProvider.getDisplayNameKey());
        pageAllFields.put(SPage.class, pageFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return pageKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return pageAllFields;
    }

}
