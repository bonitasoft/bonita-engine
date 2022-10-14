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
package org.bonitasoft.web.rest.server.api.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.bonitasoft.engine.identity.CustomUserInfo;
import org.bonitasoft.engine.identity.impl.CustomUserInfoValueImpl;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.ModelFactory;
import org.bonitasoft.web.rest.model.identity.CustomUserInfoItem;
import org.bonitasoft.web.rest.server.engineclient.CustomUserInfoEngineClient;
import org.bonitasoft.web.rest.server.engineclient.CustomUserInfoEngineClientCreator;
import org.bonitasoft.web.rest.server.framework.APIServletCall;
import org.bonitasoft.web.toolkit.client.ItemDefinitionFactory;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Vincent Elcrin
 */
@RunWith(MockitoJUnitRunner.class)
public class APICustomUserInfoUserTest {

    @Mock(answer = Answers.RETURNS_MOCKS)
    private APIServletCall caller;

    @Mock
    private HttpSession httpSession;

    @Mock
    private APISession apiSession;

    @Mock
    private CustomUserInfoEngineClient engine;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private CustomUserInfoEngineClientCreator engineClientCreator;

    private APICustomUserInfoUser api;

    @Before
    public void setUp() throws Exception {
        ItemDefinitionFactory.setDefaultFactory(new ModelFactory());
        api = new APICustomUserInfoUser(engineClientCreator);
        api.setCaller(caller);
        given(caller.getHttpSession()).willReturn(httpSession);
        given(httpSession.getAttribute("apiSession")).willReturn(apiSession);
        given(engineClientCreator.create(apiSession)).willReturn(engine);
    }

    @Test
    public void should_retrieve_the_CustomUserInfo_for_a_given_user_id() throws Exception {
        given(engine.listCustomInformation(3L, 0, 2)).willReturn(Arrays.asList(
                new CustomUserInfo(3L, new EngineCustomUserInfoDefinition(5L), new CustomUserInfoValueImpl()),
                new CustomUserInfo(3L, new EngineCustomUserInfoDefinition(6L), new CustomUserInfoValueImpl())));
        given(engine.countDefinitions()).willReturn(2L);

        List<CustomUserInfoItem> information = api
                .search(0, 2, null, "Fix order", Collections.singletonMap("userId", "3")).getResults();

        assertThat(information.get(0).getDefinition().getId()).isEqualTo(APIID.makeAPIID(5L));
        assertThat(information.get(1).getDefinition().getId()).isEqualTo(APIID.makeAPIID(6L));
    }

    @Test
    public void should_paginate_CustomUserInfo_search() throws Exception {
        given(engine.countDefinitions()).willReturn(2L);

        api.search(2, 2, null, "Fix order", Collections.singletonMap("userId", "3")).getResults();

        verify(engine).listCustomInformation(3L, 4, 2);
    }

    @Test(expected = APIException.class)
    public void should_fail_when_passing_an_order_to_the_search() {
        api.search(0, 2, null, "NAME ASC", Collections.singletonMap("userId", "3")).getResults();
    }

    @Test(expected = APIException.class)
    public void should_fail_when_passing_a_term_to_the_search() {
        api.search(0, 2, "foo", "Fix order", Collections.singletonMap("userId", "3")).getResults();
    }

    @Test(expected = APIException.class)
    public void should_fail_when_passing_a_no_filter_to_the_search() {
        api.search(0, 2, null, "Fix order", null).getResults();
    }

    @Test(expected = APIException.class)
    public void should_fail_when_passing_wrong_filter_to_the_search() {
        api.search(0, 2, null, "Fix order", Collections.singletonMap("foo", "bar")).getResults();
    }
}
