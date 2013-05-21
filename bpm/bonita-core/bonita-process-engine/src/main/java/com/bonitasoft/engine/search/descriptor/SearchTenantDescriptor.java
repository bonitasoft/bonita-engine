/*******************************************************************************
 * Copyright (C) 2009, 2012 BonitaSoft S.A.
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

import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.STenantBuilder;
import org.bonitasoft.engine.search.descriptor.FieldDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;

import com.bonitasoft.engine.platform.TenantSearchDescriptor;

/**
 * @author Zhao Na
 */
public class SearchTenantDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> tenantKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> tenantAllFields;

    SearchTenantDescriptor(final STenantBuilder tenantBuilder) {
        tenantKeys = new HashMap<String, FieldDescriptor>(9);
        tenantKeys.put(TenantSearchDescriptor.ID, new FieldDescriptor(STenant.class, tenantBuilder.getIdKey()));
        tenantKeys.put(TenantSearchDescriptor.STATUS, new FieldDescriptor(STenant.class, tenantBuilder.getStatusKey()));
        tenantKeys.put(TenantSearchDescriptor.NAME, new FieldDescriptor(STenant.class, tenantBuilder.getNameKey()));
        tenantKeys.put(TenantSearchDescriptor.CREATED, new FieldDescriptor(STenant.class, tenantBuilder.getCreatedKey()));
        tenantKeys.put(TenantSearchDescriptor.CREATEDBY, new FieldDescriptor(STenant.class, tenantBuilder.getCreatedByKey()));
        tenantKeys.put(TenantSearchDescriptor.ICONNAME, new FieldDescriptor(STenant.class, tenantBuilder.getIconNameKey()));
        tenantKeys.put(TenantSearchDescriptor.ICONPATH, new FieldDescriptor(STenant.class, tenantBuilder.getIconPathKey()));
        tenantKeys.put(TenantSearchDescriptor.DEFAULTTENANT, new FieldDescriptor(STenant.class, tenantBuilder.getDefaultTenantKey()));
        tenantKeys.put(TenantSearchDescriptor.DESCRIPTION, new FieldDescriptor(STenant.class, tenantBuilder.getDescriptionKey()));

        tenantAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> tenantFields = new HashSet<String>(6);
        tenantFields.add(tenantBuilder.getStatusKey());
        tenantFields.add(tenantBuilder.getNameKey());
        tenantFields.add(tenantBuilder.getCreatedByKey());
        tenantFields.add(tenantBuilder.getDescriptionKey());
        tenantFields.add(tenantBuilder.getIconNameKey());
        tenantFields.add(tenantBuilder.getIconPathKey());
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
