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

package org.bonitasoft.engine.resources;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;

/**
 * @author Baptiste Mesta
 */
public class ProcessResourcesServiceImpl implements ProcessResourcesService {

    private final Recorder recorder;
    private final ReadPersistenceService persistenceService;

    public ProcessResourcesServiceImpl(Recorder recorder, ReadPersistenceService persistenceService) {
        this.recorder = recorder;
        this.persistenceService = persistenceService;
    }

    @Override
    public void add(long processDefinitionId, String name, BARResourceType type, byte[] content) throws SRecorderException {
        SBARResource resource = new SBARResource(name, type, processDefinitionId, content);
        recorder.recordInsert(new InsertRecord(resource), BAR_RESOURCE);
    }

    @Override
    public void update(SBARResourceLight resource, byte[] content) throws SRecorderException {
        recorder.recordUpdate(UpdateRecord.buildSetFields(resource, Collections.singletonMap("content", content)), BAR_RESOURCE);
    }

    @Override
    public void removeAll(long processDefinitionId, BARResourceType type) throws SBonitaReadException, SRecorderException {
        List<SBARResourceLight> resources;
        while (!(resources = getLight(processDefinitionId, type, 0, 100)).isEmpty()) {
            for (SBARResourceLight resource : resources) {
                remove(resource);
            }
        }
    }

    public List<SBARResourceLight> getLight(long processDefinitionId, BARResourceType type, int from, int numberOfElements) throws SBonitaReadException {
        Map<String, Object> inputParameters = new HashMap<>(2);
        inputParameters.put("processDefinitionId", processDefinitionId);
        inputParameters.put("type", type);
        return persistenceService.selectList(new SelectListDescriptor<SBARResourceLight>("getBARResourcesLightOfType", inputParameters, SBARResourceLight.class,
                new QueryOptions(from, numberOfElements)));
    }

    @Override
    public List<SBARResource> get(long processDefinitionId, BARResourceType type, int from, int numberOfElements) throws SBonitaReadException {
        Map<String, Object> inputParameters = new HashMap<>(2);
        inputParameters.put("processDefinitionId", processDefinitionId);
        inputParameters.put("type", type);
        return persistenceService.selectList(
                new SelectListDescriptor<SBARResource>("getBARResourcesOfType", inputParameters, SBARResource.class, new QueryOptions(from, numberOfElements)));
    }

    @Override
    public long count(long processDefinitionId, BARResourceType type) throws SBonitaReadException {
        Map<String, Object> inputParameters = new HashMap<>(2);
        inputParameters.put("processDefinitionId", processDefinitionId);
        inputParameters.put("type", type);
        return persistenceService.selectOne(new SelectOneDescriptor<Long>("getNumberOfBARResourcesOfType", inputParameters, SBARResource.class));
    }

    @Override
    public SBARResource get(long processDefinitionId, BARResourceType type, String name) throws SBonitaReadException {
        Map<String, Object> inputParameters = new HashMap<>(2);
        inputParameters.put("processDefinitionId", processDefinitionId);
        inputParameters.put("type", type);
        inputParameters.put("name", name);
        return persistenceService.selectOne(new SelectOneDescriptor<SBARResource>("getBARResource", inputParameters, SBARResource.class));
    }

    @Override
    public void remove(SBARResourceLight resource) throws SRecorderException {
        recorder.recordDelete(new DeleteRecord(resource), BAR_RESOURCE);
    }

}
