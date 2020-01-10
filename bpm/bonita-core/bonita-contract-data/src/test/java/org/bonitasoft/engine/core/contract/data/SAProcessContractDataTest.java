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
package org.bonitasoft.engine.core.contract.data;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SAProcessContractDataTest {

    @Test
    public void creatingSAProcessContractDataShouldCopyNonArchivedValues() throws Exception {
        long processInstanceId = 555L;
        String some_name = "some_name";
        long value = 999999L;
        final SProcessContractData processContractData = new SProcessContractData(processInstanceId, some_name, value);
        long originalProcessDataId = 7548463269L;
        processContractData.setId(originalProcessDataId);
        processContractData.setTenantId(1L);

        final SAProcessContractData saProcessContractData = new SAProcessContractData(processContractData);

        assertThat(saProcessContractData.getTenantId()).isEqualTo(0L); // not set yet by Persistence service
        assertThat(saProcessContractData.getId()).isEqualTo(0L);
        assertThat(saProcessContractData.getName()).isEqualTo(some_name);
        assertThat(saProcessContractData.getScopeId()).isEqualTo(processInstanceId);
        assertThat(saProcessContractData.getArchiveDate()).isEqualTo(0L);
        assertThat(saProcessContractData.getSourceObjectId()).isEqualTo(originalProcessDataId);
        assertThat(saProcessContractData.getValue()).isEqualTo(value);
    }

}
