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
package org.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;

import org.bonitasoft.engine.identity.CustomUserInfoValueUpdater;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoValueBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoValueUpdateBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.impl.SCustomUserInfoValueBuilderImpl;
import org.bonitasoft.engine.identity.model.builder.impl.SCustomUserInfoValueUpdateBuilderImpl;
import org.bonitasoft.engine.identity.model.impl.SCustomUserInfoValueImpl;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Vincent Elcrin
 */
@RunWith(MockitoJUnitRunner.class)
public class SCustomUserInfoValueAPITest {

    @Mock(answer = Answers.RETURNS_MOCKS)
    private SCustomUserInfoValueBuilderFactory createFactory;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private SCustomUserInfoValueUpdateBuilderFactory updateFactory;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private IdentityService service;

    @InjectMocks
    private SCustomUserInfoValueAPI api;

    @Test
    public void update_should_call_service_to_update_custom_user_info_when_exist() throws Exception {
        final DummySCustomUserInfoValue value = new DummySCustomUserInfoValue(1L);
        final SCustomUserInfoValueUpdateBuilderImpl builder = new SCustomUserInfoValueUpdateBuilderImpl(new EntityUpdateDescriptor());
        given(updateFactory.createNewInstance()).willReturn(builder);

        api.update(value, new CustomUserInfoValueUpdater("value"));

        verify(service).updateCustomUserInfoValue(value, builder.done());
    }

    @Test
    public void update_should_return_updated_value() throws Exception {
        final DummySCustomUserInfoValue updatedValue = new DummySCustomUserInfoValue(2L, 1L, 1L, "update");
        given(service.getCustomUserInfoValue(2L)).willReturn(updatedValue);

        final SCustomUserInfoValue result = api.update(new DummySCustomUserInfoValue(2L),
                new CustomUserInfoValueUpdater("value"));

        assertThat(result.getValue()).isEqualTo("update");
    }

    @Test(expected = IllegalArgumentException.class)
    public void update_should_throw_update_exception_when_value_is_null() throws Exception {
        api.update(null, new CustomUserInfoValueUpdater("value"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void update_should_throw_update_exception_when_updater_is_null() throws Exception {
        api.update(new DummySCustomUserInfoValue(1L), null);
    }

    @Test
    public void set_should_delete_CustomUserInfoValue_when_value_is_null() throws Exception {
        final SCustomUserInfoValue value = new DummySCustomUserInfoValue(254L);
        given(service.searchCustomUserInfoValue(any(QueryOptions.class))).willReturn(Arrays.asList(value));

        api.set(1L, 2L, null);

        verify(service).deleteCustomUserInfoValue(value);
    }

    @Test
    public void set_should_delete_SCustomUserInfoValue_when_value_is_empty() throws Exception {
        final SCustomUserInfoValue value = new DummySCustomUserInfoValue(254L);
        given(service.searchCustomUserInfoValue(any(QueryOptions.class))).willReturn(Arrays.asList(value));

        api.set(1L, 2L, "");

        verify(service).deleteCustomUserInfoValue(value);
    }

    @Test
    public void set_should_return_updated_value_when_value_is_deleted() throws Exception {
        given(createFactory.createNewInstance()).willReturn(new SCustomUserInfoValueBuilderImpl(new SCustomUserInfoValueImpl()));

        final SCustomUserInfoValue value = api.set(1L, 2L, "");

        assertThat(value.getDefinitionId()).isEqualTo(1L);
        assertThat(value.getUserId()).isEqualTo(2L);
        assertThat(value.getValue()).isEqualTo("");
    }

    @Test
    public void set_should_update_SCustomUserInfoValue_when_value_exist() throws Exception {
        final SCustomUserInfoValue value = new DummySCustomUserInfoValue(642L);
        given(service.searchCustomUserInfoValue(any(QueryOptions.class))).willReturn(Arrays.asList(value));

        api.set(1L, 2L, "update");

        verify(service).updateCustomUserInfoValue(eq(value), any(EntityUpdateDescriptor.class));
    }

    @Test
    public void set_should_create_SCustomUserInfoValue_when_value_doesnt_exist() throws Exception {
        given(service.searchCustomUserInfoValue(any(QueryOptions.class))).willReturn(
                Collections.<SCustomUserInfoValue> emptyList());

        api.set(5L, 3L, "update");

        verify(service).createCustomUserInfoValue(any(SCustomUserInfoValue.class));
    }
}
