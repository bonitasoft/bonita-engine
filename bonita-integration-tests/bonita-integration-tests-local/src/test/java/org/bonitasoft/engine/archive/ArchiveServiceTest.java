/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 */
package org.bonitasoft.engine.archive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.data.instance.model.archive.impl.SAShortTextDataInstanceImpl;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.junit.Before;
import org.junit.Test;

public class ArchiveServiceTest extends CommonBPMServicesTest {

    private static final long START_OF_2009 = 1230739500052L;

    private static final long BEFORE_2009 = 1130739200052L;

    private static final int ONE_DAY = 86400000;

    private ArchiveService archiveService;

    @Before
    public void before() {
        this.archiveService = getTenantAccessor().getArchiveService();
    }

    @Test
    public void testRecordInsert() throws Exception {
        getTransactionService().begin();

        final SAShortTextDataInstanceImpl dataInstance = insertDataWithYesterdayDate();
        assertNotNull(dataInstance);

        getTransactionService().complete();
    }

    @Test
    public void archiveInSlidingArchiveNotDone() throws Exception {

        getTransactionService().begin();
        final SAShortTextDataInstanceImpl dataInstance = insertDataWithFirstJanuary2009Date();
        getTransactionService().complete();

        getTransactionService().begin();

        final SAShortTextDataInstanceImpl dataInstanceFromArchive = selectDataByIdFromDefinitiveArchive(dataInstance);
        assertNotNull("should be in definitive archive", dataInstanceFromArchive);
        assertEquals(dataInstance.getName(), dataInstanceFromArchive.getName());
        assertEquals(dataInstance.getValue(), dataInstanceFromArchive.getValue());

        getTransactionService().complete();
    }

    @Test
    public void insertWithNoDefinitiveArchiveForThatDate() throws Exception {
        getTransactionService().begin();
        try {
            insertDataWithBefore2009Date();
        } finally {
            getTransactionService().complete();
        }
    }

    private SAShortTextDataInstanceImpl insertDataWithYesterdayDate() throws SRecorderException {
        return insertData(System.currentTimeMillis() - ONE_DAY);
    }

    private SAShortTextDataInstanceImpl insertDataWithFirstJanuary2009Date() throws SRecorderException {
        return insertData(START_OF_2009);
    }

    private SAShortTextDataInstanceImpl insertDataWithBefore2009Date() throws SRecorderException {
        return insertData(BEFORE_2009);
    }

    private SAShortTextDataInstanceImpl insertData(long before2009) throws SRecorderException {
        final SAShortTextDataInstanceImpl data = new SAShortTextDataInstanceImpl();
        data.setName("archiveTestEmployee");
        data.setValue("password");
        archiveService.recordInsert(before2009, new ArchiveInsertRecord(data));
        return data;
    }

    private SAShortTextDataInstanceImpl selectDataByIdFromDefinitiveArchive(final SAShortTextDataInstanceImpl dataInstance) throws SBonitaReadException {
        final SelectByIdDescriptor<SAShortTextDataInstanceImpl> selectByIdDescriptor1 = new SelectByIdDescriptor<>(SAShortTextDataInstanceImpl.class,
                dataInstance.getId());
        return archiveService.getDefinitiveArchiveReadPersistenceService().selectById(selectByIdDescriptor1);
    }

    @Test
    public void testRecordDelete() throws Exception {
        getTransactionService().begin();

        final SAShortTextDataInstanceImpl dataInstance = insertDataWithYesterdayDate();

        getTransactionService().complete();

        getTransactionService().begin();

        archiveService.recordDelete(new DeleteRecord(dataInstance));

        getTransactionService().complete();
    }

    @Test
    public void testGetDefinitiveArchiveReadPersistenceService() {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        assertNotNull(persistenceService);
    }

}
