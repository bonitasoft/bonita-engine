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

import java.util.Comparator;
import java.util.List;

import org.bonitasoft.engine.data.instance.api.DataContainer;
import org.bonitasoft.engine.data.instance.model.archive.SADataInstance;

/**
 * The purpose of the comparator is to order data in a list starting with the most recent one in the 'closer' container
 * <p>
 * a container is closer than an other one if it is defined before in the container hierarchy.
 * <p>
 * we first order on containers then in a same containers 2 data are ordered using their archive dates.
 * <p>
 * if sorted using this comparator, a list will have as its first element the most recent data in the highest container.
 * This is useful in order to get the last archive version of a data in a current context.
 *
 * @author Baptiste Mesta
 */
class ArchivedDataInContainersComparator implements Comparator<SADataInstance> {
    private final List<DataContainer> containerHierarchy;

    ArchivedDataInContainersComparator(List<DataContainer> containerHierarchy) {
        this.containerHierarchy = containerHierarchy;
    }

    @Override
    public int compare(SADataInstance data1, SADataInstance data2) {
        final DataContainer data1Container = new DataContainer(data1.getContainerId(), data1.getContainerType());
        final DataContainer data2Container = new DataContainer(data2.getContainerId(), data2.getContainerType());
        if (areInTheSameContainer(data1Container, data2Container)) {
            return compareUsingArchiveDateInverted(data1, data2);
        }
        return compareUsingContainersOrder(data1Container, data2Container);
    }

    private int compareUsingContainersOrder(DataContainer o1Container, DataContainer o2Container) {
        return Integer.compare(containerHierarchy.indexOf(o1Container), containerHierarchy.indexOf(o2Container));
    }

    private int compareUsingArchiveDateInverted(SADataInstance o1, SADataInstance o2) {
        return Long.compare(o2.getArchiveDate(), o1.getArchiveDate());
    }

    private boolean areInTheSameContainer(DataContainer o1Container, DataContainer o2Container) {
        return containerHierarchy.indexOf(o1Container) == containerHierarchy.indexOf(o2Container);
    }
}
