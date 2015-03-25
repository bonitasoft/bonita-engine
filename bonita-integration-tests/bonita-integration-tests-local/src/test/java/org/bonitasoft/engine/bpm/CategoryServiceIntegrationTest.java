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
package org.bonitasoft.engine.bpm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.category.CategoryService;
import org.bonitasoft.engine.core.category.exception.SCategoryAlreadyExistsException;
import org.bonitasoft.engine.core.category.exception.SCategoryCreationException;
import org.bonitasoft.engine.core.category.exception.SCategoryNotFoundException;
import org.bonitasoft.engine.core.category.model.SCategory;
import org.bonitasoft.engine.core.category.model.builder.SCategoryUpdateBuilder;
import org.bonitasoft.engine.core.category.model.builder.SCategoryUpdateBuilderFactory;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Test;

/**
 * @author Yanyan Liu
 */
public class CategoryServiceIntegrationTest extends CommonBPMServicesTest {

    private static CategoryService categoryService;

    private static TransactionService transactionService;

    public CategoryServiceIntegrationTest() {
        categoryService = getTenantAccessor().getCategoryService();
        transactionService = getTransactionService();
    }

    @Test
    public void createCategory() throws Exception {
        final String name = "categoryName";
        final String description = "test create category";
        transactionService.begin();
        final SCategory category = categoryService.createCategory(name, description);
        assertNotNull(category);
        assertEquals(name, category.getName());
        assertEquals(description, category.getDescription());
        categoryService.deleteCategory(category.getId());
        transactionService.complete();
    }

    @Test(expected = SCategoryAlreadyExistsException.class)
    public void createCategoryWithSCategoryAlreadyExistsException() throws Exception {
        final String name = "categoryTestExceptionName";
        final String description = "test create category with SCategoryAlreadyExistsException";
        transactionService.begin();
        final SCategory category = categoryService.createCategory(name, description);
        assertNotNull(category);
        assertEquals(name, category.getName());
        try {
            categoryService.createCategory(name, description);
        } finally {
            categoryService.deleteCategory(category.getId());
            transactionService.complete();
        }
    }

    @Test(expected = SCategoryCreationException.class)
    public void createCategoryWithSCategoryCreationException() throws Exception {
        final String name = null;
        final String description = "test create category with SCategoryCreationException";
        transactionService.begin();
        try {
            categoryService.createCategory(name, description);
        } finally {
            transactionService.complete();
        }
    }

    @Test
    public void getCategory() throws Exception {
        final String name = "categoryName";
        final String description = "test retrieve category";
        transactionService.begin();
        // create
        final SCategory category = categoryService.createCategory(name, description);
        assertNotNull(category);
        // retrieve test
        final SCategory retrievedCategory = categoryService.getCategory(category.getId());
        assertNotNull(retrievedCategory);
        assertEquals(name, retrievedCategory.getName());
        assertEquals(description, retrievedCategory.getDescription());
        // delete
        categoryService.deleteCategory(category.getId());
        transactionService.complete();
    }

    @Test
    public void getCategoryByName() throws Exception {
        final String name = "categoryName";
        final String description = "test get category by name";
        transactionService.begin();
        // create
        final SCategory category1 = categoryService.createCategory(name, description);
        assertNotNull(category1);
        // retrieve test
        final SCategory category2 = categoryService.getCategoryByName(category1.getName());
        assertNotNull(category2);
        assertEquals(category1.getId(), category2.getId());
        assertEquals(name, category2.getName());
        assertEquals(description, category2.getDescription());
        // delete
        categoryService.deleteCategory(category2.getId());
        transactionService.complete();
    }

    @Test
    public void updateCategory() throws Exception {
        final String name = "categoryName";
        final String description = "test update category";
        transactionService.begin();
        // create
        final SCategory category1 = categoryService.createCategory(name, description);
        assertNotNull(category1);
        final long categoryId = category1.getId();
        // update
        final String newName = "updatedName";
        final String newDescription = "updatedDescription";

        final SCategoryUpdateBuilder updateBuilder = BuilderFactory.get(SCategoryUpdateBuilderFactory.class).createNewInstance();
        updateBuilder.updateName(newName).updateDescription(newDescription);
        categoryService.updateCategory(categoryId, updateBuilder.done());
        transactionService.complete();

        transactionService.begin();
        // test
        final SCategory updatedCategory = categoryService.getCategory(categoryId);
        assertNotNull(updatedCategory);
        assertEquals(newName, updatedCategory.getName());
        assertEquals(newDescription, updatedCategory.getDescription());
        // delete
        categoryService.deleteCategory(categoryId);
        transactionService.complete();
    }

