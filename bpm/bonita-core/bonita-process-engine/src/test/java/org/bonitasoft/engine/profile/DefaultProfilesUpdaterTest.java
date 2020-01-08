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
package org.bonitasoft.engine.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.profile.xml.ProfilesNode;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultProfilesUpdaterTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Mock
    public TechnicalLoggerService technicalLoggerService;
    @Mock
    public ProfilesImporter profilesImporter;
    @Mock
    public ProfilesNode defaultProfiles;
    private DefaultProfilesUpdater defaultProfilesUpdater;
    private File md5File;

    @Before
    public void before() throws IOException, BonitaHomeNotSetException, ExecutionException {
        md5File = temporaryFolder.newFile();
        defaultProfilesUpdater = spy(new DefaultProfilesUpdater(1L, technicalLoggerService, profilesImporter));
        doReturn(md5File).when(defaultProfilesUpdater).getProfilesMD5File();
        doReturn("xml content").when(defaultProfilesUpdater).getDefaultProfilesXml();
        doReturn(defaultProfiles).when(defaultProfilesUpdater).getProfilesFromXML(anyString());

        doReturn(null).when(profilesImporter).importProfiles(any(ProfilesNode.class), any(ImportPolicy.class),
                anyLong());
    }

    @Test
    public void execute_should_not_update_when_shouldUpdateProfiles_returns_false() throws Exception {
        // Given
        doReturn(false).when(defaultProfilesUpdater).shouldUpdateProfiles(any(File.class), anyString());
        // When
        boolean hasUpdated = defaultProfilesUpdater.execute();
        // Then
        assertThat(hasUpdated).isFalse();
    }

    @Test
    public void execute_should_update_when_shouldUpdateProfiles_returns_true() throws Exception {
        // Given
        doReturn(true).when(defaultProfilesUpdater).shouldUpdateProfiles(any(File.class), anyString());
        doReturn(null).when(defaultProfilesUpdater).doUpdateProfiles(any(ProfilesNode.class), any(File.class),
                anyString());
        // When
        boolean hasUpdated = defaultProfilesUpdater.execute();
        // Then
        assertThat(hasUpdated).isTrue();
    }

    @Test
    public void execute_call_doUpdateProfiles() throws Exception {
        // Given
        doReturn(true).when(defaultProfilesUpdater).shouldUpdateProfiles(any(File.class), anyString());
        doReturn(null).when(defaultProfilesUpdater).doUpdateProfiles(any(ProfilesNode.class), any(File.class),
                anyString());
        // When
        defaultProfilesUpdater.execute();
        // Then
        verify(defaultProfilesUpdater).doUpdateProfiles(any(ProfilesNode.class), any(File.class), anyString());
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
    public void doUpdateProfiles_should_not_write_MD5_if_import_fails()
            throws IOException, ExecutionException, NoSuchAlgorithmException {
        // Given
        IOUtil.writeFile(md5File, "oldHash");
        doThrow(new ExecutionException("")).when(profilesImporter).importProfiles(any(ProfilesNode.class),
                any(ImportPolicy.class), anyLong());
        // When
        defaultProfilesUpdater.doUpdateProfiles(defaultProfiles, md5File, "content of profiles");
        // Then
        assertThat(md5File).hasContent("oldHash");
    }

    @Test
    public void doUpdate_call_importer() throws IOException, NoSuchAlgorithmException, ExecutionException {
        // Given
        ProfilesNode profilesFromXML = new ProfilesNode();
        // When
        defaultProfilesUpdater.doUpdateProfiles(profilesFromXML, md5File, "content of profiles");
        // Then
        verify(profilesImporter).importProfiles(any(ProfilesNode.class), eq(ImportPolicy.UPDATE_DEFAULTS), eq(-1L));
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
