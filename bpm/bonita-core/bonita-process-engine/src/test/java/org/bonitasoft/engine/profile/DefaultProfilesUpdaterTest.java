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
 **/
package org.bonitasoft.engine.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.profile.impl.ExportedProfile;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.xml.Parser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultProfilesUpdaterTest {

    public PlatformServiceAccessor platformServiceAccessor = Mockito.mock(PlatformServiceAccessor.class);
    public TenantServiceAccessor tenantServiceAccessor = Mockito.mock(TenantServiceAccessor.class);
    @Spy
    public DefaultProfilesUpdater defaultProfilesUpdater = new DefaultProfilesUpdater(platformServiceAccessor, tenantServiceAccessor);

    @Mock
    public TransactionService transactionService;
    @Mock
    public ProfileService profileService;
    @Mock
    public Parser profileParser;
    @Mock
    public TechnicalLoggerService technicalLoggerService;
    @Mock
    public ProfilesImporter profilesImporter;
    @Mock
    public List<ExportedProfile> defaultProfiles;
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private File md5File;

    @Before
    public void before() throws IOException, BonitaHomeNotSetException, ExecutionException {
        md5File = temporaryFolder.newFile();
        doReturn(md5File).when(defaultProfilesUpdater).getProfilesMD5File();
        doReturn("xml content").when(defaultProfilesUpdater).getDefaultProfilesXml();
        doReturn(defaultProfiles).when(defaultProfilesUpdater).getProfilesFromXML(anyString());

        doReturn(transactionService).when(platformServiceAccessor).getTransactionService();
        doReturn(profileService).when(tenantServiceAccessor).getProfileService();
        doReturn(profileParser).when(tenantServiceAccessor).getProfileParser();
        doReturn(technicalLoggerService).when(tenantServiceAccessor).getTechnicalLoggerService();
        doReturn(profilesImporter).when(defaultProfilesUpdater).createProfilesImporter(anyListOf(ExportedProfile.class));
        doReturn(null).when(profilesImporter).importProfiles(anyLong());
    }

    @Test
    public void execute_should_not_update_when_shouldUpdateProfiles_returns_false() throws Exception {
        // Given
        doReturn(false).when(defaultProfilesUpdater).shouldUpdateProfiles(any(File.class), anyString());
        // When
        boolean hasUpdated = defaultProfilesUpdater.execute(false);
        // Then
        assertThat(hasUpdated).isFalse();
    }

    @Test
    public void execute_should_update_when_shouldUpdateProfiles_returns_true() throws Exception {
        // Given
        doReturn(true).when(defaultProfilesUpdater).shouldUpdateProfiles(any(File.class), anyString());
        doReturn(null).when(defaultProfilesUpdater).doUpdateProfiles(any(List.class), any(File.class), anyString());
        // When
        boolean hasUpdated = defaultProfilesUpdater.execute(false);
        // Then
        assertThat(hasUpdated).isTrue();
    }

    @Test
    public void execute_should_create_transaction_when_called_with_shouldCreateTransaction_set_to_true() throws Exception {
        // Given
        doReturn(true).when(defaultProfilesUpdater).shouldUpdateProfiles(any(File.class), anyString());
        Callable<Object> callable = Mockito.mock(Callable.class);
        doReturn(callable).when(defaultProfilesUpdater).getUpdateProfilesCallable(any(File.class), anyString(), any(List.class));
        doReturn(null).when(transactionService).executeInTransaction(callable);
        // When
        defaultProfilesUpdater.execute(true);
        // Then
        verify(transactionService).executeInTransaction(callable);
        verify(defaultProfilesUpdater, never()).doUpdateProfiles(any(List.class), any(File.class), anyString());
    }

    @Test
    public void execute_should_not_create_transaction_when_called_with_shouldCreateTransaction_set_to_false() throws Exception {
        // Given
        doReturn(true).when(defaultProfilesUpdater).shouldUpdateProfiles(any(File.class), anyString());
        doReturn(null).when(defaultProfilesUpdater).doUpdateProfiles(any(List.class), any(File.class), anyString());
        // When
        defaultProfilesUpdater.execute(false);
        // Then
        verify(transactionService, never()).executeInTransaction(any(Callable.class));
        verify(defaultProfilesUpdater).doUpdateProfiles(any(List.class), any(File.class), anyString());
    }

    @Test
    public void getUpdateProfilesCallable_callable_should_call_do_update() throws Exception {
        // Given
        String defaultProfilesXml = "the XML";
        // When
        defaultProfilesUpdater.getUpdateProfilesCallable(md5File, defaultProfilesXml, defaultProfiles).call();
        // Then
        verify(defaultProfilesUpdater).doUpdateProfiles(defaultProfiles, md5File, defaultProfilesXml);
    }

    @Test
    public void doUpdateProfiles_should_write_MD5_if_import_succeeds() throws IOException, NoSuchAlgorithmException {
        // Given
        String oldHash = "oldHash";
        IOUtil.writeFile(md5File, oldHash);
        // When
        defaultProfilesUpdater.doUpdateProfiles(defaultProfiles, md5File, "content of profiles");
        // Then
        assertThat(contentOf(md5File)).isNotEqualTo(oldHash);
    }

    @Test
    public void doUpdateProfiles_should_not_write_MD5_if_import_fails() throws IOException, ExecutionException, NoSuchAlgorithmException {
        // Given
        IOUtil.writeFile(md5File,"oldHash");
        doThrow(new ExecutionException("")).when(profilesImporter).importProfiles(anyLong());
        // When
        defaultProfilesUpdater.doUpdateProfiles(defaultProfiles, md5File, "content of profiles");
        // Then
        assertThat(md5File).hasContent("oldHash");
    }

    @Test
    public void doUpdate_call_importer() throws IOException, NoSuchAlgorithmException, ExecutionException {
        // Given
        ArrayList<ExportedProfile> profilesFromXML = new ArrayList<ExportedProfile>();
        // When
        defaultProfilesUpdater.doUpdateProfiles(profilesFromXML, md5File, "content of profiles");
        // Then
        verify(defaultProfilesUpdater).createProfilesImporter(profilesFromXML);
        verify(profilesImporter).importProfiles(-1);
        assertThat(md5File).hasContent(IOUtil.md5("content of profiles".getBytes()));
    }

    @Test
    public void shouldUpdateProfiles_should_return_false_when_md5_matches() throws Exception {
         // Given
        String defaultProfilesXml = "the content";
        IOUtil.writeMD5(md5File, defaultProfilesXml.getBytes());
        // When
        boolean shouldUpdateProfiles = defaultProfilesUpdater.shouldUpdateProfiles(md5File, defaultProfilesXml);
        // Then
        assertThat(shouldUpdateProfiles).isFalse();
    }

    @Test
    public void shouldUpdateProfiles_should_return_true_when_md5_is_missing() throws Exception {
        // Given
        String defaultProfilesXml = "the content";
        md5File.delete();
        // When
        boolean shouldUpdateProfiles = defaultProfilesUpdater.shouldUpdateProfiles(md5File, defaultProfilesXml);
        // Then
        assertThat(shouldUpdateProfiles).isTrue();
    }

    @Test
    public void shouldUpdateProfiles_should_return_true_when_md5_differs() throws Exception {
        // Given
        String defaultProfilesXml = "the content";
        IOUtil.writeMD5(md5File, "other content".getBytes());
        // When
        boolean shouldUpdateProfiles = defaultProfilesUpdater.shouldUpdateProfiles(md5File, defaultProfilesXml);
        // Then
        assertThat(shouldUpdateProfiles).isTrue();
    }
}
