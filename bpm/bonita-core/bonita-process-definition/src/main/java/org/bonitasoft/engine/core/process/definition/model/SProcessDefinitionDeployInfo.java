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
package org.bonitasoft.engine.core.process.definition.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@IdClass(PersistentObjectId.class)
@Table(name = "process_definition")
public class SProcessDefinitionDeployInfo implements PersistentObject {

    public static final String DESCRIPTION = "description";
    public static final String ID_KEY = "id";
    public static final String NAME_KEY = "name";
    public static final String VERSION_KEY = "version";
    public static final String DEPLOYMENT_DATE_KEY = "deploymentDate";
    public static final String DEPLOYED_BY_KEY = "deployedBy";
    public static final String ACTIVATION_STATE_KEY = "activationState";
    public static final String CONFIGURATION_STATE_KEY = "configurationState";
    public static final String PROCESS_ID_KEY = "processId";
    public static final String DISPLAY_NAME_KEY = "displayName";
    public static final String DISPLAY_DESCRIPTION_KEY = "displayDescription";
    public static final String LAST_UPDATE_DATE_KEY = "lastUpdateDate";
    public static final String ICON_PATH = "iconPath";
    public static final String DESIGN_CONTENT = "designContent.content";
    public static final String LABEL = "label";

    private String name;
    @Id
    private long id;
    private long deploymentDate;
    private long deployedBy;
    private String version;
    private String description;
    private String configurationState;
    private String activationState;
    @Id
    private long tenantId;
    private long processId;
    private String displayName;
    private long lastUpdateDate;
    private String iconPath;
    private String displayDescription;
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "content_tenantid", referencedColumnName = "tenantid"),
            @JoinColumn(name = "content_id", referencedColumnName = "id") })
    private SProcessDefinitionDesignContent designContent;
}
