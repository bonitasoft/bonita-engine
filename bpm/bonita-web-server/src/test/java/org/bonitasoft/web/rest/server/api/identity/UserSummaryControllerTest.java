/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.impl.SearchFilter;
import org.bonitasoft.web.rest.server.api.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class UserSummaryControllerTest extends AbstractControllerTest<UserSummaryController> {

    @Mock
    protected IdentityAPI identityAPI;

    @Override
    protected UserSummaryController createController() {
        return spy(new UserSummaryController());
    }

    @Override
    protected void configureMocks(UserSummaryController controller) throws Exception {
        doReturn(identityAPI).when(controller).getIdentityAPI(apiSession);
    }

    private User user(long id, String userName, String firstName, String lastName, String jobTitle) {
        User user = mock(User.class);
        doReturn(id).when(user).getId();
        doReturn(userName).when(user).getUserName();
        doReturn(firstName).when(user).getFirstName();
        doReturn(lastName).when(user).getLastName();
        doReturn(jobTitle).when(user).getJobTitle();
        return user;
    }

    private void mockSearchResult(long total, User... users) throws Exception {
        SearchResult<User> searchResult = mock(SearchResult.class);
        doReturn(List.of(users)).when(searchResult).getResult();
        doReturn(total).when(searchResult).getCount();
        doReturn(searchResult).when(identityAPI).searchUsers(any(SearchOptions.class));
    }

    private ArgumentCaptor<SearchOptions> captureSearchOptions() throws Exception {
        SearchResult<User> searchResult = mock(SearchResult.class);
        doReturn(List.of()).when(searchResult).getResult();
        doReturn(0L).when(searchResult).getCount();
        ArgumentCaptor<SearchOptions> captor = ArgumentCaptor.forClass(SearchOptions.class);
        doReturn(searchResult).when(identityAPI).searchUsers(captor.capture());
        return captor;
    }

    @Test
    void searchUserSummaries_should_return_users_and_content_range() throws Exception {
        //given
        mockSearchResult(5L, user(42L, "john.doe", "John", "Doe", "Developer"));

        //when-then
        mockMvc.perform(get("/API/identity/userSummary")
                .sessionAttrs(sessionAttributes)
                .param("p", "0")
                .param("c", "10")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "0-1/5"))
                .andExpect(jsonPath("$[0].id").value("42"))
                .andExpect(jsonPath("$[0].userName").value("john.doe"))
                .andExpect(jsonPath("$[0].firstname").value("John"))
                .andExpect(jsonPath("$[0].lastname").value("Doe"))
                .andExpect(jsonPath("$[0].job_title").value("Developer"));
    }

    @Test
    void searchUserSummaries_should_serialize_id_as_string() throws Exception {
        //given
        mockSearchResult(1L, user(11125555888888L, "big.id", "Big", "Id", null));

        //when-then
        mockMvc.perform(get("/API/identity/userSummary")
                .sessionAttrs(sessionAttributes)
                .param("p", "0")
                .param("c", "10")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("11125555888888"))
                .andExpect(content().json(
                        """
                                [ { "id": "11125555888888", "userName": "big.id", "firstname": "Big", "lastname": "Id", "job_title": null } ]""",
                        true));
    }

    @Test
    void searchUserSummaries_should_pass_pagination_and_search_term() throws Exception {
        //given
        ArgumentCaptor<SearchOptions> captor = captureSearchOptions();

        //when
        mockMvc.perform(get("/API/identity/userSummary")
                .sessionAttrs(sessionAttributes)
                .param("p", "2")
                .param("c", "10")
                .param("s", "jo")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        //then
        SearchOptions options = captor.getValue();
        assertThat(options.getStartIndex()).isEqualTo(20); // page 2 * count 10
        assertThat(options.getMaxResults()).isEqualTo(10);
        assertThat(options.getSearchTerm()).isEqualTo("jo");
    }

    @Test
    void searchUserSummaries_should_not_apply_blank_search_term() throws Exception {
        //given
        ArgumentCaptor<SearchOptions> captor = captureSearchOptions();

        //when
        mockMvc.perform(get("/API/identity/userSummary")
                .sessionAttrs(sessionAttributes)
                .param("p", "0")
                .param("c", "10")
                .param("s", "   ")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        //then
        assertThat(captor.getValue().getSearchTerm()).isNull();
    }

    @Test
    void searchUserSummaries_should_default_sort_to_username_asc() throws Exception {
        //given
        ArgumentCaptor<SearchOptions> captor = captureSearchOptions();

        //when
        mockMvc.perform(get("/API/identity/userSummary")
                .sessionAttrs(sessionAttributes)
                .param("p", "0")
                .param("c", "10")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        //then
        assertThat(captor.getValue().getSorts())
                .extracting("field", "order")
                .containsExactly(tuple("userName", Order.ASC));
    }

    @Test
    void searchUserSummaries_should_map_firstname_and_lastname_sorts() throws Exception {
        //given
        ArgumentCaptor<SearchOptions> firstNameCaptor = captureSearchOptions();

        //when
        mockMvc.perform(get("/API/identity/userSummary")
                .sessionAttrs(sessionAttributes)
                .param("p", "0")
                .param("c", "10")
                .param("o", "firstname ASC")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        //then
        assertThat(firstNameCaptor.getValue().getSorts())
                .extracting("field", "order")
                .containsExactly(tuple("firstName", Order.ASC));

        //given
        ArgumentCaptor<SearchOptions> lastNameCaptor = captureSearchOptions();

        //when
        mockMvc.perform(get("/API/identity/userSummary")
                .sessionAttrs(sessionAttributes)
                .param("p", "0")
                .param("c", "10")
                .param("o", "lastname DESC")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        //then
        assertThat(lastNameCaptor.getValue().getSorts())
                .extracting("field", "order")
                .containsExactly(tuple("lastName", Order.DESC));
    }

    @Test
    void searchUserSummaries_should_default_direction_to_asc_when_only_field_given() throws Exception {
        //given
        ArgumentCaptor<SearchOptions> captor = captureSearchOptions();

        //when
        mockMvc.perform(get("/API/identity/userSummary")
                .sessionAttrs(sessionAttributes)
                .param("p", "0")
                .param("c", "10")
                .param("o", "firstname")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        //then
        assertThat(captor.getValue().getSorts())
                .extracting("field", "order")
                .containsExactly(tuple("firstName", Order.ASC));
    }

    @Test
    void searchUserSummaries_should_filter_enabled_users() throws Exception {
        //given
        ArgumentCaptor<SearchOptions> captor = captureSearchOptions();

        //when
        mockMvc.perform(get("/API/identity/userSummary")
                .sessionAttrs(sessionAttributes)
                .param("p", "0")
                .param("c", "10")
                .param("f", "enabled=true")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        //then
        assertThat(captor.getValue().getFilters())
                .extracting(SearchFilter::getField, SearchFilter::getValue)
                .containsExactly(tuple("enabled", true));
    }

    @Test
    void searchUserSummaries_should_filter_disabled_users() throws Exception {
        //given
        ArgumentCaptor<SearchOptions> captor = captureSearchOptions();

        //when
        mockMvc.perform(get("/API/identity/userSummary")
                .sessionAttrs(sessionAttributes)
                .param("p", "0")
                .param("c", "10")
                .param("f", "enabled=false")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        //then
        assertThat(captor.getValue().getFilters())
                .extracting(SearchFilter::getField, SearchFilter::getValue)
                .containsExactly(tuple("enabled", false));
    }

    @Test
    void searchUserSummaries_should_not_filter_when_no_filter_provided() throws Exception {
        //given
        ArgumentCaptor<SearchOptions> captor = captureSearchOptions();

        //when
        mockMvc.perform(get("/API/identity/userSummary")
                .sessionAttrs(sessionAttributes)
                .param("p", "0")
                .param("c", "10")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        //then
        assertThat(captor.getValue().getFilters()).isEmpty();
    }

    @Test
    void searchUserSummaries_should_reject_unsupported_sort_field() throws Exception {
        //when-then
        mockMvc.perform(get("/API/identity/userSummary")
                .sessionAttrs(sessionAttributes)
                .param("p", "0")
                .param("c", "10")
                .param("o", "email ASC")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchUserSummaries_should_apply_compound_sort_in_order() throws Exception {
        //given
        ArgumentCaptor<SearchOptions> captor = captureSearchOptions();

        //when
        mockMvc.perform(get("/API/identity/userSummary")
                .sessionAttrs(sessionAttributes)
                .param("p", "0")
                .param("c", "10")
                .param("o", "lastname ASC, firstname DESC")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        //then
        assertThat(captor.getValue().getSorts())
                .extracting("field", "order")
                .containsExactly(tuple("lastName", Order.ASC), tuple("firstName", Order.DESC));
    }

    @Test
    void searchUserSummaries_should_reject_compound_sort_with_an_unknown_field() throws Exception {
        //when-then
        mockMvc.perform(get("/API/identity/userSummary")
                .sessionAttrs(sessionAttributes)
                .param("p", "0")
                .param("c", "10")
                .param("o", "username ASC, email DESC")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchUserSummaries_should_reject_unknown_filter_key() throws Exception {
        //when-then
        mockMvc.perform(get("/API/identity/userSummary")
                .sessionAttrs(sessionAttributes)
                .param("p", "0")
                .param("c", "10")
                .param("f", "role=1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchUserSummaries_should_reject_non_boolean_enabled_filter() throws Exception {
        //when-then
        mockMvc.perform(get("/API/identity/userSummary")
                .sessionAttrs(sessionAttributes)
                .param("p", "0")
                .param("c", "10")
                .param("f", "enabled=maybe")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchUserSummaries_should_reject_sort_clause_with_extra_tokens() throws Exception {
        //when-then
        mockMvc.perform(get("/API/identity/userSummary")
                .sessionAttrs(sessionAttributes)
                .param("p", "0")
                .param("c", "10")
                .param("o", "username DESC EXTRA")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchUserSummaries_should_reject_duplicate_filter_keys() throws Exception {
        //when-then
        mockMvc.perform(get("/API/identity/userSummary")
                .sessionAttrs(sessionAttributes)
                .param("p", "0")
                .param("c", "10")
                .param("f", "enabled=true")
                .param("f", "enabled=false")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchUserSummaries_should_reject_filter_without_value() throws Exception {
        //when-then
        mockMvc.perform(get("/API/identity/userSummary")
                .sessionAttrs(sessionAttributes)
                .param("p", "0")
                .param("c", "10")
                .param("f", "enabled")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchUserSummaries_should_reject_filter_with_empty_key() throws Exception {
        //when-then
        mockMvc.perform(get("/API/identity/userSummary")
                .sessionAttrs(sessionAttributes)
                .param("p", "0")
                .param("c", "10")
                .param("f", "=")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchUserSummaries_should_reject_filter_with_blank_key_and_value() throws Exception {
        //when-then
        mockMvc.perform(get("/API/identity/userSummary")
                .sessionAttrs(sessionAttributes)
                .param("p", "0")
                .param("c", "10")
                .param("f", "=value")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchUserSummaries_should_reject_request_when_page_param_is_missing() throws Exception {
        //when-then
        mockMvc.perform(get("/API/identity/userSummary")
                .sessionAttrs(sessionAttributes)
                .param("c", "10")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchUserSummaries_should_reject_request_when_count_param_is_missing() throws Exception {
        //when-then
        mockMvc.perform(get("/API/identity/userSummary")
                .sessionAttrs(sessionAttributes)
                .param("p", "0")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

}
