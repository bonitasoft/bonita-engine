/**
 * Copyright (C) 2021 Bonitasoft S.A.
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

package org.bonitasoft.permissions

import org.bonitasoft.engine.business.application.ApplicationVisibility
import org.bonitasoft.engine.business.application.impl.ApplicationImpl

import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.mock

import org.assertj.core.api.Assertions
import org.bonitasoft.engine.api.APIAccessor
import org.bonitasoft.engine.api.Logger
import org.bonitasoft.engine.api.ProfileAPI
import org.bonitasoft.engine.api.ApplicationAPI
import org.bonitasoft.engine.api.permission.APICallContext
import org.bonitasoft.engine.business.application.Application
import org.bonitasoft.engine.business.application.ApplicationMenu
import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.profile.ProfileCriterion
import org.bonitasoft.engine.profile.impl.ProfileImpl
import org.bonitasoft.engine.session.APISession
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.class)
public class ApplicationMenuPermissionRuleTest {

    @Mock
    def APISession apiSession
    @Mock
    def APICallContext apiCallContext
    @Mock
    def APIAccessor apiAccessor
    @Mock
    def Logger logger
    def ApplicationMenuPermissionRule rule = new ApplicationMenuPermissionRule()
    @Mock
    def ProfileAPI profileAPI
    @Mock
    def ApplicationAPI applicationAPI
    @Mock
    def User user
    def long currentUserId = 16l

    @Before
    public void before() {
        doReturn(profileAPI).when(apiAccessor).getProfileAPI()
        doReturn(applicationAPI).when(apiAccessor).getLivingApplicationAPI()
        doReturn(currentUserId).when(apiSession).getUserId()
    }

    @Test
    void should_return_true_when_internal_profile_ALL(){
        doReturn(true).when(apiCallContext).isGET()
        doReturn("2").when(apiCallContext).getResourceId()
        def applicationMenu = mock(ApplicationMenu.class)
        doReturn(4l).when(applicationMenu).getApplicationId()
        doReturn(applicationMenu).when(applicationAPI).getApplicationMenu(2l)
        def application = new ApplicationImpl("appToken", "1.0", "dtc")
        application.setProfileId(-1L)
        application.setVisibility(ApplicationVisibility.ALL)
        doReturn(application).when(applicationAPI).getApplication(4l)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue()
    }

    @Test
    void should_return_false_when_internal_profile_SuperAdmin_and_user_not_technical_user(){
        doReturn(true).when(apiCallContext).isGET()
        doReturn("2").when(apiCallContext).getResourceId()
        def applicationMenu = mock(ApplicationMenu.class)
        doReturn(4l).when(applicationMenu).getApplicationId()
        doReturn(applicationMenu).when(applicationAPI).getApplicationMenu(2l)
        def application = new ApplicationImpl("appToken", "1.0", "dtc")
        application.setProfileId(-1L)
        application.setVisibility(ApplicationVisibility.TECHNICAL_USER)
        doReturn(application).when(applicationAPI).getApplication(4l)
        doReturn(false).when(apiSession).isTechnicalUser()
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isFalse()
    }

    @Test
    void should_return_true_when_internal_profile_SuperAdmin_and_user_technical_user(){
        doReturn(true).when(apiCallContext).isGET()
        doReturn("2").when(apiCallContext).getResourceId()
        def applicationMenu = mock(ApplicationMenu.class)
        doReturn(4l).when(applicationMenu).getApplicationId()
        doReturn(applicationMenu).when(applicationAPI).getApplicationMenu(2l)
        def application = new ApplicationImpl("appToken", "1.0", "dtc")
        application.setProfileId(-1L)
        application.setVisibility(ApplicationVisibility.TECHNICAL_USER)
        doReturn(application).when(applicationAPI).getApplication(4l)
        doReturn(true).when(apiSession).isTechnicalUser()
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue()
    }

    @Test
    public void should_check_verify_get_with_resource_user_is_in_profile() {
        doReturn(true).when(apiCallContext).isGET()
        doReturn("2").when(apiCallContext).getResourceId()
        def applicationMenu = mock(ApplicationMenu.class)
        doReturn(4l).when(applicationMenu).getApplicationId()
        doReturn(applicationMenu).when(applicationAPI).getApplicationMenu(2l)
        def application = mock(Application.class)
        doReturn(3l).when(application).getProfileId()
        doReturn(application).when(applicationAPI).getApplication(4l)
        doReturn([profile(1), profile(2), profile(3)]).when(profileAPI).getProfilesForUser(currentUserId, 0, 100, ProfileCriterion.ID_ASC)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue()
    }

    @Test
    public void should_check_verify_get_with_resource_user_is_in_profile_with_more_than_100_elements() {
        doReturn(true).when(apiCallContext).isGET()
        doReturn("2").when(apiCallContext).getResourceId()
        def applicationMenu = mock(ApplicationMenu.class)
        doReturn(4l).when(applicationMenu).getApplicationId()
        doReturn(applicationMenu).when(applicationAPI).getApplicationMenu(2l)
        def application = mock(Application.class)
        doReturn(110l).when(application).getProfileId()
        doReturn(application).when(applicationAPI).getApplication(4l)
        doReturn((1l..100l).collect { profile(it) }).when(profileAPI).getProfilesForUser(currentUserId, 0, 100, ProfileCriterion.ID_ASC)
        doReturn((101l..110l).collect { profile(it) }).when(profileAPI).getProfilesForUser(currentUserId, 100, 100, ProfileCriterion.ID_ASC)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue()
    }

    @Test
    public void should_check_verify_get_with_resource_user_is_not_in_profile() {
        doReturn(true).when(apiCallContext).isGET()
        doReturn("2").when(apiCallContext).getResourceId()
        def applicationMenu = mock(ApplicationMenu.class)
        doReturn(4l).when(applicationMenu).getApplicationId()
        doReturn(applicationMenu).when(applicationAPI).getApplicationMenu(2l)
        def application = mock(Application.class)
        doReturn(10l).when(application).getProfileId()
        doReturn(application).when(applicationAPI).getApplication(4l)
        doReturn([profile(1), profile(3)]).when(profileAPI).getProfilesForUser(currentUserId, 0, 100, ProfileCriterion.ID_ASC)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isFalse()
    }

    @Test
    public void should_check_verify_not_get_return_false() {
        doReturn(false).when(apiCallContext).isGET()
        doReturn(true).when(apiCallContext).isPOST()
        doReturn(true).when(apiCallContext).isDELETE()
        doReturn(true).when(apiCallContext).isPUT()
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isFalse()
    }

    private ProfileImpl profile(long id) {
        def profile = new ProfileImpl("profilename")
        profile.setId(id)
        return profile
    }
}
