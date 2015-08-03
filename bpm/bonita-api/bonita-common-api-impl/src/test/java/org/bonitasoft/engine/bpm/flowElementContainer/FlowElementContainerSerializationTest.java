/*******************************************************************************
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
 ******************************************************************************/

package org.bonitasoft.engine.bpm.flowElementContainer;

import org.bonitasoft.engine.bpm.businessdata.BusinessDataDefinition;
import org.bonitasoft.engine.bpm.businessdata.impl.BusinessDataDefinitionImpl;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinition;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.impl.ConnectorDefinitionImpl;
import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.data.impl.DataDefinitionImpl;
import org.bonitasoft.engine.bpm.data.impl.TextDataDefinitionImpl;
import org.bonitasoft.engine.bpm.data.impl.XMLDataDefinitionImpl;
import org.bonitasoft.engine.bpm.document.DocumentDefinition;
import org.bonitasoft.engine.bpm.document.DocumentListDefinition;
import org.bonitasoft.engine.bpm.document.impl.DocumentDefinitionImpl;
import org.bonitasoft.engine.bpm.document.impl.DocumentListDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.EndEventDefinition;
import org.bonitasoft.engine.bpm.flownode.GatewayDefinition;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.flownode.IntermediateCatchEventDefinition;
import org.bonitasoft.engine.bpm.flownode.IntermediateThrowEventDefinition;
import org.bonitasoft.engine.bpm.flownode.SendTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ActivityDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.AutomaticTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.BoundaryEventDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.CallActivityDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.EndEventDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.GatewayDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.IntermediateCatchEventDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.IntermediateThrowEventDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ManualTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ReceiveTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.SendTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.StartEventDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.TransitionDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.UserTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.process.impl.internal.SubProcessDefinitionImpl;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author mazourd
 *         NB these tests will fail to run if the FlowElementContainerClass is missing an @XmlRootElement annotation.
 *         You also need to add the entire @XmlAlso field of ProcessDesignDefinitionImpl to the top of the FlowElementContainerDefinitionImpl class
 */
public class FlowElementContainerSerializationTest {

    //@Test
    public void simpleGenerationTestParameters() throws JAXBException {
        FlowElementContainerDefinitionImpl flowElementContainerDefinition = new FlowElementContainerDefinitionImpl();
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImpl.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        System.out.println(result2);
    }

    //@Test
    public void simpleImportExportTest() throws JAXBException {
        FlowElementContainerDefinitionImpl flowElementContainerDefinition = new FlowElementContainerDefinitionImpl();
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImpl.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(flowElementContainerDefinition).isEqualTo(resultProcessDef);
        System.out.print(result2);
    }

