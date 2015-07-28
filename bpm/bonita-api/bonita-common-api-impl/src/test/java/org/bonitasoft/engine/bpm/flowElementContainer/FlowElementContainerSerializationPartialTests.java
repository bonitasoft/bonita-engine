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
import org.bonitasoft.engine.bpm.flownode.StartEventDefinition;
import org.bonitasoft.engine.bpm.flownode.TransitionDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ActivityDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.AutomaticTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.CallActivityDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.EndEventDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.GatewayDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.IntermediateCatchEventDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.IntermediateThrowEventDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ManualTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ReceiveTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.SendTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.StartEventDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.TransitionDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.UserTaskDefinitionImpl;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author mazourd
 */
public class FlowElementContainerSerializationPartialTests {

    @Test
    public void simpleGenerationTestActivities1() throws JAXBException {
        FlowElementContainerDefinitionImplUptoActivitiesTestClass flowElementContainerDefinition = new FlowElementContainerDefinitionImplUptoActivitiesTestClass();
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImplUptoActivitiesTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        System.out.println(result2);
    }

    @Test
    public void simpleGenerationTestActivities2() throws JAXBException {
        FlowElementContainerDefinitionImplUptoActivitiesTestClass flowElementContainerDefinition = new FlowElementContainerDefinitionImplUptoActivitiesTestClass();
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
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImplUptoActivitiesTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        System.out.println(result2);
    }

    @Test
    public void simpleImportExportTestActivities() throws JAXBException {
        FlowElementContainerDefinitionImplUptoActivitiesTestClass flowElementContainerDefinition = new FlowElementContainerDefinitionImplUptoActivitiesTestClass();
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImplUptoActivitiesTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(flowElementContainerDefinition).isEqualTo(resultProcessDef);
        System.out.print(result2);
    }

    @Test
    public void ImportExportTestActivities() throws JAXBException {
        FlowElementContainerDefinitionImplUptoActivitiesTestClass flowElementContainerDefinition = new FlowElementContainerDefinitionImplUptoActivitiesTestClass();
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
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImplUptoActivitiesTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(flowElementContainerDefinition).isEqualTo(resultProcessDef);
        System.out.print(result2);
    }

    @Test
    public void simpleGenerationTestTransitions() throws JAXBException {
        FlowElementContainerDefinitionImplUptoTransitionsTestClass flowElementContainerDefinition = new FlowElementContainerDefinitionImplUptoTransitionsTestClass();
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImplUptoTransitionsTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        System.out.println(result2);
    }

    @Test
    public void GenerationTestTransitions() throws JAXBException {
        Set<TransitionDefinition> transitions = new LinkedHashSet<>();
        TransitionDefinitionImpl transitionDefinition1 = new TransitionDefinitionImpl("name 1");
        TransitionDefinitionImpl transitionDefinition2 = new TransitionDefinitionImpl("name 2");
        transitions.add(transitionDefinition1);
        transitions.add(transitionDefinition2);
        FlowElementContainerDefinitionImplUptoTransitionsTestClass flowElementContainerDefinition = new FlowElementContainerDefinitionImplUptoTransitionsTestClass();
        flowElementContainerDefinition.setTransitions(transitions);
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
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImplUptoTransitionsTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        System.out.println(result2);
    }

    @Test
    public void simpleImportExportTestTransitions() throws JAXBException {
        FlowElementContainerDefinitionImplUptoTransitionsTestClass flowElementContainerDefinition = new FlowElementContainerDefinitionImplUptoTransitionsTestClass();
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImplUptoTransitionsTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(flowElementContainerDefinition).isEqualTo(resultProcessDef);
        System.out.print(result2);
    }

    @Test
    public void ImportExportTestTransitions() throws JAXBException {
        FlowElementContainerDefinitionImplUptoTransitionsTestClass flowElementContainerDefinition = new FlowElementContainerDefinitionImplUptoTransitionsTestClass();
        Set<TransitionDefinition> transitions = new LinkedHashSet<>();
        TransitionDefinitionImpl transitionDefinition1 = new TransitionDefinitionImpl("name 1");
        TransitionDefinitionImpl transitionDefinition2 = new TransitionDefinitionImpl("name 2");
        transitions.add(transitionDefinition1);
        transitions.add(transitionDefinition2);
        flowElementContainerDefinition.setTransitions(transitions);
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
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImplUptoTransitionsTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(flowElementContainerDefinition).isEqualTo(resultProcessDef);
        System.out.print(result2);
    }

