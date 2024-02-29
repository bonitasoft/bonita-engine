/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.operation;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.assertj.core.api.SoftAssertions;
import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.contract.FileInputValue;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.AutomaticTaskDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.xml.DocumentManager;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author Baptiste Mesta
 */
public class OperationIT extends TestWithUser {

    @Test
    public void executeStringOperationOnData() throws Exception {
        createAndExecuteProcessWithOperations("executeStringOperationOnData", singletonList("myData1"),
                singletonList(String.class.getName()),
                singletonList(new LeftOperandBuilder().createNewInstance().setName("myData1").done()),
                singletonList(OperatorType.ASSIGNMENT), singletonList("="),
                singletonList(new ExpressionBuilder().createConstantStringExpression("val1")),
                singletonList(new ExpressionBuilder().createConstantStringExpression("val2")),
                singletonList("val1"),
                singletonList("val2"));
    }

    @Test
    public void executeBooleanOperationOnData() throws Exception {
        createAndExecuteProcessWithOperations("executeBooleanOperationOnData", singletonList("myData1"),
                singletonList(Boolean.class.getName()),
                singletonList(new LeftOperandBuilder().createNewInstance().setName("myData1").done()),
                singletonList(OperatorType.ASSIGNMENT), singletonList("="),
                singletonList(new ExpressionBuilder().createConstantBooleanExpression(true)),
                singletonList(new ExpressionBuilder().createConstantBooleanExpression(false)),
                singletonList(true),
                singletonList(false));
    }

