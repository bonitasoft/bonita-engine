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
package org.bonitasoft.engine.core.process.definition.model.builder.impl;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.core.operation.model.builder.SOperationBuilders;
import org.bonitasoft.engine.core.process.definition.model.bindings.SActorDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SActorInitiatorDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SAutomaticTaskDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SBoundaryEventDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SCallActivityDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SCallableElementBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SCallableElementVersionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SCatchMessageEventTriggerDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SCatchSignalEventTriggerDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SConditionalExpressionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SConnectorDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SConnectorDefinitionInputBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SCorrelationBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SCorrelationKeyBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SCorrelationValueBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SDataDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SDataInputOperationBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SDataOutputOperationBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SDefaultTransitionDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SDefaultValueBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SDisplayDescriptionAfterCompletionExpressionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SDisplayDescriptionExpressionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SDisplayNameExpressionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SDocumentDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SEndEventDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SExpressionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SFlowElementBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SGatewayDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SIncomingTransitionRefBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SIntermediateCatchEventDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SIntermediateThrowEventDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SLeftOperandBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SLoopConditionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SLoopMaxBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SManualTaskDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SMultiInstanceCompletionConditionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SMultiInstanceLoopCardinalityBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SMultiInstanceLoopCharacteristicsBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SOperationBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SOutgoingTransitionRefBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SParameterDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SProcessDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SRightOperandBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SStandardLoopCharacteristicsBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SStartEventDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SSubProcessDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.STargetFlowNodeBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.STargetProcessBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.STerminateEventTriggerDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SThrowMessageEventTriggerDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SThrowSignalEventTriggerDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.STimerEventTriggerDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.STransitionDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SUserFilterDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SUserTaskDefinitionBinding;
import org.bonitasoft.engine.core.process.definition.model.bindings.SXMLDataDefinitionBinding;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilders;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilders;
import org.bonitasoft.engine.xml.ElementBinding;
import org.bonitasoft.engine.xml.ElementBindingsFactory;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class SProcessElementBindingsFactory implements ElementBindingsFactory {

    private final SExpressionBuilders sExpressionBuilders;

    private final SDataDefinitionBuilders sDataDefinitionBuilders;

    private final SOperationBuilders sOperationBuilders;

    private final ArrayList<ElementBinding> bindings;

    public SProcessElementBindingsFactory(final SExpressionBuilders sExpressionBuilders, final SDataDefinitionBuilders sDataDefinitionBuilders,
            final SOperationBuilders sOperationBuilders) {
        this.sExpressionBuilders = sExpressionBuilders;
        this.sDataDefinitionBuilders = sDataDefinitionBuilders;
        this.sOperationBuilders = sOperationBuilders;
        bindings = new ArrayList<ElementBinding>();
        bindings.add(new SProcessDefinitionBinding());
        bindings.add(new SGatewayDefinitionBinding());
        bindings.add(new SActorDefinitionBinding());
        bindings.add(new SActorInitiatorDefinitionBinding());
        bindings.add(new SConnectorDefinitionBinding());
        bindings.add(new SDefaultTransitionDefinitionBinding());
        bindings.add(new SEndEventDefinitionBinding());
        bindings.add(new SParameterDefinitionBinding());
        bindings.add(new SStartEventDefinitionBinding());
        bindings.add(new SBoundaryEventDefinitionBinding());
        bindings.add(new STransitionDefinitionBinding());
        bindings.add(new SAutomaticTaskDefinitionBinding());
        bindings.add(new SUserTaskDefinitionBinding());
        bindings.add(new SManualTaskDefinitionBinding());
        bindings.add(new SExpressionBinding(sExpressionBuilders));
        bindings.add(new SConditionalExpressionBinding(sExpressionBuilders));
        bindings.add(new SConnectorDefinitionBinding());
        bindings.add(new SConnectorDefinitionInputBinding());
        bindings.add(new SUserFilterDefinitionBinding());
        bindings.add(new SDataDefinitionBinding(sDataDefinitionBuilders));
        bindings.add(new SXMLDataDefinitionBinding(sDataDefinitionBuilders));
        bindings.add(new SDocumentDefinitionBinding());
        bindings.add(new SDefaultValueBinding(sExpressionBuilders));
        bindings.add(new SDisplayDescriptionAfterCompletionExpressionBinding(sExpressionBuilders));
        bindings.add(new SDisplayDescriptionExpressionBinding(sExpressionBuilders));
        bindings.add(new SDisplayNameExpressionBinding(sExpressionBuilders));
        bindings.add(new STimerEventTriggerDefinitionBinding());
        bindings.add(new SIntermediateCatchEventDefinitionBinding());
        bindings.add(new SIncomingTransitionRefBinding());
        bindings.add(new SOutgoingTransitionRefBinding());
        bindings.add(new SCatchMessageEventTriggerDefinitionBinding());
        bindings.add(new SOperationBinding(sOperationBuilders));
        bindings.add(new SRightOperandBinding(sExpressionBuilders));
        bindings.add(new SLeftOperandBinding(sOperationBuilders));
        bindings.add(new SIntermediateThrowEventDefinitionBinding());
        bindings.add(new SThrowMessageEventTriggerDefinitionBinding());
        bindings.add(new SCatchSignalEventTriggerDefinitionBinding());
        bindings.add(new SThrowSignalEventTriggerDefinitionBinding());
        bindings.add(new STerminateEventTriggerDefinitionBinding());
        bindings.add(new SCorrelationBinding());
        bindings.add(new SCorrelationKeyBinding(sExpressionBuilders));
        bindings.add(new SCorrelationValueBinding(sExpressionBuilders));
        bindings.add(new SStandardLoopCharacteristicsBinding());
        bindings.add(new SLoopMaxBinding(sExpressionBuilders));
        bindings.add(new SLoopConditionBinding(sExpressionBuilders));
        bindings.add(new SMultiInstanceCompletionConditionBinding(sExpressionBuilders));
        bindings.add(new SMultiInstanceLoopCardinalityBinding(sExpressionBuilders));
        bindings.add(new SMultiInstanceLoopCharacteristicsBinding());
        bindings.add(new SCallActivityDefinitionBinding());
        bindings.add(new SCallableElementBinding(sExpressionBuilders));
        bindings.add(new SCallableElementVersionBinding(sExpressionBuilders));
        bindings.add(new SDataInputOperationBinding(sOperationBuilders));
        bindings.add(new SDataOutputOperationBinding(sOperationBuilders));
        bindings.add(new STargetProcessBinding(sExpressionBuilders));
        bindings.add(new STargetFlowNodeBinding(sExpressionBuilders));
        bindings.add(new SFlowElementBinding());
        bindings.add(new SSubProcessDefinitionBinding());
    }

    @Override
    public List<ElementBinding> getElementBindings() {
        return bindings;
    }

    @Override
    public ElementBinding createNewInstance(final Class<? extends ElementBinding> binderClass) {
        if (SProcessDefinitionBinding.class.equals(binderClass)) {
            return new SProcessDefinitionBinding();
        }
        if (SGatewayDefinitionBinding.class.equals(binderClass)) {
            return new SGatewayDefinitionBinding();
        }
        if (SActorDefinitionBinding.class.equals(binderClass)) {
            return new SActorDefinitionBinding();
        }
        if (SActorInitiatorDefinitionBinding.class.equals(binderClass)) {
            return new SActorInitiatorDefinitionBinding();
        }
        if (SConnectorDefinitionBinding.class.equals(binderClass)) {
            return new SConnectorDefinitionBinding();
        }
        if (SUserFilterDefinitionBinding.class.equals(binderClass)) {
            return new SUserFilterDefinitionBinding();
        }
        if (SDefaultTransitionDefinitionBinding.class.equals(binderClass)) {
            return new SDefaultTransitionDefinitionBinding();
        }
        if (SIncomingTransitionRefBinding.class.equals(binderClass)) {
            return new SIncomingTransitionRefBinding();
        }
        if (SOutgoingTransitionRefBinding.class.equals(binderClass)) {
            return new SOutgoingTransitionRefBinding();
        }
        if (SDefaultValueBinding.class.equals(binderClass)) {
            return new SDefaultValueBinding(sExpressionBuilders);
        }
        if (SDisplayDescriptionAfterCompletionExpressionBinding.class.equals(binderClass)) {
            return new SDisplayDescriptionAfterCompletionExpressionBinding(sExpressionBuilders);
        }
        if (SDisplayDescriptionExpressionBinding.class.equals(binderClass)) {
            return new SDisplayDescriptionExpressionBinding(sExpressionBuilders);
        }
        if (SDisplayNameExpressionBinding.class.equals(binderClass)) {
            return new SDisplayDescriptionExpressionBinding(sExpressionBuilders);
        }
        if (SDataDefinitionBinding.class.equals(binderClass)) {
            return new SDataDefinitionBinding(sDataDefinitionBuilders);
        }
        if (SXMLDataDefinitionBinding.class.equals(binderClass)) {
            return new SXMLDataDefinitionBinding(sDataDefinitionBuilders);
        }
        if (SDocumentDefinitionBinding.class.equals(binderClass)) {
            return new SDocumentDefinitionBinding();
        }
        if (SEndEventDefinitionBinding.class.equals(binderClass)) {
            return new SEndEventDefinitionBinding();
        }
        if (SParameterDefinitionBinding.class.equals(binderClass)) {
            return new SParameterDefinitionBinding();
        }
        if (SStartEventDefinitionBinding.class.equals(binderClass)) {
            return new SStartEventDefinitionBinding();
        }
        if (SBoundaryEventDefinitionBinding.class.equals(binderClass)) {
            return new SBoundaryEventDefinitionBinding();
        }
        if (SIntermediateCatchEventDefinitionBinding.class.equals(binderClass)) {
            return new SIntermediateCatchEventDefinitionBinding();
        }
        if (STransitionDefinitionBinding.class.equals(binderClass)) {
            return new STransitionDefinitionBinding();
        }
        if (SAutomaticTaskDefinitionBinding.class.equals(binderClass)) {
            return new SAutomaticTaskDefinitionBinding();
        }
        if (SUserTaskDefinitionBinding.class.equals(binderClass)) {
            return new SUserTaskDefinitionBinding();
        }
        if (SManualTaskDefinitionBinding.class.equals(binderClass)) {
            return new SManualTaskDefinitionBinding();
        }
        if (SExpressionBinding.class.equals(binderClass)) {
            return new SExpressionBinding(sExpressionBuilders);
        }
        if (SConditionalExpressionBinding.class.equals(binderClass)) {
            return new SConditionalExpressionBinding(sExpressionBuilders);
        }
        if (SConnectorDefinitionBinding.class.equals(binderClass)) {
            return new SConnectorDefinitionBinding();
        }
        if (SConnectorDefinitionInputBinding.class.equals(binderClass)) {
            return new SConnectorDefinitionInputBinding();
        }
        if (STimerEventTriggerDefinitionBinding.class.equals(binderClass)) {
            return new STimerEventTriggerDefinitionBinding();
        }
        if (SCatchMessageEventTriggerDefinitionBinding.class.equals(binderClass)) {
            return new SCatchMessageEventTriggerDefinitionBinding();
        }
        if (SCatchSignalEventTriggerDefinitionBinding.class.equals(binderClass)) {
            return new SCatchSignalEventTriggerDefinitionBinding();
        }
        if (SOperationBinding.class.equals(binderClass)) {
            return new SOperationBinding(sOperationBuilders);
        }
        if (SRightOperandBinding.class.equals(binderClass)) {
            return new SRightOperandBinding(sExpressionBuilders);
        }
        if (SLeftOperandBinding.class.equals(binderClass)) {
            return new SLeftOperandBinding(sOperationBuilders);
        }
        if (SIntermediateThrowEventDefinitionBinding.class.equals(binderClass)) {
            return new SIntermediateThrowEventDefinitionBinding();
        }
        if (SThrowMessageEventTriggerDefinitionBinding.class.equals(binderClass)) {
            return new SThrowMessageEventTriggerDefinitionBinding();
        }
        if (SThrowSignalEventTriggerDefinitionBinding.class.equals(binderClass)) {
            return new SThrowSignalEventTriggerDefinitionBinding();
        }
        if (STerminateEventTriggerDefinitionBinding.class.equals(binderClass)) {
            return new STerminateEventTriggerDefinitionBinding();
        }
        if (SCorrelationBinding.class.equals(binderClass)) {
            return new SCorrelationBinding();
        }
        if (SCorrelationKeyBinding.class.equals(binderClass)) {
            return new SCorrelationKeyBinding(sExpressionBuilders);
        }
        if (SCorrelationValueBinding.class.equals(binderClass)) {
            return new SCorrelationValueBinding(sExpressionBuilders);
        }
        if (SStandardLoopCharacteristicsBinding.class.equals(binderClass)) {
            return new SStandardLoopCharacteristicsBinding();
        }
        if (SLoopConditionBinding.class.equals(binderClass)) {
            return new SLoopConditionBinding(sExpressionBuilders);
        }
        if (SLoopMaxBinding.class.equals(binderClass)) {
            return new SLoopMaxBinding(sExpressionBuilders);
        }
        if (SMultiInstanceCompletionConditionBinding.class.equals(binderClass)) {
            return new SMultiInstanceCompletionConditionBinding(sExpressionBuilders);
        }
        if (SMultiInstanceLoopCardinalityBinding.class.equals(binderClass)) {
            return new SMultiInstanceLoopCardinalityBinding(sExpressionBuilders);
        }
        if (SMultiInstanceLoopCharacteristicsBinding.class.equals(binderClass)) {
            return new SMultiInstanceLoopCharacteristicsBinding();
        }
        if (SCallActivityDefinitionBinding.class.equals(binderClass)) {
            return new SCallActivityDefinitionBinding();
        }
        if (SCallableElementBinding.class.equals(binderClass)) {
            return new SCallableElementBinding(sExpressionBuilders);
        }
        if (SCallableElementVersionBinding.class.equals(binderClass)) {
            return new SCallableElementVersionBinding(sExpressionBuilders);
        }
        if (SDataInputOperationBinding.class.equals(binderClass)) {
            return new SDataInputOperationBinding(sOperationBuilders);
        }
        if (SDataOutputOperationBinding.class.equals(binderClass)) {
            return new SDataOutputOperationBinding(sOperationBuilders);
        }
        if (STargetProcessBinding.class.equals(binderClass)) {
            return new STargetProcessBinding(sExpressionBuilders);
        }
        if (STargetFlowNodeBinding.class.equals(binderClass)) {
            return new STargetFlowNodeBinding(sExpressionBuilders);
        }
        if (SSubProcessDefinitionBinding.class.equals(binderClass)) {
            return new SSubProcessDefinitionBinding();
        }
        if (SFlowElementBinding.class.equals(binderClass)) {
            return new SFlowElementBinding();
        }
        return null;
    }
}