    @Test
    public void simpleGenerationTestGateways() throws JAXBException {
        FlowElementContainerDefinitionImplUptoGatewaysTestClass flowElementContainerDefinition = new FlowElementContainerDefinitionImplUptoGatewaysTestClass();
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImplUptoGatewaysTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        System.out.println(result2);
    }

    @Test
    public void GenerationTestGateways() throws JAXBException {
        Set<TransitionDefinition> transitions = new LinkedHashSet<>();
        TransitionDefinitionImpl transitionDefinition1 = new TransitionDefinitionImpl("name 1");
        TransitionDefinitionImpl transitionDefinition2 = new TransitionDefinitionImpl("name 2");
        transitions.add(transitionDefinition1);
        transitions.add(transitionDefinition2);
        FlowElementContainerDefinitionImplUptoGatewaysTestClass flowElementContainerDefinition = new FlowElementContainerDefinitionImplUptoGatewaysTestClass();
        flowElementContainerDefinition.setTransitions(transitions);
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
        List<GatewayDefinition> gatewayDefinitionList = new ArrayList<>();
        GatewayDefinitionImpl gateway1 = new GatewayDefinitionImpl();
        GatewayDefinition gateway2 = new GatewayDefinitionImpl("dummy gateway type", GatewayType.PARALLEL);
        gatewayDefinitionList.add(gateway1);
        gatewayDefinitionList.add(gateway2);
        flowElementContainerDefinition.setGateways(gatewayDefinitionList);
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImplUptoGatewaysTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        System.out.println(result2);
    }

    @Test
    public void simpleImportExportTestGateways() throws JAXBException {
        FlowElementContainerDefinitionImplUptoGatewaysTestClass flowElementContainerDefinition = new FlowElementContainerDefinitionImplUptoGatewaysTestClass();
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImplUptoGatewaysTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(flowElementContainerDefinition).isEqualTo(resultProcessDef);
        System.out.print(result2);
    }

    @Test
    public void ImportExportTestGateways() throws JAXBException {
        FlowElementContainerDefinitionImplUptoGatewaysTestClass flowElementContainerDefinition = new FlowElementContainerDefinitionImplUptoGatewaysTestClass();
        Set<TransitionDefinition> transitions = new LinkedHashSet<>();
        TransitionDefinitionImpl transitionDefinition1 = new TransitionDefinitionImpl("name 1");
        TransitionDefinitionImpl transitionDefinition2 = new TransitionDefinitionImpl("name 2");
        transitions.add(transitionDefinition1);
        transitions.add(transitionDefinition2);
        flowElementContainerDefinition.setTransitions(transitions);
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
        List<GatewayDefinition> gatewayDefinitionList = new ArrayList<>();
        GatewayDefinitionImpl gateway1 = new GatewayDefinitionImpl();
        GatewayDefinition gateway2 = new GatewayDefinitionImpl("dummy gateway type", GatewayType.PARALLEL);
        gatewayDefinitionList.add(gateway1);
        gatewayDefinitionList.add(gateway2);
        flowElementContainerDefinition.setGateways(gatewayDefinitionList);
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImplUptoGatewaysTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(flowElementContainerDefinition).isEqualTo(resultProcessDef);
        System.out.print(result2);
    }

    @Test
    public void simpleGenerationTestStartEvents() throws JAXBException {
        FlowElementContainerDefinitionImplUptoStartEventsTestClass flowElementContainerDefinition = new FlowElementContainerDefinitionImplUptoStartEventsTestClass();
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImplUptoStartEventsTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        System.out.println(result2);
    }

