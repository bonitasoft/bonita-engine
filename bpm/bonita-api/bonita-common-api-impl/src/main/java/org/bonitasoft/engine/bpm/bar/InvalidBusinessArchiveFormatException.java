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
package org.bonitasoft.engine.bpm.bar;

import java.util.List;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class InvalidBusinessArchiveFormatException extends BonitaException {

    private static final long serialVersionUID = 1592038494926550438L;

    public InvalidBusinessArchiveFormatException(final Exception e) {
        super(e);
    }

    public InvalidBusinessArchiveFormatException(final String message, final Exception e) {
        super(message, e);
    }

    public InvalidBusinessArchiveFormatException(final String message) {
        super(message);
    }

    public InvalidBusinessArchiveFormatException(final List<String> messages) {
        super(constructMessage(messages));
    }

    private static String constructMessage(final List<String> messages) {
        final StringBuilder stringBuilder = new StringBuilder("Invalid business archive:\n");
        for (final String string : messages) {
            stringBuilder.append(string).append('\n');
        }
        return stringBuilder.toString();
    }

}
