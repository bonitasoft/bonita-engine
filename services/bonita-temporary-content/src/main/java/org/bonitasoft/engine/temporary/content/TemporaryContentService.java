/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.engine.temporary.content;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.services.SPersistenceException;

/**
 * @author Haroun EL ALAMI
 */
public interface TemporaryContentService {

    String add(String name, InputStream content, String mimeType)
            throws SRecorderException, IOException, SPersistenceException;

    void remove(STemporaryContent file) throws SRecorderException, SPersistenceException;

    void removeAll(List<STemporaryContent> files) throws SRecorderException, SPersistenceException;

    STemporaryContent get(String key) throws SBonitaReadException, SObjectNotFoundException;
}
