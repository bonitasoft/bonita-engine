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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

import org.bonitasoft.engine.page.SPageMapping;
import org.bonitasoft.engine.persistence.PersistentObjectId;

/**
 * @author Baptiste Mesta
 */
public class SPageMappingImpl extends PersistentObjectId implements SPageMapping {

    public static final String COMMA_DELIM = ",";
    private String key;
    private Long pageId;
    private String url;
    private String urlAdapter;
    private String pageAuthorizRules;
    private List<String> authorizationRules = new ArrayList<>();
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

    public String getPageAuthorizRules() {
        return pageAuthorizRules;
    }

    public void setPageAuthorizRules(String pageAuthorizRules) {
        this.pageAuthorizRules = pageAuthorizRules;
    }

    private void parseRules() {
        if (pageAuthorizRules != null) {
            authorizationRules.clear();
            for (StringTokenizer stringTk = new StringTokenizer(pageAuthorizRules, COMMA_DELIM, false); stringTk.hasMoreTokens();) {
                String rule = stringTk.nextToken();
                authorizationRules.add(rule);
            }
        }
    }

    private void buildRulesAsString() {
        pageAuthorizRules = null;
        if (authorizationRules != null && !authorizationRules.isEmpty()) {
            pageAuthorizRules = "";
            for (String authorizationRule : authorizationRules) {
                pageAuthorizRules += (authorizationRule + COMMA_DELIM);
            }
        }
    }

    @Override
    public List<String> getPageAuthorizationRules() {
        parseRules(); // Need to do it here because Hibernate does not call setter to set fields from DB to Object
        return authorizationRules;
    }

    public void setPageAuthorizationRules(List<String> rules) {
        authorizationRules = rules;
        buildRulesAsString();
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

    @Override
    public long getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(long lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @Override
    public long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SPageMappingImpl))
            return false;
        if (!super.equals(o))
            return false;
        SPageMappingImpl that = (SPageMappingImpl) o;
        return Objects.equals(lastUpdateDate, that.lastUpdateDate) &&
                Objects.equals(lastUpdatedBy, that.lastUpdatedBy) &&
                Objects.equals(key, that.key) &&
                Objects.equals(pageId, that.pageId) &&
                Objects.equals(url, that.url) &&
                Objects.equals(urlAdapter, that.urlAdapter) &&
                Objects.equals(pageAuthorizRules, that.pageAuthorizRules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), key, pageId, url, urlAdapter, lastUpdateDate, lastUpdatedBy, pageAuthorizRules);
    }

    @Override
    public String toString() {
        return "SPageMappingImpl{" +
                "key='" + key + '\'' +
                ", pageId=" + pageId +
                ", url='" + url + '\'' +
                ", urlAdapter='" + urlAdapter + '\'' +
                ", pageAuthorizRules='" + pageAuthorizRules + '\'' +
                ", authorizationRules=" + authorizationRules +
                ", lastUpdateDate=" + lastUpdateDate +
                ", lastUpdatedBy=" + lastUpdatedBy +
                "} " + super.toString();
    }
}
