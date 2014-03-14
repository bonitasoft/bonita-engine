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
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.search.descriptor.FieldDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;

import com.bonitasoft.engine.page.PageSearchDescriptor;
import com.bonitasoft.engine.page.SPage;
import com.bonitasoft.engine.page.SPageBuilderFactory;

/**
 * @author Baptiste Mesta
 */
public class SearchPageDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> reportKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> reportAllFields;

    SearchPageDescriptor() {
        final SPageBuilderFactory keyProvider = BuilderFactory.get(SPageBuilderFactory.class);
        reportKeys = new HashMap<String, FieldDescriptor>(5);
        reportKeys.put(PageSearchDescriptor.ID, new FieldDescriptor(SPage.class, keyProvider.getIdKey()));
        reportKeys.put(PageSearchDescriptor.NAME, new FieldDescriptor(SPage.class, keyProvider.getNameKey()));
        reportKeys.put(PageSearchDescriptor.PROVIDED, new FieldDescriptor(SPage.class, keyProvider.getProvidedKey()));
        reportKeys.put(PageSearchDescriptor.INSTALLATION_DATE, new FieldDescriptor(SPage.class, keyProvider.getInstallationDateKey()));
        reportKeys.put(PageSearchDescriptor.INSTALLED_BY, new FieldDescriptor(SPage.class, keyProvider.getInstalledByKey()));
        reportKeys.put(PageSearchDescriptor.DISPLAY_NAME, new FieldDescriptor(SPage.class, keyProvider.getDisplayNameKey()));

        reportAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> reportFields = new HashSet<String>(8);
        reportFields.add(keyProvider.getNameKey());
        reportAllFields.put(SPage.class, reportFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return reportKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return reportAllFields;
    }

}
