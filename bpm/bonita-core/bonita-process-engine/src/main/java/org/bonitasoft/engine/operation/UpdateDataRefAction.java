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

package org.bonitasoft.engine.operation;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import org.bonitasoft.engine.core.process.instance.model.business.data.SMultiRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;

import org.bonitasoft.engine.bdm.Entity;

/**
 * @author Elias Ricken de Medeiros
 */
public class UpdateDataRefAction implements EntityAction {

    private final RefBusinessDataService refBusinessDataService;
    private final FlowNodeInstanceService flowNodeInstanceService;

    public UpdateDataRefAction(RefBusinessDataService refBusinessDataService,
            FlowNodeInstanceService flowNodeInstanceService) {
        this.refBusinessDataService = refBusinessDataService;
        this.flowNodeInstanceService = flowNodeInstanceService;
    }

    @Override
    public Entity execute(final Entity entity, final BusinessDataContext businessDataContext) throws SEntityActionExecutionException {
        try {
            SRefBusinessDataInstance reference = getRefBusinessDataInstance(businessDataContext);
            checkThatIsSimpleRef(reference);
            SSimpleRefBusinessDataInstance simpleRef = (SSimpleRefBusinessDataInstance) reference;
            if (!entity.getPersistenceId().equals(simpleRef.getDataId())) {
                refBusinessDataService.updateRefBusinessDataInstance(simpleRef, entity.getPersistenceId());
            }
        } catch (SBonitaException e) {
            throw new SEntityActionExecutionException(e);
        }
        return entity;
    }

    private void checkThatIsSimpleRef(final SRefBusinessDataInstance reference) throws SEntityActionExecutionException {
        if (!(reference instanceof SSimpleRefBusinessDataInstance)) {
            throw new SEntityActionExecutionException("Incompatible types: the business data '" + reference.getName()
                    + "' is marked as multiple, but its new value is not a list. Either mark the business data as simple or use a list as new value.'");
        }
    }

    private SRefBusinessDataInstance getRefBusinessDataInstance(BusinessDataContext context)
            throws SBonitaException {
        if (DataInstanceContainer.PROCESS_INSTANCE.name().equals(context.getContainer().getType())) {
            return refBusinessDataService.getRefBusinessDataInstance(context.getName(), context.getContainer().getId());
        }
        try {
            return refBusinessDataService.getFlowNodeRefBusinessDataInstance(context.getName(), context.getContainer().getId());
        } catch (final SBonitaException sbe) {
            final long processInstanceId = flowNodeInstanceService.getProcessInstanceId(context.getContainer().getId(), context.getContainer().getType());
            return refBusinessDataService.getRefBusinessDataInstance(context.getName(), processInstanceId);
        }
    }

    @Override
    public List<Entity> execute(final List<Entity> entities, final BusinessDataContext businessDataContext) throws SEntityActionExecutionException {
        try {
            SRefBusinessDataInstance reference = getRefBusinessDataInstance(businessDataContext);
            checkThatIsMultiRef(reference);
            SMultiRefBusinessDataInstance multiRef = (SMultiRefBusinessDataInstance) reference;
            ArrayList<Long> dataIds = buildDataIdsList(entities);
            if (!multiRef.getDataIds().containsAll(dataIds) || multiRef.getDataIds().size() != dataIds.size()) {
                refBusinessDataService.updateRefBusinessDataInstance(multiRef, dataIds);
            }
        } catch (SBonitaException e) {
            throw new SEntityActionExecutionException(e);
        }
        return entities;
    }

    private void checkThatIsMultiRef(final SRefBusinessDataInstance reference) throws SEntityActionExecutionException {
        if (!(reference instanceof SMultiRefBusinessDataInstance)) {
            throw new SEntityActionExecutionException(
                    "Incompatible types: the business data '"
                            + reference.getName()
                            + "' is not marked as multiple, but its new value is a list. Either mark the business data as multiple or use a single Entity as new value.'");
        }
    }

    private ArrayList<Long> buildDataIdsList(final List<Entity> entities) throws SEntityActionExecutionException {
        ArrayList<Long> businessDataIds = new ArrayList<>(entities.size());
        for (Entity entity : entities) {
            checkNotNull(entity);
            businessDataIds.add(entity.getPersistenceId());
        }
        return businessDataIds;
    }

    private void checkNotNull(final Entity entity) throws SEntityActionExecutionException {
        if (entity == null) {
            throw new SEntityActionExecutionException("The list of entities contains some null elements. Unable to execute action against null entity.");
        }
    }
}