    @Test
    public void executeStringIndexOperation() throws Exception {
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder()
                .createNewInstance("procWithStringIndexes", "1.0");
        final String actorName = "doctor";
        designProcessDefinition.addActor(actorName).addDescription("The doc'");
        designProcessDefinition.addUserTask("step1", actorName);
        designProcessDefinition.addUserTask("step3", actorName);
        final AutomaticTaskDefinitionBuilder addAutomaticTask = designProcessDefinition.addAutomaticTask("step2");
        addAutomaticTask.addOperation(new OperationBuilder().createSetStringIndexOperation(1,
                new ExpressionBuilder().createConstantStringExpression("newValue1")));
        addAutomaticTask.addOperation(new OperationBuilder().createSetStringIndexOperation(2,
                new ExpressionBuilder().createConstantStringExpression("newValue2")));
        addAutomaticTask.addOperation(new OperationBuilder().createSetStringIndexOperation(3,
                new ExpressionBuilder().createConstantStringExpression("newValue3")));
        addAutomaticTask.addOperation(new OperationBuilder().createSetStringIndexOperation(4,
                new ExpressionBuilder().createConstantStringExpression("newValue4")));
        addAutomaticTask.addOperation(new OperationBuilder().createSetStringIndexOperation(5,
                new ExpressionBuilder().createConstantStringExpression("newValue5")));
        designProcessDefinition.addTransition("step1", "step2");
        designProcessDefinition.addTransition("step2", "step3");
        designProcessDefinition.setStringIndex(1, "label1",
                new ExpressionBuilder().createConstantStringExpression("value1"));
        designProcessDefinition.setStringIndex(2, "label2",
                new ExpressionBuilder().createConstantStringExpression("value2"));
        designProcessDefinition.setStringIndex(3, "label3",
                new ExpressionBuilder().createConstantStringExpression("value3"));
        designProcessDefinition.setStringIndex(4, "label4",
                new ExpressionBuilder().createConstantStringExpression("value4"));
        designProcessDefinition.setStringIndex(5, "label5",
                new ExpressionBuilder().createConstantStringExpression("value5"));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(),
                actorName, user);

        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(startProcess, "step1");
        ProcessInstance processInstance = getProcessAPI().getProcessInstance(startProcess.getId());
        assertEquals("value1", processInstance.getStringIndex1());
        assertEquals("value2", processInstance.getStringIndex2());
        assertEquals("value3", processInstance.getStringIndex3());
        assertEquals("value4", processInstance.getStringIndex4());
        assertEquals("value5", processInstance.getStringIndex5());
        assignAndExecuteStep(step1Id, user);
        waitForUserTask(startProcess, "step3");
        processInstance = getProcessAPI().getProcessInstance(startProcess.getId());
        assertEquals("newValue1", processInstance.getStringIndex1());
        assertEquals("newValue2", processInstance.getStringIndex2());
        assertEquals("newValue3", processInstance.getStringIndex3());
        assertEquals("newValue4", processInstance.getStringIndex4());
        assertEquals("newValue5", processInstance.getStringIndex5());
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeStringIndexOperationUsingData() throws Exception {
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder()
                .createNewInstance("procWithStringIndexes", "1.0");
        final String actorName = "doctor";
        designProcessDefinition.addData("baseData", String.class.getName(),
                new ExpressionBuilder().createConstantStringExpression("baseValue"));
        designProcessDefinition.addActor(actorName).addDescription("The doc'");
        designProcessDefinition.addUserTask("step1", actorName);
        designProcessDefinition.addUserTask("step3", actorName);
        final AutomaticTaskDefinitionBuilder addAutomaticTask = designProcessDefinition.addAutomaticTask("step2");
        addAutomaticTask.addOperation(new LeftOperandBuilder().createDataLeftOperand("baseData"),
                OperatorType.ASSIGNMENT, null, null,
                new ExpressionBuilder().createConstantStringExpression("changedData"));
        addAutomaticTask.addOperation(new OperationBuilder().createSetStringIndexOperation(1,
                new ExpressionBuilder().createDataExpression("baseData", String.class.getName())));
        designProcessDefinition.addTransition("step1", "step2");
        designProcessDefinition.addTransition("step2", "step3");
        designProcessDefinition.setStringIndex(1, "label1",
                new ExpressionBuilder().createDataExpression("baseData", String.class.getName()));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(),
                actorName, user);

        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(startProcess, "step1");
        ProcessInstance processInstance = getProcessAPI().getProcessInstance(startProcess.getId());
        assertEquals("baseValue", processInstance.getStringIndex1());
        assignAndExecuteStep(step1Id, user);
        waitForUserTask(startProcess, "step3");
        processInstance = getProcessAPI().getProcessInstance(startProcess.getId());
        assertEquals("changedData", processInstance.getStringIndex1());
        disableAndDeleteProcess(processDefinition);
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    public void executeOperationWithNoExpression() throws Exception {
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder()
                .createNewInstance("procWithStringIndexes", "1.0");
        final String actorName = "doctor";
        designProcessDefinition.addActor(actorName).addDescription("The doc'");
        designProcessDefinition.addUserTask("step1", actorName);
        designProcessDefinition.addUserTask("step3", actorName);
        final AutomaticTaskDefinitionBuilder addAutomaticTask = designProcessDefinition.addAutomaticTask("step2");
        addAutomaticTask.addOperation(new OperationBuilder().createSetStringIndexOperation(1, null));
        designProcessDefinition.addTransition("step1", "step2");
        designProcessDefinition.addTransition("step2", "step3");
        designProcessDefinition.done();
    }

    @Test
    public void executeIntegerOperationOnData() throws Exception {
        createAndExecuteProcessWithOperations("executeIntegerOperationOnData", singletonList("myData12"),
                singletonList(Integer.class.getName()),
                singletonList(new LeftOperandBuilder().createNewInstance().setName("myData12").done()),
                singletonList(OperatorType.ASSIGNMENT), singletonList("="),
                singletonList(new ExpressionBuilder().createConstantIntegerExpression(1)),
                singletonList(new ExpressionBuilder().createConstantIntegerExpression(2)),
                singletonList(1),
                singletonList(2));
    }

    @Test
    public void executeLongOperationOnData() throws Exception {
        createAndExecuteProcessWithOperations("executeLongOperationOnData", singletonList("myData12"),
                singletonList(Long.class.getName()),
                singletonList(new LeftOperandBuilder().createNewInstance().setName("myData12").done()),
                singletonList(OperatorType.ASSIGNMENT), singletonList("="),
                singletonList(new ExpressionBuilder().createConstantLongExpression(1234567891234567891L)),
                singletonList(new ExpressionBuilder().createConstantLongExpression(1234567891234567892L)),
                singletonList(1234567891234567891L),
                singletonList(1234567891234567892L));
    }

