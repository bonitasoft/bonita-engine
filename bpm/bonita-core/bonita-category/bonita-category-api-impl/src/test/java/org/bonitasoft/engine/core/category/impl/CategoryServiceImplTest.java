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
package org.bonitasoft.engine.core.category.impl;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.core.category.exception.SCategoryException;
import org.bonitasoft.engine.core.category.exception.SCategoryNotFoundException;
import org.bonitasoft.engine.core.category.model.SCategory;
import org.bonitasoft.engine.core.category.persistence.SelectDescriptorBuilder;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Celine Souchet
 */
public class CategoryServiceImplTest {

    @Mock
    private Recorder recorder;

    @Mock
    private ReadPersistenceService persistenceService;

    @Mock
    private EventService eventService;

    @Mock
    private QueriableLoggerService queriableLoggerService;

    @Mock
    private SessionService sessionService;

    @Mock
    private ReadSessionAccessor sessionAccessor;

    @InjectMocks
    private CategoryServiceImpl categoryServiceImpl;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.category.impl.CategoryServiceImpl#getCategory(long)}.
     *
     * @throws SCategoryNotFoundException
     * @throws SBonitaReadException
     */
    @Test
    public final void getCategoryById() throws SCategoryNotFoundException, SBonitaReadException {
        // Given
        final long id = 456L;
        final SCategory sCategory = mock(SCategory.class);
        doReturn(sCategory).when(persistenceService).selectById(SelectDescriptorBuilder.getCategory(id));

        // When
        final SCategory category = categoryServiceImpl.getCategory(id);

        // Then
        Assert.assertEquals(sCategory, category);
    }

    @Test(expected = SCategoryNotFoundException.class)
    public final void getCategoryByIdNotExists() throws SBonitaReadException, SCategoryNotFoundException {
        // Given
        final long id = 456L;
        doReturn(null).when(persistenceService).selectById(SelectDescriptorBuilder.getCategory(id));

        // When
        categoryServiceImpl.getCategory(id);
    }

