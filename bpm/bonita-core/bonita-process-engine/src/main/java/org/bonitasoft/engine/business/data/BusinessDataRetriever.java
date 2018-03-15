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

package org.bonitasoft.engine.business.data;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.business.data.proxy.ServerProxyfier;
import org.bonitasoft.engine.core.process.instance.model.business.data.SMultiRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;

/**
 * @author Elias Ricken de Medeiros
 */
public class BusinessDataRetriever {

    private final BusinessDataRepository businessDataRepository;
    private final ServerProxyfier proxyfier;

    public BusinessDataRetriever(BusinessDataRepository businessDataRepository, ServerProxyfier proxyfier) {
        this.businessDataRepository = businessDataRepository;
        this.proxyfier = proxyfier;
    }

    /**
     * Retrieves the Business Data related to the given {@link SSimpleRefBusinessDataInstance}. If the {@code SSimpleRefBusinessDataInstance} does not
     * references any Business Data the result will be null.
     *
     * @param dataRef the business data reference
     * @param bizClass the business data class
     * @return the Business Data related to the given {@code SSimpleRefBusinessDataInstance} or null if no Business Data is referenced.
     * @throws SBusinessDataNotFoundException when no Business Data is found for the given id
     */
    public Entity getSimpleBusinessData(SSimpleRefBusinessDataInstance dataRef, Class<? extends Entity> bizClass) throws SBusinessDataNotFoundException {
        if (dataRef.getDataId() == null) {
            return null;
        }
        final Entity entity = businessDataRepository.findById(bizClass, dataRef.getDataId());
        return proxyfier.proxify(entity);
    }

    /**
     * Retrieves the list of Business Data related to the given {@link SMultiRefBusinessDataInstance}. If the {@code SMultiRefBusinessDataInstance} does not
     * references any Business Data the result will em empty list.
     *
     * @param dataRef the multi business data reference
     * @param bizClass the business data class
     * @return the list of Business Data related to the given {@code SMultiRefBusinessDataInstance} or empty list if no Business Data is referenced.
     */
    public List<Entity> getMultiBusinessData(SMultiRefBusinessDataInstance dataRef, Class<? extends Entity> bizClass) {
        if (dataRef.getDataIds() == null || dataRef.getDataIds().isEmpty()) {
            return new ArrayList<>();
        }
        final List<? extends Entity> entities = businessDataRepository.findByIds(bizClass, dataRef.getDataIds());

        final List<Entity> e = new ArrayList<>();
        for (final Entity entity : entities) {
            e.add(proxyfier.proxify(entity));
        }
        return e;
    }

    /**
     * Retrieves a Business Data or a List of Business Data related to the given {@link SRefBusinessDataInstance} depending on its type (single {@link Entity}
     * if it's a {@link SSimpleRefBusinessDataInstance} or a List<Entity> if it's a {@link SMultiRefBusinessDataInstance}.
     * This method will use {@link #getSimpleBusinessData(SSimpleRefBusinessDataInstance, Class)} or
     * {@link #getMultiBusinessData(SMultiRefBusinessDataInstance, Class)} based on the data reference type
     *
     * @param refBusinessDataInstance the business data reference
     * @return The {@code Entity} or {@code List<Entity>} if the business data reference is a {@code SSimpleRefBusinessDataInstance} or a
     *         {@code SMultiRefBusinessDataInstance} respectively
     * @throws SBusinessDataNotFoundException
     * @throws SExpressionEvaluationException
     */
    public Object getBusinessData(final SRefBusinessDataInstance refBusinessDataInstance)
            throws SBusinessDataNotFoundException, SExpressionEvaluationException {
        try {
            final Class<Entity> bizClass = (Class<Entity>) Thread.currentThread().getContextClassLoader().loadClass(refBusinessDataInstance.getDataClassName());
            if (refBusinessDataInstance instanceof SSimpleRefBusinessDataInstance) {
                return getSimpleBusinessData((SSimpleRefBusinessDataInstance) refBusinessDataInstance, bizClass);
            }
            final SMultiRefBusinessDataInstance reference = (SMultiRefBusinessDataInstance) refBusinessDataInstance;
            return getMultiBusinessData(reference, bizClass);
        } catch (final ClassNotFoundException e) {
            throw new SExpressionEvaluationException("Unable to load class for the business data having reference '" + refBusinessDataInstance.getName() + "'",
                    e, refBusinessDataInstance.getName());
        }
    }

}