    @Test(expected = SCategoryNotFoundException.class)
    public void updateCategoryWithSCategoryNotFoundException() throws Exception {
        final long categoryId = 1;
        final String newName = "updatedName";
        final String newDescription = "updatedDescription";
        final SCategoryUpdateBuilder updateBuilder = BuilderFactory.get(SCategoryUpdateBuilderFactory.class).createNewInstance();
        updateBuilder.updateName(newName).updateDescription(newDescription);

        transactionService.begin();
        categoryService.updateCategory(categoryId, updateBuilder.done());
        transactionService.complete();
    }

    @Test
    public void deleteCategory() throws Exception {
        final String name = "categoryName_delete";
        final String description = "test delete category";
        transactionService.begin();
        // create
        final SCategory category = categoryService.createCategory(name, description);
        assertNotNull(category);
        assertEquals(name, category.getName());
        // delete test
        categoryService.deleteCategory(category.getId());
        transactionService.complete();

        transactionService.begin();
        try {
            categoryService.getCategory(category.getId());
            fail();
        } catch (final SCategoryNotFoundException e) {

        }
        transactionService.complete();
    }

    @Test
    public void getNumberOfCategories() throws Exception {
        transactionService.begin();
        long count = categoryService.getNumberOfCategories();
        assertEquals(0, count);
        // create
        final String name = "categoryName";
        final String description = "category description";
        final List<SCategory> categoryList = createCategories(3, name, description);
        assertNotNull(categoryList);
        assertEquals(3, categoryList.size());
        count = categoryService.getNumberOfCategories();
        assertEquals(3, count);
        // delete test
        for (final SCategory category : categoryList) {
            categoryService.deleteCategory(category.getId());
        }
        transactionService.complete();
    }

    @Test
    // this should be client test
    public void getCategoriesInOrder() throws Exception {
        final String name = "categoryName";
        final String description = "test get categories in order";
        transactionService.begin();
        // create
        final List<SCategory> categoryList = createCategories(5, name, description);
        assertNotNull(categoryList);
        assertEquals(5, categoryList.size());
        // test name ASC
        final List<SCategory> categoryList1 = categoryService.getCategories(0, 2, "name", OrderByType.ASC);
        assertNotNull(categoryList1);
        assertEquals(2, categoryList1.size());
        assertEquals("categoryName1", categoryList1.get(0).getName());
        assertEquals("categoryName2", categoryList1.get(1).getName());

        final List<SCategory> categoryList2 = categoryService.getCategories(1, 2, "name", OrderByType.ASC);
        assertNotNull(categoryList2);
        assertEquals(2, categoryList2.size());
        assertEquals("categoryName2", categoryList2.get(0).getName());
        assertEquals("categoryName3", categoryList2.get(1).getName());

        final List<SCategory> categoryList3 = categoryService.getCategories(3, 2, "name", OrderByType.ASC);
        assertNotNull(categoryList3);
        assertEquals(2, categoryList3.size());
        assertEquals("categoryName4", categoryList3.get(0).getName());
        assertEquals("categoryName5", categoryList3.get(1).getName());

        final List<SCategory> categoryList4 = categoryService.getCategories(5, 2, "name", OrderByType.ASC);
        assertEquals(0, categoryList4.size());

        // test name DESC
        final List<SCategory> categoryList5 = categoryService.getCategories(0, 2, "name", OrderByType.DESC);
        assertNotNull(categoryList5);
        assertEquals(2, categoryList5.size());
        assertEquals("categoryName5", categoryList5.get(0).getName());
        assertEquals("categoryName4", categoryList5.get(1).getName());

        // delete test
        for (final SCategory category : categoryList) {
            categoryService.deleteCategory(category.getId());
        }

        transactionService.complete();
    }

