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
import org.bonitasoft.engine.bpm.context.ContextEntry;
import org.bonitasoft.engine.bpm.context.ContextEntryImpl;
import org.bonitasoft.engine.bpm.contract.impl.ConstraintDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.ContractDefinitionImpl;
import org.bonitasoft.engine.bpm.parameter.impl.ParameterDefinitionImpl;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author mazourd
 */
public class DesignProcessDefinitionPartialTest {

    @Test
    public void simpleGenerationTestParameters() throws JAXBException {
        DesignProcessDefinitionImplTestUptoParametersTestClass designProcessDefinition = new DesignProcessDefinitionImplTestUptoParametersTestClass("aba", "1.0");
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(DesignProcessDefinitionImplTestUptoParametersTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(designProcessDefinition, result);
        String result2 = result.toString();
        System.out.println(result2);
    }

    @Test
    public void simpleGenerationTestParameters2() throws JAXBException {
        DesignProcessDefinitionImplTestUptoParametersTestClass designProcessDefinition = new DesignProcessDefinitionImplTestUptoParametersTestClass("aba", "1.0");
        designProcessDefinition.addParameter(new ParameterDefinitionImpl("Dummy name", "Dummy type"));
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(DesignProcessDefinitionImplTestUptoParametersTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(designProcessDefinition, result);
        String result2 = result.toString();
        System.out.println(result2);
    }

    @Test
    public void simpleImportExportParametersTest() throws JAXBException {
        DesignProcessDefinitionImplTestUptoParametersTestClass designProcessDefinition = new DesignProcessDefinitionImplTestUptoParametersTestClass("aba", "1.0");
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(DesignProcessDefinitionImplTestUptoParametersTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(designProcessDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(designProcessDefinition).isEqualTo(resultProcessDef);
        System.out.println(result2);
    }

    @Test
    public void simpleImportExportParametersTest2() throws JAXBException {
        DesignProcessDefinitionImplTestUptoParametersTestClass designProcessDefinition = new DesignProcessDefinitionImplTestUptoParametersTestClass("aba", "1.0");
        ParameterDefinitionImpl parameterDefinition = new ParameterDefinitionImpl("Dummy name", "Dummy type");
        parameterDefinition.setDescription("nananana Batman");
        designProcessDefinition.addParameter(parameterDefinition);
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(DesignProcessDefinitionImplTestUptoParametersTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(designProcessDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(designProcessDefinition).isEqualTo(resultProcessDef);
        System.out.println(result2);
    }

    @Test
    public void simpleGenerationTestActors() throws JAXBException {
        DesignProcessDefinitionImplTestUptoActorsTestClass designProcessDefinition = new DesignProcessDefinitionImplTestUptoActorsTestClass("aba", "1.0");
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(DesignProcessDefinitionImplTestUptoActorsTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(designProcessDefinition, result);
        String result2 = result.toString();
        System.out.println(result2);
    }

    @Test
    public void simpleGenerationTestActors2() throws JAXBException {
        DesignProcessDefinitionImplTestUptoActorsTestClass designProcessDefinition = new DesignProcessDefinitionImplTestUptoActorsTestClass("aba", "1.0");
        designProcessDefinition.addParameter(new ParameterDefinitionImpl("Dummy name", "Dummy type"));
        designProcessDefinition.addActor(new ActorDefinitionImpl("Actor dummy1"));
        designProcessDefinition.addActor(new ActorDefinitionImpl("Actor dummy2"));
        designProcessDefinition.setActorInitiator(new ActorDefinitionImpl("Actor Initiator"));
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(DesignProcessDefinitionImplTestUptoActorsTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(designProcessDefinition, result);
        String result2 = result.toString();
        System.out.println(result2);
    }

    @Test
    public void simpleImportExportActorsTest() throws JAXBException {
        DesignProcessDefinitionImplTestUptoActorsTestClass designProcessDefinition = new DesignProcessDefinitionImplTestUptoActorsTestClass("aba", "1.0");
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(DesignProcessDefinitionImplTestUptoActorsTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(designProcessDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(designProcessDefinition).isEqualTo(resultProcessDef);
        System.out.println(result2);
    }

    @Test
    public void simpleImportExportActorsTest2() throws JAXBException {
        DesignProcessDefinitionImplTestUptoActorsTestClass designProcessDefinition = new DesignProcessDefinitionImplTestUptoActorsTestClass("aba", "1.0");
        ParameterDefinitionImpl parameterDefinition = new ParameterDefinitionImpl("Dummy name", "Dummy type");
        parameterDefinition.setDescription("nananana Batman");
        designProcessDefinition.addActor(new ActorDefinitionImpl("Actor dummy1"));
        designProcessDefinition.addActor(new ActorDefinitionImpl("Actor dummy2"));
        designProcessDefinition.setActorInitiator(new ActorDefinitionImpl("Actor Initiator"));
        designProcessDefinition.addParameter(parameterDefinition);
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(DesignProcessDefinitionImplTestUptoActorsTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(designProcessDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(designProcessDefinition).isEqualTo(resultProcessDef);
        System.out.println(result2);
    }

    @Test
    public void simpleGenerationTestContract() throws JAXBException {
        DesignProcessDefinitionImplTestUptoContractTestClass designProcessDefinition = new DesignProcessDefinitionImplTestUptoContractTestClass("aba", "1.0");
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(DesignProcessDefinitionImplTestUptoContractTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(designProcessDefinition, result);
        String result2 = result.toString();
        System.out.println(result2);
    }

    @Test
    public void simpleGenerationTestContract2() throws JAXBException {
        DesignProcessDefinitionImplTestUptoContractTestClass designProcessDefinition = new DesignProcessDefinitionImplTestUptoContractTestClass("aba", "1.0");
        designProcessDefinition.addParameter(new ParameterDefinitionImpl("Dummy name", "Dummy type"));
        designProcessDefinition.addActor(new ActorDefinitionImpl("Actor dummy1"));
        designProcessDefinition.addActor(new ActorDefinitionImpl("Actor dummy2"));
        designProcessDefinition.setActorInitiator(new ActorDefinitionImpl("Actor Initiator"));
        ContractDefinitionImpl contractDefinition = new ContractDefinitionImpl();
        contractDefinition.addConstraint(new ConstraintDefinitionImpl("NameConstraint1", "Lalalala", "lululuicnzih"));
        contractDefinition.addConstraint(new ConstraintDefinitionImpl("NameConstraint2", "Luluulu", "shblsiuhguishngvd"));
        designProcessDefinition.setContract(contractDefinition);
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(DesignProcessDefinitionImplTestUptoContractTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(designProcessDefinition, result);
        String result2 = result.toString();
        System.out.println(result2);
    }

    @Test
    public void simpleImportExportContractTest() throws JAXBException {
        DesignProcessDefinitionImplTestUptoContractTestClass designProcessDefinition = new DesignProcessDefinitionImplTestUptoContractTestClass("aba", "1.0");
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(DesignProcessDefinitionImplTestUptoContractTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(designProcessDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(designProcessDefinition).isEqualTo(resultProcessDef);
        System.out.println(result2);
    }

    @Test
    public void simpleImportExportTestContract2() throws JAXBException {
        DesignProcessDefinitionImplTestUptoContractTestClass designProcessDefinition = new DesignProcessDefinitionImplTestUptoContractTestClass("aba", "1.0");
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
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(DesignProcessDefinitionImplTestUptoContractTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(designProcessDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(designProcessDefinition).isEqualTo(resultProcessDef);
        System.out.println(result2);
    }

    @Test
    public void simpleGenerationTestContext() throws JAXBException {
        DesignProcessDefinitionImplTestUptoContextTestClass designProcessDefinition = new DesignProcessDefinitionImplTestUptoContextTestClass("aba", "1.0");
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(DesignProcessDefinitionImplTestUptoContextTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(designProcessDefinition, result);
        String result2 = result.toString();
        System.out.println(result2);
    }

    @Test
    public void simpleGenerationTestContext2() throws JAXBException {
        DesignProcessDefinitionImplTestUptoContextTestClass designProcessDefinition = new DesignProcessDefinitionImplTestUptoContextTestClass("aba", "1.0");
        designProcessDefinition.addParameter(new ParameterDefinitionImpl("Dummy name", "Dummy type"));
        designProcessDefinition.addActor(new ActorDefinitionImpl("Actor dummy1"));
        designProcessDefinition.addActor(new ActorDefinitionImpl("Actor dummy2"));
        designProcessDefinition.setActorInitiator(new ActorDefinitionImpl("Actor Initiator"));
        ContractDefinitionImpl contractDefinition = new ContractDefinitionImpl();
        contractDefinition.addConstraint(new ConstraintDefinitionImpl("NameConstraint1", "Lalalala", "lululuicnzih"));
        contractDefinition.addConstraint(new ConstraintDefinitionImpl("NameConstraint2", "Luluulu", "shblsiuhguishngvd"));
        ContextEntryImpl contextEntry1 = new ContextEntryImpl("NTM", new ExpressionImpl());
        ContextEntryImpl contextEntry2 = new ContextEntryImpl("NTM2", new ExpressionImpl());
        List<ContextEntry> tempList = new ArrayList<>();
        tempList.add(contextEntry1);
        tempList.add(contextEntry2);
        designProcessDefinition.setContext(tempList);
        designProcessDefinition.setContract(contractDefinition);
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(DesignProcessDefinitionImplTestUptoContextTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(designProcessDefinition, result);
        String result2 = result.toString();
        System.out.println(result2);
    }

    @Test
    public void simpleImportExportContextTest() throws JAXBException {
        DesignProcessDefinitionImplTestUptoContextTestClass designProcessDefinition = new DesignProcessDefinitionImplTestUptoContextTestClass("aba", "1.0");
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(DesignProcessDefinitionImplTestUptoContextTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(designProcessDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(designProcessDefinition).isEqualTo(resultProcessDef);
        System.out.println(result2);
    }

    @Test
    public void simpleImportExportTestContext2() throws JAXBException {
        DesignProcessDefinitionImplTestUptoContextTestClass designProcessDefinition = new DesignProcessDefinitionImplTestUptoContextTestClass("aba", "1.0");
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
        List<ContextEntry> tempList = new ArrayList<>();
        tempList.add(contextEntry1);
        tempList.add(contextEntry2);
        designProcessDefinition.setContext(tempList);
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(DesignProcessDefinitionImplTestUptoContextTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(designProcessDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(designProcessDefinition).isEqualTo(resultProcessDef);
        System.out.println(result2);
    }
        @Test
    public void simpleGenerationTestIndexandValues() throws JAXBException {
        DesignProcessDefinitionImplTestUptoIndexandValuesTestClass designProcessDefinition = new DesignProcessDefinitionImplTestUptoIndexandValuesTestClass("aba", "1.0");
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(DesignProcessDefinitionImplTestUptoIndexandValuesTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(designProcessDefinition, result);
        String result2 = result.toString();
        System.out.println(result2);
    }

    @Test
    public void simpleGenerationTestIndexandValues2() throws JAXBException {
        DesignProcessDefinitionImplTestUptoIndexandValuesTestClass designProcessDefinition = new DesignProcessDefinitionImplTestUptoIndexandValuesTestClass("aba", "1.0");
        designProcessDefinition.addParameter(new ParameterDefinitionImpl("Dummy name", "Dummy type"));
        designProcessDefinition.addActor(new ActorDefinitionImpl("Actor dummy1"));
        designProcessDefinition.addActor(new ActorDefinitionImpl("Actor dummy2"));
        designProcessDefinition.setActorInitiator(new ActorDefinitionImpl("Actor Initiator"));
        ContractDefinitionImpl contractDefinition = new ContractDefinitionImpl();
        contractDefinition.addConstraint(new ConstraintDefinitionImpl("NameConstraint1", "Lalalala", "lululuicnzih"));
        contractDefinition.addConstraint(new ConstraintDefinitionImpl("NameConstraint2", "Luluulu", "shblsiuhguishngvd"));
        ContextEntryImpl contextEntry1 = new ContextEntryImpl("NTM", new ExpressionImpl());
        ContextEntryImpl contextEntry2 = new ContextEntryImpl("NTM2", new ExpressionImpl());
        List<ContextEntry> tempList = new ArrayList<>();
        tempList.add(contextEntry1);
        tempList.add(contextEntry2);
        designProcessDefinition.setContext(tempList);
        designProcessDefinition.setContract(contractDefinition);
        designProcessDefinition.setStringIndex(1, "label1", new ExpressionImpl(11));
        designProcessDefinition.setStringIndex(2, "label2", new ExpressionImpl(12));
        designProcessDefinition.setStringIndex(3, "label3", new ExpressionImpl(13));
        designProcessDefinition.setStringIndex(4, "label4", new ExpressionImpl(14));
        designProcessDefinition.setStringIndex(5,"label5",new ExpressionImpl(15));
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(DesignProcessDefinitionImplTestUptoIndexandValuesTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(designProcessDefinition, result);
        String result2 = result.toString();
        System.out.println(result2);
    }

    @Test
    public void simpleImportExportIndexandValuesTest() throws JAXBException {
        DesignProcessDefinitionImplTestUptoIndexandValuesTestClass designProcessDefinition = new DesignProcessDefinitionImplTestUptoIndexandValuesTestClass("aba", "1.0");
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(DesignProcessDefinitionImplTestUptoIndexandValuesTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(designProcessDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(designProcessDefinition).isEqualTo(resultProcessDef);
        System.out.println(result2);
    }

    @Test
    public void simpleImportExportTestIndexandValues2() throws JAXBException {
        DesignProcessDefinitionImplTestUptoIndexandValuesTestClass designProcessDefinition = new DesignProcessDefinitionImplTestUptoIndexandValuesTestClass("aba", "1.0");
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
        List<ContextEntry> tempList = new ArrayList<>();
        tempList.add(contextEntry1);
        tempList.add(contextEntry2);
        designProcessDefinition.setContext(tempList);
        designProcessDefinition.setStringIndex(1, "label1", new ExpressionImpl(11));
        designProcessDefinition.setStringIndex(2, "label2", new ExpressionImpl(12));
        designProcessDefinition.setStringIndex(3, "label3", new ExpressionImpl(13));
        designProcessDefinition.setStringIndex(4, "label4", new ExpressionImpl(14));
        designProcessDefinition.setStringIndex(5, "label5", new ExpressionImpl(15));
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(DesignProcessDefinitionImplTestUptoIndexandValuesTestClass.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(designProcessDefinition, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object resultProcessDef = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(designProcessDefinition).isEqualTo(resultProcessDef);
        System.out.println(result2);
    }
}
