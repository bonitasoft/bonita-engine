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
package org.bonitasoft.engine.dependency;

import java.util.Collection;
import java.util.List;

import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @since 6.0
 */
public interface DependencyService {

    String DEPENDENCY = "DEPENDENCY";

    String DEPENDENCYMAPPING = "DEPENDENCYMAPPING";

    /**
     * Create dependency in DB for the given dependency object.
     * 
     * @param dependency
     *        SDependency object
     * @throws SDependencyAlreadyExistsException
     *         Error thrown if the dependency already exists in DB
     * @throws SDependencyCreationException
     *         Error thrown if has exception during the dependency creation.
     */
    void createDependency(SDependency dependency) throws SDependencyAlreadyExistsException, SDependencyCreationException;

    /**
     * Delete the dependency specified by id
     * 
     * @param id
     *        Identifier of dependency
     * @throws SDependencyNotFoundException
     *         Error thrown if no dependency have an id corresponding to the parameter.
     * @throws SDependencyDeletionException
     *         Error thrown if has exception during the dependency deletion.
     */
    void deleteDependency(long id) throws SDependencyNotFoundException, SDependencyDeletionException;

    /**
     * Delete the specific dependency
     * 
     * @param dependency
     *        The dependency will be deleted
     * @throws SDependencyNotFoundException
     *         Error thrown if the dependency not found.
     * @throws SDependencyDeletionException
     *         Error thrown if has exception during the dependency deletion.
     */
    void deleteDependency(SDependency dependency) throws SDependencyNotFoundException, SDependencyDeletionException;

    /**
     * Delete the dependency specified by name
     * 
     * @param name
     * @throws SDependencyNotFoundException
     *         Error thrown if no dependency have a name corresponding to the parameter.
     * @throws SDependencyDeletionException
     *         Error thrown if has exception during the dependency deletion.
     */
    void deleteDependency(String name) throws SDependencyNotFoundException, SDependencyDeletionException;

    /**
     * Delete all dependencies in DB
     * 
     * @throws SDependencyDeletionException
     *         Error thrown if has exception during the dependency deletion.
     */
    void deleteAllDependencies() throws SDependencyDeletionException;

    /**
     * update specific dependency
     * 
     * @param dependency
     *        The dependency will be updated
     * @param descriptor
     *        The update description.
     * @throws SDependencyException
     */
    void updateDependency(SDependency dependency, EntityUpdateDescriptor descriptor) throws SDependencyException;

    /**
     * Get dependency by its id
     * 
     * @param id
     *        Identifier of dependency
     * @return
     * @throws SDependencyNotFoundException
     *         Error thrown if no dependency have an id corresponding to the parameter.
     */
    SDependency getDependency(long id) throws SDependencyNotFoundException;

    /**
     * Get dependencies for the specific queryOptions
     * 
     * @param queryOptions
     *        QueryOptions object, contains some conditions for the dependencies retrieve
     * @return a list of SDependency object
     * @throws SDependencyException
     */
    List<SDependency> getDependencies(QueryOptions queryOptions) throws SDependencyException;

    /**
     * Get dependencies for the specified ids
     * 
     * @param ids
     *        Identifiers of dependencies
     * @return a list of SDependency object
     * @throws SDependencyException
     */
    List<SDependency> getDependencies(Collection<Long> ids) throws SDependencyException;

    /**
     * Create dependencyMapping in DB for given dependencyMapping object
     * 
     * @param dependencyMapping
     *        SDependencyMapping object
     * @throws SDependencyException
     */
    void createDependencyMapping(SDependencyMapping dependencyMapping) throws SDependencyException;

    /**
     * Delete the dependencyMapping specified by id
     * 
     * @param id
     *        Identifier of dependencyMapping
     * @throws SDependencyException
     * @throws SDependencyMappingNotFoundException
     *         Error thrown if no dependencyMapping have an id corresponding to the parameter.
     */
    void deleteDependencyMapping(long id) throws SDependencyException;

    /**
     * Delete the specific dependencyMapping
     * 
     * @param dependencyMapping
     *        The dependencyMapping will be deleted
     * @throws SDependencyException
     */
    void deleteDependencyMapping(SDependencyMapping dependencyMapping) throws SDependencyException;

