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
package org.bonitasoft.web.rest.server.framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.web.rest.server.framework.exception.APIMissingIdException;
import org.bonitasoft.web.rest.server.framework.json.JSonSimpleDeserializer;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.rest.server.framework.utils.RestRequestParser;
import org.bonitasoft.web.toolkit.client.common.AbstractTreeNode;
import org.bonitasoft.web.toolkit.client.common.Tree;
import org.bonitasoft.web.toolkit.client.common.TreeLeaf;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIIncorrectIdException;
import org.bonitasoft.web.toolkit.client.common.json.JSonItemReader;
import org.bonitasoft.web.toolkit.client.common.json.JSonItemWriter;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ValidatorEngine;
import org.bonitasoft.web.toolkit.server.ServletCall;

/**
 * @author Séverin Moussel
 * @author Baptiste Mesta
 * @author Fabio Lombardi
 */
public class APIServletCall extends ServletCall {

    public static final String PARAMETER_COUNTER = "n";

    public static final String PARAMETER_DEPLOY = "d";

    public static final String PARAMETER_FILTER = "f";

    public static final String PARAMETER_SEARCH = "s";

    public static final String PARAMETER_ORDER = "o";

    public static final String PARAMETER_LIMIT = "c";

    public static final String PARAMETER_PAGE = "p";

    public static final String PARAMETER_QUERY = "q";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // REQUEST PARSING
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected API<? extends IItem> api;

    private String apiName;

    private String resourceName;

    private APIID id;

    public APIServletCall(final HttpServletRequest request, final HttpServletResponse response) {
        super(request, response);
    }

    /**
     * Constructor for tests
     */
    public APIServletCall() {
        super();

    }

