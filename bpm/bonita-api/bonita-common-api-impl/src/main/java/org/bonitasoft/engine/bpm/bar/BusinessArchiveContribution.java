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

import java.io.File;
import java.io.IOException;

/**
 * Supplies a specif information to the {@link BusinessArchive}. It's able to read/write information from/to the file system.
 * 
 * @author Baptiste Mesta
 * @see org.bonitasoft.engine.bpm.bar.BusinessArchive
 */
public interface BusinessArchiveContribution {

    /**
     * Updates the {@link BusinessArchive} with information read from the file system and returns true if the information was successfully read or false otherwise.
     * 
     * @param businessArchive the {@code BusinessArchive} to be filled with information read from the file system
     * @param barFolder the folder storing the information to be filled in the {@code BusinessArchive}
     * @return
     *         true if the information was successfully read from file system; false otherwise.
     * @throws IOException if a problem occurs when reading information from file system.
     * @throws InvalidBusinessArchiveFormatException when the folder from which the information is being read has an invalid format.
     * @see org.bonitasoft.engine.bpm.bar.BusinessArchive
     */
    boolean readFromBarFolder(BusinessArchive businessArchive, File barFolder) throws IOException, InvalidBusinessArchiveFormatException;

    /**
     * Writes the content of the BusinessArchive to the file system
     * 
     * @param businessArchive the {@code BusinessArchive} containing the information to be stored in the file system
     * @param barFolder the folder where the {@code BusinessArchive} information will be stored.
     * @throws IOException when a problem occurs when the information is being stored in the file system.
     */
    void saveToBarFolder(BusinessArchive businessArchive, File barFolder) throws IOException;

    /**
     * Checks if this {@code BusinessArchiveContribution} is mandatory
     * @return true if this {@code BusinessArchiveContribution} is mandatory; false otherwise
     */
    boolean isMandatory();

    /**
     * Retrieves the {@code BusinessArchiveContribution} name
     * @return the {@code BusinessArchiveContribution} name
     */
    String getName();

}
