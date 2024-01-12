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
package org.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;

import org.bonitasoft.engine.identity.CustomUserInfoValueUpdater;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoValueUpdateBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.impl.SCustomUserInfoValueUpdateBuilderImpl;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Vincent Elcrin
 */
@RunWith(MockitoJUnitRunner.class)
public class SCustomUserInfoValueAPITest {

    @Mock(answer = Answers.RETURNS_MOCKS)
    private SCustomUserInfoValueUpdateBuilderFactory updateFactory;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private IdentityService service;

    @InjectMocks
    private SCustomUserInfoValueAPI api;

    @Test
    public void update_should_call_service_to_update_custom_user_info_when_exist() throws Exception {
        final SCustomUserInfoValue value = SCustomUserInfoValue.builder().id(1).build();
        final SCustomUserInfoValueUpdateBuilderImpl builder = new SCustomUserInfoValueUpdateBuilderImpl(
                new EntityUpdateDescriptor());
        given(updateFactory.createNewInstance()).willReturn(builder);

        api.update(value, new CustomUserInfoValueUpdater("value"));

        verify(service).updateCustomUserInfoValue(value, builder.done());
    }

    @Test
    public void update_should_return_updated_value() throws Exception {
        given(service.getCustomUserInfoValue(2L)).willReturn(SCustomUserInfoValue.builder().value("updated").build());

        final SCustomUserInfoValue result = api.update(SCustomUserInfoValue.builder().id(2).build(),
                new CustomUserInfoValueUpdater("value"));

        assertThat(result.getValue()).isEqualTo("updated");
    }

    @Test(expected = IllegalArgumentException.class)
    public void update_should_throw_update_exception_when_value_is_null() throws Exception {
        api.update(null, new CustomUserInfoValueUpdater("value"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void update_should_throw_update_exception_when_updater_is_null() throws Exception {
        api.update(new SCustomUserInfoValue(), null);
    }

    @Test
    public void set_should_delete_CustomUserInfoValue_when_value_is_null() throws Exception {
        final SCustomUserInfoValue value = SCustomUserInfoValue.builder().id(254).build();
        given(service.getCustomUserInfoValueOfUserAndDefinitions(2L, Collections.singletonList(1L)))
                .willReturn(Arrays.asList(value));

        api.set(1L, 2L, null);

        verify(service).deleteCustomUserInfoValue(value);
    }

    @Test
    public void set_should_delete_SCustomUserInfoValue_when_value_is_empty() throws Exception {
        final SCustomUserInfoValue value = SCustomUserInfoValue.builder().id(254).build();
        given(service.getCustomUserInfoValueOfUserAndDefinitions(2L, Collections.singletonList(1L)))
                .willReturn(Arrays.asList(value));

        api.set(1L, 2L, "");

        verify(service).deleteCustomUserInfoValue(value);
    }

    @Test
    public void set_should_return_updated_value_when_value_is_deleted() throws Exception {
        final SCustomUserInfoValue value = api.set(1L, 2L, "");

        assertThat(value.getDefinitionId()).isEqualTo(1L);
        assertThat(value.getUserId()).isEqualTo(2L);
        assertThat(value.getValue()).isEqualTo("");
    }

    @Test
    public void set_should_update_SCustomUserInfoValue_when_value_exist() throws Exception {
        final SCustomUserInfoValue value = SCustomUserInfoValue.builder().id(254).build();
        given(service.getCustomUserInfoValueOfUserAndDefinitions(2L, Collections.singletonList(1L)))
                .willReturn(Arrays.asList(value));

        api.set(1L, 2L, "update");

        verify(service).updateCustomUserInfoValue(eq(value), any(EntityUpdateDescriptor.class));
    }

    @Test
    public void set_should_create_SCustomUserInfoValue_when_value_doesnt_exist() throws Exception {

        api.set(5L, 3L, "update");

        verify(service).createCustomUserInfoValue(any(SCustomUserInfoValue.class));
    }
}
