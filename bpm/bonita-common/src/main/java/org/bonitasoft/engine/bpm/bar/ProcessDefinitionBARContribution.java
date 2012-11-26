/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.bar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.ValidationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.bonitasoft.engine.bpm.bar.xml.ActorDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.ActorInitiatorDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.AutomaticTaskDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.BoundaryEventDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.CallActivityDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.CallableElementBinding;
import org.bonitasoft.engine.bpm.bar.xml.CallableElementVersionBinding;
import org.bonitasoft.engine.bpm.bar.xml.CatchErrorEventTriggerDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.CatchMessageEventTriggerDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.CatchSignalEventTriggerDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.ConditionalExpressionBinding;
import org.bonitasoft.engine.bpm.bar.xml.ConnectorDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.ConnectorDefinitionInputBinding;
import org.bonitasoft.engine.bpm.bar.xml.CorrelationBinding;
import org.bonitasoft.engine.bpm.bar.xml.CorrelationKeyBinding;
import org.bonitasoft.engine.bpm.bar.xml.CorrelationValueBinding;
import org.bonitasoft.engine.bpm.bar.xml.DataDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.DataInputOperationBinding;
import org.bonitasoft.engine.bpm.bar.xml.DataOutputOperationBinding;
import org.bonitasoft.engine.bpm.bar.xml.DefaultTransitionDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.DefaultValueBinding;
import org.bonitasoft.engine.bpm.bar.xml.DisplayDescriptionAfterCompletionExpressionBinding;
import org.bonitasoft.engine.bpm.bar.xml.DisplayDescriptionExpressionBinding;
import org.bonitasoft.engine.bpm.bar.xml.DisplayNameExpressionBinding;
import org.bonitasoft.engine.bpm.bar.xml.DocumentDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.EndEventDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.ExpressionBinding;
import org.bonitasoft.engine.bpm.bar.xml.FlowElementBinding;
import org.bonitasoft.engine.bpm.bar.xml.GatewayDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.IncomingTransitionRefBinding;
import org.bonitasoft.engine.bpm.bar.xml.IntermediateCatchEventBinding;
import org.bonitasoft.engine.bpm.bar.xml.IntermediateThrowEventDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.LeftOperandBinding;
import org.bonitasoft.engine.bpm.bar.xml.LoopConditionBinding;
import org.bonitasoft.engine.bpm.bar.xml.LoopMaxBinding;
import org.bonitasoft.engine.bpm.bar.xml.ManualTaskDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.MultiInstanceCompletionConditionBinding;
import org.bonitasoft.engine.bpm.bar.xml.MultiInstanceLoopCardinalityBinding;
import org.bonitasoft.engine.bpm.bar.xml.MultiInstanceLoopCharacteristicsBinding;
import org.bonitasoft.engine.bpm.bar.xml.OperationBinding;
import org.bonitasoft.engine.bpm.bar.xml.OutgoingTransitionRefBinding;
import org.bonitasoft.engine.bpm.bar.xml.ParameterDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.ProcessDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.RightOperandBinding;
import org.bonitasoft.engine.bpm.bar.xml.StandardLoopCharacteristicsBinding;
import org.bonitasoft.engine.bpm.bar.xml.StartEventDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.SubProcessDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.TargetFlowNodeBinding;
import org.bonitasoft.engine.bpm.bar.xml.TargetProcessBinding;
import org.bonitasoft.engine.bpm.bar.xml.TerminateEventTriggerDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.ThrowErrorEventTriggerDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.ThrowMessageEventTriggerDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.ThrowSignalEventTriggerDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.TimerEventTriggerDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.TransitionDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.UserFilterDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.UserTaskDefinitionBinding;
import org.bonitasoft.engine.bpm.bar.xml.XMLProcessDefinition;
import org.bonitasoft.engine.bpm.model.DesignProcessDefinition;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.InvalidBusinessArchiveFormat;
import org.bonitasoft.engine.io.xml.ElementBinding;
import org.bonitasoft.engine.io.xml.XMLHandler;
import org.bonitasoft.engine.io.xml.XMLNode;
import org.bonitasoft.engine.io.xml.exceptions.InvalidSchemaException;
import org.bonitasoft.engine.io.xml.exceptions.XMLParseException;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class ProcessDefinitionBARContribution implements BusinessArchiveContribution {

    public static final String PROCESS_DEFINITION_XML = "process-design.xml";

    private XMLHandler handler;

    public ProcessDefinitionBARContribution() {
        final List<Class<? extends ElementBinding>> bindings = new ArrayList<Class<? extends ElementBinding>>();
        bindings.add(ProcessDefinitionBinding.class);
        bindings.add(ActorDefinitionBinding.class);
        bindings.add(ActorInitiatorDefinitionBinding.class);
        bindings.add(UserTaskDefinitionBinding.class);
        bindings.add(ManualTaskDefinitionBinding.class);
        bindings.add(AutomaticTaskDefinitionBinding.class);
        bindings.add(TransitionDefinitionBinding.class);
        bindings.add(GatewayDefinitionBinding.class);
        bindings.add(DefaultTransitionDefinitionBinding.class);
        bindings.add(ConnectorDefinitionBinding.class);
        bindings.add(ConnectorDefinitionInputBinding.class);
        bindings.add(UserFilterDefinitionBinding.class);
        bindings.add(ParameterDefinitionBinding.class);
        bindings.add(StartEventDefinitionBinding.class);
        bindings.add(IntermediateCatchEventBinding.class);
        bindings.add(BoundaryEventDefinitionBinding.class);
        bindings.add(TimerEventTriggerDefinitionBinding.class);
        bindings.add(EndEventDefinitionBinding.class);
        bindings.add(ExpressionBinding.class);
        bindings.add(ConditionalExpressionBinding.class);
        bindings.add(DataDefinitionBinding.class);
        bindings.add(DocumentDefinitionBinding.class);
        bindings.add(DefaultValueBinding.class);
        bindings.add(DisplayDescriptionAfterCompletionExpressionBinding.class);
        bindings.add(DisplayDescriptionExpressionBinding.class);
        bindings.add(DisplayNameExpressionBinding.class);
        bindings.add(OutgoingTransitionRefBinding.class);
        bindings.add(IncomingTransitionRefBinding.class);
        bindings.add(CatchMessageEventTriggerDefinitionBinding.class);
        bindings.add(OperationBinding.class);
        bindings.add(RightOperandBinding.class);
        bindings.add(LeftOperandBinding.class);
        bindings.add(ThrowMessageEventTriggerDefinitionBinding.class);
        bindings.add(CatchSignalEventTriggerDefinitionBinding.class);
        bindings.add(ThrowSignalEventTriggerDefinitionBinding.class);
        bindings.add(IntermediateThrowEventDefinitionBinding.class);
        bindings.add(CatchErrorEventTriggerDefinitionBinding.class);
        bindings.add(ThrowErrorEventTriggerDefinitionBinding.class);
        bindings.add(CorrelationBinding.class);
        bindings.add(CorrelationKeyBinding.class);
        bindings.add(CorrelationValueBinding.class);
        bindings.add(StandardLoopCharacteristicsBinding.class);
        bindings.add(MultiInstanceLoopCharacteristicsBinding.class);
        bindings.add(LoopConditionBinding.class);
        bindings.add(LoopMaxBinding.class);
        bindings.add(MultiInstanceLoopCardinalityBinding.class);
        bindings.add(MultiInstanceCompletionConditionBinding.class);
        bindings.add(CallActivityDefinitionBinding.class);
        bindings.add(DataInputOperationBinding.class);
        bindings.add(DataOutputOperationBinding.class);
        bindings.add(CallableElementBinding.class);
        bindings.add(CallableElementVersionBinding.class);
        bindings.add(TerminateEventTriggerDefinitionBinding.class);
        bindings.add(TargetProcessBinding.class);
        bindings.add(TargetFlowNodeBinding.class);
        bindings.add(SubProcessDefinitionBinding.class);
        bindings.add(FlowElementBinding.class);

        final InputStream schemaStream = ProcessDefinitionBARContribution.class.getResourceAsStream("ProcessDefinition.xsd");
        try {
            handler = new XMLHandler(bindings);
            handler.setSchema(schemaStream);
        } catch (final ParserConfigurationException e) {
            throw new BonitaRuntimeException(e);
        } catch (final InvalidSchemaException e) {
            throw new BonitaRuntimeException(e);
        } catch (final TransformerConfigurationException e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    public boolean isMandatory() {
        return true;
    }

    @Override
    public boolean readFromBarFolder(final BusinessArchive businessArchive, final File barFolder) throws IOException, InvalidBusinessArchiveFormat {
        final File file = new File(barFolder, PROCESS_DEFINITION_XML);
        if (!file.exists()) {
            return false;
        }
        final DesignProcessDefinition processDefinition = deserializeProcessDefinition(file);
        businessArchive.setProcessDefinition(processDefinition);
        return true;
    }

    private DesignProcessDefinition deserializeProcessDefinition(final File file) throws FileNotFoundException, IOException, InvalidBusinessArchiveFormat {
        Object objectFromXML;
        try {
            handler.validate(file);
            objectFromXML = handler.getObjectFromXML(file);
        } catch (final XMLParseException e) {
            throw new InvalidBusinessArchiveFormat(e);
        } catch (final ValidationException e) {
            throw new InvalidBusinessArchiveFormat(e);
        }
        if (objectFromXML instanceof DesignProcessDefinition) {
            return (DesignProcessDefinition) objectFromXML;
        } else {
            throw new InvalidBusinessArchiveFormat("the file did not contain a process but: " + objectFromXML);
        }
    }

    @Override
    public void saveToBarFolder(final BusinessArchive businessArchive, final File barFolder) throws IOException {
        FileOutputStream fout;
        try {
            fout = new FileOutputStream(new File(barFolder, PROCESS_DEFINITION_XML));
            try {
                final DesignProcessDefinition processDefinition = businessArchive.getProcessDefinition();
                final XMLNode rootNode = new XMLProcessDefinition().getXMLProcessDefinition(processDefinition);
                handler.write(rootNode, fout);
            } finally {
                fout.close();
            }
        } catch (final FileNotFoundException e) {
            throw new IOException(e);
        }
    }

    @Override
    public String getName() {
        return "Process design";
    }

}
