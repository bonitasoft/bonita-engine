/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectReadException;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SGatewayDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.GatewayInstanceService;
import org.bonitasoft.engine.core.process.instance.api.TokenService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SGatewayCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SGatewayModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SGatewayNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SGatewayReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SGatewayInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;

/**
 * @author Feng Hui
 * @author Zhao Na
 * @author Matthieu Chaffotte
 */
public class GatewayInstanceServiceImpl implements GatewayInstanceService {

    public static final Class<GatewayInstanceServiceImpl> TAG = GatewayInstanceServiceImpl.class;
    private final Recorder recorder;

    private final EventService eventService;

    private final ReadPersistenceService persistenceRead;

    private final SGatewayInstanceBuilderFactory sGatewayInstanceBuilderFactory;

    private final TokenService tokenService;
    private FlowNodeInstanceService flowNodeInstanceService;

    private final TechnicalLoggerService logger;

    public GatewayInstanceServiceImpl(final Recorder recorder, final EventService eventService, final ReadPersistenceService persistenceRead,
                                      final TechnicalLoggerService logger, final TokenService tokenService, FlowNodeInstanceService flowNodeInstanceService) {
        this.recorder = recorder;
        this.eventService = eventService;
        this.persistenceRead = persistenceRead;
        this.logger = logger;
        this.tokenService = tokenService;
        this.flowNodeInstanceService = flowNodeInstanceService;
        sGatewayInstanceBuilderFactory = BuilderFactory.get(SGatewayInstanceBuilderFactory.class);
    }

