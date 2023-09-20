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
package org.bonitasoft.web.rest.server.api.resource;

import java.io.FileNotFoundException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.core.JsonParseException;
import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.web.rest.server.datastore.filter.Filters;
import org.bonitasoft.web.rest.server.datastore.utils.SearchOptionsCreator;
import org.bonitasoft.web.rest.server.datastore.utils.Sorts;
import org.bonitasoft.web.rest.server.framework.APIServletCall;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Range;
import org.restlet.data.Status;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Emmanuel Duchastenier
 */
public class CommonResource extends ServerResource {

    private APISession sessionSingleton = null;

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonResource.class.getName());

    /**
     * Get the tenant session to access the engine APIs
     */
    public APISession getEngineSession() {
        if (sessionSingleton == null) {
            final HttpSession session = getHttpSession();
            sessionSingleton = (APISession) session.getAttribute("apiSession");
        }
        return sessionSingleton;
    }

    public HttpSession getHttpSession() {
        return getHttpRequest().getSession();
    }

    public HttpServletRequest getHttpRequest() {
        return ServletUtils.getRequest(getRequest());
    }

    protected Map<String, String> getSearchFilters() {
        return parseFilters(getParameterAsList(APIServletCall.PARAMETER_FILTER));
    }

    protected String getQueryParameter(final boolean mandatory) {
        return getParameter(APIServletCall.PARAMETER_QUERY, mandatory);
    }

    /**
     * Builds a map where keys are Engine constants defining filter keys, and values are values corresponding to those
     * keys.
     *
     * @param parameters The filters passed as string according to the form ["key1=value1", "key2=value2"].
     * @return a map of the form: [key1: value1, key2: value2].
     */
    protected Map<String, String> parseFilters(final List<String> parameters) {
        if (parameters == null) {
            return null;
        }
        final Map<String, String> results = new HashMap<>();
        for (final String parameter : parameters) {
            final String[] split = parameter.split("=");
            if (split.length < 2) {
                results.put(split[0], null);
            } else {
                results.put(split[0], parameter.substring(split[0].length() + 1));
            }
        }
        return results;
    }

    protected String getSearchOrder() {
        return getParameter(APIServletCall.PARAMETER_ORDER, false);
    }

    protected String getSearchTerm() {
        return getParameter(APIServletCall.PARAMETER_SEARCH, false);
    }

    public Integer getIntegerParameter(final String parameterName, final boolean mandatory) {
        final String parameterValue = getParameter(parameterName, mandatory);
        if (parameterValue != null) {
            return Integer.parseInt(parameterValue);
        }
        return null;
    }

    public Long getLongParameter(final String parameterName, final boolean mandatory) {
        final String parameterValue = getParameter(parameterName, mandatory);
        if (parameterValue != null) {
            return Long.parseLong(parameterValue);
        }
        return null;
    }

    public String getParameter(final String parameterName, final boolean mandatory) {
        final String parameter = getRequestParameter(parameterName);
        if (mandatory) {
            verifyNotNullParameter(parameter, parameterName);
        }
        return parameter;
    }

    protected String getRequestParameter(final String parameterName) {
        return getQueryValue(parameterName);
    }

    protected void verifyNotNullParameter(final Object parameter, final String parameterName) {
        if (parameter == null) {
            throw new IllegalArgumentException("Parameter " + parameterName + " is mandatory.");
        }
    }

    /**
     * Get a list of parameter values by name. If the parameter doesn't exist the result will be an empty list.
     *
     * @param name
     *        The name of the parameter (case sensitive).
     * @return The values of a parameter as a list of String.
     */
    public List<String> getParameterAsList(final String name) {
        return Arrays.asList(getQuery().getValuesArray(name));
    }

    public SearchOptions buildSearchOptions() {
        return new SearchOptionsCreator(getSearchPageNumber(), getSearchPageSize(), getSearchTerm(), new Sorts(
                getSearchOrder()), buildFilters()).create();
    }

    protected Filters buildFilters() {
        return new Filters(getSearchFilters());
    }

    @Override
    protected void doCatch(final Throwable throwable) {
        final Throwable t = throwable.getCause() != null ? throwable.getCause() : throwable;
        // Don't need to log the wrapping exception, the cause itself is more interesting:

        final ErrorMessage errorMessage = new ErrorMessage(t);
        final String message = "Error while querying REST resource " + getClass().getName() + " message: "
                + t.getMessage();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("***" + message);
        }
        Status status;
        if (t instanceof IllegalArgumentException || t instanceof JsonParseException) {
            status = Status.CLIENT_ERROR_BAD_REQUEST;
        } else if (t instanceof FileNotFoundException) {
            status = Status.CLIENT_ERROR_NOT_FOUND;
            errorMessage.setMessage("File Not Found");
        } else if (t instanceof NotFoundException) {
            status = Status.CLIENT_ERROR_NOT_FOUND;
        } else if (t instanceof InvalidSessionException) {
            status = Status.CLIENT_ERROR_UNAUTHORIZED;
            SessionUtil.sessionLogout(getHttpSession());
        } else {
            super.doCatch(t);
            status = getStatus();
        }
        if (getResponse() != null) {
            getResponse().setStatus(status, message);
            getResponse().setEntity(errorMessage.toEntity());
        }
    }

    @Override
    protected Representation doHandle(final Variant variant) throws ResourceException {
        setContentType(variant);
        // Used to ensure output is correctly encoded:
        variant.setCharacterSet(CharacterSet.UTF_8);
        return super.doHandle(variant);
    }

    protected void setContentType(final Variant variant) {
        // Set JSON as default content type (Resources methods returning a different content type need to override this method)
        variant.setMediaType(MediaType.APPLICATION_JSON);
    }

    @Override
    public String getAttribute(final String name) {
        final String attribute = super.getAttribute(name);
        return attribute != null ? URLDecoder.decode(attribute, StandardCharsets.UTF_8) : null;
    }

    public Long getPathParamAsLong(final String parameterName) {
        final String value = getAttribute(parameterName);
        return convertToLong(value);
    }

    private Long convertToLong(final String value) {
        try {
            return Long.parseLong(value);
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("[ " + value + " ] must be a number");
        }
    }

    public List<Long> getParameterAsLongList(final String parameterName) {
        final String values = getQuery().getValues(parameterName);
        if (values != null) {
            final String[] parameterValues = values.split(",");
            if (parameterValues != null && parameterValues.length > 0) {
                final List<Long> longValues = new ArrayList<>();
                for (final String parameterValue : parameterValues) {
                    longValues.add(convertToLong(parameterValue));
                }
                return longValues;
            }
        }
        return null;
    }

    public String getPathParam(final String name) {
        return getAttribute(name);
    }

    protected int getSearchPageNumber() {
        try {
            return getIntegerParameter(APIServletCall.PARAMETER_PAGE, true);
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("query parameter p (page) should be a number");
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException("query parameter p (page) is mandatory");
        }
    }

    protected int getSearchPageSize() {
        try {
            return getIntegerParameter(APIServletCall.PARAMETER_LIMIT, true);
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("query parameter c (count) should be a number");
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException("query parameter c (count) is mandatory");
        }
    }

    protected void setContentRange(final SearchResult<?> searchResult) {
        setContentRange(getSearchPageNumber(), getSearchPageSize(), searchResult.getCount());
    }

    protected void setContentRange(final int pageNumber, final int pageSize, final long count) {
        //This is mandatory as our API is not conform to the Content-range header specs
        getResponse().getEntity().setRange(new Range(pageNumber, pageSize - pageNumber + 1, count, ""));
    }

    protected void manageContractViolationException(final ContractViolationException e,
            final String statusErrorMessage) {
        if (LOGGER.isInfoEnabled()) {
            final StringBuilder explanations = new StringBuilder();
            for (final String explanation : e.getExplanations()) {
                explanations.append(explanation);
            }
            LOGGER.info(e.getSimpleMessage() + "\nExplanations:\n" + explanations);
        }
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, statusErrorMessage);
        final ErrorMessageWithExplanations errorMessage = new ErrorMessageWithExplanations(e);
        errorMessage.setMessage(e.getSimpleMessage());
        errorMessage.setExplanations(e.getExplanations());
        getResponse().setEntity(errorMessage.toEntity());
    }

}