    @Test(expected = SCategoryNotFoundException.class)
    public void addProcessDefinitionToCategoryWithSCategoryNotFoundException() throws Exception {
        // generate the meaningful of ProcessDefinition id
        final SProcessDefinition processDefinition = createSProcessDefinition("processName", "test category not found exceptioin");

        transactionService.begin();
        try {
            categoryService.addProcessDefinitionToCategory(1, processDefinition.getId());
        } finally {
            transactionService.complete();

            // Clean-up
            deleteSProcessDefinition(processDefinition);
        }
    }

    @Test
    public void getNumberOfCategories4Process() throws Exception {
        // generate a meaningful processDefinitionId
        final long processDefinitionId = createSProcessDefinition("processName", "test get number of categories of process").getId();

        transactionService.begin();
        long count = categoryService.getNumberOfCategoriesOfProcess(processDefinitionId);
        assertEquals(0, count);

        final String name = "categoryName";
        final String description = "category description";
        // create category
        final List<SCategory> categoryList = createCategories(2, name, description);
        assertNotNull(categoryList);
        assertEquals(2, categoryList.size());
        // add process definition info to category
        for (final SCategory category : categoryList) {
            categoryService.addProcessDefinitionToCategory(category.getId(), processDefinitionId);
        }
        // check
        count = categoryService.getNumberOfCategoriesOfProcess(processDefinitionId);
        assertEquals(2, count);
        // delete category and process definition
        for (final SCategory category : categoryList) {
            categoryService.deleteCategory(category.getId());
        }
        transactionService.complete();

        // Clean-up
        deleteSProcessDefinition(processDefinitionId);
    }

    @Test
    public void getNumberOfCategoriesUnrelatedToProcess() throws Exception {
        final List<SProcessDefinition> processDefinitions = createSProcessDefinitions(2, "processName", "test get number of categories of process");

        transactionService.begin();
        // generate a meaningful processDefinitionId
        final long processDefinitionId = processDefinitions.get(0).getId();
        long count = categoryService.getNumberOfCategoriesUnrelatedToProcess(processDefinitionId);
        assertEquals(0, count);

        final String name = "categoryName";
        final String description = "category description";
        // create category
        final List<SCategory> categoryList = createCategories(4, name, description);
        assertNotNull(categoryList);
        assertEquals(4, categoryList.size());
        // add process definition info to category
        categoryService.addProcessDefinitionToCategory(categoryList.get(0).getId(), processDefinitionId);
        categoryService.addProcessDefinitionToCategory(categoryList.get(1).getId(), processDefinitionId);

        // check
        count = categoryService.getNumberOfCategoriesUnrelatedToProcess(processDefinitionId);
        assertEquals(2, count);

        count = categoryService.getNumberOfCategoriesUnrelatedToProcess(processDefinitions.get(1).getId());
        assertEquals(4, count);

        // delete category and process definition
        for (final SCategory category : categoryList) {
            categoryService.deleteCategory(category.getId());
        }
        transactionService.complete();

        // Clean-up
        deleteSProcessDefinitions(processDefinitions);
    }

    @Test
    public void getCategoriesUnrelatedToProcess() throws Exception {
        final List<SProcessDefinition> processDefinitions = createSProcessDefinitions(2, "processName", "test get number of categories of process");

        transactionService.begin();
        // generate a meaningful processDefinitionId
        final long processDefinitionId = processDefinitions.get(0).getId();
        List<SCategory> categories = categoryService.getCategoriesUnrelatedToProcessDefinition(processDefinitionId, 0, 4, OrderByType.ASC);
        assertEquals(0, categories.size());

        final String name = "categoryName";
        final String description = "category description";
        // create category
        final List<SCategory> categoryList = createCategories(4, name, description);
        assertNotNull(categoryList);
        assertEquals(4, categoryList.size());
        // add process definition info to category
        categoryService.addProcessDefinitionToCategory(categoryList.get(0).getId(), processDefinitionId);
        categoryService.addProcessDefinitionToCategory(categoryList.get(1).getId(), processDefinitionId);

        // check
        categories = categoryService.getCategoriesUnrelatedToProcessDefinition(processDefinitionId, 0, 4, OrderByType.ASC);
        assertEquals(2, categories.size());
        assertEquals(categoryList.get(2).getId(), categories.get(0).getId());
        assertEquals(categoryList.get(3).getId(), categories.get(1).getId());

        categories = categoryService.getCategoriesUnrelatedToProcessDefinition(processDefinitions.get(1).getId(), 0, 4, OrderByType.ASC);
        assertEquals(4, categories.size());
        assertEquals(categoryList.get(0).getId(), categories.get(0).getId());
        assertEquals(categoryList.get(1).getId(), categories.get(1).getId());
        assertEquals(categoryList.get(2).getId(), categories.get(2).getId());
        assertEquals(categoryList.get(3).getId(), categories.get(3).getId());

        // delete category and process definition
        for (final SCategory category : categoryList) {
            categoryService.deleteCategory(category.getId());
        }
        transactionService.complete();

        // Clean-up
        deleteSProcessDefinitions(processDefinitions);
    }

