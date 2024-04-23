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

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.profile.xml.ProfilesNode;
import org.bonitasoft.engine.session.SessionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Update provided profiles from profiles.xml from classpath
 */
@Slf4j
@Component
//must be initialized before page and application import to ensure applications are mapped to profile
@Order(3)
public class DefaultProfilesUpdater implements TenantLifecycleService {

    private final ProfilesImporter profilesImporter;
    private final Long tenantId;

    public DefaultProfilesUpdater(@Value("${tenantId}") Long tenantId,
            ProfilesImporter profilesImporter) {
        this.profilesImporter = profilesImporter;
        this.tenantId = tenantId;
    }

    @Override
    public void init() throws SBonitaException {
        execute();
    }

    /**
     * Executes a default profile update
     *
     * @return whether the default profiles where updated
     * @throws Exception if execution fails
     */
    public boolean execute() {
        try {
            final File md5File = getProfilesMD5File();
            final String defaultProfilesXml = getDefaultProfilesXml();
            if (shouldUpdateProfiles(md5File, defaultProfilesXml)) {
                // Default profiles do not exist or are different
                log.info(
                        "Default profiles not up to date, updating them...");
                final ProfilesNode defaultProfiles = getProfilesFromXML(defaultProfilesXml);
                doUpdateProfiles(defaultProfiles, md5File, defaultProfilesXml);
                return true;
            } else {
                // No update required
                log.info(
                        "Default profiles are up to date");
                return false;
            }

        } catch (IOException e) {
            log.error(
                    "Unable to read the read the default profile file to update them", e);
        } catch (Exception e) {
            log.error(
                    "Unable to update default profiles", e);
        }
        return false;
    }

    Object doUpdateProfiles(final ProfilesNode defaultProfiles, final File md5File, final String defaultProfilesXml)
            throws NoSuchAlgorithmException, IOException {
        try {
            final List<ImportStatus> importStatuses = profilesImporter.importProfiles(defaultProfiles,
                    ImportPolicy.UPDATE_DEFAULTS, SessionService.SYSTEM_ID);
            log.info(
                    "Updated default profiles " + importStatuses);
            if (md5File != null) { // but may not exist
                IOUtil.writeMD5(md5File, defaultProfilesXml.getBytes());
            }
        } catch (ExecutionException e) {
            log.error("Unable to update default profiles", e);
        }
        return null;
    }

    File getProfilesMD5File() throws BonitaHomeNotSetException, IOException {
        return ProfilesImporter.getFileContainingMD5(tenantId);
    }

    /**
     * Checks if profiles should be updated: if MD5 file differs from MD5 of XML file
     *
     * @param md5File
     * @param defaultProfilesXml
     * @return true if profiles should be updated
     * @throws NoSuchAlgorithmException
     */
    boolean shouldUpdateProfiles(final File md5File, final String defaultProfilesXml) throws NoSuchAlgorithmException {
        return md5File == null || !IOUtil.checkMD5(md5File, defaultProfilesXml.getBytes());
    }

    /**
     * @return content of the XML file that contains default profiles
     * @throws IOException in case of problem reading profiles file
     */
    String getDefaultProfilesXml() throws IOException {
        String profiles = IOUtil.readResource("profiles-sp.xml");
        if (profiles != null) {
            log.info(
                    "Loading profiles from file profiles-sp.xml");
        } else {
            profiles = IOUtil.readResource("profiles.xml");
            log.info(
                    "Loading profiles from file profiles.xml");
        }
        return profiles;
    }

    ProfilesNode getProfilesFromXML(final String defaultProfilesXml) throws IOException {
        return profilesImporter.convertFromXml(defaultProfilesXml);
    }
}
