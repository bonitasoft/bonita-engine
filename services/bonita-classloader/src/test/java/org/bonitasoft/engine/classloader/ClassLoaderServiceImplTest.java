package org.bonitasoft.engine.classloader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.Collections;
import java.util.stream.Stream;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class ClassLoaderServiceImplTest {

    private ParentClassLoaderResolver parentClassLoaderResolver = new ParentClassLoaderResolver() {

        @Override
        public ClassLoaderIdentifier getParentClassLoaderIdentifier(ClassLoaderIdentifier childId) {
            if (childId.getType().equals(CHILD_TYPE)) {
                return new ClassLoaderIdentifier(PARENT_TYPE, PARENT_ID);
            } else {
                return ClassLoaderIdentifier.GLOBAL;
            }
        }
    };
    private ParentClassLoaderResolver badParentClassLoaderResolver = new ParentClassLoaderResolver() {

        @Override
        public ClassLoaderIdentifier getParentClassLoaderIdentifier(ClassLoaderIdentifier childId) {
            if (childId.getType().equals(CHILD_TYPE)) {
                return new ClassLoaderIdentifier(PARENT_TYPE, PARENT_ID);
            } else {
                return null;
            }
        }
    };
    @Mock
    private TechnicalLoggerService logger;
    @Mock
    private EventService eventService;
    private ClassLoaderServiceImpl classLoaderService;
    private ClassLoader testClassLoader;
    private VirtualClassLoader processClassLoader;
    private MyClassLoaderListener myClassLoaderListener;
    private VirtualClassLoader tenantClassLoader;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private String CHILD_TYPE = "child";
    private String PARENT_TYPE = "parent";
    private long CHILD_ID = 12;
    private long PARENT_ID = 13;

    @Before
    public void before() throws Exception {
        classLoaderService = spy(new ClassLoaderServiceImpl(parentClassLoaderResolver, logger, eventService));
        processClassLoader = classLoaderService.getLocalClassLoader(CHILD_TYPE, CHILD_ID);
        tenantClassLoader = classLoaderService.getLocalClassLoader(PARENT_TYPE, PARENT_ID);
        testClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(processClassLoader);
        myClassLoaderListener = new MyClassLoaderListener();
        temporaryFolder.create();
        final File file = temporaryFolder.newFolder();
        doReturn(file.toURI()).when(classLoaderService).getLocalTemporaryFolder(anyString(), anyLong());
    }

    @After
    public void after() {
        Thread.currentThread().setContextClassLoader(testClassLoader);
    }

    @Test
    public void should_addListener_add_on_specified_classloader_do_not_call_on_others() throws Exception {
        //given
        classLoaderService.addListener(PARENT_TYPE, PARENT_ID, myClassLoaderListener);
        //when
        processClassLoader.destroy();
        //then
        assertThat(myClassLoaderListener.isOnDestroyCalled()).isFalse();
    }

    @Test
    public void should_addListener_add_on_specified_classloader_call_listener() throws Exception {
        //given
        classLoaderService.addListener(PARENT_TYPE, PARENT_ID, myClassLoaderListener);
        //when
        tenantClassLoader.destroy();
        //then
        assertThat(myClassLoaderListener.isOnDestroyCalled()).isTrue();
    }

    @Test
    public void should_removeListener_remove_the_listener() throws Exception {
        //given
        classLoaderService.addListener(PARENT_TYPE, PARENT_ID, myClassLoaderListener);
        classLoaderService.removeListener(PARENT_TYPE, PARENT_ID, myClassLoaderListener);
        //when
        processClassLoader.destroy();
        //then
        assertThat(myClassLoaderListener.isOnDestroyCalled()).isFalse();

    }

    @Test
    public void should_refreshClassLoader_call_replace_classloader() throws Exception {
        //given
        classLoaderService.addListener(CHILD_TYPE, CHILD_ID, myClassLoaderListener);
        //when
        classLoaderService.refreshLocalClassLoader(CHILD_TYPE, CHILD_ID, Stream.empty());
        //then
        assertThat(myClassLoaderListener.isOnUpdateCalled()).isTrue();
        assertThat(myClassLoaderListener.isOnDestroyCalled()).isFalse();
    }

    @Test
    public void should_stop_destroy_all_classloaders() throws Exception {
        //given
        classLoaderService.addListener(CHILD_TYPE, CHILD_ID, myClassLoaderListener);
        classLoaderService.addListener(PARENT_TYPE, PARENT_ID, myClassLoaderListener);
        classLoaderService.getLocalClassLoader(CHILD_TYPE, 125);
        classLoaderService.addListener(CHILD_TYPE, 125, myClassLoaderListener);
        classLoaderService.getLocalClassLoader(CHILD_TYPE, 126);
        classLoaderService.addListener(CHILD_TYPE, 126, myClassLoaderListener);
        classLoaderService.getLocalClassLoader(CHILD_TYPE, 127);
        classLoaderService.addListener(CHILD_TYPE, 127, myClassLoaderListener);
        classLoaderService.getLocalClassLoader(CHILD_TYPE, 128);
        classLoaderService.addListener(CHILD_TYPE, 128, myClassLoaderListener);
        classLoaderService.getLocalClassLoader(CHILD_TYPE, 129);
        classLoaderService.addListener(CHILD_TYPE, 129, myClassLoaderListener);
        classLoaderService.getLocalClassLoader(CHILD_TYPE, 130);
        classLoaderService.addListener(CHILD_TYPE, 130, myClassLoaderListener);
        //when
        classLoaderService.stop();
        //then
        assertThat(myClassLoaderListener.getOnUpdateCalled()).isEqualTo(0);
        assertThat(myClassLoaderListener.getOnDestroyCalled()).isEqualTo(8);
    }

    @Test
    public void should_removeLocalClassLoader_call_destroy() throws Exception {
        //given
        classLoaderService.addListener(CHILD_TYPE, CHILD_ID, myClassLoaderListener);
        classLoaderService.addListener(PARENT_TYPE, PARENT_ID, myClassLoaderListener);
        //when
        classLoaderService.removeLocalClassLoader(CHILD_TYPE, CHILD_ID);

        //then
        assertThat(myClassLoaderListener.getOnDestroyCalled()).isEqualTo(1);
    }

    @Test(expected = SClassLoaderException.class)
    public void should_removeLocalClassLoader_throw_exception_if_parent_not_removed() throws Exception {
        //given
        classLoaderService.getLocalClassLoader(CHILD_TYPE, 17);//second classloader
        //when
        classLoaderService.removeLocalClassLoader(CHILD_TYPE, CHILD_ID);
        classLoaderService.removeLocalClassLoader(PARENT_TYPE, PARENT_ID);
    }

    @Test
    public void should_removeLocalClassLoader_work_if_remove_in_right_order() throws Exception {
        //given
        //when
        classLoaderService.removeLocalClassLoader(CHILD_TYPE, CHILD_ID);
        classLoaderService.removeLocalClassLoader(PARENT_TYPE, PARENT_ID);
    }


    @Test
    public void should_getLocalClassLoader_create_expected_hierarchy() throws Exception {
        //given
        VirtualClassLoader localClassLoader = classLoaderService.getLocalClassLoader(CHILD_TYPE, CHILD_ID);
        //when

        assertThat(localClassLoader.getIdentifier()).isEqualTo(new ClassLoaderIdentifier(CHILD_TYPE, CHILD_ID));
        ClassLoader parent = localClassLoader.getParent();
        assertThat(parent).isInstanceOf(VirtualClassLoader.class);
        assertThat(((VirtualClassLoader) parent).getIdentifier()).isEqualTo(new ClassLoaderIdentifier(PARENT_TYPE, PARENT_ID));
        ClassLoader global = parent.getParent();
        assertThat(global).isInstanceOf(VirtualClassLoader.class);
        assertThat(((VirtualClassLoader) global).getIdentifier()).isEqualTo(ClassLoaderIdentifier.GLOBAL);
        ClassLoader root = global.getParent();
        assertThat(root).isNotInstanceOf(VirtualClassLoader.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_create_throw_an_exception_if_bad_resolver() throws Exception {
        //given
        classLoaderService = spy(new ClassLoaderServiceImpl(badParentClassLoaderResolver, logger, eventService));
        //when
        processClassLoader = classLoaderService.getLocalClassLoader(CHILD_TYPE, CHILD_ID);

        //then exception

    }


    @Test
    public void should_globalListeners_be_called_on_destroy() throws Exception {
        //given
        classLoaderService.getLocalClassLoader(CHILD_TYPE, 17);//second classloader
        ClassLoaderListener listener = mock(ClassLoaderListener.class);
        classLoaderService.addListener(listener);
        //when
        classLoaderService.removeLocalClassLoader(CHILD_TYPE, CHILD_ID);
        classLoaderService.removeLocalClassLoader(CHILD_TYPE, 17);

        //then
        verify(listener, times(2)).onDestroy(any(VirtualClassLoader.class));
    }

    @Test
    public void should_globalListeners_be_called_on_update() throws Exception {
        //given
        classLoaderService.getLocalClassLoader(CHILD_TYPE, 17);//second classloader
        ClassLoaderListener listener = mock(ClassLoaderListener.class);
        classLoaderService.addListener(listener);
        //when
        classLoaderService.refreshLocalClassLoader(CHILD_TYPE, CHILD_ID, Stream.empty());
        classLoaderService.refreshLocalClassLoader(CHILD_TYPE, 17, Stream.empty());

        //then
        verify(listener, times(2)).onUpdate(any(VirtualClassLoader.class));
    }

}
