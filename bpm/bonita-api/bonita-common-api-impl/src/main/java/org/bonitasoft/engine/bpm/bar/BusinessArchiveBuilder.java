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

import java.util.ArrayList;
import java.util.Map;

import org.bonitasoft.engine.bpm.bar.form.model.FormMappingModel;
import org.bonitasoft.engine.bpm.document.DocumentDefinition;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;

/**
 * <b>Creates {@link BusinessArchive}</b>
 * <p>
 * A typical use of this class is to build programmatically Business archive in order to deploy it using the {@link org.bonitasoft.engine.api.ProcessAPI} <p>
 * <p>
 * If you wish to deploy a BusinessArchived stored in a .bar file use {@link BusinessArchiveFactory} instead.
 * <p>
 * <p>
 * <p>
 * Usage example:
 * <p> {@code BusinessArchive businessArchive = BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinition).done();}
 *
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @version 6.3.5
 * @since 6.0.0
 */
public class BusinessArchiveBuilder {

    private BusinessArchive entity;

    public BusinessArchiveBuilder createNewBusinessArchive() {
        entity = new BusinessArchive();
        return this;
    }

    /**
     * Set the process definition of the {@link BusinessArchive} that is currently build
     * <p> {@link DesignProcessDefinition} can be constructed using {@link org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder}
     *
     * @see org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder
     * @param processDefinition
     * @return
     *         the same {@link BusinessArchiveBuilder} in order to chain calls
     */
    public BusinessArchiveBuilder setProcessDefinition(final DesignProcessDefinition processDefinition) {
        entity.setProcessDefinition(processDefinition);
        return this;
    }

    /**
     * Set the parameters values
     * <p>
     * Parameters must also be defined in the {@link org.bonitasoft.engine.bpm.process.ProcessDefinition} using
     * {@link org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder} <p> {@link DesignProcessDefinition} can be
     * constructed using {@link org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder}
     *
     * @see org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder
     * @param parameters
     *        the parameters value to use
     * @return
     *         the same {@link BusinessArchiveBuilder} in order to chain calls
     */
    public BusinessArchiveBuilder setParameters(final Map<String, String> parameters) {
        entity.setParameters(parameters);
        return this;
    }

    /**
     * Add JAR dependencies to the business archive.
     * <p>
     * Everything that is added as classpath resource will be added in the (JAVA) classpath of the process
     * <p>
     * <p>
     * e.g. if you add a connector in your process add here dependencies the process need to execute it
     *
     * @param resource
     *        the {@link BarResource} the represent a JAR file
     * @return
     *         the same {@link BusinessArchiveBuilder} in order to chain calls
     */
    public BusinessArchiveBuilder addClasspathResource(final BarResource resource) {
        addBarResourceInPath(resource, "classpath/");
        return this;
    }

    /**
     * Add connector implementation descriptor to the business archive.
     * <p>
     * This resource must be a connector implementation descriptor file (.impl) and must be compliant to the connector-implementation-descriptor.xsd
     * <p>
     * A connector definition should also be added in the {@link org.bonitasoft.engine.bpm.process.ProcessDefinition} using
     * {@link org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder#addConnector(String, String, String, org.bonitasoft.engine.bpm.connector.ConnectorEvent)}
     * or
     * {@link org.bonitasoft.engine.bpm.process.impl.ActivityDefinitionBuilder#addConnector(String, String, String, org.bonitasoft.engine.bpm.connector.ConnectorEvent)}
     * <p>
     * <p>
     * e.g. if you add a connector in your process add here dependencies the process need to execute it
     *
     * @param resource
     *        the {@link BarResource} the represent a connector implementation descriptor file
     * @return
     *         the same {@link BusinessArchiveBuilder} in order to chain calls
     */
    public BusinessArchiveBuilder addConnectorImplementation(final BarResource resource) {
        addBarResourceInPath(resource, "connector/");
        return this;
    }

