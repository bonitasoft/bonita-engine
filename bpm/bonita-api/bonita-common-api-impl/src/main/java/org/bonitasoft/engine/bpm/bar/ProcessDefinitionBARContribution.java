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
package org.bonitasoft.engine.bpm.bar;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.BoundaryEventDefinition;
import org.bonitasoft.engine.bpm.flownode.EndEventDefinition;
import org.bonitasoft.engine.bpm.flownode.ErrorEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.FlowElementContainerDefinition;
import org.bonitasoft.engine.bpm.flownode.IntermediateCatchEventDefinition;
import org.bonitasoft.engine.bpm.flownode.IntermediateThrowEventDefinition;
import org.bonitasoft.engine.bpm.flownode.MessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.SignalEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.StartEventDefinition;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.internal.BoundaryEventDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.EndEventDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.IntermediateCatchEventDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.IntermediateThrowEventDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.StartEventDefinitionImpl;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.internal.DesignProcessDefinitionImpl;
import org.bonitasoft.engine.bpm.process.impl.internal.SubProcessDefinitionImpl;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.io.xml.XMLParseException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class ProcessDefinitionBARContribution implements BusinessArchiveContribution {

    public static final String PROCESS_DEFINITION_XML = "process-design.xml";

    public static final String PROCESS_INFOS_FILE = "process-infos.txt";

    private Marshaller marshaller;

    private Unmarshaller unmarshaller;

    public ProcessDefinitionBARContribution() {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(DesignProcessDefinitionImpl.class);
            marshaller = jaxbContext.createMarshaller();
            unmarshaller = jaxbContext.createUnmarshaller();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    public boolean isMandatory() {
        return true;
    }

    @Override
    public boolean readFromBarFolder(final BusinessArchive businessArchive, final File barFolder) throws IOException, InvalidBusinessArchiveFormatException {
        final File file = new File(barFolder, PROCESS_DEFINITION_XML);
        if (!file.exists()) {
            return false;
        }
        final DesignProcessDefinition processDefinition = deserializeProcessDefinition(file);
        businessArchive.setProcessDefinition(processDefinition);
        checkProcessInfos(barFolder, processDefinition);
        return true;
    }

    protected void checkProcessInfos(final File barFolder, final DesignProcessDefinition processDefinition) throws InvalidBusinessArchiveFormatException {
        final String processInfos = getProcessInfos(generateInfosFromDefinition(processDefinition));
        String fileContent;
        try {
            fileContent = IOUtil.read(new File(barFolder, PROCESS_INFOS_FILE));
            if (!processInfos.equals(fileContent.trim())) {
                throw new InvalidBusinessArchiveFormatException("Invalid Business Archive format");
            }
        } catch (final IOException e) {
            throw new InvalidBusinessArchiveFormatException("Invalid Business Archive format");
        }
    }

    public DesignProcessDefinition deserializeProcessDefinition(final File file) throws IOException, InvalidBusinessArchiveFormatException {
        try {
            Object deserializedObject = unmarshaller.unmarshal(file);
            if (!(deserializedObject instanceof DesignProcessDefinition)) {
                throw new InvalidBusinessArchiveFormatException("The file did not contain a process, but: " + deserializedObject);
            }
            DesignProcessDefinitionImpl process = (DesignProcessDefinitionImpl) deserializedObject;
            if (process.getActorInitiator() != null) {
                process.getActorInitiator().setInitiator(true);
            }
            updateEventTriggersOnProcess(process.getFlowElementContainer());
            return (DesignProcessDefinition) deserializedObject;
        } catch (final ValidationException e) {
            checkVersion(IOUtil.read(file));
            throw new InvalidBusinessArchiveFormatException(e);
        } catch (JAXBException e) {
            throw new InvalidBusinessArchiveFormatException("Deserialization of the ProcessDesignFailed", e);
        }
    }

    void checkVersion(final String content) throws InvalidBusinessArchiveFormatException {
        final Pattern pattern = Pattern.compile("http://www\\.bonitasoft\\.org/ns/process/client/6.([0-9])");
        final Matcher matcher = pattern.matcher(content);
        final boolean find = matcher.find();
        if (!find) {
            throw new InvalidBusinessArchiveFormatException("There is no bonitasoft process namespace declaration");
        }
        final String group = matcher.group(1);
        if (!group.equals("3")) {
            throw new InvalidBusinessArchiveFormatException("Wrong version of your process definition, 6." + group
                    + " namespace is not compatible with your current version. Use the studio to update it.");
        }
    }

    @Override
    public void saveToBarFolder(final BusinessArchive businessArchive, final File barFolder) throws IOException {
        final DesignProcessDefinition processDefinition = businessArchive.getProcessDefinition();
        serializeProcessDefinition(barFolder, processDefinition);
    }

    public void serializeProcessDefinition(final File barFolder, final DesignProcessDefinition processDefinition) throws IOException {
        StringWriter result = new StringWriter();
        try {
            marshaller.marshal(processDefinition, result);
            try (FileOutputStream outputStream = new FileOutputStream(new File(barFolder, PROCESS_DEFINITION_XML))) {
                outputStream.write(result.toString().getBytes());
            }
            final String infos = generateInfosFromDefinition(processDefinition);
            IOUtil.writeContentToFile(getProcessInfos(infos), new File(barFolder, PROCESS_INFOS_FILE));
        } catch (final FileNotFoundException | JAXBException e) {
            throw new IOException(e);
        }
    }

    public String convertProcessToXml(DesignProcessDefinition processDefinition) throws IOException {
        final StringWriter writer = new StringWriter();
        try {
            marshaller.marshal(processDefinition, writer);
        } catch (JAXBException e) {
            throw new IOException(e);
        }
        return writer.toString();
    }

    public DesignProcessDefinition convertXmlToProcess(String content) throws IOException, XMLParseException {
        try (InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {

            DesignProcessDefinition process = (DesignProcessDefinition) unmarshaller.unmarshal(stream);
            if (process.getActorInitiator() != null) {
                process.getActorInitiator().setInitiator(true);
            }
            updateEventTriggersOnProcess(process.getFlowElementContainer());
            return process;
        } catch (java.lang.UnsupportedOperationException | JAXBException e) {
            throw new IOException("Failed to deserialize the XML string provided", e);
        }
    }

    private void updateEventTriggersOnProcess(FlowElementContainerDefinition flowElementContainer) {
        IntermediateThrowEventDefinitionImpl throwEventImpl;
        IntermediateCatchEventDefinitionImpl catchEventImpl;
        EndEventDefinitionImpl endEventImpl;
        BoundaryEventDefinitionImpl boundaryEventImpl;
        StartEventDefinitionImpl startEventImpl;
        SubProcessDefinitionImpl subProcess;
        for (IntermediateCatchEventDefinition catchEvent : flowElementContainer.getIntermediateCatchEvents()) {
            catchEventImpl = (IntermediateCatchEventDefinitionImpl) catchEvent;
            for (MessageEventTriggerDefinition messageEventTrigger : catchEvent.getMessageEventTriggerDefinitions()) {
                catchEventImpl.addEventTrigger(messageEventTrigger);
            }
            for (ErrorEventTriggerDefinition errorEventTrigger : catchEvent.getErrorEventTriggerDefinitions()) {
                catchEventImpl.addEventTrigger(errorEventTrigger);
            }
            for (SignalEventTriggerDefinition signalEventTrigger : catchEventImpl.getSignalEventTriggerDefinitions()) {
                catchEventImpl.addEventTrigger(signalEventTrigger);
            }
            for (TimerEventTriggerDefinition timerEventTrigger : catchEventImpl.getTimerEventTriggerDefinitions()) {
                catchEventImpl.addEventTrigger(timerEventTrigger);
            }

        }
        for (IntermediateThrowEventDefinition throwEvent : flowElementContainer.getIntermediateThrowEvents()) {
            throwEventImpl = (IntermediateThrowEventDefinitionImpl) throwEvent;
            for (MessageEventTriggerDefinition messageEventTriggerDefinition : throwEventImpl.getMessageEventTriggerDefinitions()) {
                throwEventImpl.addEventTrigger(messageEventTriggerDefinition);
            }
            for (SignalEventTriggerDefinition signalEventTriggerDefinition : throwEventImpl.getSignalEventTriggerDefinitions()) {
                throwEventImpl.addEventTrigger(signalEventTriggerDefinition);
            }
        }
        for (EndEventDefinition endEvent : flowElementContainer.getEndEvents()) {
            endEventImpl = (EndEventDefinitionImpl) endEvent;
            for (ErrorEventTriggerDefinition errorEventTrigger : endEventImpl.getErrorEventTriggerDefinitions()) {
                endEventImpl.addEventTrigger(errorEventTrigger);
            }
            for (SignalEventTriggerDefinition signalEventTrigger : endEventImpl.getSignalEventTriggerDefinitions()) {
                endEventImpl.addEventTrigger(signalEventTrigger);
            }
            for (MessageEventTriggerDefinition messageEventTrigger : endEventImpl.getMessageEventTriggerDefinitions()) {
                endEventImpl.addEventTrigger(messageEventTrigger);
            }
        }
        for (ActivityDefinition activity : flowElementContainer.getActivities()) {
            for (BoundaryEventDefinition boundaryEvent : activity.getBoundaryEventDefinitions()) {
                boundaryEventImpl = (BoundaryEventDefinitionImpl) boundaryEvent;
                for (MessageEventTriggerDefinition messageEventTrigger : boundaryEvent.getMessageEventTriggerDefinitions()) {
                    boundaryEventImpl.addEventTrigger(messageEventTrigger);
                }
                for (ErrorEventTriggerDefinition errorEventTrigger : boundaryEvent.getErrorEventTriggerDefinitions()) {
                    boundaryEventImpl.addEventTrigger(errorEventTrigger);
                }
                for (SignalEventTriggerDefinition signalEventTrigger : boundaryEventImpl.getSignalEventTriggerDefinitions()) {
                    boundaryEventImpl.addEventTrigger(signalEventTrigger);
                }
                for (TimerEventTriggerDefinition timerEventTrigger : boundaryEvent.getTimerEventTriggerDefinitions()) {
                    boundaryEventImpl.addEventTrigger(timerEventTrigger);
                }
            }
            if (activity.getClass() == SubProcessDefinitionImpl.class) {
                subProcess = (SubProcessDefinitionImpl) activity;
                updateEventTriggersOnProcess(subProcess.getSubProcessContainer());
            }
        }
        for (StartEventDefinition startEvent : flowElementContainer.getStartEvents()) {
            startEventImpl = (StartEventDefinitionImpl) startEvent;
            for (MessageEventTriggerDefinition messageEventTrigger : startEvent.getMessageEventTriggerDefinitions()) {
                startEventImpl.addEventTrigger(messageEventTrigger);
            }
            for (ErrorEventTriggerDefinition errorEventTrigger : startEvent.getErrorEventTriggerDefinitions()) {
                startEventImpl.addEventTrigger(errorEventTrigger);
            }
            for (SignalEventTriggerDefinition signalEventTrigger : startEvent.getSignalEventTriggerDefinitions()) {
                startEventImpl.addEventTrigger(signalEventTrigger);
            }
            for (TimerEventTriggerDefinition timerEventTrigger : startEvent.getTimerEventTriggerDefinitions()) {
                startEventImpl.addEventTrigger(timerEventTrigger);
            }
        }
    }

    protected String generateInfosFromDefinition(final DesignProcessDefinition processDefinition) {
        final FlowElementContainerDefinition processContainer = processDefinition.getFlowElementContainer();
        return new StringBuilder("key1:").append(processDefinition.getActorsList().size()).append(",key2:").append(processContainer.getTransitions().size())
                .append(",key3:").append(processContainer.getActivities().size()).toString();
    }

    protected String getProcessInfos(final String infos) {
        return Base64.encodeBase64String(DigestUtils.md5(infos)).trim();
    }

    @Override
    public String getName() {
        return "Process design";
    }

}