    /**
     * Delete all dependencyMappings in DB
     * 
     * @throws SDependencyException
     */
    void deleteAllDependencyMappings() throws SDependencyException;

    /**
     * Update the specific dependencyMapping
     * 
     * @param dependencyMapping
     *        The dependencyMapping will be updated
     * @param descriptor
     *        Update description
     * @throws SDependencyException
     */
    void updateDependencyMapping(SDependencyMapping dependencyMapping, EntityUpdateDescriptor descriptor) throws SDependencyException;

    /**
     * Get dependencyMapping by its id
     * 
     * @param id
     *        Identifier of dependencyMapping
     * @return a SDependencyMapping object
     * @throws SDependencyMappingNotFoundException
     *         Error thrown if no dependencyMapping have an id corresponding to the parameter.
     */
    SDependencyMapping getDependencyMapping(long id) throws SDependencyMappingNotFoundException;

    /**
     * Get all dependencyMappings for specific the queryOptions
     * 
     * @param queryOptions
     *        QueryOptions object, it contains some query conditions.
     * @return a list of SDependencyMapping objects
     * @throws SDependencyException
     */
    List<SDependencyMapping> getDependencyMappings(QueryOptions queryOptions) throws SDependencyException;

    /**
     * Get all dependencyMappings for specific dependency
     * 
     * @param dependencyId
     *        Identifier of dependency
     * @param queryOptions
     *        QueryOptions object, it contains some query conditions.
     * @return a list of SDependencyMapping objects
     * @throws SDependencyException
     */
    List<SDependencyMapping> getDependencyMappings(long dependencyId, QueryOptions queryOptions) throws SDependencyException;

    /**
     * Get all dependency ids for specific artifact
     * 
     * @param artifactId
     *        Identifier of artifact
     * @param artifactType
     *        Type of artifact
     * @param startIndex
     * @param maxResult
     * @return a list of Long objects
     * @throws SDependencyException
     */
    List<Long> getDependencyIds(long artifactId, ScopeType artifactType, int startIndex, int maxResult) throws SDependencyException;

    // returns the last time an artifact has been impacted by a dependency change: update on a dependency, new/updated/removed mapping...
    /**
     * Get the last time an artifact has been impacted by a dependency change
     * 
     * @param artifactType
     *        Type of artifact
     * @param artifactId
     *        The identifier of artifact
     * @return
     */
    long getLastUpdatedTimestamp(ScopeType artifactType, long artifactId);

    /**
     * Remove the disconnected dependencyMappings.
     * 
     * @param artifactAccessor
     *        ArtifactAccessor object, used to judge artifact exists or not
     * @return a list of SDependencyMapping objects which are deleted
     * @throws SDependencyException
     */
    List<SDependencyMapping> removeDisconnectedDependencyMappings(final ArtifactAccessor artifactAccessor) throws SDependencyException;

    /**
     * Get all disconnected dependencyMappings according to queryOptions.
     * 
     * @param artifactAccessor
     *        ArtifactAccessor object, used to judge an artifact exists or not
     * @param queryOptions
     *        QueryOptions object, it contains some query conditions.
     * @return a list of SDependencyMapping objects disconnected
     * @throws SDependencyException
     */
    List<SDependencyMapping> getDisconnectedDependencyMappings(final ArtifactAccessor artifactAccessor, final QueryOptions queryOptions)
            throws SDependencyException;

    /**
     * @param id
     * @param type
     * @throws SDependencyException
     */
    void deleteDependencies(long id, ScopeType type) throws SDependencyException;

    void refreshClassLoader(ScopeType type, long id) throws SDependencyException;

    /**
     * Update dependencies of this artifact with the given list of dependencies.
     * If a dependency with the same name exists it will update it.
     * If it does not it will create a new one.
     * If the list does not contains a existing dependency of the element it will delete it.
     * 
     * @param id
     * @param type
     * @param dependencies
     * @throws SDependencyException
     */
    void updateDependenciesOfArtifact(long id, ScopeType type, List<SDependency> dependencies) throws SDependencyException;

}
