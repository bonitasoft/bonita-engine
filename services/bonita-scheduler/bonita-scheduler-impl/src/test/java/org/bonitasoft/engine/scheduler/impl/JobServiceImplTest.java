package org.bonitasoft.engine.scheduler.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorCreationException;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JobServiceImplTest {

    private static final long TENANT_ID = 1l;

    private JobServiceImpl jobService;

    @Mock
    private ReadPersistenceService readPersistenceService;

    @Mock
    private EventService eventService;

    @Mock
    private Recorder recorder;

    @Mock
    private SJobDescriptor sJobDescriptor;

    @Mock
    SRecorderException sRecorderException;

    @Before
    public void before() throws Exception {
        doReturn("message").when(sRecorderException).getLocalizedMessage();

        jobService = spy(new JobServiceImpl(eventService, recorder, readPersistenceService));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createJobDescriptor_should_check_null_job_descriptor() throws Exception {

        //when
        jobService.createJobDescriptor(null, TENANT_ID);

        //then exception

    }

    @Test(expected = IllegalArgumentException.class)
    public void createJobDescriptor_should_check_bad_job_descriptor() throws Exception {
        //given
        when(sJobDescriptor.getJobName()).thenReturn(null);

        //when
        jobService.createJobDescriptor(sJobDescriptor, TENANT_ID);

        //then exception

    }

    @Test(expected = SJobDescriptorCreationException.class)
    public void createJobDescriptor_with_recorderexception() throws Exception {
        //given
        doThrow(sRecorderException).when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));
        when(sJobDescriptor.getJobName()).thenReturn("jobName");

        //when
        jobService.createJobDescriptor(sJobDescriptor, TENANT_ID);

        //then exception

    }
}
