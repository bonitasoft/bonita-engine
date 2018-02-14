/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.engine.business.data.impl;

import java.util.List;

import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.Query;
import org.bonitasoft.engine.bdm.model.assertion.QueryAssert;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class CountQueryProviderTest {

    public static final String QUALIFIED_NAME = "com.corp.Arrival";

    private CountQueryProvider provider;

    @Before
    public void setUp() throws Exception {
        provider = new CountQueryProvider();
    }

    @Test
    public void getCountQueryDefinition_should_return_related_count_query_for_generated_queries() throws Exception {
        //given
        final BusinessObject bo = buildBusinessObject();
        Query find = new Query("find", "", List.class.getName());

        //when
        Query countQueryDefinition = provider.getCountQueryDefinition(bo, find);

        //then
        QueryAssert.assertThat(countQueryDefinition).isNotNull();
        QueryAssert.assertThat(countQueryDefinition).hasName("countForFind");
    }

    @Test
    public void getCountQueryDefinition_should_return_null_when_base_query_is_not_multiple() throws Exception {
        //given
        final BusinessObject bo = buildBusinessObject();
        Query find = new Query("findByPeople", "", QUALIFIED_NAME);

        //when
        Query countQueryDefinition = provider.getCountQueryDefinition(bo, find);

        //then
        QueryAssert.assertThat(countQueryDefinition).isNull();
    }

    @Test
    public void getCountQueryDefinition_should_return_related_query_for_multiple_custom_queries() throws Exception {
        //given
        final BusinessObject bo = buildBusinessObject();
        bo.addQuery("firstMultiCustomQuery", "", List.class.getName());
        Query secondMultiCustomQuery = bo.addQuery("secondMultiCustomQuery", "", List.class.getName());
        bo.addQuery("thirdMultiCustomQuery", "", List.class.getName());
        bo.addQuery("countForFirstMultiCustomQuery", "", Long.class.getName());
        Query countForSecondMultiCustomQuery = bo.addQuery("countForSecondMultiCustomQuery", "", Long.class.getName());
        bo.addQuery("countForThirdMultiCustomQuery", "", Long.class.getName());

        //when
        Query countQueryDefinition = provider.getCountQueryDefinition(bo, secondMultiCustomQuery);

        //then
        QueryAssert.assertThat(countQueryDefinition).isNotNull();
        QueryAssert.assertThat(countQueryDefinition).hasName(countForSecondMultiCustomQuery.getName());
    }

    @Test
    public void getCountQueryDefinition_should_return_null_when_count_query_does_not_return_a_long() throws Exception {
        //given
        final BusinessObject bo = buildBusinessObject();
        Query secondMultiCustomQuery = bo.addQuery("secondMultiCustomQuery", "", List.class.getName());
        bo.addQuery("countForSecondMultiCustomQuery", "", QUALIFIED_NAME);

        //when
        Query countQueryDefinition = provider.getCountQueryDefinition(bo, secondMultiCustomQuery);

        //then
        QueryAssert.assertThat(countQueryDefinition).isNull();
    }

    @Test
    public void getCountQueryDefinition_should_return_null_when_base_query_returns_single_result() throws Exception {
        //given
        final BusinessObject bo = buildBusinessObject();
        Query secondMultiCustomQuery = bo.addQuery("secondMultiCustomQuery", "", QUALIFIED_NAME);
        bo.addQuery("countForSecondMultiCustomQuery", "", Long.class.getName());

        //when
        Query countQueryDefinition = provider.getCountQueryDefinition(bo, secondMultiCustomQuery);

        //then
        QueryAssert.assertThat(countQueryDefinition).isNull();
    }

    private BusinessObject buildBusinessObject() {
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName(QUALIFIED_NAME);
        final SimpleField field = new SimpleField();
        field.setName("people");
        field.setType(FieldType.INTEGER);
        bo.addField(field);
        bo.addUniqueConstraint("someName", "people");
        return bo;
    }

}