    @Test
    public void executeMultipleOperations() throws Exception {
        createAndExecuteProcessWithOperations("executeMultipleOperations",
                Arrays.asList("myData1", "myData2", "myData3"), Arrays.asList(
                        String.class.getName(), Boolean.class.getName(), Long.class.getName()),
                Arrays.asList(
                        new LeftOperandBuilder().createNewInstance().setName("myData1").done(),
                        new LeftOperandBuilder().createNewInstance().setName("myData2").done(),
                        new LeftOperandBuilder().createNewInstance().setName("myData3").done()),
                Arrays.asList(OperatorType.ASSIGNMENT, OperatorType.ASSIGNMENT,
                        OperatorType.ASSIGNMENT),
                Arrays.asList("=", "=", "="),
                Arrays.asList(new ExpressionBuilder().createConstantStringExpression("test1"),
                        new ExpressionBuilder().createConstantBooleanExpression(false),
                        new ExpressionBuilder().createConstantLongExpression(1234567891234567891L)),
                Arrays.asList(new ExpressionBuilder().createConstantStringExpression("test2"),
                        new ExpressionBuilder().createConstantBooleanExpression(true),
                        new ExpressionBuilder().createConstantLongExpression(1234567891234567892L)),
                Arrays.asList("test1", false,
                        1234567891234567891L),
                Arrays.asList("test2", true, 1234567891234567892L));
    }

    @Test
    public void executeMultipleOperationsWithSameData() throws Exception {
        createAndExecuteProcessWithOperations("executeMultipleOperationsWithSameData",
                Arrays.asList("myData1", "myData2", "myData3", "myData1"),
                Arrays.asList(String.class.getName(), Boolean.class.getName(), Long.class.getName(),
                        String.class.getName()),
                Arrays.asList(
                        new LeftOperandBuilder().createNewInstance().setName("myData1").done(),
                        new LeftOperandBuilder().createNewInstance().setName("myData2")
                                .done(),
                        new LeftOperandBuilder().createNewInstance().setName("myData3").done(),
                        new LeftOperandBuilder().createNewInstance()
                                .setName("myData1").done()),
                Arrays.asList(OperatorType.ASSIGNMENT, OperatorType.ASSIGNMENT, OperatorType.ASSIGNMENT,
                        OperatorType.ASSIGNMENT),
                Arrays.asList("=", "=", "=", "="), Arrays.asList(
                        new ExpressionBuilder().createConstantStringExpression("test1"),
                        new ExpressionBuilder().createConstantBooleanExpression(false),
                        new ExpressionBuilder().createConstantLongExpression(1234567891234567891L),
                        new ExpressionBuilder().createConstantStringExpression("test1")),
                Arrays.asList(
                        new ExpressionBuilder().createConstantStringExpression("test2"),
                        new ExpressionBuilder().createConstantBooleanExpression(true),
                        new ExpressionBuilder().createConstantLongExpression(1234567891234567892L),
                        new ExpressionBuilder().createConstantStringExpression("test3")),
                Arrays.asList("test1", false,
                        1234567891234567891L, "test1"),
                Arrays.asList("test3", true, 1234567891234567892L,
                        "test3"));
    }