    /**
     * same as {@link #addConnectorImplementation(BarResource)} but for {@link org.bonitasoft.engine.filter.UserFilter}
     *
     * @see #addConnectorImplementation(BarResource)
     * @param resource
     * @return
     *         the same {@link BusinessArchiveBuilder} in order to chain calls
     */
    public BusinessArchiveBuilder addUserFilters(final BarResource resource) {
        addBarResourceInPath(resource, "userFilters/");
        return this;
    }

    /**
     * Set the actor mapping for this {@link BusinessArchive} <p>
     * The file must be compliant with the xsd actorMapping.xsd The actor mapping specify for each {@link org.bonitasoft.engine.bpm.actor.ActorDefinition} of
     * the process who it is in the
     * organization.
     * <p>
     * It is not mandatory to set it in the {@link BusinessArchive}, it can be set after the process was deployed using
     * {@link org.bonitasoft.engine.api.ProcessAPI#addUserToActor(long, long)}, {@link org.bonitasoft.engine.api.ProcessAPI#addGroupToActor(long, long)},
     * {@link org.bonitasoft.engine.api.ProcessAPI#addRoleToActor(long, long)} or
     * {@link org.bonitasoft.engine.api.ProcessAPI#addRoleAndGroupToActor(long, long, long)}
     *
     * @param xmlContent
     *        the xml file content that describe the actor mapping
     * @return
     *         the same {@link BusinessArchiveBuilder} in order to chain calls
     */
    public BusinessArchiveBuilder setActorMapping(final byte[] xmlContent) {
        entity.addResource(ActorMappingContribution.ACTOR_MAPPING_FILE, xmlContent);
        return this;
    }

    /**
     * Set the form mappings Model for this {@link BusinessArchive}.
     * <p>
     * This allows to map tasks and process to internal forms or external pages, so that .
     * <p>
     *
     * @param formMappingModel
     *        The model containing the form mappings for this process definition.
     * @return
     *         the same {@link BusinessArchiveBuilder} in order to chain calls
     */
    public BusinessArchiveBuilder setFormMappings(final FormMappingModel formMappingModel) {
        entity.setFormMappings(formMappingModel);
        return this;
    }

    /**
     * Add resource in the {@link BusinessArchive} used by extensions
     *
     * @param resource
     *        the resource to be added in the business archive
     * @return
     *         the same {@link BusinessArchiveBuilder} in order to chain calls
     */
    public BusinessArchiveBuilder addExternalResource(final BarResource resource) {
        addBarResourceInPath(resource, ExternalResourceContribution.EXTERNAL_RESOURCE_FOLDER + "/");
        return this;
    }

    /**
     * Add document contents in the {@link BusinessArchive}
     *
     * @see org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder#addDocumentDefinition(String)
     * @param resource
     * @return
     */
    public BusinessArchiveBuilder addDocumentResource(final BarResource resource) {
        addBarResourceInPath(resource, "documents/");
        return this;
    }

    protected void addBarResourceInPath(final BarResource resource, final String path) {
        entity.addResource(path + resource.getName(), resource.getContent());
    }

    /**
     * @return
     *         The BusinessArchive
     * @throws InvalidBusinessArchiveFormatException
     *         when the business archive is inconsistent in the current state
     */
    public BusinessArchive done() throws InvalidBusinessArchiveFormatException {
        if (entity.getProcessDefinition() == null) {
            throw new InvalidBusinessArchiveFormatException("missing process definition");
        }
        final ArrayList<String> errors = new ArrayList<String>();
        for (final DocumentDefinition document : entity.getProcessDefinition().getProcessContainer().getDocumentDefinitions()) {
            if (document.getFile() != null && !document.getFile().isEmpty()) {
                final byte[] resources = entity.getResource("documents/" + document.getFile());
                if (resources == null) {
                    errors.add("missing document in the business archive that is present in the process definition " + document.getFile());
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new InvalidBusinessArchiveFormatException(errors);
        }
        return entity;
    }

}
