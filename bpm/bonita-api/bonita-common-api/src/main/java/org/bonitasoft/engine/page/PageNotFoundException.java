/**
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
 **/
package org.bonitasoft.engine.page;

import org.bonitasoft.engine.exception.NotFoundException;

/**
 * @author Laurent Leseigneur
 */
public class PageNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 2842457668242337487L;

    public enum PageAttribute {
        NAME, MAPPING_KEY
    }

    /**
     * @deprecated Use {@link #PageNotFoundException(String, PageAttribute)} constructor instead.
     */
    @Deprecated
    public PageNotFoundException(final String name) {
        this(name, PageAttribute.NAME);
    }

    public PageNotFoundException(final String value, PageAttribute pageAttribute) {
        super(String.format("Unable to find page with %s: %s", pageAttribute.name().toLowerCase(), value));
    }

    public PageNotFoundException(final Throwable cause) {
        super(cause);
    }

}
