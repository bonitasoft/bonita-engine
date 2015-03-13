/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.core.contract.data;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SAContractDataTest {

    @Test
    public void create_should_copy_values() throws Exception {
        final SContractData contractData = new SContractData("id", 15124245748545L, 1983L);
        contractData.setId(7548463269L);
        contractData.setTenantId(1L);

        final SAContractData saContractData = new SAContractData(contractData);

        assertThat(saContractData.getTenantId()).isEqualTo(0L);
        assertThat(saContractData.getId()).isEqualTo(0L);
        assertThat(saContractData.getName()).isEqualTo("id");
        assertThat(saContractData.getArchiveDate()).isEqualTo(0L);
        assertThat(saContractData.getScopeId()).isEqualTo(1983L);
        assertThat(saContractData.getSourceObjectId()).isEqualTo(7548463269L);
        assertThat(saContractData.getValue()).isEqualTo(15124245748545L);
    }

}
