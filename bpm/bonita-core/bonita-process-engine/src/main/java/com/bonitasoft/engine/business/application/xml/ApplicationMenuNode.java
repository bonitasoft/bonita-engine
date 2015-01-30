/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * @author Elias Ricken de Medeiros
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ApplicationMenuNode {

    @XmlElement(required = true)
    private String displayName;

    @XmlAttribute
    private String applicationPage;

    @XmlElementWrapper(name = "applicationMenus")
    @XmlElement(name = "applicationMenu")
    private List<ApplicationMenuNode> applicationMenus;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getApplicationPage() {
        return applicationPage;
    }

    public void setApplicationPage(String applicationPage) {
        this.applicationPage = applicationPage;
    }

    public List<ApplicationMenuNode> getApplicationMenus() {
        return applicationMenus == null ? Collections.<ApplicationMenuNode> emptyList() : Collections.unmodifiableList(applicationMenus);
    }

    public void addApplicationMenu(ApplicationMenuNode applicationMenu) {
        if (applicationMenus == null) {
            applicationMenus = new ArrayList<ApplicationMenuNode>();
        }
        applicationMenus.add(applicationMenu);
    }
}
