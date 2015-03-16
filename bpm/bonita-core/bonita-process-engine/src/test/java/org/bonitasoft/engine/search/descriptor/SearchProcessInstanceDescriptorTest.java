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
package org.bonitasoft.engine.search.descriptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance;
import org.bonitasoft.engine.exception.IncorrectParameterException;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.search.FilterOperationType;
import org.bonitasoft.engine.search.SearchFilterOperation;
import org.bonitasoft.engine.search.impl.SearchFilter;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 * @version 6.4.0
 * @since 6.4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchProcessInstanceDescriptorTest {

    private SearchProcessInstanceDescriptor searchProcessInstanceDescriptor;

    @Before
    public void before() {
        searchProcessInstanceDescriptor = new SearchProcessInstanceDescriptor();
    }

    /**
     * Test method for {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#getEntityKeys()}.
     */
    @Test
    public final void getEntityKeys_should_return_map_containing_process_instance_name() {
        // When
        final Map<String, FieldDescriptor> entityKeys = searchProcessInstanceDescriptor.getEntityKeys();

        // Then
        final FieldDescriptor fieldDescriptor = entityKeys.get(ProcessInstanceSearchDescriptor.NAME);
        assertNotNull(fieldDescriptor);
        assertEquals(SProcessInstance.class, fieldDescriptor.getPersistentClass());
        assertEquals("name", fieldDescriptor.getValue());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#getEntityKeys()}.
     */
    @Test
    public final void getEntityKeys_should_return_map_containing_process_definition_id() {
        // When
        final Map<String, FieldDescriptor> entityKeys = searchProcessInstanceDescriptor.getEntityKeys();

        // Then
        final FieldDescriptor fieldDescriptor = entityKeys.get(ProcessInstanceSearchDescriptor.PROCESS_DEFINITION_ID);
        assertNotNull(fieldDescriptor);
        assertEquals(SProcessInstance.class, fieldDescriptor.getPersistentClass());
        assertEquals("processDefinitionId", fieldDescriptor.getValue());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#getEntityKeys()}.
     */
    @Test
    public final void getEntityKeys_should_return_map_containing_last_update_date() {
        // When
        final Map<String, FieldDescriptor> entityKeys = searchProcessInstanceDescriptor.getEntityKeys();

        // Then
        final FieldDescriptor fieldDescriptor = entityKeys.get(ProcessInstanceSearchDescriptor.LAST_UPDATE);
        assertNotNull(fieldDescriptor);
        assertEquals(SProcessInstance.class, fieldDescriptor.getPersistentClass());
        assertEquals("lastUpdate", fieldDescriptor.getValue());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#getEntityKeys()}.
     */
    @Test
    public final void getEntityKeys_should_return_map_containing_start_date() {
        // When
        final Map<String, FieldDescriptor> entityKeys = searchProcessInstanceDescriptor.getEntityKeys();

        // Then
        final FieldDescriptor fieldDescriptor = entityKeys.get(ProcessInstanceSearchDescriptor.START_DATE);
        assertNotNull(fieldDescriptor);
        assertEquals(SProcessInstance.class, fieldDescriptor.getPersistentClass());
        assertEquals("startDate", fieldDescriptor.getValue());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#getEntityKeys()}.
     */
    @Test
    public final void getEntityKeys_should_return_map_containing_end_date() {
        // When
        final Map<String, FieldDescriptor> entityKeys = searchProcessInstanceDescriptor.getEntityKeys();

        // Then
        final FieldDescriptor fieldDescriptor = entityKeys.get(ProcessInstanceSearchDescriptor.END_DATE);
        assertNotNull(fieldDescriptor);
        assertEquals(SProcessInstance.class, fieldDescriptor.getPersistentClass());
        assertEquals("endDate", fieldDescriptor.getValue());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#getEntityKeys()}.
     */
    @Test
    public final void getEntityKeys_should_return_map_containing_state_id() {
        // When
        final Map<String, FieldDescriptor> entityKeys = searchProcessInstanceDescriptor.getEntityKeys();

        // Then
        final FieldDescriptor fieldDescriptor = entityKeys.get(ProcessInstanceSearchDescriptor.STATE_ID);
        assertNotNull(fieldDescriptor);
        assertEquals(SProcessInstance.class, fieldDescriptor.getPersistentClass());
        assertEquals("stateId", fieldDescriptor.getValue());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#getEntityKeys()}.
     */
    @Test
    public final void getEntityKeys_should_return_map_containing_state_name() {
        // When
        final Map<String, FieldDescriptor> entityKeys = searchProcessInstanceDescriptor.getEntityKeys();

        // Then
        final FieldDescriptor fieldDescriptor = entityKeys.get(ProcessInstanceSearchDescriptor.STATE_NAME);
        assertNotNull(fieldDescriptor);
        assertEquals(SProcessInstance.class, fieldDescriptor.getPersistentClass());
        assertEquals("stateId", fieldDescriptor.getValue());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#getEntityKeys()}.
     */
    @Test
    public final void getEntityKeys_should_return_map_containing_process_instance_id() {
        // When
        final Map<String, FieldDescriptor> entityKeys = searchProcessInstanceDescriptor.getEntityKeys();

        // Then
        final FieldDescriptor fieldDescriptor = entityKeys.get(ProcessInstanceSearchDescriptor.ID);
        assertNotNull(fieldDescriptor);
        assertEquals(SProcessInstance.class, fieldDescriptor.getPersistentClass());
        assertEquals("id", fieldDescriptor.getValue());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#getEntityKeys()}.
     */
    @Test
    public final void getEntityKeys_should_return_map_containing_started_by() {
        // When
        final Map<String, FieldDescriptor> entityKeys = searchProcessInstanceDescriptor.getEntityKeys();

        // Then
        final FieldDescriptor fieldDescriptor = entityKeys.get(ProcessInstanceSearchDescriptor.STARTED_BY);
        assertNotNull(fieldDescriptor);
        assertEquals(SProcessInstance.class, fieldDescriptor.getPersistentClass());
        assertEquals("startedBy", fieldDescriptor.getValue());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#getEntityKeys()}.
     */
    @Test
    public final void getEntityKeys_should_return_map_containing_caller_id() {
        // When
        final Map<String, FieldDescriptor> entityKeys = searchProcessInstanceDescriptor.getEntityKeys();

        // Then
        final FieldDescriptor fieldDescriptor = entityKeys.get(ProcessInstanceSearchDescriptor.CALLER_ID);
        assertNotNull(fieldDescriptor);
        assertEquals(SProcessInstance.class, fieldDescriptor.getPersistentClass());
        assertEquals("callerId", fieldDescriptor.getValue());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#getEntityKeys()}.
     */
    @Test
    public final void getEntityKeys_should_return_map_containing_user_id() {
        // When
        final Map<String, FieldDescriptor> entityKeys = searchProcessInstanceDescriptor.getEntityKeys();

        // Then
        final FieldDescriptor fieldDescriptor = entityKeys.get(ProcessInstanceSearchDescriptor.USER_ID);
        assertNotNull(fieldDescriptor);
        assertEquals(SProcessSupervisor.class, fieldDescriptor.getPersistentClass());
        assertEquals("userId", fieldDescriptor.getValue());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#getEntityKeys()}.
     */
    @Test
    public final void getEntityKeys_should_return_map_containing_group_id() {
        // When
        final Map<String, FieldDescriptor> entityKeys = searchProcessInstanceDescriptor.getEntityKeys();

        // Then
        final FieldDescriptor fieldDescriptor = entityKeys.get(ProcessInstanceSearchDescriptor.GROUP_ID);
        assertNotNull(fieldDescriptor);
        assertEquals(SProcessSupervisor.class, fieldDescriptor.getPersistentClass());
        assertEquals("groupId", fieldDescriptor.getValue());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#getEntityKeys()}.
     */
    @Test
    public final void getEntityKeys_should_return_map_containing_role_id() {
        // When
        final Map<String, FieldDescriptor> entityKeys = searchProcessInstanceDescriptor.getEntityKeys();

        // Then
        final FieldDescriptor fieldDescriptor = entityKeys.get(ProcessInstanceSearchDescriptor.ROLE_ID);
        assertNotNull(fieldDescriptor);
        assertEquals(SProcessSupervisor.class, fieldDescriptor.getPersistentClass());
        assertEquals("roleId", fieldDescriptor.getValue());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#getEntityKeys()}.
     */
    @Test
    public final void getEntityKeys_should_return_map_containing_assignee_id() {
        // When
        final Map<String, FieldDescriptor> entityKeys = searchProcessInstanceDescriptor.getEntityKeys();

        // Then
        final FieldDescriptor fieldDescriptor = entityKeys.get(ProcessInstanceSearchDescriptor.ASSIGNEE_ID);
        assertNotNull(fieldDescriptor);
        assertEquals(SUserTaskInstance.class, fieldDescriptor.getPersistentClass());
        assertEquals("assigneeId", fieldDescriptor.getValue());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#getAllFields()}.
     */
    @Test
    public final void getAllFields_should_return_map_containing_process_instance_name() {
        // When
        final Map<Class<? extends PersistentObject>, Set<String>> allFields = searchProcessInstanceDescriptor.getAllFields();

        // Then
        final Set<String> fieldDescriptor = allFields.get(SProcessInstance.class);
        assertNotNull(fieldDescriptor);
        assertTrue(fieldDescriptor.contains("name"));
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#convertFilterValue(java.lang.String, java.io.Serializable)}.
     */
    @Test
    public final void convertFilterValue_should_convert_state_name_to_state_id_if_state_name_is_ProcessInstanceState() {
        // When
        final Serializable convertFilterValue = searchProcessInstanceDescriptor.convertFilterValue(ProcessInstanceSearchDescriptor.STATE_NAME,
                ProcessInstanceState.ERROR);

        // Then
        assertEquals(7, convertFilterValue);
    }

    @Test
    public final void convertFilterValue_should_convert_state_name_to_state_id_if_state_name_is_String() {
        // When
        final Serializable convertFilterValue = searchProcessInstanceDescriptor.convertFilterValue(ProcessInstanceSearchDescriptor.STATE_NAME,
                ProcessInstanceState.ABORTED.name());

        // Then
        assertEquals(4, convertFilterValue);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void convertFilterValue_should_throw_exception_if_state_name_is_another_type() {
        // When
        searchProcessInstanceDescriptor.convertFilterValue(ProcessInstanceSearchDescriptor.STATE_NAME, 6);
    }

    @Test
    public final void convertFilterValue_should_not_convert_if_field_is_not_state_name() {
        // When
        final Serializable convertFilterValue = searchProcessInstanceDescriptor.convertFilterValue(ProcessInstanceSearchDescriptor.ID, 6);

        // Then
        assertEquals(6, convertFilterValue);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#constructFilterOption(org.bonitasoft.engine.search.impl.SearchFilter, FieldDescriptor)     *
     */
    @Test
    public final void constructFilterOption_should_convert_SearchFilter_to_FilterOption_when_SearchFilterOperation_is_EQUALS() {
        //Given
        final String fieldName = "name";
        final SearchFilter filter = new SearchFilter(fieldName, SearchFilterOperation.EQUALS, "plop");
        final FieldDescriptor fieldDescriptor = new FieldDescriptor(SProcessInstance.class, fieldName);

        // When
        final FilterOption filterOption = searchProcessInstanceDescriptor.constructFilterOption(filter, fieldDescriptor);

        // Then
        assertEquals(FilterOperationType.EQUALS, filterOption.getFilterOperationType());
        assertEquals(SProcessInstance.class, filterOption.getPersistentClass());
        assertEquals(fieldName, filterOption.getFieldName());
        assertEquals("plop", filterOption.getValue());
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#constructFilterOption(org.bonitasoft.engine.search.impl.SearchFilter, FieldDescriptor)     *
     */
    @Test
    public final void constructFilterOption_should_convert_SearchFilter_to_FilterOption_when_SearchFilterOperation_is_DIFFERENT() {
        //Given
        final String fieldName = "name";
        final SearchFilter filter = new SearchFilter(fieldName, SearchFilterOperation.DIFFERENT, "plop");
        final FieldDescriptor fieldDescriptor = new FieldDescriptor(SProcessInstance.class, fieldName);

        // When
        final FilterOption filterOption = searchProcessInstanceDescriptor.constructFilterOption(filter, fieldDescriptor);

        // Then
        assertEquals(FilterOperationType.DIFFERENT, filterOption.getFilterOperationType());
        assertEquals(SProcessInstance.class, filterOption.getPersistentClass());
        assertEquals(fieldName, filterOption.getFieldName());
        assertEquals("plop", filterOption.getValue());
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#constructFilterOption(org.bonitasoft.engine.search.impl.SearchFilter, FieldDescriptor)
     */
    @Test
    public final void constructFilterOption_should_convert_SearchFilter_to_FilterOption_when_SearchFilterOperation_is_GREATER_OR_EQUAL() {
        //Given
        final String fieldName = "name";
        final SearchFilter filter = new SearchFilter(fieldName, SearchFilterOperation.GREATER_OR_EQUAL, "plop");
        final FieldDescriptor fieldDescriptor = new FieldDescriptor(SProcessInstance.class, fieldName);

        // When
        final FilterOption filterOption = searchProcessInstanceDescriptor.constructFilterOption(filter, fieldDescriptor);

        // Then
        assertEquals(FilterOperationType.GREATER_OR_EQUALS, filterOption.getFilterOperationType());
        assertEquals(SProcessInstance.class, filterOption.getPersistentClass());
        assertEquals(fieldName, filterOption.getFieldName());
        assertEquals("plop", filterOption.getValue());
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#constructFilterOption(org.bonitasoft.engine.search.impl.SearchFilter, FieldDescriptor)
     */
    @Test
    public final void constructFilterOption_should_convert_SearchFilter_to_FilterOption_when_SearchFilterOperation_is_GREATER_THAN() {
        //Given
        final String fieldName = "name";
        final SearchFilter filter = new SearchFilter(fieldName, SearchFilterOperation.GREATER_THAN, "plop");
        final FieldDescriptor fieldDescriptor = new FieldDescriptor(SProcessInstance.class, fieldName);

        // When
        final FilterOption filterOption = searchProcessInstanceDescriptor.constructFilterOption(filter, fieldDescriptor);

        // Then
        assertEquals(FilterOperationType.GREATER, filterOption.getFilterOperationType());
        assertEquals(SProcessInstance.class, filterOption.getPersistentClass());
        assertEquals(fieldName, filterOption.getFieldName());
        assertEquals("plop", filterOption.getValue());
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#constructFilterOption(org.bonitasoft.engine.search.impl.SearchFilter, FieldDescriptor)
     */
    @Test
    public final void constructFilterOption_should_convert_SearchFilter_to_FilterOption_when_SearchFilterOperation_is_LESS_OR_EQUAL() {
        //Given
        final String fieldName = "name";
        final SearchFilter filter = new SearchFilter(fieldName, SearchFilterOperation.LESS_OR_EQUAL, "plop");
        final FieldDescriptor fieldDescriptor = new FieldDescriptor(SProcessInstance.class, fieldName);

        // When
        final FilterOption filterOption = searchProcessInstanceDescriptor.constructFilterOption(filter, fieldDescriptor);

        // Then
        assertEquals(FilterOperationType.LESS_OR_EQUALS, filterOption.getFilterOperationType());
        assertEquals(SProcessInstance.class, filterOption.getPersistentClass());
        assertEquals(fieldName, filterOption.getFieldName());
        assertEquals("plop", filterOption.getValue());
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#constructFilterOption(org.bonitasoft.engine.search.impl.SearchFilter, FieldDescriptor)
     */
    @Test
    public final void constructFilterOption_should_convert_SearchFilter_to_FilterOption_when_SearchFilterOperation_is_LESS_THAN() {
        //Given
        final String fieldName = "name";
        final SearchFilter filter = new SearchFilter(fieldName, SearchFilterOperation.LESS_THAN, "plop");
        final FieldDescriptor fieldDescriptor = new FieldDescriptor(SProcessInstance.class, fieldName);

        // When
        final FilterOption filterOption = searchProcessInstanceDescriptor.constructFilterOption(filter, fieldDescriptor);

        // Then
        assertEquals(FilterOperationType.LESS, filterOption.getFilterOperationType());
        assertEquals(SProcessInstance.class, filterOption.getPersistentClass());
        assertEquals(fieldName, filterOption.getFieldName());
        assertEquals("plop", filterOption.getValue());
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#constructFilterOption(org.bonitasoft.engine.search.impl.SearchFilter, FieldDescriptor)
     */
    @Test
    public final void constructFilterOption_should_convert_SearchFilter_to_FilterOption_when_SearchFilterOperation_is_BETWEEN() {
        //Given
        final String fieldName = "id";
        final long from = 697L;
        final long to = 9633L;
        final SearchFilter filter = new SearchFilter(fieldName, from, to);
        final FieldDescriptor fieldDescriptor = new FieldDescriptor(SProcessInstance.class, fieldName);

        // When
        final FilterOption filterOption = searchProcessInstanceDescriptor.constructFilterOption(filter, fieldDescriptor);

        // Then
        assertEquals(FilterOperationType.BETWEEN, filterOption.getFilterOperationType());
        assertEquals(SProcessInstance.class, filterOption.getPersistentClass());
        assertEquals(fieldName, filterOption.getFieldName());
        assertEquals(from, filterOption.getFrom());
        assertEquals(to, filterOption.getTo());
        assertNull(filterOption.getValue());
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#constructFilterOption(org.bonitasoft.engine.search.impl.SearchFilter, FieldDescriptor)
     */
    @Test
    public final void constructFilterOption_should_convert_SearchFilter_to_FilterOption_when_SearchFilterOperation_is_AND() throws IncorrectParameterException {
        //Given
        final SearchFilter filter = new SearchFilter(SearchFilterOperation.AND);

        // When
        final FilterOption filterOption = searchProcessInstanceDescriptor.constructFilterOption(filter, null);

        // Then
        assertEquals(FilterOperationType.AND, filterOption.getFilterOperationType());
        assertNull(filterOption.getPersistentClass());
        assertNull(filterOption.getFieldName());
        assertNull(filterOption.getValue());
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#constructFilterOption(org.bonitasoft.engine.search.impl.SearchFilter, FieldDescriptor)
     */
    @Test
    public final void constructFilterOption_should_convert_SearchFilter_to_FilterOption_when_SearchFilterOperation_is_OR() throws IncorrectParameterException {
        //Given
        final SearchFilter filter = new SearchFilter(SearchFilterOperation.OR);

        // When
        final FilterOption filterOption = searchProcessInstanceDescriptor.constructFilterOption(filter, null);

        // Then
        assertEquals(FilterOperationType.OR, filterOption.getFilterOperationType());
        assertNull(filterOption.getPersistentClass());
        assertNull(filterOption.getFieldName());
        assertNull(filterOption.getValue());
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#constructFilterOption(org.bonitasoft.engine.search.impl.SearchFilter, FieldDescriptor)
     */
    @Test
    public final void constructFilterOption_should_convert_SearchFilter_to_FilterOption_when_SearchFilterOperation_is_L_PARENTHESIS()
            throws IncorrectParameterException {
        //Given
        final SearchFilter filter = new SearchFilter(SearchFilterOperation.L_PARENTHESIS);

        // When
        final FilterOption filterOption = searchProcessInstanceDescriptor.constructFilterOption(filter, null);

        // Then
        assertEquals(FilterOperationType.L_PARENTHESIS, filterOption.getFilterOperationType());
        assertNull(filterOption.getPersistentClass());
        assertNull(filterOption.getFieldName());
        assertNull(filterOption.getValue());
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor#constructFilterOption(org.bonitasoft.engine.search.impl.SearchFilter, FieldDescriptor)
     */
    @Test
    public final void constructFilterOption_should_convert_SearchFilter_to_FilterOption_when_SearchFilterOperation_is_R_PARENTHESIS()
            throws IncorrectParameterException {
        //Given
        final SearchFilter filter = new SearchFilter(SearchFilterOperation.R_PARENTHESIS);

        // When
        final FilterOption filterOption = searchProcessInstanceDescriptor.constructFilterOption(filter, null);

        // Then
        assertEquals(FilterOperationType.R_PARENTHESIS, filterOption.getFilterOperationType());
        assertNull(filterOption.getPersistentClass());
        assertNull(filterOption.getFieldName());
        assertNull(filterOption.getValue());
    }

}
