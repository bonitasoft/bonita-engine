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
import org.bonitasoft.engine.data.instance.model.SDataInstance;

/**
 * The purpose of the comparator is to order data in a list starting with the one in the 'closer' container
 * <p>
 * a container is closer than an other one if it is defined before in the container hierarchy.
 * <p>
 * if sorted using this comparator, a list will have as its first element the data in the closer container.
 * This is useful in order to get the data in a current context with scope shadowing.
 *
 * @author Baptiste Mesta
 */
class DataInContainersComparator implements Comparator<SDataInstance> {

    private final List<DataContainer> containerHierarchy;

    public DataInContainersComparator(List<DataContainer> containerHierarchy) {
        this.containerHierarchy = containerHierarchy;
    }

    @Override
    public int compare(SDataInstance o1, SDataInstance o2) {
        final DataContainer o1Container = new DataContainer(o1.getContainerId(), o1.getContainerType());
        final DataContainer o2Container = new DataContainer(o2.getContainerId(), o2.getContainerType());
        return containerHierarchy.indexOf(o1Container) - containerHierarchy.indexOf(o2Container);
    }
}
