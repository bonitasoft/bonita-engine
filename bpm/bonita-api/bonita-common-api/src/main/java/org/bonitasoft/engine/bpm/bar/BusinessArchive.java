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
package org.bonitasoft.engine.bpm.bar;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.bonitasoft.engine.bpm.bar.form.model.FormMappingModel;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;

/**
 * Represents the content of a BusinessArchive file (*.bar). It contains the {@link org.bonitasoft.engine.bpm.process.DesignProcessDefinition} and all resources
 * necessary to the process execution.
 *
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @see org.bonitasoft.engine.bpm.process.DesignProcessDefinition
 */
public class BusinessArchive implements Serializable {

    private static final long serialVersionUID = -6410347766671025202L;

    private DesignProcessDefinition processDefinition;

    private Map<String, String> parameters;

    private final Map<String, byte[]> resources = new HashMap<String, byte[]>();

    private FormMappingModel formMappingModel = new FormMappingModel();

    /**
     * Default constructor. Creates an instance of {@code BusinessArchive}
     */
    public BusinessArchive() {
    }

    /**
     * Retrieves the related {@link org.bonitasoft.engine.bpm.process.DesignProcessDefinition}
     *
     * @return the related {@code DesignProcessDefinition}
     * @see org.bonitasoft.engine.bpm.process.DesignProcessDefinition
     */
    public DesignProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    /**
     * Defines the related {@link org.bonitasoft.engine.bpm.process.DesignProcessDefinition}
     *
     * @param processDefinition the related {@code DesignProcessDefinition}
     * @see org.bonitasoft.engine.bpm.process.DesignProcessDefinition
     */
    public void setProcessDefinition(final DesignProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
    }

    /**
     * Retrieves the {@code BusinessArchive} parameters
     *
     * @return the {@code BusinessArchive} parameters
     */
    public Map<String, String> getParameters() {
        if (parameters == null) {
            parameters = new HashMap<String, String>();
        }
        return parameters;
    }

    /**
     * Defines the {@code BusinessArchive} parameters
     *
     * @param parameters the {@code BusinessArchive} parameters
     */
    public void setParameters(final Map<String, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * Retrieves a byte array representing the content of the resource identified by the given path.
     * <br>
     * <p>Example:</p>
     * <pre>businessArchive.getResource("actorMapping.xml")</pre>
     * <br>
     *
     * @param resourcePath the complete resource path
     * @return a byte array representing the content of the resource identified by the given path.
     */
    public byte[] getResource(final String resourcePath) {
        return resources.get(resourcePath);
    }

    /**
     * Retrieves a {@link java.util.Map} representing the resources having paths matching with the given regular expression. The {@code Map} keys store the
     * resource paths and the {@code Map} values store the resource content.
     * <br>
     * <p>Example:</p>
     * <pre>businessArchive.getResources("^classpath/.*$")</pre>
     * <br>
     *
     * @param regex the regular expression used to match the resource path
     * @return a {@link java.util.Map} representing the resources having paths matching with the given regular expression
     */
    public Map<String, byte[]> getResources(final String regex) {
        final Pattern pattern = Pattern.compile(regex);
        final Map<String, byte[]> result = new HashMap<>();
        for (final Entry<String, byte[]> resource : getResources().entrySet()) {
            if (pattern.matcher(resource.getKey()).matches()) {
                result.put(resource.getKey(), resource.getValue());
            }
        }
        return result;
    }

    /**
     * Adds a resource to this {@code BusinessArchive}
     *
     * @param resourcePath the complete resource path. It must contain the full path and filename
     * @param resourceData the byte array representing the resource content
     */
    protected void addResource(final String resourcePath, final byte[] resourceData) {
        resources.put(resourcePath, resourceData);
    }

    /**
     * Retrieves a {@code Map} containing all resources of this {@code BusinessArchive}. The {@code Map} keys store the resource full paths and the {@code Map}
     * values store the resource content.
     *
     * @return a {@code Map} containing all resources of this {@code BusinessArchive}
     */
    public Map<String, byte[]> getResources() {
        return Collections.unmodifiableMap(resources);
    }

    /**
     * Sets the task-to-form (or process-to-form) mapping in this {@link BusinessArchive}.
     *
     * @param formMappingModel the model to store.
     */
    public void setFormMappings(final FormMappingModel formMappingModel) {
        this.formMappingModel = formMappingModel;
    }

    /**
     * Retrieves the form mapping model for this {@link BusinessArchive}.
     *
     * @return the form mapping model for this {@link BusinessArchive}.
     */
    public FormMappingModel getFormMappingModel() {
        return formMappingModel;
    }

}
