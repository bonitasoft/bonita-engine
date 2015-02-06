/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
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
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.STenantBuilderFactory;
import org.bonitasoft.engine.search.descriptor.FieldDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;

import com.bonitasoft.engine.platform.TenantSearchDescriptor;

/**
 * @author Zhao Na
 */
public class SearchTenantDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> tenantKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> tenantAllFields;

    SearchTenantDescriptor() {
        final STenantBuilderFactory tenantBuilderFact = BuilderFactory.get(STenantBuilderFactory.class);
        tenantKeys = new HashMap<String, FieldDescriptor>(9);
        tenantKeys.put(TenantSearchDescriptor.ID, new FieldDescriptor(STenant.class, tenantBuilderFact.getIdKey()));
        tenantKeys.put(TenantSearchDescriptor.STATUS, new FieldDescriptor(STenant.class, tenantBuilderFact.getStatusKey()));
        tenantKeys.put(TenantSearchDescriptor.NAME, new FieldDescriptor(STenant.class, tenantBuilderFact.getNameKey()));
        tenantKeys.put(TenantSearchDescriptor.CREATION_DATE, new FieldDescriptor(STenant.class, tenantBuilderFact.getCreatedKey()));
        tenantKeys.put(TenantSearchDescriptor.CREATED_BY, new FieldDescriptor(STenant.class, tenantBuilderFact.getCreatedByKey()));
        tenantKeys.put(TenantSearchDescriptor.ICON_NAME, new FieldDescriptor(STenant.class, tenantBuilderFact.getIconNameKey()));
        tenantKeys.put(TenantSearchDescriptor.ICON_PATH, new FieldDescriptor(STenant.class, tenantBuilderFact.getIconPathKey()));
        tenantKeys.put(TenantSearchDescriptor.DEFAULT_TENANT, new FieldDescriptor(STenant.class, tenantBuilderFact.getDefaultTenantKey()));
        tenantKeys.put(TenantSearchDescriptor.DESCRIPTION, new FieldDescriptor(STenant.class, tenantBuilderFact.getDescriptionKey()));

        tenantAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> tenantFields = new HashSet<String>(6);
        tenantFields.add(tenantBuilderFact.getStatusKey());
        tenantFields.add(tenantBuilderFact.getNameKey());
        tenantFields.add(tenantBuilderFact.getCreatedByKey());
        tenantFields.add(tenantBuilderFact.getDescriptionKey());
        tenantFields.add(tenantBuilderFact.getIconNameKey());
        tenantFields.add(tenantBuilderFact.getIconPathKey());
        tenantAllFields.put(STenant.class, tenantFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return tenantKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return tenantAllFields;
    }

}
