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
package org.bonitasoft.engine.classloader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.bonitasoft.engine.classloader.ClassLoaderIdentifier.GLOBAL;
import static org.bonitasoft.engine.classloader.ClassLoaderIdentifier.identifier;
import static org.bonitasoft.engine.dependency.model.ScopeType.PROCESS;
import static org.bonitasoft.engine.dependency.model.ScopeType.TENANT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.bonitasoft.engine.dependency.impl.PlatformDependencyService;
import org.bonitasoft.engine.dependency.impl.TenantDependencyService;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class ClassLoaderServiceImplTest {

    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Mock
    private EventService eventService;
    @Mock
    private PlatformDependencyService platformDependencyService;
    @Mock
    private TenantDependencyService tenantDependencyService;
    @Mock
    private SessionAccessor sessionAccessor;
    @Mock
    private UserTransactionService userTransactionService;
    @Mock
    private BroadcastService broadcastService;
    @Mock
    private ClassLoaderUpdater classLoaderUpdater;

    @Mock
    private PlatformClassLoaderListener platformClassLoaderListener1;
    @Mock
    private PlatformClassLoaderListener platformClassLoaderListener2;

    @Captor
    private ArgumentCaptor<RefreshClassloaderSynchronization> synchronizationArgumentCaptor;

    private ClassLoaderServiceImpl classLoaderService;
    private ClassLoader testClassLoader;
    private BonitaClassLoader processClassLoader;
    private MyClassLoaderListener myClassLoaderListener;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private final long PROCESS_ID = 12;
    private final long TENANT_ID = 13;

    @Before
    public void before() throws Exception {
        classLoaderService = new ClassLoaderServiceImpl(new ParentClassLoaderResolver(sessionAccessor),
                eventService,
                platformDependencyService, sessionAccessor, userTransactionService, broadcastService,
                classLoaderUpdater, Arrays.asList(platformClassLoaderListener1, platformClassLoaderListener2));

        when(classLoaderUpdater.initializeClassLoader(eq(classLoaderService), any()))
                .thenAnswer(a -> classLoaderService.createClassloader(a.getArgument(1)));
        doReturn(TENANT_ID).when(sessionAccessor).getTenantId();
        classLoaderService.registerDependencyServiceOfTenant(TENANT_ID, tenantDependencyService);
        processClassLoader = classLoaderService.getClassLoader(identifier(PROCESS, PROCESS_ID));
        classLoaderService.getClassLoader(identifier(TENANT, TENANT_ID));
        testClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(processClassLoader);
        myClassLoaderListener = new MyClassLoaderListener();
        temporaryFolder.create();
    }

    @After
    public void after() {
        Thread.currentThread().setContextClassLoader(testClassLoader);
    }

    @Test
    public void should_addListener_add_on_specified_classloader_do_not_call_on_others() {
        //given
        classLoaderService.addListener(identifier(TENANT, TENANT_ID), myClassLoaderListener);
        //when
        processClassLoader.destroy();
        //then
        assertThat(myClassLoaderListener.isOnDestroyCalled()).isFalse();
    }

    @Test
    public void should_addListener_add_on_specified_classloader_call_listener() throws SClassLoaderException {
        //given
        classLoaderService.addListener(identifier(PROCESS, PROCESS_ID), myClassLoaderListener);
        //when
        classLoaderService.removeLocalClassloader(identifier(PROCESS, PROCESS_ID));
        //then
        assertThat(myClassLoaderListener.isOnDestroyCalled()).isTrue();
    }

    @Test
    public void should_not_be_able_to_destroy_classloader_having_children() {

        assertThatThrownBy(() -> classLoaderService.removeLocalClassloader(identifier(TENANT, TENANT_ID)))
                .hasMessageContaining(
                        "Unable to delete classloader TENANT:13 because it has children: [BonitaClassLoader[id=PROCESS:12");
    }

    @Test
    public void should_removeListener_remove_the_listener() {
        //given
        classLoaderService.addListener(identifier(TENANT, TENANT_ID), myClassLoaderListener);
        classLoaderService.removeListener(identifier(TENANT, TENANT_ID), myClassLoaderListener);
        //when
        processClassLoader.destroy();
        //then
        assertThat(myClassLoaderListener.isOnDestroyCalled()).isFalse();

    }

    @Test
    public void should_refreshClassLoader_call_replace_classloader() throws Exception {
        //given
        classLoaderService.addListener(identifier(PROCESS, PROCESS_ID), myClassLoaderListener);
        //when
        classLoaderService.refreshClassLoaderImmediately(identifier(PROCESS, PROCESS_ID));
        //then
        assertThat(myClassLoaderListener.isOnUpdateCalled()).isTrue();
        assertThat(myClassLoaderListener.isOnDestroyCalled()).isTrue();
    }

    @Test
    public void should_stop_destroy_all_classloaders() throws Exception {
        //given
        classLoaderService.addListener(identifier(PROCESS, PROCESS_ID), myClassLoaderListener);
        classLoaderService.addListener(identifier(TENANT, TENANT_ID), myClassLoaderListener);
        classLoaderService.getClassLoader(identifier(PROCESS, 125));
        classLoaderService.addListener(identifier(PROCESS, 125), myClassLoaderListener);
        classLoaderService.getClassLoader(identifier(PROCESS, 126));
        classLoaderService.addListener(identifier(PROCESS, 126), myClassLoaderListener);
        classLoaderService.getClassLoader(identifier(PROCESS, 127));
        classLoaderService.addListener(identifier(PROCESS, 127), myClassLoaderListener);
        classLoaderService.getClassLoader(identifier(PROCESS, 128));
        classLoaderService.addListener(identifier(PROCESS, 128), myClassLoaderListener);
        classLoaderService.getClassLoader(identifier(PROCESS, 129));
        classLoaderService.addListener(identifier(PROCESS, 129), myClassLoaderListener);
        classLoaderService.getClassLoader(identifier(PROCESS, 130));
        classLoaderService.addListener(identifier(PROCESS, 130), myClassLoaderListener);
        //when
        classLoaderService.stop();
        //then
        assertThat(myClassLoaderListener.getOnUpdateCalled()).isEqualTo(0);
        assertThat(myClassLoaderListener.getOnDestroyCalled()).isEqualTo(8);
    }

    @Test
    public void should_removeLocalClassLoader_call_destroy() throws Exception {
        //given
        classLoaderService.addListener(identifier(PROCESS, PROCESS_ID), myClassLoaderListener);
        classLoaderService.addListener(identifier(TENANT, TENANT_ID), myClassLoaderListener);
        //when
        classLoaderService.removeLocalClassloader(identifier(PROCESS, PROCESS_ID));

        //then
        assertThat(myClassLoaderListener.getOnDestroyCalled()).isEqualTo(1);
    }

    @Test(expected = SClassLoaderException.class)
    public void should_removeLocalClassLoader_throw_exception_if_parent_not_removed() throws Exception {
        //given
        classLoaderService.getClassLoader(identifier(PROCESS, 17));//second classloader
        //when
        classLoaderService.removeLocalClassloader(identifier(PROCESS, PROCESS_ID));
        classLoaderService.removeLocalClassloader(identifier(TENANT, TENANT_ID));
    }

    @Test
    public void should_removeLocalClassLoader_work_if_remove_in_right_order() throws Exception {
        //given
        //when
        classLoaderService.removeLocalClassloader(identifier(PROCESS, PROCESS_ID));
        classLoaderService.removeLocalClassloader(identifier(TENANT, TENANT_ID));
    }

    @Test
    public void should_getLocalClassLoader_create_expected_hierarchy() {
        //given
        BonitaClassLoader localClassLoader = classLoaderService.getClassLoader(identifier(PROCESS, PROCESS_ID));
        //when

        assertThat(localClassLoader.getIdentifier()).isEqualTo(identifier(PROCESS, PROCESS_ID));
        ClassLoader parent = localClassLoader.getParent();
        assertThat(parent).isInstanceOf(BonitaClassLoader.class);
        assertThat(((BonitaClassLoader) parent).getIdentifier())
                .isEqualTo(identifier(TENANT, TENANT_ID));
        ClassLoader global = parent.getParent();
        assertThat(global).isInstanceOf(BonitaClassLoader.class);
        assertThat(((BonitaClassLoader) global).getIdentifier()).isEqualTo(ClassLoaderIdentifier.GLOBAL);
        ClassLoader root = global.getParent();
        assertThat(root).isNotInstanceOf(BonitaClassLoader.class);
    }

    @Test
    public void should_globalListeners_be_called_on_destroy() throws Exception {
        //given
        classLoaderService.getClassLoader(identifier(PROCESS, 17));//second classloader
        //when
        classLoaderService.removeLocalClassloader(identifier(PROCESS, PROCESS_ID));
        classLoaderService.removeLocalClassloader(identifier(PROCESS, 17));

        //then
        verify(platformClassLoaderListener1, times(2)).onDestroy(any(BonitaClassLoader.class));
        verify(platformClassLoaderListener2, times(2)).onDestroy(any(BonitaClassLoader.class));
    }

    @Test
    public void should_globalListeners_be_called_on_update() throws Exception {
        //given
        classLoaderService.getClassLoader(identifier(PROCESS, 17));//second classloader
        //when
        classLoaderService.refreshClassLoaderImmediately(identifier(PROCESS, PROCESS_ID));
        classLoaderService.refreshClassLoaderImmediately(identifier(PROCESS, 17));

        //then
        verify(platformClassLoaderListener1, times(2)).onUpdate(
                any(BonitaClassLoader.class));
        verify(platformClassLoaderListener2, times(2)).onUpdate(
                any(BonitaClassLoader.class));
    }

    @Test
    public void should_refresh_classloader_after_transaction() throws Exception {
        doNothing().when(userTransactionService).registerBonitaSynchronization(synchronizationArgumentCaptor.capture());

        classLoaderService.refreshClassLoaderAfterUpdate(identifier(PROCESS, 42));

        assertThat(synchronizationArgumentCaptor.getValue()).isInstanceOf(RefreshClassloaderSynchronization.class);
    }

    @Test
    public void should_register_only_one_synchronization_when_refreshing_multiple_classloader_in_the_same_transaction()
            throws Exception {
        doNothing().when(userTransactionService).registerBonitaSynchronization(synchronizationArgumentCaptor.capture());

        classLoaderService.refreshClassLoaderAfterUpdate(identifier(PROCESS, 42));
        classLoaderService.refreshClassLoaderAfterUpdate(identifier(PROCESS, 42));
        classLoaderService.refreshClassLoaderAfterUpdate(identifier(PROCESS, 42));
        classLoaderService.refreshClassLoaderAfterUpdate(identifier(PROCESS, 42));

        assertThat(synchronizationArgumentCaptor.getAllValues()).hasSize(1);
    }

    @Test
    public void should_refresh_multiple_classloaders_after_transaction() throws Exception {
        doNothing().when(userTransactionService).registerBonitaSynchronization(synchronizationArgumentCaptor.capture());

        classLoaderService.refreshClassLoaderAfterUpdate(identifier(PROCESS, 41));
        classLoaderService.refreshClassLoaderAfterUpdate(identifier(PROCESS, 42));
        classLoaderService.refreshClassLoaderAfterUpdate(identifier(PROCESS, 43));
        classLoaderService.refreshClassLoaderAfterUpdate(identifier(PROCESS, 42));

        assertThat(synchronizationArgumentCaptor.getAllValues()).hasSize(1);
        assertThat(synchronizationArgumentCaptor.getValue().getIdentifiers())
                .containsExactlyInAnyOrder(identifier(PROCESS, 41L), identifier(PROCESS, 42L),
                        identifier(PROCESS, 43L));
    }

    @Test
    public void should_refresh_classloader_after_transaction_once_per_transaction() throws Exception {
        doNothing().when(userTransactionService).registerBonitaSynchronization(synchronizationArgumentCaptor.capture());

        classLoaderService.refreshClassLoaderAfterUpdate(identifier(PROCESS, 42));
        // Simulate an end + begin of a transaction:
        classLoaderService.removeRefreshClassLoaderSynchronization();
        classLoaderService.refreshClassLoaderAfterUpdate(identifier(PROCESS, 42));

        assertThat(synchronizationArgumentCaptor.getAllValues()).hasSize(2);
    }

    @Test
    public void should_only_warn_when_refreshing_classloader_on_not_existing_tenant() throws Exception {
        doReturn(55L).when(sessionAccessor).getTenantId();
        systemOutRule.clearLog();

        classLoaderService.refreshClassLoaderImmediately(identifier(TENANT, 55L));
        assertThat(systemOutRule.getLog()).contains("No dependency service is initialized");
    }

    @Test
    public void should_initialize_class_loader_when_getting_it() {
        classLoaderService.getClassLoader(identifier(TENANT, 43L));

        verify(classLoaderUpdater).initializeClassLoader(classLoaderService, identifier(TENANT, 43L));
    }

    @Test
    public void should_initialize_only_once_classloader() {

        classLoaderService.getClassLoader(identifier(TENANT, 43L));
        classLoaderService.getClassLoader(identifier(TENANT, 43L));

        verify(classLoaderUpdater, times(1)).initializeClassLoader(classLoaderService, identifier(TENANT, 43L));
    }

    @Test
    public void should_not_initialize_classloader_when_adding_and_removing_listener() {
        SingleClassLoaderListener singleClassLoaderListener = mock(SingleClassLoaderListener.class);

        assertThat(classLoaderService.addListener(identifier(TENANT, 44L), singleClassLoaderListener)).isTrue();
        assertThat(classLoaderService.removeListener(identifier(TENANT, 44L), singleClassLoaderListener)).isTrue();

        verify(classLoaderUpdater, never()).initializeClassLoader(classLoaderService, identifier(TENANT, 44L));
    }

    @Test
    public void should_add_and_remove_listeners_for_one_classloader() throws Exception {
        //given
        SingleClassLoaderListener classLoaderListener1 = new SingleClassLoaderListener() {
        };
        SingleClassLoaderListener classLoaderListener2 = new SingleClassLoaderListener() {
        };
        classLoaderService.addListener(identifier(TENANT, 12), classLoaderListener1);
        classLoaderService.addListener(identifier(TENANT, 12), classLoaderListener2);
        //when
        classLoaderService.removeListener(identifier(TENANT, 12), classLoaderListener1);
        //then
        assertThat(classLoaderService.getListeners(identifier(TENANT, 12))).containsExactly(classLoaderListener2);
    }

    @Test
    public void should_call_destroy_on_all_class_loader_when_refreshing_a_parent_classloader() throws Exception {
        MyClassLoaderListener process12Listener = new MyClassLoaderListener();
        classLoaderService.addListener(identifier(PROCESS, 12), process12Listener);
        MyClassLoaderListener process13Listener = new MyClassLoaderListener();
        classLoaderService.addListener(identifier(PROCESS, 13), process13Listener);
        MyClassLoaderListener tenantListener = new MyClassLoaderListener();
        classLoaderService.addListener(identifier(TENANT, TENANT_ID), tenantListener);
        MyClassLoaderListener globalListener = new MyClassLoaderListener();
        classLoaderService.addListener(GLOBAL, globalListener);

        classLoaderService.getClassLoader(identifier(PROCESS, 12));
        classLoaderService.getClassLoader(identifier(PROCESS, 13));

        classLoaderService.refreshClassLoaderImmediately(GLOBAL);

        //process and tenant classloaders are only destroyed
        assertThat(process12Listener.isOnDestroyCalled()).isTrue();
        assertThat(process12Listener.isOnUpdateCalled()).isFalse();
        assertThat(process13Listener.isOnDestroyCalled()).isTrue();
        assertThat(process13Listener.isOnUpdateCalled()).isFalse();
        assertThat(tenantListener.isOnDestroyCalled()).isTrue();
        assertThat(tenantListener.isOnUpdateCalled()).isFalse();
        //global classloader is destroyed and updated
        assertThat(globalListener.isOnDestroyCalled()).isTrue();
        assertThat(globalListener.isOnUpdateCalled()).isTrue();
    }

}
