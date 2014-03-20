package org.bonitasoft.engine.api.impl;

import java.util.Collections;
import java.util.Map;

import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.CustomUserInfoValue;
import org.bonitasoft.engine.identity.CustomUserInfoValueUpdater;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoValueBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoValueUpdateBuilder;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoValueUpdateBuilderFactory;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

/**
 * @author Vincent Elcrin
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomUserInfoValueAPITest {

    @Mock(answer = Answers.RETURNS_MOCKS)
    private IdentityService service;

    @Mock
    private SCustomUserInfoValueBuilderFactory createFactory;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private SCustomUserInfoValueUpdateBuilderFactory updateFactory;

    @InjectMocks
    private CustomUserInfoValueAPI api;

    @Test
    public void update_should_call_service_to_update_custom_user_info_when_exist() throws Exception {
        DummySCustomUserInfoValue value = new DummySCustomUserInfoValue(1L);
        SCustomUserInfoValueUpdateBuilder builder = createDummySCustomUserInfoValueUpdateBuilder(Collections.singletonMap("key", "value"));
        given(updateFactory.createNewInstance()).willReturn(builder);

        api.update(updateFactory, value, new CustomUserInfoValueUpdater("value"));

        verify(service).updateCustomUserInfoValue(value, builder.done());
    }

    @Test
    public void update_should_return_updated_value() throws Exception {
        DummySCustomUserInfoValue updatedValue = new DummySCustomUserInfoValue(2L, 1L, 1L, "updated");
        given(service.getCustomUserInfoValue(2L)).willReturn(updatedValue);

        CustomUserInfoValue result = api.update(updateFactory, new DummySCustomUserInfoValue(2L), new CustomUserInfoValueUpdater("value"));

        assertThat(result.getValue()).isEqualTo("updated");
    }

    @Test(expected = UpdateException.class)
    public void update_should_throw_update_exception_when_factory_is_null() throws Exception {
        api.update(null, new DummySCustomUserInfoValue(1L), new CustomUserInfoValueUpdater("value"));
    }

    @Test(expected = UpdateException.class)
    public void update_should_throw_update_exception_when_value_is_null() throws Exception {
        api.update(updateFactory, null, new CustomUserInfoValueUpdater("value"));
    }

    @Test(expected = UpdateException.class)
    public void update_should_throw_update_exception_when_updater_is_null() throws Exception {
        api.update(updateFactory, new DummySCustomUserInfoValue(1L), null);
    }

    @Test
    public void searchValue_should_return_null_when_service_seach_method_return_an_empty_list() throws Exception {
        given(service.searchCustomUserInfoValue(any(QueryOptions.class))).willReturn(Collections.<SCustomUserInfoValue> emptyList());

        SCustomUserInfoValue value = api.searchValue(1L, 2L);

        assertThat(value).isNull();
    }

    private SCustomUserInfoValueUpdateBuilder createDummySCustomUserInfoValueUpdateBuilder(final Map<String, String> fields) {

        return new SCustomUserInfoValueUpdateBuilder() {
            @Override
            public SCustomUserInfoValueUpdateBuilder updateValue(String value) {
                return this;
            }

            @Override
            public EntityUpdateDescriptor done() {
                EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
                descriptor.getFields().putAll(fields);
                return descriptor;
            }
        };
    }
}
