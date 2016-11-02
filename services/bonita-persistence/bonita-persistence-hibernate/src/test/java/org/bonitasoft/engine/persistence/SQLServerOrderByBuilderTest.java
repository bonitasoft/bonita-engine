/**
 * Copyright (C) 2016 BonitaSoft S.A.
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
package org.bonitasoft.engine.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Laurent Leseigneur
 */
public class SQLServerOrderByBuilderTest {

    private StringBuilder builder;

    private SQLServerOrderByBuilder sqlServerInterceptor;

    @Before
    public void before() {
        builder = new StringBuilder();
        sqlServerInterceptor = new SQLServerOrderByBuilder();
    }

    @Test
    public void should_sort_asc() {
        //when
        sqlServerInterceptor.appendOrderBy(builder, "field", OrderByType.ASC);

        //then
        assertThat(builder.toString()).isEqualTo("field ASC");
    }

    @Test
    public void should_sort_asc_with_nulls_first() {
        //when
        sqlServerInterceptor.appendOrderBy(builder, "field", OrderByType.ASC_NULLS_FIRST);

        //then
        assertThat(builder.toString()).isEqualTo("CASE WHEN field IS NULL THEN 0 ELSE 1 END ASC, field ASC");
    }

    @Test
    public void should_sort_asc_with_nulls_last() {
        //when
        sqlServerInterceptor.appendOrderBy(builder, "field", OrderByType.ASC_NULLS_LAST);

        //then
        assertThat(builder.toString()).isEqualTo("CASE WHEN field IS NULL THEN 0 ELSE 1 END DESC, field ASC");
    }

    @Test
    public void should_sort_desc() {
        //when
        sqlServerInterceptor.appendOrderBy(builder, "field", OrderByType.DESC);

        //then
        assertThat(builder.toString()).isEqualTo("field DESC");
    }

    @Test
    public void should_sort_desc_with_nulls_first() {
        //when
        sqlServerInterceptor.appendOrderBy(builder, "field", OrderByType.DESC_NULLS_FIRST);

        //then
        assertThat(builder.toString()).isEqualTo("CASE WHEN field IS NULL THEN 0 ELSE 1 END ASC, field DESC");
    }

    @Test
    public void should_sort_desc_with_nulls_last() {
        //when
        sqlServerInterceptor.appendOrderBy(builder, "field", OrderByType.DESC_NULLS_LAST);

        //then
        assertThat(builder.toString()).isEqualTo("CASE WHEN field IS NULL THEN 0 ELSE 1 END DESC, field DESC");
    }
}
