/**
 * Copyright (C) 2022 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.web.rest.server.api.bpm.process;

import static junit.framework.Assert.assertNull;
import static org.bonitasoft.web.rest.model.builder.bpm.process.CategoryItemBuilder.aCategoryItem;
import static org.bonitasoft.web.toolkit.client.data.APIID.makeAPIID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import org.bonitasoft.engine.bpm.category.Category;
import org.bonitasoft.test.toolkit.bpm.TestCategory;
import org.bonitasoft.test.toolkit.bpm.TestCategoryFactory;
import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.web.rest.model.bpm.process.CategoryItem;
import org.bonitasoft.web.rest.server.datastore.bpm.process.CategoryDatastore;
import org.bonitasoft.web.test.AbstractConsoleTest;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIForbiddenException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.After;
import org.junit.Test;

/**
 * @author Nicolas Tith
 */
public class APICategoryIT extends AbstractConsoleTest {

    private APICategory api;

    @Override
    public void consoleTestSetUp() throws Exception {
        api = new APICategory();
        api.setCaller(getAPICaller(getInitiator().getSession(), "API/bpm/category"));
    }

    @Override
    protected TestUser getInitiator() {
        return TestUserFactory.getRidleyScott();
    }

    @After
    public void cleanCategoriesInDB() {
        final List<TestCategory> allCategories = TestCategoryFactory.getAllCategories(getInitiator().getSession());
        for (final TestCategory category : allCategories) {
            TestCategoryFactory.removeTestCategoryFromList(category);
            category.delete();
        }
    }

    /**
     * Fetch a Category by id from engine
     *
     * @return the category or null if not found
     */
    private CategoryItem getFromEngine(final long categoryId) {
        try {
            return new CategoryDatastore(getInitiator().getSession()).get(makeAPIID(categoryId));
        } catch (final APIException e) {
            if (e instanceof APIItemNotFoundException) {
                return null;
            }
            throw e;
        }
    }

    @Test
    public void testSearchCategoryItem() {
        final List<TestCategory> catList = TestCategoryFactory.getCategories(3);

        // Search the CommentItem
        final List<CategoryItem> actualCatList = api.runSearch(0, 10, null, null, new HashMap<>(), new ArrayList<>(),
                new ArrayList<>()).getResults();
        Assert.assertNotNull("Categories not found", actualCatList);
        Assert.assertTrue(catList.size() == 3);
    }

    @Test
    public void addCategoryTest() {
        //before

        // API call
        final CategoryItem categoryItem = new CategoryItem();
        categoryItem.setName("categoryTest");
        categoryItem.setDescription("categoryDescription");
        api.runAdd(categoryItem);

        // Check
        final List<TestCategory> catList = TestCategoryFactory.getAllCategories(getInitiator().getSession());
        final int nbOfCategories = catList.size();
        String message = "No categories added. " + nbOfCategories + " categories found. Categories are: \n";
        for (final TestCategory testCategory : catList) {
            message += " catgeory with id " + testCategory.getId() + ": " + testCategory.getCategory().getName() + "\n";
        }
        Assert.assertEquals(message, 1, nbOfCategories);
        final Category resultCategory = catList.get(0).getCategory();
        Assert.assertEquals("Wrong category found (not same name)", categoryItem.getName(), resultCategory.getName());
        Assert.assertEquals("Wrong category found (not same description)", categoryItem.getDescription(),
                resultCategory.getDescription());

    }

    @Test
    public void updateCategoryTest() {
        final String newDescription = "Lorem ipsum dolor sit amet";

        // Init
        final TestCategory category = TestCategoryFactory.getRandomCategory();

        // Update
        final Map<String, String> updates = new HashMap<>();
        updates.put(CategoryItem.ATTRIBUTE_DESCRIPTION, newDescription);
        api.runUpdate(APIID.makeAPIID(category.getCategory().getId()), updates);

        // Get
        final CategoryItem output = api.runGet(APIID.makeAPIID(category.getCategory().getId()), new ArrayList<>(),
                new ArrayList<>());

        Assert.assertNotNull("Category not found", output);
        Assert.assertEquals("Update of category failed", newDescription, output.getDescription());

    }

    @Test
    public void getCategoryTest() {
        // Init
        final TestCategory category = TestCategoryFactory.getRandomCategory();

        // API Call
        final CategoryItem catItem = api.runGet(APIID.makeAPIID(category.getId()), new ArrayList<>(),
                new ArrayList<>());

        Assert.assertNotNull("Category not found", category);
        Assert.assertEquals("Wrong category description found", category.getCategory().getDescription(),
                catItem.getDescription());
        Assert.assertEquals("Wrong category found", category.getCategory().getName(), catItem.getName());
    }

    @Test
    public void deleteCategoryTest() {
        final TestCategory category = TestCategoryFactory.getRandomCategory();

        api.runDelete(List.of(makeAPIID(category.getId())));

        assertNull(getFromEngine(category.getId()));

        TestCategoryFactory.removeTestCategoryFromList(category);
    }

    @Test(expected = APIForbiddenException.class)
    public void addingTwiceSameCategoryIsForbidden() {
        //given
        final CategoryItem categoryItem = aCategoryItem().build();

        //when then exception
        api.runAdd(categoryItem);
        api.runAdd(categoryItem);

    }

}
