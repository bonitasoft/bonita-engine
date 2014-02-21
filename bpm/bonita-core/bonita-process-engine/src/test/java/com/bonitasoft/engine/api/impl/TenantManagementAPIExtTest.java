package com.bonitasoft.engine.api.impl;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.builder.STenantBuilderFactory;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Test;

import com.bonitasoft.engine.api.TenantMode;
import com.bonitasoft.engine.bdm.BusinessObject;
import com.bonitasoft.engine.bdm.BusinessObjectModel;
import com.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import com.bonitasoft.engine.bdm.Field;
import com.bonitasoft.engine.bdm.FieldType;

public class TenantManagementAPIExtTest {

    @Test
    public void setTenantMaintenanceModeShouldUpdateMaintenanceField() throws Exception {
        long tenantId = 17;
        TenantManagementAPIExt tenantManagementAPI = spy(new TenantManagementAPIExt());
        PlatformService platformService = mock(PlatformService.class);
        doNothing().when(tenantManagementAPI).updateTenantFromId(eq(17), eq(platformService), any(EntityUpdateDescriptor.class));
        doReturn(platformService).when(tenantManagementAPI).getPlatformService();

        tenantManagementAPI.setTenantMaintenanceMode(tenantId, TenantMode.MAINTENANCE);

        EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        String inMaintenanceKey = BuilderFactory.get(STenantBuilderFactory.class).getInMaintenanceKey();
        entityUpdateDescriptor.addField(inMaintenanceKey, true);

        verify(tenantManagementAPI).updateTenantFromId(17, platformService, entityUpdateDescriptor);
    }

    @Test
    public void deployBusinessDataRepositoryShouldHaveAnnotationAvailableOnMaintenanceTenant() throws Exception {
        // given:
        Method method = TenantManagementAPIExt.class.getMethod("deployBusinessDataRepository", byte[].class);

        // then:
        assertTrue("Annotation @AvailableOnMaintenanceTenant should be present on API method TenantManagementAPIExt.deployBusinessDataRepository()",
                method.isAnnotationPresent(AvailableOnMaintenanceTenant.class));
    }

    @Test
    public void setTenantMaintenanceModeShouldHaveAnnotationAvailableOnMaintenanceTenant() throws Exception {
        // given:
        Method method = TenantManagementAPIExt.class.getMethod("setTenantMaintenanceMode", long.class, TenantMode.class);

        // then:
        assertTrue("Annotation @AvailableOnMaintenanceTenant should be present on API method TenantManagementAPIExt.setTenantMaintenanceMode()",
                method.isAnnotationPresent(AvailableOnMaintenanceTenant.class));
    }

    @Test
    public void isTenantInMaintenanceShouldHaveAnnotationAvailableOnMaintenanceTenant() throws Exception {
        // given:
        Method method = TenantManagementAPIExt.class.getMethod("isTenantInMaintenance", long.class);

        // then:
        assertTrue("Annotation @AvailableOnMaintenanceTenant should be present on API method TenantManagementAPIExt.isTenantInMaintenance()",
                method.isAnnotationPresent(AvailableOnMaintenanceTenant.class));
    }
    
    @Test
	public void shouldBuildBDMJAR_ReturnAByteArray() throws Exception {
    	BusinessObjectModel bom = new BusinessObjectModel();
    	BusinessObject businessObject = new BusinessObject();
    	businessObject.setQualifiedName("org.bonitasoft.pojo.Employee");
    	Field name = new Field();
    	name.setName("name");
    	name.setType(FieldType.STRING);
    	businessObject.setFields(Arrays.asList(name));
		bom.addBusinessObject(businessObject );
		assertThat(new TenantManagementAPIExt().buildBDMJAR(new BusinessObjectModelConverter().zip(bom))).isNotEmpty();
	}
}
