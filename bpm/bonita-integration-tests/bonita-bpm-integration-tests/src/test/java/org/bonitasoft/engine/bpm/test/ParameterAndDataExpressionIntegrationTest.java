package org.bonitasoft.engine.bpm.test;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.impl.LoginAPIImpl;
import org.bonitasoft.engine.api.impl.ProcessAPIImpl;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.model.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.model.ProcessDefinition;
import org.bonitasoft.engine.bpm.model.ProcessDefinitionBuilder;
import org.bonitasoft.engine.commons.CollectionUtil;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.data.DataService;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilders;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilders;
import org.bonitasoft.engine.data.model.builder.SDataSourceBuilder;
import org.bonitasoft.engine.expression.ExpressionService;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilder;
import org.bonitasoft.engine.parameter.SParameter;
import org.bonitasoft.engine.parameter.propertyfile.SParameterImpl;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.test.util.PlatformUtil;
import org.bonitasoft.engine.test.util.TestUtil;
import org.bonitasoft.engine.transaction.BusinessTransaction;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import com.bonitasoft.engine.api.impl.LoginAPIExt;

public class ParameterAndDataExpressionIntegrationTest extends CommonBPMServicesSPTest {

    private static final Map<Integer, Serializable> EMPTY_RESOLVED_EXPRESSIONS = Collections.<Integer, Serializable> emptyMap();

    private static APISession sSession;

    private final ProcessAPIImpl processAPIImpl = new ProcessAPIImpl();

    @After
    public void tearDownPersistence() throws Exception {
        final BusinessTransaction btx = getTransactionService().createTransaction();
        btx.begin();
        final long tenantId = PlatformUtil.getDefaultTenantId(getPlatformService());
        btx.complete();
        sSession = new LoginAPIExt().login(tenantId, TestUtil.getDefaultUserName(), TestUtil.getDefaultPassword());
        getSessionAccessor().setSessionInfo(sSession.getId(), sSession.getTenantId());

        new LoginAPIImpl().logout(sSession);
    }

    protected static SessionAccessor getSessionAccessor() {
        return getServicesBuilder().getSessionAccessor();
    }

    protected static PlatformService getPlatformService() {
        return getServicesBuilder().getPlatformService();
    }

    protected ExpressionService getExpressionService() {
        return getServicesBuilder().getExpressionService();
    }

    protected SExpressionBuilder getExpressionBuilder() {
        return getServicesBuilder().getExpressionBuilders().getExpressionBuilder();
    }

    // protected ParameterService getParameterService() {
    // return getServicesBuilder().getParameterService();
    // }

    protected ProcessDefinitionService getProcessDefinitionService() {
        return getServicesBuilder().getProcessDefinitionService();
    }

    protected SDataDefinitionBuilders getSDataDefinitionBuilders() {
        return getServicesBuilder().geterSDataDefinitionBuilders();
    }

    protected SDataInstanceBuilders getSDataInstanceBuilders() {
        return getServicesBuilder().geterSDataInstanceBuilder();
    }

    protected DataInstanceService getDataInstanceService() {
        return getServicesBuilder().getDataInstanceService();
    }

    protected static TransactionService getTransactionService() {
        return getServicesBuilder().getTransactionService();
    }

    protected static SDataSourceBuilder getDataSourceBuilder() {
        return getServicesBuilder().getDataSourceModelBuilder();
    }

    protected static DataService getDataService() {
        return getServicesBuilder().getDataService();
    }

    protected String getParameterClassName() {
        return String.class.getName();
    }

    protected SParameter createParameter(final String name, final String value) {
        return new SParameterImpl(name, value);
    }

    private SExpression newExpression(final String content, final String expressionType, final String returnType, final String interpreter,
            final List<SExpression> dependencies) throws SInvalidExpressionException {
        final SExpressionBuilder eb = getExpressionBuilder().createNewInstance();
        eb.setContent(content);
        eb.setExpressionType(expressionType);
        eb.setInterpreter(interpreter);
        eb.setReturnType(returnType);
        eb.setDependencies(dependencies);
        return eb.done();
    }

    private Serializable createAndEvaluateParameterExpression(final String nameParameter, final Long deployId, final String key) throws Exception {
        final SExpression strExpr = newExpression(nameParameter, SExpression.TYPE_PARAMETER, String.class.getName(), null, null);
        final Map<String, Object> dependencies = CollectionUtil.buildSimpleMap(key, deployId);
        return getExpressionService().evaluate(strExpr, dependencies, EMPTY_RESOLVED_EXPRESSIONS);
    }

    private ProcessDefinition createProcessAndInsertParameterAndDeployIt(final String processName, final String version, final String taskName,
            final String parameterName, final String parameterValue) throws Exception {
        // create process Definition
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(processName, version);
        // processBuilder.addParameter(parameterName, String.class.getCanonicalName()).addUserTask(taskName, null);
        final Map<String, String> params = new HashMap<String, String>();
        params.put(parameterName, parameterValue);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        // create business archive
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setParameters(params);
        businessArchive.setProcessDefinition(processDefinition);
        // deploy the archive

        return processAPIImpl.deploy(businessArchive.done());
    }

    @Ignore("Wait until new service configuration")
    @Test
    public void evaluateParameterExpression() throws Exception {
        final String nameParameter = "name";
        final ProcessDefinition deploy = createProcessAndInsertParameterAndDeployIt("firstProcess", "1.0", "userTask1", nameParameter, "baptiste");
        final Long deployId = deploy.getId();
        // create expression
        // check
        assertEquals("baptiste", createAndEvaluateParameterExpression(nameParameter, deployId, "processDefinitionId"));
        processAPIImpl.deleteProcess(deploy.getId());
    }

    @Test(expected = SExpressionEvaluationException.class)
    public void evaluateExpressionWithAnUnknownParameter() throws Exception {
        final String nameParameter = "name";
        final ProcessDefinition deploy = createProcessAndInsertParameterAndDeployIt("firstProcess", "1.0", "userTask1", nameParameter, "baptiste");
        final Long deployId = deploy.getId();

        try {
            createAndEvaluateParameterExpression("nonExistingParameter", deployId, "processDefinitionId");
        } finally {
            processAPIImpl.deleteProcess(deploy.getId());
        }
    }

    @Test
    public void evaluateExpWithParameterAndDataFromDB() throws Exception {
        final String parameterName = "name";
        final String parameterValue = "baptiste";
        final String strDataName = "address";
        final String intDataName = "phone";
        final String strDataValue = "zhangan street 151";
        final String expContent = "'welcome '+name+' to '+address+',Please call '+phone";
        final List<String> variableNames = new ArrayList<String>();
        variableNames.add(parameterName);
        variableNames.add(strDataName);
        variableNames.add(intDataName);
        // create expression
        final SExpression strExpr = newExpression(expContent, SExpression.TYPE_READ_ONLY_SCRIPT, String.class.getName(), SExpression.GROOVY, null);
        final Map<String, Object> dependencies = new HashMap<String, Object>();
        dependencies.put(parameterName, parameterValue);
        dependencies.put(strDataName, strDataValue);
        final int intDataValue = 13812345;
        dependencies.put(intDataName, intDataValue);
        // check
        assertEquals("welcome " + parameterValue + " to " + strDataValue + ",Please call " + intDataValue,
                getExpressionService().evaluate(strExpr, dependencies, EMPTY_RESOLVED_EXPRESSIONS));
    }

}
