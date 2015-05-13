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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
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
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProfilesUpdaterRestartHandlerTest {

    @Mock
    public PlatformServiceAccessor platformServiceAccessor;
    @Mock
    public TenantServiceAccessor tenantServiceAccessor;
    @Mock
    public TransactionService transactionService;
    @Mock
    public ProfileService profileService;
    @Mock
    public Parser profileParser;
    @Spy
    public ProfilesUpdaterRestartHandler profilesUpdaterRestartHandler;
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private File md5File;
    @Mock
    public TechnicalLoggerService technicalLoggerService;
    @Mock
    public ProfilesImporter profilesImporter;

    @Before
    public void before() throws IOException, BonitaHomeNotSetException {
        doReturn(12l).when(tenantServiceAccessor).getTenantId();
        doReturn(transactionService).when(platformServiceAccessor).getTransactionService();
        doReturn(profileService).when(tenantServiceAccessor).getProfileService();
        doReturn(technicalLoggerService).when(tenantServiceAccessor).getTechnicalLoggerService();
        doReturn(profileParser).when(tenantServiceAccessor).getProfileParser();
        md5File = temporaryFolder.newFile();
        doReturn(md5File).when(profilesUpdaterRestartHandler).getMD5File(anyLong());
        doReturn(profilesImporter).when(profilesUpdaterRestartHandler).createProfilesImporter(any(TenantServiceAccessor.class), anyListOf(ExportedProfile.class));
    }

    @Test
    public void should_not_call_update_when_md5_is_ok() throws Exception {
        //given
        doReturn("the content").when(profilesUpdaterRestartHandler).getXMLContent(anyString());
        IOUtil.writeMD5(md5File, "the content".getBytes());
        //when
        profilesUpdaterRestartHandler.afterServicesStart(platformServiceAccessor, tenantServiceAccessor);

        //verify
        verify(transactionService, times(0)).executeInTransaction(any(Callable.class));
        verify(technicalLoggerService).log(ProfilesUpdaterRestartHandler.class, TechnicalLogSeverity.INFO, "Default profiles are up to date");
    }

    @Test
    public void should_call_update_when_md5_is_not_ok() throws Exception {
        //given
        doReturn("the content").when(profilesUpdaterRestartHandler).getXMLContent(anyString());
        IOUtil.writeMD5(md5File, "the old content".getBytes());
        //when
        profilesUpdaterRestartHandler.afterServicesStart(platformServiceAccessor, tenantServiceAccessor);

        //verify
        verify(transactionService, times(1)).executeInTransaction(any(Callable.class));
        verify(technicalLoggerService).log(ProfilesUpdaterRestartHandler.class, TechnicalLogSeverity.INFO, "Default profiles not up to date, updating them...");
    }

    @Test
    public void should_call_update_when_md5_does_not_exists() throws Exception {
        //given
        doReturn("the content").when(profilesUpdaterRestartHandler).getXMLContent(anyString());
        md5File.delete();
        //when
        profilesUpdaterRestartHandler.afterServicesStart(platformServiceAccessor, tenantServiceAccessor);

        //verify
        verify(transactionService, times(1)).executeInTransaction(any(Callable.class));
        verify(technicalLoggerService).log(ProfilesUpdaterRestartHandler.class, TechnicalLogSeverity.INFO, "Default profiles not up to date, updating them...");
    }

    @Test
    public void callable_call_do_update() throws Exception {
        //given
        ArrayList<ExportedProfile> exportedProfiles = new ArrayList<ExportedProfile>();
        Callable<Object> content = profilesUpdaterRestartHandler.getUpdateProfilesCallable(tenantServiceAccessor, md5File, "content", exportedProfiles);
        doReturn(null).when(profilesUpdaterRestartHandler).doUpdateProfiles(any(TenantServiceAccessor.class), anyListOf(ExportedProfile.class), any(File.class),
                anyString());
        //when
        content.call();
        //then
        verify(profilesUpdaterRestartHandler, times(1)).doUpdateProfiles(tenantServiceAccessor, exportedProfiles, md5File, "content");

    }

    @Test
    public void doUpdate_call_importer() throws IOException, NoSuchAlgorithmException, ExecutionException {
        //when
        ArrayList<ExportedProfile> profilesFromXML = new ArrayList<ExportedProfile>();
        profilesUpdaterRestartHandler.doUpdateProfiles(tenantServiceAccessor, profilesFromXML, md5File, "content of profiles");
        //then
        verify(profilesUpdaterRestartHandler).createProfilesImporter(tenantServiceAccessor, profilesFromXML);
        verify(profilesImporter).importProfiles(-1);
        assertThat(md5File).hasContent(IOUtil.md5("content of profiles".getBytes()));
    }

    @Test
    public void doUpdate_do_not_write_md5_if_not_ok() throws IOException, NoSuchAlgorithmException, ExecutionException {
        //given
        IOUtil.writeFile(md5File,"oldHash");
        doThrow(new ExecutionException("")).when(profilesImporter).importProfiles(anyLong());
        //when
        ArrayList<ExportedProfile> profilesFromXML = new ArrayList<ExportedProfile>();
        profilesUpdaterRestartHandler.doUpdateProfiles(tenantServiceAccessor, profilesFromXML, md5File, "content of profiles");
        //then
        assertThat(md5File).hasContent("oldHash");
    }
}
