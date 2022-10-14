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
package org.bonitasoft.web.rest.server.datastore.applicationmenu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.io.Serializable;

import org.bonitasoft.web.rest.server.datastore.filter.Filter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationMenuFilterCreatorTest {

    @Mock
    private ApplicationMenuSearchDescriptorConverter converter;

    @InjectMocks
    private ApplicationMenuFilterCreator creator;

    @Test
    public void should_return_filter_based_on_given_field_and_value_on_create() throws Exception {
        //given
        given(converter.convert("name")).willReturn("name");

        //when
        final Filter<? extends Serializable> filter = creator.create("name", "a name");

        //then
        assertThat(filter).isNotNull();
        assertThat(filter.getField()).isEqualTo("name");
        assertThat(filter.getValue()).isEqualTo("a name");
    }

}
