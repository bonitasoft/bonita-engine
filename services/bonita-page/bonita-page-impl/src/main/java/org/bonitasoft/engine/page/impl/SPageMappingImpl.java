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

package org.bonitasoft.engine.page.impl;

import java.util.Objects;

import org.bonitasoft.engine.page.SPageMapping;
import org.bonitasoft.engine.persistence.PersistentObjectId;

/**
 * @author Baptiste Mesta
 */
public class SPageMappingImpl extends  PersistentObjectId implements SPageMapping {

    private String key;
    private Long pageId;
    private String url;
    private String urlAdapter;
    private long lastUpdateDate;
    private long lastUpdatedBy;

    public SPageMappingImpl() {
    }

    @Override
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getUrlAdapter() {
        return urlAdapter;
    }

    public void setUrlAdapter(String urlAdapter) {
        this.urlAdapter = urlAdapter;
    }

    @Override
    public Long getPageId() {
        return pageId;
    }

    public void setPageId(Long pageId) {
        this.pageId = pageId;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getDiscriminator() {
        return this.getClass().getName();
    }

    public long getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(long lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SPageMappingImpl)) return false;
        if (!super.equals(o)) return false;
        SPageMappingImpl that = (SPageMappingImpl) o;
        return Objects.equals(lastUpdateDate, that.lastUpdateDate) &&
                Objects.equals(lastUpdatedBy, that.lastUpdatedBy) &&
                Objects.equals(key, that.key) &&
                Objects.equals(pageId, that.pageId) &&
                Objects.equals(url, that.url) &&
                Objects.equals(urlAdapter, that.urlAdapter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), key, pageId, url, urlAdapter, lastUpdateDate, lastUpdatedBy);
    }

    @Override
    public String toString() {
        return "SPageMappingImpl{" +
                "key='" + key + '\'' +
                ", pageId=" + pageId +
                ", url='" + url + '\'' +
                ", urlAdapter='" + urlAdapter + '\'' +
                ", lastUpdateDate=" + lastUpdateDate +
                ", lastUpdatedBy=" + lastUpdatedBy +
                "} " + super.toString();
    }
}
