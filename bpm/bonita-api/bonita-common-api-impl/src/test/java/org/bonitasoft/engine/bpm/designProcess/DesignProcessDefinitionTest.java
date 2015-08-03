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

package org.bonitasoft.engine.bpm.designProcess;

import org.bonitasoft.engine.bpm.actor.impl.ActorDefinitionImpl;
import org.bonitasoft.engine.bpm.context.ContextEntryImpl;
import org.bonitasoft.engine.bpm.contract.impl.ConstraintDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.ContractDefinitionImpl;
import org.bonitasoft.engine.bpm.parameter.impl.ParameterDefinitionImpl;
import org.bonitasoft.engine.bpm.process.impl.internal.DesignProcessDefinitionImpl;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author mazourd
 */
public class DesignProcessDefinitionTest {

    @Test
    public void simpleGenerationTest() throws JAXBException {
        DesignProcessDefinitionImpl designProcessDefinition = new DesignProcessDefinitionImpl("aba", "1.0");
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(DesignProcessDefinitionImpl.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(designProcessDefinition, result);
        String result2 = result.toString();
        System.out.print(result2);
    }

    @Test
    public void simpleImportExportTest() throws JAXBException {
        DesignProcessDefinitionImpl designProcessDefinition = new DesignProcessDefinitionImpl("aba", "1.0");
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(DesignProcessDefinitionImpl.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(designProcessDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(designProcessDefinition).isEqualTo(resultProcessDef);
        System.out.print(result2);
    }

    @Test
    public void CompleteNaturalImportExportTest() throws JAXBException {
        DesignProcessDefinitionImpl designProcessDefinition = new DesignProcessDefinitionImpl("aba", "1.0");
        ParameterDefinitionImpl parameterDefinition = new ParameterDefinitionImpl("Dummy name", "Dummy type");
        parameterDefinition.setDescription("nananana Batman");
        designProcessDefinition.addActor(new ActorDefinitionImpl("Actor dummy1"));
        designProcessDefinition.addActor(new ActorDefinitionImpl("Actor dummy2"));
        designProcessDefinition.setActorInitiator(new ActorDefinitionImpl("Actor Initiator"));
        designProcessDefinition.addParameter(parameterDefinition);
        ContractDefinitionImpl contractDefinition = new ContractDefinitionImpl();
        contractDefinition.addConstraint(new ConstraintDefinitionImpl("NameConstraint1", "Lalalala", "lululuicnzih"));
        contractDefinition.addConstraint(new ConstraintDefinitionImpl("NameConstraint2", "Luluulu", "shblsiuhguishngvd"));
        designProcessDefinition.setContract(contractDefinition);
        ContextEntryImpl contextEntry1 = new ContextEntryImpl("NTM", new ExpressionImpl());
        ContextEntryImpl contextEntry2 = new ContextEntryImpl("NTM2", new ExpressionImpl());
        designProcessDefinition.addContextEntry(contextEntry1);
        designProcessDefinition.addContextEntry(contextEntry2);
        designProcessDefinition.setStringIndex(1, "label1", new ExpressionImpl(11));
        designProcessDefinition.setStringIndex(2, "label2", new ExpressionImpl(12));
        designProcessDefinition.setStringIndex(3, "label3", new ExpressionImpl(13));
        designProcessDefinition.setStringIndex(4, "label4", new ExpressionImpl(14));
        designProcessDefinition.setStringIndex(5, "label5", new ExpressionImpl(15));
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(DesignProcessDefinitionImpl.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(designProcessDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(designProcessDefinition).isEqualTo(resultProcessDef);
        System.out.print(result2);
    }

    /*@Test
    public void CompleteExtensiveImportExportTest() throws JAXBException, InvalidProcessDefinitionException {
        ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder();
        processDefinitionBuilder.createNewInstance("New Instance","1.0");
        processDefinitionBuilder.addCallActivity("", new ExpressionImpl(), new ExpressionImpl());
        processDefinitionBuilder.addManualTask("", "");
        processDefinitionBuilder.addManualTask("Manual task name", "actor name");
        processDefinitionBuilder.addReceiveTask("", "");
        processDefinitionBuilder.addReceiveTask("String1", "String2");
        processDefinitionBuilder.addSendTask("", "", new ExpressionImpl());
        processDefinitionBuilder.addSendTask("String1", "String2", new ExpressionImpl());
        processDefinitionBuilder.addUserTask("", "");
        processDefinitionBuilder.addUserTask("name1", "name2");
        processDefinitionBuilder.addSubProcess("", false);
        processDefinitionBuilder.addSubProcess("Subprocess name", false);
        processDefinitionBuilder.addActor("Actor dummy1");
        processDefinitionBuilder.addActor("Actor dummy2");
        processDefinitionBuilder.addActor("Actor dummy3", true);
        processDefinitionBuilder.addParameter("Dummy name", "Dummy type");
        processDefinitionBuilder.addContract();
        processDefinitionBuilder.addContextEntry("NTM", new ExpressionImpl());
        processDefinitionBuilder.addContextEntry("NTM2", new ExpressionImpl());
        processDefinitionBuilder.addEndEvent("");
        processDefinitionBuilder.addEndEvent("Event def map 1");
        processDefinitionBuilder.addStartEvent("");
        processDefinitionBuilder.addStartEvent("Start event 1");
        processDefinitionBuilder.addAutomaticTask("");
        processDefinitionBuilder.addAutomaticTask("Auto Task 1");
        processDefinitionBuilder.addIntermediateCatchEvent("");
        processDefinitionBuilder.addIntermediateCatchEvent("Catch event 12 name");
        processDefinitionBuilder.addIntermediateThrowEvent("");
        processDefinitionBuilder.addIntermediateThrowEvent("Throw event 12 name");
        DesignProcessDefinitionImpl designProcessDefinition = (DesignProcessDefinitionImpl) processDefinitionBuilder.done();
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(DesignProcessDefinitionImpl.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(designProcessDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(designProcessDefinition).isEqualTo(resultProcessDef);
        System.out.print(result2);

    }*/
}
