/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.bar;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class BusinessArchive implements Serializable {

    private static final long serialVersionUID = -6410347766671025202L;

    private DesignProcessDefinition processDefinition;

    private Map<String, String> parameters;

    private final Map<String, byte[]> resources = new HashMap<String, byte[]>();

    public BusinessArchive() {
    }

    public DesignProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    public void setProcessDefinition(final DesignProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
    }

    public Map<String, String> getParameters() {
        if (parameters == null) {
            parameters = new HashMap<String, String>();
        }
        return parameters;
    }

    public void setParameters(final Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public byte[] getResource(final String resourceName) {
        return resources.get(resourceName);
    }

    public Map<String, byte[]> getResources(final String regex) {
        final Pattern pattern = Pattern.compile(regex);
        final Map<String, byte[]> result = new HashMap<String, byte[]>();
        for (final Entry<String, byte[]> resource : resources.entrySet()) {
            if (pattern.matcher(resource.getKey()).matches()) {
                result.put(resource.getKey(), resource.getValue());
            }
        }
        return result;
    }

    /**
     * Adds a resource to this BusinessArchive
     * 
     * @param resourcePath
     *            must contain the full path and filename
     * @param resourceData
     *            the byte array of the resource content
     */
    protected void addResource(final String resourcePath, final byte[] resourceData) {
        resources.put(resourcePath, resourceData);
    }

    public Map<String, byte[]> getResources() {
        return Collections.unmodifiableMap(resources);
    }

}
