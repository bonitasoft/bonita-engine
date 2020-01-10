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
package org.bonitasoft.engine.bdm.validator.rule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.api.result.Status.Level.ERROR;

import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Danila Mazour
 */
public class UniqueSimpleNameValidationRuleTest {

    private UniqueSimpleNameValidationRule uniqueSimpleNameValidationRule;

    private BusinessObject customerPackageA;

    private BusinessObject customerPackageB;

    private BusinessObject customerUpperCase;

    private BusinessObject client;

    @Before
    public void setUp() {
        uniqueSimpleNameValidationRule = new UniqueSimpleNameValidationRule();
        createObjects();
    }

    private void createObjects() {
        customerPackageA = new BusinessObject();
        customerPackageB = new BusinessObject();
        customerUpperCase = new BusinessObject();
        client = new BusinessObject();
        customerUpperCase.setQualifiedName("net.company.model.CUSTOMER");
        customerPackageA.setQualifiedName("com.company.model.Customer");
        customerPackageB.setQualifiedName("net.company.model.Customer");
        client.setQualifiedName("org.company.model.Client");
    }

    @Test
    public void should_return_error_when_same_business_object_name() {
        //given
        BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(customerPackageA);
        bom.addBusinessObject(customerPackageB);

        //when
        ValidationStatus validationStatus = uniqueSimpleNameValidationRule.validate(bom);

        //then
        assertThat(validationStatus.getStatuses().size()).isEqualTo(1);
        assertThat(validationStatus.getStatuses().get(0).getLevel()).isEqualTo(ERROR);

    }

    @Test
    public void should_return_error_when_same_business_object_name_but_upperCase() {
        //given
        BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(customerPackageA);
        bom.addBusinessObject(customerUpperCase);

        //when
        ValidationStatus validationStatus = uniqueSimpleNameValidationRule.validate(bom);

        //then
        assertThat(validationStatus.getStatuses().size()).isEqualTo(1);
        assertThat(validationStatus.getStatuses().get(0).getLevel()).isEqualTo(ERROR);

    }

    @Test
    public void should_return_a_valid_status_when_no_identical_object_name() {
        //given
        BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(customerPackageA);
        bom.addBusinessObject(client);

        //when
        ValidationStatus validationStatus = uniqueSimpleNameValidationRule.validate(bom);

        //then
        assertThat(validationStatus.getStatuses()).isEmpty();
    }
}