    //@Test
    public void ImportExportTest() throws JAXBException {
        FlowElementContainerDefinitionImpl flowElementContainerDefinition = new FlowElementContainerDefinitionImpl();
        TransitionDefinitionImpl transitionDefinition1 = new TransitionDefinitionImpl("name 1");
        TransitionDefinitionImpl transitionDefinition2 = new TransitionDefinitionImpl("name 2");
        flowElementContainerDefinition.addTransition(transitionDefinition1);
        flowElementContainerDefinition.addTransition(transitionDefinition2);
        ActivityDefinitionImpl activityDefinition1 = new AutomaticTaskDefinitionImpl(17, "abba");
        ActivityDefinitionImpl activityDefinition2 = new CallActivityDefinitionImpl(18, "mt");
        ActivityDefinitionImpl activityDefinition3 = new ManualTaskDefinitionImpl("tralala", "lulu");
        ActivityDefinitionImpl activityDefinition4 = new ReceiveTaskDefinitionImpl("trololololo", "drululu");
        ActivityDefinitionImpl activityDefinition5 = new SendTaskDefinitionImpl("trololololo", "drululu", new ExpressionImpl());
        ActivityDefinitionImpl activityDefinition6 = new UserTaskDefinitionImpl(45, "dummyname", "dummy actor name");
        flowElementContainerDefinition.addActivity(activityDefinition1);
        flowElementContainerDefinition.addActivity(activityDefinition2);
        flowElementContainerDefinition.addActivity(activityDefinition3);
        flowElementContainerDefinition.addActivity(activityDefinition4);
        flowElementContainerDefinition.addActivity(activityDefinition5);
        flowElementContainerDefinition.addActivity(activityDefinition6);
        GatewayDefinitionImpl gateway1 = new GatewayDefinitionImpl();
        GatewayDefinition gateway2 = new GatewayDefinitionImpl("dummy gateway type", GatewayType.PARALLEL);
        flowElementContainerDefinition.addGateway(gateway1);
        flowElementContainerDefinition.addGateway(gateway2);
        StartEventDefinitionImpl startEventDefinition1 = new StartEventDefinitionImpl();
        StartEventDefinitionImpl startEventDefinition2 = new StartEventDefinitionImpl("Dummy start event");
        flowElementContainerDefinition.addStartEvent(startEventDefinition1);
        flowElementContainerDefinition.addStartEvent(startEventDefinition2);
        IntermediateCatchEventDefinition intermediateCatchEventDefinition1 = new IntermediateCatchEventDefinitionImpl();
        IntermediateCatchEventDefinition intermediateCatchEventDefinition2 = new IntermediateCatchEventDefinitionImpl("Catch1");
        IntermediateThrowEventDefinition intermediateThrowEventDefinition1 = new IntermediateThrowEventDefinitionImpl();
        IntermediateThrowEventDefinition intermediateThrowEventDefinition2 = new IntermediateThrowEventDefinitionImpl("Throw1");
        EndEventDefinition endEventDefinition1 = new EndEventDefinitionImpl();
        EndEventDefinition endEventDefinition2 = new EndEventDefinitionImpl("End1");
        flowElementContainerDefinition.addIntermediateCatchEvent(intermediateCatchEventDefinition1);
        flowElementContainerDefinition.addIntermediateCatchEvent(intermediateCatchEventDefinition2);
        flowElementContainerDefinition.addIntermediateThrowEvent(intermediateThrowEventDefinition1);
        flowElementContainerDefinition.addIntermediateThrowEvent(intermediateThrowEventDefinition2);
        flowElementContainerDefinition.addEndEvent(endEventDefinition1);
        flowElementContainerDefinition.addEndEvent(endEventDefinition2);
        DataDefinition dataDefinition1 = new DataDefinitionImpl();
        DataDefinition dataDefinition2 = new DataDefinitionImpl("Name datadef1", new ExpressionImpl(45648));
        DataDefinition dataDefinition3 = new TextDataDefinitionImpl();
        DataDefinition dataDefinition4 = new TextDataDefinitionImpl("Text data name 1", new ExpressionImpl(787997));
        DataDefinition dataDefinition5 = new XMLDataDefinitionImpl();
        DataDefinition dataDefinition6 = new XMLDataDefinitionImpl("Xml data name", new ExpressionImpl());
        BusinessDataDefinition businessDataDefinition1 = new BusinessDataDefinitionImpl();
        BusinessDataDefinition businessDataDefinition2 = new BusinessDataDefinitionImpl("BusinessData name 1", new ExpressionImpl(45646878));
        DocumentDefinition documentDefinition1 = new DocumentDefinitionImpl();
        DocumentDefinition documentDefinition2 = new DocumentDefinitionImpl("documentDefinition name");
        ConnectorDefinition connectorDefinition1 = new ConnectorDefinitionImpl();
        ConnectorDefinition connectorDefinition2 = new ConnectorDefinitionImpl("Connector name", "468787540450453958", "0.1", ConnectorEvent.ON_FINISH);
        DocumentListDefinition documentListDefinition1 = new DocumentListDefinitionImpl();
        DocumentListDefinition documentListDefinition2 = new DocumentListDefinitionImpl("Document list name");
        flowElementContainerDefinition.addDataDefinition(dataDefinition1);
        flowElementContainerDefinition.addDataDefinition(dataDefinition2);
        flowElementContainerDefinition.addDataDefinition(dataDefinition3);
        flowElementContainerDefinition.addDataDefinition(dataDefinition4);
        flowElementContainerDefinition.addDataDefinition(dataDefinition5);
        flowElementContainerDefinition.addDataDefinition(dataDefinition6);
        flowElementContainerDefinition.addBusinessDataDefinition(businessDataDefinition1);
        flowElementContainerDefinition.addBusinessDataDefinition(businessDataDefinition2);
        flowElementContainerDefinition.addDocumentDefinition(documentDefinition1);
        flowElementContainerDefinition.addDocumentDefinition(documentDefinition2);
        flowElementContainerDefinition.addDocumentListDefinition(documentListDefinition1);
        flowElementContainerDefinition.addDocumentListDefinition(documentListDefinition2);
        flowElementContainerDefinition.addConnector(connectorDefinition1);
        flowElementContainerDefinition.addConnector(connectorDefinition2);
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImpl.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(flowElementContainerDefinition).isEqualTo(resultProcessDef);
        System.out.print(result2);
    }

