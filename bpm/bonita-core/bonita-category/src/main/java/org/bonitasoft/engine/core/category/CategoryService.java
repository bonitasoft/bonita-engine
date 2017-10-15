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
package org.bonitasoft.engine.core.category;

import java.util.List;

import org.bonitasoft.engine.core.category.exception.SCategoryAlreadyExistsException;
import org.bonitasoft.engine.core.category.exception.SCategoryCreationException;
import org.bonitasoft.engine.core.category.exception.SCategoryDeletionException;
import org.bonitasoft.engine.core.category.exception.SCategoryException;
import org.bonitasoft.engine.core.category.exception.SCategoryInProcessAlreadyExistsException;
import org.bonitasoft.engine.core.category.exception.SCategoryNotFoundException;
import org.bonitasoft.engine.core.category.exception.SIndexOutOfRangeException;
import org.bonitasoft.engine.core.category.exception.SPageOutOfRangeException;
import org.bonitasoft.engine.core.category.model.SCategory;
import org.bonitasoft.engine.core.category.model.SProcessCategoryMapping;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @since 6.0
 */
public interface CategoryService {

    String CATEGORY = "CATEGORY";

    /**
     * Create a category by give name and description
     * 
     * @param name
     *        The name of category
     * @param description
     *        The description of category
     * @return the category object with id
     * @throws SCategoryAlreadyExistsException
     *         Error thrown if category is already exist
     * @throws SCategoryCreationException
     *         Error thrown if has exceptions during the category creation.
     */
    SCategory createCategory(String name, String description) throws SCategoryAlreadyExistsException, SCategoryCreationException;

    /**
     * Get category by its id
     * 
     * @param id
     *        Identifier of the category
     * @return a category object
     * @throws SCategoryNotFoundException
     *         Error thrown if no category have an id corresponding to the parameter.
     */
    SCategory getCategory(long id) throws SCategoryNotFoundException;

    /**
     * Get category by its name
     * 
     * @param name
     *        Name of the category
     * @return a category object
     * @throws SCategoryNotFoundException
     *         Error thrown if no category have a name corresponding to the parameter.
     */
    SCategory getCategoryByName(String name) throws SCategoryNotFoundException;

    /**
     * Update a category by its id
     * 
     * @param categoryId
     *        Identifier of the category
     * @param newCategory
     *        An category object used to update the categoryId specified category
     * @throws SCategoryNotFoundException
     *         Error thrown if no category have an id corresponding to the parameter.
     * @throws SCategoryException
     *         Error thrown if has exception during the category update
     */
    void updateCategory(long categoryId, EntityUpdateDescriptor descriptor) throws SCategoryNotFoundException, SCategoryException;

    /**
     * Delete a category by its id
     * 
     * @param categoryId
     *        Identifier of the category
     * @throws SCategoryNotFoundException
     *         Error thrown if no category have an id corresponding to the parameter.
     * @throws SCategoryDeletionException
     *         Error thrown if has exception during the category deletion
     */
    void deleteCategory(long categoryId) throws SCategoryNotFoundException, SCategoryDeletionException;

    /**
     * Get the total number of categories
     * 
     * @return The total number of Categories
     * @throws SCategoryException
     */
    long getNumberOfCategories() throws SCategoryException;

    /**
     * Retrieves a list of categories, The returned list is paginated
     * 
     * @param fromIndex
     *        Index of the record to be returned. First record has index 0.
     * @param numberOfCategories
     *        Number of categories per page. Maximum number of categories returned.
     * @param field
     *        The field used by the list order
     * @param order
     *        ASC or DESC
     * @return The list of category objects
     * @throws SPageOutOfRangeException
     *         Error thrown if page is out of the range.
     * @throws SCategoryException
     *         Error thrown if has exception during the category retrieve
     */
    List<SCategory> getCategories(int fromIndex, int numberOfCategories, String field, OrderByType order) throws SPageOutOfRangeException, SCategoryException;

