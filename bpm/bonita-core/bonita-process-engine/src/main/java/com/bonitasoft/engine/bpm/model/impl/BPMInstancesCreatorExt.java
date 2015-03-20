/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.model.impl;

import java.util.List;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.data.instance.TransientDataService;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SBusinessDataDefinition;
import org.bonitasoft.engine.core.process.definition.model.SLoopCharacteristics;
import org.bonitasoft.engine.core.process.definition.model.SMultiInstanceLoopCharacteristics;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.GatewayInstanceService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.api.ParentContainerResolver;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.expression.exception.SExpressionException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.SBonitaReadException;

import com.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SRefBusinessDataInstanceCreationException;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SRefBusinessDataInstanceNotFoundException;
import com.bonitasoft.engine.core.process.instance.model.SMultiRefBusinessDataInstance;
import com.bonitasoft.engine.core.process.instance.model.SRefBusinessDataInstance;
import com.bonitasoft.engine.core.process.instance.model.builder.SRefBusinessDataInstanceBuilderFactory;

/**
 * @author Matthieu Chaffotte
 */
public class BPMInstancesCreatorExt extends BPMInstancesCreator {

    private final RefBusinessDataService refBusinessDataService;

    public BPMInstancesCreatorExt(final ActivityInstanceService activityInstanceService, final ActorMappingService actorMappingService,
            final GatewayInstanceService gatewayInstanceService, final EventInstanceService eventInstanceService,
            final ConnectorInstanceService connectorInstanceService, final ExpressionResolverService expressionResolverService,
            final DataInstanceService dataInstanceService, final TechnicalLoggerService logger, final TransientDataService transientDataService,
            final RefBusinessDataService refBusinessDataService, final ParentContainerResolver parentContainerResolver) {
        super(activityInstanceService, actorMappingService, gatewayInstanceService, eventInstanceService, connectorInstanceService, expressionResolverService,
                dataInstanceService, logger, transientDataService, parentContainerResolver);
        this.refBusinessDataService = refBusinessDataService;
    }

    @Override
    protected void createDataInstancesForMultiInstance(final SActivityDefinition activityDefinition, final SFlowNodeInstance flowNodeInstance,
            final SExpressionContext expressionContext) throws SDataInstanceException, SExpressionException {
        final SLoopCharacteristics loopCharacteristics = activityDefinition.getLoopCharacteristics();
        final SMultiInstanceLoopCharacteristics miLoop = (SMultiInstanceLoopCharacteristics) loopCharacteristics;
        final SBusinessDataDefinition outputBusinessData = activityDefinition.getBusinessDataDefinition(miLoop.getDataOutputItemRef());
        final SRefBusinessDataInstanceBuilderFactory instanceFactory = BuilderFactory.get(SRefBusinessDataInstanceBuilderFactory.class);
        if (outputBusinessData != null) {
            final SRefBusinessDataInstance outputRefInstance = instanceFactory.createNewInstanceForFlowNode(outputBusinessData.getName(),
                    flowNodeInstance.getId(), null, outputBusinessData.getClassName()).done();
            addRefBusinessData(outputRefInstance);
        }
        final SBusinessDataDefinition inputBusinessData = activityDefinition.getBusinessDataDefinition(miLoop.getDataInputItemRef());
        if (inputBusinessData != null) {
            try {
                final SMultiRefBusinessDataInstance loopDataRefInstance = (SMultiRefBusinessDataInstance) refBusinessDataService.getRefBusinessDataInstance(
                        miLoop.getLoopDataInputRef(), flowNodeInstance.getParentProcessInstanceId());
                final List<Long> dataIds = loopDataRefInstance.getDataIds();
                final SRefBusinessDataInstance inputRefInstance = instanceFactory.createNewInstanceForFlowNode(inputBusinessData.getName(),
                        flowNodeInstance.getId(), dataIds.get(flowNodeInstance.getLoopCounter()), inputBusinessData.getClassName()).done();
                addRefBusinessData(inputRefInstance);
            } catch (final SRefBusinessDataInstanceNotFoundException srbdinfe) {
                throw new SDataInstanceException(srbdinfe);
            } catch (final SBonitaReadException sbe) {
                throw new SDataInstanceException(sbe);
            }
        }
        super.createDataInstancesForMultiInstance(activityDefinition, flowNodeInstance, expressionContext);
    }

    private void addRefBusinessData(final SRefBusinessDataInstance instance) throws SDataInstanceException {
        try {
            refBusinessDataService.addRefBusinessDataInstance(instance);
        } catch (final SRefBusinessDataInstanceCreationException sbrdice) {
            throw new SDataInstanceException(sbrdice);
        }
    }

}
