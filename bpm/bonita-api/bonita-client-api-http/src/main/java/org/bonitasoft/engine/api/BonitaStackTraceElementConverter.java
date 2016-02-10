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
package org.bonitasoft.engine.api;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.extended.StackTraceElementConverter;
import com.thoughtworks.xstream.converters.extended.StackTraceElementFactory;

/**
 * Override default stack trace element parser to avoid having big exception when there is parsing issues
 * 
 * 
 * 
 * @author Baptiste Mesta
 * 
 */
public class BonitaStackTraceElementConverter extends StackTraceElementConverter {

    private static final StackTraceElementFactory FACTORY = new StackTraceElementFactory();

    @Override
    public Object fromString(final String str) {
        try {
            return super.fromString(str);
        } catch (ConversionException e) {
            return FACTORY.element(str, " ", " ", -3);
        }
    }

}
