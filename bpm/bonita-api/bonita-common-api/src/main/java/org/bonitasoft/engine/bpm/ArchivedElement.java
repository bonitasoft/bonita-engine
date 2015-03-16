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
package org.bonitasoft.engine.bpm;

import java.util.Date;

/**
 * Interface <code>ArchivedElement</code> is the root interface of the archived client model hierarchy of Bonita BPM.
 *
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @since 6.0.0
 */
public interface ArchivedElement {

    /**
     * Gets the date when the element was archived.
     *
     * @return the archived date
     */
    Date getArchiveDate();

    /**
     * Gets the identifier of the <code>BaseElement</code> that the archived element comes from.
     *
     * @return the identifier of the source object
     */
    long getSourceObjectId();

}
