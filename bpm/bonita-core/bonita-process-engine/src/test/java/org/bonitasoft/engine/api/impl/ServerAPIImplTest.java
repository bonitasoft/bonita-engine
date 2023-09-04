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

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.api.NoSessionRequired;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.TenantAdministrationAPI;
import org.bonitasoft.engine.api.impl.transaction.CustomTransactions;
import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.core.login.LoginService;
import org.bonitasoft.engine.core.platform.login.PlatformLoginService;
import org.bonitasoft.engine.exception.*;
import org.bonitasoft.engine.platform.PlatformManager;
import org.bonitasoft.engine.platform.PlatformState;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.service.APIAccessResolver;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.*;
import org.bonitasoft.engine.session.impl.APISessionImpl;
import org.bonitasoft.engine.session.impl.PlatformSessionImpl;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.hamcrest.CustomTypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 * @author Aurelien Pupier
 */
@RunWith(MockitoJUnitRunner.class)
public class ServerAPIImplTest {

    private static final long TENANT_SESSION_ID = 54335453241L;
    private static final long PLATFORM_SESSION_ID = 54335453241L;
    private APISession tenantSession = new APISessionImpl(TENANT_SESSION_ID, new Date(), 10000, "john", 14L,
            "theTenant", 42L);
    private PlatformSession platformSession = new PlatformSessionImpl(PLATFORM_SESSION_ID, new Date(), 10000, "john",
            14L);
    @Mock
    private SessionAccessor sessionAccessor;
    @Mock
    private UserTransactionService userTransactionService;
    @Mock
    private ServiceAccessorFactory serviceAccessorFactory;
    @Mock
    private ServiceAccessor serviceAccessor;
    @Mock
    private LoginService tenantLoginService;
    @Mock
    private SchedulerService schedulerService;
    @Mock
    private SessionService sessionService;
    @Mock
    private ClassLoaderService classLoaderService;
    @Mock
    private PlatformLoginService platformLoginService;
    @Mock
    private PlatformSessionService platformSessionService;
    @Mock
    private PlatformManager platformManager;

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    private ServerAPIImpl serverAPIImpl;
    private MyApiImplementation myApi = new MyApiImplementation();
    private MyApiWithNoSessionImpl myApiWithNoSession = new MyApiWithNoSessionImpl();
    private ApiFullyAccessibleWhenTenantIsPausedImpl myApiFullyAccessibleWhenTenantIsPaused = new ApiFullyAccessibleWhenTenantIsPausedImpl();
    private APIAccessResolver accessResolver;
    @Mock
    private PlatformAPI platformApi;
    @Mock
    private TenantAdministrationAPI tenantAdministrationApi;

    private boolean isTenantPaused = false;

