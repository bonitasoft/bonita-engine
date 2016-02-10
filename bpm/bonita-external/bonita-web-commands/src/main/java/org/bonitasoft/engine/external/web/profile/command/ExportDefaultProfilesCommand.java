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
package org.bonitasoft.engine.external.web.profile.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.api.impl.SessionInfos;
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
 * @author Charles Souillard
 */
public class ExportDefaultProfilesCommand extends TenantCommand {

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        try {
            final InputStream xmlStream = ExportDefaultProfilesCommand.class.getResourceAsStream("/profiles.xml");
            final byte[] xmlContent = IOUtils.toByteArray(xmlStream);
            xmlStream.close();
            return xmlContent;
        } catch (final Exception e) {
            throw new SCommandParameterizationException("Unable to export default profiles", e);
        }
    }

}
