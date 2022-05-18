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

import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.profile.xml.ProfilesNode;
import org.bonitasoft.engine.session.SessionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Update provided profiles from profiles.xml from classpath
 */
@Component
public class DefaultProfilesUpdater implements TenantLifecycleService {

    private final TechnicalLoggerService logger;
    private final ProfilesImporter profilesImporter;
    private final Long tenantId;

    public DefaultProfilesUpdater(@Value("${tenantId}") Long tenantId, TechnicalLoggerService logger,
            ProfilesImporter profilesImporter) {
        this.logger = logger;
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
                logger.log(DefaultProfilesUpdater.class, TechnicalLogSeverity.INFO,
                        "Default profiles not up to date, updating them...");
                final ProfilesNode defaultProfiles = getProfilesFromXML(defaultProfilesXml);
                doUpdateProfiles(defaultProfiles, md5File, defaultProfilesXml);
                return true;
            } else {
                // No update required
                logger.log(DefaultProfilesUpdater.class, TechnicalLogSeverity.INFO,
                        "Default profiles are up to date");
                return false;
            }

        } catch (IOException e) {
            logger.log(DefaultProfilesUpdater.class, TechnicalLogSeverity.ERROR,
                    "Unable to read the read the default profile file to update them", e);
        } catch (Exception e) {
            logger.log(DefaultProfilesUpdater.class, TechnicalLogSeverity.ERROR,
                    "Unable to update default profiles", e);
        }
        return false;
    }

    Object doUpdateProfiles(final ProfilesNode defaultProfiles, final File md5File, final String defaultProfilesXml)
            throws NoSuchAlgorithmException, IOException {
        try {
            final List<ImportStatus> importStatuses = profilesImporter.importProfiles(defaultProfiles,
                    ImportPolicy.UPDATE_DEFAULTS, SessionService.SYSTEM_ID);
            logger.log(DefaultProfilesUpdater.class, TechnicalLogSeverity.INFO,
                    "Updated default profiles " + importStatuses);
            if (md5File != null) {
                IOUtil.writeMD5(md5File, defaultProfilesXml.getBytes());
            }
        } catch (ExecutionException e) {
            logger.log(DefaultProfilesUpdater.class, TechnicalLogSeverity.ERROR,
                    "Unable to update default profiles", e);
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
            logger.log(DefaultProfilesUpdater.class, TechnicalLogSeverity.INFO,
                    "Loading profiles from file profiles-sp.xml");
        } else {
            profiles = IOUtil.readResource("profiles.xml");
            logger.log(DefaultProfilesUpdater.class, TechnicalLogSeverity.INFO,
                    "Loading profiles from file profiles.xml");
        }
        return profiles;
    }

    ProfilesNode getProfilesFromXML(final String defaultProfilesXml) throws IOException {
        return profilesImporter.convertFromXml(defaultProfilesXml);
    }
}
