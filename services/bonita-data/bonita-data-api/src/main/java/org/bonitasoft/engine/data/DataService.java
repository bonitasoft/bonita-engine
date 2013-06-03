/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.data;

import java.util.Collection;

import org.bonitasoft.engine.data.model.SDataSource;
import org.bonitasoft.engine.data.model.SDataSourceParameter;
import org.bonitasoft.engine.persistence.QueryOptions;

/**
 * @author Matthieu Chaffotte
 * @since 6.0
 */
public interface DataService {

    String DATASOURCE = "DATASOURCE";

    String DATASOURCEPARAMETER = "DATASOURCEPARAMETER";

    // gerer le cycle de vie: passer d'un etat a l'autre (juste une methode setState?)

    /**
     * Create dataSource object by given dataSource
     * 
     * @param dataSource
     *            The dataSource object
     * @throws SDataSourceAlreadyExistException
     *             Error thrown if dataSource is already existed
     * @throws SDataException
     */
    void createDataSource(SDataSource dataSource) throws SDataSourceAlreadyExistException, SDataException;

    /**
     * Create dataSourceParameter object by give dataSourceParameter.
     * 
     * @param dataSourceParameter
     *            The dataSourceParameter object
     * @throws SDataSourceParameterAlreadyExistException
     *             Error thrown if dataSourceParameter is already existed
     * @throws SDataException
     */
    void createDataSourceParameter(SDataSourceParameter dataSourceParameter) throws SDataSourceParameterAlreadyExistException, SDataException;

    /**
     * Create some dataSourceParameter objects by give dataSourceParameters.
     * 
     * @param parameters
     * @throws SDataSourceParameterAlreadyExistException
     *             Error thrown if dataSourceParameter is already existed
     * @throws SDataException
     */
    void createDataSourceParameters(Collection<SDataSourceParameter> parameters) throws SDataSourceParameterAlreadyExistException, SDataException;

    /**
     * Remove dataSource object by its id.
     * 
     * @param dataSourceId
     *            Identifier of dataSource
     * @throws SDataSourceNotFoundException
     *             Error thrown if no dataSource have an id corresponding to the parameter.
     */
    void removeDataSource(long dataSourceId) throws SDataSourceNotFoundException;

    /**
     * Remove the specific dataSource object.
     * 
     * @param dataSource
     *            The dataSource object will be removed
     * @throws SDataSourceNotFoundException
     *             Error thrown if the specific dataSource not existed.
     */
    void removeDataSource(SDataSource dataSource) throws SDataSourceNotFoundException;

    /**
     * Remove dataSourceParameter object by its id.
     * 
     * @param dataSourceParameterId
     * @throws SDataSourceParameterNotFoundException
     *             Error thrown if no dataSourceParameter have an id corresponding to the parameter.
     */
    void removeDataSourceParameter(long dataSourceParameterId) throws SDataSourceParameterNotFoundException;

    /**
     * Remove the specific dataSourceParameter object.
     * 
     * @param dataSourceParameter
     *            The dataSourceParameter object will be removed
     * @throws SDataSourceParameterNotFoundException
     *             Error thrown if the specific dataSource not existed.
     */
    void removeDataSourceParameter(SDataSourceParameter dataSourceParameter) throws SDataSourceParameterNotFoundException;

    /**
     * Remove dataSourceParameter object by given dataSource id.
     * 
     * @param dataSourceId
     * @throws SDataException
     */
    void removeDataSourceParameters(long dataSourceId) throws SDataException;

    /**
     * Remove dataSourceParameter objects by their ids.
     * 
     * @param dataSourceParameterIds
     * @throws SDataSourceParameterNotFoundException
     */
    void removeDataSourceParameters(Collection<Long> dataSourceParameterIds) throws SDataSourceParameterNotFoundException;

    /**
     * Get dataSource implementation for given dataSource id and type
     * 
     * @param <T>
     * @param dataSourceType
     *            The class which extends DataSourceImplementation
     * @param dataSourceId
     *            Identifier of dataSource
     * @return An object of the class which extends DataSourceImplementation
     * @throws SDataSourceNotFoundException
     *             Error thrown if no dataSource have an id corresponding to the parameter dataSourceId.
     * @throws SDataSourceInitializationException
     * @throws SDataSourceInactiveException
     * @throws SDataException
     */
    <T extends DataSourceImplementation> T getDataSourceImplementation(Class<T> dataSourceType, long dataSourceId) throws SDataSourceNotFoundException,
            SDataSourceInitializationException, SDataSourceInactiveException, SDataException;

    /**
     * Get dataSource object by its id
     * 
     * @param dataSourceId
     *            Identifier of dataSource
     * @return A SDataSource object
     * @throws SDataSourceNotFoundException
     *             Error thrown if no dataSource have an id corresponding to the parameter.
     */
    SDataSource getDataSource(long dataSourceId) throws SDataSourceNotFoundException;

    /**
     * Get dataSource object by its name and version
     * 
     * @param name
     *            The name of dataSource
     * @param version
     *            The version of dataSource
     * @return A SDataSource object
     * @throws SDataSourceNotFoundException
     *             Error thrown if no dataSource have an name and version corresponding to the parameters.
     */
    SDataSource getDataSource(String name, String version) throws SDataSourceNotFoundException;

    /**
     * Get dataSourceParameter object by its id
     * 
     * @param dataSourceParameterId
     *            Identifier of dataSourceParameter
     * @return A SDataSourceParameter object
     * @throws SDataSourceParameterNotFoundException
     *             Error thrown if no dataSourceParameter have an id corresponding to the parameter.
     */
    SDataSourceParameter getDataSourceParameter(long dataSourceParameterId) throws SDataSourceParameterNotFoundException;

    /**
     * Get dataSourceParameter object by its name and dataSource id
     * 
     * @param name
     *            The name of dataSourceParameter
     * @param dataSourceId
     *            The identifier of dataSource
     * @return A SDataSourceParameter object
     * @throws SDataSourceParameterNotFoundException
     *             Error thrown if no corresponding dataSourceParameter found
     */
    SDataSourceParameter getDataSourceParameter(String name, long dataSourceId) throws SDataSourceParameterNotFoundException;

    /**
     * Get dataSourceParameter objects for given dataSource id
     * 
     * @param dataSourceId
     *            The identifier of dataSource
     * @param queryOptions
     *            Options to the query
     * @return a collection of SDataSourceParameter objects
     * @throws SDataException
     */
    Collection<SDataSourceParameter> getDataSourceParameters(long dataSourceId, QueryOptions queryOptions) throws SDataException;

    /**
     * Get dataSource objects according to specific query options
     * 
     * @param queryOptions
     *            Options to the query
     * @return a collection of SDataSource objects
     * @throws SDataException
     */
    Collection<SDataSource> getDataSources(QueryOptions queryOptions) throws SDataException;

}
