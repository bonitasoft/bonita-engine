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
package org.bonitasoft.engine.continuation;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.work.BonitaWork;

public class ListAdder extends BonitaWork {

    private static final long serialVersionUID = -2980862072974732348L;

    private final List<String> list;

    private final String toAdd;

    public ListAdder(final List<String> arrayList, final String toAdd) {
        list = arrayList;
        this.toAdd = toAdd;
    }

    @Override
    public String getDescription() {
        return getClass().getSimpleName() + ": Adding " + toAdd + " to " + list.toString();
    }

    @Override
    public void work(final Map<String, Object> context) {
        list.add(toAdd);
    }

    @Override
    public void handleFailure(final Exception e, final Map<String, Object> context) {
    }
}
