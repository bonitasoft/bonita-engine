package org.bonitasoft.engine.api.impl;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.identity.CustomUserInfo;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * @author Vincent Elcrin
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomUserInfoAPITest {

    private CustomUserInfoAPI api;

    @Mock
    private IdentityService service;

    @Before
    public void setUp() throws Exception {
        api = new CustomUserInfoAPI(service);
    }

    @Test
    public void list_should_retrieve_CustomUserItems_for_a_given_user() throws Exception {
        given(service.getCustomUserInfoDefinitions(0, 2)).willReturn(
                Arrays.<SCustomUserInfoDefinition> asList(
                        new DummySCustomUserInfoDefinition(1L),
                        new DummySCustomUserInfoDefinition(2L)));

        List<CustomUserInfo> result = api.list(1, 0, 2);

        assertThat(result.get(0).getDefinition().getId()).isEqualTo(1L);
        assertThat(result.get(1).getDefinition().getId()).isEqualTo(2L);
    }
}