    @Test(expected = SCategoryNotFoundException.class)
    public final void getCategoryByIdThrowException() throws SBonitaReadException, SCategoryNotFoundException {
        // Given
        final long id = 456L;
        doThrow(new SBonitaReadException("")).when(persistenceService).selectById(SelectDescriptorBuilder.getCategory(id));

        // When
        categoryServiceImpl.getCategory(id);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.category.impl.CategoryServiceImpl#getCategories(int, int, java.lang.String, org.bonitasoft.engine.persistence.OrderByType)}
     * .
     *
     * @throws SCategoryException
     * @throws SBonitaReadException
     */
    @Test
    public final void getCategories() throws SCategoryException, SBonitaReadException {
        // Given
        final List<SCategory> sCategories = new ArrayList<SCategory>();
        final String field = "field";
        final OrderByType order = OrderByType.ASC;
        final int fromIndex = 0;
        final int numberOfCategories = 1;
        doReturn(sCategories).when(persistenceService).selectList(SelectDescriptorBuilder.getCategories(field, order, fromIndex, numberOfCategories));

        // When
        final List<SCategory> categories = categoryServiceImpl.getCategories(fromIndex, numberOfCategories, field, order);

        // Then
        Assert.assertEquals(sCategories, categories);
    }

    @Test(expected = SCategoryException.class)
    public final void getCategoriesThrowException() throws SBonitaReadException, SCategoryException {
        // Given
        final String field = "field";
        final OrderByType order = OrderByType.ASC;
        final int fromIndex = 0;
        final int numberOfCategories = 1;
        doThrow(new SBonitaReadException("")).when(persistenceService).selectList(
                SelectDescriptorBuilder.getCategories(field, order, fromIndex, numberOfCategories));

        // When
        categoryServiceImpl.getCategories(fromIndex, numberOfCategories, field, order);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.category.impl.CategoryServiceImpl#getCategoriesOfProcessDefinition(long, int, int, org.bonitasoft.engine.persistence.OrderByType)}
     * .
     *
     * @throws SCategoryException
     * @throws SBonitaReadException
     */
    @Test
    public final void getCategoriesOfProcessDefinition() throws SCategoryException, SBonitaReadException {
        // Given
        final List<SCategory> sCategories = new ArrayList<SCategory>();
        final long processDefinitionId = 2L;
        final int fromIndex = 0;
        final int numberOfCategories = 1;
        final OrderByType order = OrderByType.ASC;
        doReturn(sCategories).when(persistenceService).selectList(
                SelectDescriptorBuilder.getCategoriesOfProcess(processDefinitionId, fromIndex, numberOfCategories, order));

        // When
        final List<SCategory> categoriesOfProcessDefinition = categoryServiceImpl.getCategoriesOfProcessDefinition(processDefinitionId, fromIndex,
                numberOfCategories, order);

        // Then
        Assert.assertEquals(sCategories, categoriesOfProcessDefinition);
    }

    @Test(expected = SCategoryException.class)
    public final void getCategoriesOfProcessDefinitionThrowException() throws SBonitaReadException, SCategoryException {
        // Given
        final long processDefinitionId = 2L;
        final int fromIndex = 0;
        final int numberOfCategories = 1;
        final OrderByType order = OrderByType.ASC;
        doThrow(new SBonitaReadException("")).when(persistenceService).selectList(
                SelectDescriptorBuilder.getCategoriesOfProcess(processDefinitionId, fromIndex, numberOfCategories, order));

        // When
        categoryServiceImpl.getCategoriesOfProcessDefinition(processDefinitionId, fromIndex, numberOfCategories, order);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.category.impl.CategoryServiceImpl#getCategoryByName(java.lang.String)}.
     *
     * @throws SCategoryNotFoundException
     * @throws SBonitaReadException
     */
    @Test
    public final void getCategoryByName() throws SCategoryNotFoundException, SBonitaReadException {
        // Given
        final SCategory sCategory = mock(SCategory.class);
        final String name = "name";
        doReturn(sCategory).when(persistenceService).selectOne(SelectDescriptorBuilder.getCategory(name));

        // When
        final SCategory categoryByName = categoryServiceImpl.getCategoryByName(name);

        // Then
        Assert.assertEquals(sCategory, categoryByName);
    }

    @Test(expected = SCategoryNotFoundException.class)
    public final void getCategoryByNameNotExists() throws SBonitaReadException, SCategoryNotFoundException {
        // Given
        final String name = "name";
        doReturn(null).when(persistenceService).selectOne(SelectDescriptorBuilder.getCategory(name));

        // When
        categoryServiceImpl.getCategoryByName(name);
    }

    @Test(expected = SCategoryNotFoundException.class)
    public final void getCategoryByNameThrowException() throws SBonitaReadException, SCategoryNotFoundException {
        // Given
        final String name = "name";
        doThrow(new SBonitaReadException("")).when(persistenceService).selectOne(SelectDescriptorBuilder.getCategory(name));

        // When
        categoryServiceImpl.getCategoryByName(name);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.category.impl.CategoryServiceImpl#getNumberOfCategories()}.
     *
     * @throws SBonitaReadException
     * @throws SCategoryException
     */
    @Test
    public final void getNumberOfCategories() throws SBonitaReadException, SCategoryException {
        // Given
        final long numberOfCategories = 3L;
        doReturn(numberOfCategories).when(persistenceService).selectOne(SelectDescriptorBuilder.getNumberOfElement("Category", SCategory.class));

        // When
        final long result = categoryServiceImpl.getNumberOfCategories();

        // Then
        Assert.assertEquals(numberOfCategories, result);
    }

    @Test(expected = SCategoryException.class)
    public final void getNumberOfCategoriesThrowException() throws SCategoryException, SBonitaReadException {
        // Given
        doThrow(new SBonitaReadException("")).when(persistenceService).selectOne(SelectDescriptorBuilder.getNumberOfElement("Category", SCategory.class));

        // When
        categoryServiceImpl.getNumberOfCategories();
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.category.impl.CategoryServiceImpl#getNumberOfCategoriesOfProcess(long)}.
     *
     * @throws SCategoryException
     * @throws SBonitaReadException
     */
    @Test
    public final void getNumberOfCategoriesOfProcess() throws SCategoryException, SBonitaReadException {
        // Given
        final long numberOfCategories = 3L;
        final long processDefinitionId = 1589L;
        doReturn(numberOfCategories).when(persistenceService).selectOne(SelectDescriptorBuilder.getNumberOfCategoriesOfProcess(processDefinitionId));

        // When
        final long result = categoryServiceImpl.getNumberOfCategoriesOfProcess(processDefinitionId);

        // Then
        Assert.assertEquals(numberOfCategories, result);
    }

    @Test(expected = SCategoryException.class)
    public final void getNumberOfCategoriesOfProcessThrowException() throws SCategoryException, SBonitaReadException {
        // Given
        final long processDefinitionId = 1589L;
        doThrow(new SBonitaReadException("")).when(persistenceService).selectOne(SelectDescriptorBuilder.getNumberOfCategoriesOfProcess(processDefinitionId));

        // When
        categoryServiceImpl.getNumberOfCategoriesOfProcess(processDefinitionId);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.category.impl.CategoryServiceImpl#getNumberOfCategoriesUnrelatedToProcess(long)}.
     *
     * @throws SCategoryException
     * @throws SBonitaReadException
     */
    @Test
    public final void getNumberOfCategoriesUnrelatedToProcess() throws SCategoryException, SBonitaReadException {
        // Given
        final long numberOfCategories = 3L;
        final long processDefinitionId = 1589L;
        doReturn(numberOfCategories).when(persistenceService).selectOne(SelectDescriptorBuilder.getNumberOfCategoriesUnrelatedToProcess(processDefinitionId));

        // When
        final long numberOfCategoriesUnrelatedToProcess = categoryServiceImpl.getNumberOfCategoriesUnrelatedToProcess(processDefinitionId);

        // Then
        Assert.assertEquals(numberOfCategories, numberOfCategoriesUnrelatedToProcess);
    }

    @Test(expected = SCategoryException.class)
    public final void getNumberOfCategoriesUnrelatedToProcessThrowException() throws SCategoryException, SBonitaReadException {
        // Given
        final long processDefinitionId = 1589L;
        doThrow(new SBonitaReadException("")).when(persistenceService).selectOne(
                SelectDescriptorBuilder.getNumberOfCategoriesUnrelatedToProcess(processDefinitionId));

        // When
        categoryServiceImpl.getNumberOfCategoriesUnrelatedToProcess(processDefinitionId);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.category.impl.CategoryServiceImpl#getNumberOfCategorizedProcessIds(java.util.List)}.
     *
     * @throws SCategoryException
     * @throws SBonitaReadException
     */
    @Test
    public final void getNumberOfCategorizedProcessIds() throws SCategoryException, SBonitaReadException {
        // Given
        final List<Long> processIds = new ArrayList<Long>();
        processIds.add(14654L);
        final long numberOfCategories = 3L;
        doReturn(numberOfCategories).when(persistenceService).selectOne(SelectDescriptorBuilder.getNumberOfCategorizedProcessIds(processIds));

        // When
        final long numberOfCategorizedProcessIds = categoryServiceImpl.getNumberOfCategorizedProcessIds(processIds);

        // Then
        Assert.assertEquals(numberOfCategories, numberOfCategorizedProcessIds);
    }

    @Test
    public final void getNumberOfCategorizedProcessIdsWithNullList() throws SCategoryException {
        // When
        final long numberOfCategorizedProcessIds = categoryServiceImpl.getNumberOfCategorizedProcessIds(null);

        // Then
        Assert.assertEquals(0, numberOfCategorizedProcessIds);
    }

    @Test
    public final void getNumberOfCategorizedProcessIdsWithEmptyList() throws SCategoryException {
        // When
        final long numberOfCategorizedProcessIds = categoryServiceImpl.getNumberOfCategorizedProcessIds(new ArrayList<Long>());

        // Then
        Assert.assertEquals(0, numberOfCategorizedProcessIds);
    }

    @Test(expected = SCategoryException.class)
    public final void getNumberOfCategorizedProcessIdsThrowException() throws SBonitaReadException, SCategoryException {
        // Given
        final List<Long> processIds = new ArrayList<Long>();
        processIds.add(14654L);
        doThrow(new SBonitaReadException("")).when(persistenceService).selectOne(SelectDescriptorBuilder.getNumberOfCategorizedProcessIds(processIds));

        // When
        categoryServiceImpl.getNumberOfCategorizedProcessIds(processIds);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.category.impl.CategoryServiceImpl#getCategoriesUnrelatedToProcessDefinition(long, int, int, org.bonitasoft.engine.persistence.OrderByType)}
     * .
     */
    @Test
    public final void getCategoriesUnrelatedToProcessDefinition() throws SBonitaReadException, SCategoryException {
        // Given
        final List<SCategory> sCategories = new ArrayList<SCategory>();
        final long processDefinitionId = 54894L;
        final int fromIndex = 0;
        final int numberOfCategories = 10;
        final OrderByType order = OrderByType.ASC;
        doReturn(sCategories).when(persistenceService).selectList(SelectDescriptorBuilder.getCategoriesUnrelatedToProcess(processDefinitionId, fromIndex,
                numberOfCategories, order));

        // When
        final List<SCategory> categoriesUnrelatedToProcessDefinition = categoryServiceImpl.getCategoriesUnrelatedToProcessDefinition(processDefinitionId,
                fromIndex, numberOfCategories, order);

        // Then
        Assert.assertEquals(sCategories, categoriesUnrelatedToProcessDefinition);
    }

    @Test(expected = SCategoryException.class)
    public final void getCategoriesUnrelatedToProcessDefinitionThrowException() throws SBonitaReadException, SCategoryException {
        // Given
        final long processDefinitionId = 54894L;
        final int fromIndex = 0;
        final int numberOfCategories = 10;
        final OrderByType order = OrderByType.ASC;
        doThrow(new SBonitaReadException("")).when(persistenceService).selectList(
                SelectDescriptorBuilder.getCategoriesUnrelatedToProcess(processDefinitionId, fromIndex,
                        numberOfCategories, order));

        // When
        categoryServiceImpl.getCategoriesUnrelatedToProcessDefinition(processDefinitionId, fromIndex, numberOfCategories, order);
    }

}
