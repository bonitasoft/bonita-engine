package org.bonitasoft.engine.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.actor.ActorMappingImportException;
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
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDeployException;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class ProcessCategoryTest extends CommonAPITest {

    protected static final String USERNAME = "dwight";

    protected static final String PASSWORD = "Schrute";

    private final String name = "category";

    private final String description = "description";

    @After
    public void afterTest() throws BonitaException {
        logout();
    }

    @Before
    public void beforeTest() throws BonitaException {
        login();
    }

    @Cover(classes = { ProcessAPI.class, Category.class }, concept = BPMNConcept.PROCESS, keywords = { "Create", "Category" })
    @Test
    public void testCreateCategory() throws Exception {
        final Category category = getProcessAPI().createCategory(name, description);
        assertNotNull(category);
        assertEquals(name, category.getName());
        final long categoryId = category.getId();
        final Category rCategory = getProcessAPI().getCategory(categoryId);
        assertNotNull(rCategory);
        assertEquals(name, rCategory.getName());
        getProcessAPI().deleteCategory(categoryId);
    }

    @Cover(classes = { ProcessAPI.class, Category.class, User.class }, concept = BPMNConcept.PROCESS, keywords = { "Create", "Category", "Creator" }, jira = "ENGINE-619")
    @Test
    public void testCreateCategoryWithCreatorAsAnID() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);
        logout();
        loginWith(USERNAME, PASSWORD);

        final Category category = getProcessAPI().createCategory(name, description);
        assertNotNull(category);
        assertEquals(name, category.getName());
        assertEquals(user.getId(), category.getCreator());

        getProcessAPI().deleteCategory(category.getId());
        deleteUser(user.getId());
    }

    @Cover(classes = { ProcessAPI.class, Category.class, AlreadyExistsException.class }, concept = BPMNConcept.PROCESS, keywords = { "Create", "Category",
            "Exception" })
    @Test(expected = AlreadyExistsException.class)
    public void testCreateCategoryWithCategoryAlreadyExistException() throws Exception {
        final Category category = getProcessAPI().createCategory(name, description);
        assertNotNull(category);
        assertEquals(name, category.getName());
        final long categoryId = category.getId();
        try {
            getProcessAPI().createCategory(name, description);
        } finally {
            getProcessAPI().deleteCategory(categoryId);
        }
    }

    @Cover(classes = { ProcessAPI.class, Category.class, CreationException.class }, concept = BPMNConcept.PROCESS, keywords = { "Create", "Category",
            "Exception" })
    @Test(expected = CreationException.class)
    public void testCreateCategoryWithCategoryCreationException() throws Exception {
        getProcessAPI().createCategory(null, description);
    }

    @Cover(classes = { ProcessAPI.class, Category.class }, concept = BPMNConcept.PROCESS, keywords = { "Number", "Category" })
    @Test
    public void testGetNumberOfCategories() throws Exception {
        final Category category1 = getProcessAPI().createCategory(name + 1, description);
        final Category category2 = getProcessAPI().createCategory(name + 2, description);

        final long categoriesCount = getProcessAPI().getNumberOfCategories();
        assertEquals(2, categoriesCount);

        getProcessAPI().deleteCategory(category1.getId());
        getProcessAPI().deleteCategory(category2.getId());
    }

    @Cover(classes = { ProcessAPI.class, Category.class }, concept = BPMNConcept.PROCESS, keywords = { "Category", "Existed" })
    @Test
    public void testGetCategory() throws Exception {
        final Category category = getProcessAPI().createCategory(name, description);
        assertNotNull(category);
        assertEquals(name, category.getName());
        final long categoryId = category.getId();
        final Category rCategory = getProcessAPI().getCategory(categoryId);
        assertNotNull(rCategory);
        assertEquals(categoryId, rCategory.getId());
        assertEquals(name, rCategory.getName());
        assertEquals(description, rCategory.getDescription());
        getProcessAPI().deleteCategory(categoryId);
    }

    @Cover(classes = { ProcessAPI.class, CategoryNotFoundException.class }, concept = BPMNConcept.PROCESS, keywords = { "Category", "Unexisted", "Exception" })
    @Test(expected = CategoryNotFoundException.class)
    public void testGetCategoryWithCategoryNotFoundException() throws Exception {
        final Category category = getProcessAPI().createCategory(name, description);
        assertNotNull(category);
        assertEquals(name, category.getName());
        final long categoryId = category.getId();
        try {
            getProcessAPI().getCategory(categoryId + 1);

        } finally {
            getProcessAPI().deleteCategory(categoryId);
        }
    }

    @Cover(classes = { ProcessAPI.class, Category.class }, concept = BPMNConcept.PROCESS, keywords = { "Category", "Existed", "Several" })
    @Test
    public void testGetCategories() throws Exception {
        final Category category1 = getProcessAPI().createCategory("category1", description);
        final Category category2 = getProcessAPI().createCategory("category2", description);
        final Category category3 = getProcessAPI().createCategory("category3", description);
        final Category category4 = getProcessAPI().createCategory("category4", description);
        final Category category5 = getProcessAPI().createCategory("category5", description);

        List<Category> categoriesNameAsc = getProcessAPI().getCategories(0, 2, CategoryCriterion.NAME_ASC);
        assertEquals(2, categoriesNameAsc.size());
        assertEquals("category1", categoriesNameAsc.get(0).getName());
        assertEquals("category2", categoriesNameAsc.get(1).getName());

        categoriesNameAsc = getProcessAPI().getCategories(2, 2, CategoryCriterion.NAME_ASC);
        assertEquals(2, categoriesNameAsc.size());
        assertEquals("category3", categoriesNameAsc.get(0).getName());
        assertEquals("category4", categoriesNameAsc.get(1).getName());

        categoriesNameAsc = getProcessAPI().getCategories(4, 2, CategoryCriterion.NAME_ASC);
        assertEquals(1, categoriesNameAsc.size());
        assertEquals("category5", categoriesNameAsc.get(0).getName());

        categoriesNameAsc = getProcessAPI().getCategories(6, 2, CategoryCriterion.NAME_ASC);
        assertEquals(0, categoriesNameAsc.size());

        getProcessAPI().deleteCategory(category1.getId());
        getProcessAPI().deleteCategory(category2.getId());
        getProcessAPI().deleteCategory(category3.getId());
        getProcessAPI().deleteCategory(category4.getId());
        getProcessAPI().deleteCategory(category5.getId());
    }

    @Cover(classes = { ProcessAPI.class, Category.class }, concept = BPMNConcept.PROCESS, keywords = { "Category", "Update" })
    @Test
    public void testUpdateCategory() throws Exception {
        final Category oldCategory = getProcessAPI().createCategory(name, description);
        final String newName = "updatedName";
        final String newDescription = "updatedDescription";

        final long categoryId = oldCategory.getId();
        final CategoryUpdater updater = new CategoryUpdater();
        updater.setName(newName).setDescription(newDescription);

        getProcessAPI().updateCategory(oldCategory.getId(), updater);
        final Category categoryUpdated = getProcessAPI().getCategory(categoryId);
        assertNotNull(categoryUpdated);
        assertEquals(newName, categoryUpdated.getName());
        assertEquals(newDescription, categoryUpdated.getDescription());
        getProcessAPI().deleteCategory(categoryId);
    }

    @Cover(classes = { ProcessAPI.class, CategoryNotFoundException.class }, concept = BPMNConcept.PROCESS, keywords = { "Category", "Unexisted", "Exception",
            "Update" })
    @Test(expected = CategoryNotFoundException.class)
    public void testUpdateCategoryWithCategoryNotFoundException() throws Exception {
        final Category category = getProcessAPI().createCategory(name, description);
        final long categoryId = category.getId();
        try {
            final String newName = "updatedName";
            final String newDescription = "updatedDescription";
            final CategoryUpdater updater = new CategoryUpdater();
            updater.setName(newName).setDescription(newDescription);
            getProcessAPI().updateCategory(categoryId + 1, updater);
        } finally {
            getProcessAPI().deleteCategory(categoryId);
        }
    }

    @Cover(classes = { ProcessAPI.class, UpdateException.class }, concept = BPMNConcept.PROCESS, keywords = { "Category", "Exception", "Update" })
    @Test(expected = UpdateException.class)
    public void testUpdateCategoryWithCategoryUpdateException() throws Exception {
        final long categoryId = 0;
        getProcessAPI().updateCategory(categoryId, null);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Category", "Delete" })
    @Test
    public void testDeleteCategory() throws Exception {
        final Category category = getProcessAPI().createCategory(name, description);
        getProcessAPI().deleteCategory(category.getId());
        Category category1 = null;
        try {
            category1 = getProcessAPI().getCategory(category.getId());
        } catch (final CategoryNotFoundException e) {
        } finally {
            assertNull(category1);
        }
    }

    @Cover(classes = { ProcessAPI.class, CategoryNotFoundException.class }, concept = BPMNConcept.PROCESS, keywords = { "Category", "Unexisted", "Exception",
            "Delete" })
    @Test(expected = DeletionException.class)
    public void testDeleteCategoryWithCategoryNotFoundException() throws Exception {
        getProcessAPI().deleteCategory(Long.MAX_VALUE);
    }

    @Cover(classes = { ProcessAPI.class, DeletionException.class }, concept = BPMNConcept.PROCESS, keywords = { "Category", "Exception", "Delete" })
    @Test(expected = DeletionException.class)
    public void testDeleteCategoryWithCategoryDeletionException() throws Exception {
        final long categoryId = 0;
        getProcessAPI().deleteCategory(categoryId);
    }

    @Test
    public void testProcessDefinitionWithCategoryPagination() throws Exception {
        // generate category id;
        final long categoryId = getProcessAPI().createCategory(name, description).getId();
        // test
        final List<ProcessDeploymentInfo> processDeploymentInfos = getProcessAPI().getProcessDeploymentInfosOfCategory(categoryId, 0, 10, null);
        assertEquals(0, processDeploymentInfos.size());
        // delete category and process definition
        getProcessAPI().deleteCategory(categoryId);
    }

    @Test
    public void testRemoveSeveralCategoriesToProcessDefinition() throws Exception {
        // generate category id;
        final long categoryId1 = getProcessAPI().createCategory("Human resources", "Category for personnel Management").getId();
        final long categoryId2 = getProcessAPI().createCategory("Travel Service", "Category for all related travel matters").getId();
        final long categoryId3 = getProcessAPI().createCategory("Cleaning Service", "Category for all related travel matters").getId();
        // generate process definition id
        final ProcessDefinition processDefinition = generateProcessDefinition(1, "processName", "version").get(0);
        final long processDefinitionId = processDefinition.getId();

        // test number of current categories form this process:
        getProcessAPI().addCategoriesToProcess(processDefinitionId, Arrays.asList(new Long[] { categoryId1, categoryId2, categoryId3 }));
        List<Category> categories = getProcessAPI().getCategoriesOfProcessDefinition(processDefinitionId, 0, 10, CategoryCriterion.NAME_DESC);
        assertEquals(3, categories.size());

        getProcessAPI().removeCategoriesFromProcess(processDefinitionId, Arrays.asList(new Long[] { categoryId1, categoryId3 }));
        // test number of current categories form this process:
        categories = getProcessAPI().getCategoriesOfProcessDefinition(processDefinitionId, 0, 10, CategoryCriterion.NAME_DESC);
        assertEquals(1, categories.size());
        // check if the remaining one is the good one:
        assertEquals(categoryId2, categories.get(0).getId());

        // delete category and process definition
        getProcessAPI().deleteCategory(categoryId1);
        getProcessAPI().deleteCategory(categoryId2);
        getProcessAPI().deleteCategory(categoryId3);
        getProcessAPI().deleteProcess(processDefinitionId);
    }

    @Test
    public void testAddSeveralCategoriesToProcessDefinition() throws Exception {
        // generate category id;
        final long categoryId1 = getProcessAPI().createCategory("Human resources", "Category for personnel Management").getId();
        final long categoryId2 = getProcessAPI().createCategory("Travel Service", "Category for all related travel matters").getId();
        // generate process definition id
        final ProcessDefinition processDefinition = generateProcessDefinition(1, "processName", "version").get(0);
        final long processDefinitionId = processDefinition.getId();
        // test
        getProcessAPI().addCategoriesToProcess(processDefinitionId, Arrays.asList(new Long[] { categoryId1, categoryId2 }));
        final List<Category> categories = getProcessAPI().getCategoriesOfProcessDefinition(processDefinitionId, 0, 10, CategoryCriterion.NAME_DESC);
        assertEquals(2, categories.size());
        assertEquals(categoryId1, categories.get(1).getId());
        assertEquals(categoryId2, categories.get(0).getId());
        // delete category and process definition
        getProcessAPI().deleteCategory(categoryId1);
        getProcessAPI().deleteCategory(categoryId2);
        getProcessAPI().deleteProcess(processDefinitionId);
    }

    @Test
    public void testAddProcessDefinitionToCategory() throws Exception {
        // generate category id;
        final long categoryId = getProcessAPI().createCategory(name, description).getId();
        // generate process definition id
        final ProcessDefinition processDefinition = generateProcessDefinition(1, "processName", "version").get(0);
        final long processDefinitionId = processDefinition.getId();
        // test
        getProcessAPI().addProcessDefinitionToCategory(categoryId, processDefinitionId);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfosOfCategory(categoryId, 0, 10, null).get(0);
        assertEquals(processDefinition.getName(), processDeploymentInfo.getName());
        assertEquals(processDefinition.getVersion(), processDeploymentInfo.getVersion());
        // delete category and process definition
        getProcessAPI().deleteCategory(categoryId);
        getProcessAPI().deleteProcess(processDefinitionId); // TODO should modify the categories.xml file
    }

    @Test(expected = CreationException.class)
    public void testAddProcessDefinitionToUnknownCategory() throws Exception {
        // generate wrong category id;
        final long categoryId = 0;
        // generate process definition id
        final ProcessDefinition processDefinition = generateProcessDefinition(1, "processName", "version").get(0);
        final long processDefinitionId = processDefinition.getId();
        // test
        try {
            getProcessAPI().addProcessDefinitionToCategory(categoryId, processDefinitionId);
        } finally {
            getProcessAPI().deleteProcess(processDefinitionId);
        }
    }

    @Test(expected = CreationException.class)
    public void testAddUnknowProcessDefinitionToCategory() throws Exception {
        // generate category id;
        final long categoryId = getProcessAPI().createCategory(name, description).getId();
        final long processDefinitionId = 0;
        // test
        try {
            getProcessAPI().addProcessDefinitionToCategory(categoryId, processDefinitionId);
        } finally {
            getProcessAPI().deleteCategory(categoryId);
        }
    }

    @Test
    public void testAddProcessDefinitionosToCategory() throws Exception {
        // generate category id;
        final long categoryId = getProcessAPI().createCategory(name, description).getId();
        // generate process definition id
        final List<ProcessDefinition> processDefinitionList = generateProcessDefinition(3, "process", "version");
        final List<Long> processDefinitionIds = new ArrayList<Long>();
        for (final ProcessDefinition processDefinition : processDefinitionList) {
            processDefinitionIds.add(processDefinition.getId());
        }
        // test
        getProcessAPI().addProcessDefinitionsToCategory(categoryId, processDefinitionIds);
        final List<ProcessDeploymentInfo> processDeploymentInfoList = getProcessAPI().getProcessDeploymentInfosOfCategory(categoryId, 0, 10, null);
        assertNotNull(processDeploymentInfoList);
        assertEquals(3, processDeploymentInfoList.size());
        // delete
        getProcessAPI().deleteCategory(categoryId);
        for (final Long processDefinitionId : processDefinitionIds) {
            getProcessAPI().deleteProcess(processDefinitionId);
        }
    }

    @Test
    public void testGetNumberOfCategoriesByProcessDefinition() throws Exception {
        // generate categories
        final List<Category> categoryList = generateCategory(3, "categoryName", "test get number of categories by process definition");
        // generate process definition
        final ProcessDefinition processDefinition = generateProcessDefinition(1, "processName", "version").get(0);
        final long processDefinitionId = processDefinition.getId();
        // test
        for (final Category category : categoryList) {
            getProcessAPI().addProcessDefinitionToCategory(category.getId(), processDefinitionId);
        }
        final long categoryNumber = getProcessAPI().getNumberOfCategories(processDefinitionId);
        assertEquals(3, categoryNumber);
        // delete
        for (final Category category : categoryList) {
            getProcessAPI().deleteCategory(category.getId());
        }
        getProcessAPI().deleteProcess(processDefinitionId);
    }

    @Test
    public void testGetNumberOfCategoriesOfInexistentProcess() throws Exception {
        final long numberOfCategories = getProcessAPI().getNumberOfCategories(Long.MAX_VALUE);
        assertEquals(0, numberOfCategories);
    }

    @Test
    public void testGetNumberOfProcessesInCategory() throws Exception {
        // generate categories
        final Category category = generateCategory(1, "categoryName", "test get number of processes in category").get(0);
        // generate process definition
        final List<ProcessDefinition> processDefinitionList = generateProcessDefinition(3, "processName", "version");
        final long categoryId = category.getId();
        // test
        for (final ProcessDefinition processDefinition : processDefinitionList) {
            getProcessAPI().addProcessDefinitionToCategory(categoryId, processDefinition.getId());
        }
        final long processNumber = getProcessAPI().getNumberOfProcessDefinitionsOfCategory(categoryId);
        assertEquals(3, processNumber);
        // delete category and process definitions
        getProcessAPI().deleteCategory(categoryId);
        for (final ProcessDefinition processDefinition : processDefinitionList) {
            getProcessAPI().deleteProcess(processDefinition.getId());
        }
    }

    @Test
    public void testGetNumberOfProcessesInCategoryWithInexistentCategory() throws Exception {
        final long processesInCategory = getProcessAPI().getNumberOfProcessDefinitionsOfCategory(Long.MAX_VALUE);
        assertEquals(0, processesInCategory);
    }

    @Test
    public void testGetProcessDeploymentInfosOfCategory() throws Exception {
        // generate category
        final long categoryId = getProcessAPI().createCategory(name, description).getId();
        // generate process definitions
        final List<ProcessDefinition> processDefinitionList = generateProcessDefinition(3, "process", "version");
        final List<Long> processDefinitionIds = new ArrayList<Long>();
        for (final ProcessDefinition processDefinition : processDefinitionList) {
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

        // delete
        getProcessAPI().deleteCategory(categoryId);
        for (final Long processDefinitionId : processDefinitionIds) {
            getProcessAPI().deleteProcess(processDefinitionId);
        }
    }

    @Test
    public void testGetCategoriesOfProcessDefinition() throws Exception {
        // generate categories
        final List<Category> categoryList = generateCategory(3, "category", "test get categories of process definition ");
        // generate process definition
        final ProcessDefinition processDefinition = generateProcessDefinition(1, "process", "version").get(0);
        final long processDefinitionId = processDefinition.getId();

        // add
        for (final Category category : categoryList) {
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

        // delete
        for (final Category category : categoryList) {
            getProcessAPI().deleteCategory(category.getId());
        }
        getProcessAPI().deleteProcess(processDefinitionId);
    }

    @Cover(classes = { ProcessAPI.class, Category.class }, concept = BPMNConcept.PROCESS, keywords = { "Get", "Category", "ProcessDefinition" })
    @Test
    public void testGetCategoriesUnrelatedToProcessDefinition() throws Exception {
        // generate categories
        final List<Category> categoryList = generateCategory(3, "category", "test get categories of process definition ");
        // generate process definition
        final List<ProcessDefinition> processDefinitions = generateProcessDefinition(3, "process", "version");
        final long processDefinitionId = processDefinitions.get(0).getId();

        // add
        getProcessAPI().addProcessDefinitionToCategory(categoryList.get(1).getId(), processDefinitions.get(1).getId());
        getProcessAPI().addProcessDefinitionToCategory(categoryList.get(1).getId(), processDefinitions.get(2).getId());
        getProcessAPI().addProcessDefinitionToCategory(categoryList.get(2).getId(), processDefinitions.get(2).getId());

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

        // delete
        for (final Category category : categoryList) {
            getProcessAPI().deleteCategory(category.getId());
        }
        for (final ProcessDefinition processDefinition : processDefinitions) {
            getProcessAPI().deleteProcess(processDefinition.getId());
        }
    }

    @Test
    public void testSearchCategoriesNotOfProcessDefinition() throws Exception {
        // generate categories
        final List<Category> categoryList = generateCategory(3, "category", "test get categories of process definition ");
        // generate process definition
        final ProcessDefinition processDefinition = generateProcessDefinition(1, "process", "version").get(0);
        final long processDefinitionId = processDefinition.getId();

        // add
        for (final Category category : categoryList) {
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

        // delete
        for (final Category category : categoryList) {
            getProcessAPI().deleteCategory(category.getId());
        }
        getProcessAPI().deleteProcess(processDefinitionId);
    }

    @Test
    public void testRemoveProcessDefinitionsOfCategory() throws Exception {
        // generate category
        final long categoryId = getProcessAPI().createCategory(name, description).getId();
        // generate process definitions
        final List<ProcessDefinition> processDefinitionList = generateProcessDefinition(3, "process", "version");
        final List<Long> processDefinitionIds = new ArrayList<Long>();
        for (final ProcessDefinition processDefinition : processDefinitionList) {
            processDefinitionIds.add(processDefinition.getId());
        }
        // add process definitions to category
        getProcessAPI().addProcessDefinitionsToCategory(categoryId, processDefinitionIds);
        // test
        getProcessAPI().removeAllProcessDefinitionsFromCategory(categoryId);
        final List<ProcessDeploymentInfo> processDeploymentInfos = getProcessAPI().getProcessDeploymentInfosOfCategory(categoryId, 0, 10, null);
        assertNotNull(processDeploymentInfos);
        assertEquals(0, processDeploymentInfos.size());
        // delete
        getProcessAPI().deleteCategory(categoryId);
        for (final Long processDefinitionId : processDefinitionIds) {
            getProcessAPI().deleteProcess(processDefinitionId);
        }
    }

    @Test
    public void testRemoveProcessDefinitionsOfUnknownCategoryDoesntThrowException() throws Exception {
        // generate process definitions
        final List<ProcessDefinition> processDefinitionList = generateProcessDefinition(3, "process", "version");
        final List<Long> processDefinitionIds = new ArrayList<Long>();
        for (final ProcessDefinition processDefinition : processDefinitionList) {
            processDefinitionIds.add(processDefinition.getId());
        }
        // test
        try {
            getProcessAPI().removeAllProcessDefinitionsFromCategory(Long.MAX_VALUE);
        } finally {
            for (final Long processDefinitionId : processDefinitionIds) {
                getProcessAPI().deleteProcess(processDefinitionId);
            }
        }
    }

    @Test
    public void testGetNumberOfUnCategoriedProcessesDefinitions() throws Exception {
        long processDefinitionCount = getProcessAPI().getNumberOfUncategorizedProcessDefinitions();
        assertEquals(0, processDefinitionCount);

        final ProcessDefinition processDefinition = generateProcessDefinition(1, "processName", "version").get(0);
        final long processDefinitionId = processDefinition.getId();
        processDefinitionCount = getProcessAPI().getNumberOfUncategorizedProcessDefinitions();
        assertEquals(1, processDefinitionCount);

        getProcessAPI().deleteProcess(processDefinitionId);
    }

    @Test
    public void testGetUnCategoriedProcesses() throws Exception {
        final List<ProcessDefinition> processDefinitionList = generateProcessDefinition(3, "process", "version");
        final List<Long> processDefinitionIds = new ArrayList<Long>();
        for (final ProcessDefinition processDefinition : processDefinitionList) {
            processDefinitionIds.add(processDefinition.getId());
        }
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

        for (final Long processDefinitionId : processDefinitionIds) {
            getProcessAPI().deleteProcess(processDefinitionId);
        }
    }

    @Test
    public void testRemoveProcessDefinitionFromCategory() throws Exception {
        // generate categories
        final List<Category> categoryList = generateCategory(3, "category", "test remove one specified process definition from all categories ");
        // generate process definition
        final ProcessDefinition processDefinition = generateProcessDefinition(1, "process", "version").get(0);
        final long processDefinitionId = processDefinition.getId();

        // add
        for (final Category category : categoryList) {
            getProcessAPI().addProcessDefinitionToCategory(category.getId(), processDefinitionId);
        }
        long count = getProcessAPI().getNumberOfCategories(processDefinitionId);
        assertEquals(3, count);
        // test
        getProcessAPI().removeAllCategoriesFromProcessDefinition(processDefinitionId);
        count = getProcessAPI().getNumberOfCategories(processDefinitionId);
        assertEquals(0, count);
        // delete
        for (final Category category : categoryList) {
            getProcessAPI().deleteCategory(category.getId());
        }
        getProcessAPI().deleteProcess(processDefinitionId);
    }

    @Test
    public void testRemoveUnknowProcessDefinitionFromCategoryDontThrowsException() throws Exception {
        final long processDefinitionId = 0;
        getProcessAPI().removeAllCategoriesFromProcessDefinition(processDefinitionId);
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
            throws InvalidBusinessArchiveFormatException, ProcessDeployException, ProcessDefinitionNotFoundException, InvalidProcessDefinitionException,
            ActorMappingImportException, AlreadyExistsException {
        final List<ProcessDefinition> processDefinitionList = new ArrayList<ProcessDefinition>();
        for (int i = 1; i <= count; i++) {
            final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps(processName + i, version + i,
                    Arrays.asList("step1"), Arrays.asList(true));
            final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition)
                    .done();
            processDefinitionList.add(getProcessAPI().deploy(businessArchive));
        }
        return processDefinitionList;
    }

    @Test
    public void cannotAddTheSameCategoryToAProcess() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder();
        final DesignProcessDefinition definition = processBuilder.createNewInstance("category", "0.9").addAutomaticTask("step1").getProcess();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(definition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);
        final long categoryId = getProcessAPI().createCategory("Human resources", "Category for personnel Management").getId();
        getProcessAPI().addCategoriesToProcess(processDefinition.getId(), Arrays.asList(categoryId));
        try {
            getProcessAPI().addCategoriesToProcess(processDefinition.getId(), Arrays.asList(categoryId));
            fail("It is not allowed to add twice the same category to a process");
        } catch (final AlreadyExistsException cipaee) {
            getProcessAPI().deleteCategory(categoryId);
            getProcessAPI().deleteProcess(processDefinition.getId());
        }
    }

    @Test
    public void cannotAddTheSameCategoryToAProcessList() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder();
        final DesignProcessDefinition definition = processBuilder.createNewInstance("category", "0.9").addAutomaticTask("step1").getProcess();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(definition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);
        final long categoryId = getProcessAPI().createCategory("Human resources", "Category for personnel Management").getId();
        getProcessAPI().addProcessDefinitionsToCategory(categoryId, Arrays.asList(processDefinition.getId()));
        try {
            getProcessAPI().addProcessDefinitionsToCategory(categoryId, Arrays.asList(processDefinition.getId()));
            fail("It is not allowed to add twice the same category to a process");
        } catch (final AlreadyExistsException cipaee) {
            getProcessAPI().deleteCategory(categoryId);
            getProcessAPI().deleteProcess(processDefinition.getId());
        }
    }

    @Cover(classes = { Category.class, ProcessDefinition.class }, concept = BPMNConcept.PROCESS, jira = "ENGINE-1047", keywords = { "Category", "Process" })
    @Test(expected = AlreadyExistsException.class)
    public void cannotAssociateTheSameCategoryTwiceWitAProcess() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder();
        final DesignProcessDefinition definition = processBuilder.createNewInstance("category", "0.9").addAutomaticTask("step1").getProcess();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(definition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);
        final long categoryId = getProcessAPI().createCategory("Human resources", "Category for personnel Management").getId();
        getProcessAPI().addProcessDefinitionToCategory(categoryId, processDefinition.getId());
        try {
            getProcessAPI().addProcessDefinitionToCategory(categoryId, processDefinition.getId());
            fail("It is not allowed to add twice the same category to a process");
        } finally {
            getProcessAPI().deleteCategory(categoryId);
            getProcessAPI().deleteProcess(processDefinition.getId());
        }
    }

}