    /**
     * Add a process definition to a category
     * 
     * @param categoryId
     *        Identifier of the category
     * @param processDefinitionId
     *        Identifier of the process definition
     * @throws SCategoryNotFoundException
     *         Error thrown if no category have an id corresponding to the parameter.
     * @throws SCategoryException
     *         Error thrown if has exception during the adding action
     */
    void addProcessDefinitionToCategory(long categoryId, long processDefinitionId) throws SCategoryNotFoundException, SCategoryInProcessAlreadyExistsException,
            SCategoryException;

    /**
     * Add process definitions to a category
     * 
     * @param categoryId
     *        Identifier of the category
     * @param processDefinitionIds
     *        Identifier of the process definition
     * @throws SCategoryNotFoundException
     *         Error thrown if no category have an id corresponding to the parameter.
     * @throws SCategoryException
     *         Error thrown if has exception during the adding action
     * @throws SCategoryInProcessAlreadyExistsException
     */
    void addProcessDefinitionsToCategory(long categoryId, List<Long> processDefinitionIds) throws SCategoryNotFoundException, SCategoryException,
            SCategoryInProcessAlreadyExistsException;

    /**
     * Get number of categories of the specific process definition
     * 
     * @param processDefinitionId
     *        Identifier of the process definition
     * @return number of categories
     * @throws SCategoryException
     *         Error thrown if has exception during the category number retrieve action
     */
    long getNumberOfCategoriesOfProcess(long processDefinitionId) throws SCategoryException;

    /**
     * Get categories for specific process definition, the result list is paginated
     * 
     * @param processId
     *        Identifier of the process definition
     * @param fromIndex
     *        Start index of satisfied record
     * @param numberOfCategories
     *        Number of categories per page. Maximum number of categories returned.
     * @param order
     *        Criterion for order, default order by name
     * @return The matching list of category
     * @throws SCategoryException
     *         Error thrown if has exception during the category retrieve action
     * @throws SIndexOutOfRangeException
     *         Error thrown if index is out of the range.
     */
    List<SCategory> getCategoriesOfProcessDefinition(long processId, int fromIndex, int numberOfCategories, OrderByType order) throws SCategoryException,
            SIndexOutOfRangeException;

    /**
     * Get number of categorized processes
     * 
     * @param processIds
     *        Identifier of the process definition
     * @return the number of categorized processes
     * @throws SCategoryException
     */
    long getNumberOfCategorizedProcessIds(List<Long> processIds) throws SCategoryException;

    /**
     * Get the number of process definition for specific category
     * 
     * @param categoryId
     *        Identifier of the category
     * @return The number of process definition for specific category
     * @throws SCategoryNotFoundException
     *         Error thrown if no category have an id corresponding to the parameter.
     * @throws SCategoryException
     *         Error thrown if has exception during the process definition id retrieve
     */
    long getNumberOfProcessDeploymentInfosOfCategory(long categoryId) throws SBonitaReadException;

    /**
     * Remove specific categories for specific process definition
     * 
     * @param processId
     *        Identifier of the process definition
     * @param categoryIds
     *        Identifiers of the categories
     * @throws SCategoryException
     */
    void removeCategoriesFromProcessDefinition(long processId, List<Long> categoryIds) throws SCategoryException;

    /**
     * Get categories not attached for specific process definition, the result list is paginated
     * 
     * @param processDefinitionId
     * @param fromIndex
     * @param numberOfCategories
     * @param order
     * @return The matching list of category
     * @throws SCategoryException
     */
    List<SCategory> getCategoriesUnrelatedToProcessDefinition(long processDefinitionId, int fromIndex, int numberOfCategories, OrderByType order)
            throws SCategoryException;

    /**
     * Get number of categories not attached of the specific process definition
     * 
     * @param processDefinitionId
     * @return number of categories
     * @throws SCategoryException
     *         Error thrown if has exception during the category number retrieve action
     */
    long getNumberOfCategoriesUnrelatedToProcess(long processDefinitionId) throws SCategoryException;

    /**
     * Search process category mappings corresponding to criteria
     * 
     * @param queryOptions
     * @return List of process category mappings
     * @throws SBonitaReadException
     * @since 6.1
     */
    List<SProcessCategoryMapping> searchProcessCategoryMappings(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * @param mappings
     * @return Number of deleted category mapping
     * @since 6.1
     */
    long deleteProcessCategoryMappings(List<SProcessCategoryMapping> mappings);

}
