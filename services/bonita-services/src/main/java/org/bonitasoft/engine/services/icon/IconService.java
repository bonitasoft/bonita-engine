/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.engine.services.icon;

import java.util.Optional;

import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.SRecorderException;

public interface IconService {

    String EVENT_NAME = "ICON";

    Optional<Long> replaceIcon(String iconFilename, byte[] iconContent, Long iconIdToReplace)
            throws SBonitaReadException, SRecorderException;

    SIcon createIcon(String iconFilename, byte[] iconContent) throws SRecorderException;

    SIcon getIcon(Long id) throws SBonitaReadException;

    void deleteIcon(Long iconId) throws SBonitaReadException, SRecorderException;
}
