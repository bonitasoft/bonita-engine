/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api.form;

import java.io.Serializable;
import java.util.Date;

import org.bonitasoft.engine.form.FormMapping;
import org.bonitasoft.engine.form.FormMappingTarget;
import org.bonitasoft.engine.form.FormMappingType;

/**
 * Created by Fabio Lombardi
 */
public class FormMappingItem implements Serializable {

    private String id;
    private String processDefinitionId;
    private FormMappingType type;
    private FormMappingTarget target;
    private String task;
    private String pageId;
    private String pageURL;
    private String pageMappingKey;
    private String lastUpdatedBy;
    private Date lastUpdateDate;
    private boolean formRequired;

    public FormMappingItem(final FormMapping item) {
        id = String.valueOf(item.getId());
        processDefinitionId = String.valueOf(item.getProcessDefinitionId());
        type = item.getType();
        target = item.getTarget();
        task = item.getTask();
        pageId = null;
        if (item.getPageId() != null) {
            pageId = String.valueOf(item.getPageId());
        }
        pageURL = item.getURL();
        pageMappingKey = item.getPageMappingKey();
        lastUpdatedBy = String.valueOf(item.getLastUpdatedBy());
        lastUpdateDate = item.getLastUpdateDate();
        formRequired = item.isFormRequired();
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(final String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getPageMappingKey() {
        return pageMappingKey;
    }

    public void setPageMappingKey(final String pageMappingKey) {
        this.pageMappingKey = pageMappingKey;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getTask() {
        return task;
    }

    public void setTask(final String task) {
        this.task = task;
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(final String pageId) {
        this.pageId = pageId;
    }

    public String getURL() {
        return pageURL;
    }

    public void setPageURL(final String pageURL) {
        this.pageURL = pageURL;
    }

    public FormMappingType getType() {
        return type;
    }

    public void setType(final FormMappingType type) {
        this.type = type;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(final String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(final Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public FormMappingTarget getTarget() {
        return target;
    }

    public void setTarget(final FormMappingTarget target) {
        this.target = target;
    }

    public boolean isFormRequired() {
        return formRequired;
    }

    public void setFormRequired(final boolean formRequired) {
        this.formRequired = formRequired;
    }

}
