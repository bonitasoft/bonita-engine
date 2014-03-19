/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
 */
package org.bonitasoft.engine.api.impl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.CustomUserInfoValue;
import org.bonitasoft.engine.identity.CustomUserInfoValueUpdater;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SCustomUserInfoValueNotFoundException;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoDefinitionBuilder;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoDefinitionBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoValueBuilder;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoValueBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoValueUpdateBuilder;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoValueUpdateBuilderFactory;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;
import org.bonitasoft.engine.search.identity.SearchCustomUserInfoValues;

/**
 * @author Vincent Elcrin
 */
public class CustomUserInfoValueAPI {

    private final IdentityService service;

    private final CustomUserInfoConverter converter = new CustomUserInfoConverter();

    public CustomUserInfoValueAPI(IdentityService service) {
        this.service = service;
    }

    public SearchResult<CustomUserInfoValue> search(SearchEntityDescriptor descriptor, final SearchOptions options) {
        SearchCustomUserInfoValues search = new SearchCustomUserInfoValues(service, descriptor, options);
        try {
            search.execute();
            return search.getResult();
        } catch (SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    public CustomUserInfoValue update(SCustomUserInfoValueBuilderFactory factory, long definitionId, long userId, CustomUserInfoValueUpdater updater) throws UpdateException {
        if (updater == null) {
            throw new UpdateException("The update descriptor does not contains field updates");
        }
        try {
            SCustomUserInfoValue value = searchValue(definitionId, userId);
            if(value != null) {
                service.updateCustomUserInfoValue(value, createUpdateDescriptor(updater));
                return converter.convert(service.getCustomUserInfoValue(value.getId()));
            }
            return converter.convert(create(factory, definitionId, userId, updater.getValue()));
        } catch (final SBonitaException e) {
            throw new UpdateException(e);
        }
    }

    public SCustomUserInfoValue create(SCustomUserInfoValueBuilderFactory factory, long definitionId, long userId, String value) throws SIdentityException {
        return service.createCustomUserInfoValue(factory.createNewInstance()
                .setDefinitionId(definitionId)
                .setUserId(userId)
                .setValue(value)
                .done());
    }

    private SCustomUserInfoValue searchValue(long definitionId, long userId) throws SBonitaSearchException, SCustomUserInfoValueNotFoundException {
        List<SCustomUserInfoValue> result = service.searchCustomUserInfoValue(new QueryOptions(
                0,
                1,
                Collections.<OrderByOption>emptyList(),
                Arrays.asList(
                        new FilterOption(SCustomUserInfoDefinition.class, "definitionId", definitionId),
                        new FilterOption(SCustomUserInfoDefinition.class, "userId", userId)),
                null));
        if(result.size() == 0) {
            throw new SCustomUserInfoValueNotFoundException(definitionId, userId);
        }
        return result.get(0);
    }

    private EntityUpdateDescriptor createUpdateDescriptor(final CustomUserInfoValueUpdater updater) {
        return BuilderFactory.get(SCustomUserInfoValueUpdateBuilderFactory.class)
                .createNewInstance()
                .updateValue(updater.getValue())
                .done();
    }
}