    @Test
    public void should_set_documents_with_operations() throws Exception {
        final String actorName = "Document supplier";
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("should_set_documents_with_operations", "1.0");
        processDefinitionBuilder.addActor(actorName).addDescription("Process documents");
        processDefinitionBuilder.addDocumentDefinition("doc1");
        processDefinitionBuilder.addDocumentDefinition("doc2").addDescription("Will receive the content of doc1");
        processDefinitionBuilder.addDocumentListDefinition("docList1");
        processDefinitionBuilder.addDocumentListDefinition("docList2")
                .addDescription("Will receive the content of docList1");

        processDefinitionBuilder.addUserTask("step0", actorName);
        processDefinitionBuilder
                .addAutomaticTask("step1")
                .addOperation(new OperationBuilder().createSetDocumentList("docList1",
                        new ExpressionBuilder().createGroovyScriptExpression("setDocumentList1()",
                                "[new org.bonitasoft.engine.bpm.contract.FileInputValue('doc1.txt', 'content of doc1'.getBytes('UTF-8'))"
                                        + ",new org.bonitasoft.engine.bpm.contract.FileInputValue('doc2.txt', 'content of doc2'.getBytes('UTF-8'))]",
                                List.class.getName())))
                .addOperation(new OperationBuilder().createSetDocumentList("docList2",
                        new ExpressionBuilder().createGroovyScriptExpression("setDocList1ValueToDocList2()",
                                "return docList1",
                                List.class.getName(),
                                // mimic what we have in BS-18741, force usage of document list reference as this is done
                                // in the Studio (automatic dependencies resolution)
                                new ExpressionBuilder().createDocumentListExpression("docList1"))))
                .addOperation(new OperationBuilder().createSetDocument("doc1",
                        new ExpressionBuilder().createGroovyScriptExpression("setDoc1()",
                                "new org.bonitasoft.engine.bpm.contract.FileInputValue('doc1.txt', 'content of doc1'.getBytes('UTF-8'))",
                                FileInputValue.class.getName())))
                .addOperation(new OperationBuilder().createSetDocument("doc2",
                        new ExpressionBuilder().createGroovyScriptExpression("setDoc1ValueToDoc2()",
                                "doc1",
                                DocumentValue.class.getName()
                                // mimic what we have in BS-18741, force usage of document reference as this is done
                                // in the Studio (automatic dependencies resolution)
                                , new ExpressionBuilder().createDocumentReferenceExpression("doc1"))));

        processDefinitionBuilder.addUserTask("step2", actorName);
        processDefinitionBuilder.addTransition("step0", "step1");
        processDefinitionBuilder.addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(),
                actorName, user);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(processInstance, "step0", user);
        waitForUserTask(processInstance, "step2");

        SoftAssertions softly = new SoftAssertions();

        final List<org.bonitasoft.engine.bpm.document.Document> docList1AfterExecution = getProcessAPI()
                .getDocumentList(processInstance.getId(), "docList1", 0, 100);
        softly.assertThat(docList1AfterExecution).as("docList1 after execution")
                .hasSize(2)
                .extracting(org.bonitasoft.engine.bpm.document.Document::getContentFileName)
                .containsOnly("doc1.txt", "doc2.txt");
        final List<org.bonitasoft.engine.bpm.document.Document> docList2AfterExecution = getProcessAPI()
                .getDocumentList(processInstance.getId(), "docList2", 0, 100);
        softly.assertThat(docList2AfterExecution).as("docList2 after execution")
                .hasSize(2)
                .extracting(org.bonitasoft.engine.bpm.document.Document::getContentFileName)
                .containsOnly("doc1.txt", "doc2.txt");

        final org.bonitasoft.engine.bpm.document.Document doc1AfterExecution = getProcessAPI()
                .getLastDocument(processInstance.getId(), "doc1");
        softly.assertThat(doc1AfterExecution.hasContent()).isTrue();
        softly.assertThat(doc1AfterExecution.getContentFileName()).isEqualTo("doc1.txt");

        final org.bonitasoft.engine.bpm.document.Document doc2AfterExecution = getProcessAPI()
                .getLastDocument(processInstance.getId(), "doc2");
        softly.assertThat(doc2AfterExecution.hasContent()).isTrue();
        softly.assertThat(doc2AfterExecution.getContentFileName()).isEqualTo("doc1.txt");

