/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api.form;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.form.FormMapping;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FormMappingItemTest {

    @Mock
    FormMapping formMapping;

    @Test
    public void it_should_handle_a_null_pageId() {
        //given
        when(formMapping.getPageId()).thenReturn(null);

        //when
        FormMappingItem formMappingItem = new FormMappingItem(formMapping);

        assertThat(formMappingItem.getPageId()).isEqualTo(null);
    }

    @Test
    public void it_should_properly_handle_long_values() {
        //given
        when(formMapping.getId()).thenReturn(1L);
        when(formMapping.getProcessDefinitionId()).thenReturn(885556662223334788L);
        when(formMapping.getLastUpdatedBy()).thenReturn(2L);
        when(formMapping.getPageId()).thenReturn(2L);

        //when
        FormMappingItem formMappingItem = new FormMappingItem(formMapping);

        assertThat(formMappingItem.getId()).isEqualTo("1");
        assertThat(formMappingItem.getProcessDefinitionId()).isEqualTo("885556662223334788");
        assertThat(formMappingItem.getLastUpdatedBy()).isEqualTo("2");
        assertThat(formMappingItem.getPageId()).isEqualTo("2");
    }

}
