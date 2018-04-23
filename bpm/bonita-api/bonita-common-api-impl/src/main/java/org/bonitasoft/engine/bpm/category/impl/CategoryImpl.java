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
package org.bonitasoft.engine.bpm.category.impl;

import java.util.Date;

import org.bonitasoft.engine.bpm.category.Category;

/**
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class CategoryImpl implements Category {

    private static final long serialVersionUID = 5609899843000875034L;

    private final long id;

    private final String name;

    private String description;

    private long creator;

    private Date creationDate;

    private Date lastUpdate;

    public CategoryImpl(final long id, final String name) {
        super();
        this.id = id;
        this.name = name;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public long getCreator() {
        return creator;
    }

    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    @Override
    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setCreator(final long creator) {
        this.creator = creator;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setLastUpdate(final Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public String toString() {
        return "CategoryImpl [id=" + id + ", name=" + name + ", description=" + description + ", creator=" + creator + ", creationDate=" + creationDate
                + ", lastUpdate=" + lastUpdate + "]";
    }

}
