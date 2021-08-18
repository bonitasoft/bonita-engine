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

import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.bonitasoft.engine.profile.xml.ProfilesNode;
import org.bonitasoft.engine.xml.parser.AbstractParser;

/**
 * @author Baptiste Mesta
 */
public class ProfilesParser extends AbstractParser<ProfilesNode> {

    @Override
    protected JAXBContext initJAXBContext() throws JAXBException {
        return JAXBContext.newInstance(ProfilesNode.class);
    }

    @Override
    protected URL initSchemaURL() {
        return ProfilesParser.class.getResource("/profiles.xsd");
    }

    @Override
    public String convert(ProfilesNode model) throws JAXBException {
        // Do a clone by serializing of the original object to avoid modifying it
        // we could have used org.apache.commons.lang3.SerializationUtils but it does the same thing using java serialization
        ProfilesNode clone = convert(super.convert(model));
        // Unset all profile entries before export
        clone.getProfiles().forEach(p -> p.setParentProfileEntries(null));
        return super.convert(clone);
    }
}
