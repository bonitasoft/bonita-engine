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
package org.bonitasoft.engine.core.process.instance.api;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SGatewayCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SGatewayModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SGatewayNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SGatewayReadException;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Feng Hui
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @since 6.0
 */
public interface GatewayInstanceService {

    String FINISH = "FINISH:";

    String GATEWAYINSTANCE = "GATEWAYINSTANCE";

    String GATEWAYINSTANCE_STATE = "GATEWAYINSTANCE_STATE";

    String GATEWAYINSTANCE_HITBYS = "GATEWAYINSTANCE_HITBYS";

    /**
     * Create gatewayInstance in DB according to the given gateway instance object
     * 
     * @param gatewayInstance
     *            the gatewayInsttance object
     * @throws SGatewayCreationException
     */
    void createGatewayInstance(SGatewayInstance gatewayInstance) throws SGatewayCreationException;

    /**
     * Get gateway instance by its id
     * 
     * @param gatewayInstanceId
     *            identifier of gateway instance
     * @return an SGatewayInstance object
     * @throws SGatewayNotFoundException
     * @throws SGatewayReadException
     */
    SGatewayInstance getGatewayInstance(long gatewayInstanceId) throws SGatewayNotFoundException, SGatewayReadException;

    /**
     * Change state of the specific gateway
     * 
     * @param gatewayInstance
     *            the gateway instance will be updated
     * @param stateId
     *            identifier of gateway state
     * @throws SGatewayModificationException
     */
    void setState(SGatewayInstance gatewayInstance, int stateId) throws SGatewayModificationException;

    /**
     * @param sDefinition
     * @param gatewayInstance
     * @return
     * @throws SBonitaException
     */
    boolean checkMergingCondition(SProcessDefinition sDefinition, SGatewayInstance gatewayInstance) throws SBonitaException;

    /**
     * Add transitionDefinitionName to hitBy of specific gatewayInstance
     * 
     * @param gatewayInstance
     *            the gateway instance will be updated
     * @param transitionIndex
     *            value will be added to hitBy of gatewayInstance
     * @throws SGatewayModificationException
     * @throws SGatewayCreationException
     */
    void hitTransition(SGatewayInstance gatewayInstance, long transitionIndex) throws SGatewayModificationException, SGatewayCreationException;

    /**
     * Get active gatewayInstance in the specific process instance
     * 
     * @param parentProcessInstanceId
     *            identifier of parent process instance
     * @param name
     *            name of gateway instance
     * @return
     * @throws SGatewayNotFoundException
     * @throws SGatewayReadException
     */
    SGatewayInstance getActiveGatewayInstanceOfTheProcess(long parentProcessInstanceId, String name) throws SGatewayNotFoundException, SGatewayReadException;

    List<SGatewayInstance> setFinishAndCreateNewGatewayForRemainingToken(SProcessDefinition processDefinition, SGatewayInstance gatewayInstance) throws SBonitaException;


    List<SGatewayInstance> getInclusiveGatewaysOfProcessInstanceThatShouldFire(SProcessDefinition processDefinition, long processInstanceId) throws SBonitaReadException;
}
