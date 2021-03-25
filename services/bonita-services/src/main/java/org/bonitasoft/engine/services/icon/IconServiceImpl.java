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

import javax.activation.MimetypesFileTypeMap;

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

    private static final MimetypesFileTypeMap MIMETYPES_FILE_TYPE_MAP = new MimetypesFileTypeMap();

    static {
        //on jdk 8 there is no png by default in mime types
        MIMETYPES_FILE_TYPE_MAP.addMimeTypes("image/png\t\tpng PNG");
        MIMETYPES_FILE_TYPE_MAP.addMimeTypes("image/gif\t\tgif GIF");
        MIMETYPES_FILE_TYPE_MAP.addMimeTypes("image/jpeg\t\tjpeg jpg jpe JPG");
    }

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
        SIcon entity = new SIcon(getContentType(iconFilename), iconContent);
        recorder.recordInsert(new InsertRecord(entity), EVENT_NAME);
        return entity;
    }

    private String getContentType(String iconFilename) {
        if (iconFilename == null) {
            return "image/png";
        }
        return MIMETYPES_FILE_TYPE_MAP.getContentType(iconFilename);
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
