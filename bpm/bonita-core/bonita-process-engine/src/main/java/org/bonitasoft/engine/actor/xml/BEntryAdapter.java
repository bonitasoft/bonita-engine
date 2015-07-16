
/*******************************************************************************
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
 ******************************************************************************/

package org.bonitasoft.engine.actor.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.bonitasoft.engine.bpm.bar.xml.XMLProcessDefinition;

/**
 * Created by mazourd on 15/07/15.
 */
public class BEntryAdapter extends XmlAdapter<BEntryAdapter.Entry, XMLProcessDefinition.BEntry<String, String>> {

    @Override
    public XMLProcessDefinition.BEntry<String, String> unmarshal(Entry v) throws Exception {
        XMLProcessDefinition.BEntry<String, String> mapEntry = new XMLProcessDefinition.BEntry<>(v.group, v.role);
        return mapEntry;
    }

    @Override
    public Entry marshal(XMLProcessDefinition.BEntry<String, String> mapEntry) throws Exception {
        Entry entree = new Entry();
        entree.group = mapEntry.getKey();
        entree.role = mapEntry.getValue();
        return entree;
    }

    public static class Entry {

        public String group;

        public String role;

    }

}
