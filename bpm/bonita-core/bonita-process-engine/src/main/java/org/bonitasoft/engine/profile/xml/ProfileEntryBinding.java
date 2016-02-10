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
package org.bonitasoft.engine.profile.xml;

import java.util.Map;

import org.bonitasoft.engine.profile.ExportedProfileEntryBuilder;
import org.bonitasoft.engine.profile.impl.ExportedProfileEntry;
import org.bonitasoft.engine.xml.ElementBinding;

/**
 * @author Zhao Na
 * @author Celine Souchet
 */
public class ProfileEntryBinding extends ElementBinding {

    private ExportedProfileEntryBuilder profileEntryBuilder = null;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        profileEntryBuilder = new ExportedProfileEntryBuilder(attributes.get("name"));
        String isCustom = attributes.get("isCustom");
        profileEntryBuilder.setCustom(Boolean.valueOf(isCustom));
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
        if ("description".equals(name)) {
            profileEntryBuilder.setDescription(value);
        } else if ("type".equals(name)) {
            profileEntryBuilder.setType(value);
        } else if ("page".equals(name)) {
            profileEntryBuilder.setPage(value);
        } else if ("index".equals(name)) {
            profileEntryBuilder.setIndex(Long.parseLong(value));
        } else if ("parentName".equals(name)) {
            profileEntryBuilder.setParentName(value);
        }
    }

    @Override
    public void setChildObject(final String name, final Object value) {
    }

    @Override
    public ExportedProfileEntry getObject() {
        return profileEntryBuilder.done();
    }

    @Override
    public String getElementTag() {
        return "profileEntry";
    }

}
