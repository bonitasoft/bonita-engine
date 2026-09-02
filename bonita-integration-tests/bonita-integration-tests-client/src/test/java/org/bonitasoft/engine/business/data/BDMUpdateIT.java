/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.business.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.api.TenantAdministrationAPI;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.bonitasoft.engine.tenant.TenantResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Those tests fail on MySQL and SQLServer because after installing the second BDM version, Hibernate is not aware of
 * previous FK_ that stay in database, so it does not drop them, and then the 'drop table' fails on test cleanup.
 * Those cases should be handled when we support more "BDM update" scenarios, leveraging Hibernate poor level of
 * support on HBM2DDL.
 */
public class BDMUpdateIT extends CommonAPIIT {

    public static final String DOT = ".";
    public static final String PARENT_BO = "ParentBO";
    public static final String CHILD_BO = "ChildBO";
    public static final String OTHER_CHILD_BO = "OtherChildBO";
    private static final String BDM_PACKAGE_PREFIX = "com.company.model";
    private TenantAdministrationAPI tenantAdministrationAPI;

    @Before
    public void setUp() throws Exception {
        loginWithTechnicalUser();
        tenantAdministrationAPI = getTenantAdministrationAPI();
    }

    @After
    public void cleanup() throws Exception {
        cleanAndUninstallBusinessDataModel();
        logout();
    }

    @Test
    public void should_change_single_aggregation_relation() throws Exception {
        checkSingleRelation(RelationField.Type.AGGREGATION);
    }

    @Test
    public void should_change_single_composition_relation() throws Exception {
        checkSingleRelation(RelationField.Type.COMPOSITION);
    }

    @Test
    public void should_change_multiple_aggregation_relation() throws Exception {
        checkMultipleRelation(RelationField.Type.AGGREGATION);
    }

    @Test
    public void should_change_multiple_composition_relation() throws Exception {
        checkMultipleRelation(RelationField.Type.COMPOSITION);
    }

    protected void checkSingleRelation(RelationField.Type relationType) throws Exception {
        // given
        final BusinessObject businessObject = getBusinessObject(PARENT_BO);
        businessObject.addField(getSingleRelationField(getBusinessObject(CHILD_BO), relationType));
        final String version = installBusinessDataModel(getBusinessObjectModel(businessObject));
        assertThat(version).as("should have deployed BDM").isNotNull();
        ensureBDMIsInstalled();

        // when
        uninstallBusinessDataModel();
        assertThat(tenantAdministrationAPI.getBusinessDataModelVersion())
                .as("should uninstall BusinessDataModel")
                .isNull();
        final BusinessObject newBusinessObject = getBusinessObject(PARENT_BO);
        newBusinessObject.addField(getSingleRelationField(getBusinessObject(OTHER_CHILD_BO), relationType));
        final String newVersion = installBusinessDataModel(getBusinessObjectModel(newBusinessObject));

        // then
        assertThat(newVersion).isNotNull().isNotEqualTo(version);
    }

    protected void checkMultipleRelation(RelationField.Type relationType) throws Exception {
        // given
        final BusinessObject businessObject = getBusinessObject(PARENT_BO);
        businessObject.addField(getMultipleRelationField(getBusinessObject(CHILD_BO), relationType));
        final String version = installBusinessDataModel(getBusinessObjectModel(businessObject));
        assertThat(version).as("should have deployed BDM").isNotNull();
        ensureBDMIsInstalled();

        // when
        uninstallBusinessDataModel();
        assertThat(tenantAdministrationAPI.getBusinessDataModelVersion())
                .as("should uninstall BusinessDataModel")
                .isNull();

        final BusinessObject newBusinessObject = getBusinessObject(PARENT_BO);
        newBusinessObject.addField(getMultipleRelationField(getBusinessObject(OTHER_CHILD_BO), relationType));
        final String newVersion = installBusinessDataModel(getBusinessObjectModel(newBusinessObject));

        // then
        assertThat(newVersion).isNotNull().isNotEqualTo(version);
    }

    protected BusinessObjectModel getBusinessObjectModel(BusinessObject parentBusinessObject) {
        final BusinessObjectModel businessObjectModel = new BusinessObjectModel();
        businessObjectModel.addBusinessObject(parentBusinessObject);
        addChildBusinessObjects(businessObjectModel);
        return businessObjectModel;
    }

    protected void addChildBusinessObjects(BusinessObjectModel businessObjectModel1) {
        businessObjectModel1.addBusinessObject(getBusinessObject(BDMUpdateIT.CHILD_BO));
        businessObjectModel1.addBusinessObject(getBusinessObject(BDMUpdateIT.OTHER_CHILD_BO));
    }

    private BusinessObject getBusinessObject(String boName) {
        final BusinessObject businessObject = new BusinessObject();
        businessObject.setQualifiedName(BDM_PACKAGE_PREFIX + DOT + boName);
        businessObject.addField(getSimpleField());
        return businessObject;
    }

    private RelationField getSingleRelationField(BusinessObject businessObject, RelationField.Type relationType) {
        return getRelationField(businessObject, relationType, "single", Boolean.FALSE);
    }

    private RelationField getMultipleRelationField(BusinessObject businessObject, RelationField.Type relationType) {
        return getRelationField(businessObject, relationType, "multiple", Boolean.TRUE);
    }

    private RelationField getRelationField(BusinessObject businessObject, RelationField.Type relationType, String name,
            Boolean isCollection) {
        final RelationField relationField = new RelationField();
        relationField.setType(relationType);
        relationField.setFetchType(RelationField.FetchType.LAZY);
        relationField.setName(name);
        relationField.setCollection(isCollection);
        relationField.setNullable(Boolean.TRUE);
        relationField.setReference(businessObject);
        return relationField;
    }

    private SimpleField getSimpleField() {
        SimpleField simpleField = new SimpleField();
        simpleField.setName("name");
        simpleField.setType(FieldType.STRING);
        return simpleField;
    }

    private void ensureBDMIsInstalled() {
        TenantResource tenantResource = tenantAdministrationAPI.getBusinessDataModelResource();
        assertThat(tenantResource.getLastUpdateDate()).isAfter(OffsetDateTime.now().minusMinutes(1));
    }

}
