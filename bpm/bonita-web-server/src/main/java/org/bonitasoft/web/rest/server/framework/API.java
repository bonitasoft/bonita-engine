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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.bonitasoft.web.rest.server.framework.api.Datastore;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasAdd;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasDelete;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasGet;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasSearch;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasUpdate;
import org.bonitasoft.web.rest.server.framework.exception.APIFileUploadNotFoundException;
import org.bonitasoft.web.rest.server.framework.exception.ForbiddenAttributesException;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIMethodNotAllowedException;
import org.bonitasoft.web.toolkit.client.common.util.MapUtil;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Séverin Moussel
 */
public abstract class API<ITEM extends IItem> {

    protected ItemDefinition<ITEM> itemDefinition = null;

    private final Map<String, Deployer> deployers = new HashMap<>();

    private static Logger LOGGER = LoggerFactory.getLogger(API.class.getName());

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public API() {
        this.itemDefinition = defineItemDefinition();
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SETTERS AND GETTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * The ServletCall responsible of this service.
     */
    private APIServletCall caller = null;

    /**
     * Set the caller.
     *
     * @param caller
     *        The ServletCall responsible of this service.
     */
    public final void setCaller(final APIServletCall caller) {
        this.caller = caller;
    }

    /**
     * @return the itemDefinition
     */
    public final ItemDefinition<ITEM> getItemDefinition() {
        return this.itemDefinition;
    }

    /**
     * Define the ItemDefinition for current class
     */
    protected ItemDefinition<ITEM> defineItemDefinition() {
        // FIXME [API V2] Make this method abstract after suppression of API V1
        return null;
    }

    /**
     * @see org.bonitasoft.web.toolkit.server.ServletCall#getHttpSession()
     */
    protected final HttpSession getHttpSession() {
        return this.caller.getHttpSession();
    }

    protected String getLocale() {
        return this.caller.getLocale();
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ENTRY POINTS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    public ITEM runAdd(final IItem item) {
        // FIXME Activate at end of APIs refactoring
        // if (!(this instanceof APIHasAdd)) {
        // throw new APIMethodNotAllowedException("POST method not allowed.");
        // }

        // Stop there if forbidden attributes are set
        checkForbiddenAttributes(item.getAttributes());

        // Run specific implementation
        return add((ITEM) item);
    }

    @SuppressWarnings("unchecked")
    public ITEM add(final ITEM item) {
        final Datastore datastore = getDefaultDatastore();

        if (!(datastore instanceof DatastoreHasAdd<?>)) {
            throw new APIMethodNotAllowedException("POST method not allowed.");
        }

        return ((DatastoreHasAdd<ITEM>) datastore).add(item);
    }

    public ITEM runUpdate(final APIID id, final Map<String, String> attributes) {
        // FIXME Activate at end of APIs refactoring
        // if (!(this instanceof APIHasUpdate)) {
        // throw new APIMethodNotAllowedException("PUT method not allowed.");
        // }

        id.setItemDefinition(getItemDefinition());

        // Stop there if forbidden attributes are set
        checkForbiddenAttributes(attributes);

        // Run specific implementation
        return this.update(id, attributes);
    }

    @SuppressWarnings("unchecked")
    public ITEM update(final APIID id, final Map<String, String> attributes) {
        final Datastore datastore = getDefaultDatastore();

        if (datastore == null || !(datastore instanceof DatastoreHasUpdate<?>)) {
            throw new APIMethodNotAllowedException("PUT method not allowed.");
        }

        return ((DatastoreHasUpdate<ITEM>) datastore).update(id, attributes);

    }

    public ITEM runGet(final APIID id, final List<String> deploys, final List<String> counters) {
        // FIXME Activate at end of APIs refactoring
        // if (!(this instanceof APIHasGet)) {
        // throw new APIMethodNotAllowedException("GET method not allowed.");
        // }

        id.setItemDefinition(getItemDefinition());

        final ITEM item = get(id);
        if (item == null) {
            throw new APIItemNotFoundException(getItemDefinition().getToken(), id);
        }

        fillDeploys(item, deploys != null ? deploys : new ArrayList<>());
        fillCounters(item, counters != null ? counters : new ArrayList<>());

        return item;
    }

    @SuppressWarnings("unchecked")
    public ITEM get(final APIID id) {
        final Datastore datastore = getDefaultDatastore();

        if (datastore == null || !(datastore instanceof DatastoreHasGet<?>)) {
            throw new APIMethodNotAllowedException("GET method not allowed.");
        }

        return ((DatastoreHasGet<ITEM>) datastore).get(id);
    }

    public ItemSearchResult<ITEM> runSearch(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters, final List<String> deploys, final List<String> counters) {

        // FIXME Activate at end of APIs refactoring
        // if (!(this instanceof APIHasSearch)) {
        // throw new APIMethodNotAllowedException("SEARCH method not allowed.");
        // }

        String realOrders = orders;
        if (orders == null || orders.length() == 0) {
            realOrders = defineDefaultSearchOrder();

            // TODO remove this test and exception while the automated unit test over all APis
            if (realOrders == null) {
                throw new APIException(
                        "No default search order defined. Please, override the defineDefaultSearchOrder method in "
                                + this.getClass().toString()
                                + ".");
            }
        }

        final ItemSearchResult<ITEM> searchResult = search(page, resultsByPage, search, realOrders,
                filters != null ? filters : new HashMap<>());

        for (final ITEM item : searchResult.getResults()) {
            fillDeploys(item, deploys != null ? deploys : new ArrayList<>());
            fillCounters(item, counters != null ? counters : new ArrayList<>());
        }

        return searchResult;
    }

    @SuppressWarnings("unchecked")
    public ItemSearchResult<ITEM> search(final int page, final int resultsByPage, final String search,
            final String orders, final Map<String, String> filters) {

        final Datastore datastore = getDefaultDatastore();

        if (datastore == null || !(datastore instanceof DatastoreHasSearch<?>)) {
            throw new APIMethodNotAllowedException("SEARCH method not allowed.");
        }

        return ((DatastoreHasSearch<ITEM>) datastore).search(page, resultsByPage, search, orders, filters);
    }

    /**
     * Define the default search order.
     */
    public String defineDefaultSearchOrder() {
        return null;
    }

    public void runDelete(final List<APIID> ids) {
        // FIXME Activate at end of APIs refactoring
        // if (!(this instanceof APIHasDelete)) {
        // throw new APIMethodNotAllowedException("DELETE method not allowed.");
        // }

        for (final APIID id : ids) {
            id.setItemDefinition(getItemDefinition());
        }

        delete(ids);
    }

    public void delete(final List<APIID> ids) {

        final Datastore datastore = getDefaultDatastore();

        if (datastore == null || !(datastore instanceof DatastoreHasDelete)) {
            throw new APIMethodNotAllowedException("DELETE method not allowed.");
        }

        ((DatastoreHasDelete) datastore).delete(ids);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // AUTOMATED CRUDS BASED ON INTERFACES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected Datastore defineDefaultDatastore() {
        return null;
    }

    public final Datastore getDefaultDatastore() {
        return this.defineDefaultDatastore();
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // PARAMETERS TOOLS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected final File getUploadedFile(final String attributeName, final String attributeValue) throws IOException {
        if (attributeValue == null || attributeValue.isEmpty()) {
            return null;
        }

        final String tmpIconPath = getCompleteTempFilePath(attributeValue);

        final File file = new File(tmpIconPath);
        if (!file.exists()) {
            throw new APIFileUploadNotFoundException(attributeName, attributeValue);
        }
        return file;
    }

    abstract protected String getCompleteTempFilePath(String path) throws IOException;

    /**
     * Rename and upload the file to the defined directory.
     *
     * @param attributeName
     *        The name of the attribute representing the file.
     * @param attributeValue
     *        The value of the attribute representing the file.
     * @param newDirectory
     *        The destination directory path.
     * @param newName
     *        The name to set to the file without the extension (the original extension will be kept)
     * @return This method return the file in the destination directory.
     * @throws IOException
     */
    protected final File upload(final String attributeName, final String attributeValue, final String newDirectory,
            final String newName) throws IOException {

        // Check if the destination directory already exists. If not, creates it.
        final File destinationDirectory = new File(newDirectory);
        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdir();
        }

        // Construct the destination fileName
        final File file = getUploadedFile(attributeName, attributeValue);
        String destinationName = file.getName();
        if (newName != null) {
            destinationName = newName + getFileExtension(file.getName());
        }

        // Move the file
        final File destinationFile = new File(
                destinationDirectory.getAbsolutePath() + File.separator + destinationName);
        try {
            destinationFile.delete();
            Files.move(file.toPath(), destinationFile.toPath());
        } catch (final Exception e) {
            e.getMessage();
        }

        return destinationFile;
    }

    /**
     * @param fileName
     * @return
     */
    private String getFileExtension(final String fileName) {
        String extension = "";
        final int dotPos = fileName.lastIndexOf('.');
        if (dotPos >= 0) {
            extension = fileName.substring(dotPos);
        }
        return extension;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DEPLOYS AND COUNTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void addDeployer(final Deployer deployer) {
        deployers.put(deployer.getDeployedAttribute(), deployer);
    }

    public Map<String, Deployer> getDeployers() {
        return Collections.unmodifiableMap(deployers);
    }

    protected void fillDeploys(final ITEM item, final List<String> deploys) {
        for (final String attribute : deploys) {
            deployAttribute(attribute, item);
        }
    }

    private void deployAttribute(final String attribute, final ITEM item) {
        if (deployers.containsKey(attribute)) {
            try {
                deployers.get(attribute).deployIn(item);
            } catch (final Exception e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(getFailedDeployMessage(attribute, item), e);
                } else if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(getFailedDeployMessage(attribute, item));
                }
            }
        }
    }