    @Override
    public void createGatewayInstance(final SGatewayInstance gatewayInstance) throws SGatewayCreationException {
        final InsertRecord insertRecord = new InsertRecord(gatewayInstance);
        SInsertEvent insertEvent = null;
        if (eventService.hasHandlers(GATEWAYINSTANCE, EventActionType.CREATED)) {
            insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(GATEWAYINSTANCE).setObject(gatewayInstance).done();
        }
        try {
            recorder.recordInsert(insertRecord, insertEvent);
        } catch (final SRecorderException e) {
            throw new SGatewayCreationException(e);
        }
        if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
            final StringBuilder stb = new StringBuilder();
            stb.append("Created gateway instance [name = <" + gatewayInstance.getName());
            stb.append(">, id = <" + gatewayInstance.getId());
            stb.append(">, parent process instance id = <" + gatewayInstance.getParentProcessInstanceId());
            stb.append(">, root process instance id = <" + gatewayInstance.getRootProcessInstanceId());
            stb.append(">, process definition id = <" + gatewayInstance.getRootProcessInstanceId());
            stb.append(">]");
            logger.log(this.getClass(), TechnicalLogSeverity.DEBUG,
                    stb.toString());
        }
    }

    @Override
    public SGatewayInstance getGatewayInstance(final long gatewayInstanceId) throws SGatewayNotFoundException, SGatewayReadException {
        SGatewayInstance selectOne;
        try {
            selectOne = persistenceRead.selectById(SelectDescriptorBuilder.getElementById(SGatewayInstance.class, "SGatewayInstance", gatewayInstanceId));
        } catch (final SBonitaReadException e) {
            throw new SGatewayReadException(e);
        }
        if (selectOne == null) {
            throw new SGatewayNotFoundException(gatewayInstanceId);
        }
        return selectOne;
    }

    @Override
    public boolean checkMergingCondition(final SProcessDefinition sDefinition, final SGatewayInstance gatewayInstance) throws SBonitaException {
        switch (gatewayInstance.getGatewayType()) {
            case EXCLUSIVE:
                return true;
            case INCLUSIVE:
                return inclusiveBehavior(sDefinition, gatewayInstance);
            case PARALLEL:
                return parallelBehavior(sDefinition, gatewayInstance);
            default:
                return false;
        }
    }

    private boolean inclusiveBehavior(final SProcessDefinition sDefinition, final SGatewayInstance gatewayInstance) throws SObjectReadException {

        /*
         * final SFlowNodeDefinition flowNode = sDefinition.getProcessContainer().getFlowNode(gatewayInstance.getFlowNodeDefinitionId());
         * if (flowNode.getIncomingTransitions().size() == 1) {
         * return true;
         * }
         * // get the token refId that hit the gate
         * final Long tokenRefId = gatewayInstance.getTokenRefId();
         * // if there is NO more token than the number of transitions that hit the gate merge is ok
         * final int size = getHitByTransitionList(gatewayInstance).size();
         * return tokenService.getNumberOfToken(gatewayInstance.getParentContainerId(), tokenRefId) <= size;
         */
        try {
            logger.log(TAG, TechnicalLogSeverity.DEBUG,
                    "Evaluate if gateway " + gatewayInstance.getName() + " of instance " + gatewayInstance.getRootProcessInstanceId() + " of definition "
                            + sDefinition.getName() + " must be activated ");
            return shouldFire(sDefinition, gatewayInstance);
        } catch (SBonitaReadException e) {
            throw new SObjectReadException(e);
        }
    }

    private boolean shouldFire(SProcessDefinition sDefinition, SGatewayInstance gatewayInstance) throws SBonitaReadException {
        //        IsEnabled(Workflow graph G, State s, Or-join A)
        SFlowElementContainerDefinition processContainer = sDefinition.getProcessContainer();
        SFlowNodeDefinition gatewayDefinition = processContainer.getFlowNode(gatewayInstance.getFlowNodeDefinitionId());
        //        Red := {e | e is an incoming edge of A such that s(e) > 0}
        List<String> hitByTransitionList = getHitByTransitionList(gatewayInstance);
        List<STransitionDefinition> incomingWithTokens = new ArrayList<STransitionDefinition>();
        List<STransitionDefinition> incomingWithoutTokens = new ArrayList<STransitionDefinition>();
        List<STransitionDefinition> incomingTransitions = gatewayDefinition.getIncomingTransitions();

        logger.log(TAG, TechnicalLogSeverity.DEBUG, "HitBys = " + gatewayInstance.getHitBys());
        for (int i = 0; i < incomingTransitions.size(); i++) {
            if (hitByTransitionList.contains(String.valueOf(i + 1))) {
                incomingWithTokens.add(incomingTransitions.get(i));
            } else {
                incomingWithoutTokens.add(incomingTransitions.get(i));
            }
        }
        if (incomingWithTokens.size() == 0) {
            logger.log(TAG, TechnicalLogSeverity.DEBUG, "Not transition with token on it");
            return true;
        }
        List<STransitionDefinition> red = new ArrayList<STransitionDefinition>();
        List<STransitionDefinition> green = new ArrayList<STransitionDefinition>();
        //        while there exist edges e = (v1, v2) ∈ Red and e0 = (v3, v4) < Red such that v4 = v1 , A do
        //            Red := Red ∪{e
        addBackwardReachableTransitions(processContainer, gatewayDefinition, incomingWithTokens, red, Collections.<STransitionDefinition>emptyList());

        //            Green := {e | e is an incoming edge of A such that s(e) = 0}
        //            while there exist an edge e = (v1, v2) ∈ Green and e0 = (v3, v4) < (Green ∪ Red) such that
        //            v4 = v1 , A do
        //                Green := Green ∪{e
        //            }
        //        }
        addBackwardReachableTransitions(processContainer, gatewayDefinition, incomingWithoutTokens, green, red);

        logger.log(TAG, TechnicalLogSeverity.DEBUG, "Green transitions: " + green);
        logger.log(TAG, TechnicalLogSeverity.DEBUG, "Red transitions: " + red);

        //                return Green ∩{e | s(e) > 0} = ∅.
        return !greenContainsToken(green, gatewayDefinition, gatewayInstance, processContainer);
    }

    private boolean greenContainsToken(List<STransitionDefinition> green, SFlowNodeDefinition gatewayDefinition, SGatewayInstance gatewayInstance,
                                       SFlowElementContainerDefinition processContainer) throws SBonitaReadException {
        List<SFlowNodeDefinition> sourceElements = new ArrayList<SFlowNodeDefinition>();
        List<SFlowNodeDefinition> targetElements = new ArrayList<SFlowNodeDefinition>();

        for (STransitionDefinition sTransitionDefinition : green) {
            SFlowNodeDefinition source = processContainer.getFlowNode(sTransitionDefinition.getSource());
            if (!source.equals(gatewayDefinition) && !sourceElements.contains(source)) {
                sourceElements.add(source);
            }
            SFlowNodeDefinition target = processContainer.getFlowNode(sTransitionDefinition.getTarget());
            if (!target.equals(gatewayDefinition) && !targetElements.contains(target)) {
                targetElements.add(target);
            }
        }
        for (SFlowNodeDefinition sourceElement : sourceElements) {
            logger.log(TAG, TechnicalLogSeverity.DEBUG, "check if there is a token on source element of transition " + sourceElement.getName());
            List<FilterOption> filters = new ArrayList<FilterOption>();
            filters.add(new FilterOption(SFlowNodeInstance.class, "name", sourceElement.getName()));
            filters.add(new FilterOption(SFlowNodeInstance.class, "parentContainerId", gatewayInstance.getParentContainerId()));
            QueryOptions searchOptions = new QueryOptions(0, 20, Collections.<OrderByOption>emptyList(), filters, null);
            List<SFlowNodeInstance> sFlowNodeInstances = flowNodeInstanceService.searchFlowNodeInstances(
                    SFlowNodeInstance.class,
                    searchOptions);
            logger.log(TAG, TechnicalLogSeverity.DEBUG, (sFlowNodeInstances.isEmpty() ? "No" : "") + " flow node found for " + sourceElement.getName());
            for (SFlowNodeInstance sFlowNodeInstance : sFlowNodeInstances) {
                if (sFlowNodeInstance.isTerminal()) {
                    logger.log(TAG, TechnicalLogSeverity.DEBUG, "flow node " + sFlowNodeInstance.getName() + " contain a token, gateway not merged");
                    return true;
                }
            }
        }
        for (SFlowNodeDefinition targetElement : targetElements) {
            logger.log(TAG, TechnicalLogSeverity.DEBUG, "check if there is a token on target element of transition " + targetElement.getName());
            List<FilterOption> filters = new ArrayList<FilterOption>();
            filters.add(new FilterOption(SFlowNodeInstance.class, "name", targetElement.getName()));
            filters.add(new FilterOption(SFlowNodeInstance.class, "parentContainerId", gatewayInstance.getParentContainerId()));
            QueryOptions searchOptions = new QueryOptions(0, 20, Collections.<OrderByOption>emptyList(), filters, null);
            List<SFlowNodeInstance> sFlowNodeInstances = flowNodeInstanceService.searchFlowNodeInstances(
                    SFlowNodeInstance.class,
                    searchOptions);
            logger.log(TAG, TechnicalLogSeverity.DEBUG, (sFlowNodeInstances.isEmpty() ? "No" : "") + " flow node found for " + targetElement.getName());
            for (SFlowNodeInstance sFlowNodeInstance : sFlowNodeInstances) {
                if (!sFlowNodeInstance.isTerminal()) {
                    logger.log(TAG, TechnicalLogSeverity.DEBUG, "flow node " + sFlowNodeInstance.getName() + " contain a token, gateway not merged");
                    return true;
                }
            }
        }
        logger.log(TAG, TechnicalLogSeverity.DEBUG, "No token to wait, gateway will fire");
        return false;
    }

    private void addBackwardReachableTransitions(SFlowElementContainerDefinition processContainer, SFlowNodeDefinition gatewayDefinition,
                                                 List<STransitionDefinition> transitions, List<STransitionDefinition> backwardReachable, List<STransitionDefinition> notIn) {
        for (STransitionDefinition sTransitionDefinition : transitions) {
            if (!backwardReachable.contains(sTransitionDefinition) && !notIn.contains(sTransitionDefinition)) {
                backwardReachable.add(sTransitionDefinition);
                SFlowNodeDefinition flowNode = processContainer.getFlowNode(sTransitionDefinition.getSource());
                if (!flowNode.equals(gatewayDefinition)) {
                    addBackwardReachableTransitions(processContainer, gatewayDefinition, flowNode.getIncomingTransitions(), backwardReachable, notIn);
                }
            }
        }
    }

    private boolean parallelBehavior(final SProcessDefinition sDefinition, final SGatewayInstance gatewayInstance) {
        final List<String> hitsBy = getHitByTransitionList(gatewayInstance);
        final List<STransitionDefinition> trans = getTransitionDefinitions(gatewayInstance, sDefinition);
        boolean go = true;
        int i = 1;
        while (go && i <= trans.size()) {
            go = hitsBy.contains(String.valueOf(i));
            i++;
        }
        return go;
    }

    /**
     * @return the list of transition indexes that hit the gateway
     */
    private List<String> getHitByTransitionList(final SGatewayInstance gatewayInstance) {
        return Arrays.asList(gatewayInstance.getHitBys().split(","));
    }

    protected List<STransitionDefinition> getTransitionDefinitions(final SGatewayInstance gatewayInstance, final SProcessDefinition processDefinition) {
        final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
        final SGatewayDefinition gatewayDefinition = processContainer.getGateway(gatewayInstance.getName());
        return gatewayDefinition.getIncomingTransitions();
    }

    @Override
    public void setState(final SGatewayInstance gatewayInstance, final int stateId) throws SGatewayModificationException {
        updateOneColum(gatewayInstance, sGatewayInstanceBuilderFactory.getStateIdKey(), stateId, GATEWAYINSTANCE_STATE);
    }

    @Override
    public void hitTransition(final SGatewayInstance gatewayInstance, final long transitionIndex) throws SGatewayModificationException {
        logger.log(TAG, TechnicalLogSeverity.DEBUG, "Hit gateway " + gatewayInstance.getName() + " of instance " + gatewayInstance.getRootProcessInstanceId()
                + " with transition index " + transitionIndex);
        final String hitBys = gatewayInstance.getHitBys();
        String columnValue;
        if (hitBys == null || hitBys.isEmpty()) {
            columnValue = String.valueOf(transitionIndex);
        } else {
            columnValue = hitBys + "," + transitionIndex;
        }
        updateOneColum(gatewayInstance, sGatewayInstanceBuilderFactory.getHitBysKey(), columnValue, GATEWAYINSTANCE_HITBYS);
    }

    private void updateOneColum(final SGatewayInstance gatewayInstance, final String columnName, final Serializable columnValue, final String event)
            throws SGatewayModificationException {
        final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        entityUpdateDescriptor.addField(columnName, columnValue);

        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(gatewayInstance, entityUpdateDescriptor);

        SUpdateEvent updateEvent = null;
        if (eventService.hasHandlers(event, EventActionType.UPDATED)) {
            updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(event).setObject(gatewayInstance).done();
        }
        try {
            recorder.recordUpdate(updateRecord, updateEvent);
        } catch (final SRecorderException e) {
            throw new SGatewayModificationException(e);
        }
    }

    @Override
    public SGatewayInstance getActiveGatewayInstanceOfTheProcess(final long parentProcessInstanceId, final String name) throws SGatewayNotFoundException,
            SGatewayReadException {
        SGatewayInstance selectOne;
        try {
            selectOne = persistenceRead.selectOne(SelectDescriptorBuilder.getActiveGatewayInstanceOfProcess(parentProcessInstanceId, name));// FIXME select more
            // one and get the oldest
        } catch (final SBonitaReadException e) {
            throw new SGatewayReadException(e);
        }
        if (selectOne == null) {
            throw new SGatewayNotFoundException(parentProcessInstanceId, name);
        }
        return selectOne;
    }

    @Override
    public void setFinish(final SGatewayInstance gatewayInstance) throws SGatewayModificationException {
        final String columnValue = FINISH + gatewayInstance.getHitBys().split(",").length;
        logger.log(TAG, TechnicalLogSeverity.TRACE, "set finish on gateway " + gatewayInstance.getName() + " " + columnValue);
        updateOneColum(gatewayInstance, sGatewayInstanceBuilderFactory.getHitBysKey(), columnValue, GATEWAYINSTANCE_HITBYS);
    }

    @Override
    public SGatewayInstance getGatewayMergingToken(final long processInstanceId, final Long tokenRefId) throws SGatewayReadException {
        final HashMap<String, Object> hashMap = new HashMap<String, Object>(2);
        hashMap.put("processInstanceId", processInstanceId);
        hashMap.put("tokenRefId", tokenRefId);
        try {
            return persistenceRead.selectOne(new SelectOneDescriptor<SGatewayInstance>("getGatewayMergingToken", hashMap, SGatewayInstance.class));
        } catch (final SBonitaReadException e) {
            throw new SGatewayReadException(e);
        }
    }

}