    @Test
    public void getCategoriesOfProcessDefinition() throws Exception {
        final SProcessDefinition processDefinition = createSProcessDefinitions(1, "processName", "test get categores of process definition").get(0);

        transactionService.begin();
        // generate the meaningful processDefinition id and delete it in the end
        final long processDefinitionId = processDefinition.getId();
        List<SCategory> categoryList = categoryService.getCategoriesOfProcessDefinition(processDefinitionId, 0, 2, OrderByType.ASC);
        assertEquals(0, categoryList.size());
        final String name = "categoryName";
        final String description = "test get categories of process definition";
        // create categories, add process definition to categories
        categoryList = createCategories(5, name, description);
        transactionService.complete();

        transactionService.begin();
        for (final SCategory category : categoryList) {
            categoryService.addProcessDefinitionToCategory(category.getId(), processDefinitionId);
        }
        transactionService.complete();

        transactionService.begin();
        // test
        final List<SCategory> categoryList1 = categoryService.getCategoriesOfProcessDefinition(processDefinitionId, 0, 2, OrderByType.ASC);
        assertNotNull(categoryList1);
        assertEquals(2, categoryList1.size());
        assertEquals("categoryName1", categoryList1.get(0).getName());
        assertEquals("categoryName2", categoryList1.get(1).getName());

        final List<SCategory> categoryList2 = categoryService.getCategoriesOfProcessDefinition(processDefinitionId, 1, 2, OrderByType.ASC);
        assertNotNull(categoryList2);
        assertEquals(2, categoryList2.size());
        assertEquals("categoryName2", categoryList2.get(0).getName());
        assertEquals("categoryName3", categoryList2.get(1).getName());

        final List<SCategory> categoryList3 = categoryService.getCategoriesOfProcessDefinition(processDefinitionId, 2, 2, OrderByType.ASC);
        assertNotNull(categoryList3);
        assertEquals(2, categoryList3.size());
        assertEquals("categoryName3", categoryList3.get(0).getName());
        assertEquals("categoryName4", categoryList3.get(1).getName());

        final List<SCategory> categoryList4 = categoryService.getCategoriesOfProcessDefinition(processDefinitionId, 4, 2, OrderByType.ASC);
        assertNotNull(categoryList4);
        assertEquals(1, categoryList4.size());
        assertEquals("categoryName5", categoryList4.get(0).getName());

        final List<SCategory> categories = categoryService.getCategoriesOfProcessDefinition(processDefinitionId, 5, 2, OrderByType.ASC);
        assertTrue(categories.isEmpty());
        final List<SCategory> categoryList5 = categoryService.getCategoriesOfProcessDefinition(processDefinitionId, 0, 3, OrderByType.DESC);
        assertNotNull(categoryList5);
        assertEquals(3, categoryList5.size());
        assertEquals("categoryName5", categoryList5.get(0).getName());
        assertEquals("categoryName4", categoryList5.get(1).getName());
        assertEquals("categoryName3", categoryList5.get(2).getName());

        // delete categories and process
        for (final SCategory category : categoryList) {
            categoryService.deleteCategory(category.getId());
        }
        transactionService.complete();

        // Clean-up
        deleteSProcessDefinition(processDefinitionId);
    }

    private List<SCategory> createCategories(final int count, final String name, final String description) throws Exception {
        final List<SCategory> categoryList = new ArrayList<SCategory>();
        for (int i = 1; i <= count; i++) {
            final SCategory category = categoryService.createCategory(name + i, description + i);
            categoryList.add(category);
        }
        return categoryList;
    }

}
