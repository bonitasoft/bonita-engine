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
package org.bonitasoft.engine.page;

import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

public interface SPageUpdateBuilder {

    EntityUpdateDescriptor done();

    /**
     * @deprecated since bonita 7.13 the update of name is no more supported, it will not be take into account on the
     *             page update
     */
    @Deprecated
    SPageUpdateBuilder updateName(String value);

    SPageUpdateBuilder updateDescription(String value);

    SPageUpdateBuilder updateDisplayName(String value);

    SPageUpdateBuilder updateLastModificationDate(long currentTimeMillis);

    SPageUpdateBuilder updateLastUpdatedBy(long userId);

    SPageUpdateBuilder updateContentName(String value);

    SPageUpdateBuilder updateContentType(String contentType);

    SPageUpdateBuilder updateProcessDefinitionId(Long processDedfinitionId);

    SPageUpdateBuilder updatePageHash(String hash);

    SPageUpdateBuilder markNonProvided();

}
