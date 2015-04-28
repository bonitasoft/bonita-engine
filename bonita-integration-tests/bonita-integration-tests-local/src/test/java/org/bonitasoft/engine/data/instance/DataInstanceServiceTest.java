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
package org.bonitasoft.engine.data.instance;

import static org.bonitasoft.engine.matchers.ListContainsMatcher.namesContain;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilder;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilderFactory;
import org.bonitasoft.engine.data.definition.model.builder.SXMLDataDefinitionBuilder;
import org.bonitasoft.engine.data.definition.model.builder.SXMLDataDefinitionBuilderFactory;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.api.ParentContainerResolver;
import org.bonitasoft.engine.data.instance.api.impl.DataInstanceServiceImpl;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceNotFoundException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.SXMLDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SADataInstance;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilder;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilderFactory;
import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.ExpressionService;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilder;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Zhao Na
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @author Emmanuel Duchastenier
 */
@SuppressWarnings("javadoc")
public class DataInstanceServiceTest extends CommonBPMServicesTest {

    private static final Map<Integer, Object> EMPTY_RESOLVED_EXPRESSIONS = Collections.<Integer, Object> emptyMap();

    protected ExpressionService expressionService;

    protected DataInstanceService dataInstanceService;

    protected ParentContainerResolver parentContainerResolver;

    public DataInstanceServiceTest() {
        expressionService = getTenantAccessor().getExpressionService();
    }

