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
package org.bonitasoft.engine.api.impl.projectdeployer.descriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.bonitasoft.engine.api.impl.projectdeployer.model.*;
import org.bonitasoft.engine.api.impl.projectdeployer.model.Process;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "version",
        "description",
        "targetVersion",
        "organization",
        "profiles",
        "businessDataModel",
        "bdmAccessControl",
        "processes",
        "restAPIExtensions",
        "pages",
        "layouts",
        "themes",
        "applications",
        "modelVersion"
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeploymentDescriptor {

    @JsonProperty("name")
    private String name;
    @JsonProperty("version")
    private String version;
    @JsonProperty("description")
    private String description;
    @JsonProperty("targetVersion")
    private String targetVersion;
    @JsonProperty("organization")
    private Organization organization;
    @JsonProperty("profiles")
    @Singular
    private List<Profile> profiles;
    @JsonProperty("businessDataModel")
    private BusinessDataModel businessDataModel;
    @JsonProperty("bdmAccessControl")
    private BdmAccessControl bdmAccessControl;
    @JsonProperty("processes")
    @Singular
    private List<Process> processes;
    @JsonProperty("restAPIExtensions")
    @Singular
    private List<RestAPIExtension> restAPIExtensions;
    @JsonProperty("pages")
    @Singular
    private List<Page> pages;
    @JsonProperty("layouts")
    @Singular
    private List<Layout> layouts;
    @JsonProperty("themes")
    @Singular
    private List<Theme> themes;
    @JsonProperty("applications")
    @Singular
    private List<Application> applications;
    @JsonProperty("modelVersion")
    private String modelVersion;

    private Properties configurationProperties;

    public List<Profile> getProfiles() {
        if (profiles == null) {
            return Collections.emptyList();
        }
        return profiles;
    }

    public void add(Profile profile) {
        if (profiles == null) {
            profiles = new ArrayList<>();
        }
        profiles.add(profile);
    }

    public List<Process> getProcesses() {
        if (processes == null) {
            return Collections.emptyList();
        }
        return processes;
    }

    public List<RestAPIExtension> getRestAPIExtensions() {
        if (restAPIExtensions == null) {
            return Collections.emptyList();
        }
        return restAPIExtensions;
    }

    public void add(RestAPIExtension restApiExtenstion) {
        if (restAPIExtensions == null) {
            restAPIExtensions = new ArrayList<>();
        }
        restAPIExtensions.add(restApiExtenstion);
    }

    public List<Page> getPages() {
        if (pages == null) {
            return Collections.emptyList();
        }
        return pages;
    }

    public void add(Page page) {
        if (pages == null) {
            pages = new ArrayList<>();
        }
        pages.add(page);
    }

    public List<Layout> getLayouts() {
        if (layouts == null) {
            return Collections.emptyList();
        }
        return layouts;
    }

    public List<Theme> getThemes() {
        if (themes == null) {
            return Collections.emptyList();
        }
        return themes;
    }

    public List<Application> getApplications() {
        if (applications == null) {
            return Collections.emptyList();
        }
        return applications;
    }

    public void add(Application application) {
        if (applications == null) {
            applications = new ArrayList<>();
        }
        applications.add(application);
    }

}
