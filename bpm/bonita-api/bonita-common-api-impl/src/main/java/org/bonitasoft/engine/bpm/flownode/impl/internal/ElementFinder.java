/*
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
 */
package org.bonitasoft.engine.bpm.flownode.impl.internal;

import org.bonitasoft.engine.bpm.BaseElement;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

public class ElementFinder implements Serializable {

    public ElementFinder() {
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return true;
    }
    public <T extends BaseElement> T getElementById(final Collection<T> elements, final long id) {
        T element = null;
        boolean found = false;
        final Iterator<T> iterator = elements.iterator();
        while (!found && iterator.hasNext()) {
            final T next = iterator.next();
            if (next.getId() == id) {
                found = true;
                element = next;
            }
        }
        return element;
    }
}
