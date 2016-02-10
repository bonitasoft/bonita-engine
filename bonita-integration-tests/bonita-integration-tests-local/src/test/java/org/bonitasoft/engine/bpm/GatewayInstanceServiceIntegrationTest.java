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
package org.bonitasoft.engine.bpm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SGatewayType;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeDeletionException;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SGatewayInstanceBuilderFactory;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.junit.Test;

/**
 * @author Feng Hui
 * @author Zhao Na
 */
public class GatewayInstanceServiceIntegrationTest extends CommonBPMServicesTest {


    protected void deleteGatewayInstance(final SGatewayInstance gatewayInstance) throws SBonitaException {
        getTransactionService().begin();
        try {
            getTenantAccessor().getActivityInstanceService().deleteFlowNodeInstance(gatewayInstance);
        } catch (final SBonitaException e) {
            throw new SFlowNodeDeletionException(e);
        } finally {
            getTransactionService().complete();
        }
    }

    @Test
    public void testCreateAndGetGatewayInstance() throws SBonitaException {
        final SGatewayInstance gatewayInstance = BuilderFactory.get(SGatewayInstanceBuilderFactory.class)
                .createNewInstance("Gateway1", 1, 1, 1, SGatewayType.EXCLUSIVE, 2, 3, 3).setStateId(1).setHitBys("a,b,c").done();

        insertGatewayInstance(gatewayInstance);

        final SGatewayInstance gatewayInstanceRes = getGatewayInstanceFromDB(gatewayInstance.getId());

        checkGateway(gatewayInstance, gatewayInstanceRes, 2, 3);

        deleteGatewayInstance(gatewayInstanceRes);
    }

    private SGatewayInstance getGatewayInstanceFromDB(final Long gatewayId) throws SBonitaException {
        getTransactionService().begin();
        final SGatewayInstance gatewayInstanceRes = getTenantAccessor().getGatewayInstanceService().getGatewayInstance(gatewayId);
        getTransactionService().complete();
        return gatewayInstanceRes;
    }

    private void checkGateway(final SGatewayInstance gatewayInstance, final SGatewayInstance gatewayInstanceRes, final long expectedProcessDefinitionId,
            final long expectedProcessInstanceId) {
        assertNotNull(gatewayInstance);
        final SGatewayInstanceBuilderFactory gatewayInstanceBuilderFact = BuilderFactory.get(SGatewayInstanceBuilderFactory.class);
        final long actualProcessDefinitionId = gatewayInstanceRes.getLogicalGroup(gatewayInstanceBuilderFact.getProcessDefinitionIndex());
        final long actualProcessInstanceId = gatewayInstanceRes.getLogicalGroup(gatewayInstanceBuilderFact.getRootProcessInstanceIndex());
        assertEquals(expectedProcessDefinitionId, actualProcessDefinitionId);
        assertEquals(expectedProcessInstanceId, actualProcessInstanceId);
        assertEquals(gatewayInstance, gatewayInstanceRes);
    }

    private void updateGatewayState(final SGatewayInstance gatewayInstance, final int stateId) throws SBonitaException {
        getTransactionService().begin();
        final SGatewayInstance gatewayInstance2 = getTenantAccessor().getGatewayInstanceService().getGatewayInstance(gatewayInstance.getId());
        getTenantAccessor().getGatewayInstanceService().setState(gatewayInstance2, stateId);
        getTransactionService().complete();
    }

    private void updateGatewayHitbys(final SGatewayInstance gatewayInstance, final long transitionIndex) throws SBonitaException {
        getTransactionService().begin();
        final SGatewayInstance gatewayInstance2 = getTenantAccessor().getGatewayInstanceService().getGatewayInstance(gatewayInstance.getId());
        getTenantAccessor().getGatewayInstanceService().hitTransition(gatewayInstance2, transitionIndex);
        getTransactionService().complete();
    }

    // @Test
    public void testCheckMergingCondition() {
        // it's implement need to be improved
    }

    @Test
    public void testSetState() throws SBonitaException {
        final SGatewayInstance gatewayInstance = BuilderFactory.get(SGatewayInstanceBuilderFactory.class)
                .createNewInstance("Gateway1", 1, 1, 1, SGatewayType.EXCLUSIVE, 2, 3, 3).setStateId(1).setHitBys("a,b,c").done();

        insertGatewayInstance(gatewayInstance);

        final SGatewayInstance gatewayInstanceRes = getGatewayInstanceFromDB(gatewayInstance.getId());

        checkGateway(gatewayInstance, gatewayInstanceRes, 2, 3);

        updateGatewayState(gatewayInstanceRes, 2);

        final SGatewayInstance gatewayInstanceRes2 = getGatewayInstanceFromDB(gatewayInstance.getId());
        assertNotNull(gatewayInstanceRes2);
        assertEquals(2, gatewayInstanceRes2.getStateId());

        deleteGatewayInstance(gatewayInstanceRes);
    }

    @Test
    public void testHitTransition() throws SBonitaException {
        final SGatewayInstance gatewayInstance = BuilderFactory.get(SGatewayInstanceBuilderFactory.class)
                .createNewInstance("Gateway1", 1, 1, 1, SGatewayType.EXCLUSIVE, 2, 3, 3).setStateId(1).setHitBys("1,2,3").done();

        insertGatewayInstance(gatewayInstance);

        final SGatewayInstance gatewayInstanceRes = getGatewayInstanceFromDB(gatewayInstance.getId());

        checkGateway(gatewayInstance, gatewayInstanceRes, 2, 3);

        updateGatewayHitbys(gatewayInstanceRes, 4);

        final SGatewayInstance gatewayInstanceRes2 = getGatewayInstanceFromDB(gatewayInstance.getId());
        assertNotNull(gatewayInstanceRes2);
        assertEquals("1,2,3,4", gatewayInstanceRes2.getHitBys());

        deleteGatewayInstance(gatewayInstanceRes);
    }
}
