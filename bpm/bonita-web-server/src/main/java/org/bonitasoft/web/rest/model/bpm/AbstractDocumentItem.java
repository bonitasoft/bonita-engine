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
package org.bonitasoft.web.rest.model.bpm;

import java.util.Date;

import org.bonitasoft.web.toolkit.client.common.util.StringUtil;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasUniqueId;

/**
 * @author Paul AMAR
 */
public abstract class AbstractDocumentItem extends Item implements ItemHasUniqueId {

    public static final String ATTRIBUTE_NAME = "name";

    public static final String ATTRIBUTE_AUTHOR = "author";

    public static final String ATTRIBUTE_CREATION_DATE = "creationDate";

    public static final String ATTRIBUTE_HAS_CONTENT = "hasContent";

    public static final String ATTRIBUTE_FILENAME = "filename";

    public static final String ATTRIBUTE_CONTENT_MIME_TYPE = "contentMimetype";

    public static final String ATTRIBUTE_FILE = "file";

    public static final String ATTRIBUTE_URL = "url";

    // /////////////////////////////////////////////////////////////////////////////////
    // / FILTER
    // /////////////////////////////////////////////////////////////////////////////////

    public static final String FILTER_SUPERVISOR_ID = "supervisor_id";

    // /////////////////////////////////////////////////////////////////////////////////
    // / GETTERS
    // /////////////////////////////////////////////////////////////////////////////////

    public String getName() {
        return this.getAttributeValue(ATTRIBUTE_NAME);
    }

    public String getFileName() {
        return this.getAttributeValue(ATTRIBUTE_FILENAME);
    }

    public String getContentMimeType() {
        return this.getAttributeValue(ATTRIBUTE_CONTENT_MIME_TYPE);
    }

    public APIID getAuthor() {
        return this.getAttributeValueAsAPIID(ATTRIBUTE_AUTHOR);
    }

    public String getContentFileName() {
        return this.getAttributeValue(ATTRIBUTE_FILENAME);
    }

    public Date getCreationDate() {
        return this.getAttributeValueAsDate(ATTRIBUTE_CREATION_DATE);
    }

    public String getUrl() {
        return this.getAttributeValue(ATTRIBUTE_URL);
    }

    public String getFile() {
        return this.getAttributeValue(ATTRIBUTE_FILE);
    }

    public boolean hasContent() {
        return StringUtil.toBoolean(this.getAttributeValue(ATTRIBUTE_HAS_CONTENT));
    }

    // //////////////////////////////////////////////////////////////////////////////////
    // / SETTERS
    // /////////////////////////////////////////////////////////////////////////////////

    @Override
    public void setId(final String id) {
        this.setAttribute(ATTRIBUTE_ID, id);
    }

    @Override
    public void setId(final Long id) {
        this.setAttribute(ATTRIBUTE_ID, id);
    }

    public void setHasContent(final boolean hasContent) {
        this.setAttribute(ATTRIBUTE_NAME, hasContent);
    }

    public void setContentMimeType(final String contentMimeType) {
        this.setAttribute(ATTRIBUTE_CONTENT_MIME_TYPE, contentMimeType);
    }

    public void setFileName(final String fileName) {
        this.setAttribute(ATTRIBUTE_FILENAME, fileName);
    }

    public void setUrl(final String url) {
        this.setAttribute(ATTRIBUTE_URL, url);
    }

    public void setAuthor(final long author) {
        this.setAttribute(ATTRIBUTE_AUTHOR, author);
    }

    public void setAuthor(final APIID author) {
        this.setAttribute(ATTRIBUTE_AUTHOR, author);
    }

    public void setAuthor(final String author) {
        this.setAttribute(ATTRIBUTE_AUTHOR, author);
    }

    public void setCreationDate(final Date creationDate) {
        this.setAttribute(ATTRIBUTE_CREATION_DATE, creationDate);
    }

    public void setFile(final String file) {
        this.setAttribute(ATTRIBUTE_FILE, file);
    }

    public void setName(final String name) {
        this.setAttribute(ATTRIBUTE_NAME, name);
    }

}
