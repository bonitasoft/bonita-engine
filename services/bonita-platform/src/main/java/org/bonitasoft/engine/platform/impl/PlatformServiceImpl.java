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
package org.bonitasoft.engine.platform.impl;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.platform.PlatformRetriever;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.exception.*;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.SPlatformProperties;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.services.UpdateDescriptor;

/**
 * @author Charles Souillard
 * @author Celine Souchet
 */
@Slf4j
public class PlatformServiceImpl implements PlatformService {

    private final PersistenceService platformPersistenceService;
    private final SPlatformProperties sPlatformProperties;
    private final Recorder recorder;
    private final PlatformRetriever platformRetriever;

    public PlatformServiceImpl(final PersistenceService platformPersistenceService, PlatformRetriever platformRetriever,
            final Recorder recorder, final SPlatformProperties sPlatformProperties) {
        this.platformPersistenceService = platformPersistenceService;
        this.sPlatformProperties = sPlatformProperties;
        this.recorder = recorder;
        this.platformRetriever = platformRetriever;
    }

    @Override
    public SPlatform getPlatform() throws SPlatformNotFoundException {
        try {
            return platformRetriever.getPlatform();
        } catch (SPlatformNotFoundException e) {
            throw e;
        } catch (final Exception e) {
            throw new SPlatformNotFoundException("Unable to check if a platform already exists : " + e.getMessage(), e);
        }
    }

    // FIXME: Not necessary anymore, as platform is always created by ScriptExecutor at startup
    @Override
    public boolean isPlatformCreated() {
        try {
            getPlatform();
            return true;
        } catch (final SPlatformNotFoundException e) {
            return false;
        }
    }

    @Override
    public void activateServices() throws SPlatformNotFoundException, SPlatformUpdateException {
        final SPlatform platform = getPlatform();
        final UpdateDescriptor desc = new UpdateDescriptor(platform);
        desc.addField(SPlatform.STATUS, SPlatform.ACTIVATED);
        try {
            platformPersistenceService.update(desc);
        } catch (final SPersistenceException e) {
            throw new SPlatformUpdateException("Problem while activating services", e);
        }
    }

    @Override
    public void deactivateServices() throws SPlatformNotFoundException, SPlatformUpdateException {
        final SPlatform platform = getPlatform();
        final UpdateDescriptor desc = new UpdateDescriptor(platform);
        desc.addField(SPlatform.STATUS, SPlatform.DEACTIVATED);
        try {
            platformPersistenceService.update(desc);
        } catch (final SPersistenceException e) {
            throw new SPlatformUpdateException("Problem while deactivating services", e);
        }
    }

    @Override
    public void pauseServices() throws SPlatformNotFoundException, SPlatformUpdateException {
        final SPlatform platform = getPlatform();
        final UpdateDescriptor desc = new UpdateDescriptor(platform);
        desc.addField(SPlatform.STATUS, SPlatform.PAUSED);
        try {
            platformPersistenceService.update(desc);
        } catch (final SPersistenceException e) {
            throw new SPlatformUpdateException("Unable to update platform status in database.", e);
        }
    }

    @Override
    public SPlatformProperties getSPlatformProperties() {
        return sPlatformProperties;
    }

    @Override
    public void updatePlatform(final EntityUpdateDescriptor descriptor) throws SPlatformUpdateException {
        try {
            recorder.recordUpdate(UpdateRecord.buildSetFields(getPlatform(), descriptor), PLATFORM);
        } catch (final SRecorderException | SPlatformNotFoundException e) {
            throw new SPlatformUpdateException("Problem while updating platform: ", e);
        }
    }

}
