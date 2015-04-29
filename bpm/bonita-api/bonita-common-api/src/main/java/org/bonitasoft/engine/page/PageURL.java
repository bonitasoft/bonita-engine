/*
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
 */
package org.bonitasoft.engine.page;

import java.util.Objects;

/**
 * author Emmanuel Duchastenier
 */
public class PageURL {

    private String url;

    private Long pageId;

    public String getUrl() {
        return url;
    }

    public Long getPageId() {
        return pageId;
    }

    public PageURL(String url, Long pageId) {
        this.url = url;
        this.pageId = pageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageURL pageURL = (PageURL) o;
        return Objects.equals(url, pageURL.url) &&
                Objects.equals(pageId, pageURL.pageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, pageId);
    }

    @Override
    public String toString() {
        return "PageURL{" +
                "url='" + url + '\'' +
                ", pageId=" + pageId +
                '}';
    }
}
