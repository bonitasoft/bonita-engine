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
package org.bonitasoft.web.rest.server.datastore.bpm.cases;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.console.common.server.api.CommandCaller;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.comment.Comment;
import org.bonitasoft.engine.bpm.comment.SearchCommentsDescriptor;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.web.rest.model.bpm.cases.CommentItem;
import org.bonitasoft.web.rest.server.datastore.CommonDatastore;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasAdd;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasSearch;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.rest.server.framework.utils.SearchOptionsBuilderUtil;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APISessionInvalidException;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Vincent Elcrin
 */
public class CommentDatastore extends CommonDatastore<CommentItem, Comment>
        implements DatastoreHasAdd<CommentItem>, DatastoreHasSearch<CommentItem> {

    /**
     * Conversion look up table to for sortable fields
     */
    private static final String[][] SORTABLE_FIELDS_LUT = {
            { CommentItem.ATTRIBUTE_POST_DATE, SearchCommentsDescriptor.POSTDATE },
    };

    /**
     * Command to fetch comments supervised by
     */
    private static final String COMMAND_SEARCH_COMMENTS_SUPERVISEDBY = "searchSCommentSupervisedBy";

    private static final String PROPERTY_SEARCH_OPTION_KEY = "SEARCH_OPTIONS_KEY";

    private static final String PROPERTY_SUPERVISOR_ID_KEY = "supervisorId";

    /**
     * Default Constructor.
     *
     * @param engineSession
     */
    public CommentDatastore(final APISession engineSession) {
        super(engineSession);
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.web.toolkit.server.api.DatastoreHasSearch#search(int, int, java.lang.String,
     * java.lang.String, java.util.Map)
     */
    @Override
    public ItemSearchResult<CommentItem> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {

        // prepare search
        final SearchOptionsBuilder builder = SearchOptionsBuilderUtil.buildSearchOptions(page, resultsByPage, "",
                search);
        adjustSearchBuilder(filters, builder);

        /**
         * Need to convert field name passed by API into sortable field provided by engine.
         */
        if (orders != null && !orders.isEmpty()) {
            final String[] sort = orders.split(" ");
            for (int i = 0; i < SORTABLE_FIELDS_LUT.length; i++) {
                if (sort[0].equals(SORTABLE_FIELDS_LUT[i][0])) {
                    builder.sort(SORTABLE_FIELDS_LUT[i][1], Order.valueOf(sort[1]));
                }
            }
        }

        SearchResult<Comment> engineSearchResults = null;

        /*
         * Search depends on the type of user which is defined in the filter. Only one at a time.
         */
        final APIID teamManagerAPIID = APIID.makeAPIID(filters.get(CommentItem.FILTER_TEAM_MANAGER_ID));
        final APIID supervisorAPIID = APIID.makeAPIID(filters.get(CommentItem.FILTER_SUPERVISOR_ID));
        final APIID userAPIID = APIID.makeAPIID(filters.get(CommentItem.FILTER_USER_ID));

        if (teamManagerAPIID != null && teamManagerAPIID.isValidLongID()) {
            engineSearchResults = runTeamManagerSearch(teamManagerAPIID.toLong(), builder);
        } else if (supervisorAPIID != null && supervisorAPIID.isValidLongID()) {
            engineSearchResults = runSupervisorSearch(supervisorAPIID.toLong(), builder);
        } else if (userAPIID != null && userAPIID.isValidLongID()) {
            engineSearchResults = runUserSearch(userAPIID.toLong(), builder);
        } else {
            engineSearchResults = runCustomSearch(builder);
        }

        if (engineSearchResults != null) {
            /*
             * Process result to convert engine items into console items
             */
            final List<CommentItem> consoleSearchResults = new ArrayList<>();
            for (final Comment comment : engineSearchResults.getResult()) {
                consoleSearchResults.add(convertEngineToConsoleItem(comment));
            }
            return new ItemSearchResult<>(page, resultsByPage, engineSearchResults.getCount(), consoleSearchResults);
        } else {
            throw new APIException("Search failed for the following parameters <page: " + page + " - resulsByPage: "
                    + resultsByPage + " - search: " + search
                    + " - filters: " + filters + " - orders: " + orders + ">");
        }
    }

    /**
     * Search comments managed by specified user (ex administrator)
     *
     * @param builder
     * @return
     */
    private SearchResult<Comment> runTeamManagerSearch(final long teamManagerId, final SearchOptionsBuilder builder) {
        try {
            final ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(getEngineSession());
            return processAPI.searchCommentsManagedBy(teamManagerId, builder.done());
        } catch (final InvalidSessionException e) {
            throw new APISessionInvalidException(e);
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    /**
     * Search comment involving specified user.
     *
     * @param userId
     * @param builder
     * @return
     */
    private SearchResult<Comment> runUserSearch(final long userId, final SearchOptionsBuilder builder) {
        try {
            final ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(getEngineSession());
            return processAPI.searchCommentsInvolvingUser(userId, builder.done());
        } catch (final InvalidSessionException e) {
            throw new APISessionInvalidException(e);
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    /**
     * Search custom. It's up to you!
     *
     * @param builder
     * @return
     */
    private SearchResult<Comment> runCustomSearch(final SearchOptionsBuilder builder) {
        try {
            final ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(getEngineSession());
            return processAPI.searchComments(builder.done());
        } catch (final InvalidSessionException e) {
            throw new APISessionInvalidException(e);
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    /**
     * Search comment supervised by specified user (ex process owner)
     *
     * @param supervisorId
     * @param builder
     * @return
     */
    @SuppressWarnings("unchecked")
    private SearchResult<Comment> runSupervisorSearch(final long supervisorId, final SearchOptionsBuilder builder) {
        // FIXME change me with API method
        return (SearchResult<Comment>) new CommandCaller(getEngineSession(), COMMAND_SEARCH_COMMENTS_SUPERVISEDBY)
                .addParameter(PROPERTY_SEARCH_OPTION_KEY, builder.done())
                .addParameter(PROPERTY_SUPERVISOR_ID_KEY, supervisorId)
                .run();
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.web.toolkit.server.api.DatastoreHasAdd#add(org.bonitasoft.web.toolkit.client.data.item.Item)
     */
    @Override
    public CommentItem add(final CommentItem item) {
        try {
            final ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(getEngineSession());
            return convertEngineToConsoleItem(
                    processAPI.addProcessComment(item.getProcessInstanceId().toLong(), item.getContent()));
        } catch (final InvalidSessionException e) {
            throw new APISessionInvalidException(e);
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CONVERTS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Convert ProcessItem filters into engine filers
     *
     * @param filters
     * @param builder
     */
    private void adjustSearchBuilder(final Map<String, String> filters, final SearchOptionsBuilder builder) {
        addStringFilterToSearchBuilder(filters, builder, CommentItem.ATTRIBUTE_PROCESS_INSTANCE_ID,
                SearchCommentsDescriptor.PROCESS_INSTANCE_ID);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.bonitasoft.console.server.credentials.bpm.CommonDatastore#convertEngineToConsoleItem(java.io.Serializable)
     */
    @Override
    protected CommentItem convertEngineToConsoleItem(final Comment engineItem) {
        if (engineItem == null) {
            return null;
        }

        final CommentItem consoleItem = new CommentItem();
        consoleItem.setId(engineItem.getId());
        consoleItem.setUserId(engineItem.getUserId());
        consoleItem.setProcessInstanceId(engineItem.getProcessInstanceId());
        consoleItem.setPostDate(engineItem.getPostDate());
        consoleItem.setContent(engineItem.getContent());

        return consoleItem;
    }

}
