/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.engine.business.data.impl;

import java.util.Collections;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.business.data.DataRetentionBdmTrackingRepository;
import org.bonitasoft.engine.business.data.DataRetentionBdmTrackingService;
import org.bonitasoft.engine.business.data.SDataRetentionBdmTrackingException;
import org.bonitasoft.engine.business.data.model.SDataRetentionBdmTracking;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.services.SPersistenceException;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link DataRetentionBdmTrackingService} that delegates to
 * {@link DataRetentionBdmTrackingRepository} and wraps persistence-layer exceptions
 * into {@link SDataRetentionBdmTrackingException}.
 */
@Service("dataRetentionBdmTrackingService")
@Slf4j
@RequiredArgsConstructor
public class DataRetentionBdmTrackingServiceImpl implements DataRetentionBdmTrackingService {

    private final DataRetentionBdmTrackingRepository bdmTrackingRepository;

    @Override
    public void create(long dataId, String dataClassname) throws SDataRetentionBdmTrackingException {
        try {
            long now = System.currentTimeMillis();
            bdmTrackingRepository.create(
                    SDataRetentionBdmTracking.builder()
                            .dataId(dataId)
                            .dataClassname(dataClassname)
                            .createdAt(now)
                            .lastModifiedAt(now)
                            .build());
        } catch (SPersistenceException e) {
            throw new SDataRetentionBdmTrackingException(
                    "Failed to create BDM tracking record with data id " + dataId + " and classname " + dataClassname,
                    e);
        }
    }

    @Override
    public void upsert(long dataId, String dataClassname) throws SDataRetentionBdmTrackingException {
        try {
            SDataRetentionBdmTracking tracking = bdmTrackingRepository.getByDataIdAndClassname(dataId, dataClassname);
            if (tracking != null) {
                tracking.setLastModifiedAt(System.currentTimeMillis());
                bdmTrackingRepository.updateLastModifiedDate(tracking);
            } else {
                log.debug("No tracking record found for {}#{}, creating one", dataClassname, dataId);
                create(dataId, dataClassname);
            }
        } catch (SBonitaReadException | SPersistenceException e) {
            throw new SDataRetentionBdmTrackingException(
                    "Failed to upsert data retention tracking record for " + dataClassname + "#" + dataId, e);
        }
    }

    @Override
    public void updateLastModifiedDate(long id) throws SDataRetentionBdmTrackingException {
        try {
            bdmTrackingRepository.updateLastModifiedDate(
                    SDataRetentionBdmTracking.builder()
                            .id(id)
                            .lastModifiedAt(System.currentTimeMillis())
                            .build());
        } catch (SPersistenceException e) {
            throw new SDataRetentionBdmTrackingException("Failed to update BDM tracking record with id " + id, e);
        }
    }

    @Override
    public void delete(long dataId, String dataClassname) throws SDataRetentionBdmTrackingException {
        try {
            int rowsDeleted = bdmTrackingRepository.delete(dataId, dataClassname);
            if (rowsDeleted == 0) {
                log.debug("No tracking record found for {}#{}, nothing to delete", dataClassname, dataId);
            }
        } catch (SPersistenceException e) {
            throw new SDataRetentionBdmTrackingException(
                    "Failed to delete data retention tracking record for " + dataClassname + "#" + dataId, e);
        }
    }

    @Override
    public void deleteAll() throws SDataRetentionBdmTrackingException {
        try {
            bdmTrackingRepository.deleteAll(Collections.emptyList());
        } catch (SPersistenceException e) {
            throw new SDataRetentionBdmTrackingException("Failed to delete all BDM tracking records", e);
        }
    }

}
