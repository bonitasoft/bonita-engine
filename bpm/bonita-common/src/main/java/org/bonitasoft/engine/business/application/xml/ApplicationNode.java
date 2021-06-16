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
package org.bonitasoft.engine.business.application.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.*;

import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * @author Elias Ricken de Medeiros
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ApplicationNode {

    @XmlAttribute(required = true)
    private String token;

    @XmlAttribute(required = true)
    private String version;

    @XmlElement(required = true)
    private String displayName;

    @XmlElement
    private String description;

    @XmlAttribute
    private String profile;

    @XmlAttribute
    private String homePage;

    @XmlAttribute(required = true)
    private String state;

    @XmlAttribute
    private String layout;

    @XmlAttribute
    private String theme;

    @XmlElement
    private String iconPath;

    @XmlElementWrapper(name = "applicationPages")
    @XmlElement(name = "applicationPage")
    private List<ApplicationPageNode> applicationPages = new ArrayList<>();

    @XmlElementWrapper(name = "applicationMenus")
    @XmlElement(name = "applicationMenu")
    private List<ApplicationMenuNode> applicationMenus = new ArrayList<>();

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getHomePage() {
        return homePage;
    }

    public void setHomePage(String homePage) {
        this.homePage = homePage;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(final String layout) {
        this.layout = layout;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(final String theme) {
        this.theme = theme;
    }

    public List<ApplicationPageNode> getApplicationPages() {
        return applicationPages;
    }

    public void setApplicationPages(List<ApplicationPageNode> applicationPages) {
        this.applicationPages = applicationPages;
    }

    public void addApplicationPage(ApplicationPageNode applicationPage) {
        this.applicationPages.add(applicationPage);
    }

    public List<ApplicationMenuNode> getApplicationMenus() {
        return applicationMenus;
    }

    public void setApplicationMenus(List<ApplicationMenuNode> applicationMenus) {
        this.applicationMenus = applicationMenus;
    }

    public void addApplicationMenu(ApplicationMenuNode applicationMenu) {
        applicationMenus.add(applicationMenu);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        ApplicationNode that = (ApplicationNode) o;

        return new EqualsBuilder().append(getToken(), that.getToken()).append(getVersion(), that.getVersion())
                .append(getDisplayName(), that.getDisplayName()).append(getDescription(), that.getDescription())
                .append(getProfile(), that.getProfile()).append(getHomePage(), that.getHomePage())
                .append(getState(), that.getState()).append(getLayout(), that.getLayout())
                .append(getTheme(), that.getTheme()).append(getIconPath(), that.getIconPath())
                .append(getApplicationPages(), that.getApplicationPages())
                .append(getApplicationMenus(), that.getApplicationMenus()).isEquals();
    }
}