    @Before
    public void createServerAPI() throws Exception {
        doReturn(true).when(platformApi).isNodeStarted();
        when(userTransactionService.executeInTransaction(any()))
                .thenAnswer(invocation -> ((Callable<?>) invocation.getArgument(0)).call());
        doReturn(serviceAccessor).when(serviceAccessorFactory).createServiceAccessor();
        doReturn(sessionAccessor).when(serviceAccessorFactory).createSessionAccessor();
        doReturn(schedulerService).when(serviceAccessor).getSchedulerService();
        doReturn(platformLoginService).when(serviceAccessor).getPlatformLoginService();
        doReturn(platformSessionService).when(serviceAccessor).getPlatformSessionService();
        doReturn(platformManager).when(serviceAccessor).getPlatformManager();
        doReturn(tenantLoginService).when(serviceAccessor).getLoginService();
        doReturn(sessionService).when(serviceAccessor).getSessionService();
        doReturn(classLoaderService).when(serviceAccessor).getClassLoaderService();

        doReturn(true).when(tenantLoginService).isValid(TENANT_SESSION_ID);
        doReturn(true).when(platformLoginService).isValid(PLATFORM_SESSION_ID);

        doAnswer(invocation -> isTenantPaused).when(tenantAdministrationApi).isPaused();

        accessResolver = new APIAccessResolver() {

            @Override
            public <T> T getAPIImplementation(Class<T> apiInterface) throws APIImplementationNotFoundException {
                if (apiInterface.isAssignableFrom(MyApi.class)) {
                    return (T) myApi;
                } else if (apiInterface.equals(MyApiWithNoSession.class)) {
                    return (T) myApiWithNoSession;
                } else if (apiInterface.equals(ApiFullyAccessibleWhenTenantIsPaused.class)) {
                    return (T) myApiFullyAccessibleWhenTenantIsPaused;
                } else if (apiInterface.equals(PlatformAPI.class)) {
                    return (T) platformApi;
                } else if (apiInterface.equals(TenantAdministrationAPI.class)) {
                    return (T) tenantAdministrationApi;
                } else {
                    throw new APIImplementationNotFoundException("not the FakeApi");
                }

            }
        };
        serverAPIImpl = new ServerAPIImpl(accessResolver) {

            @Override
            UserTransactionService selectUserTransactionService(Session session, SessionType sessionType)
                    throws BonitaHomeNotSetException, InstantiationException, IllegalAccessException,
                    ClassNotFoundException, IOException, BonitaHomeConfigurationException {
                return userTransactionService;
            }

            @Override
            ServiceAccessorFactory getServiceAccessorFactoryInstance() {
                return serviceAccessorFactory;
            }
        };
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void should_throw_ClassNotFoundException_when_api_is_not_known() throws Throwable {

        expectedException.expect(ServerWrappedException.class);
        expectedException.expectCause(
                new CustomTypeSafeMatcher<Throwable>("should be a runtime caused by a ClassNotFoundException") {

                    @Override
                    protected boolean matchesSafely(Throwable throwable) {
                        return throwable instanceof BonitaRuntimeException
                                && throwable.getCause() instanceof ClassNotFoundException;
                    }
                });

        serverAPIImpl.invokeMethod(options(tenantSession), "UnknownApi", "someMethod", emptyList(), null);
    }

    @Test
    public void should_not_open_transaction_on_CustomTransaction_annotated_methods() throws Throwable {

        serverAPIImpl.invokeMethod(options(tenantSession), MyApi.class.getName(), "customTxAPIMethod", emptyList(),
                null);

        assertThat(myApi.customTxAPIMethodCalled).isTrue();
        //only one call: "tenantAdministrationApi.isTenantPaused"
        verify(userTransactionService, only()).executeInTransaction(any());
    }

    @Test
    public void should_be_able_to_call_methods_that_dont_require_sessions_without_session() throws Throwable {

        serverAPIImpl.invokeMethod(emptyMap(), MyApiWithNoSession.class.getName(), "someMethod", emptyList(), null);

        assertThat(myApiWithNoSession.someMethodCalled).isTrue();
    }

    @Test
    public void should_call_normal_method_in_transaction() throws Throwable {

        serverAPIImpl.invokeMethod(options(tenantSession), MyApi.class.getName(), "notAnnotatedMethod", emptyList(),
                null);

        assertThat(myApi.notAnnotatedMethodCalled).isTrue();
        verify(userTransactionService).executeInTransaction(any());
    }

    @Test
    public void should_fail_when_calling_normal_method_without_session() throws Throwable {

        expectedException.expectCause(instanceOf(InvalidSessionException.class));
        serverAPIImpl.invokeMethod(options(null), MyApi.class.getName(), "notAnnotatedMethod", emptyList(), null);
    }

    @Test
    public void should_not_be_able_to_call_normal_method_when_tenant_is_paused() throws Exception {
        isTenantPaused = true;

        expectedException.expectCause(instanceOf(TenantStatusException.class));
        serverAPIImpl.invokeMethod(options(tenantSession), MyApi.class.getName(), "notAnnotatedMethod", emptyList(),
                null);
    }

    @Test
    public void should_be_able_to_call_method_with_AvailableWhenTenantIsPaused_when_tenant_is_paused()
            throws Exception {
        isTenantPaused = true;

        serverAPIImpl.invokeMethod(options(tenantSession), MyApi.class.getName(), "availableWhenTenantIsPaused",
                emptyList(), null);

        assertThat(myApi.availableWhenTenantIsPausedCalled).isTrue();
    }

    @Test
    public void should_be_able_to_call_method_with_AvailableWhenTenantIsPaused_when_tenant_is_not_paused()
            throws Exception {
        isTenantPaused = false;

        serverAPIImpl.invokeMethod(options(tenantSession), MyApi.class.getName(), "availableWhenTenantIsPaused",
                emptyList(), null);

        assertThat(myApi.availableWhenTenantIsPausedCalled).isTrue();
    }

    @Test
    public void should_not_be_able_to_call_method_with_OnlyAvailableWhenTenantIsPaused_when_tenant_is_not_paused()
            throws Exception {
        isTenantPaused = false;

        expectedException.expectCause(instanceOf(TenantStatusException.class));

        serverAPIImpl.invokeMethod(options(tenantSession), MyApi.class.getName(), "onlyAvailableWhenTenantIsPaused",
                emptyList(), null);
    }

    @Test
    public void should_be_able_to_call_method_with_OnlyAvailableWhenTenantIsPaused_when_tenant_is_paused()
            throws Exception {
        isTenantPaused = true;

        serverAPIImpl.invokeMethod(options(tenantSession), MyApi.class.getName(), "onlyAvailableWhenTenantIsPaused",
                emptyList(), null);

        assertThat(myApi.onlyAvailableWhenTenantIsPausedCalled).isTrue();
    }

    @Test
    public void should_be_able_to_call_method_of_api_fully_available_when_tenant_is_paused() throws Exception {
        isTenantPaused = true;

        //There is no real check in server API whether it is a tenant or a platform api, we only check the type of session
        serverAPIImpl.invokeMethod(options(tenantSession), ApiFullyAccessibleWhenTenantIsPaused.class.getName(),
                "aMethod", emptyList(), null);

        assertThat(myApiFullyAccessibleWhenTenantIsPaused.aMethodCalled).isTrue();
    }

    @Test
    public void should_be_able_to_call_platform_apis_when_tenant_is_paused() throws Exception {
        isTenantPaused = true;

        //There is no real check in server API whether it is a tenant or a platform api, we only check the type of session
        serverAPIImpl.invokeMethod(options(platformSession), MyApi.class.getName(), "notAnnotatedMethod", emptyList(),
                null);

        assertThat(myApi.notAnnotatedMethodCalled).isTrue();
    }

    @Test
    public void should_fail_if_tenant_session_is_not_valid() throws Exception {
        doReturn(false).when(tenantLoginService).isValid(TENANT_SESSION_ID);

        expectedException.expectCause(instanceOf(InvalidSessionException.class));
        serverAPIImpl.invokeMethod(options(tenantSession), MyApi.class.getName(), "notAnnotatedMethod", emptyList(),
                null);
    }

    @Test
    public void should_fail_if_platform_session_is_not_valid() throws Exception {
        doReturn(false).when(platformLoginService).isValid(PLATFORM_SESSION_ID);

        expectedException.expectCause(instanceOf(InvalidSessionException.class));
        serverAPIImpl.invokeMethod(options(platformSession), MyApi.class.getName(), "notAnnotatedMethod", emptyList(),
                null);
    }

    @Test
    public void should_renew_tenant_session_when_call_is_ok() throws Exception {

        serverAPIImpl.invokeMethod(options(tenantSession), MyApi.class.getName(), "notAnnotatedMethod", emptyList(),
                null);

        verify(sessionService).renewSession(TENANT_SESSION_ID);
    }

    @Test
    public void should_renew_platform_session_when_call_is_ok() throws Exception {
        doReturn(PlatformState.STARTING).when(platformManager).getState();
        serverAPIImpl.invokeMethod(options(platformSession), MyApi.class.getName(), "notAnnotatedMethod", emptyList(),
                null);

        verify(platformSessionService).renewSession(PLATFORM_SESSION_ID);
    }

    @Test
    public void should_checkMethodAccessibility_do_not_warn_user_when_method_is_not_deprecated() throws Throwable {
        //given
        final Method callMe = MyApiImplementation.class.getDeclaredMethod("callMeNew");
        systemOutRule.clearLog();
        //when
        serverAPIImpl.checkMethodAccessibility(new MyApiImplementation(), MyApi.class.getName(), callMe, null, true);

        //then
        assertThat(systemOutRule.getLog()).doesNotContain("is deprecated");
    }

    @Test
    public void should_checkMethodAccessibility_warn_user_when_method_is_deprecated() throws Throwable {
        //given
        final Method callMe = MyApiImplementation.class.getDeclaredMethod("callMeOld");
        systemOutRule.clearLog();

        //when
        serverAPIImpl.checkMethodAccessibility(new MyApiImplementation(), MyApi.class.getName(), callMe, null, true);

        //then

        assertThat(systemOutRule.getLog())
                .contains("The API method " + this.getClass().getName() + "$MyApi.callMeOld is deprecated.");
    }

    @Test
    public void should_throw_ServerWrappedException_when_some_exception_is_thrown() throws Throwable {
        expectedException.expect(ServerWrappedException.class);
        expectedException.expectCause(instanceOf(BonitaRuntimeException.class));

        serverAPIImpl.invokeMethod(options(tenantSession), MyApi.class.getName(), "methodThatThrowRuntimeException",
                emptyList(), null);
    }

    private Map<String, Serializable> options(Session session) {
        final Map<String, Serializable> options = new HashMap<>();
        options.put("session", session);
        return options;
    }

    // ----
    // APIS for tests
    // ----

    interface ApiFullyAccessibleWhenTenantIsPaused {

        void aMethod();
    }

    @AvailableWhenTenantIsPaused
    static class ApiFullyAccessibleWhenTenantIsPausedImpl implements ApiFullyAccessibleWhenTenantIsPaused {

        boolean aMethodCalled;

        @Override
        public void aMethod() {
            aMethodCalled = true;
        }
    }

    @NoSessionRequired
    interface MyApiWithNoSession {

        void someMethod();
    }

    static class MyApiWithNoSessionImpl implements MyApiWithNoSession {

        boolean someMethodCalled;

        @Override
        public void someMethod() {
            someMethodCalled = true;
        }
    }

    interface MyApi {

        @Deprecated
        @AvailableOnStoppedNode
        void callMeOld();

        @AvailableOnStoppedNode
        void callMeNew();

        void availableWhenTenantIsPaused();

        void onlyAvailableWhenTenantIsPaused();

        void customTxAPIMethod();

        void notAnnotatedMethod();

        void methodThatThrowRuntimeException();
    }

    static class MyApiImplementation implements MyApi {

        boolean customTxAPIMethodCalled;
        boolean notAnnotatedMethodCalled;
        boolean availableWhenTenantIsPausedCalled;
        boolean onlyAvailableWhenTenantIsPausedCalled;

        @Deprecated
        @AvailableOnStoppedNode
        public void callMeOld() {

        }

        @AvailableOnStoppedNode
        public void callMeNew() {

        }

        @AvailableWhenTenantIsPaused
        public void availableWhenTenantIsPaused() {
            availableWhenTenantIsPausedCalled = true;
        }

        @AvailableWhenTenantIsPaused(onlyAvailableWhenPaused = true)
        public void onlyAvailableWhenTenantIsPaused() {
            onlyAvailableWhenTenantIsPausedCalled = true;
        }

        @CustomTransactions
        public void customTxAPIMethod() {
            customTxAPIMethodCalled = true;
        }

        public void notAnnotatedMethod() {
            notAnnotatedMethodCalled = true;

        }

        @Override
        public void methodThatThrowRuntimeException() {
            throw new BonitaRuntimeException("some exception");
        }
    }

}
