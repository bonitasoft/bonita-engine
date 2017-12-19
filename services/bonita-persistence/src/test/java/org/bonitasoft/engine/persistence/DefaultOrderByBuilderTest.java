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
public class DefaultOrderByBuilderTest {

    StringBuilder builder;

    DefaultOrderByBuilder defaultOrderByBuilder;

    @Before
    public void before() {
        //given
        builder = new StringBuilder();
        defaultOrderByBuilder = new DefaultOrderByBuilder();
    }

    @Test
    public void should_sort_asc() {
        //when
        defaultOrderByBuilder.appendOrderBy(builder, "field", OrderByType.ASC);

        //then
        assertThat(builder.toString()).isEqualTo("field ASC");
    }

    @Test
    public void should_sort_desc() {
        //when
        defaultOrderByBuilder.appendOrderBy(builder, "field", OrderByType.DESC);

        //then
        assertThat(builder.toString()).isEqualTo("field DESC");
    }

    @Test
    public void should_sort_asc_with_nulls_first() {
        //when
        defaultOrderByBuilder.appendOrderBy(builder, "field", OrderByType.ASC_NULLS_FIRST);

        //then
        assertThat(builder.toString()).isEqualTo("field ASC NULLS FIRST");
    }

    @Test
    public void should_sort_asc_with_nulls_last() {
        //when
        defaultOrderByBuilder.appendOrderBy(builder, "field", OrderByType.ASC_NULLS_LAST);

        //then
        assertThat(builder.toString()).isEqualTo("field ASC NULLS LAST");
    }

    @Test
    public void should_sort_desc_with_nulls_first() {
        //when
        defaultOrderByBuilder.appendOrderBy(builder, "field", OrderByType.DESC_NULLS_FIRST);

        //then
        assertThat(builder.toString()).isEqualTo("field DESC NULLS FIRST");
    }

    @Test
    public void should_sort_desc_with_nulls_last() {
        //when
        defaultOrderByBuilder.appendOrderBy(builder, "field", OrderByType.DESC_NULLS_LAST);

        //then
        assertThat(builder.toString()).isEqualTo("field DESC NULLS LAST");
    }

}
