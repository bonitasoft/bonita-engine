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
package org.bonitasoft.engine.theme;

import java.util.Date;

import org.bonitasoft.engine.bpm.BaseElement;

/**
 * @author Celine Souchet
 */
public interface Theme extends BaseElement {

    /**
     * @return The zip file associated with this theme, as a binary content.
     */
    byte[] getContent();

    /**
     * @return The CSS file associated with this theme, as a binary content.
     */
    byte[] getCssContent();

    /**
     * This theme is default, or not.
     * 
     * @return True if this theme is default, false otherwise.
     * @since 6.2
     */
    boolean isDefault();

    /**
     * Get the date when this theme was last modified.
     * 
     * @return The date when this theme was last modified.
     * @since 6.2
     */
    Date getLastUpdatedDate();

    /**
     * Get the type of this theme
     * 
     * @return The type of this theme
     * @since 6.2
     */
    ThemeType getType();

}
