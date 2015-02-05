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
package org.bonitasoft.engine.xml;

import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;

/**
 * @author Matthieu Chaffotte
 */
public abstract class ElementBinding {

    private Stack<ElementBinding> binders;

    public final void setBinders(final Stack<ElementBinding> binders) {
        this.binders = binders;
    }

    @SuppressWarnings("unchecked")
    public <T> T findObject(final Class<T> clazz) {
        if (binders != null && !binders.isEmpty()) {
            final ListIterator<ElementBinding> listIter = binders.listIterator(binders.size());
            while (listIter.hasPrevious()) {
                final Object object = listIter.previous().getObject();
                if (object != null && clazz.isAssignableFrom(object.getClass())) {
                    return (T) object;
                }
            }
            return null;
        }
        return null;
    }

    public abstract void setAttributes(final Map<String, String> attributes) throws SXMLParseException;

    public abstract void setChildElement(final String name, final String value, final Map<String, String> attributes) throws SXMLParseException;

    public abstract void setChildObject(final String name, final Object value) throws SXMLParseException;

    public abstract Object getObject();

    public abstract String getElementTag();

}
