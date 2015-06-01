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

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.profile.impl.ExportedProfile;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * Class that creates and updates default profiles<br/>
 * Called at node restart and tenant creation
 * @author Philippe Ozil
 */
public class DefaultProfilesUpdater {

    private static final String DEFAULT_PROFILES_FILE = "profiles.xml";

    protected PlatformServiceAccessor platformServiceAccessor;
    protected TenantServiceAccessor tenantServiceAccessor;

    public DefaultProfilesUpdater(final PlatformServiceAccessor platformServiceAccessor, final TenantServiceAccessor tenantServiceAccessor) {
        this.platformServiceAccessor = platformServiceAccessor;
        this.tenantServiceAccessor = tenantServiceAccessor;
    }

    /**
     * Executes a default profile update
     * @param shouldCreateTransaction whether or not a new transaction is created to execute updater
     * @return whether the default profiles where updated
     * @throws Exception if execution fails
     */
    public boolean execute(final boolean shouldCreateTransaction) throws Exception {
        try {
            final File md5File = getProfilesMD5File();
            final String defaultProfilesXml = getDefaultProfilesXml();
            if (shouldUpdateProfiles(md5File, defaultProfilesXml)) {
                // Default profiles do not exist or are different
                tenantServiceAccessor.getTechnicalLoggerService().log(DefaultProfilesUpdater.class, TechnicalLogSeverity.INFO,
                        "Default profiles not up to date, updating them...");
                final List<ExportedProfile> defaultProfiles = getProfilesFromXML(defaultProfilesXml);
                if (shouldCreateTransaction) {
                    final TransactionService transactionService = platformServiceAccessor.getTransactionService();
                    transactionService.executeInTransaction(getUpdateProfilesCallable(md5File, defaultProfilesXml, defaultProfiles));
                }
                else {
                    doUpdateProfiles(defaultProfiles, md5File, defaultProfilesXml);
                }
                return true;
            } else {
                // No update required
                tenantServiceAccessor.getTechnicalLoggerService().log(DefaultProfilesUpdater.class, TechnicalLogSeverity.INFO,
                        "Default profiles are up to date");
                return false;
            }

        } catch (IOException e) {
            tenantServiceAccessor.getTechnicalLoggerService().log(DefaultProfilesUpdater.class, TechnicalLogSeverity.ERROR,
                    "Unable to read the read the default profile file to update them", e);
        } catch (Exception e) {
            tenantServiceAccessor.getTechnicalLoggerService().log(DefaultProfilesUpdater.class, TechnicalLogSeverity.ERROR,
                    "Unable to update default profiles", e);
        }
        return false;
    }

    Callable<Object> getUpdateProfilesCallable(final File md5File, final String defaultProfilesXml, final List<ExportedProfile> defaultProfiles) {
        return new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                return doUpdateProfiles(defaultProfiles, md5File, defaultProfilesXml);
            }

        };
    }

    Object doUpdateProfiles(final List<ExportedProfile> defaultProfiles, final File md5File, final String defaultProfilesXml)
            throws NoSuchAlgorithmException, IOException {
        try {
            final ProfilesImporter profilesImporter = createProfilesImporter(defaultProfiles);
            final List<ImportStatus> importStatuses = profilesImporter.importProfiles(-1);
            tenantServiceAccessor.getTechnicalLoggerService().log(DefaultProfilesUpdater.class, TechnicalLogSeverity.INFO,
                    "Updated default profiles " + importStatuses);
            IOUtil.writeMD5(md5File, defaultProfilesXml.getBytes());
        } catch (ExecutionException e) {
            tenantServiceAccessor.getTechnicalLoggerService().log(DefaultProfilesUpdater.class, TechnicalLogSeverity.ERROR,
                    "Unable to update default profiles", e);
        }
        return null;
    }

    protected ProfilesImporter createProfilesImporter(final List<ExportedProfile> profilesFromXML) {
        return new ProfilesImporter(tenantServiceAccessor.getProfileService(), tenantServiceAccessor
                .getIdentityService(), profilesFromXML, ImportPolicy.UPDATE_DEFAULTS);
    }

    File getProfilesMD5File() throws BonitaHomeNotSetException, IOException {
        return ProfilesImporter.getFileContainingMD5(tenantServiceAccessor.getTenantId());
    }

    /**
     * Checks if profiles should be updated: if MD5 file differs from MD5 of XML file
     * @param md5File
     * @param defaultProfilesXml
     * @return true if profiles should be updated
     * @throws NoSuchAlgorithmException
     */
    boolean shouldUpdateProfiles(final File md5File, final String defaultProfilesXml) throws NoSuchAlgorithmException {
        return !IOUtil.checkMD5(md5File, defaultProfilesXml.getBytes());
    }

    /**
     * @return content of the XML file that contains default profiles
     * @throws IOException
     */
    String getDefaultProfilesXml() throws IOException {
        return IOUtil.readResource(DEFAULT_PROFILES_FILE);
    }

    List<ExportedProfile> getProfilesFromXML(final String defaultProfilesXml) throws IOException {
        return ProfilesImporter.getProfilesFromXML(defaultProfilesXml, tenantServiceAccessor.getProfileParser());
    }
}