    //Needs a setter for the flow nodes container , use ImportExportTestWithNaturalFlownodeMapping if it is absent
    //@Test
    public void ImportExportTestWithFlownodeMapping() throws JAXBException {
        FlowElementContainerDefinitionImpl flowElementContainerDefinition = new FlowElementContainerDefinitionImpl();
        TransitionDefinitionImpl transitionDefinition1 = new TransitionDefinitionImpl("name 1");
        TransitionDefinitionImpl transitionDefinition2 = new TransitionDefinitionImpl("name 2");
        flowElementContainerDefinition.addTransition(transitionDefinition1);
        flowElementContainerDefinition.addTransition(transitionDefinition2);
        ActivityDefinitionImpl activityDefinition1 = new AutomaticTaskDefinitionImpl(17, "abba");
        ActivityDefinitionImpl activityDefinition2 = new CallActivityDefinitionImpl(18, "mt");
        ActivityDefinitionImpl activityDefinition3 = new ManualTaskDefinitionImpl("tralala", "lulu");
        ActivityDefinitionImpl activityDefinition4 = new ReceiveTaskDefinitionImpl("trololololo", "drululu");
        ActivityDefinitionImpl activityDefinition5 = new SendTaskDefinitionImpl("trololololo", "drululu", new ExpressionImpl());
        ActivityDefinitionImpl activityDefinition6 = new UserTaskDefinitionImpl(45, "dummyname", "dummy actor name");
        flowElementContainerDefinition.addActivity(activityDefinition1);
        flowElementContainerDefinition.addActivity(activityDefinition2);
        flowElementContainerDefinition.addActivity(activityDefinition3);
        flowElementContainerDefinition.addActivity(activityDefinition4);
        flowElementContainerDefinition.addActivity(activityDefinition5);
        flowElementContainerDefinition.addActivity(activityDefinition6);
        GatewayDefinitionImpl gateway1 = new GatewayDefinitionImpl();
        GatewayDefinition gateway2 = new GatewayDefinitionImpl("dummy gateway type", GatewayType.PARALLEL);
        flowElementContainerDefinition.addGateway(gateway1);
        flowElementContainerDefinition.addGateway(gateway2);
        StartEventDefinitionImpl startEventDefinition1 = new StartEventDefinitionImpl();
        StartEventDefinitionImpl startEventDefinition2 = new StartEventDefinitionImpl("Dummy start event");
        flowElementContainerDefinition.addStartEvent(startEventDefinition1);
        flowElementContainerDefinition.addStartEvent(startEventDefinition2);
        IntermediateCatchEventDefinition intermediateCatchEventDefinition1 = new IntermediateCatchEventDefinitionImpl();
        IntermediateCatchEventDefinition intermediateCatchEventDefinition2 = new IntermediateCatchEventDefinitionImpl("Catch1");
        IntermediateThrowEventDefinition intermediateThrowEventDefinition1 = new IntermediateThrowEventDefinitionImpl();
        IntermediateThrowEventDefinition intermediateThrowEventDefinition2 = new IntermediateThrowEventDefinitionImpl("Throw1");
        EndEventDefinition endEventDefinition1 = new EndEventDefinitionImpl();
        EndEventDefinition endEventDefinition2 = new EndEventDefinitionImpl("End1");
        flowElementContainerDefinition.addIntermediateCatchEvent(intermediateCatchEventDefinition1);
        flowElementContainerDefinition.addIntermediateCatchEvent(intermediateCatchEventDefinition2);
        flowElementContainerDefinition.addIntermediateThrowEvent(intermediateThrowEventDefinition1);
        flowElementContainerDefinition.addIntermediateThrowEvent(intermediateThrowEventDefinition2);
        flowElementContainerDefinition.addEndEvent(endEventDefinition1);
        flowElementContainerDefinition.addEndEvent(endEventDefinition2);
        DataDefinition dataDefinition1 = new DataDefinitionImpl();
        DataDefinition dataDefinition2 = new DataDefinitionImpl("Name datadef1", new ExpressionImpl(45648));
        DataDefinition dataDefinition3 = new TextDataDefinitionImpl();
        DataDefinition dataDefinition4 = new TextDataDefinitionImpl("Text data name 1", new ExpressionImpl(787997));
        DataDefinition dataDefinition5 = new XMLDataDefinitionImpl();
        DataDefinition dataDefinition6 = new XMLDataDefinitionImpl("Xml data name", new ExpressionImpl());
        BusinessDataDefinition businessDataDefinition1 = new BusinessDataDefinitionImpl();
        BusinessDataDefinition businessDataDefinition2 = new BusinessDataDefinitionImpl("BusinessData name 1", new ExpressionImpl(45646878));
        DocumentDefinition documentDefinition1 = new DocumentDefinitionImpl();
        DocumentDefinition documentDefinition2 = new DocumentDefinitionImpl("documentDefinition name");
        ConnectorDefinition connectorDefinition1 = new ConnectorDefinitionImpl();
        ConnectorDefinition connectorDefinition2 = new ConnectorDefinitionImpl("Connector name", "468787540450453958", "0.1", ConnectorEvent.ON_FINISH);
        DocumentListDefinition documentListDefinition1 = new DocumentListDefinitionImpl();
        DocumentListDefinition documentListDefinition2 = new DocumentListDefinitionImpl("Document list name");
        EndEventDefinitionImpl endEventDefinitionImpl1 = new EndEventDefinitionImpl();
        EndEventDefinitionImpl endEventDefinitionImpl2 = new EndEventDefinitionImpl("Event def map 1");
        StartEventDefinitionImpl startEventDefinitionImpl1 = new StartEventDefinitionImpl();
        StartEventDefinitionImpl startEventDefinitionImpl2 = new StartEventDefinitionImpl("Start event 1");
        IntermediateCatchEventDefinitionImpl intermediateCatchEventDefinition11 = new IntermediateCatchEventDefinitionImpl();
        IntermediateCatchEventDefinitionImpl intermediateCatchEventDefinition12 = new IntermediateCatchEventDefinitionImpl("Catch event 12 name");
        IntermediateThrowEventDefinitionImpl intermediateThrowEventDefinition11 = new IntermediateThrowEventDefinitionImpl();
        IntermediateThrowEventDefinitionImpl intermediateThrowEventDefinition12 = new IntermediateThrowEventDefinitionImpl("Throw event 12 name");
        StartEventDefinitionImpl startEventDefinition11 = new StartEventDefinitionImpl();
        StartEventDefinitionImpl startEventDefinition12 = new StartEventDefinitionImpl("Start event name 12");
        AutomaticTaskDefinitionImpl automaticTaskDefinition1 = new AutomaticTaskDefinitionImpl("Auto Task 1");
        AutomaticTaskDefinitionImpl automaticTaskDefinition2 = new AutomaticTaskDefinitionImpl();
        CallActivityDefinitionImpl callActivityDefinition1 = new CallActivityDefinitionImpl();
        CallActivityDefinitionImpl callActivityDefinition2 = new CallActivityDefinitionImpl("Call Activity name 2");
        ManualTaskDefinitionImpl manualTaskDefinition1 = new ManualTaskDefinitionImpl();
        ManualTaskDefinitionImpl manualTaskDefinition2 = new ManualTaskDefinitionImpl("Manual task name", "actor name");
        ReceiveTaskDefinitionImpl receiveTaskDefinition1 = new ReceiveTaskDefinitionImpl();
        ReceiveTaskDefinitionImpl receiveTaskDefinition2 = new ReceiveTaskDefinitionImpl("String1", "String2");
        SendTaskDefinitionImpl sendTaskDefinition1 = new SendTaskDefinitionImpl();
        SendTaskDefinition sendTaskDefinition2 = new SendTaskDefinitionImpl("String1", "String2", new ExpressionImpl());
        UserTaskDefinitionImpl userTaskDefinition1 = new UserTaskDefinitionImpl();
        UserTaskDefinitionImpl userTaskDefinition2 = new UserTaskDefinitionImpl(654631124, "name1", "name2");
        BoundaryEventDefinitionImpl boundaryEventDefinition1 = new BoundaryEventDefinitionImpl();
        BoundaryEventDefinitionImpl boundaryEventDefinition2 = new BoundaryEventDefinitionImpl("Boundary name");
        GatewayDefinitionImpl gatewayDefinition1 = new GatewayDefinitionImpl();
        GatewayDefinition gatewayDefinition2 = new GatewayDefinitionImpl("Gateway name", GatewayType.INCLUSIVE);
        SubProcessDefinitionImpl subProcessDefinition1 = new SubProcessDefinitionImpl();
        SubProcessDefinitionImpl subProcessDefinition2 = new SubProcessDefinitionImpl("Subprocess name", true);
        /*
         * flowElementContainerDefinition.addFlowNodes(endEventDefinitionImpl1);
         * flowElementContainerDefinition.addFlowNodes(endEventDefinitionImpl2);
         * flowElementContainerDefinition.addFlowNodes(startEventDefinitionImpl1);
         * flowElementContainerDefinition.addFlowNodes(startEventDefinitionImpl2);
         * flowElementContainerDefinition.addFlowNodes(intermediateCatchEventDefinition11);
         * flowElementContainerDefinition.addFlowNodes(intermediateCatchEventDefinition12);
         * flowElementContainerDefinition.addFlowNodes(intermediateThrowEventDefinition11);
         * flowElementContainerDefinition.addFlowNodes(intermediateThrowEventDefinition12);
         * flowElementContainerDefinition.addFlowNodes(startEventDefinition11);
         * flowElementContainerDefinition.addFlowNodes(startEventDefinition12);
         * flowElementContainerDefinition.addFlowNodes(automaticTaskDefinition1);
         * flowElementContainerDefinition.addFlowNodes(automaticTaskDefinition2);
         * flowElementContainerDefinition.addFlowNodes(callActivityDefinition1);
         * flowElementContainerDefinition.addFlowNodes(callActivityDefinition2);
         * flowElementContainerDefinition.addFlowNodes(manualTaskDefinition1);
         * flowElementContainerDefinition.addFlowNodes( manualTaskDefinition2 );
         * flowElementContainerDefinition.addFlowNodes(receiveTaskDefinition1);
         * flowElementContainerDefinition.addFlowNodes(receiveTaskDefinition2);
         * flowElementContainerDefinition.addFlowNodes(sendTaskDefinition1);
         * flowElementContainerDefinition.addFlowNodes(sendTaskDefinition2);
         * flowElementContainerDefinition.addFlowNodes(userTaskDefinition1);
         * flowElementContainerDefinition.addFlowNodes(userTaskDefinition2);
         * flowElementContainerDefinition.addFlowNodes( boundaryEventDefinition1 );
         * flowElementContainerDefinition.addFlowNodes( boundaryEventDefinition2 );
         * flowElementContainerDefinition.addFlowNodes(gatewayDefinition1);
         * flowElementContainerDefinition.addFlowNodes(gatewayDefinition2);
         * flowElementContainerDefinition.addFlowNodes(subProcessDefinition1);
         * flowElementContainerDefinition.addFlowNodes(subProcessDefinition2);
         */
        flowElementContainerDefinition.addDataDefinition(dataDefinition1);
        flowElementContainerDefinition.addDataDefinition(dataDefinition2);
        flowElementContainerDefinition.addDataDefinition(dataDefinition3);
        flowElementContainerDefinition.addDataDefinition(dataDefinition4);
        flowElementContainerDefinition.addDataDefinition(dataDefinition5);
        flowElementContainerDefinition.addDataDefinition(dataDefinition6);
        flowElementContainerDefinition.addBusinessDataDefinition(businessDataDefinition1);
        flowElementContainerDefinition.addBusinessDataDefinition(businessDataDefinition2);
        flowElementContainerDefinition.addDocumentDefinition(documentDefinition1);
        flowElementContainerDefinition.addDocumentDefinition(documentDefinition2);
        flowElementContainerDefinition.addDocumentListDefinition(documentListDefinition1);
        flowElementContainerDefinition.addDocumentListDefinition(documentListDefinition2);
        flowElementContainerDefinition.addConnector(connectorDefinition1);
        flowElementContainerDefinition.addConnector(connectorDefinition2);
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImpl.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(flowElementContainerDefinition).isEqualTo(resultProcessDef);
        System.out.print(result2);
    }