    public String getApiName() {
        return apiName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public APIID getId() {
        return id;
    }

    /**
     * Read the inputStream and parse it as an IItem compatible with the called API.
     */
    private IItem getJSonStreamAsItem() {
        final IItem item = JSonItemReader.parseItem(getInputStream(), api.getItemDefinition());

        ValidatorEngine.validate(item, false);

        return item;
    }

    /**
     * Read elements form the request
     * <ul>
     * <li>API tokens</li>
     * <li>item id (if defined)</li>
     * <li>parameters</li>
     * </ul>
     *
     * @param request
     * @param response
     */
    @Override
    protected final void parseRequest(final HttpServletRequest request, final HttpServletResponse response) {

        parsePath(request);

        // Fixes BS-400. This is ugly.
        I18n.getInstance();

        api = APIs.get(apiName, resourceName);
        api.setCaller(this);

        super.parseRequest(request, response);

    }

    void parsePath(final HttpServletRequest request) {
        final RestRequestParser restRequestParser = new RestRequestParser(request).invoke();
        APIID resourceQualifiers = restRequestParser.getResourceQualifiers();
        if (resourceQualifiers != null && resourceQualifiers.getIds().size() > 0
                && isAnyNumberIdNegativeOrZero(resourceQualifiers.getIds())) {
            throw new APIIncorrectIdException("Id must be non-zero positive for " + restRequestParser.getApiName()
                    + " on resource " + restRequestParser.getResourceName());
        }
        id = resourceQualifiers;
        apiName = restRequestParser.getApiName();
        resourceName = restRequestParser.getResourceName();
    }

    private boolean isAnyNumberIdNegativeOrZero(List<String> ids) {
        for (String id : ids) {
            try {
                if (Long.parseLong(id) <= 0L) {
                    return true;
                }
            } catch (NumberFormatException e) {
                // Ignore non-number ids, since they are acceptable
            }
        }
        return false;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // EXECUTE METHODS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Entry point for GET and SEARCH
     */
    @Override
    public final void doGet() {
        try {
            // GET one
            if (id != null) {
                output(api.runGet(id, getParameterAsList(PARAMETER_DEPLOY), getParameterAsList(PARAMETER_COUNTER)));
            } else if (countParameters() == 0) {
                throw new APIMissingIdException(getRequestURL());
            }
            // Search
            else {
                final ItemSearchResult<?> result = api.runSearch(Integer.parseInt(getParameter(PARAMETER_PAGE, "0")),
                        Integer.parseInt(getParameter(PARAMETER_LIMIT, "10")), getParameter(PARAMETER_SEARCH),
                        getParameter(PARAMETER_ORDER), parseFilters(getParameterAsList(PARAMETER_FILTER)),
                        getParameterAsList(PARAMETER_DEPLOY), getParameterAsList(PARAMETER_COUNTER));
                head("Content-Range", result.getPage() + "-" + result.getLength() + "/" + result.getTotal());

                output(result.getResults());
            }
        } catch (final APIException e) {
            e.setApi(apiName);
            e.setResource(resourceName);
            throw e;
        }
    }

    @Override
    protected void output(final Object object) {
        super.output(object);
    }

    @Override
    protected void head(final String name, final String value) {
        super.head(name, value);
    }

    /**
     * Entry point for CREATE
     */
    @Override
    public final void doPost() {
        try {
            final IItem jSonStreamAsItem = getJSonStreamAsItem();
            final IItem outputItem = api.runAdd(jSonStreamAsItem);

            output(JSonItemWriter.itemToJSON(outputItem));
        } catch (final APIException e) {
            e.setApi(apiName);
            e.setResource(resourceName);
            throw e;

        }
    }

    /**
     * Entry point for UPDATE
     */
    @Override
    public final void doPut() {
        try {
            if (id == null) {
                throw new APIMissingIdException(getRequestURL());
            }

            final String inputStream = getInputStream();
            if (inputStream.length() == 0) {
                api.runUpdate(id, new HashMap<>());
                return;
            }

            Item.setApplyValidatorMandatoryByDefault(false);
            final IItem item = getJSonStreamAsItem();
            api.runUpdate(id, getAttributesWithDeploysAsJsonString(item));
        } catch (final APIException e) {
            e.setApi(apiName);
            e.setResource(resourceName);
            throw e;

        }
    }

    /**
     * Get deploys and add them in json representation in map<String, String>
     * Workaround to be able to have included json objects in main object in PUT request
     * You have to unserialize them to be able to use them in java representation
     */
    private HashMap<String, String> getAttributesWithDeploysAsJsonString(final IItem item) {
        final HashMap<String, String> map = new HashMap<>();
        map.putAll(item.getAttributes());
        for (final Entry<String, IItem> deploy : item.getDeploys().entrySet()) {
            map.put(deploy.getKey(), deploy.getValue().toJson());
        }
        return map;
    }

    /**
     * Entry point for DELETE
     */
    @Override
    public final void doDelete() {
        try {
            final List<APIID> ids = new ArrayList<>();

            // Using ids in json input stream
            if (id == null) {
                final String inputStream = getInputStream();
                if (inputStream.length() == 0) {
                    throw new APIMissingIdException("Id of the element to delete is missing [" + inputStream + "]");
                }

                // Parsing ids in Json input stream
                final AbstractTreeNode<String> tree = JSonSimpleDeserializer.unserializeTree(inputStream);

                if (tree instanceof Tree<?>) {
                    final List<AbstractTreeNode<String>> nodes = ((Tree<String>) tree).getNodes();

                    for (final AbstractTreeNode<String> node : nodes) {
                        if (node instanceof Tree<?>) {
                            ids.add(APIID.makeAPIID(((Tree<String>) node).getValues()));
                        } else if (node instanceof TreeLeaf<?>) {
                            ids.add(APIID.makeAPIID(((TreeLeaf<String>) node).getValue()));
                        } else {
                            throw new APIMissingIdException(
                                    "Id of the elements to delete are missing or misswritten \"" + inputStream + "\"");
                        }
                    }
                } else {
                    throw new APIMissingIdException(
                            "Id of the elements to delete are missing or misswritten \"" + inputStream + "\"");
                }
            }

            // Using id in URL
            else {
                ids.add(id);
            }

            api.runDelete(ids);
        } catch (final APIException e) {
            e.setApi(apiName);
            e.setResource(resourceName);
            throw e;

        }
    }

    /**
     * @param parameters
     * @return
     */
    private Map<String, String> parseFilters(final List<String> parameters) {
        if (parameters == null) {
            return null;
        }
        final Map<String, String> results = new HashMap<>();
        for (final String parameter : parameters) {
            final String[] split = parameter.split("=");
            if (split.length < 2) {
                results.put(split[0], null);
            } else {
                results.put(split[0], split[1]);
            }
        }
        return results;
    }
}
