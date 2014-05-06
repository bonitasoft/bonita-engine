package com.bonitasoft.engine.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.platform.model.impl.STenantImpl;
import org.junit.Test;

import com.bonitasoft.engine.platform.Tenant;

public class SPModelConvertorTest {

    @Test
    public void clientToServerTenantConversionShouldTreatAllFields() throws Exception {
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

    // @Test
    // public void defaultTenantMaintenanceModeIsFalse() throws Exception {
    // final STenant sTenant = BuilderFactory.get(STenantBuilderFactory.class)
    // .createNewInstance("model_conversion", "Scoobidoo", 4874411255L, "activated", false).done();
    // Tenant tenant = SPModelConvertor.toTenant(sTenant);
    //
    //
    // // assertThat(tenant. isInMaintenance()).isEqualTo(false);
    // }

}
