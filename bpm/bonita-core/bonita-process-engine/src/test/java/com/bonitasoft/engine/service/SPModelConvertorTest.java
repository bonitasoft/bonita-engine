/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.bonitasoft.engine.platform.model.impl.STenantImpl;
import org.junit.Test;

import com.bonitasoft.engine.businessdata.MultipleBusinessDataReference;
import com.bonitasoft.engine.businessdata.SimpleBusinessDataReference;
import com.bonitasoft.engine.core.process.instance.model.impl.SProcessMultiRefBusinessDataInstanceImpl;
import com.bonitasoft.engine.core.process.instance.model.impl.SProcessSimpleRefBusinessDataInstanceImpl;
import com.bonitasoft.engine.core.process.instance.model.impl.SSimpleRefBusinessDataInstanceImpl;
import com.bonitasoft.engine.platform.Tenant;

public class SPModelConvertorTest {

    @Test
    public void clientToServerTenantConversionShouldTreatAllFields() {
        final String name = "tenant_conversion_test";
        final String createdBy = "Scoobidoo";
        final long created = 4874411255L;
        final String status = "activated";
        final boolean defaultTenant = false;
        final String description = "this tenant serves test purposes";
        final String iconName = "kikoolol_Tenant.gif";
        final String iconPath = "icons/tenant_icons/";

        final STenantImpl sTenant = new STenantImpl(name, createdBy, created, status, defaultTenant);
        sTenant.setDescription(description);
        sTenant.setIconName(iconName);
        sTenant.setIconPath(iconPath);
        final Tenant tenant = SPModelConvertor.toTenant(sTenant);

        assertThat(tenant.getName()).isEqualTo(name);
        assertThat(tenant.getCreationDate().getTime()).isEqualTo(created);
        assertThat(tenant.getDescription()).isEqualTo(description);
        assertThat(tenant.getIconName()).isEqualTo(iconName);
        assertThat(tenant.getIconPath()).isEqualTo(iconPath);
        assertThat(tenant.getState()).isEqualTo(status);
    }

    @Test
    public void convertSSimpleBusinessDataReferencetoClientObject() throws Exception {
        final SSimpleRefBusinessDataInstanceImpl sReference = new SProcessSimpleRefBusinessDataInstanceImpl();
        sReference.setName("employee");
        sReference.setDataClassName("com.bonitasoft.Employee");
        sReference.setId(465L);
        sReference.setDataId(87997L);

        final SimpleBusinessDataReference reference = SPModelConvertor.toSimpleBusinessDataReference(sReference);
        assertThat(reference.getStorageId()).isEqualTo(87997L);
        assertThat(reference.getName()).isEqualTo("employee");
        assertThat(reference.getType()).isEqualTo("com.bonitasoft.Employee");
    }

    @Test
    public void convertSMultiBusinessDataReferencetoClientObject() throws Exception {
        final SProcessMultiRefBusinessDataInstanceImpl sReference = new SProcessMultiRefBusinessDataInstanceImpl();
        sReference.setName("employees");
        sReference.setDataClassName("com.bonitasoft.Employee");
        sReference.setId(465L);
        sReference.setDataIds(Arrays.asList(87997L, 654312354L, 4786454L));

        final MultipleBusinessDataReference reference = SPModelConvertor.toMultipleBusinessDataReference(sReference);
        assertThat(reference.getStorageIds()).isEqualTo(Arrays.asList(87997L, 654312354L, 4786454L));
        assertThat(reference.getName()).isEqualTo("employees");
        assertThat(reference.getType()).isEqualTo("com.bonitasoft.Employee");
    }

}
