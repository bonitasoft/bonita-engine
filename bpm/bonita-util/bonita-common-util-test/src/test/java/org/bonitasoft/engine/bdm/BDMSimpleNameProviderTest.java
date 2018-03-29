/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.engine.bdm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class BDMSimpleNameProviderTest {

    @Test
    public void getSimpleNameAlias_should_return_the_first_char_in_lower_case() throws Exception {
        assertThat(BDMSimpleNameProvider.getSimpleNameAlias("employee")).isEqualTo('e');
        assertThat(BDMSimpleNameProvider.getSimpleNameAlias("Car")).isEqualTo('c');
    }

    @Test
    public void getSimpleBusinessObjectName_should_ignore_package_name() throws Exception {
        //given
        String businessObjectQualifiedName = "com.company.Employee";

        //when
        String simpleName = BDMSimpleNameProvider.getSimpleBusinessObjectName(businessObjectQualifiedName);

        //then
        assertThat(simpleName).isEqualTo("Employee");
    }

    @Test
    public void getSimpleBusinessObjectName_should_return_the_entire_name_when_package_name_is_absent() throws Exception {
        //given
        String businessObjectQualifiedName = "Employee";

        //when
        String simpleName = BDMSimpleNameProvider.getSimpleBusinessObjectName(businessObjectQualifiedName);

        //then
        assertThat(simpleName).isEqualTo("Employee");
    }

}