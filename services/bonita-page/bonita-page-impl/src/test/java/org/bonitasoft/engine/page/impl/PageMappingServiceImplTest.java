package org.bonitasoft.engine.page.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.bonitasoft.engine.page.SPageMapping;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PageMappingServiceImplTest {

    @Mock
    private Recorder recorder;

    @Mock
    private ReadPersistenceService persistenceService;

    @Mock
    private SessionService sessionService;

    @Mock
    private ReadSessionAccessor sessionAccessor;

    @InjectMocks
    private PageMappingServiceImpl pageMappingService;

    @Test
    public void get_should_return_page_mappings() throws Exception {
        final QueryOptions options = new QueryOptions(0, 2);
        final SelectListDescriptor<SPageMapping> listDescriptor = new SelectListDescriptor<SPageMapping>("getPageMappingByPageId",
                Collections.<String, Object> singletonMap("pageId", 1983L), SPageMapping.class, options);

        pageMappingService.get(1983L, 0, 2);

        verify(persistenceService).selectList(listDescriptor);
    }

    @Test(expected = SBonitaReadException.class)
    public void get_should_throw_an_exception() throws Exception {
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenThrow(new SBonitaReadException("exception"));

        pageMappingService.get(1983L, 0, 2);
    }

}
