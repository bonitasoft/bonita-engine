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
package org.bonitasoft.engine.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.category.Category;
import org.bonitasoft.engine.bpm.category.CategoryCriterion;
import org.bonitasoft.engine.bpm.category.CategoryNotFoundException;
import org.bonitasoft.engine.bpm.category.CategoryUpdater;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeployException;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProcessCategoryIT extends TestWithTechnicalUser {

    protected static final String USERNAME = "dwight";

    protected static final String PASSWORD = "Schrute";

    private final String name = "category";

    private final String description = "description";

    List<Category> categories;

    List<ProcessDefinition> processDefinitions;

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        categories = new ArrayList<Category>();
        processDefinitions = new ArrayList<ProcessDefinition>();
    }

    @Override
    @After
    public void after() throws Exception {
        for (final Category category : categories) {
            getProcessAPI().deleteCategory(category.getId());
        }
        for (final ProcessDefinition processDefinitionId : processDefinitions) {
            deleteProcess(processDefinitionId);
        }
        super.after();
    }

    @Cover(classes = { ProcessAPI.class, Category.class }, concept = BPMNConcept.PROCESS, keywords = { "Create", "Category" }, jira = "")
    @Test
    public void createCategory() throws Exception {
        final Category category = getProcessAPI().createCategory(name, description);
        categories.add(category);
        assertNotNull(category);
        assertEquals(name, category.getName());
        final long categoryId = category.getId();
        final Category rCategory = getProcessAPI().getCategory(categoryId);
        assertNotNull(rCategory);
        assertEquals(name, rCategory.getName());
    }

    @Cover(classes = { ProcessAPI.class, Category.class, User.class }, concept = BPMNConcept.PROCESS, keywords = { "Create", "Category", "Creator" }, jira = "ENGINE-619")
    @Test
    public void createCategoryWithCreatorAsAnID() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);
        logoutOnTenant();
        loginOnDefaultTenantWith(USERNAME, PASSWORD);

        final Category category = getProcessAPI().createCategory(name, description);
        categories.add(category);
        assertNotNull(category);
        assertEquals(name, category.getName());
        assertEquals(user.getId(), category.getCreator());

        deleteUser(user);
    }

    @Cover(classes = { ProcessAPI.class, Category.class, AlreadyExistsException.class }, concept = BPMNConcept.PROCESS, keywords = { "Create", "Category",
            "Exception" }, jira = "")
    @Test(expected = AlreadyExistsException.class)
    public void createCategoryWithCategoryAlreadyExistException() throws Exception {
        final Category category = getProcessAPI().createCategory(name, description);
        categories.add(category);
        assertNotNull(category);
        assertEquals(name, category.getName());
        getProcessAPI().createCategory(name, description);
    }

    @Cover(classes = { ProcessAPI.class, Category.class, CreationException.class }, concept = BPMNConcept.PROCESS, keywords = { "Create", "Category",
            "Exception" }, jira = "")
    @Test(expected = CreationException.class)
    public void createCategoryWithCategoryCreationException() throws Exception {
        getProcessAPI().createCategory(null, description);
    }

    @Cover(classes = { ProcessAPI.class, Category.class }, concept = BPMNConcept.PROCESS, keywords = { "Number", "Category" }, jira = "")
    @Test
    public void getNumberOfCategories() throws Exception {
        final Category category1 = getProcessAPI().createCategory(name + 1, description);
        categories.add(category1);
        final Category category2 = getProcessAPI().createCategory(name + 2, description);
        categories.add(category2);

        final long categoriesCount = getProcessAPI().getNumberOfCategories();
        assertEquals(2, categoriesCount);
    }

    @Cover(classes = { ProcessAPI.class, Category.class }, concept = BPMNConcept.PROCESS, keywords = { "Category", "Existed" }, jira = "")
    @Test
    public void getCategory() throws Exception {
        final Category category = getProcessAPI().createCategory(name, description);
        categories.add(category);
        assertNotNull(category);
        assertEquals(name, category.getName());
        final long categoryId = category.getId();
        final Category rCategory = getProcessAPI().getCategory(categoryId);
        assertNotNull(rCategory);
        assertEquals(categoryId, rCategory.getId());
        assertEquals(name, rCategory.getName());
        assertEquals(description, rCategory.getDescription());
    }

    @Cover(classes = { ProcessAPI.class, CategoryNotFoundException.class }, concept = BPMNConcept.PROCESS, keywords = { "Category", "Unexisted", "Exception" }, jira = "")
    @Test(expected = CategoryNotFoundException.class)
    public void getCategoryWithCategoryNotFoundException() throws Exception {
        final Category category = getProcessAPI().createCategory(name, description);
        categories.add(category);
        assertNotNull(category);
        assertEquals(name, category.getName());
        getProcessAPI().getCategory(category.getId() + 1);
    }

    @Cover(classes = { ProcessAPI.class, Category.class }, concept = BPMNConcept.PROCESS, keywords = { "Category", "Existed", "Several" }, jira = "")
    @Test
    public void getCategories() throws Exception {
        final Category category1 = getProcessAPI().createCategory("category1", description);
        categories.add(category1);
        final Category category2 = getProcessAPI().createCategory("category2", description);
        categories.add(category2);
        final Category category3 = getProcessAPI().createCategory("category3", description);
        categories.add(category3);
        final Category category4 = getProcessAPI().createCategory("category4", description);
        categories.add(category4);
        final Category category5 = getProcessAPI().createCategory("category5", description);
        categories.add(category5);

        List<Category> categoriesNameAsc = getProcessAPI().getCategories(0, 2, CategoryCriterion.NAME_ASC);
        assertEquals(2, categoriesNameAsc.size());
        assertEquals(category1.getName(), categoriesNameAsc.get(0).getName());
        assertEquals(category2.getName(), categoriesNameAsc.get(1).getName());

        categoriesNameAsc = getProcessAPI().getCategories(2, 2, CategoryCriterion.NAME_ASC);
        assertEquals(2, categoriesNameAsc.size());
        assertEquals(category3.getName(), categoriesNameAsc.get(0).getName());
        assertEquals(category4.getName(), categoriesNameAsc.get(1).getName());

        categoriesNameAsc = getProcessAPI().getCategories(4, 2, CategoryCriterion.NAME_ASC);
        assertEquals(1, categoriesNameAsc.size());
        assertEquals(category5.getName(), categoriesNameAsc.get(0).getName());

        categoriesNameAsc = getProcessAPI().getCategories(6, 2, CategoryCriterion.NAME_ASC);
        assertEquals(0, categoriesNameAsc.size());
    }

    @Cover(classes = { ProcessAPI.class, Category.class }, concept = BPMNConcept.PROCESS, keywords = { "Category", "Update" }, jira = "")
    @Test
    public void updateCategory() throws Exception {
        final Category oldCategory = getProcessAPI().createCategory(name, description);
        categories.add(oldCategory);

        final String newName = "updatedName";
        final String newDescription = "updatedDescription";
        final CategoryUpdater updater = new CategoryUpdater();
        updater.setName(newName).setDescription(newDescription);
        getProcessAPI().updateCategory(oldCategory.getId(), updater);

        final Category categoryUpdated = getProcessAPI().getCategory(oldCategory.getId());
        assertNotNull(categoryUpdated);
        assertEquals(newName, categoryUpdated.getName());
        assertEquals(newDescription, categoryUpdated.getDescription());
    }

    @Cover(classes = { ProcessAPI.class, CategoryNotFoundException.class }, concept = BPMNConcept.PROCESS, keywords = { "Category", "Unexisted", "Exception",
            "Update" }, jira = "")
    @Test(expected = CategoryNotFoundException.class)
    public void updateCategoryWithCategoryNotFoundException() throws Exception {
        final Category category = getProcessAPI().createCategory(name, description);
        categories.add(category);
        final CategoryUpdater updater = new CategoryUpdater();
        updater.setName("updatedName").setDescription("updatedDescription");
        getProcessAPI().updateCategory(category.getId() + 1, updater);
    }

    @Cover(classes = { ProcessAPI.class, UpdateException.class }, concept = BPMNConcept.PROCESS, keywords = { "Category", "Exception", "Update" }, jira = "")
    @Test(expected = UpdateException.class)
    public void updateCategoryWithCategoryUpdateException() throws Exception {
        getProcessAPI().updateCategory(0, null);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Category", "Delete" }, jira = "")
    @Test(expected = CategoryNotFoundException.class)
    public void deleteCategory() throws Exception {
        final Category category = getProcessAPI().createCategory(name, description);
        getProcessAPI().deleteCategory(category.getId());
        getProcessAPI().getCategory(category.getId());
    }

    @Cover(classes = { ProcessAPI.class, CategoryNotFoundException.class }, concept = BPMNConcept.PROCESS, keywords = { "Category", "Unexisted", "Exception",
            "Delete" }, jira = "")
    @Test(expected = DeletionException.class)
    public void deleteCategoryWithCategoryNotFoundException() throws Exception {
        getProcessAPI().deleteCategory(Long.MAX_VALUE);
    }

    @Cover(classes = { ProcessAPI.class, DeletionException.class }, concept = BPMNConcept.PROCESS, keywords = { "Category", "Exception", "Delete" }, jira = "")
    @Test(expected = DeletionException.class)
    public void deleteCategoryWithCategoryDeletionException() throws Exception {
        getProcessAPI().deleteCategory(0);
    }

    @Test
    public void processDefinitionWithCategoryPagination() throws Exception {
        final Category category = getProcessAPI().createCategory(name, description);
        categories.add(category);
        // test
        final List<ProcessDeploymentInfo> processDeploymentInfos = getProcessAPI().getProcessDeploymentInfosOfCategory(category.getId(), 0, 10,
                ProcessDeploymentInfoCriterion.ACTIVATION_STATE_ASC);
        assertEquals(0, processDeploymentInfos.size());
    }

    @Test
    public void removeSeveralCategoriesToProcessDefinition() throws Exception {
        // generate category id;
        final Category category1 = getProcessAPI().createCategory("Human resources", "Category for personnel Management");
        categories.add(category1);
        final Category category2 = getProcessAPI().createCategory("Travel Service", "Category for all related travel matters");
        categories.add(category2);
        final Category category3 = getProcessAPI().createCategory("Cleaning Service", "Category for all related travel matters");
        categories.add(category3);
        // generate process definition id
        final ProcessDefinition processDefinition = generateProcessDefinition(1, "processName", "version").get(0);
        processDefinitions.add(processDefinition);
        final long processDefinitionId = processDefinition.getId();

        // test number of current categories form this process:
        getProcessAPI().addCategoriesToProcess(processDefinitionId, Arrays.asList(new Long[] { category1.getId(), category2.getId(), category3.getId() }));
        List<Category> categories = getProcessAPI().getCategoriesOfProcessDefinition(processDefinitionId, 0, 10, CategoryCriterion.NAME_DESC);
        assertEquals(3, categories.size());

        getProcessAPI().removeCategoriesFromProcess(processDefinitionId, Arrays.asList(new Long[] { category1.getId(), category3.getId() }));
        // test number of current categories form this process:
        categories = getProcessAPI().getCategoriesOfProcessDefinition(processDefinitionId, 0, 10, CategoryCriterion.NAME_DESC);
        assertEquals(1, categories.size());
        // check if the remaining one is the good one:
        assertEquals(category2.getId(), categories.get(0).getId());
    }

    @Test
    public void addSeveralCategoriesToProcessDefinition() throws Exception {
        final Category category1 = getProcessAPI().createCategory("Human resources", "Category for personnel Management");
        categories.add(category1);
        final Category category2 = getProcessAPI().createCategory("Travel Service", "Category for all related travel matters");
        categories.add(category2);
        final ProcessDefinition processDefinition = generateProcessDefinition(1, "processName", "version").get(0);
        processDefinitions.add(processDefinition);
        // test
        getProcessAPI().addCategoriesToProcess(processDefinition.getId(), Arrays.asList(new Long[] { category1.getId(), category2.getId() }));
        final List<Category> categories = getProcessAPI().getCategoriesOfProcessDefinition(processDefinition.getId(), 0, 10, CategoryCriterion.NAME_DESC);
        assertEquals(2, categories.size());
        assertEquals(category1.getId(), categories.get(1).getId());
        assertEquals(category2.getId(), categories.get(0).getId());
    }

    @Test
    public void addProcessDefinitionToCategory() throws Exception {
        // generate category id;
        final Category category = getProcessAPI().createCategory(name, description);
        categories.add(category);
        final long categoryId = category.getId();
        // generate process definition id
        final ProcessDefinition processDefinition = generateProcessDefinition(1, "processName", "version").get(0);
        processDefinitions.add(processDefinition);
        // test
        getProcessAPI().addProcessDefinitionToCategory(categoryId, processDefinition.getId());
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfosOfCategory(categoryId, 0, 10,
                ProcessDeploymentInfoCriterion.ACTIVATION_STATE_ASC).get(0);
        assertEquals(processDefinition.getName(), processDeploymentInfo.getName());
        assertEquals(processDefinition.getVersion(), processDeploymentInfo.getVersion());
    }

    @Test(expected = CreationException.class)
    public void addProcessDefinitionToUnknownCategory() throws Exception {
        // generate process definition id
        final ProcessDefinition processDefinition = generateProcessDefinition(1, "processName", "version").get(0);
        processDefinitions.add(processDefinition);
        // test
        getProcessAPI().addProcessDefinitionToCategory(0, processDefinition.getId());
    }

    @Test(expected = CreationException.class)
    public void addUnknowProcessDefinitionToCategory() throws Exception {
        // generate category id;
        final Category category = getProcessAPI().createCategory(name, description);
        categories.add(category);
        // test
        getProcessAPI().addProcessDefinitionToCategory(category.getId(), 0);
    }

    @Test
    public void addProcessDefinitionosToCategory() throws Exception {
        // generate category id;
        final Category category = getProcessAPI().createCategory(name, description);
        categories.add(category);
        final long categoryId = category.getId();
        // generate process definition id
        processDefinitions = generateProcessDefinition(3, "process", "version");
        final List<Long> processDefinitionIds = new ArrayList<Long>();
        for (final ProcessDefinition processDefinition : processDefinitions) {
            processDefinitionIds.add(processDefinition.getId());
        }
        // test
        getProcessAPI().addProcessDefinitionsToCategory(categoryId, processDefinitionIds);
        final List<ProcessDeploymentInfo> processDeploymentInfoList = getProcessAPI().getProcessDeploymentInfosOfCategory(categoryId, 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertNotNull(processDeploymentInfoList);
        assertEquals(3, processDeploymentInfoList.size());
    }

    @Test
    public void getNumberOfCategoriesByProcessDefinition() throws Exception {
        // generate categories
        categories = generateCategory(3, "categoryName", "test get number of categories by process definition");
        // generate process definition
        final ProcessDefinition processDefinition = generateProcessDefinition(1, "processName", "version").get(0);
        processDefinitions.add(processDefinition);
        final long processDefinitionId = processDefinition.getId();
        // test
        for (final Category category : categories) {
            getProcessAPI().addProcessDefinitionToCategory(category.getId(), processDefinitionId);
        }
        final long categoryNumber = getProcessAPI().getNumberOfCategories(processDefinitionId);
        assertEquals(3, categoryNumber);
    }

    @Test
    public void getNumberOfCategoriesOfInexistentProcess() {
        final long numberOfCategories = getProcessAPI().getNumberOfCategories(Long.MAX_VALUE);
        assertEquals(0, numberOfCategories);
    }

    @Test
    public void getNumberOfProcessesInCategory() throws Exception {
        // generate categories
        final Category category = generateCategory(1, "categoryName", "test get number of processes in category").get(0);
        categories.add(category);
        // generate process definition
        processDefinitions = generateProcessDefinition(3, "processName", "version");
        // test
        for (final ProcessDefinition processDefinition : processDefinitions) {
            getProcessAPI().addProcessDefinitionToCategory(category.getId(), processDefinition.getId());
        }
        final long processNumber = getProcessAPI().getNumberOfProcessDefinitionsOfCategory(category.getId());
        assertEquals(3, processNumber);
    }

    @Test
    public void getNumberOfProcessesInCategoryWithInexistentCategory() {
        final long processesInCategory = getProcessAPI().getNumberOfProcessDefinitionsOfCategory(Long.MAX_VALUE);
        assertEquals(0, processesInCategory);
    }

    @Test
    public void getProcessDeploymentInfosOfCategory() throws Exception {
        // generate category
        final Category category = getProcessAPI().createCategory(name, description);
        categories.add(category);
        final long categoryId = category.getId();
        // generate process definitions
        processDefinitions = generateProcessDefinition(3, "process", "version");
        final List<Long> processDefinitionIds = new ArrayList<Long>();
        for (final ProcessDefinition processDefinition : processDefinitions) {
            processDefinitionIds.add(processDefinition.getId());
        }
        // add process definitions to category
        getProcessAPI().addProcessDefinitionsToCategory(categoryId, processDefinitionIds);
        // test
        final List<ProcessDeploymentInfo> processDeploymentInfoList_NameASC = getProcessAPI().getProcessDeploymentInfosOfCategory(categoryId, 0, 3,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertNotNull(processDeploymentInfoList_NameASC);
        assertEquals(3, processDeploymentInfoList_NameASC.size());
        assertEquals("process1", processDeploymentInfoList_NameASC.get(0).getName());
        assertEquals("process2", processDeploymentInfoList_NameASC.get(1).getName());
        assertEquals("process3", processDeploymentInfoList_NameASC.get(2).getName());

        final List<ProcessDeploymentInfo> processDeploymentInfoList_NameDESC = getProcessAPI().getProcessDeploymentInfosOfCategory(categoryId, 0, 3,
                ProcessDeploymentInfoCriterion.NAME_DESC);
        assertNotNull(processDeploymentInfoList_NameDESC);
        assertEquals(3, processDeploymentInfoList_NameDESC.size());
        assertEquals("process3", processDeploymentInfoList_NameDESC.get(0).getName());
        assertEquals("process2", processDeploymentInfoList_NameDESC.get(1).getName());
        assertEquals("process1", processDeploymentInfoList_NameDESC.get(2).getName());

        final List<ProcessDeploymentInfo> outOfRangeList = getProcessAPI().getProcessDeploymentInfosOfCategory(categoryId, 3, 3,
                ProcessDeploymentInfoCriterion.NAME_DESC);
        assertEquals(0, outOfRangeList.size());
    }

    @Test
    public void getCategoriesOfProcessDefinition() throws Exception {
        // generate categories
        categories = generateCategory(3, "category", "test get categories of process definition ");
        // generate process definition
        final ProcessDefinition processDefinition = generateProcessDefinition(1, "process", "version").get(0);
        processDefinitions.add(processDefinition);
        final long processDefinitionId = processDefinition.getId();

        // add
        for (final Category category : categories) {
            getProcessAPI().addProcessDefinitionToCategory(category.getId(), processDefinitionId);
        }
        // test
        final List<Category> categoryList_ASC = getProcessAPI().getCategoriesOfProcessDefinition(processDefinitionId, 0, 10, CategoryCriterion.NAME_ASC);
        assertNotNull(categoryList_ASC);
        assertEquals(3, categoryList_ASC.size());
        assertEquals("category1", categoryList_ASC.get(0).getName());
        assertEquals("category2", categoryList_ASC.get(1).getName());
        assertEquals("category3", categoryList_ASC.get(2).getName());

        final List<Category> categoryList_DESC = getProcessAPI().getCategoriesOfProcessDefinition(processDefinitionId, 0, 10, CategoryCriterion.NAME_DESC);
        assertNotNull(categoryList_DESC);
        assertEquals(3, categoryList_DESC.size());
        assertEquals("category3", categoryList_DESC.get(0).getName());
        assertEquals("category2", categoryList_DESC.get(1).getName());
        assertEquals("category1", categoryList_DESC.get(2).getName());

        final List<Category> outOfRangeCategories = getProcessAPI().getCategoriesOfProcessDefinition(processDefinitionId, 3, 10, CategoryCriterion.NAME_ASC);
        assertEquals(0, outOfRangeCategories.size());
    }

    @Cover(classes = { ProcessAPI.class, Category.class }, concept = BPMNConcept.PROCESS, keywords = { "Get", "Category", "ProcessDefinition" }, jira = "")
    @Test
    public void getCategoriesUnrelatedToProcessDefinition() throws Exception {
        // generate categories
        categories = generateCategory(3, "category", "test get categories of process definition ");
        // generate process definition
        processDefinitions = generateProcessDefinition(3, "process", "version");
        final long processDefinitionId = processDefinitions.get(0).getId();

        // add
        getProcessAPI().addProcessDefinitionToCategory(categories.get(1).getId(), processDefinitions.get(1).getId());
        getProcessAPI().addProcessDefinitionToCategory(categories.get(1).getId(), processDefinitions.get(2).getId());
        getProcessAPI().addProcessDefinitionToCategory(categories.get(2).getId(), processDefinitions.get(2).getId());

        // Test : No category related
        final List<Category> categoryList_ASC = getProcessAPI().getCategoriesUnrelatedToProcessDefinition(processDefinitionId, 0, 10,
                CategoryCriterion.NAME_ASC);
        assertNotNull(categoryList_ASC);
        assertEquals(3, categoryList_ASC.size());
        assertEquals("category1", categoryList_ASC.get(0).getName());
        assertEquals("category2", categoryList_ASC.get(1).getName());
        assertEquals("category3", categoryList_ASC.get(2).getName());

        // Test : 2 related categories
        final List<Category> categoryList_DESC = getProcessAPI().getCategoriesUnrelatedToProcessDefinition(processDefinitions.get(2).getId(), 0, 10,
                CategoryCriterion.NAME_DESC);
        assertNotNull(categoryList_DESC);
        assertEquals(1, categoryList_DESC.size());
        assertEquals("category1", categoryList_DESC.get(0).getName());
    }

    @Test
    public void searchCategoriesNotOfProcessDefinition() throws Exception {
        // generate categories
        categories = generateCategory(3, "category", "test get categories of process definition ");
        // generate process definition
        final ProcessDefinition processDefinition = generateProcessDefinition(1, "process", "version").get(0);
        processDefinitions.add(processDefinition);
        final long processDefinitionId = processDefinition.getId();

        // add
        for (final Category category : categories) {
            getProcessAPI().addProcessDefinitionToCategory(category.getId(), processDefinitionId);
        }

        // test
        final List<Category> categoryList_ASC = getProcessAPI().getCategoriesOfProcessDefinition(processDefinitionId, 0, 10, CategoryCriterion.NAME_ASC);
        assertNotNull(categoryList_ASC);
        assertEquals(3, categoryList_ASC.size());
        assertEquals("category1", categoryList_ASC.get(0).getName());
        assertEquals("category2", categoryList_ASC.get(1).getName());
        assertEquals("category3", categoryList_ASC.get(2).getName());

        final List<Category> categoryList_DESC = getProcessAPI().getCategoriesOfProcessDefinition(processDefinitionId, 0, 10, CategoryCriterion.NAME_DESC);
        assertNotNull(categoryList_DESC);
        assertEquals(3, categoryList_DESC.size());
        assertEquals("category3", categoryList_DESC.get(0).getName());
        assertEquals("category2", categoryList_DESC.get(1).getName());
        assertEquals("category1", categoryList_DESC.get(2).getName());

        final List<Category> outOfRangeCategories = getProcessAPI().getCategoriesOfProcessDefinition(processDefinitionId, 3, 10, CategoryCriterion.NAME_ASC);
        assertEquals(0, outOfRangeCategories.size());
    }

    @Test
    @Deprecated
    public void oldRemoveProcessDefinitionsOfCategory() throws Exception {
        // generate category
        final Category category = getProcessAPI().createCategory(name, description);
        categories.add(category);
        // generate process definitions
        processDefinitions = generateProcessDefinition(3, "process", "version");
        final List<Long> processDefinitionIds = new ArrayList<Long>();
        for (final ProcessDefinition processDefinition : processDefinitions) {
            processDefinitionIds.add(processDefinition.getId());
        }
        // add process definitions to category
        getProcessAPI().addProcessDefinitionsToCategory(category.getId(), processDefinitionIds);
        // test
        getProcessAPI().removeAllProcessDefinitionsFromCategory(category.getId());
        final List<ProcessDeploymentInfo> processDeploymentInfos = getProcessAPI().getProcessDeploymentInfosOfCategory(category.getId(), 0, 10,
                ProcessDeploymentInfoCriterion.ACTIVATION_STATE_ASC);
        assertNotNull(processDeploymentInfos);
        assertEquals(0, processDeploymentInfos.size());
    }

    @Test
    public void removeProcessDefinitionsOfCategory() throws Exception {
        // generate category
        final Category category = getProcessAPI().createCategory(name, description);
        categories.add(category);
        // generate process definitions
        processDefinitions = generateProcessDefinition(3, "process", "version");
        final List<Long> processDefinitionIds = new ArrayList<Long>();
        for (final ProcessDefinition processDefinition : processDefinitions) {
            processDefinitionIds.add(processDefinition.getId());
        }
        // add process definitions to category
        getProcessAPI().addProcessDefinitionsToCategory(category.getId(), processDefinitionIds);
        // test
        getProcessAPI().removeProcessDefinitionsFromCategory(category.getId(), 0, 2);
        final List<ProcessDeploymentInfo> processDeploymentInfos = getProcessAPI().getProcessDeploymentInfosOfCategory(category.getId(), 0, 10,
                ProcessDeploymentInfoCriterion.ACTIVATION_STATE_ASC);
        assertNotNull(processDeploymentInfos);
        assertEquals(1, processDeploymentInfos.size());
    }

    @Test
    @Deprecated
    public void oldRemoveProcessDefinitionsOfUnknownCategoryDoesntThrowException() throws Exception {
        // generate process definitions
        processDefinitions = generateProcessDefinition(3, "process", "version");
        // test
        getProcessAPI().removeAllProcessDefinitionsFromCategory(Long.MAX_VALUE);
    }

    @Test
    public void removeProcessDefinitionsOfUnknownCategoryDoesntThrowException() throws Exception {
        // generate process definitions
        processDefinitions = generateProcessDefinition(3, "process", "version");
        // test
        getProcessAPI().removeProcessDefinitionsFromCategory(Long.MAX_VALUE, 0, 0);
    }

    @Test
    @Deprecated
    public void oldRemoveProcessDefinitionFromCategory() throws Exception {
        // generate categories
        categories = generateCategory(3, "category", "test remove one specified process definition from all categories ");
        // generate process definition
        final ProcessDefinition processDefinition = generateProcessDefinition(1, "process", "version").get(0);
        processDefinitions.add(processDefinition);
        final long processDefinitionId = processDefinition.getId();

        // add
        for (final Category category : categories) {
            getProcessAPI().addProcessDefinitionToCategory(category.getId(), processDefinitionId);
        }
        long count = getProcessAPI().getNumberOfCategories(processDefinitionId);
        assertEquals(3, count);
        // test
        getProcessAPI().removeAllCategoriesFromProcessDefinition(processDefinitionId);
        count = getProcessAPI().getNumberOfCategories(processDefinitionId);
        assertEquals(0, count);
    }

    @Test
    public void removeProcessDefinitionFromCategory() throws Exception {
        // generate categories
        categories = generateCategory(3, "category", "test remove one specified process definition from all categories ");
        // generate process definition
        final ProcessDefinition processDefinition = generateProcessDefinition(1, "process", "version").get(0);
        processDefinitions.add(processDefinition);
        final long processDefinitionId = processDefinition.getId();

        // add
        for (final Category category : categories) {
            getProcessAPI().addProcessDefinitionToCategory(category.getId(), processDefinitionId);
        }
        long count = getProcessAPI().getNumberOfCategories(processDefinitionId);
        assertEquals(3, count);
        // test
        getProcessAPI().removeCategoriesFromProcessDefinition(processDefinitionId, 0, 2);
        count = getProcessAPI().getNumberOfCategories(processDefinitionId);
        assertEquals(1, count);
    }

    @Test
    @Deprecated
    public void oldRemoveUnknowProcessDefinitionFromCategoryDontThrowsException() throws Exception {
        getProcessAPI().removeAllCategoriesFromProcessDefinition(0);
    }

    @Test
    public void removeUnknowProcessDefinitionFromCategoryDontThrowsException() throws Exception {
        getProcessAPI().removeCategoriesFromProcessDefinition(0, 0, 1);
    }

    @Test
    public void getNumberOfUnCategoriedProcessesDefinitions() throws Exception {
        long processDefinitionCount = getProcessAPI().getNumberOfUncategorizedProcessDefinitions();
        assertEquals(0, processDefinitionCount);

        final ProcessDefinition processDefinition = generateProcessDefinition(1, "processName", "version").get(0);
        processDefinitions.add(processDefinition);
        final long processDefinitionId = processDefinition.getId();
        processDefinitionCount = getProcessAPI().getNumberOfUncategorizedProcessDefinitions();
        assertEquals(1, processDefinitionCount);
    }

    @Test
    public void getUnCategoriedProcesses() throws Exception {
        processDefinitions = generateProcessDefinition(3, "process", "version");
        final List<ProcessDeploymentInfo> processDeploymentInfoList_NameASC = getProcessAPI().getUncategorizedProcessDeploymentInfos(0, 3,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertNotNull(processDeploymentInfoList_NameASC);
        assertEquals(3, processDeploymentInfoList_NameASC.size());
        assertEquals("process1", processDeploymentInfoList_NameASC.get(0).getName());
        assertEquals("process2", processDeploymentInfoList_NameASC.get(1).getName());
        assertEquals("process3", processDeploymentInfoList_NameASC.get(2).getName());

        final List<ProcessDeploymentInfo> processDeploymentInfoList_NameDESC = getProcessAPI().getUncategorizedProcessDeploymentInfos(0, 2,
                ProcessDeploymentInfoCriterion.NAME_DESC);
        assertNotNull(processDeploymentInfoList_NameDESC);
        assertEquals(2, processDeploymentInfoList_NameDESC.size());
        assertEquals("process3", processDeploymentInfoList_NameDESC.get(0).getName());
        assertEquals("process2", processDeploymentInfoList_NameDESC.get(1).getName());

        final List<ProcessDeploymentInfo> outOfRangeProcesses = getProcessAPI().getUncategorizedProcessDeploymentInfos(3, 3,
                ProcessDeploymentInfoCriterion.NAME_DESC);
        assertEquals(0, outOfRangeProcesses.size());
    }

    private List<Category> generateCategory(final int count, final String categoryName, final String description) throws AlreadyExistsException,
            CreationException {
        final List<Category> categoryList = new ArrayList<Category>();
        for (int i = 1; i <= count; i++) {
            categoryList.add(getProcessAPI().createCategory(categoryName + i, description + i));
        }
        return categoryList;
    }

    private List<ProcessDefinition> generateProcessDefinition(final int count, final String processName, final String version)
            throws InvalidBusinessArchiveFormatException, ProcessDeployException, InvalidProcessDefinitionException, AlreadyExistsException {
        final List<ProcessDefinition> processDefinitionList = new ArrayList<ProcessDefinition>();
        for (int i = 1; i <= count; i++) {
            final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(processName + i,
                    version + i, Arrays.asList("step1"), Arrays.asList(true));
            final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition)
                    .done();
            processDefinitionList.add(getProcessAPI().deploy(businessArchive));
        }
        return processDefinitionList;
    }

    @Test(expected = AlreadyExistsException.class)
    public void cannotAddTheSameCategoryToAProcess() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder();
        final DesignProcessDefinition definition = processBuilder.createNewInstance("category", "0.9").addAutomaticTask("step1").getProcess();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(definition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);
        processDefinitions.add(processDefinition);
        final Category category = getProcessAPI().createCategory("Human resources", "Category for personnel Management");
        categories.add(category);
        getProcessAPI().addCategoriesToProcess(processDefinition.getId(), Arrays.asList(category.getId()));
        getProcessAPI().addCategoriesToProcess(processDefinition.getId(), Arrays.asList(category.getId()));
        fail("It is not allowed to add twice the same category to a process");
    }

    @Test(expected = AlreadyExistsException.class)
    public void cannotAddTheSameCategoryToAProcessList() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder();
        final DesignProcessDefinition definition = processBuilder.createNewInstance("category", "0.9").addAutomaticTask("step1").getProcess();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(definition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);
        processDefinitions.add(processDefinition);
        final Category category = getProcessAPI().createCategory("Human resources", "Category for personnel Management");
        categories.add(category);
        getProcessAPI().addProcessDefinitionsToCategory(category.getId(), Arrays.asList(processDefinition.getId()));
        getProcessAPI().addProcessDefinitionsToCategory(category.getId(), Arrays.asList(processDefinition.getId()));
        fail("It is not allowed to add twice the same category to a process");
    }

    @Cover(classes = { Category.class, ProcessDefinition.class }, concept = BPMNConcept.PROCESS, jira = "ENGINE-1047", keywords = { "Category", "Process" })
    @Test(expected = AlreadyExistsException.class)
    public void cannotAssociateTheSameCategoryTwiceWitAProcess() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder();
        final DesignProcessDefinition definition = processBuilder.createNewInstance("category", "0.9").addAutomaticTask("step1").getProcess();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(definition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);
        processDefinitions.add(processDefinition);
        final Category category = getProcessAPI().createCategory("Human resources", "Category for personnel Management");
        categories.add(category);
        getProcessAPI().addProcessDefinitionToCategory(category.getId(), processDefinition.getId());
        getProcessAPI().addProcessDefinitionToCategory(category.getId(), processDefinition.getId());
        fail("It is not allowed to add twice the same category to a process");
    }

}
