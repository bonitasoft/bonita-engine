/**
 * Copyright (C) 2017 Bonitasoft S.A.
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

package org.bonitasoft.engine.business.data.impl.jackson;

import com.fasterxml.jackson.databind.ext.Java7Support;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

/**
 * Do not ignore property annotated with JsonIgnore as we manage this with a links property instead.
 */
public class EntityJacksonAnnotationIntrospector extends JacksonAnnotationIntrospector {

    // taken from super class, except that we can directly set the field as we are running in a Java 7+ environment
    // NOTE: loading of Java7 dependencies is encapsulated by handlers in Java7Support,
    //  here we do not really need any handling; but for extra-safety use try-catch
    private static final Java7Support _java7Helper = Java7Support.instance();

    @Override
    protected boolean _isIgnorable(Annotated a) {
        Boolean b = _java7Helper.findTransient(a);
        if (b != null) {
            return b.booleanValue();
        }
        return false;
    }

}
