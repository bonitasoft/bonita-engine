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

package org.bonitasoft.engine.page;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.transaction.STransactionCommitException;
import org.bonitasoft.engine.transaction.STransactionCreationException;
import org.bonitasoft.engine.transaction.STransactionRollbackException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Baptiste Mesta
 */
public class PageMappingServiceIT extends CommonBPMServicesTest {

    public static final long PAGE_ID = 12345L;
    public static final long NEW_PAGE_ID = 88854L;
    private final TransactionService transactionService;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    public PageMappingService pageMappingService;

    public PageMappingServiceIT() {
        super();
        transactionService = getTransactionService();
        pageMappingService = getTenantAccessor().getPageMappingService();
    }

    @After
    public void tearDown() throws SBonitaReadException, SObjectModificationException, STransactionCreationException,
            STransactionCommitException, STransactionRollbackException {
    }

    @Test
    public void getByKey() throws Exception {
        transactionService.begin();
        SPageMapping internal = pageMappingService.create("getByKey/process1/12.0", PAGE_ID, Collections.<String> emptyList());
        SPageMapping external = pageMappingService.create("getByKey/process2/12.0", "http://www.google.com", null, Collections.<String> emptyList());
        SPageMapping externalWithAdapter = pageMappingService.create("getByKey/process3/12.0", "http://www.google.com", "theAdapter",
                Collections.<String> emptyList());

        transactionService.complete();

        transactionService.begin();
        SPageMapping internalPersisted = pageMappingService.get("getByKey/process1/12.0");
        SPageMapping externalPersisted = pageMappingService.get("getByKey/process2/12.0");
        SPageMapping exterAdaPersisted = pageMappingService.get("getByKey/process3/12.0");

        transactionService.complete();

        assertThat(internalPersisted).isEqualTo(internal);
        assertThat(internalPersisted.getPageId()).isEqualTo(PAGE_ID);
        assertThat(internalPersisted.getUrl()).isNull();
        assertThat(externalPersisted).isEqualTo(external);
        assertThat(externalPersisted.getUrl()).isEqualTo("http://www.google.com");
        assertThat(exterAdaPersisted).isEqualTo(externalWithAdapter);
        assertThat(exterAdaPersisted.getUrl()).isEqualTo("http://www.google.com");
        assertThat(exterAdaPersisted.getUrlAdapter()).isEqualTo("theAdapter");
    }

    @Test
    public void delete() throws Exception {
        transactionService.begin();
        SPageMapping internal = pageMappingService.create("delete/process1/12.0", PAGE_ID, Collections.<String> emptyList());
        transactionService.complete();

        transactionService.begin();
        pageMappingService.delete(internal);
        transactionService.complete();

        try {
            expectedException.expect(SObjectNotFoundException.class);
            transactionService.begin();
            pageMappingService.get(internal.getKey());
        } finally {
            transactionService.complete();
        }
    }

    @Test
    public void update() throws Exception {

        String key = "theKey/process1/12.0";
        transactionService.begin();
        pageMappingService.create(key, PAGE_ID, Collections.<String> emptyList());
        transactionService.complete();

        transactionService.begin();
        SPageMapping pageMapping1 = pageMappingService.get(key);
        pageMappingService.update(pageMapping1, NEW_PAGE_ID);
        transactionService.complete();

        transactionService.begin();
        SPageMapping updated = pageMappingService.get(key);
        transactionService.complete();

        assertThat(updated).isEqualTo(pageMapping1);
        assertThat(updated.getPageId()).isEqualTo(NEW_PAGE_ID);
        assertThat(updated.getUrl()).isNull();
        assertThat(updated.getUrlAdapter()).isNull();

        Thread.sleep(10);

        transactionService.begin();
        SPageMapping reUpdated = pageMappingService.get(key);
        pageMappingService.update(reUpdated, "http://www.yahoo.com", "adapterURL");
        transactionService.complete();

        assertThat(reUpdated.getKey()).isEqualTo(key);
        assertThat(reUpdated.getPageId()).isNull();
        assertThat(reUpdated.getUrl()).isEqualTo("http://www.yahoo.com");
        assertThat(reUpdated.getUrlAdapter()).isEqualTo("adapterURL");
        assertThat(reUpdated.getLastUpdateDate()).isGreaterThan(updated.getLastUpdateDate());
        assertThat(reUpdated.getLastUpdatedBy()).isEqualTo(-1);

    }
}
