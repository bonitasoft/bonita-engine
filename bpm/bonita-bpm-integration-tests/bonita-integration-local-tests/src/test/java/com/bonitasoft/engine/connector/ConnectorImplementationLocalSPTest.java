/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.connector;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.connectors.TestConnector;
import org.bonitasoft.engine.connectors.TestConnectorWithModifiedOutput;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.Test;

import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;

/**
 * @author Emmanuel Duchastenier
 */
@SuppressWarnings("javadoc")
public class ConnectorImplementationLocalSPTest extends ConnectorExecutionTest {

    protected TenantServiceAccessor getTenantAccessor() throws InvalidSessionException {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new InvalidSessionException(e);
        }
    }

    @Test
    public void setConnectorImplementationCleansOldDependencies() throws Exception {
        final String connectorId = "org.bonitasoft.connector.testConnector";
        final String connectorVersion = "1.0";
        final ProcessDefinitionBuilderExt processDefinitionBuilderExt = new ProcessDefinitionBuilderExt().createNewInstance(
                "testSetConnectorImplementationCleansOldDependencies", "1.0");
        processDefinitionBuilderExt.addConnector("myConnector", connectorId, connectorVersion, ConnectorEvent.ON_ENTER);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithConnector(
                processDefinitionBuilderExt,
                Arrays.asList(BuildTestUtil.getContentAndBuildBarResource("OldConnector.impl", APIAccessorConnector.class)),
                Arrays.asList(BuildTestUtil.generateJarAndBuildBarResource(TestConnector.class, "TestConnector.jar"),
                        BuildTestUtil.generateJarAndBuildBarResource(VariableStorage.class, "VariableStorage.jar")));

        Pair<List<Long>, List<SDependencyMapping>> pair = buildPairWithDependencyIdAndMapping(processDefinition);
        assertEquals(2, pair._1.size());
        assertEquals(2, pair._2.size());

        // prepare zip byte array of connector implementation
        final String implSourchFile = "/com/bonitasoft/engine/connector/NewConnector.impl";
        final Class<TestConnectorWithModifiedOutput> implClass = TestConnectorWithModifiedOutput.class;
        final byte[] connectorImplementationArchive = generateZipByteArrayForConnector(implSourchFile, implClass);
        getProcessAPI().setConnectorImplementation(processDefinition.getId(), connectorId, connectorVersion, connectorImplementationArchive);

        pair = buildPairWithDependencyIdAndMapping(processDefinition);
        assertEquals(1, pair._1.size());
        assertEquals(1, pair._2.size());

        disableAndDeleteProcess(processDefinition);
    }

    private Pair<List<Long>, List<SDependencyMapping>> buildPairWithDependencyIdAndMapping(final ProcessDefinition processDefinition) throws Exception {
        final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
        sessionAccessor.setSessionInfo(getSession().getId(), getSession().getTenantId()); // set session info
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DependencyService dependencyService = tenantAccessor.getDependencyService();
        final UserTransactionService userTransactionService = tenantAccessor.getUserTransactionService();

        Pair<List<Long>, List<SDependencyMapping>> pair = userTransactionService
                .executeInTransaction(new Callable<Pair<List<Long>, List<SDependencyMapping>>>() {

                    @Override
                    public Pair<List<Long>, List<SDependencyMapping>> call() throws Exception {
                        final List<Long> dependencyIds = dependencyService.getDependencyIds(processDefinition.getId(), ScopeType.PROCESS,
                                0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS);

                        final List<FilterOption> filters = new ArrayList<FilterOption>();
                        filters.add(new FilterOption(SDependencyMapping.class, "artifactId", processDefinition.getId()));
                        filters.add(new FilterOption(SDependencyMapping.class, "artifactType", ScopeType.PROCESS.name()));
                        final QueryOptions queryOptions = new QueryOptions(0, 10, Collections.singletonList(new OrderByOption(SDependencyMapping.class, "id",
                                OrderByType.ASC)), filters, null);
                        final List<SDependencyMapping> dependencyMappings = dependencyService.getDependencyMappings(queryOptions);
                        return new Pair<List<Long>, List<SDependencyMapping>>(dependencyIds, dependencyMappings);
                    }
                });
        return pair;
    }

    private static class Pair<T, V> {

        T _1;

        V _2;

        Pair(final T _1, final V _2) {
            this._1 = _1;
            this._2 = _2;
        }

    }
}