    @Test
    public void GenerationTestStartEvents() throws JAXBException {
        Set<TransitionDefinition> transitions = new LinkedHashSet<>();
        TransitionDefinitionImpl transitionDefinition1 = new TransitionDefinitionImpl("name 1");
        TransitionDefinitionImpl transitionDefinition2 = new TransitionDefinitionImpl("name 2");
        transitions.add(transitionDefinition1);
        transitions.add(transitionDefinition2);
        FlowElementContainerDefinitionImplUptoStartEventsTestClass flowElementContainerDefinition = new FlowElementContainerDefinitionImplUptoStartEventsTestClass();
        flowElementContainerDefinition.setTransitions(transitions);
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
        List<GatewayDefinition> gatewayDefinitionList = new ArrayList<>();
        GatewayDefinitionImpl gateway1 = new GatewayDefinitionImpl();
        GatewayDefinition gateway2 = new GatewayDefinitionImpl("dummy gateway type", GatewayType.PARALLEL);
        gatewayDefinitionList.add(gateway1);
        gatewayDefinitionList.add(gateway2);
        flowElementContainerDefinition.setGateways(gatewayDefinitionList);
        List<StartEventDefinition> startEvents = new ArrayList<>();
        StartEventDefinitionImpl startEventDefinition1 = new StartEventDefinitionImpl();
        StartEventDefinitionImpl startEventDefinition2 = new StartEventDefinitionImpl("Dummy start event");
        startEvents.add(startEventDefinition1);
        startEvents.add(startEventDefinition2);
        flowElementContainerDefinition.setStartEvents(startEvents);
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImplUptoStartEventsTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        System.out.println(result2);
    }

    @Test
    public void simpleImportExportTestStartEvents() throws JAXBException {
        FlowElementContainerDefinitionImplUptoGatewaysTestClass flowElementContainerDefinition = new FlowElementContainerDefinitionImplUptoGatewaysTestClass();
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImplUptoGatewaysTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(flowElementContainerDefinition).isEqualTo(resultProcessDef);
        System.out.print(result2);
    }

    @Test
    public void ImportExportTestStartEvents() throws JAXBException {
        FlowElementContainerDefinitionImplUptoStartEventsTestClass flowElementContainerDefinition = new FlowElementContainerDefinitionImplUptoStartEventsTestClass();
        Set<TransitionDefinition> transitions = new LinkedHashSet<>();
        TransitionDefinitionImpl transitionDefinition1 = new TransitionDefinitionImpl("name 1");
        TransitionDefinitionImpl transitionDefinition2 = new TransitionDefinitionImpl("name 2");
        transitions.add(transitionDefinition1);
        transitions.add(transitionDefinition2);
        flowElementContainerDefinition.setTransitions(transitions);
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
        List<GatewayDefinition> gatewayDefinitionList = new ArrayList<>();
        GatewayDefinitionImpl gateway1 = new GatewayDefinitionImpl();
        GatewayDefinition gateway2 = new GatewayDefinitionImpl("dummy gateway type", GatewayType.PARALLEL);
        gatewayDefinitionList.add(gateway1);
        gatewayDefinitionList.add(gateway2);
        flowElementContainerDefinition.setGateways(gatewayDefinitionList);
        List<StartEventDefinition> startEvents = new ArrayList<>();
        StartEventDefinitionImpl startEventDefinition1 = new StartEventDefinitionImpl();
        StartEventDefinitionImpl startEventDefinition2 = new StartEventDefinitionImpl("Dummy start event");
        startEvents.add(startEventDefinition1);
        startEvents.add(startEventDefinition2);
        flowElementContainerDefinition.setStartEvents(startEvents);
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImplUptoStartEventsTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(flowElementContainerDefinition).isEqualTo(resultProcessDef);
        System.out.print(result2);
    }

    @Test
    public void simpleGenerationTestEventDef() throws JAXBException {
        FlowElementContainerDefinitionImplUptoEventDefTestClass flowElementContainerDefinition = new FlowElementContainerDefinitionImplUptoEventDefTestClass();
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImplUptoEventDefTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        System.out.println(result2);
    }

