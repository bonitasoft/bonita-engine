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

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.bonitasoft.engine.bpm.contract.FileInputValue;
import org.junit.jupiter.api.Test;

class SAProcessContractDataTest {

    @Test
    void clearFileInputContent() {
        var contractData = new SProcessContractData();
        contractData.setId(1);
        contractData.setName("myFile");
        contractData.setValue(new FileInputValue("theFile", "content".getBytes()));

        var archivedContractData = new SAProcessContractData(contractData);

        assertThat(archivedContractData.getValue()).isInstanceOf(FileInputValue.class)
                .extracting("content")
                .isNull();
    }

    @Test
    void clearMultipleFileInputContent() {
        var contractData = new SProcessContractData();
        contractData.setId(1);
        contractData.setName("myFile");
        contractData.setValue((Serializable) List.of(
                new FileInputValue("theFile", "content".getBytes()),
                new FileInputValue("theFile1", "content1".getBytes())));

        var archivedContractData = new SAProcessContractData(contractData);

        assertThat(archivedContractData.getValue()).isInstanceOf(Collection.class);
        assertThat((Collection<?>) archivedContractData.getValue())
                .extracting("content")
                .containsNull();
    }

    @Test
    void creatingSAProcessContractDataShouldCopyNonArchivedValues() {
        long processInstanceId = 555L;
        String some_name = "some_name";
        long value = 999999L;
        final SProcessContractData processContractData = new SProcessContractData(processInstanceId, some_name, value);
        long originalProcessDataId = 7548463269L;
        processContractData.setId(originalProcessDataId);
        processContractData.setTenantId(1L);

        final SAProcessContractData saProcessContractData = new SAProcessContractData(processContractData);

        assertThat(saProcessContractData.getTenantId()).isZero(); // not set yet by Persistence service
        assertThat(saProcessContractData.getId()).isZero();
        assertThat(saProcessContractData.getName()).isEqualTo(some_name);
        assertThat(saProcessContractData.getScopeId()).isEqualTo(processInstanceId);
        assertThat(saProcessContractData.getArchiveDate()).isZero();
        assertThat(saProcessContractData.getSourceObjectId()).isEqualTo(originalProcessDataId);
        assertThat(saProcessContractData.getValue()).isEqualTo(value);
    }

}
