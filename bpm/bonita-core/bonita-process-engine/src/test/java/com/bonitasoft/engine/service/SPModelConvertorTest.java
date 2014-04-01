package com.bonitasoft.engine.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.platform.model.impl.STenantImpl;
import org.junit.Test;

import com.bonitasoft.engine.platform.Tenant;

public class SPModelConvertorTest {

    @Test
    public void clientToServerTenantConversionShouldTreatAllFields() throws Exception {
        String name = "tenant_conversion_test";
        String createdBy = "Scoobidoo";
        long created = 4874411255L;
        String status = "activated";
        boolean defaultTenant = false;
        String description = "this tenant serves test purposes";
        String iconName = "kikoolol_Tenant.gif";
        String iconPath = "icons/tenant_icons/";

        final STenantImpl sTenant = new STenantImpl(name, createdBy, created, status, defaultTenant);
        sTenant.setDescription(description);
        sTenant.setIconName(iconName);
        sTenant.setIconPath(iconPath);
        Tenant tenant = SPModelConvertor.toTenant(sTenant);

        assertThat(tenant.getName()).isEqualTo(name);
        assertThat(tenant.getCreationDate().getTime()).isEqualTo(created);
        assertThat(tenant.getDescription()).isEqualTo(description);
        assertThat(tenant.getIconName()).isEqualTo(iconName);
        assertThat(tenant.getIconPath()).isEqualTo(iconPath);
        assertThat(tenant.getState()).isEqualTo(status);
    }

}
