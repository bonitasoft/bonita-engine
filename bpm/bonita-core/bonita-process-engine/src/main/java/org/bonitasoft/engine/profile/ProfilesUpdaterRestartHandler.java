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
import org.bonitasoft.engine.api.impl.PlatformAPIImpl;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.execution.work.RestartException;
import org.bonitasoft.engine.execution.work.TenantRestartHandler;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.profile.impl.ExportedProfile;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * Check that default profiles are up to date
 *
 * @author Baptiste Mesta
 */
public class ProfilesUpdaterRestartHandler implements TenantRestartHandler {

    @Override
    public void beforeServicesStart(final PlatformServiceAccessor platformServiceAccessor, final TenantServiceAccessor tenantServiceAccessor)
            throws RestartException {

    }

    @Override
    public void afterServicesStart(final PlatformServiceAccessor platformServiceAccessor, final TenantServiceAccessor tenantServiceAccessor) {
        try {
            String profilesFile = getProfilesFileName();
            final File file = getMD5File(tenantServiceAccessor);
            final String xmlContent = getXMLContent(profilesFile);
            if (IOUtil.checkMD5(file, xmlContent.getBytes())) {
                tenantServiceAccessor.getTechnicalLoggerService().log(ProfilesUpdaterRestartHandler.class, TechnicalLogSeverity.INFO, "Default profiles are up to date");
                return;
            } else {
                tenantServiceAccessor.getTechnicalLoggerService().log(ProfilesUpdaterRestartHandler.class, TechnicalLogSeverity.INFO,
                        "Default profiles not up to date, updating them...");
            }
            updateProfiles(platformServiceAccessor, tenantServiceAccessor, file, xmlContent);
        } catch (IOException e) {
            tenantServiceAccessor.getTechnicalLoggerService().log(ProfilesUpdaterRestartHandler.class, TechnicalLogSeverity.ERROR,
                    "Unable to read the read the default profile file to update them", e);
        } catch (Exception e) {
            tenantServiceAccessor.getTechnicalLoggerService().log(ProfilesUpdaterRestartHandler.class, TechnicalLogSeverity.ERROR, "Unable to update default profiles", e);
        }

    }

    String getXMLContent(String profilesFile) throws IOException {
        return IOUtil.readResource(profilesFile);
    }

    private void updateProfiles(PlatformServiceAccessor platformServiceAccessor, final TenantServiceAccessor tenantServiceAccessor, final File file,
            final String xmlContent) throws Exception {
        final List<ExportedProfile> profilesFromXML = ProfilesImporter.getProfilesFromXML(xmlContent, tenantServiceAccessor.getProfileParser());

        final TransactionService transactionService = platformServiceAccessor.getTransactionService();
        transactionService.executeInTransaction(getCallable(tenantServiceAccessor, file, xmlContent, profilesFromXML));
    }

    Callable<Object> getCallable(final TenantServiceAccessor tenantServiceAccessor, final File file, final String xmlContent, final List<ExportedProfile> profilesFromXML) {
        return new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                return doUpdateProfiles(tenantServiceAccessor, profilesFromXML, file, xmlContent);
            }

        };
    }

    Object doUpdateProfiles(TenantServiceAccessor tenantServiceAccessor, List<ExportedProfile> profilesFromXML, File file, String xmlContent)
            throws NoSuchAlgorithmException, IOException {
        List<ImportStatus> importStatuses;
        try {
            ProfilesImporter profilesImporter = createProfilesImporter(tenantServiceAccessor, profilesFromXML);
            importStatuses = profilesImporter.importProfiles(-1);

        } catch (ExecutionException e) {
            tenantServiceAccessor.getTechnicalLoggerService().log(ProfilesUpdaterRestartHandler.class, TechnicalLogSeverity.ERROR, "Unable to update default profiles", e);
            return null;
        }
        tenantServiceAccessor.getTechnicalLoggerService().log(ProfilesUpdaterRestartHandler.class, TechnicalLogSeverity.INFO, "Updated default profiles " + importStatuses);
        IOUtil.writeMD5(file, xmlContent.getBytes());
        return null;
    }

    protected String getProfilesFileName() {
        return PlatformAPIImpl.PROFILES_FILE;
    }

    protected ProfilesImporter createProfilesImporter(TenantServiceAccessor tenantServiceAccessor, List<ExportedProfile> profilesFromXML) {
        return new ProfilesImporter(tenantServiceAccessor.getProfileService(), tenantServiceAccessor
                .getIdentityService(), profilesFromXML, ImportPolicy.UPDATE_DEFAULTS);
    }

    File getMD5File(TenantServiceAccessor tenantServiceAccessor) throws BonitaHomeNotSetException {
        return ProfilesImporter.getFileContainingMD5(tenantServiceAccessor);
    }

}
