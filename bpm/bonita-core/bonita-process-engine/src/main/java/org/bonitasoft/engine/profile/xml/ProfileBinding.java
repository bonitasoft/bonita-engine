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

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.profile.ExportedProfileBuilder;
import org.bonitasoft.engine.profile.impl.ExportedParentProfileEntry;
import org.bonitasoft.engine.profile.impl.ExportedProfile;
import org.bonitasoft.engine.profile.impl.ExportedProfileMapping;
import org.bonitasoft.engine.xml.ElementBinding;

/**
 * @author Zhao Na
 * @author Celine Souchet
 */
public class ProfileBinding extends ElementBinding {

    protected ExportedProfileBuilder profileBuilder = null;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        profileBuilder = new ExportedProfileBuilder(attributes.get("name"), Boolean.valueOf(attributes.get("isDefault")));
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
        if ("description".equals(name)) {
            profileBuilder.setDescription(value);
        }
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if ("profileEntries".equals(name)) {
            profileBuilder.setParentProfileEntries((List<ExportedParentProfileEntry>) value);
        } else if ("profileMapping".equals(name)) {
            profileBuilder.setProfileMapping((ExportedProfileMapping) value);
        }
    }

    @Override
    public ExportedProfile getObject() {
        return profileBuilder.done();
    }

    @Override
    public String getElementTag() {
        return "profile";
    }

}
