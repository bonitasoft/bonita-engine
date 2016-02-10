/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.engine.bpm.bar;

import java.io.File;
import java.io.IOException;

import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping;
import org.bonitasoft.engine.io.IOUtil;

/**
 * @author Emmanuel Duchastenier
 */
public class ActorMappingContribution extends GenericFileContribution {

    public static final String ACTOR_MAPPING_FILE = "actorMapping.xml";

    @Override
    public boolean isMandatory() {
        return false;
    }

    @Override
    public String getName() {
        return "ActorMapping";
    }

    @Override
    public String getFileName() {
        return ACTOR_MAPPING_FILE;
    }

    @Override
    public boolean readFromBarFolder(final BusinessArchive businessArchive, final File barFolder) throws IOException {
        final File file = new File(barFolder, ACTOR_MAPPING_FILE);
        if (file.exists()) {
            final byte[] content = IOUtil.getContent(file);
            try {
                businessArchive.setActorMapping(new ActorMappingMarshaller().deserializeFromXML(content));
            } catch (XmlMarshallException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void saveToBarFolder(final BusinessArchive businessArchive, final File barFolder) throws IOException {
        final ActorMapping actorMapping = businessArchive.getActorMapping();
        if (actorMapping != null) {
            try {
                final byte[] fileContent = new ActorMappingMarshaller().serializeToXML(actorMapping);
                final File file = new File(barFolder, ACTOR_MAPPING_FILE);
                IOUtil.write(file, fileContent);
            } catch (XmlMarshallException e) {
                throw new IOException("Cannot write Actor Mapping to Bar folder", e);
            }
        }
    }

}
