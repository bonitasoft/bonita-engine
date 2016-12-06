/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

package org.bonitasoft.engine.data.instance.api.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bonitasoft.engine.data.instance.api.DataContainer;
import org.bonitasoft.engine.data.instance.model.archive.SADataInstance;
import org.bonitasoft.engine.data.instance.model.archive.impl.SALongTextDataInstanceImpl;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class ArchivedDataInContainersComparatorTest {

    private ArchivedDataInContainersComparator comparator = new ArchivedDataInContainersComparator(
            Arrays.asList(new DataContainer(1L, "SUBTASK"),
                    new DataContainer(2L, "TASK"),
                    new DataContainer(3L, "PROC")));
    private DateFormat formatter = new SimpleDateFormat("dd/MM/yy");

    private SADataInstance data(long containerId, String containerType, String archivedDate) throws ParseException {
        SALongTextDataInstanceImpl sLongTextDataInstance = new SALongTextDataInstanceImpl();
        sLongTextDataInstance.setId(UUID.randomUUID().getLeastSignificantBits());
        sLongTextDataInstance.setContainerId(containerId);
        sLongTextDataInstance.setContainerType(containerType);
        sLongTextDataInstance.setArchiveDate(formatter.parse(archivedDate).getTime());
        return sLongTextDataInstance;
    }

    @Test
    public void should_compare_return_0_when_same_date_and_container() throws Exception {
        //given
        SADataInstance data1 = data(1L, "SUBTASK", "27/05/1987");
        SADataInstance data2 = data(1L, "SUBTASK", "27/05/1987");
        //then
        assertThat(comparator.compare(data1, data2)).isEqualTo(0);
        assertThat(comparator.compare(data2, data1)).isEqualTo(0);
    }

    @Test
    public void should_sort_in_same_container_return_the_most_recent_first() throws Exception {
        //given
        SADataInstance data1 = data(1L, "SUBTASK", "27/05/1987");
        SADataInstance data2 = data(1L, "SUBTASK", "28/05/1987");
        List<SADataInstance> list = Arrays.asList(data1, data2);
        //when
        Collections.sort(list, comparator);
        //then
        assertThat(list).containsExactly(data2, data1);
    }


    @Test
    public void should_compare_very_different_date_in_same_container() throws Exception {
        //given
        SADataInstance data1 = data(1L, "SUBTASK", "01/01/0001");
        SADataInstance data2 = data(1L, "SUBTASK", "28/05/3000");
        List<SADataInstance> list = Arrays.asList(data1, data2);
        //when
        Collections.sort(list, comparator);
        //then
        assertThat(list).containsExactly(data2, data1);
    }

    @Test
    public void should_sort_in_different_container_using_same_date_return_closer_data_first() throws Exception {
        //given
        SADataInstance data1 = data(1L, "SUBTASK", "27/05/1987");
        SADataInstance data2 = data(2L, "TASK", "27/05/1987");
        SADataInstance data3 = data(3L, "PROC", "27/05/1987");
        List<SADataInstance> list = Arrays.asList(data3, data1, data2);
        //when
        Collections.sort(list, comparator);
        //then
        assertThat(list).containsExactly(data1, data2, data3);
    }

}