    @Test
    public void GenerationTestEventDef() throws JAXBException {
        Set<TransitionDefinition> transitions = new LinkedHashSet<>();
        TransitionDefinitionImpl transitionDefinition1 = new TransitionDefinitionImpl("name 1");
        TransitionDefinitionImpl transitionDefinition2 = new TransitionDefinitionImpl("name 2");
        transitions.add(transitionDefinition1);
        transitions.add(transitionDefinition2);
        FlowElementContainerDefinitionImplUptoEventDefTestClass flowElementContainerDefinition = new FlowElementContainerDefinitionImplUptoEventDefTestClass();
        flowElementContainerDefinition.setTransitions(transitions);
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
        List<GatewayDefinition> gatewayDefinitionList = new ArrayList<>();
        GatewayDefinitionImpl gateway1 = new GatewayDefinitionImpl();
        GatewayDefinition gateway2 = new GatewayDefinitionImpl("dummy gateway type", GatewayType.PARALLEL);
        gatewayDefinitionList.add(gateway1);
        gatewayDefinitionList.add(gateway2);
        flowElementContainerDefinition.setGateways(gatewayDefinitionList);
        List<StartEventDefinition> startEvents = new ArrayList<>();
        StartEventDefinitionImpl startEventDefinition1 = new StartEventDefinitionImpl();
        StartEventDefinitionImpl startEventDefinition2 = new StartEventDefinitionImpl("Dummy start event");
        startEvents.add(startEventDefinition1);
        startEvents.add(startEventDefinition2);
        flowElementContainerDefinition.setStartEvents(startEvents);
        List<IntermediateCatchEventDefinition> listCatchEvents = new ArrayList<>();
        IntermediateCatchEventDefinition intermediateCatchEventDefinition1 = new IntermediateCatchEventDefinitionImpl();
        IntermediateCatchEventDefinition intermediateCatchEventDefinition2 = new IntermediateCatchEventDefinitionImpl("Catch1");
        listCatchEvents.add(intermediateCatchEventDefinition1);
        listCatchEvents.add(intermediateCatchEventDefinition2);
        List<IntermediateThrowEventDefinition> listThrowEvent = new ArrayList<>();
        IntermediateThrowEventDefinition intermediateThrowEventDefinition1 = new IntermediateThrowEventDefinitionImpl();
        IntermediateThrowEventDefinition intermediateThrowEventDefinition2 = new IntermediateThrowEventDefinitionImpl("Throw1");
        listThrowEvent.add(intermediateThrowEventDefinition1);
        listThrowEvent.add(intermediateThrowEventDefinition2);
        List<EndEventDefinition> endEventDefinitionList = new ArrayList<>();
        EndEventDefinition endEventDefinition1 = new EndEventDefinitionImpl();
        EndEventDefinition endEventDefinition2 = new EndEventDefinitionImpl("End1");
        endEventDefinitionList.add(endEventDefinition1);
        endEventDefinitionList.add(endEventDefinition2);
        flowElementContainerDefinition.setIntermediateCatchEvents(listCatchEvents);
        flowElementContainerDefinition.setIntermediateThrowEvents(listThrowEvent);
        flowElementContainerDefinition.setEndEvents(endEventDefinitionList);
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImplUptoEventDefTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        System.out.println(result2);
    }

    @Test
    public void simpleImportExportTestEventDef() throws JAXBException {
        FlowElementContainerDefinitionImplUptoEventDefTestClass flowElementContainerDefinition = new FlowElementContainerDefinitionImplUptoEventDefTestClass();
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImplUptoEventDefTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(flowElementContainerDefinition).isEqualTo(resultProcessDef);
        System.out.print(result2);
    }

