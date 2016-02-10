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
package org.bonitasoft.engine.bpm.data.impl;

import java.io.Serializable;
import java.util.Date;

import org.bonitasoft.engine.bpm.data.ArchivedDataInstance;

/**
 * @author Matthieu Chaffotte
 */
public class ArchivedDataInstanceImpl extends DataInstanceImpl implements ArchivedDataInstance {

    private static final long serialVersionUID = 2770043188433234604L;

    private Date archiveDate;

    private long sourceObjectId;

    private Serializable value;

    @Override
    public Date getArchiveDate() {
        return archiveDate;
    }

    public void setArchiveDate(final Date archiveDate) {
        this.archiveDate = archiveDate;
    }

    @Override
    public long getSourceObjectId() {
        return sourceObjectId;
    }

    public void setSourceObjectId(final long sourceObjectId) {
        this.sourceObjectId = sourceObjectId;
    }

    @Override
    public Serializable getValue() {
        return value;
    }

    @Override
    public void setValue(final Serializable value) {
        this.value = value;
    }

}
