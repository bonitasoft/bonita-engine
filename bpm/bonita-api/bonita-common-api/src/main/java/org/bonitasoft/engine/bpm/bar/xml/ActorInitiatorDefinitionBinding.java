/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.bpm.bar.xml;

import java.util.Map;

import org.bonitasoft.engine.io.xml.XMLParseException;

/**
 * @author Feng Hui
 * @author Matthieu Chaffotte
 */
public class ActorInitiatorDefinitionBinding extends NamedElementBinding {

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) throws XMLParseException {
    }

    @Override
    public void setChildObject(final String name, final Object value) throws XMLParseException {
    }

    @Override
    public String getObject() {
        return name;
    }

    @Override
    public String getElementTag() {
        return XMLProcessDefinition.INITIATOR_NODE;
    }

}