    @Before
    public void setupDataInstanceService() {
        final Recorder recorder = getTenantAccessor().getRecorder();
        final ReadPersistenceService persistenceService = getTenantAccessor().getReadPersistenceService();
        final TechnicalLoggerService technicalLoggerService = getTenantAccessor().getTechnicalLoggerService();
        final ArchiveService archiveService = getTenantAccessor().getArchiveService();
        parentContainerResolver = getTenantAccessor().getParentContainerResolver();
        dataInstanceService = new DataInstanceServiceImpl(recorder, persistenceService, archiveService,
                technicalLoggerService);
        final CacheService cacheService = getTenantAccessor().getCacheService();
        if (cacheService.isStopped()) {
            try {
                cacheService.start();
            } catch (final SBonitaException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @After
    public void tearDown() {
        final CacheService cacheService = getTenantAccessor().getCacheService();
        try {
            cacheService.stop();
            cacheService.start();
        } catch (final SBonitaException e) {
            throw new RuntimeException(e);
        }
    }

    public SDataInstance buildDataInstance(final String instanceName, final String className, final String description, final String content,
            final long containerId, final String containerType, final boolean isTransient) throws SBonitaException {
        // create definition
        final SDataDefinitionBuilder dataDefinitionBuilder = BuilderFactory.get(SDataDefinitionBuilderFactory.class).createNewInstance(instanceName, className);
        initializeBuilder(dataDefinitionBuilder, description, content, className, isTransient);
        final SDataDefinition dataDefinition = dataDefinitionBuilder.done();

        // create data instance
        final SDataInstanceBuilder dataInstanceBuilder = BuilderFactory.get(SDataInstanceBuilderFactory.class).createNewInstance(dataDefinition);
        evaluateDefaultValueOf(dataDefinition, dataInstanceBuilder);
        return dataInstanceBuilder.setContainerId(containerId).setContainerType(containerType).done();
    }

    private SDataInstance buildDataInstanceConstant(final String instanceName, final String className, final String description, final String content,
            final long containerId, final String containerType, final boolean isTransient) throws SBonitaException {
        // create definition
        final SDataDefinitionBuilder dataDefinitionBuilder = BuilderFactory.get(SDataDefinitionBuilderFactory.class).createNewInstance(instanceName, className);
        initializeBuilderConstant(dataDefinitionBuilder, description, content, className, isTransient);
        final SDataDefinition dataDefinition = dataDefinitionBuilder.done();
        // create datainstance
        final SDataInstanceBuilder dataInstanceBuilder = BuilderFactory.get(SDataInstanceBuilderFactory.class).createNewInstance(dataDefinition);
        evaluateDefaultValueOf(dataDefinition, dataInstanceBuilder);
        return dataInstanceBuilder.setContainerId(containerId).setContainerType(containerType).done();
    }

    private void evaluateDefaultValueOf(final SDataDefinition dataDefinition, final SDataInstanceBuilder dataInstanceBuilder) throws SBonitaException {
        final SExpression expression = dataDefinition.getDefaultValueExpression();
        if (expression != null) {
            dataInstanceBuilder.setValue((Serializable) expressionService.evaluate(expression, Collections.<String,Object>singletonMap("processDefinitionId",546l),EMPTY_RESOLVED_EXPRESSIONS, ContainerState.ACTIVE));
        }
    }

    private SDataInstance buildLongTextDataInstance(final String instanceName, final String description, final String content, final long containerId,
            final String containerType, final Boolean isTransient) throws SBonitaException {
        // create definition
        final SDataDefinitionBuilder dataDefinitionBuilder = BuilderFactory.get(SDataDefinitionBuilderFactory.class).createNewTextData(instanceName)
                .setAsLongText(true);

        initializeBuilder(dataDefinitionBuilder, description, content, String.class.getName(), isTransient);
        final SDataDefinition dataDefinition = dataDefinitionBuilder.done();
        final SDataInstanceBuilder dataInstanceBuilder = BuilderFactory.get(SDataInstanceBuilderFactory.class).createNewInstance(dataDefinition)
                .setContainerId(containerId).setContainerType(containerType);
        // create data instance
        evaluateDefaultValueOf(dataDefinition, dataInstanceBuilder);
        return dataInstanceBuilder.done();
    }

    @Test
    public void testCreateAndRetrieveDateDataInstance() throws Exception {
        final long time = System.currentTimeMillis();
        verifyCreateAndRetrieveDataInstance("createDate", Date.class.getName(), "creates new Date", "new java.util.Date(" + time + ")", 9L, "process", false,
                new Date(time));
    }

    @Test
    public void createAndRetrieveNullDateDataInstanceShouldBeSupported() throws Exception {
        verifyCreateAndRetrieveDataInstance("createNullDate", Date.class.getName(), "creates a null Date", "null", 9L, "process", false, null);
    }

    //    private SDataInstance buildDateDataInstance(final String instanceName, final String description, final String content, final long containerId,
    //            final String containerType, final Boolean isTransient) throws SBonitaException {
    //        // create definition
    //        final SDataDefinitionBuilder dataDefinitionBuilder = BuilderFactory.get(SDataDefinitionBuilderFactory.class).createNewInstance(instanceName, Date.class.getName());
    //
    //        initializeBuilder(dataDefinitionBuilder, description, content, Date.class.getName(), isTransient);
    //        final SDataDefinition dataDefinition = dataDefinitionBuilder.done();
    //        final SDataInstanceBuilder dataInstanceBuilder = BuilderFactory.get(SDataInstanceBuilderFactory.class).createNewInstance(dataDefinition)
    //                .setContainerId(containerId).setContainerType(containerType);
    //        // create data instance
    //        evaluateDefaultValueOf(dataDefinition, dataInstanceBuilder);
    //        return dataInstanceBuilder.done();
    //    }

    private SDataInstance buildXMLDataInstance(final String instanceName, final String description, final String namespace, final String xmlElement,
            final String content, final long containerId, final String containerType) throws SBonitaException {
        // create definition
        final SDataDefinition dataDefinition = buildDataDefinition(instanceName, description, namespace, xmlElement, content, String.class.getName());

        // create data instance
        final SDataInstanceBuilder dataInstanceBuilder = BuilderFactory.get(SDataInstanceBuilderFactory.class).createNewInstance(dataDefinition)
                .setContainerId(containerId).setContainerType(containerType);
        evaluateDefaultValueOf(dataDefinition, dataInstanceBuilder);
        return dataInstanceBuilder.done();
    }

    private SDataDefinition buildDataDefinition(final String instanceName, final String description, final String namespace, final String xmlElement,
            final String content, final String defaultValueReturnType) throws SInvalidExpressionException {
        final SXMLDataDefinitionBuilder dataDefinitionBuilder = BuilderFactory.get(SXMLDataDefinitionBuilderFactory.class).createNewXMLData(instanceName)
                .setNamespace(namespace).setElement(xmlElement);
        dataDefinitionBuilder.setDescription(description);
        dataDefinitionBuilder.setTransient(false);
        SExpression expression = null;
        if (content != null) {
            // create expression
            final SExpressionBuilder expreBuilder = BuilderFactory.get(SExpressionBuilderFactory.class).createNewInstance();
            // this discrimination'll be changed.
            expreBuilder.setContent(content).setReturnType(defaultValueReturnType).setExpressionType(SExpression.TYPE_READ_ONLY_SCRIPT)
                    .setInterpreter(SExpression.GROOVY);
            expression = expreBuilder.done();
        }
        dataDefinitionBuilder.setDefaultValue(expression);
        return dataDefinitionBuilder.done();
    }

    private void initializeBuilder(final SDataDefinitionBuilder dataDefinitionBuilder, final String description, final String content,
            final String defaultValueReturnType, final boolean isTransient) throws SInvalidExpressionException {
        SExpression expression = null;
        if (content != null) {
            // create expression
            final SExpressionBuilder expreBuilder = BuilderFactory.get(SExpressionBuilderFactory.class).createNewInstance();
            // this discrimination'll be changed.
            expreBuilder.setContent(content).setReturnType(defaultValueReturnType).setExpressionType(SExpression.TYPE_READ_ONLY_SCRIPT)
                    .setInterpreter(SExpression.GROOVY);
            expression = expreBuilder.done();
        }

        dataDefinitionBuilder.setDescription(description);
        dataDefinitionBuilder.setTransient(isTransient);
        dataDefinitionBuilder.setDefaultValue(expression);
    }

    private void initializeBuilderConstant(final SDataDefinitionBuilder dataDefinitionBuilder, final String description, final String content,
            final String defaultValueReturnType, final boolean isTransient) throws SInvalidExpressionException {
        SExpression expression = null;
        if (content != null) {
            // create expression
            final SExpressionBuilder expreBuilder = BuilderFactory.get(SExpressionBuilderFactory.class).createNewInstance();
            // this discrimination'll be changed.
            expreBuilder.setContent(content).setReturnType(defaultValueReturnType).setExpressionType(SExpression.TYPE_CONSTANT);
            expression = expreBuilder.done();
        }

        dataDefinitionBuilder.setDescription(description);
        dataDefinitionBuilder.setTransient(isTransient);
        dataDefinitionBuilder.setDefaultValue(expression);
    }

    private void checkDataInstance(final SDataInstance dataInstance, final String name, final String description, final boolean isTransient,
            final String className, final Serializable value, final long containerId, final String containerType) {
        assertEquals(name, dataInstance.getName());
        assertEquals(description, dataInstance.getDescription());
        assertEquals(isTransient, dataInstance.isTransientData());
        assertEquals(className, dataInstance.getClassName());
        assertEquals(value, dataInstance.getValue());
        assertEquals(containerId, dataInstance.getContainerId());
        assertEquals(containerType, dataInstance.getContainerType());
    }

    private void checkXMLDataInstance(final SXMLDataInstance dataInstance, final String name, final String description, final boolean isTransient,
            final String className, final Serializable value, final long containerId, final String containerType, final String namespace, final String element) {
        assertEquals(namespace, dataInstance.getNamespace());
        assertEquals(element, dataInstance.getElement());
        checkDataInstance(dataInstance, name, description, isTransient, className, value, containerId, containerType);
    }

    private EntityUpdateDescriptor getUpdateDescriptor(final String description, final Serializable newValue) {
        // update data instance
        final EntityUpdateDescriptor updateDescriptor = new EntityUpdateDescriptor();
        final SDataInstanceBuilderFactory fact = BuilderFactory.get(SDataInstanceBuilderFactory.class);
        updateDescriptor.addField(fact.getValueKey(), newValue);
        updateDescriptor.addField(fact.getDescriptionKey(), description);

        return updateDescriptor;
    }

    private void insertDataInstance(final SDataInstance dataInstance) throws SBonitaException {
        getTransactionService().begin();
        // create data instance
        dataInstanceService.createDataInstance(dataInstance);
        getTransactionService().complete();
    }

    private SDataInstance getDataInstance(final long dataInstanceId) throws SBonitaException {
        getTransactionService().begin();

        final SDataInstance dataInstanceRes = dataInstanceService.getDataInstance(dataInstanceId);
        getTransactionService().complete();

        return dataInstanceRes;
    }

    private SDataInstance getDataInstanceByNameAndContainer(final String dataName, final long containerId, final String containerType) throws SBonitaException {
        getTransactionService().begin();
        // get the data instance by several conditions
        final SDataInstance dataInstanceRes = dataInstanceService.getLocalDataInstance(dataName, containerId, containerType);
        getTransactionService().complete();

        return dataInstanceRes;
    }

    private String getLongText() {
        final StringBuilder stb = new StringBuilder();
        for (int i = 0; i < 255; i++) {
            stb.append(i);
        }
        return stb.toString();
    }

    private void deleteDataInstance(final SDataInstance dataInstance) throws SBonitaException {
        getTransactionService().begin();
        dataInstanceService.deleteDataInstance(dataInstance);
        getTransactionService().complete();
    }

    private void updateDataInstance(final String dataName, final Long containerId, final String containerType, final String newDescription,
            final Serializable value) throws SBonitaException {
        getTransactionService().begin();
        // retrieve the data instance
        final SDataInstance dataInstanceRes = dataInstanceService.getLocalDataInstance(dataName, containerId, containerType);
        // update the data instance and this step must be with an activity data Instance in same transaction.
        final EntityUpdateDescriptor updateDescriptor = getUpdateDescriptor(newDescription, value);
        dataInstanceService.updateDataInstance(dataInstanceRes, updateDescriptor);
        getTransactionService().complete();
    }

    private void verifyCreateAndRetrieveDataInstance(final String name, final String classType, final String description, final String content,
            final Long containerId, final String containerType, final Boolean isTransient, final Serializable checkValue) throws Exception {
        final SDataInstance dataInstance = buildDataInstance(name, classType, description, content, containerId, containerType, isTransient);
        insertDataInstance(dataInstance);

        final SDataInstance dataInstanceRes = getDataInstanceByNameAndContainer(dataInstance.getName(), dataInstance.getContainerId(),
                dataInstance.getContainerType());
        checkDataInstance(dataInstanceRes, name, description, isTransient, classType, checkValue, containerId, containerType);

        deleteDataInstance(dataInstanceRes);
    }

    private void verifyCreateAndRetrieveDataInstanceConstant(final String name, final String classType, final String description, final String content,
            final Long containerId, final String containerType, final Boolean isTransient, final Serializable checkValue) throws Exception {
        final SDataInstance dataInstance = buildDataInstanceConstant(name, classType, description, content, containerId, containerType, isTransient);
        insertDataInstance(dataInstance);

        final SDataInstance dataInstanceRes = getDataInstanceByNameAndContainer(dataInstance.getName(), dataInstance.getContainerId(),
                dataInstance.getContainerType());
        checkDataInstance(dataInstanceRes, name, description, isTransient, classType, checkValue, containerId, containerType);

        deleteDataInstance(dataInstanceRes);
    }

    private void verifyCreateAndRetrieveDoubleDataInstance(final boolean isTransient) throws Exception {
        verifyCreateAndRetrieveDataInstance("createDouble1", Double.class.getName(), "testCreateDescriptionForDouble1", "new Double(5.0)", 6L, "process",
                isTransient, 5.0);
    }

    private void verifyCreateAndRetrieveFloatDataInstance(final boolean isTransient) throws Exception {
        verifyCreateAndRetrieveDataInstance("createFloat1", Float.class.getName(), "testCreateDescriptionForFloat1", "new Float(5.0)", 6L, "process",
                isTransient, 5.0F);
    }

    @Test
    public void getDataInstancesWithTransientAndNonTransientData() throws Exception {
        final long containerId = 4444L;
        final String containerType = "ActivityScope";
        final String instance1Name = "non-transient";
        final SDataInstance data1Instance = buildDataInstance(instance1Name, Integer.class.getName(), "some non transient data", null, containerId,
                containerType, false);
        insertDataInstance(data1Instance);
        final String instance2Name = "transient";
        final SDataInstance data2Instance = buildDataInstance(instance2Name, Integer.class.getName(), "some transient data", null, containerId, containerType,
                true);
        insertDataInstance(data2Instance);
        getTransactionService().begin();
        getTransactionService().complete();

        final List<String> dataNames = new ArrayList<String>(2);
        dataNames.add(instance1Name);
        dataNames.add(instance2Name);
        getTransactionService().begin();
        final List<SDataInstance> dataInstances = dataInstanceService.getDataInstances(dataNames, containerId, containerType, parentContainerResolver);
        getTransactionService().complete();
        assertEquals(2, dataInstances.size());
        assertThat("Not all data instances have been found", Arrays.asList(dataInstances.get(0), dataInstances.get(1)),
                namesContain(instance1Name, instance2Name));
    }

    @Test
    public void getSADataInstancesWithEmptyList() throws Exception {
        getTransactionService().begin();
        final List<SADataInstance> dataInstances = dataInstanceService.getSADataInstances(13544L, "dummyContainerType", parentContainerResolver, Arrays.<String> asList(), 1111111L);
        getTransactionService().complete();
        assertEquals(0, dataInstances.size());
    }

    @Test
    public void createDataWithComplexName() throws Exception {
        final SDataInstance dataInstance = buildDataInstance("FirstLetterUpperCase", Integer.class.getName(), null, "987456321", 4444L, "ActivityScope", false);
        insertDataInstance(dataInstance);
        assertTrue(0 != dataInstance.getId());

        final SDataInstance dataInstance2 = buildDataInstance("var with spaces", Double.class.getName(), null, "221d", 4654L, "DummyScopeType", false);
        insertDataInstance(dataInstance2);
        assertTrue(0 != dataInstance2.getId());
    }

    @Test
    public void testCreateAndRetrieveDoubleInstance() throws Exception {
        verifyCreateAndRetrieveDoubleDataInstance(false);
    }

    @Test
    public void testCreateAndRetrieveDoubleInstanceTransient() throws Exception {
        verifyCreateAndRetrieveDoubleDataInstance(true);
    }

    @Test
    public void testCreateAndRetrieveFloatInstance() throws Exception {
        verifyCreateAndRetrieveFloatDataInstance(false);
    }

    @Test
    public void testCreateAndRetrieveFloatInstanceTransient() throws Exception {
        verifyCreateAndRetrieveFloatDataInstance(true);
    }

    private void verifyCreateAndRetrieveBooleanDataInstance(final boolean isTransient) throws Exception {
        verifyCreateAndRetrieveDataInstance("createBoolean1", Boolean.class.getName(), "testCreateDescriptionForBoolean1", "true", 6L, "process", isTransient,
                true);
    }

    @Test
    public void testCreateAndRetrieveBooleanInstance() throws Exception {
        verifyCreateAndRetrieveBooleanDataInstance(false);
    }

    @Test
    public void testCreateAndRetrieveBooleanInstanceTransient() throws Exception {
        verifyCreateAndRetrieveBooleanDataInstance(true);
    }

    @Test
    public void testCreateAndRetrieveIntegerInstance() throws Exception {
        verifyCreateAndRetrieveDataInstance("createInteger", Integer.class.getName(), "testCreateDescription", "1+2", 7L, "process", false, 3);
    }

    @Test
    public void testCreateAndRetrieveIntegerInstanceTransient() throws Exception {
        verifyCreateAndRetrieveDataInstance("createInteger", Integer.class.getName(), "testCreateDescription", "1+2", 7L, "process", true, 3);
    }

    @Test
    public void testCreateAndRetrieveLongInstance() throws Exception {
        verifyCreateAndRetrieveDataInstance("createLong", Long.class.getName(), "testCreateDescription", "new Long(2)", 8L, "process", false, 2L);
    }

    @Test
    public void testCreateAndRetrieveLongInstanceTransient() throws Exception {
        verifyCreateAndRetrieveDataInstance("createLong", Long.class.getName(), "testCreateDescription", "new Long(2)", 8L, "process", true, 2L);
    }

    @Test
    public void testCreateAndRetrieveShortTextInstance() throws Exception {
        verifyCreateAndRetrieveDataInstance("createString", String.class.getName(), "testCreateDescription", "'test123'", 9L, "process", false, "test123");
    }

    @Test
    public void testCreateAndRetrieveShortTextInstanceTransient() throws Exception {
        verifyCreateAndRetrieveDataInstance("createString", String.class.getName(), "testCreateDescription", "'test123'", 9L, "process", true, "test123");
    }

    private void verifyCreateAndRetrieveLongTextData(final boolean isTransient) throws Exception {

        final String longText = getLongText();

        final SDataInstance longTextDataInstance = buildLongTextDataInstance("longTextData", "A very long text", getStringForExpression(longText), 11L,
                "process", isTransient);
        insertDataInstance(longTextDataInstance);

        final SDataInstance dataInstance = getDataInstanceByNameAndContainer(longTextDataInstance.getName(), longTextDataInstance.getContainerId(),
                longTextDataInstance.getContainerType());
        checkDataInstance(dataInstance, "longTextData", "A very long text", isTransient, String.class.getName(), longText, 11L, "process");

        deleteDataInstance(dataInstance);
    }

    @Test
    public void testCreateAndRetrieveLongTextData() throws Exception {
        verifyCreateAndRetrieveLongTextData(false);
    }

    @Test
    public void testCreateAndRetrieveLongTextDataTransient() throws Exception {
        verifyCreateAndRetrieveLongTextData(true);
    }

    @Test
    public void testCreateAndRetrieveBlobDataInstanceWithoutDefaultValue() throws Exception {
        verifyCreateAndRetrieveDataInstance("blobTextData", LightEmployee.class.getName(), "A custom java object as blob",
                "return new org.bonitasoft.engine.data.instance.LightEmployee(\"manu\", \"duch\", 35)", 15L, "process", false, new LightEmployee("manu",
                        "duch", 35));
    }

    @Test
    public void testCreateAndRetrieveBlobDataInstanceWithoutDefaultValueTransient() throws Exception {
        verifyCreateAndRetrieveDataInstance("blobTextData", LightEmployee.class.getName(), "A custom java object as blob",
                "return new org.bonitasoft.engine.data.instance.LightEmployee(\"manu\", \"duch\", 53)", 15L, "process", true, new LightEmployee("manu", "duch",
                        53));
    }

    @Test
    public void testCreateBooleanInstanceWithoutDefaultValue() throws Exception {
        verifyCreateAndRetrieveDataInstanceConstant("createBoolean2", Boolean.class.getName(), "testCreateDescriptionForBoolean2", "true", 11L, "process",
                false, true);
    }

    @Test
    public void testCreateBooleanInstanceWithoutDefaultValueTransient() throws Exception {
        verifyCreateAndRetrieveDataInstanceConstant("createBoolean2", Boolean.class.getName(), "testCreateDescriptionForBoolean2", "false", 11L, "process",
                true, false);
    }

    @Test
    public void testCreateDoubleInstanceWithoutDefaultValue() throws Exception {
        verifyCreateAndRetrieveDataInstanceConstant("createDouble2", Double.class.getName(), "testCreateDescriptionForDouble2", "37.0", 12L, "task", false,
                37.0);
    }

    @Test
    public void testCreateDoubleInstanceWithoutDefaultValueTransient() throws Exception {
        verifyCreateAndRetrieveDataInstanceConstant("createDouble2", Double.class.getName(), "testCreateDescriptionForDouble2", "73.0", 12L, "task", true, 73.0);
    }

    @Test
    public void testCreateFloatInstanceWithoutDefaultValue() throws Exception {
        verifyCreateAndRetrieveDataInstanceConstant("createFloat2", Float.class.getName(), "testCreateDescriptionForFloat2", "34.F", 12L, "task", false, 34.F);
    }

    @Test
    public void testCreateFloatInstanceWithoutDefaultValueTransient() throws Exception {
        verifyCreateAndRetrieveDataInstanceConstant("createFloat2", Float.class.getName(), "testCreateDescriptionForFloat2", "43.F", 12L, "task", true, 43.F);
    }

    @Test
    public void testCreateIntegerInstanceWithoutDefaultValue() throws Exception {
        verifyCreateAndRetrieveDataInstanceConstant("createInteger2", Integer.class.getName(), "testCreateDescriptionForInteger2", "32", 13L, "task1", false,
                32);
    }

    @Test
    public void testCreateIntegerInstanceWithoutDefaultValueTransient() throws Exception {
        verifyCreateAndRetrieveDataInstanceConstant("createInteger2", Integer.class.getName(), "testCreateDescriptionForInteger2", "23", 13L, "task1", true, 23);
    }

    @Test
    public void testCreateLongInstanceWithoutDefaultValue() throws Exception {
        verifyCreateAndRetrieveDataInstanceConstant("createLong2", Long.class.getName(), "testCreateDescriptionForLong2", "47", 14L, "task2", false, 47L);
    }

    @Test
    public void testCreateLongInstanceWithoutDefaultValueTransient() throws Exception {
        verifyCreateAndRetrieveDataInstanceConstant("createLong2", Long.class.getName(), "testCreateDescriptionForLong2", "74", 14L, "task2", true, 74L);
    }

    @Test
    public void testCreateShortTextInstanceWithoutDefaultValue() throws Exception {
        verifyCreateAndRetrieveDataInstanceConstant("createString2", String.class.getName(), "testCreateDescriptionForString2", "strTest", 15L, "task3", false,
                "strTest");
    }

    @Test
    public void testCreateShortTextInstanceWithoutDefaultValueTransient() throws Exception {
        verifyCreateAndRetrieveDataInstanceConstant("createString2", String.class.getName(), "testCreateDescriptionForString2", "strTest2", 15L, "task3", true,
                "strTest2");
    }

    private void verifyDeleteDataInstance(final String name, final String classType, final String description, final String content, final Long containerId,
            final String containerType, final Boolean isTransient, final Serializable checkValue, final String assertContent) throws Exception {
        final SDataInstance dataInstance = buildDataInstance(name, classType, description, content, containerId, containerType, isTransient);
        insertDataInstance(dataInstance);

        final SDataInstance dataInstanceRes = getDataInstance(dataInstance.getId());
        assertNotNull(dataInstanceRes);
        checkDataInstance(dataInstanceRes, name, description, isTransient, classType, checkValue, containerId, containerType);

        deleteDataInstance(dataInstance);

        final SDataInstance dataDeletedRes = getDataInstance(dataInstance.getId());
        assertNull(assertContent, dataDeletedRes);
    }

    @Test(expected = SDataInstanceNotFoundException.class)
    public void testDeleteBooleanInstance() throws Exception {
        verifyDeleteDataInstance("deleteBoolean1", Boolean.class.getName(), "testDeleteDescriptionForBoolean", "true", 1L, "task", false, true,
                "the Boolean instance was not deleted");
    }

    @Test(expected = SDataInstanceNotFoundException.class)
    public void testDeleteTransientBooleanInstance() throws Exception {
        verifyDeleteDataInstance("deleteBoolean1", Boolean.class.getName(), "testDeleteDescriptionForBoolean", "true", 1L, "task", true, true,
                "the Boolean instance was not deleted");
    }

    @Test(expected = SDataInstanceNotFoundException.class)
    public void testDeleteDoubleInstance() throws Exception {
        verifyDeleteDataInstance("deleteDouble1", Double.class.getName(), "testDeleteDescriptionForDouble", "1.23D", 1L, "task", false, 1.23D,
                "the Double instance was not deleted");
    }

    @Test(expected = SDataInstanceNotFoundException.class)
    public void testDeleteFloatInstance() throws Exception {
        verifyDeleteDataInstance("deleteFloat1", Float.class.getName(), "testDeleteDescriptionForFloat", "1.23F", 1L, "task", false, 1.23F,
                "the Float instance was not deleted");
    }

    @Test(expected = SDataInstanceNotFoundException.class)
    public void testTransientDeleteDoubleInstance() throws Exception {
        verifyDeleteDataInstance("deleteDouble1", Double.class.getName(), "testDeleteDescriptionForDouble", "1.23D", 1L, "task", true, 1.23D,
                "the Double instance was not deleted");
    }

    @Test(expected = SDataInstanceNotFoundException.class)
    public void testTransientDeleteFloatInstance() throws Exception {
        verifyDeleteDataInstance("deleteFloat1", Float.class.getName(), "testDeleteDescriptionForFloat", "1.23F", 1L, "task", true, 1.23F,
                "the Float instance was not deleted");
    }

    @Test(expected = SDataInstanceNotFoundException.class)
    public void testDeleteIntegerInstance() throws Exception {
        verifyDeleteDataInstance("deleteInteger1", Integer.class.getName(), "testDeleteDescriptionForInteger", "8+4", 1L, "task", false, 12,
                "the Integer instance was not deleted");
    }

    @Test(expected = SDataInstanceNotFoundException.class)
    public void testTransientDeleteIntegerInstance() throws Exception {
        verifyDeleteDataInstance("deleteInteger1", Integer.class.getName(), "testDeleteDescriptionForInteger", "8+4", 1L, "task", true, 12,
                "the Integer instance was not deleted");
    }

    @Test(expected = SDataInstanceNotFoundException.class)
    public void testDeleteLongInstance() throws Exception {
        verifyDeleteDataInstance("deleteLong1", Long.class.getName(), "testDeleteDescriptionForLong", "12L", 1L, "task", false, 12L,
                "the Long instance was not deleted");
    }

    @Test(expected = SDataInstanceNotFoundException.class)
    public void testTransientDeleteLongInstance() throws Exception {
        verifyDeleteDataInstance("deleteLong1", Long.class.getName(), "testDeleteDescriptionForLong", "12L", 1L, "task", true, 12L,
                "the Long instance was not deleted");
    }

    @Test(expected = SDataInstanceNotFoundException.class)
    public void testDeleteShortTextInstance() throws Exception {
        verifyDeleteDataInstance("deleteString1", String.class.getName(), "testDeleteDescriptionForString", "'test123'", 1L, "task", false, "test123",
                "the Short Text instance was not deleted");
    }

    @Test(expected = SDataInstanceNotFoundException.class)
    public void testTransientDeleteShortTextInstance() throws Exception {
        verifyDeleteDataInstance("deleteString1", String.class.getName(), "testDeleteDescriptionForString", "'test123'", 1L, "task", true, "test123",
                "the Short Text instance was not deleted");
    }

    @Test(expected = SDataInstanceNotFoundException.class)
    public void testDeleteBlobInstance() throws Exception {
        final LightEmployee employee = new LightEmployee("firstName", "lastName", 30);
        verifyDeleteDataInstance("blobData", LightEmployee.class.getName(), "A custom java object",
                "new org.bonitasoft.engine.data.instance.LightEmployee(\"firstName\", \"lastName\", 30)", 11L, "processTask", false, employee,
                "the blob data instance was not deleted");
    }

    @Test(expected = SDataInstanceNotFoundException.class)
    public void testTransientDeleteBlobInstance() throws Exception {
        final LightEmployee employee = new LightEmployee("firstName", "lastName", 30);
        verifyDeleteDataInstance("blobData", LightEmployee.class.getName(), "A custom java object",
                "new org.bonitasoft.engine.data.instance.LightEmployee(\"firstName\", \"lastName\", 30)", 11L, "processTask", true, employee,
                "the blob data instance was not deleted");
    }

    private void verifyDeleteLongTextData(final Boolean isTransient) throws Exception {
        final String longText = getLongText();

        final SDataInstance longTextDataInstance = buildLongTextDataInstance("longTextData", "A very long text", getStringForExpression(longText), 0, null,
                isTransient);
        insertDataInstance(longTextDataInstance);

        final SDataInstance dataInstance = getDataInstance(longTextDataInstance.getId());
        assertNotNull(dataInstance);

        deleteDataInstance(dataInstance);

        final SDataInstance dataInstanceRes = getDataInstance(longTextDataInstance.getId());
        assertNull("the Long Text instance was not deleted", dataInstanceRes);
    }

    @Test(expected = SDataInstanceNotFoundException.class)
    public void testDeleteLongTextData() throws Exception {
        verifyDeleteLongTextData(false);
    }

    @Test(expected = SDataInstanceNotFoundException.class)
    public void testTransientDeleteLongTextData() throws Exception {
        verifyDeleteLongTextData(true);
    }

    private long verifyUpdateDataInstance(final String name, final String classType, final String description, final String content, final Long containerId,
            final String containerType, final Boolean isTransient, final String updateDescription, final Serializable updateValue) throws Exception {
        final SDataInstance dataInstance = buildDataInstance(name, classType, description, content, containerId, containerType, isTransient);
        insertDataInstance(dataInstance);
        updateDataInstance(dataInstance.getName(), dataInstance.getContainerId(), dataInstance.getContainerType(), updateDescription, updateValue);

        final long dataInstanceId = dataInstance.getId();
        final SDataInstance dataInstanceRes = getDataInstance(dataInstanceId);
        checkDataInstance(dataInstanceRes, name, updateDescription, isTransient, classType, updateValue, containerId, containerType);
        deleteDataInstance(dataInstanceRes);
        return dataInstanceId;

    }

    @Test
    public void testUpdateBooleanInstance() throws Exception {
        verifyUpdateDataInstance("updateBoolean", Boolean.class.getName(), "testUpdateDescription", "true", 11L, "miniTask", false,
                "testUpdateDescription123456", false);
    }

    @Test
    public void testTransientUpdateBooleanInstance() throws Exception {
        verifyUpdateDataInstance("updateBoolean", Boolean.class.getName(), "testUpdateDescription", "true", 11L, "miniTask", true,
                "testUpdateDescription123456", false);
    }

    @Test
    public void testUpdateDoubleInstance() throws Exception {
        verifyUpdateDataInstance("updateDouble", Double.class.getName(), "testUpdateDescription", "new Double(5.0)", 11L, "miniTask", false,
                "testUpdateDescription4", 7.0);
    }

    @Test
    public void testTransientUpdateDoubleInstance() throws Exception {
        verifyUpdateDataInstance("updateDouble", Double.class.getName(), "testUpdateDescription", "new Double(5.0)", 11L, "miniTask", true,
                "testUpdateDescription4", 7.0);
    }

    @Test
    public void testUpdateFloatInstance() throws Exception {
        verifyUpdateDataInstance("updateFloat", Float.class.getName(), "testUpdateDescription", "new Float(5.0)", 11L, "miniTask", false,
                "testUpdateDescription4", 7.0F);
    }

    @Test
    public void testTransientUpdateFloatInstance() throws Exception {
        verifyUpdateDataInstance("updateFloat", Float.class.getName(), "testUpdateDescription", "new Float(5.0)", 11L, "miniTask", true,
                "testUpdateDescription4", 7.0F);
    }

    @Test
    public void testUpdateIntegerInstance() throws Exception {
        verifyUpdateDataInstance("updateInteger", Integer.class.getName(), "testUpdateDescription", "new Integer(5)", 11L, "miniTask", false,
                "testUpdateDescription2", 8);
    }

    @Test
    public void testTransientUpdateIntegerInstance() throws Exception {
        verifyUpdateDataInstance("updateInteger", Integer.class.getName(), "testUpdateDescription", "new Integer(5)", 11L, "miniTask", true,
                "testUpdateDescription2", 8);
    }

    @Test
    public void testUpdateLongInstance() throws Exception {
        verifyUpdateDataInstance("updateLong", Long.class.getName(), "testUpdateDescription", "new Long(5)", 11L, "miniTask", false, "testUpdateDescription2",
                6L);
    }

    @Test
    public void testTransientUpdateLongInstance() throws Exception {
        verifyUpdateDataInstance("updateLong", Long.class.getName(), "testUpdateDescription", "new Long(5)", 11L, "miniTask", true, "testUpdateDescription2",
                6L);
    }

    @Test
    public void testUpdateShortTextInstance() throws Exception {
        verifyUpdateDataInstance("updateShortText", String.class.getName(), "testUpdateDescription", "'123qwe'", 11L, "miniTask", false,
                "testUpdateDescription2", "123qwe2");
    }

    @Test
    public void testTransientUpdateShortTextInstance() throws Exception {
        verifyUpdateDataInstance("updateShortText", String.class.getName(), "testUpdateDescription", "'123qwe'", 11L, "miniTask", true,
                "testUpdateDescription2", "123qwe2");
    }

    private void verifyUpdateLongTextDataInstance(final String name, final String classType, final String description, final String content,
            final Long containerId, final String containerType, final Boolean isTransient, final String updateDescription, final Serializable updateValue)
            throws Exception {
        final SDataInstance dataInstance = buildLongTextDataInstance(name, description, content, containerId, containerType, isTransient);
        insertDataInstance(dataInstance);

        updateDataInstance(dataInstance.getName(), dataInstance.getContainerId(), dataInstance.getContainerType(), updateDescription, updateValue);

        final SDataInstance dataInstanceRes = getDataInstance(dataInstance.getId());
        checkDataInstance(dataInstanceRes, name, updateDescription, isTransient, classType, updateValue, containerId, containerType);
        deleteDataInstance(dataInstanceRes);// dataInstance has no id here.
    }

    @Test
    public void testUpdateLongTextData() throws Exception {
        final String longText = getLongText();
        verifyUpdateLongTextDataInstance("longTextData", String.class.getName(), "A very long text", getStringForExpression(longText), 11L, "processTask",
                false, "Updated description for long text", longText + "Updated");
    }

    @Test
    public void testTransientUpdateLongTextData() throws Exception {
        final String longText = getLongText();
        verifyUpdateLongTextDataInstance("longTextData", String.class.getName(), "A very long text", getStringForExpression(longText), 11L, "processTask",
                true, "Updated description for long text", longText + "Updated");
    }

    @Test
    public void testUpdateBlobDataInstance() throws Exception {
        final LightEmployee employee = new LightEmployee("firstName", "lastName", 30);
        verifyUpdateDataInstance("blobTextData", LightEmployee.class.getName(), "A custom java object", null, 11L, "processTask", false,
                "A custom java updated", employee);
    }

    @Test
    public void testTransientUpdateBlobDataInstance() throws Exception {
        final LightEmployee employee = new LightEmployee("firstName", "lastName", 30);
        verifyUpdateDataInstance("blobTextData", LightEmployee.class.getName(), "A custom java object", null, 11L, "processTask", true,
                "A custom java updated", employee);
    }

    private void verifyCreateAndGetDataInstance(final String name, final String classType, final String description, final String content,
            final Long containerId, final String containerType, final Boolean isTransient, final Serializable checkValue) throws Exception {
        final SDataInstance dataInstance = buildDataInstance(name, classType, description, content, containerId, containerType, isTransient);
        insertDataInstance(dataInstance);

        final SDataInstance dataInstanceRes = getDataInstanceByNameAndContainer(dataInstance.getName(), dataInstance.getContainerId(),
                dataInstance.getContainerType());
        checkDataInstance(dataInstanceRes, name, description, isTransient, classType, checkValue, containerId, containerType);

        deleteDataInstance(dataInstanceRes);
    }

    @Test
    public void testGetDataIntegerInstance() throws Exception {
        verifyCreateAndGetDataInstance("getInteger", Integer.class.getName(), "testgetDescription", "new Integer(2)", 20L, "processInstance", false, 2);
    }

    @Test
    public void testGetTransientIntegerDataInstance() throws Exception {
        verifyCreateAndGetDataInstance("getInteger", Integer.class.getName(), "testgetDescription", "new Integer(2)", 20L, "processInstance", true, 2);
    }

    @Test
    public void testGetDataBooleanInstance() throws Exception {
        verifyCreateAndGetDataInstance("getBoolean", Boolean.class.getName(), "testgetDescription", "true", 20L, "processInstance", false, true);
    }

    @Test
    public void testGetTransientBooleanDataInstance() throws Exception {
        verifyCreateAndGetDataInstance("getBoolean", Boolean.class.getName(), "testgetDescription", "true", 20L, "processInstance", true, true);
    }

    @Test
    public void testGetDataShortTextInstance() throws Exception {
        verifyCreateAndGetDataInstance("getShortText", String.class.getName(), "testgetDescription", "'1122aabb'", 20L, "processInstance", false, "1122aabb");
    }

    @Test
    public void testGetTransientShortTextDataInstance() throws Exception {
        verifyCreateAndGetDataInstance("getShortText", String.class.getName(), "testgetDescription", "'1122aabb'", 20L, "processInstance", true, "1122aabb");
    }

    @Test
    public void testGetLongDataInstance() throws Exception {
        verifyCreateAndGetDataInstance("getLong", Long.class.getName(), "testgetDescription", "new Long(2)", 20L, "processInstance", false, 2L);
    }

    @Test
    public void testGetTransientLongDataInstance() throws Exception {
        verifyCreateAndGetDataInstance("getLong", Long.class.getName(), "testgetDescription", "new Long(2)", 20L, "processInstance", true, 2L);
    }

    @Test
    public void testGetDataDoubleInstance() throws Exception {
        verifyCreateAndGetDataInstance("getDouble", Double.class.getName(), "testgetDescription", "new Double(2.0)", 20L, "processInstance", false, 2.0);
    }

    @Test
    public void testGetTransientDoubleDataInstance() throws Exception {
        verifyCreateAndGetDataInstance("getDouble", Double.class.getName(), "testgetDescription", "new Double(2.0)", 20L, "processInstance", true, 2.0);
    }

    @Test
    public void testGetDataFloatInstance() throws Exception {
        verifyCreateAndGetDataInstance("getFloat", Float.class.getName(), "testgetDescription", "new Float(2.0)", 20L, "processInstance", false, 2.0F);
    }

    @Test
    public void testGetTransientFloatDataInstance() throws Exception {
        verifyCreateAndGetDataInstance("getFloat", Float.class.getName(), "testgetDescription", "new Float(2.0)", 20L, "processInstance", true, 2.0F);
    }

    @Test
    public void testGetLongTextData() throws Exception {
        verifyCreateAndRetrieveLongTextData(false);
    }

    @Test
    public void testGetLongTextDataTransient() throws Exception {
        verifyCreateAndRetrieveLongTextData(true);
    }

    @Test
    public void testGetBlobData() throws Exception {
        final LightEmployee employee = new LightEmployee("firstName", "lastName", 30);
        verifyCreateAndGetDataInstance("blobTextData", LightEmployee.class.getName(), "A custom java object2",
                "new org.bonitasoft.engine.data.instance.LightEmployee(\"firstName\", \"lastName\", 30)", 3L, "processTaskDefinition", false, employee);
    }

    @Test
    public void testGetBlobDataTransient() throws Exception {
        final LightEmployee employee = new LightEmployee("firstName", "lastName", 30);
        verifyCreateAndGetDataInstance("blobTextData", LightEmployee.class.getName(), "A custom java object3",
                "new org.bonitasoft.engine.data.instance.LightEmployee(\"firstName\", \"lastName\", 30)", 11L, "processTask", true, employee);
    }

    @Test
    public void testCreateAndRetrieveXMLDataById() throws Exception {
        final String xmlContent = buildSimpleXML1();

        final long containerId = 15;
        final String containerType = "ActivityInstance";

        final SDataInstance dataInstance = buildXMLDataInstance("xmlVar", "This is a xml variable",
                "org.bonitasoft.engine.data.instance.model.impl.SDataInstanceImpl", "xmlElement", getStringForExpression(xmlContent), containerId,
                containerType);
        insertDataInstance(dataInstance);

        // get the data instance by several conditions
        final SDataInstance dataInstanceRes = getDataInstance(dataInstance.getId());
        assertTrue(dataInstanceRes instanceof SXMLDataInstance);
        checkXMLDataInstance((SXMLDataInstance) dataInstanceRes, "xmlVar", "This is a xml variable", false, String.class.getName(), xmlContent, containerId,
                containerType, "org.bonitasoft.engine.data.instance.model.impl.SDataInstanceImpl", "xmlElement");

        deleteDataInstance(dataInstanceRes);
    }

    private String getStringForExpression(final String str) {
        return "'" + str + "'";
    }

    private String buildSimpleXML1() {
        final StringBuilder stb = new StringBuilder();
        stb.append("<root>");
        stb.append("<child>");
        stb.append("value");
        stb.append("</child>");
        stb.append("</root>");
        final String xmlContent = stb.toString();
        return xmlContent;
    }

    private String buildSimpleXML2() {
        final StringBuilder stb = new StringBuilder();
        stb.append("<root>");
        stb.append("<child/>");
        stb.append("</root>");
        final String xmlContent = stb.toString();
        return xmlContent;
    }

    @Test
    public void testUpdateXMLData() throws Exception {
        final String xmlContent = buildSimpleXML1();
        final long containerId = 15;
        final String containerType = "ActivityInstance";
        final SDataInstance dataInstance = buildXMLDataInstance("xmlVar", "This is a xml variable",
                "org.bonitasoft.engine.data.instance.model.impl.SDataInstanceImpl", "xmlElement", getStringForExpression(xmlContent), containerId,
                containerType);
        insertDataInstance(dataInstance);
        // get the data instance by several conditions
        SDataInstance dataInstanceRes = getDataInstance(dataInstance.getId());
        assertTrue(dataInstanceRes instanceof SXMLDataInstance);
        checkXMLDataInstance((SXMLDataInstance) dataInstanceRes, "xmlVar", "This is a xml variable", false, String.class.getName(), xmlContent, containerId,
                containerType, "org.bonitasoft.engine.data.instance.model.impl.SDataInstanceImpl", "xmlElement");
        final String updatedContent = buildSimpleXML2();
        updateDataInstance(dataInstanceRes.getName(), dataInstanceRes.getContainerId(), dataInstanceRes.getContainerType(),
                "This is a xml variable after update", updatedContent);
        dataInstanceRes = getDataInstance(dataInstance.getId());
        checkXMLDataInstance((SXMLDataInstance) dataInstanceRes, "xmlVar", "This is a xml variable after update", false, String.class.getName(),
                updatedContent, containerId, containerType, "org.bonitasoft.engine.data.instance.model.impl.SDataInstanceImpl", "xmlElement");
        deleteDataInstance(dataInstanceRes);
    }

    @Test(expected = SDataInstanceNotFoundException.class)
    public void testDeleteXMLData() throws Exception {
        final String xmlContent = buildSimpleXML1();

        final long containerId = 15;
        final String containerType = "ActivityInstance";

        final SDataInstance dataInstance = buildXMLDataInstance("xmlVar", "This is a xml variable",
                "org.bonitasoft.engine.data.instance.model.impl.SDataInstanceImpl", "xmlElement", getStringForExpression(xmlContent), containerId,
                containerType);
        insertDataInstance(dataInstance);

        // get the data instance by several conditions
        final SDataInstance dataInstanceRes = getDataInstance(dataInstance.getId());
        assertTrue(dataInstanceRes instanceof SXMLDataInstance);
        checkXMLDataInstance((SXMLDataInstance) dataInstanceRes, "xmlVar", "This is a xml variable", false, String.class.getName(), xmlContent, containerId,
                containerType, "org.bonitasoft.engine.data.instance.model.impl.SDataInstanceImpl", "xmlElement");

        deleteDataInstance(dataInstanceRes);

        getDataInstance(dataInstance.getId());
    }

    @Test
    public void testRetrieveXMLDataByNameAndContainer() throws Exception {
        final String xmlContent = buildSimpleXML1();

        final long containerId = 16;
        final String containerType = "ActivityInstance";

        final SDataInstance dataInstance = buildXMLDataInstance("xmlVar", "This is a xml variable",
                "org.bonitasoft.engine.data.instance.model.impl.SDataInstanceImpl", "xmlElement", getStringForExpression(xmlContent), containerId,
                containerType);
        insertDataInstance(dataInstance);

        // get the data instance by several conditions
        final SDataInstance dataInstanceRes = getDataInstanceByNameAndContainer("xmlVar", containerId, containerType);
        assertTrue(dataInstanceRes instanceof SXMLDataInstance);
        checkXMLDataInstance((SXMLDataInstance) dataInstanceRes, "xmlVar", "This is a xml variable", false, String.class.getName(), xmlContent, containerId,
                containerType, "org.bonitasoft.engine.data.instance.model.impl.SDataInstanceImpl", "xmlElement");

        deleteDataInstance(dataInstanceRes);
    }

    @Test
    public void testGetSADataInstance() throws Exception {
        final String classType = Integer.class.getName();
        final SDataInstance dataInstance = buildDataInstance("updateInteger", classType, "testUpdateDescription", "new Integer(5)", 111l, "miniTask", false);
        insertDataInstance(dataInstance);
        Thread.sleep(10);

        final long beforeUpdateDate = System.currentTimeMillis();
        Thread.sleep(10);
        updateDataInstance(dataInstance.getName(), dataInstance.getContainerId(), dataInstance.getContainerType(), "testUpdateDescription2", 8);

        final SDataInstance dataInstanceRes = getDataInstance(dataInstance.getId());
        checkDataInstance(dataInstanceRes, "updateInteger", "testUpdateDescription2", false, classType, 8, 111l, "miniTask");
        deleteDataInstance(dataInstanceRes);
        final long dataInstanceId = dataInstance.getId();

        getTransactionService().begin();
        try {
            final SADataInstance beforeUpdate = dataInstanceService.getSADataInstance(dataInstanceId, beforeUpdateDate);
            assertEquals(5, beforeUpdate.getValue());
            assertTrue(beforeUpdate.getArchiveDate() <= beforeUpdateDate);

            final SADataInstance afterUpdate = dataInstanceService.getSADataInstance(dataInstanceId, System.currentTimeMillis());
            assertEquals(8, afterUpdate.getValue());
            assertTrue(afterUpdate.getArchiveDate() <= System.currentTimeMillis() && afterUpdate.getArchiveDate() >= beforeUpdateDate);
        } finally {
            getTransactionService().complete();
        }
    }
}
