/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.resources;

import static org.bonitasoft.engine.resources.STenantResourceState.INSTALLED;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;

/**
 * @author Baptiste Mesta
 */
@Slf4j
public class TenantResourcesServiceImpl implements TenantResourcesService {

    private final Recorder recorder;
    private final ReadPersistenceService persistenceService;

    public TenantResourcesServiceImpl(Recorder recorder, ReadPersistenceService persistenceService) {
        this.recorder = recorder;
        this.persistenceService = persistenceService;
    }

    @Override
    public void add(String name, TenantResourceType type, byte[] content, long userId) throws SRecorderException {
        if (content != null && content.length > 0) {
            STenantResource resource = new STenantResource(name, type, content, userId, Instant.now().toEpochMilli(),
                    INSTALLED);
            recorder.recordInsert(new InsertRecord(resource), TENANT_RESOURCE);
        } else {
            log.warn(
                    "Tenant resource file contains an empty file {} that will be ignored. Check that this is not a mistake.",
                    name);
        }
    }

    @Override
    public void removeAll(TenantResourceType type) throws SBonitaReadException, SRecorderException {
        List<STenantResourceLight> resources;
        while (!(resources = getLight(type, 0, 100)).isEmpty()) {
            for (STenantResourceLight resource : resources) {
                remove(resource);
            }
        }
    }

    public List<STenantResourceLight> getLight(TenantResourceType type, int from, int numberOfElements)
            throws SBonitaReadException {
        Map<String, Object> inputParameters = new HashMap<>();
        inputParameters.put("type", type);
        return persistenceService
                .selectList(new SelectListDescriptor<>("getTenantResourcesLightOfType",
                        inputParameters, STenantResourceLight.class,
                        new QueryOptions(from, numberOfElements)));
    }

    @Override
    public List<STenantResource> get(TenantResourceType type, int from, int numberOfElements)
            throws SBonitaReadException {
        Map<String, Object> inputParameters = new HashMap<>();
        inputParameters.put("type", type);
        return persistenceService.selectList(
                new SelectListDescriptor<>("getTenantResourcesOfType", inputParameters,
                        STenantResource.class,
                        new QueryOptions(from, numberOfElements)));
    }

    @Override
    public long count(TenantResourceType type) throws SBonitaReadException {
        Map<String, Object> inputParameters = new HashMap<>();
        inputParameters.put("type", type);
        return persistenceService.selectOne(new SelectOneDescriptor<>("getNumberOfTenantResourcesOfType",
                inputParameters, STenantResource.class));
    }

    @Override
    public long count(TenantResourceType type, String name) throws SBonitaReadException {
        Map<String, Object> inputParameters = new HashMap<>();
        inputParameters.put("type", type);
        inputParameters.put("name", name);
        return persistenceService.selectOne(new SelectOneDescriptor<>("getNumberOfTenantResourcesOfTypeAndName",
                inputParameters, STenantResource.class));
    }

    @Override
    public STenantResource get(TenantResourceType type, String name) throws SBonitaReadException {
        Map<String, Object> inputParameters = new HashMap<>();
        inputParameters.put("type", type);
        inputParameters.put("name", name);
        return persistenceService.selectOne(
                new SelectOneDescriptor<>("getTenantResource", inputParameters, STenantResource.class));
    }

    @Override
    public STenantResourceLight getSingleLightResource(TenantResourceType type) throws SBonitaReadException {
        Map<String, Object> inputParameters = new HashMap<>();
        inputParameters.put("type", type);
        return persistenceService.selectOne(new SelectOneDescriptor<>(
                "getTenantResourcesLightOfType", inputParameters, STenantResourceLight.class));
    }

    @Override
    public void remove(AbstractSTenantResource resource) throws SRecorderException {
        recorder.recordDelete(new DeleteRecord(resource), TENANT_RESOURCE);
    }

}
