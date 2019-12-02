/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.core.filter.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * @author Danila Mazour
 */

public class FilterResultImplTest {

    @Test
    public void filterResult_should_not_return_duplicates() {
        //given
        List<Long> listWithDuplicates = Arrays.asList(15L, 15L, 15L, 28L, 32L, 28L, 39L);
        FilterResultImpl filterResult = new FilterResultImpl(listWithDuplicates, true);

        //when
        List<Long> result = filterResult.getResult();

        //then
        assertThat(result).containsExactly(15L, 28L, 32L, 39L);
    }

}
