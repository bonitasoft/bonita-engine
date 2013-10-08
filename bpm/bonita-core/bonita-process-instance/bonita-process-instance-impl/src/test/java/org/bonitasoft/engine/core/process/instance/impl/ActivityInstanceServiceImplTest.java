package org.bonitasoft.engine.core.process.instance.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityReadException;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ActivityInstanceServiceImplTest {

    @Mock
    private ReadPersistenceService persistenceService;

    @Mock
    private BPMInstanceBuilders instanceBuilders;

    @InjectMocks
    private ActivityInstanceServiceImpl activityInstanceService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getPossibleUserIdsOfPendingTasks() throws SActivityReadException, SBonitaReadException {
        final List<Long> sUserIds = new ArrayList<Long>();
        Collections.addAll(sUserIds, 78l, 2l, 5l, 486l);
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(sUserIds);

        final List<Long> userIds = activityInstanceService.getPossibleUserIdsOfPendingTasks(2, 0, 10);
        assertEquals(sUserIds, userIds);
    }

    @Test(expected = SActivityReadException.class)
    public void throwExceptionwhenGettingPossibleUserIdsOfPendingTasksDueToPersistenceException() throws SActivityReadException, SBonitaReadException {
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenThrow(new SBonitaReadException("database out"));
        activityInstanceService.getPossibleUserIdsOfPendingTasks(2, 0, 10);
    }

    @Test
    public void getEmptyPossibleUserIdsOfPendingTasks() throws SActivityReadException, SBonitaReadException {
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(Collections.emptyList());

        final List<Long> userIds = activityInstanceService.getPossibleUserIdsOfPendingTasks(2, 0, 10);
        assertEquals(Collections.emptyList(), userIds);
    }

}
