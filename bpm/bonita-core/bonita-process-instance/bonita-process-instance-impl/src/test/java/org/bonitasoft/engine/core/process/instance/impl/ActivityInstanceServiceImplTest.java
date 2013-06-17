/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.core.process.instance.impl;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;

import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class ActivityInstanceServiceImplTest {

    @Mock(answer = Answers.RETURNS_MOCKS)
    private BPMInstanceBuilders instanceBuilders;

    @Mock
    private EventService eventService;

    @Mock
    private Recorder recorder;

    @Mock
    private ReadPersistenceService persistenceRead;

    @Mock
    private QueriableLoggerService queriableLoggerService;

    @Mock
    private TechnicalLoggerService logger;

    @InjectMocks
    private ActivityInstanceServiceImpl activityInstanceServiceImpl;

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#getFlowNodeInstance(long)}.
     */
    @Test
    public final void getFlowNodeInstanceById() throws SBonitaReadException, SFlowNodeNotFoundException, SFlowNodeReadException {
        final SFlowNodeInstance sFlowNodeInstance = mock(SFlowNodeInstance.class);
        when(persistenceRead.selectById(any(SelectByIdDescriptor.class))).thenReturn(sFlowNodeInstance);

        Assert.assertEquals(sFlowNodeInstance, activityInstanceServiceImpl.getFlowNodeInstance(456L));
    }

    @Test(expected = SFlowNodeNotFoundException.class)
    public final void getFlowNodeInstanceByIdNotExists() throws SBonitaReadException, SFlowNodeNotFoundException, SFlowNodeReadException {
        when(persistenceRead.selectById(any(SelectByIdDescriptor.class))).thenReturn(null);

        activityInstanceServiceImpl.getFlowNodeInstance(456L);
    }

    @Test(expected = SFlowNodeReadException.class)
    public final void getFlowNodeInstanceByIdThrowException() throws SBonitaReadException, SFlowNodeNotFoundException, SFlowNodeReadException {
        when(persistenceRead.selectById(any(SelectByIdDescriptor.class))).thenThrow(new SBonitaReadException(""));

        activityInstanceServiceImpl.getFlowNodeInstance(456L);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#getFlowNodeInstances(long, int, int)}.
     */
    @Test
    public final void getFlowNodeInstances() throws SBonitaReadException, SFlowNodeReadException {
        final List<SFlowNodeInstance> sFlowNodeInstances = Collections.singletonList(mock(SFlowNodeInstance.class));
        when(persistenceRead.selectList(any(SelectListDescriptor.class))).thenReturn(sFlowNodeInstances);

        Assert.assertEquals(sFlowNodeInstances, activityInstanceServiceImpl.getFlowNodeInstances(56456L, 526, 565));
    }

    @Test
    public final void getFlowNodeInstancesByListOfIdsWithEmptyList() throws SBonitaReadException, SFlowNodeReadException {
        Assert.assertEquals(Collections.emptyList(), activityInstanceServiceImpl.getFlowNodeInstances(56456L, 526, 565));
    }

    @Test
    public final void getFlowNodeInstancesByListOfIdsWithNullList() throws SBonitaReadException, SFlowNodeReadException {
        Assert.assertEquals(Collections.emptyList(), activityInstanceServiceImpl.getFlowNodeInstances(56456L, 526, 565));
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#getFlowNodeInstancesToRestart(org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void getFlowNodeInstancesToRestart() throws SBonitaReadException, SFlowNodeReadException {
        final QueryOptions queryOptions = mock(QueryOptions.class);
        final List<SFlowNodeInstance> sFlowNodeInstances = Collections.singletonList(mock(SFlowNodeInstance.class));
        when(persistenceRead.selectList(any(SelectListDescriptor.class))).thenReturn(sFlowNodeInstances);

        Assert.assertEquals(sFlowNodeInstances, activityInstanceServiceImpl.getFlowNodeInstancesToRestart(queryOptions));
    }

    @Test
    public final void getFlowNodeInstancesToRestartByListOfIdsWithEmptyList() throws SBonitaReadException, SFlowNodeReadException {
        Assert.assertEquals(Collections.emptyList(), activityInstanceServiceImpl.getFlowNodeInstancesToRestart(mock(QueryOptions.class)));
    }

    @Test
    public final void getFlowNodeInstancesToRestartByListOfIdsWithNullList() throws SBonitaReadException, SFlowNodeReadException {
        Assert.assertEquals(Collections.emptyList(), activityInstanceServiceImpl.getFlowNodeInstancesToRestart(mock(QueryOptions.class)));
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#initializeLogBuilder(org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder, java.lang.String)}
     * .
     */
    @Test
    public final void initializeLogBuilder() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#updateLog(org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction.ActionType, org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction)}
     * .
     */
    @Test
    public final void updateLog() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#getQueriableLog(org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction.ActionType, java.lang.String, org.bonitasoft.engine.core.process.instance.model.SFlowElementInstance)}
     * .
     */
    @Test
    public final void getQueriableLog() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#setState(org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance, org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState)}
     * .
     */
    @Test
    public final void setState() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#setExecuting(org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance)}
     * .
     */
    @Test
    public final void setExecuting() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#updateDisplayName(org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance, java.lang.String)}
     * .
     */
    @Test
    public final void updateDisplayName() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#updateDisplayDescription(org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance, java.lang.String)}
     * .
     */
    @Test
    public final void updateDisplayDescription() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#setTaskPriority(org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance, org.bonitasoft.engine.core.process.instance.model.STaskPriority)}
     * .
     */
    @Test
    public final void setTaskPriority() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#getActiveFlowNodes(long)}.
     */
    @Test
    public final void getActiveFlowNodes() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#getArchivedFlowNodeInstances(long, int, int)}.
     */
    @Test
    public final void getArchivedFlowNodeInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#getArchivedFlowNodeInstance(long, org.bonitasoft.engine.persistence.ReadPersistenceService)}
     * .
     */
    @Test
    public final void getArchivedFlowNodeInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#setStateCategory(org.bonitasoft.engine.core.process.instance.model.SFlowElementInstance, org.bonitasoft.engine.core.process.instance.model.SStateCategory)}
     * .
     */
    @Test
    public final void setStateCategory() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#setExecutedBy(org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance, long)}
     * .
     */
    @Test
    public final void setExecutedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#setExecutedByDelegate(org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance, long)}
     * .
     */
    @Test
    public final void setExecutedByDelegate() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#setExpectedEndDate(org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance, long)}
     * .
     */
    @Test
    public final void setExpectedEndDate() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#updateFlowNode(org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance, java.lang.String, org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor)}
     * .
     */
    @Test
    public final void updateFlowNode() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#getUnmodifiableList(java.util.List)}.
     */
    @Test
    public final void getUnmodifiableList() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#getNumberOfFlowNodeInstances(java.lang.Class, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void getNumberOfFlowNodeInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#searchFlowNodeInstances(java.lang.Class, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void searchFlowNodeInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#getNumberOfArchivedFlowNodeInstances(java.lang.Class, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void getNumberOfArchivedFlowNodeInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#searchArchivedFlowNodeInstances(java.lang.Class, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void searchArchivedFlowNodeInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#getEventService()}.
     */
    @Test
    public final void getEventService() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#getRecorder()}.
     */
    @Test
    public final void getRecorder() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#getPersistenceRead()}.
     */
    @Test
    public final void getPersistenceRead() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl#getInstanceBuilders()}.
     */
    @Test
    public final void getInstanceBuilders() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#createActivityInstance(org.bonitasoft.engine.core.process.instance.model.SActivityInstance)}
     * .
     */
    @Test
    public final void createActivityInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getQueriableLog(org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction.ActionType, java.lang.String, org.bonitasoft.engine.core.process.instance.model.SPendingActivityMapping)}
     * .
     */
    @Test
    public final void getQueriableLogActionTypeStringSPendingActivityMapping() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#addPendingActivityMappings(org.bonitasoft.engine.core.process.instance.model.SPendingActivityMapping)}
     * .
     */
    @Test
    public final void addPendingActivityMappings() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#deletePendingMappings(long)}.
     */
    @Test
    public final void deletePendingMappings() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getPendingMappings(long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void getPendingMappings() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getActivityInstance(long)}.
     */
    @Test
    public final void getActivityInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getHumanTaskInstance(long)}.
     */
    @Test
    public final void getHumanTaskInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getActivitiesWithStates(long, java.util.Set)}.
     */
    @Test
    public final void getActivitiesWithStatesLongSetOfInteger() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getActivitiesWithStates(long, int, int, java.lang.String, org.bonitasoft.engine.persistence.OrderByType, java.util.Set)}
     * .
     */
    @Test
    public final void getActivitiesWithStatesLongIntIntStringOrderByTypeSetOfInteger() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getOpenActivityInstances(long, int, int, java.lang.String, org.bonitasoft.engine.persistence.OrderByType)}
     * .
     */
    @Test
    public final void getOpenActivityInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getArchivedActivityInstance(long, org.bonitasoft.engine.persistence.ReadPersistenceService)}
     * .
     */
    @Test
    public final void getArchivedActivityInstanceLongReadPersistenceService() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getArchivedActivityInstances(long, org.bonitasoft.engine.persistence.ReadPersistenceService, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void getArchivedActivityInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getPendingTasks(long, java.util.Set, int, int, java.lang.String, org.bonitasoft.engine.persistence.OrderByType)}
     * .
     */
    @Test
    public final void getPendingTasks() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getAssignedUserTasks(long, int, int, java.lang.String, org.bonitasoft.engine.persistence.OrderByType)}
     * .
     */
    @Test
    public final void getAssignedUserTasks() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getNumberOfOpenActivityInstances(long)}.
     */
    @Test
    public final void getNumberOfOpenActivityInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getActivityInstances(long, int, int, java.lang.String, org.bonitasoft.engine.persistence.OrderByType)}
     * .
     */
    @Test
    public final void getActivityInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getNumberOfActivityInstances(long)}.
     */
    @Test
    public final void getNumberOfActivityInstancesLong() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#createManualUserTask(long, java.lang.String, long, java.lang.String, long, java.lang.String, long, org.bonitasoft.engine.core.process.instance.model.STaskPriority)}
     * .
     */
    @Test
    public final void createManualUserTask() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#assignHumanTask(long, long)}.
     */
    @Test
    public final void assignHumanTask() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getNumberOfAssignedHumanTaskInstances(long)}.
     */
    @Test
    public final void getNumberOfAssignedHumanTaskInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getArchivedActivityInstance(long, int, org.bonitasoft.engine.persistence.ReadPersistenceService)}
     * .
     */
    @Test
    public final void getArchivedActivityInstanceLongIntReadPersistenceService() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getNumberOfArchivedTasksManagedBy(long, org.bonitasoft.engine.persistence.QueryOptions, org.bonitasoft.engine.persistence.ReadPersistenceService)}
     * .
     */
    @Test
    public final void getNumberOfArchivedTasksManagedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#searchArchivedTasksManagedBy(long, org.bonitasoft.engine.persistence.QueryOptions, org.bonitasoft.engine.persistence.ReadPersistenceService)}
     * .
     */
    @Test
    public final void searchArchivedTasksManagedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getNumberOfArchivedTasksSupervisedBy(long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void getNumberOfArchivedTasksSupervisedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getNumberOfAssignedTasksSupervisedBy(long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void getNumberOfAssignedTasksSupervisedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#searchAssignedTasksSupervisedBy(long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void searchAssignedTasksSupervisedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getNumberOfHumanTasks(org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void getNumberOfHumanTasks() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#searchHumanTasks(org.bonitasoft.engine.persistence.QueryOptions)}.
     */
    @Test
    public final void searchHumanTasks() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#searchArchivedTasksSupervisedBy(long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void searchArchivedTasksSupervisedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#searchArchivedTasks(org.bonitasoft.engine.persistence.QueryOptions, org.bonitasoft.engine.persistence.ReadPersistenceService)}
     * .
     */
    @Test
    public final void searchArchivedTasks() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getNumberOfArchivedTasks(org.bonitasoft.engine.persistence.QueryOptions, org.bonitasoft.engine.persistence.ReadPersistenceService)}
     * .
     */
    @Test
    public final void getNumberOfArchivedTasks() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getNumberOfAssignedTasksManagedBy(long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void getNumberOfAssignedTasksManagedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#searchAssignedTasksManagedBy(long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void searchAssignedTasksManagedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#searchPendingTasksSupervisedBy(long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void searchPendingTasksSupervisedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getNumberOfPendingTasksSupervisedBy(long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void getNumberOfPendingTasksSupervisedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getNumberOfOpenTasksForUsers(java.util.List)}.
     */
    @Test
    public final void getNumberOfOpenTasksForUsers() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#searchNumberOfPendingTasksManagedBy(long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void searchNumberOfPendingTasksManagedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#searchPendingTasksManagedBy(long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void searchPendingTasksManagedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#incrementLoopCounter(org.bonitasoft.engine.core.process.instance.model.SLoopActivityInstance)}
     * .
     */
    @Test
    public final void incrementLoopCounter() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getNumberOfOverdueOpenTasksForUsers(java.util.List)}.
     */
    @Test
    public final void getNumberOfOverdueOpenTasksForUsers() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getChildrenOfAnActivity(long, int, int)}.
     */
    @Test
    public final void getChildrenOfAnActivity() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#setLoopMax(org.bonitasoft.engine.core.process.instance.model.SLoopActivityInstance, java.lang.Integer)}
     * .
     */
    @Test
    public final void setLoopMax() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#setLoopCardinality(org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance, int)}
     * .
     */
    @Test
    public final void setLoopCardinality() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#addMultiInstanceNumberOfActiveActivities(org.bonitasoft.engine.core.process.instance.model.SMultiInstanceActivityInstance, int)}
     * .
     */
    @Test
    public final void addMultiInstanceNumberOfActiveActivities() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#addMultiInstanceNumberOfTerminatedActivities(org.bonitasoft.engine.core.process.instance.model.SMultiInstanceActivityInstance, int)}
     * .
     */
    @Test
    public final void addMultiInstanceNumberOfTerminatedActivities() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#addMultiInstanceNumberOfCompletedActivities(org.bonitasoft.engine.core.process.instance.model.SMultiInstanceActivityInstance, int)}
     * .
     */
    @Test
    public final void addMultiInstanceNumberOfCompletedActivities() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getNumberOfActivityInstances(java.lang.Class, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void getNumberOfActivityInstancesClassOfQextendsPersistentObjectQueryOptions() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#searchActivityInstances(java.lang.Class, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void searchActivityInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getNumberOfArchivedActivityInstances(java.lang.Class, org.bonitasoft.engine.persistence.QueryOptions, org.bonitasoft.engine.persistence.ReadPersistenceService)}
     * .
     */
    @Test
    public final void getNumberOfArchivedActivityInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#searchArchivedActivityInstances(java.lang.Class, org.bonitasoft.engine.persistence.QueryOptions, org.bonitasoft.engine.persistence.ReadPersistenceService)}
     * .
     */
    @Test
    public final void searchArchivedActivityInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#setTokenCount(org.bonitasoft.engine.core.process.instance.model.SActivityInstance, int)}
     * .
     */
    @Test
    public final void setTokenCount() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#hideTasks(long, java.lang.Long[])}.
     */
    @Test
    public final void hideTasks() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#unhideTasks(long, java.lang.Long[])}.
     */
    @Test
    public final void unhideTasks() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#unhideTask(long, long)}.
     */
    @Test
    public final void unhideTask() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getHiddenTask(long, long)}.
     */
    @Test
    public final void getHiddenTaskLongLong() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getHiddenTask(long)}.
     */
    @Test
    public final void getHiddenTaskLong() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#searchHiddenTasksForActivity(long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void searchHiddenTasksForActivity() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#deleteHiddenTasksForActivity(long)}.
     */
    @Test
    public final void deleteHiddenTasksForActivity() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getNumberOfPendingHiddenTasks(long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void getNumberOfPendingHiddenTasks() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#searchPendingHiddenTasks(long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void searchPendingHiddenTasks() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getHiddenTaskQueriableLog(org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction.ActionType, java.lang.String, org.bonitasoft.engine.core.process.instance.model.SHiddenTaskInstance)}
     * .
     */
    @Test
    public final void getHiddenTaskQueriableLog() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getNumberOfPendingTasksForUser(long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void getNumberOfPendingTasksForUser() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#searchPendingTasksForUser(long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void searchPendingTasksForUser() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getNumberOfPendingOrAssignedTasks(long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void getNumberOfPendingOrAssignedTasks() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#searchPendingOrAssignedTasks(long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void searchPendingOrAssignedTasks() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#isTaskHidden(long, long)}.
     */
    @Test
    public final void isTaskHidden() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#deleteArchivedPendingMappings(long)}.
     */
    @Test
    public final void deleteArchivedPendingMappings() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#setAbortedByBoundaryEvent(org.bonitasoft.engine.core.process.instance.model.SActivityInstance, long)}
     * .
     */
    @Test
    public final void setAbortedByBoundaryEvent() {
        // TODO : Not yet implemented
    }

}