    @Test
    public void ImportExportTestEventDef() throws JAXBException {
        FlowElementContainerDefinitionImplUptoEventDefTestClass flowElementContainerDefinition = new FlowElementContainerDefinitionImplUptoEventDefTestClass();
        Set<TransitionDefinition> transitions = new LinkedHashSet<>();
        TransitionDefinitionImpl transitionDefinition1 = new TransitionDefinitionImpl("name 1");
        TransitionDefinitionImpl transitionDefinition2 = new TransitionDefinitionImpl("name 2");
        transitions.add(transitionDefinition1);
        transitions.add(transitionDefinition2);
        flowElementContainerDefinition.setTransitions(transitions);
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
        List<GatewayDefinition> gatewayDefinitionList = new ArrayList<>();
        GatewayDefinitionImpl gateway1 = new GatewayDefinitionImpl();
        GatewayDefinition gateway2 = new GatewayDefinitionImpl("dummy gateway type", GatewayType.PARALLEL);
        gatewayDefinitionList.add(gateway1);
        gatewayDefinitionList.add(gateway2);
        flowElementContainerDefinition.setGateways(gatewayDefinitionList);
        List<StartEventDefinition> startEvents = new ArrayList<>();
        StartEventDefinitionImpl startEventDefinition1 = new StartEventDefinitionImpl();
        StartEventDefinitionImpl startEventDefinition2 = new StartEventDefinitionImpl("Dummy start event");
        startEvents.add(startEventDefinition1);
        startEvents.add(startEventDefinition2);
        flowElementContainerDefinition.setStartEvents(startEvents);
        List<IntermediateCatchEventDefinition> listCatchEvents = new ArrayList<>();
        IntermediateCatchEventDefinition intermediateCatchEventDefinition1 = new IntermediateCatchEventDefinitionImpl();
        IntermediateCatchEventDefinition intermediateCatchEventDefinition2 = new IntermediateCatchEventDefinitionImpl("Catch1");
        listCatchEvents.add(intermediateCatchEventDefinition1);
        listCatchEvents.add(intermediateCatchEventDefinition2);
        List<IntermediateThrowEventDefinition> listThrowEvent = new ArrayList<>();
        IntermediateThrowEventDefinition intermediateThrowEventDefinition1 = new IntermediateThrowEventDefinitionImpl();
        IntermediateThrowEventDefinition intermediateThrowEventDefinition2 = new IntermediateThrowEventDefinitionImpl("Throw1");
        listThrowEvent.add(intermediateThrowEventDefinition1);
        listThrowEvent.add(intermediateThrowEventDefinition2);
        List<EndEventDefinition> endEventDefinitionList = new ArrayList<>();
        EndEventDefinition endEventDefinition1 = new EndEventDefinitionImpl();
        EndEventDefinition endEventDefinition2 = new EndEventDefinitionImpl("End1");
        endEventDefinitionList.add(endEventDefinition1);
        endEventDefinitionList.add(endEventDefinition2);
        flowElementContainerDefinition.setIntermediateCatchEvents(listCatchEvents);
        flowElementContainerDefinition.setIntermediateThrowEvents(listThrowEvent);
        flowElementContainerDefinition.setEndEvents(endEventDefinitionList);
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImplUptoEventDefTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(flowElementContainerDefinition).isEqualTo(resultProcessDef);
        System.out.print(result2);
    }

    @Test
    public void simpleGenerationTestConnectors() throws JAXBException {
        FlowElementContainerDefinitionImplUptoConnectorsTestClass flowElementContainerDefinition = new FlowElementContainerDefinitionImplUptoConnectorsTestClass();
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImplUptoConnectorsTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        System.out.println(result2);
    }

    @Test
    public void GenerationTestConnectors() throws JAXBException {
        TransitionDefinitionImpl transitionDefinition1 = new TransitionDefinitionImpl("name 1");
        TransitionDefinitionImpl transitionDefinition2 = new TransitionDefinitionImpl("name 2");
        FlowElementContainerDefinitionImplUptoConnectorsTestClass flowElementContainerDefinition = new FlowElementContainerDefinitionImplUptoConnectorsTestClass();
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
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImplUptoConnectorsTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        System.out.println(result2);
    }

    @Test
    public void simpleImportExportTestConnector() throws JAXBException {
        FlowElementContainerDefinitionImplUptoConnectorsTestClass flowElementContainerDefinition = new FlowElementContainerDefinitionImplUptoConnectorsTestClass();
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImplUptoConnectorsTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(flowElementContainerDefinition).isEqualTo(resultProcessDef);
        System.out.print(result2);
    }

    @Test
    public void ImportExportTestConnectors() throws JAXBException {
        TransitionDefinitionImpl transitionDefinition1 = new TransitionDefinitionImpl("name 1");
        TransitionDefinitionImpl transitionDefinition2 = new TransitionDefinitionImpl("name 2");
        FlowElementContainerDefinitionImplUptoConnectorsTestClass flowElementContainerDefinition = new FlowElementContainerDefinitionImplUptoConnectorsTestClass();
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
        JAXBContext jaxbContext = JAXBContext.newInstance(FlowElementContainerDefinitionImplUptoConnectorsTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(flowElementContainerDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(flowElementContainerDefinition).isEqualTo(resultProcessDef);
        System.out.print(result2);
    }
}
