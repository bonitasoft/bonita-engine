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
package org.bonitasoft.test.toolkit.bpm;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.category.Category;
import org.bonitasoft.engine.bpm.category.CategoryCriterion;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.test.toolkit.exception.TestToolkitException;
import org.bonitasoft.test.toolkit.organization.TestToolkitCtx;

/**
 * @author Séverin Moussel
 */
public class TestCategory {

    private final Category category;

    /**
     * Default Constructor.
     */
    public TestCategory(final Category category) {
        this.category = category;
    }

    /**
     * Default Constructor.
     */
    public TestCategory(final APISession apiSession, final String name, final String description) {
        this(createCategory(apiSession, name, description));
    }

    public TestCategory(final String name, final String description) {
        this(TestToolkitCtx.getInstance().getInitiator().getSession(), name, description);
    }

    private static Category createCategory(final APISession apiSession, final String name, final String description) {
        try {
            return TenantAPIAccessor.getProcessAPI(apiSession).createCategory(name, description);
        } catch (final Exception e) {
            throw new TestToolkitException("Can't create category.", e);
        }
    }

    public void delete(final APISession apiSession) {
        try {
            TenantAPIAccessor.getProcessAPI(apiSession).deleteCategory(this.category.getId());
        } catch (final Exception e) {
            throw new TestToolkitException("Can't delete category", e);
        }
    }

    public void delete() {
        try {
            delete(TestToolkitCtx.getInstance().getInitiator().getSession());
        } catch (final Exception e) {
            throw new TestToolkitException("Can't delete category", e);
        }
    }

    public Category getCategory() {
        return this.category;
    }

    public static List<TestCategory> getAll(final APISession apiSession) {
        try {
            final ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(apiSession);
            final int nbCat = (int) processAPI.getNumberOfCategories();
            final List<Category> catList = processAPI.getCategories(0, nbCat, CategoryCriterion.NAME_ASC);
            final List<TestCategory> testCatList = new ArrayList<>();
            for (final Category cat : catList) {
                testCatList.add(new TestCategory(cat));
            }
            return testCatList;
        } catch (final Exception e) {
            throw new TestToolkitException("Can't get categories", e);
        }

    }

    public long getId() {
        return this.category.getId();
    }
}
