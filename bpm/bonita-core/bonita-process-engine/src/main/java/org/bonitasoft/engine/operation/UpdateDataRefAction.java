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

import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.business.data.RefBusinessDataRetriever;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import org.bonitasoft.engine.core.process.instance.model.business.data.SMultiRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SSimpleRefBusinessDataInstance;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class UpdateDataRefAction implements EntityAction {

    private final RefBusinessDataService refBusinessDataService;
    private final RefBusinessDataRetriever refBusinessDataRetriever;

    public UpdateDataRefAction(final RefBusinessDataService refBusinessDataService, final RefBusinessDataRetriever refBusinessDataRetriever) {
        this.refBusinessDataService = refBusinessDataService;
        this.refBusinessDataRetriever = refBusinessDataRetriever;
    }

    @Override
    public Entity execute(final Entity entity, final BusinessDataContext businessDataContext) throws SEntityActionExecutionException {
        try {
            final SRefBusinessDataInstance reference = refBusinessDataRetriever.getRefBusinessDataInstance(businessDataContext);
            checkThatIsSimpleRef(reference);
            final SSimpleRefBusinessDataInstance simpleRef = (SSimpleRefBusinessDataInstance) reference;
            if (!entity.getPersistenceId().equals(simpleRef.getDataId())) {
                refBusinessDataService.updateRefBusinessDataInstance(simpleRef, entity.getPersistenceId());
            }
        } catch (final SBonitaException e) {
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

    @Override
    public List<Entity> execute(final List<Entity> entities, final BusinessDataContext businessDataContext) throws SEntityActionExecutionException {
        try {
            final SRefBusinessDataInstance reference = refBusinessDataRetriever.getRefBusinessDataInstance(businessDataContext);
            checkThatIsMultiRef(reference);
            final SMultiRefBusinessDataInstance multiRef = (SMultiRefBusinessDataInstance) reference;
            final ArrayList<Long> dataIds = buildDataIdsList(entities);
            if (!multiRef.getDataIds().containsAll(dataIds) || multiRef.getDataIds().size() != dataIds.size()) {
                refBusinessDataService.updateRefBusinessDataInstance(multiRef, dataIds);
            }
        } catch (final SBonitaException e) {
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
        final ArrayList<Long> businessDataIds = new ArrayList<>(entities.size());
        for (final Entity entity : entities) {
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

    @Override
    public void handleNull(final BusinessDataContext businessDataContext) throws SEntityActionExecutionException {
        try {
            final SRefBusinessDataInstance reference = refBusinessDataRetriever.getRefBusinessDataInstance(businessDataContext);
            if (reference instanceof SSimpleRefBusinessDataInstance) {
                final SSimpleRefBusinessDataInstance simpleReference = (SSimpleRefBusinessDataInstance) reference;
                refBusinessDataService.updateRefBusinessDataInstance(simpleReference, null);
            } else {
                final SMultiRefBusinessDataInstance multiReference = (SMultiRefBusinessDataInstance) reference;
                refBusinessDataService.updateRefBusinessDataInstance(multiReference, new ArrayList<Long>());
            }
        } catch (final SBonitaException sbe) {
            throw new SEntityActionExecutionException(sbe);
        }
    }

}
