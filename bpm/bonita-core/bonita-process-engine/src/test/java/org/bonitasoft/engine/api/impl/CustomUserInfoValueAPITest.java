package org.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.identity.CustomUserInfoValueUpdater;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoValueBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoValueUpdateBuilder;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoValueUpdateBuilderFactory;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.BDDMockito.given;
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

    @Mock
    private SCustomUserInfoValueUpdateBuilderFactory updateFactory;

    @InjectMocks
    private CustomUserInfoValueAPI api;

    @Test
    public void update_should_call_service_to_update_custom_user_info_when_exist() throws Exception {
        DummySCustomUserInfoValue value = new DummySCustomUserInfoValue(1L);
        SCustomUserInfoValueUpdateBuilder builder = createDummySCustomUserInfoValueUpdateBuilder();
        given(updateFactory.createNewInstance()).willReturn(builder);

        api.update(updateFactory, value, new CustomUserInfoValueUpdater("value"));

        verify(service).updateCustomUserInfoValue(value, builder.done());
    }

    private SCustomUserInfoValueUpdateBuilder createDummySCustomUserInfoValueUpdateBuilder() {

        return new SCustomUserInfoValueUpdateBuilder() {
            @Override
            public SCustomUserInfoValueUpdateBuilder updateValue(String value) {
                return this;
            }

            @Override
            public EntityUpdateDescriptor done() {
                return new EntityUpdateDescriptor();
            }
        };
    }
}
