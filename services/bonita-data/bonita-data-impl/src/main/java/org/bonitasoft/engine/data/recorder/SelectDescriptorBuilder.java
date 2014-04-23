/**
 * Copyright (C) 2011, 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.data.recorder;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.data.model.SDataSource;
import org.bonitasoft.engine.data.model.SDataSourceParameter;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;

/**
 * @author Charles Souillard
 * @author Celine Souchet
 */
public class SelectDescriptorBuilder {

    private SelectDescriptorBuilder() {
        // For Sonar
    }

    public static <T extends PersistentObject> SelectByIdDescriptor<T> getElementById(final Class<T> clazz, final String elementName, final long id) {
        return new SelectByIdDescriptor<T>("get" + elementName + "ById", clazz, id);
    }

    public static SelectOneDescriptor<SDataSource> getDataSource(final String name, final String version) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("name", name);
        parameters.put("version", version);
        return new SelectOneDescriptor<SDataSource>("getDataSourceByNameAndVersion", parameters, SDataSource.class);
    }

    public static SelectOneDescriptor<SDataSourceParameter> getDataSourceParameter(final String name, final long dataSourceId) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("name", name);
        parameters.put("dataSourceId", dataSourceId);
        return new SelectOneDescriptor<SDataSourceParameter>("getDataSourceParameterByNameAndDataSourceId", parameters, SDataSourceParameter.class);
    }

    public static SelectListDescriptor<SDataSourceParameter> getDataSourceParameters(final long dataSourceId, final QueryOptions queryOptions) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("dataSourceId", dataSourceId);
        return new SelectListDescriptor<SDataSourceParameter>("getDataSourceParametersByDataSourceId", parameters, SDataSourceParameter.class, queryOptions);
    }

    public static SelectListDescriptor<SDataSource> getDataSources(final QueryOptions queryOptions) {
        return new SelectListDescriptor<SDataSource>("getDataSources", null, SDataSource.class, queryOptions);
    }

}
