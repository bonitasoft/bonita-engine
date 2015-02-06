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
package org.bonitasoft.engine.search.identity;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.identity.CustomUserInfoValue;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;
import org.bonitasoft.engine.service.ModelConvertor;

/**
 * @author Vincent Elcrin
 */
public class SearchCustomUserInfoValues extends AbstractSearchEntity<CustomUserInfoValue, SCustomUserInfoValue> {

    private final IdentityService service;

    public SearchCustomUserInfoValues(IdentityService service, SearchEntityDescriptor searchDescriptor, SearchOptions options) {
        super(searchDescriptor, options);
        this.service = service;
    }

    @Override
    public long executeCount(QueryOptions options) throws SBonitaReadException {
        return service.getNumberOfCustomUserInfoValue(options);
    }

    @Override
    public List<SCustomUserInfoValue> executeSearch(QueryOptions options) throws SBonitaReadException {
        return service.searchCustomUserInfoValue(options);
    }

    @Override
    public List<CustomUserInfoValue> convertToClientObjects(List<SCustomUserInfoValue> sValues) {
        List<CustomUserInfoValue> values = new ArrayList<CustomUserInfoValue>(sValues.size());
        for (SCustomUserInfoValue value : sValues) {
            values.add(ModelConvertor.convert(value));
        }
        return values;
    }
}
