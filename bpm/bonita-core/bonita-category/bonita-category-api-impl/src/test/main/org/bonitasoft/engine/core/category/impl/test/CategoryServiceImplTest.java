package org.bonitasoft.engine.core.category.impl.test;

/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.core.category.exception.SCategoryException;
import org.bonitasoft.engine.core.category.exception.SCategoryNotFoundException;
import org.bonitasoft.engine.core.category.impl.CategoryServiceImpl;
import org.bonitasoft.engine.core.category.model.SCategory;
import org.bonitasoft.engine.core.category.model.builder.SCategoryBuilderAccessor;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Celine Souchet
 * 
 */
public class CategoryServiceImplTest {

    private Recorder recorder;

    private ReadPersistenceService persistenceService;

    private EventService eventService;

    private QueriableLoggerService queriableLoggerService;

    private SessionService sessionService;

    private ReadSessionAccessor sessionAccessor;

    private SCategoryBuilderAccessor categoryBuilderAccessor;

    private CategoryServiceImpl categoryServiceImpl;

    @Before
    public void setUp() throws Exception {
        recorder = mock(Recorder.class);
        persistenceService = mock(ReadPersistenceService.class);
        eventService = mock(EventService.class);
        queriableLoggerService = mock(QueriableLoggerService.class);
        sessionService = mock(SessionService.class);
        sessionAccessor = mock(ReadSessionAccessor.class);
        categoryBuilderAccessor = mock(SCategoryBuilderAccessor.class);
        categoryServiceImpl = new CategoryServiceImpl(persistenceService, recorder, eventService, sessionService, sessionAccessor, categoryBuilderAccessor,
                queriableLoggerService);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.category.impl.CategoryServiceImpl#getCategory(long)}.
     * 
     * @throws SCategoryNotFoundException
     * @throws SBonitaReadException
     */
    @Test
    public final void getCategoryById() throws SCategoryNotFoundException, SBonitaReadException {
        final SCategory sCategory = mock(SCategory.class);
        when(persistenceService.selectById(any(SelectByIdDescriptor.class))).thenReturn(sCategory);

        Assert.assertEquals(sCategory, categoryServiceImpl.getCategory(456L));
    }

    @Test(expected = SCategoryNotFoundException.class)
    public final void getCategoryByIdNotExists() throws SBonitaReadException, SCategoryNotFoundException {
        when(persistenceService.selectById(any(SelectByIdDescriptor.class))).thenReturn(null);

        categoryServiceImpl.getCategory(456L);
    }

    @Test(expected = SCategoryNotFoundException.class)
    public final void getCategoryByIdWithException() throws SBonitaReadException, SCategoryNotFoundException {
        when(persistenceService.selectById(any(SelectByIdDescriptor.class))).thenThrow(new SBonitaReadException(""));

        categoryServiceImpl.getCategory(456L);
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
        final List<SCategory> sCategories = new ArrayList<SCategory>();
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(sCategories);

        Assert.assertEquals(sCategories, categoryServiceImpl.getCategories(0, 1, "field", OrderByType.ASC));
    }

    @Test(expected = SCategoryException.class)
    public final void getCategoriesWithException() throws SBonitaReadException, SCategoryException {
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenThrow(new SBonitaReadException(""));

        categoryServiceImpl.getCategories(0, 1, "field", OrderByType.ASC);
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
        final List<SCategory> sCategories = new ArrayList<SCategory>();
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(sCategories);

        Assert.assertEquals(sCategories, categoryServiceImpl.getCategoriesOfProcessDefinition(2L, 0, 1, OrderByType.ASC));
    }

    @Test(expected = SCategoryException.class)
    public final void getCategoriesOfProcessDefinitionWithException() throws SBonitaReadException, SCategoryException {
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenThrow(new SBonitaReadException(""));

        categoryServiceImpl.getCategoriesOfProcessDefinition(2L, 0, 1, OrderByType.ASC);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.category.impl.CategoryServiceImpl#getCategorizedProcessIds(java.util.List)}.
     * 
     * @throws SCategoryException
     * @throws SBonitaReadException
     */
    @Test
    public final void getCategorizedProcessIds() throws SCategoryException, SBonitaReadException {
        final List<SCategory> sCategories = new ArrayList<SCategory>();
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(sCategories);

        final List<Long> processIds = new ArrayList<Long>();
        processIds.add(14654L);
        Assert.assertEquals(sCategories, categoryServiceImpl.getCategorizedProcessIds(processIds));
    }

    @Test
    public final void getCategorizedProcessIdsWithNullList() throws SCategoryException {
        Assert.assertTrue(categoryServiceImpl.getCategorizedProcessIds(null).isEmpty());
    }

    @Test
    public final void getCategorizedProcessIdsWithEmptyList() throws SCategoryException {
        Assert.assertTrue(categoryServiceImpl.getCategorizedProcessIds(new ArrayList<Long>()).isEmpty());
    }

    @Test(expected = SCategoryException.class)
    public final void getCategorizedProcessIdsWithException() throws SBonitaReadException, SCategoryException {
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenThrow(new SBonitaReadException(""));

        final List<Long> processIds = new ArrayList<Long>();
        processIds.add(14654L);
        categoryServiceImpl.getCategorizedProcessIds(processIds);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.category.impl.CategoryServiceImpl#getCategoryByName(java.lang.String)}.
     * 
     * @throws SCategoryNotFoundException
     * @throws SBonitaReadException
     */
    @Test
    public final void getCategoryByName() throws SCategoryNotFoundException, SBonitaReadException {
        final SCategory sCategory = mock(SCategory.class);
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(sCategory);

        Assert.assertEquals(sCategory, categoryServiceImpl.getCategoryByName("name"));
    }

    @Test(expected = SCategoryNotFoundException.class)
    public final void getCategoryByNameNotExists() throws SBonitaReadException, SCategoryNotFoundException {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(null);

        categoryServiceImpl.getCategoryByName("name");
    }

    @Test(expected = SCategoryNotFoundException.class)
    public final void getCategoryByNameWithException() throws SBonitaReadException, SCategoryNotFoundException {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException(""));

        categoryServiceImpl.getCategoryByName("name");
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.category.impl.CategoryServiceImpl#getNumberOfCategories()}.
     * 
     * @throws SBonitaReadException
     * @throws SCategoryException
     */
    @Test
    public final void getNumberOfCategories() throws SBonitaReadException, SCategoryException {
        final long numberOfCategories = 3L;
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(numberOfCategories);

        Assert.assertEquals(numberOfCategories, categoryServiceImpl.getNumberOfCategories());
    }

    @Test(expected = SCategoryException.class)
    public final void getNumberOfCategoriesWithException() throws SCategoryException, SBonitaReadException {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException(""));

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
        final long numberOfCategories = 3L;
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(numberOfCategories);

        Assert.assertEquals(numberOfCategories, categoryServiceImpl.getNumberOfCategoriesOfProcess(1589L));
    }

    @Test(expected = SCategoryException.class)
    public final void getNumberOfCategoriesOfProcessWithException() throws SCategoryException, SBonitaReadException {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException(""));

        categoryServiceImpl.getNumberOfCategoriesOfProcess(1589L);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.category.impl.CategoryServiceImpl#getNumberOfCategoriesUnrelatedToProcess(long)}.
     * 
     * @throws SCategoryException
     * @throws SBonitaReadException
     */
    @Test
    public final void getNumberOfCategoriesUnrelatedToProcess() throws SCategoryException, SBonitaReadException {
        final long numberOfCategories = 3L;
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(numberOfCategories);

        Assert.assertEquals(numberOfCategories, categoryServiceImpl.getNumberOfCategoriesUnrelatedToProcess(1589L));
    }

    @Test(expected = SCategoryException.class)
    public final void getNumberOfCategoriesUnrelatedToProcessWithException() throws SCategoryException, SBonitaReadException {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException(""));

        categoryServiceImpl.getNumberOfCategoriesUnrelatedToProcess(1589L);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.category.impl.CategoryServiceImpl#getNumberOfCategorizedProcessIds(java.util.List)}.
     * 
     * @throws SCategoryException
     * @throws SBonitaReadException
     */
    @Test
    public final void getNumberOfCategorizedProcessIds() throws SCategoryException, SBonitaReadException {
        final long numberOfCategories = 3L;
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(numberOfCategories);

        final List<Long> processIds = new ArrayList<Long>();
        processIds.add(14654L);
        Assert.assertEquals(numberOfCategories, categoryServiceImpl.getNumberOfCategorizedProcessIds(processIds));
    }

    @Test
    public final void getNumberOfCategorizedProcessIdsWithNullList() throws SCategoryException {
        Assert.assertEquals(0, categoryServiceImpl.getNumberOfCategorizedProcessIds(null));
    }

    @Test
    public final void getNumberOfCategorizedProcessIdsWithEmptyList() throws SCategoryException {
        Assert.assertEquals(0, categoryServiceImpl.getNumberOfCategorizedProcessIds(new ArrayList<Long>()));
    }

    @Test(expected = SCategoryException.class)
    public final void getNumberOfCategorizedProcessIdsWithException() throws SBonitaReadException, SCategoryException {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException(""));

        final List<Long> processIds = new ArrayList<Long>();
        processIds.add(14654L);
        categoryServiceImpl.getNumberOfCategorizedProcessIds(processIds);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.category.impl.CategoryServiceImpl#getProcessDefinitionIdsOfCategory(long)}.
     * 
     * @throws SCategoryException
     * @throws SBonitaReadException
     */
    @Test
    public final void getProcessDefinitionIdsOfCategory() throws SCategoryException, SBonitaReadException {
        final List<SCategory> sCategories = new ArrayList<SCategory>();
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(sCategories);

        Assert.assertEquals(sCategories, categoryServiceImpl.getProcessDefinitionIdsOfCategory(54894L));
    }

    @Test(expected = SCategoryException.class)
    public final void getProcessDefinitionIdsOfCategoryWithException() throws SBonitaReadException, SCategoryException {
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenThrow(new SBonitaReadException(""));

        categoryServiceImpl.getProcessDefinitionIdsOfCategory(54894L);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.category.impl.CategoryServiceImpl#getCategoriesUnrelatedToProcessDefinition(long, int, int, org.bonitasoft.engine.persistence.OrderByType)}
     * .
     */
    @Test
    public final void getCategoriesUnrelatedToProcessDefinition() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.category.impl.CategoryServiceImpl#createCategory(java.lang.String, java.lang.String)}.
     */
    @Test
    public final void createCategory() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.category.impl.CategoryServiceImpl#updateCategory(long, org.bonitasoft.engine.core.category.model.SCategory)}.
     */
    @Test
    public final void updateCategory() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.category.impl.CategoryServiceImpl#deleteCategory(long)}.
     */
    @Test
    public final void deleteCategory() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.category.impl.CategoryServiceImpl#addProcessDefinitionToCategory(long, long)}.
     */
    @Test
    public final void addProcessDefinitionToCategory() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.category.impl.CategoryServiceImpl#addProcessDefinitionsToCategory(long, java.util.List)}.
     */
    @Test
    public final void addProcessDefinitionsToCategory() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.category.impl.CategoryServiceImpl#removeCategoriesFromProcessDefinition(long, java.util.List)}.
     */
    @Test
    public final void removeCategoriesFromProcessDefinition() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.category.impl.CategoryServiceImpl#removeProcessDefinitionsOfCategory(long)}.
     */
    @Test
    public final void removeProcessDefinitionsOfCategory() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.category.impl.CategoryServiceImpl#removeProcessIdOfCategories(long)}.
     */
    @Test
    public final void removeProcessIdOfCategories() {
        // TODO : Not yet implemented
    }

}
