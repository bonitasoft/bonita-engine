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

package org.bonitasoft.engine.core.operation.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class LeftOperandIndexesTest {

    @Test
    public void default_constructor_should_create_index_with_negative_values() throws Exception {
        //when
        LeftOperandIndexes indexes = new LeftOperandIndexes();

        //then
        assertThat(indexes.getLastIndex()).isEqualTo(-1);
        assertThat(indexes.getNextIndex()).isEqualTo(-1);
    }

    @Test
    public void setLastIndex_should_modify_lastIndex() throws Exception {
        //given
        LeftOperandIndexes indexes = new LeftOperandIndexes();

        //when
        indexes.setLastIndex(4);

        //then
        assertThat(indexes.getLastIndex()).isEqualTo(4);
        assertThat(indexes.getNextIndex()).isEqualTo(-1);

    }

    @Test
    public void setNextIndex_should_modify_nextIndex() throws Exception {
        //given
        LeftOperandIndexes indexes = new LeftOperandIndexes();

        //when
        indexes.setNextIndex(4);

        //then
        assertThat(indexes.getLastIndex()).isEqualTo(-1);
        assertThat(indexes.getNextIndex()).isEqualTo(4);

    }

}
