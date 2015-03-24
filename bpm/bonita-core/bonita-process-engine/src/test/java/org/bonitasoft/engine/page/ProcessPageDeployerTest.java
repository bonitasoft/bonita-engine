package org.bonitasoft.engine.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProcessPageDeployerTest {

    public static final byte[] CONTENT1 = "content1".getBytes();
    public static final byte[] CONTENT2 = "content2".getBytes();
    public static final long PROCESS_DEFINITION_ID = 15L;
    public static final long USER_ID = 12L;
    public static final long PAGE_ID = 45L;
    public static final String CUSTOMPAGE_STEP1_ZIP = "custompage_step1.zip";
    public static final String CUSTOMPAGE_STEP2_ZIP = "custompage_step2.zip";
    @Mock
    private PageService pageService;

    @Mock
    private BusinessArchive businessArchive;

    @Mock
    private SPage sPage;

    private java.util.Map<java.lang.String, byte[]> ressources;

    @Before
    public void setUp() throws Exception {
        ressources = new HashMap<>();
        ressources.put("resources/customPages/custompage_step1.zip", CONTENT1);
        ressources.put("resources/customPages/custompage_step2.zip", CONTENT2);
        ressources.put("resources/otherResource/data.txt", "data".getBytes());

        doReturn(ressources).when(businessArchive).getResources();
        when(businessArchive.getResources(anyString())).thenCallRealMethod();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void should_deploy_process_insert_pages() throws Exception {
        //given
        doReturn(null).when(pageService).getPageByNameAndProcessDefinitionId(anyString(), anyLong());
        ProcessPageDeployer processPageDeployer = new ProcessPageDeployer(pageService);

        //when
        processPageDeployer.deployProcessPages(businessArchive, PROCESS_DEFINITION_ID, USER_ID);

        //then
        verify(pageService, times(2)).addPage(any(SPage.class), any(byte[].class));
        verify(pageService, never()).updatePageContent(anyLong(), any(byte[].class), anyString());
    }

    @Test
    public void should_deploy_process_update_pages() throws Exception {
        //given
        doReturn(PAGE_ID).when(sPage).getId();
        doReturn(sPage).when(pageService).getPageByNameAndProcessDefinitionId(anyString(), anyLong());
        ProcessPageDeployer processPageDeployer = new ProcessPageDeployer(pageService);

        //when
        processPageDeployer.deployProcessPages(businessArchive, PROCESS_DEFINITION_ID, USER_ID);

        //then
        verify(pageService, never()).addPage(any(SPage.class), any(byte[].class));
        verify(pageService).updatePageContent(PAGE_ID, CONTENT1, CUSTOMPAGE_STEP1_ZIP);
        verify(pageService).updatePageContent(PAGE_ID, CONTENT2, CUSTOMPAGE_STEP2_ZIP);
    }

    @Test
    public void should_getPageResources_filter_resources() throws Exception {
        //given
        ProcessPageDeployer processPageDeployer = new ProcessPageDeployer(pageService);

        //when
        final Map<String, byte[]> pageResources = processPageDeployer.getPageResources(businessArchive);

        //then
        assertThat(pageResources).hasSize(2).containsKeys("resources/customPages/custompage_step1.zip", "resources/customPages/custompage_step2.zip");
    }
}
