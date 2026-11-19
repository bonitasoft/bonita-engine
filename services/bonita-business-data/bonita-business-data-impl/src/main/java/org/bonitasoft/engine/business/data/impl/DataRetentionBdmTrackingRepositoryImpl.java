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

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.bonitasoft.engine.business.data.DataRetentionBdmTrackingRepository;
import org.bonitasoft.engine.business.data.model.SDataRetentionBdmTracking;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.services.UpdateDescriptor;
import org.springframework.stereotype.Repository;

/**
 * Hibernate-based implementation of {@link DataRetentionBdmTrackingRepository}.
 * <p>
 * Uses {@link PersistenceService} directly (not {@code Recorder}) to avoid generating
 * queriable log entries — tracking records are internal bookkeeping for the data retention
 * service, not user-auditable actions.
 * <p>
 * All operations target the Bonita DB (not the Business Data DB).
 */
@Repository
@RequiredArgsConstructor
public class DataRetentionBdmTrackingRepositoryImpl implements DataRetentionBdmTrackingRepository {

    private final PersistenceService persistenceService;

    @Override
    public void create(SDataRetentionBdmTracking tracking) throws SPersistenceException {
        persistenceService.insert(tracking);
    }

    @Override
    public void updateLastModifiedDate(SDataRetentionBdmTracking tracking) throws SPersistenceException {
        persistenceService.update(
                UpdateDescriptor.buildSetField(tracking, "lastModifiedAt", tracking.getLastModifiedAt()));
    }

    @Override
    public SDataRetentionBdmTracking getByDataIdAndClassname(long dataId, String dataClassname)
            throws SBonitaReadException {
        return persistenceService.selectOne(new SelectOneDescriptor<>(
                "getDataRetentionBdmTrackingByDataIdAndClassname",
                Map.of("dataId", dataId, "dataClassname", dataClassname),
                SDataRetentionBdmTracking.class));
    }

    @Override
    public List<SDataRetentionBdmTracking> getByClassname(String dataClassname) throws SBonitaReadException {
        return persistenceService.selectList(new SelectListDescriptor<>(
                "getDataRetentionBdmTrackingByClassname",
                Map.of("dataClassname", dataClassname),
                SDataRetentionBdmTracking.class,
                QueryOptions.ALL_RESULTS));
    }

    @Override
    public List<SDataRetentionBdmTracking> getExpiredByCreatedDate(String dataClassname, long deadline)
            throws SBonitaReadException {
        return persistenceService.selectList(new SelectListDescriptor<>(
                "getExpiredTrackingByClassnameAndCreatedAt",
                Map.of("dataClassname", dataClassname, "deadline", deadline),
                SDataRetentionBdmTracking.class,
                QueryOptions.ALL_RESULTS));
    }

    @Override
    public List<SDataRetentionBdmTracking> getExpiredByLastModifiedDate(String dataClassname, long deadline)
            throws SBonitaReadException {
        return persistenceService.selectList(new SelectListDescriptor<>(
                "getExpiredTrackingByClassnameAndLastModifiedAt",
                Map.of("dataClassname", dataClassname, "deadline", deadline),
                SDataRetentionBdmTracking.class,
                QueryOptions.ALL_RESULTS));
    }

    @Override
    public void delete(SDataRetentionBdmTracking tracking) throws SPersistenceException {
        persistenceService.delete(tracking);
    }

    @Override
    public int delete(long dataId, String dataClassname) throws SPersistenceException {
        return persistenceService.update("deleteDataRetentionBdmTrackingByDataIdAndClassname",
                Map.of("dataId", dataId, "dataClassname", dataClassname));
    }

    @Override
    public void deleteAll(List<FilterOption> filterOptions) throws SPersistenceException {
        persistenceService.deleteAll(SDataRetentionBdmTracking.class, filterOptions);
    }
}
