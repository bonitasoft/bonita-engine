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
import java.sql.Blob;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.persistence.*;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;
import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author Haroun EL ALAMI
 */
@Service
public class TemporaryContentServiceImpl implements TemporaryContentService {

    private final Duration cleanupDelay;
    private final PersistenceService platformPersistenceService;

    public TemporaryContentServiceImpl(
            @Value("${bonita.runtime.temporary-content.cleanup.delay:PT30M}") String cleanupDelay,
            PersistenceService platformPersistenceService) {
        this.cleanupDelay = Duration.parse(cleanupDelay);
        this.platformPersistenceService = platformPersistenceService;
    }

    @Override
    public String add(String fileName, InputStream content, String mimeType)
            throws SRecorderException, IOException, SPersistenceException {
        Blob data = BlobProxy.generateProxy(content, content.available());
        STemporaryContent temporaryContent = new STemporaryContent(fileName, data, mimeType);
        platformPersistenceService.insert(temporaryContent);
        return temporaryContent.getKey();
    }

    @Override
    public void remove(STemporaryContent file) throws SRecorderException, SPersistenceException {
        platformPersistenceService.delete(file);
    }

    @Override
    public void removeAll(List<STemporaryContent> files) throws SRecorderException, SPersistenceException {
        for (STemporaryContent file : files) {
            remove(file);
        }
    }

    @Override
    public int cleanOutDatedTemporaryContent() throws SPersistenceException {
        long creationDate = System.currentTimeMillis() - cleanupDelay.toMillis();
        return platformPersistenceService.update("cleanOutDatedTemporaryResources",
                Map.of("creationDate", creationDate));
    }

    @Override
    public STemporaryContent get(String key) throws SBonitaReadException, SObjectNotFoundException {
        Map<String, Object> inputParameters = new HashMap<>();
        inputParameters.put("key", key);
        STemporaryContent sTemporaryContent = platformPersistenceService.selectOne(
                new SelectOneDescriptor<>("getTemporaryResource", inputParameters, STemporaryContent.class));
        if (sTemporaryContent == null) {
            throw new SObjectNotFoundException("No temporary resource found with key " + key);
        }
        return sTemporaryContent;
    }
}
