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

import org.bonitasoft.engine.profile.ExportedParentProfileEntryBuilder;
import org.bonitasoft.engine.profile.impl.ExportedParentProfileEntry;
import org.bonitasoft.engine.profile.impl.ExportedProfileEntry;
import org.bonitasoft.engine.xml.ElementBinding;

/**
 * @author Zhao Na
 * @author Celine Souchet
 */
public class ParentProfileEntryBinding extends ElementBinding {

    private ExportedParentProfileEntryBuilder parentProfileEntryBuilder = null;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        parentProfileEntryBuilder = new ExportedParentProfileEntryBuilder(attributes.get("name"));
        String isCustom = attributes.get("isCustom");
        parentProfileEntryBuilder.setCustom(Boolean.valueOf(isCustom));
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
        if ("description".equals(name)) {
            parentProfileEntryBuilder.setDescription(value);
        } else if ("type".equals(name)) {
            parentProfileEntryBuilder.setType(value);
        } else if ("page".equals(name)) {
            parentProfileEntryBuilder.setPage(value);
        } else if ("index".equals(name)) {
            parentProfileEntryBuilder.setIndex(Long.parseLong(value));
        } else if ("parentName".equals(name)) {
            parentProfileEntryBuilder.setParentName(value);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setChildObject(final String name, final Object value) {
        if ("childrenEntries".equals(name)) {
            parentProfileEntryBuilder.setChildProfileEntries((List<ExportedProfileEntry>) value);
        }
    }

    @Override
    public ExportedParentProfileEntry getObject() {
        return parentProfileEntryBuilder.done();
    }

    @Override
    public String getElementTag() {
        return "parentProfileEntry";
    }

}
