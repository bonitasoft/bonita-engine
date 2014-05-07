/**
 * Copyright (C) 2012-2014 Bonitasoft S.A.
 * Bonitasoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.external.web.profile.command;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.command.TenantCommand;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.profile.ImportPolicy;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.ProfilesImporter;
import org.bonitasoft.engine.profile.impl.ExportedProfile;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.xml.Parser;

/**
 * Specific Command to import profiles xml content as byte[].
 * "byte[]" : xml content
 * 
 * @author Zhao Na
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class ImportProfilesCommand extends TenantCommand {

    /**
     * @return a List<String> is a warning message list in case of non-existing User, Group or Role to map the profile to.
     */
    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        final ProfileService profileService = serviceAccessor.getProfileService();
        final IdentityService identityService = serviceAccessor.getIdentityService();

        byte[] xmlContent = null;
        try {
            xmlContent = (byte[]) parameters.get("xmlContent");
            if (xmlContent == null) {
                throw new SCommandParameterizationException("Parameters map must contain an entry  xmlContent with a byte array value.");
            }
        } catch (final Exception e) {
            throw new SCommandParameterizationException("Parameters map must contain an entry  xmlContent with a byte array value.", e);
        }

        final Parser parser = serviceAccessor.getProfileParser();
        try {
            final List<ExportedProfile> profiles = ProfilesImporter.getProfilesFromXML(new String(xmlContent), parser);

            return (Serializable) ProfilesImporter.toWarnings(new ProfilesImporter(profileService, identityService, profiles, ImportPolicy.DELETE_EXISTING)
                    .importProfiles());
        } catch (ExecutionException e) {
            throw new SCommandExecutionException(e);
        }
    }

}
