/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.connectors.TestConnector;
import org.bonitasoft.engine.connectors.TestConnectorWithModifiedOutput;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Test;

import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;
import com.bonitasoft.engine.connector.ConnectorExecutionTest;
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
        final ProcessDefinitionBuilderExt processDesign = new ProcessDefinitionBuilderExt().createNewInstance(
                "testSetConnectorImplementationCleansOldDependencies", "1.0");
        processDesign.addConnector("myConnector", connectorId, connectorVersion, ConnectorEvent.ON_ENTER);

        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(processDesign.done());
        final List<BarResource> resources = new ArrayList<BarResource>(1);
        addResource(resources, "/com/bonitasoft/engine/connectors/OldConnector.impl", "OldConnector.impl");
        businessArchiveBuilder.addConnectorImplementation(resources.get(0));

        final List<BarResource> barResources = new ArrayList<BarResource>(2);
        addResource(barResources, TestConnector.class, "TestConnector.jar");
        addResource(barResources, VariableStorage.class, "VariableStorage.jar");
        for (BarResource barResource : barResources) {
            businessArchiveBuilder.addClasspathResource(barResource);
        }

        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchiveBuilder.done());

        final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
        sessionAccessor.setSessionInfo(getSession().getId(), getSession().getTenantId()); // set session info
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DependencyService dependencyService = tenantAccessor.getDependencyService();
        final TransactionService transactionService = tenantAccessor.getTransactionService();

        transactionService.begin();
        List<Long> dependencyIds = dependencyService.getDependencyIds(processDefinition.getId(), "process", QueryOptions.defaultQueryOptions());
        List<SDependencyMapping> dependencyMappings = dependencyService.getDependencyMappings(processDefinition.getId(), "process",
                QueryOptions.defaultQueryOptions());
        transactionService.complete();
        assertEquals(2, dependencyIds.size());
        assertEquals(2, dependencyMappings.size());

        // prepare zip byte array of connector implementation
        final String implSourchFile = "/com/bonitasoft/engine/connectors/NewConnector.impl";
        final Class<TestConnectorWithModifiedOutput> implClass = TestConnectorWithModifiedOutput.class;
        final byte[] connectorImplementationArchive = generateZipByteArrayForConnector(implSourchFile, implClass);
        getProcessAPI().setConnectorImplementation(processDefinition.getId(), connectorId, connectorVersion, connectorImplementationArchive);

        transactionService.begin();
        sessionAccessor.setSessionInfo(getSession().getId(), getSession().getTenantId()); // set session info
        dependencyIds = dependencyService.getDependencyIds(processDefinition.getId(), "process", QueryOptions.defaultQueryOptions());
        dependencyMappings = dependencyService.getDependencyMappings(processDefinition.getId(), "process", QueryOptions.defaultQueryOptions());
        transactionService.complete();
        assertEquals(1, dependencyIds.size());
        assertEquals(1, dependencyMappings.size());

        getProcessAPI().deleteProcess(processDefinition.getId());
    }
}
