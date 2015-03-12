/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
import java.util.Iterator;
import java.util.List;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.GatewayInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SGatewayCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SGatewayModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SGatewayNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SGatewayReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SGatewayInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.impl.SGatewayInstanceImpl;
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
import org.bonitasoft.engine.persistence.SelectListDescriptor;
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

    private FlowNodeInstanceService flowNodeInstanceService;

    private final TechnicalLoggerService logger;

    public GatewayInstanceServiceImpl(final Recorder recorder, final EventService eventService, final ReadPersistenceService persistenceRead,
                                      final TechnicalLoggerService logger, FlowNodeInstanceService flowNodeInstanceService) {
        this.recorder = recorder;
        this.eventService = eventService;
        this.persistenceRead = persistenceRead;
        this.logger = logger;
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
                return isInclusiveGatewayActivated(sDefinition, gatewayInstance);
            case PARALLEL:
                return isParallelGatewayActivated(sDefinition, gatewayInstance);
            default:
                return false;
        }
    }

    boolean isInclusiveGatewayActivated(final SProcessDefinition sDefinition, final SGatewayInstance gatewayInstance) throws SBonitaReadException {
            logger.log(TAG, TechnicalLogSeverity.DEBUG,
                    "Evaluate if gateway " + gatewayInstance.getName() + " of instance " + gatewayInstance.getRootProcessInstanceId() + " of definition "
                            + sDefinition.getName() + " must be activated ");
        SFlowElementContainerDefinition processContainer = sDefinition.getProcessContainer();
        SFlowNodeDefinition gatewayDefinition = processContainer.getFlowNode(gatewayInstance.getFlowNodeDefinitionId());
        long processInstanceId = gatewayInstance.getParentContainerId();

        List<String> hitByTransitionList = getHitByTransitionList(gatewayInstance);
        List<STransitionDefinition> incomingTransitions = gatewayDefinition.getIncomingTransitions();
        List<STransitionDefinition> incomingWithTokens = new ArrayList<STransitionDefinition>();
        List<STransitionDefinition> incomingWithoutTokens = new ArrayList<STransitionDefinition>();

        logger.log(TAG, TechnicalLogSeverity.DEBUG, "HitBys = " + gatewayInstance.getHitBys());
        for (int i = 0; i < incomingTransitions.size(); i++) {
            STransitionDefinition currentTransition = incomingTransitions.get(i);
            if (hitByTransitionList.contains(String.valueOf(i + 1))) {
                incomingWithTokens.add(currentTransition);
            } else {
                incomingWithoutTokens.add(currentTransition);
            }
        }
        /*
            We create two lists that will contain transition definitions that have a path to the gateway finishing with
            a token and the list of transition that does not have a path to the gateway finishing with a token
         */
        List<STransitionDefinition> finishWithAToken = new ArrayList<STransitionDefinition>();
        List<STransitionDefinition> doesNotFinishWithAToken = new ArrayList<STransitionDefinition>();

        /*
         * we calculate all transition definitions that can block the gateway
         */
        addBackwardReachableTransitions(processContainer, gatewayDefinition, incomingWithTokens, finishWithAToken, Collections.<STransitionDefinition>emptyList());
        addBackwardReachableTransitions(processContainer, gatewayDefinition, incomingWithoutTokens, doesNotFinishWithAToken, finishWithAToken);
        /*
         * we check if one of the transitions that are 'blocking' contains a token in this process instance
         */
        return !transitionsContainsAToken(doesNotFinishWithAToken, gatewayDefinition, processInstanceId, processContainer);
    }

    boolean transitionsContainsAToken(List<STransitionDefinition> transitions, SFlowNodeDefinition gatewayDefinition, long processInstanceId,
                                              SFlowElementContainerDefinition processContainer) throws SBonitaReadException {
        List<SFlowNodeDefinition> sourceElements = new ArrayList<SFlowNodeDefinition>();
        List<SFlowNodeDefinition> targetElements = new ArrayList<SFlowNodeDefinition>();

        logger.log(TAG, TechnicalLogSeverity.DEBUG, "Check if there is a token on "+transitions);
        for (STransitionDefinition sTransitionDefinition : transitions) {
            SFlowNodeDefinition source = processContainer.getFlowNode(sTransitionDefinition.getSource());
            if (!source.equals(gatewayDefinition) && !sourceElements.contains(source)) {
                sourceElements.add(source);
            }
            SFlowNodeDefinition target = processContainer.getFlowNode(sTransitionDefinition.getTarget());
            if (!target.equals(gatewayDefinition) && !targetElements.contains(target)) {
                targetElements.add(target);
            }
        }
        List<SFlowNodeDefinition> sourceAndTarget = extractElementThatAreSourceAndTarget(sourceElements, targetElements);
        if (containsToken(processInstanceId, sourceAndTarget, null)) return true;
        if (containsToken(processInstanceId, sourceElements, true)) return true;
        if (containsToken(processInstanceId, targetElements, false)) return true;
        logger.log(TAG, TechnicalLogSeverity.DEBUG, "No token to wait, gateway will fire");
        return false;
    }

    List<SFlowNodeDefinition> extractElementThatAreSourceAndTarget(List<SFlowNodeDefinition> sourceElements, List<SFlowNodeDefinition> targetElements) {
        List<SFlowNodeDefinition> sourceAndTarget = new ArrayList<SFlowNodeDefinition>();
        Iterator<SFlowNodeDefinition> iterator = sourceElements.iterator();
        while (iterator.hasNext()){
            SFlowNodeDefinition sourceElement = iterator.next();
            if(targetElements.contains(sourceElement) || sourceElement.getIncomingTransitions().isEmpty() /* if it's a start element it's also considered as a target */){
                iterator.remove();
                targetElements.remove(sourceElement);
                sourceAndTarget.add(sourceElement);
            }
        }
        return sourceAndTarget;
    }

    /**
     *
     * @param processInstanceId
     * @param elements
     * @param shouldBeTerminal
     *      is true is the element should be in state terminal
     *      is false if it should not be in terminal
     *      is null if it should be either in terminal or not
     * @return
     * @throws SBonitaReadException
     */
    boolean containsToken(long processInstanceId, List<SFlowNodeDefinition> elements, Boolean shouldBeTerminal) throws SBonitaReadException {
        for (SFlowNodeDefinition element : elements) {
            List<SFlowNodeInstance> sFlowNodeInstances = getFlowNodes(processInstanceId, element);
            for (SFlowNodeInstance sFlowNodeInstance : sFlowNodeInstances) {
                if (shouldBeTerminal == null || (shouldBeTerminal ^ !sFlowNodeInstance.isTerminal())) {
                    logger.log(TAG, TechnicalLogSeverity.DEBUG, "flow node " + sFlowNodeInstance.getName() + " contain a token, gateway not merged");
                    return true;
                }
            }
        }
        return false;
    }

    private List<SFlowNodeInstance> getFlowNodes(long processInstanceId, SFlowNodeDefinition sourceElement) throws SBonitaReadException {
        List<FilterOption> filters = new ArrayList<FilterOption>();
        filters.add(new FilterOption(SFlowNodeInstance.class, "name", sourceElement.getName()));
        filters.add(new FilterOption(SFlowNodeInstance.class, "parentContainerId", processInstanceId));
        QueryOptions searchOptions = new QueryOptions(0, 20, Collections.<OrderByOption>emptyList(), filters, null);
        return flowNodeInstanceService.searchFlowNodeInstances(
                SFlowNodeInstance.class,
                searchOptions);
    }

    void addBackwardReachableTransitions(SFlowElementContainerDefinition processContainer, SFlowNodeDefinition gatewayDefinition,
                                                 List<STransitionDefinition> transitions, List<STransitionDefinition> backwardReachable, List<STransitionDefinition> notIn) {
        for (STransitionDefinition sTransitionDefinition : transitions) {
            if (!backwardReachable.contains(sTransitionDefinition) && !notIn.contains(sTransitionDefinition)) {
                backwardReachable.add(sTransitionDefinition);
                //if the source is the gateway we stop searching backward
                SFlowNodeDefinition flowNode = processContainer.getFlowNode(sTransitionDefinition.getSource());
                if (!flowNode.equals(gatewayDefinition)) {
                    addBackwardReachableTransitions(processContainer, gatewayDefinition, flowNode.getIncomingTransitions(), backwardReachable, notIn);
                }
            }
        }
    }

    boolean isParallelGatewayActivated(final SProcessDefinition sDefinition, final SGatewayInstance gatewayInstance) {
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
        final SFlowNodeDefinition gatewayDefinition = processContainer.getFlowNode(gatewayInstance.getFlowNodeDefinitionId());
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
            selectOne = persistenceRead.selectOne(SelectDescriptorBuilder.getActiveGatewayInstanceOfProcess(parentProcessInstanceId, name));
        } catch (final SBonitaReadException e) {
            throw new SGatewayReadException(e);
        }
        if (selectOne == null) {
            throw new SGatewayNotFoundException(parentProcessInstanceId, name);
        }
        return selectOne;
    }

    @Override
    public List<SGatewayInstance> setFinishAndCreateNewGatewayForRemainingToken(SProcessDefinition processDefinition, final SGatewayInstance gatewayInstance) throws SBonitaException {
        List<String> hitBys = getHitByTransitionList(gatewayInstance);
        List<String> merged = getMergedTokens(gatewayInstance, hitBys);
        setFinished(gatewayInstance, merged.size());
        List<String> remaining = getRemainingTokens(hitBys, merged);
        logger.log(TAG, TechnicalLogSeverity.DEBUG, "There is "+remaining+" remaining token to merge on gateway "+gatewayInstance.getName()+" will create a new if there is");
        if(remaining.isEmpty()){
            return Collections.emptyList();
        }
        List<SGatewayInstance> toFire = new ArrayList<SGatewayInstance>();
        SGatewayInstance newGatewayInstance = createGatewayWithRemainingTokens(gatewayInstance, remaining);
        if(checkMergingCondition(processDefinition,newGatewayInstance)){
            toFire.add(newGatewayInstance);
            toFire.addAll(setFinishAndCreateNewGatewayForRemainingToken(processDefinition, newGatewayInstance));
        }
        return toFire;
    }

    List<String> getMergedTokens(SGatewayInstance gatewayInstance, List<String> hitBys) {
        List<String> merged = null;
        switch (gatewayInstance.getGatewayType()){
            case PARALLEL:
                merged = unique(hitBys);
                break;
            case INCLUSIVE:
                merged = unique(hitBys);
                break;
            case EXCLUSIVE:
                merged = Collections.singletonList(hitBys.get(0));
                break;
        }
        return merged;
    }

    private ArrayList<String> unique(List<String> hitBys) {
        ArrayList<String> merged = new ArrayList<String>();
        for (String hitBy : hitBys) {
            if(!merged.contains(hitBy)){
                merged.add(hitBy);
            }
        }
        return merged;
    }

    ArrayList<String> getRemainingTokens(List<String> hitBys, List<String> merged) {
        ArrayList<String> remaining = new ArrayList<String>(hitBys);
        for (String mergedToken : merged) {
            remaining.remove(mergedToken);
        }
        return remaining;
    }

    /**
     * create a new gateway instance with the remaining token
     * 
     * @param gatewayInstance
     *        the original gatewayInstance
     * @param remaining
     *        the token that already hit the gateway
     * @return
     *         the new gateway
     */
    private SGatewayInstance createGatewayWithRemainingTokens(SGatewayInstance gatewayInstance, List<String> remaining) throws SGatewayCreationException {
        SGatewayInstanceImpl sGatewayInstance = new SGatewayInstanceImpl(gatewayInstance);
        String remainingTokenAsString = "";
        for (String token : remaining) {
            remainingTokenAsString += token + ",";
        }
        sGatewayInstance.setHitBys(remainingTokenAsString.substring(0, remainingTokenAsString.length() - 1));
        createGatewayInstance(sGatewayInstance);
        return sGatewayInstance;
    }

    void setFinished(SGatewayInstance gatewayInstance, int numberOfTokenToMerge) throws SGatewayModificationException {
        final String columnValue = FINISH + numberOfTokenToMerge;
        logger.log(TAG, TechnicalLogSeverity.TRACE, "set finish on gateway " + gatewayInstance.getName() + " " + columnValue);
        updateOneColum(gatewayInstance, sGatewayInstanceBuilderFactory.getHitBysKey(), columnValue, GATEWAYINSTANCE_HITBYS);
    }


    @Override
    public List<SGatewayInstance> getInclusiveGatewaysOfProcessInstanceThatShouldFire(SProcessDefinition processDefinition, long processInstanceId) throws SBonitaReadException {
        List<SGatewayInstance> sGatewayInstances = getInclusiveGatewayInstanceOfProcessInstance(processInstanceId);
        ArrayList<SGatewayInstance> shouldFire = new ArrayList<SGatewayInstance>();
        for (SGatewayInstance sGatewayInstance : sGatewayInstances) {
            if(isInclusiveGatewayActivated(processDefinition, sGatewayInstance)){
                shouldFire.add(sGatewayInstance);
            }
        }
        return shouldFire;
    }

    private List<SGatewayInstance> getInclusiveGatewayInstanceOfProcessInstance(long processInstanceId) throws SBonitaReadException {
        final HashMap<String, Object> hashMap = new HashMap<String, Object>(2);
        hashMap.put("processInstanceId", processInstanceId);
        SelectListDescriptor<SGatewayInstance> getGatewayMergingToken = new SelectListDescriptor<SGatewayInstance>("getInclusiveGatewayInstanceOfProcessInstance", hashMap, SGatewayInstance.class, new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS));
        return persistenceRead.selectList(getGatewayMergingToken);
    }
}