    //requires the getflownodes() method to be public
    //@Test
    public void ImportExportTestWithNaturalFlownodeMapping() throws JAXBException {
        FlowElementContainerDefinitionImpl flowElementContainerDefinition = new FlowElementContainerDefinitionImpl();
        TransitionDefinitionImpl transitionDefinition1 = new TransitionDefinitionImpl("name 1");
        TransitionDefinitionImpl transitionDefinition2 = new TransitionDefinitionImpl("name 2");
        flowElementContainerDefinition.addTransition(transitionDefinition1);
        flowElementContainerDefinition.addTransition(transitionDefinition2);
        ActivityDefinitionImpl activityDefinition1 = new AutomaticTaskDefinitionImpl(17, "abba");
        ActivityDefinitionImpl activityDefinition2 = new CallActivityDefinitionImpl(18, "mt");
        ActivityDefinitionImpl activityDefinition3 = new ManualTaskDefinitionImpl("tralala", "lulu");
        ActivityDefinitionImpl activityDefinition4 = new ReceiveTaskDefinitionImpl("trololololo", "drululu");
        ActivityDefinitionImpl activityDefinition5 = new SendTaskDefinitionImpl("trololololo", "drululu", new ExpressionImpl());
        ActivityDefinitionImpl activityDefinition6 = new UserTaskDefinitionImpl(45, "dummyname", "dummy actor name");
        flowElementContainerDefinition.addActivity(activityDefinition1);
        flowElementContainerDefinition.addActivity(activityDefinition2);
        flowElementContainerDefinition.addActivity(activityDefinition3);
        flowElementContainerDefinition.addActivity(activityDefinition4);
        flowElementContainerDefinition.addActivity(activityDefinition5);
        flowElementContainerDefinition.addActivity(activityDefinition6);
        GatewayDefinitionImpl gateway1 = new GatewayDefinitionImpl();
        GatewayDefinition gateway2 = new GatewayDefinitionImpl("dummy gateway type", GatewayType.PARALLEL);
        flowElementContainerDefinition.addGateway(gateway1);
        flowElementContainerDefinition.addGateway(gateway2);
        StartEventDefinitionImpl startEventDefinition1 = new StartEventDefinitionImpl();
        StartEventDefinitionImpl startEventDefinition2 = new StartEventDefinitionImpl("Dummy start event");
        flowElementContainerDefinition.addStartEvent(startEventDefinition1);
        flowElementContainerDefinition.addStartEvent(startEventDefinition2);
        IntermediateCatchEventDefinition intermediateCatchEventDefinition1 = new IntermediateCatchEventDefinitionImpl();
        IntermediateCatchEventDefinition intermediateCatchEventDefinition2 = new IntermediateCatchEventDefinitionImpl("Catch1");
        IntermediateThrowEventDefinition intermediateThrowEventDefinition1 = new IntermediateThrowEventDefinitionImpl();
        IntermediateThrowEventDefinition intermediateThrowEventDefinition2 = new IntermediateThrowEventDefinitionImpl("Throw1");
        EndEventDefinition endEventDefinition1 = new EndEventDefinitionImpl();
        EndEventDefinition endEventDefinition2 = new EndEventDefinitionImpl("End1");
        flowElementContainerDefinition.addIntermediateCatchEvent(intermediateCatchEventDefinition1);
        flowElementContainerDefinition.addIntermediateCatchEvent(intermediateCatchEventDefinition2);
        flowElementContainerDefinition.addIntermediateThrowEvent(intermediateThrowEventDefinition1);
        flowElementContainerDefinition.addIntermediateThrowEvent(intermediateThrowEventDefinition2);
        flowElementContainerDefinition.addEndEvent(endEventDefinition1);
        flowElementContainerDefinition.addEndEvent(endEventDefinition2);
        DataDefinition dataDefinition1 = new DataDefinitionImpl();
        DataDefinition dataDefinition2 = new DataDefinitionImpl("Name datadef1", new ExpressionImpl(45648));
        DataDefinition dataDefinition3 = new TextDataDefinitionImpl();
        DataDefinition dataDefinition4 = new TextDataDefinitionImpl("Text data name 1", new ExpressionImpl(787997));
        DataDefinition dataDefinition5 = new XMLDataDefinitionImpl();
        DataDefinition dataDefinition6 = new XMLDataDefinitionImpl("Xml data name", new ExpressionImpl());
        BusinessDataDefinition businessDataDefinition1 = new BusinessDataDefinitionImpl();
        BusinessDataDefinition businessDataDefinition2 = new BusinessDataDefinitionImpl("BusinessData name 1", new ExpressionImpl(45646878));
        DocumentDefinition documentDefinition1 = new DocumentDefinitionImpl();
        DocumentDefinition documentDefinition2 = new DocumentDefinitionImpl("documentDefinition name");
        ConnectorDefinition connectorDefinition1 = new ConnectorDefinitionImpl();
        ConnectorDefinition connectorDefinition2 = new ConnectorDefinitionImpl("Connector name", "468787540450453958", "0.1", ConnectorEvent.ON_FINISH);
        DocumentListDefinition documentListDefinition1 = new DocumentListDefinitionImpl();
        DocumentListDefinition documentListDefinition2 = new DocumentListDefinitionImpl("Document list name");
        flowElementContainerDefinition.addDataDefinition(dataDefinition1);
        flowElementContainerDefinition.addDataDefinition(dataDefinition2);
        flowElementContainerDefinition.addDataDefinition(dataDefinition3);
        flowElementContainerDefinition.addDataDefinition(dataDefinition4);
        flowElementContainerDefinition.addDataDefinition(dataDefinition5);
        flowElementContainerDefinition.addDataDefinition(dataDefinition6);
        flowElementContainerDefinition.addBusinessDataDefinition(businessDataDefinition1);
        flowElementContainerDefinition.addBusinessDataDefinition(businessDataDefinition2);
        flowElementContainerDefinition.addDocumentDefinition(documentDefinition1);
        flowElementContainerDefinition.addDocumentDefinition(documentDefinition2);
        flowElementContainerDefinition.addDocumentListDefinition(documentListDefinition1);
        flowElementContainerDefinition.addDocumentListDefinition(documentListDefinition2);
        flowElementContainerDefinition.addConnector(connectorDefinition1);
        flowElementContainerDefinition.addConnector(connectorDefinition2);
        //Set<FlowNodeDefinition> Set=  flowElementContainerDefinition.getFlowNodes();
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImpl.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(flowElementContainerDefinition).isEqualTo(resultProcessDef);
        System.out.print(result2);
    }
}