    protected String getFailedDeployMessage(final String attribute, final ITEM item) {
        return "Could not deploy attribute '" + attribute + "' on item " + item.toString();
    }

    protected void fillCounters(final ITEM item, final List<String> counters) {
        // Do Nothing if not override
    }

    /**
     * @param attributeName
     * @param deploys
     * @param item
     */
    protected final boolean isDeployable(final String attributeName, final List<String> deploys, final IItem item) {
        final String attributeValue = item.getAttributeValue(attributeName);

        if (deploys.contains(attributeName) && attributeValue != null && !attributeValue.isEmpty()) {
            try {
                final long longAttrValue = Long.parseLong(attributeValue);
                //only positive numeric Ids are supported
                return longAttrValue > 0L;
            } catch (final NumberFormatException e) {
                //non numeric Id are supported
                return true;
            }
        }
        return false;

    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UPLOADS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FORBIDDEN ATTRIBUTES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Override this method to define attributes that are not allowed to be set manually during ADD or UPDATE.
     *
     * @return This method must returns a List of forbidden attributes' name.
     */
    protected List<String> defineReadOnlyAttributes() {
        return null;
    }

    private void checkForbiddenAttributes(final Map<String, String> attributes) {

        // List forbidden attributes
        final List<String> forbiddenAttributes = new ArrayList<>();
        final List<String> definedForbiddenAttributes = defineReadOnlyAttributes();
        if (definedForbiddenAttributes != null) {
            forbiddenAttributes.addAll(definedForbiddenAttributes);
        }

        // No forbidden attributes defined, no need to go further in the check process.
        if (forbiddenAttributes.size() == 0) {
            return;
        }

        // List forbidden attributes found in the request
        final List<String> errorAttributes = new ArrayList<>();
        for (final String forbiddenAttribute : forbiddenAttributes) {
            if (!MapUtil.isBlank(attributes, forbiddenAttribute)) {
                errorAttributes.add(forbiddenAttribute);
            }
        }

        // If at least one is found, throw a ForbiddenAttributesException
        if (errorAttributes.size() > 0) {
            throw new ForbiddenAttributesException(errorAttributes);
        }
    }

}