        disableAndDeleteProcess(processDefinition);
        softly.assertAll();
    }

    protected void createAndExecuteProcessWithOperations(final String procName, final List<String> dataName,
            final List<String> className,
            final List<LeftOperand> leftOperand, final List<OperatorType> assignment, final List<String> operator,
            final List<Expression> dataDefaultValue,
            final List<Expression> expression, final List<Object> valueBefore, final List<Object> valueAfter)
            throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder()
                .createNewInstance(procName, "1.0");
        final HashSet<String> dataSet = new HashSet<>(dataName.size());
        for (int i = 0; i < dataName.size(); i++) {
            final String name = dataName.get(i);
            if (!dataSet.contains(name)) {
                designProcessDefinition.addData(name, className.get(i), dataDefaultValue.get(i));
                dataSet.add(name);
            }
        }
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addUserTask("step0", delivery);
        final AutomaticTaskDefinitionBuilder addAutomaticTask = designProcessDefinition.addAutomaticTask("step1");
        for (int i = 0; i < leftOperand.size(); i++) {
            // No Java operation, so empty string passed:
            addAutomaticTask.addOperation(leftOperand.get(i), assignment.get(i), operator.get(i), "",
                    expression.get(i));
        }
        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step0", "step1");
        designProcessDefinition.addTransition("step1", "step2");
        designProcessDefinition.setStringIndex(1, "label1",
                new ExpressionBuilder().createConstantStringExpression("value1"));
        designProcessDefinition.setStringIndex(2, "label2",
                new ExpressionBuilder().createConstantStringExpression("value2"));
        designProcessDefinition.setStringIndex(3, "label3",
                new ExpressionBuilder().createConstantStringExpression("value3"));
        designProcessDefinition.setStringIndex(4, "label4",
                new ExpressionBuilder().createConstantStringExpression("value4"));
        designProcessDefinition.setStringIndex(5, "label5",
                new ExpressionBuilder().createConstantStringExpression("value5"));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(),
                delivery, user);

        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        final HashMap<String, Integer> inverDataOrder = new HashMap<>(dataName.size());
        for (int i = dataName.size() - 1; i >= 0; i--) {
            final String name = dataName.get(i);
            if (!inverDataOrder.containsKey(name)) {
                inverDataOrder.put(name, i);
            }
        }
        for (final Entry<String, Integer> dataIndex : inverDataOrder.entrySet()) {
            assertEquals("before execution of operation " + dataIndex.getKey(), valueBefore.get(dataIndex.getValue()),
                    getProcessAPI().getProcessDataInstance(dataIndex.getKey(), startProcess.getId()).getValue());
        }

        waitForUserTaskAndExecuteIt(startProcess, "step0", user);
        waitForUserTask(startProcess, "step2");

        for (final Entry<String, Integer> dataIndex : inverDataOrder.entrySet()) {
            assertEquals("after execution of operation " + dataIndex.getKey(), valueAfter.get(dataIndex.getValue()),
                    getProcessAPI().getProcessDataInstance(dataIndex.getKey(), startProcess.getId()).getValue());
        }
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void updateAnAttributeOfAnXMLNodeOfADataUsingXPath() throws Exception {
        final String defaultValue = "<root name=\"before\"/>";
        final String updatedValue = "<root name=\"after\"/>";
        final String xPathExpression = "/root/@name";
        final String updatedContent = "after";
        executeProcessAndUpdateData(defaultValue, xPathExpression, updatedContent, updatedValue);
    }

    @Test
    public void setTheContentOfAnXMLNodeOfADataUsingXPath() throws Exception {
        final String defaultValue = "<root />";
        final String updatedValue = "<root>after</root>";
        final String xPathExpression = "/root/text()";
        final String updatedContent = "after";
        executeProcessAndUpdateData(defaultValue, xPathExpression, updatedContent, updatedValue);
    }

    @Test
    public void updateTheContentOfAnXMLNodeOfADataUsingXPath() throws Exception {
        final String defaultValue = "<root>before</root>";
        final String updatedValue = "<root>after</root>";
        final String xPathExpression = "/root/text()";
        final String updatedContent = "after";
        executeProcessAndUpdateData(defaultValue, xPathExpression, updatedContent, updatedValue);
    }

    @Test
    public void updateTheContentOfASubNodeOfAnXMLNodeOfADataUsingXPath() throws Exception {
        final String defaultValue = "<node><subnode>before</subnode></node>";
        final String updatedValue = "<node><subnode>after</subnode></node>";
        final String xPathExpression = "/node/subnode[1]/text()";
        final String updatedContent = "after";
        executeProcessAndUpdateData(defaultValue, xPathExpression, updatedContent, updatedValue);
    }

    @Test
    public void removeAndCreateANewNodeOfAnXMLNodeOfADataUsingXPath() throws Exception {
        final String defaultValue = "<node><before/></node>";
        final String updatedValue = "<node><after/></node>";
        final String xPathExpression = "/node/before";
        final String updatedContent = "after";
        final ExpressionBuilder expressionBuilder = new ExpressionBuilder();
        final Expression rightOperand = expressionBuilder.createGroovyScriptExpression(
                "removeAndCreateANewNodeOfAnXMLNodeOfADataUsingXPath",
                "import javax.xml.parsers.DocumentBuilder;"
                        + "import javax.xml.parsers.DocumentBuilderFactory;import org.w3c.dom.Node;"
                        + "DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder(); return builder.newDocument().createElement(\""
                        + updatedContent + "\");",
                Node.class.getName());
        executeProcessAndUpdateData(defaultValue, xPathExpression, rightOperand, updatedValue);
    }

    @Test
    public void updateTheContentOfAnXMLNodeOfADataWithIntegerValueUsingXPath() throws Exception {
        updateAttributeAndContentWithNumericValueUsingXPath(
                new ExpressionBuilder().createConstantIntegerExpression(123), "123");
    }

    private void updateAttributeAndContentWithNumericValueUsingXPath(final Expression rightOperand, final String result)
            throws Exception {
        executeProcessAndUpdateData(Arrays.asList("<root>before</root>", "<root name=\"before\"/>"),
                Arrays.asList("/root/text()", "/root/@name"),
                Arrays.asList(rightOperand, rightOperand),
                Arrays.asList("<root>" + result + "</root>", "<root name=\"" + result + "\"/>"));
    }

    @Test
    public void updateTheContentOfAnXMLNodeOfADataWithBooleanValueUsingXPath() throws Exception {
        updateAttributeAndContentWithNumericValueUsingXPath(
                new ExpressionBuilder().createConstantBooleanExpression(true), "true");
    }

    @Test
    public void updateTheContentOfAnXMLNodeOfADataWithLongValueUsingXPath() throws Exception {
        updateAttributeAndContentWithNumericValueUsingXPath(new ExpressionBuilder().createConstantLongExpression(123),
                "123");
    }

    @Test
    public void updateTheContentOfAnXMLNodeOfADataWithDoubleValueUsingXPath() throws Exception {
        updateAttributeAndContentWithNumericValueUsingXPath(
                new ExpressionBuilder().createConstantDoubleExpression(123.123), "123.123");
    }

    private void executeProcessAndUpdateData(final String defaultValue, final String xPathExpression,
            final String updatedContent, final String updatedValue)
            throws Exception {
        final ExpressionBuilder expressionBuilder = new ExpressionBuilder();
        final Expression rightOperand = expressionBuilder.createConstantStringExpression(updatedContent);
        executeProcessAndUpdateData(defaultValue, xPathExpression, rightOperand, updatedValue);
    }

    private void executeProcessAndUpdateData(final List<String> defaultValues, final List<String> xPathExpressions,
            final List<Expression> rightOperands,
            final List<String> updatedValues) throws Exception {
        final ExpressionBuilder expressionBuilder = new ExpressionBuilder();
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder()
                .createNewInstance("XPathEvaluation", "1.0");
        final String variableBaseName = "var";
        int i = 0;
        designProcessDefinition.addActor("Workers");
        final UserTaskDefinitionBuilder userTask = designProcessDefinition.addAutomaticTask("start")
                .addUserTask("step1", "Workers");
        final Iterator<String> xPathExpression = xPathExpressions.iterator();
        final Iterator<Expression> rightOperand = rightOperands.iterator();
        for (final String defaultValue : defaultValues) {
            final String variableName = variableBaseName + i;
            i++;
            final Expression defaultExpression = expressionBuilder.createConstantStringExpression(defaultValue);
            designProcessDefinition.addXMLData(variableName, defaultExpression).setNamespace("http://www.w3c.org");
            userTask.addOperation(new OperationBuilder().createXPathOperation(variableName, xPathExpression.next(),
                    rightOperand.next()));
        }

        designProcessDefinition.addUserTask("step2", "Workers").addTransition("start", "step1").addTransition("step1",
                "step2");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(),
                "Workers", user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(startProcess, "step1");

        i = 0;
        for (final String defaultValue : defaultValues) {
            final String variableName = variableBaseName + i;
            i++;
            assertEquals(defaultValue,
                    getProcessAPI().getProcessDataInstance(variableName, startProcess.getId()).getValue());
        }
        assignAndExecuteStep(step1Id, user);

        waitForUserTask(startProcess, "step2");
        i = 0;
        for (final String updatedValue : updatedValues) {
            final String variableName = variableBaseName + i;
            i++;
            final Document expected = DocumentManager.generateDocument(updatedValue);
            final Document actual = DocumentManager.generateDocument(
                    (String) getProcessAPI().getProcessDataInstance(variableName, startProcess.getId())
                            .getValue());

            XMLUnit.setIgnoreWhitespace(true);
            assertTrue(XMLUnit.compareXML(expected, actual).identical());
        }
        disableAndDeleteProcess(processDefinition);
    }

    private void executeProcessAndUpdateData(final String defaultValue, final String xPathExpression,
            final Expression rightOperand, final String updatedValue) throws Exception {
        executeProcessAndUpdateData(singletonList(defaultValue), singletonList(xPathExpression),
                singletonList(rightOperand), singletonList(updatedValue));
    }

    @Test
    public void initializeAVariableUsingAPIAccessor() throws Exception {
        final ExpressionBuilder expressionBuilder = new ExpressionBuilder();
        final Expression apiAccessor = expressionBuilder.createAPIAccessorExpression();
        final List<Expression> dependencies = new ArrayList<>(1);
        dependencies.add(apiAccessor);
        final Expression defaultExpression = expressionBuilder.createGroovyScriptExpression(
                "initializeAVariableUsingAPIAccessor",
                "apiAccessor.getIdentityAPI().getNumberOfUsers()", Long.class.getName(), dependencies);

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder()
                .createNewInstance("APIAccessorInGroovyScript", "1.0");
        designProcessDefinition.addLongData("users", defaultExpression);
        designProcessDefinition.addActor("Workers").addUserTask("step1", "Workers");
        designProcessDefinition.addAutomaticTask("start").addTransition("start", "step1");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(),
                "Workers", user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(startProcess, "step1");

        final long numberOfUsers = getIdentityAPI().getNumberOfUsers();
        assertEquals(numberOfUsers, getProcessAPI().getProcessDataInstance("users", startProcess.getId()).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void updateAVariableUsingLoggedUser() throws Exception {
        final LeftOperandBuilder leftOperandBuilder = new LeftOperandBuilder();
        final LeftOperand leftOperand = leftOperandBuilder.createDataLeftOperand("userId");

        final ExpressionBuilder expressionBuilder = new ExpressionBuilder();
        final Expression loggedUser = expressionBuilder.createEngineConstant(ExpressionConstants.LOGGED_USER_ID);

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder()
                .createNewInstance("APIAccessorInGroovyScript", "1.0");
        designProcessDefinition.addLongData("userId", new ExpressionBuilder().createConstantLongExpression(123L));
        designProcessDefinition.addActor("Workers").addUserTask("step1", "Workers").addOperation(leftOperand,
                OperatorType.ASSIGNMENT, "=", null, loggedUser);
        designProcessDefinition.addAutomaticTask("start").addUserTask("step2", "Workers")
                .addTransition("start", "step1").addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(),
                "Workers", user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(startProcess, "step1", user);
        waitForUserTask(startProcess, "step2");
        assertEquals(user.getId(), getProcessAPI().getProcessDataInstance("userId", startProcess.getId()).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void updateAVariableUsingAPIAccessor() throws Exception {
        final LeftOperandBuilder leftOperandBuilder = new LeftOperandBuilder();
        final LeftOperand leftOperand = leftOperandBuilder.createNewInstance("users").done();

        final ExpressionBuilder expressionBuilder = new ExpressionBuilder();
        final Expression apiAccessor = expressionBuilder.createAPIAccessorExpression();
        final List<Expression> dependencies = new ArrayList<>(1);
        dependencies.add(apiAccessor);
        final Expression defaultExpression = expressionBuilder.createGroovyScriptExpression(
                "updateAVariableUsingAPIAccessor",
                "apiAccessor.getIdentityAPI().getNumberOfUsers()", Long.class.getName(), dependencies);

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder()
                .createNewInstance("APIAccessorInGroovyScript", "1.0");
        designProcessDefinition.addLongData("users", null);
        designProcessDefinition.addActor("Workers").addUserTask("step1", "Workers")
                .addOperation(leftOperand, OperatorType.ASSIGNMENT, "=", null, defaultExpression);
        designProcessDefinition.addAutomaticTask("start").addUserTask("step2", "Workers")
                .addTransition("start", "step1").addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(),
                "Workers", user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(startProcess, "step1", user);
        waitForUserTask(startProcess, "step2");

        final long numberOfUsers = getIdentityAPI().getNumberOfUsers();
        assertEquals(numberOfUsers, getProcessAPI().getProcessDataInstance("users", startProcess.getId()).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void takeTransitionUsingAPIAccessor() throws Exception {
        final ExpressionBuilder expressionBuilder = new ExpressionBuilder();
        final Expression apiAccessor = expressionBuilder.createAPIAccessorExpression();
        final List<Expression> dependencies = new ArrayList<>(1);
        dependencies.add(apiAccessor);
        final Expression defaultExpression = expressionBuilder.createGroovyScriptExpression(
                "takeTransitionUsingAPIAccessor",
                "apiAccessor.getIdentityAPI().getNumberOfUsers() < 100", Boolean.class.getName(), dependencies);

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder()
                .createNewInstance("APIAccessorInGroovyScript", "1.0");
        designProcessDefinition.addActor("Workers").addUserTask("step1", "Workers");
        designProcessDefinition.addAutomaticTask("start").addUserTask("step2", "Workers")
                .addTransition("start", "step1", defaultExpression)
                .addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(),
                "Workers", user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(startProcess, "step1");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void javaMethodOperationWithPrimitiveParameters() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(
                "javaMethodOperationWithPrimitiveParameters",
                String.valueOf(System.currentTimeMillis()));
        final AutomaticTaskDefinitionBuilder task1Def = processDefinitionBuilder
                .addActor(ACTOR_NAME)
                .addData(
                        "myDatum",
                        "java.lang.StringBuilder",
                        new ExpressionBuilder().createGroovyScriptExpression("myInitGroovyScript",
                                "new java.lang.StringBuilder(\"_\")",
                                "java.lang.StringBuilder"))
                .addAutomaticTask("step1");
        task1Def.addOperation(new OperationBuilder().createJavaMethodOperation("myDatum", "append", "int",
                new ExpressionBuilder().createConstantIntegerExpression(55)));
        task1Def.addUserTask("step2", ACTOR_NAME).addTransition("step1", "step2");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(),
                ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step2Id = waitForUserTask(processInstance, "step2");
        final DataInstance activityDataInstance = getProcessAPI().getActivityDataInstance("myDatum", step2Id);

        assertEquals(StringBuilder.class, activityDataInstance.getValue().getClass());
        assertEquals("_55", activityDataInstance.getValue().toString());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeJavaOperationWithCustomType() throws Exception {
        final BusinessArchiveBuilder builder = new BusinessArchiveBuilder().createNewBusinessArchive();
        builder.addClasspathResource(getResource("/custom-0.1.jar.bak", "custom-0.1.jar"));
        builder.addClasspathResource(getResource("/org.bonitasoft.dfgdfg.bak", "org.bonitasoft.dfgdfg.jar"));

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder()
                .createNewInstance("ProcessWithCustomData", "1.0");
        designProcessDefinition.addData("adress", "org.bonitasoft.custom.Address",
                new ExpressionBuilder().createGroovyScriptExpression("create adress",
                        "new org.bonitasoft.custom.Address(\"name1\",\"Rue ampère\",\"38000\",\"Grenoble\",\"France\")",
                        "org.bonitasoft.custom.Address"));
        designProcessDefinition.addData("contact", "org.bonitasoft.dfgdfg.Contact",
                new ExpressionBuilder().createGroovyScriptExpression("create contact",
                        "new org.bonitasoft.dfgdfg.Contact()", "org.bonitasoft.dfgdfg.Contact"));
        designProcessDefinition
                .addActor("Workers")
                .addUserTask("step1", "Workers")
                .addOperation(
                        new OperationBuilder().createJavaMethodOperation("adress", "setName", String.class.getName(),
                                new ExpressionBuilder().createConstantStringExpression("myAddress")))
                .addOperation(
                        new OperationBuilder().createJavaMethodOperation("contact", "setSite", Long.class.getName(),
                                new ExpressionBuilder().createConstantLongExpression(12)));
        designProcessDefinition.addAutomaticTask("start").addUserTask("step2", "Workers")
                .addTransition("start", "step1").addTransition("step1", "step2");
        builder.setProcessDefinition(designProcessDefinition.done());
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), "Workers", user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(startProcess, "step1", user);
        waitForUserTask(startProcess, "step2");
        disableAndDeleteProcess(processDefinition);
    }

}
