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
package org.bonitasoft.engine.identity;

import java.util.Optional;

import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.services.PersistenceService;
import org.springframework.stereotype.Service;

@Service
public class IconServiceImpl implements IconService {

    private final Recorder recorder;
    private final PersistenceService persistenceService;

    public IconServiceImpl(Recorder recorder, PersistenceService persistenceService) {
        this.recorder = recorder;
        this.persistenceService = persistenceService;
    }

    @Override
    public Optional<Long> replaceIcon(String iconFilename, byte[] iconContent, Long iconIdToReplace)
            throws SBonitaReadException, SRecorderException {
        deleteIcon(iconIdToReplace);
        if (iconContent != null) {
            return Optional.of(createIcon(iconFilename, iconContent).getId());
        }
        return Optional.empty();
    }

    @Override
    public SIcon createIcon(String iconFilename, byte[] iconContent) throws SRecorderException {
        if (iconFilename == null || iconFilename.isBlank() || iconContent == null || iconContent.length == 0) {
            throw new IllegalArgumentException("Unable to create an icon without filename or content");
        }
        SIcon entity = new SIcon(IOUtil.getContentTypeForIcon(iconFilename), iconContent);
        recorder.recordInsert(new InsertRecord(entity), EVENT_NAME);
        return entity;
    }

    @Override
    public SIcon getIcon(Long iconId) throws SBonitaReadException {
        if (iconId == null) {
            return null;
        }
        return persistenceService.selectById(new SelectByIdDescriptor<>(SIcon.class, iconId));
    }

    @Override
    public void deleteIcon(Long iconId) throws SBonitaReadException, SRecorderException {
        if (iconId == null) {
            return;
        }

        SIcon icon = getIcon(iconId);
        if (icon == null) {
            return;
        }
        recorder.recordDelete(new DeleteRecord(icon), EVENT_NAME);
    }
}
