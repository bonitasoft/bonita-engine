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
package org.bonitasoft.web.toolkit.client.data.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ValidationException;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.Validator;

/**
 * This class is the super class of all Items definitions.
 * <p>
 * To define a new IItem type, just create a class that extends ItemDefinition.<br>
 * Then, you will have to override the function beginning with <i>define....</i> and in each overridden functions, you
 * will call the corresponding setters.
 * <p>
 * There are some functions for which the override is not mandatory but you can override any define function.
 * <p>
 * <b>It's highly recommended to define attributes names using <code>public static final</code> strings</b>
 *
 * @author Séverin Moussel
 */
public abstract class ItemDefinition<E extends IItem> {

    public ItemDefinition() {
        setToken(defineToken());
        defineAttributes();
        setAPIUrl(defineAPIUrl());
        definePrimaryKeys();
        defineDeploys();
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////////
    // Token
    // ///////////////////////////////////////////////////////////////////////////////////////////////////

    private String token = null;

    /**
     * This function must be override to define the token to use to access to the current view.
     */
    protected abstract String defineToken();

    /**
     * @param token
     *        the token to set
     */
    public final void setToken(final String token) {
        this.token = token;
    }

    /**
     * @return the token
     */
    public final String getToken() {
        return this.token;
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////////
    // SINGULAR RESOURCE URL
    // ///////////////////////////////////////////////////////////////////////////////////////////////////

    private String APIUrl = null;

    public final String getAPIUrl() {
        return this.APIUrl;
    }

    protected final void setAPIUrl(final String url) {
        this.APIUrl = url;
    }

    /**
     * This function must be overridden to define the singular version of the resource URL.
     * <p>
     * This function must call this.setSingularResourceUrl(...)
     * <p>
     * <b>Example</b> :<br>
     * <code>this.setUrl("/API/organization/user");</code>
     */
    protected abstract String defineAPIUrl();

    // ///////////////////////////////////////////////////////////////////////////////////////////////////
    // ATTRIBUTES
    // ///////////////////////////////////////////////////////////////////////////////////////////////////

    private final LinkedHashMap<String, ItemAttribute> attributes = new LinkedHashMap<>();

    private final ArrayList<String> primaryKeys = new ArrayList<>();

    /**
     * Create and save a new attribute for the current item type.
     * <p>
     * <b>Example :</b><br>
     * <code>final ItemAttribute firstName = this.createAttribute(this.FIRSTNAME, ItemAttribute.TYPE.STRING);</code>
     *
     * @param name
     *        The name of the attribute
     * @param type
     *        The type of the attribute. Must be one of ItemAttribute.TYPE.xxx
     * @return This function returns the Attribute created to allow to add other details on it.
     */
    public ItemAttribute createAttribute(final String name, final ItemAttribute.TYPE type) {
        final ItemAttribute attribute = new ItemAttribute(name, type);
        this.attributes.put(name, attribute);
        return attribute;
    }

    /**
     * This function must be overridden to define the attributes of an Item.
     * <p>
     * Defining attributes is made by calling several <i>createAttribute()</i>
     * <p>
     * <b>Example :</b><br>
     * <code>
     * final ItemAttribute firstName = this.createAttribute(this.FIRSTNAME, ItemAttribute.TYPE.STRING);<br>
     * firstName.setLabel("Firstname");<br>
     * firstName.setTooltip("Enter your firstname");<br>
     * firstName.setTableSortable(true);<br>
     * firstName.setFormEditable(true);<br>
     * <br>
     * final ItemAttribute firstName = this.createAttribute(this.LASTNAME, ItemAttribute.TYPE.STRING);<br>
     * firstName.setLabel("Lastname");<br>
     * firstName.setTooltip("Enter your lastname");<br>
     * firstName.setTableSortable(true);<br>
     * firstName.setFormEditable(true);<br>
     * ...
     * </code>
     */
    protected abstract void defineAttributes();

    public final ArrayList<ItemAttribute> getAttributes() {
        return new ArrayList<>(this.attributes.values());
    }

    protected abstract void definePrimaryKeys();

    protected final void setPrimaryKeys(final String... primaryKeys) {
        this.primaryKeys.clear();
        for (final String key : primaryKeys) {
            this.primaryKeys.add(key);

            // Create the attribute if it's missing
            if (!this.attributes.containsKey(key)) {
                createAttribute(key, ItemAttribute.TYPE.ITEM_ID);
            }
        }
    }

    public final ArrayList<String> getPrimaryKeys() {
        return this.primaryKeys;
    }

    /**
     * Retrieve an attribute by its name.
     *
     * @param name
     *        The name of the attribute
     * @return This function returns the attribute or null if there is no attribute with the defined name.
     */
    public final ItemAttribute getAttribute(final String name) {
        return this.attributes.get(name);
    }

    public final boolean containsAttribute(final String attributeName) {
        return this.attributes.containsKey(attributeName);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DEPLOYS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final Map<String, ItemDefinition<?>> deploys = new HashMap<>();

    protected void defineDeploys() {
        // No deploys by default
    }

    /**
     * Declare an attribute as deployable.
     *
     * @param attributeName
     *        The name of the deployable attribute.
     * @param definition
     *        The ItemDefinition of the item contained in the deployed attribute.
     */
    protected final void declareDeployable(final String attributeName, final ItemDefinition<?> definition) {
        this.deploys.put(attributeName, definition);
    }

    public final ItemDefinition<?> getDeployDefinition(final String attributeName) {
        return this.deploys.containsKey(attributeName)
                ? this.deploys.get(attributeName)
                : Definitions.get(DummyItemDefinition.TOKEN);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // VALIDATORS AND MODIFIERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Get the validators in a map <attribute name, list of validators>
     */
    public final Map<String, List<Validator>> getValidators() {
        final Map<String, List<Validator>> validators = new HashMap<>();
        for (final ItemAttribute attribute : getAttributes()) {
            validators.put(attribute.getName(), attribute.getValidators());
        }
        return validators;
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////////
    // AUTOMATICALY INSTANCIATE ITEM AND ITEM DEPENDENT TOOLS
    // ///////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This helps Items definitions which needs an attribute value to determine their concrete implementation.
     *
     * @return helper for discriminating and find the concrete implementation
     */
    public Optional<DiscriminatedItemDefinitionHelper<E>> getDiscriminatedHelper() {
        return Optional.empty();
    }

    /**
     * This function create a new empty Item
     *
     * @return This function returns the new Item
     */
    abstract protected E _createItem();

    public final E createItem() {
        try {
            return this.createItem((Map<String, String>) null);
        } catch (final ValidationException e) {
            // DO NOTHING : this can't fail due to validation because validation is disabled.
            return null;
        }
    }

    public final E createItem(final Map<String, String> attributes) throws ValidationException {
        final Optional<E> discriminatedItem = getDiscriminatedHelper().map(h -> h.findItemCreator(attributes).get());
        final E item = discriminatedItem.orElseGet(this::_createItem);

        if (attributes != null) {
            item.setAttributes(attributes);
        }

        return item;
    }

    public final E createItem(IItem sourceItem) {
        return sourceItem == null ? null : createItem(sourceItem.getAttributes());
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // MAKE APIID
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public final APIID makeAPIID(final String... id) {
        return makeAPIID(Arrays.asList(id));
    }

    public final APIID makeAPIID(final Long... ids) {
        final APIID apiid = APIID.makeAPIID(ids);
        apiid.setItemDefinition(this);
        return apiid;
    }

    public final APIID makeAPIID(final List<String> ids) {
        final APIID apiid = APIID.makeAPIID(ids);
        apiid.setItemDefinition(this);
        return apiid;
    }

}
