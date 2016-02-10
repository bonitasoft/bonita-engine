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
package org.bonitasoft.engine.search.comment;

import java.util.List;

import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.comment.model.archive.SAComment;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractArchivedCommentsSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;

/**
 * @author Hongwen Zang
 * @author Celine Souchet
 */
public class SearchArchivedComments extends AbstractArchivedCommentsSearchEntity {

    private final SCommentService sCommentService;

    public SearchArchivedComments(final SCommentService sCommentService, final SearchEntityDescriptor searchDescriptor, final SearchOptions options) {
        super(searchDescriptor, options);
        this.sCommentService = sCommentService;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaReadException {
        return sCommentService.getNumberOfArchivedComments(searchOptions);
    }

    @Override
    public List<SAComment> executeSearch(final QueryOptions searchOptions) throws SBonitaReadException {
        return sCommentService.searchArchivedComments(searchOptions);
    }

}
