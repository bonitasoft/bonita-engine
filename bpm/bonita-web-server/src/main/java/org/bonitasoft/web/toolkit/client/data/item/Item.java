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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.web.toolkit.client.common.json.JSonSerializer;
import org.bonitasoft.web.toolkit.client.common.util.StringUtil;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ModifierEngine;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ValidatorEngine;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasDualDescription;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasDualName;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasUniqueId;
import org.bonitasoft.web.toolkit.client.ui.utils.DateFormat;

/**
 * @author Julien Mege, Séverin Moussel
 */
public abstract class Item implements IItem {

    public Item() {
        super();
    }

    public Item(final IItem item) {
        super();
        attributes.putAll(item.getAttributes());
    }

    @Override
    public List<String> getAPIIDOrder() {
        return getItemDefinition().getPrimaryKeys();
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DEFAULT FILTERS SUPERVISOR AND TEAM MANAGER
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final Map<String, String> attributes = new HashMap<>();

    private final Map<String, IItem> deploys = new HashMap<>();

    // private final Map<String, Long> counters = new HashMap<String, Long>();

    @Override
    public final void setId(final APIID id) {
        setAttribute(ItemHasUniqueId.ATTRIBUTE_ID, id);
    }

    @Override
    public APIID getId() {
        APIID apiid = null;
        final ItemDefinition<?> itemDefinition = getItemDefinition();

        if (this instanceof ItemHasUniqueId) {
            apiid = getAttributeValueAsAPIID(ItemHasUniqueId.ATTRIBUTE_ID);
        } else {

            final List<String> primaryKeysValues = new ArrayList<>();

            // Filling values
            final List<String> primaryKeys = itemDefinition.getPrimaryKeys();
            if (primaryKeys.isEmpty()) {
                primaryKeys.add(ItemHasUniqueId.ATTRIBUTE_ID);
            }

            for (final String key : primaryKeys) {
                primaryKeysValues.add(this.getAttributeValue(key));
            }

            apiid = APIID.makeAPIID(primaryKeysValues);
        }

        // Setting definition
        apiid.setItemDefinition(itemDefinition);

        return apiid;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DEFAULT BEHAVIOR
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static boolean applyOutputModifiersByDefault = true;

    private static boolean applyInputModifiersByDefault = true;

    private static boolean applyValidatorsByDefault = true;

    private static boolean applyValidatorMandatoryByDefault = true;

    private Boolean applyOutputModifiers = null;

    private Boolean applyInputModifiers = null;

    private Boolean applyValidators = null;

    private Boolean applyValidatorMandatory = null;

    /**
     * @param applyOutputModifiersByDefault
     *        the applyOutputModifiersByDefault to set
     */
    public static void setApplyOutputModifiersByDefault(final boolean applyOutputModifiersByDefault) {
        Item.applyOutputModifiersByDefault = applyOutputModifiersByDefault;
    }

    /**
     * @param applyInputModifiersByDefault
     *        the applyInputModifiersByDefault to set
     */
    public static void setApplyInputModifiersByDefault(final boolean applyInputModifiersByDefault) {
        Item.applyInputModifiersByDefault = applyInputModifiersByDefault;
    }

    /**
     * @param applyValidatorsByDefault
     *        the applyValidatorsByDefault to set
     */
    public static void setApplyValidatorsByDefault(final boolean applyValidatorsByDefault) {
        Item.applyValidatorsByDefault = applyValidatorsByDefault;
    }

    /**
     * @param applyValidatorMandatoryByDefault
     *        the applyValidatorMandatoryByDefault to set
     */
    public static void setApplyValidatorMandatoryByDefault(final boolean applyValidatorMandatoryByDefault) {
        Item.applyValidatorMandatoryByDefault = applyValidatorMandatoryByDefault;
    }

    /**
     * @param applyOutputModifiers
     *        the applyOutputModifiers to set
     */
    @Override
    public final void setApplyOutputModifiers(final boolean applyOutputModifiers) {
        this.applyOutputModifiers = applyOutputModifiers;
    }

    /**
     * @param applyInputModifiers
     *        the applyInputModifiers to set
     */
    @Override
    public final void setApplyInputModifiers(final boolean applyInputModifiers) {
        this.applyInputModifiers = applyInputModifiers;
    }

    /**
     * @param applyValidators
     *        the applyValidators to set
     */
    @Override
    public final void setApplyValidators(final boolean applyValidators) {
        this.applyValidators = applyValidators;
    }

    /**
     * @param applyValidatorMandatory
     *        the applyValidatorMandatory to set
     */
    @Override
    public final void setApplyValidatorMandatory(final boolean applyValidatorMandatory) {
        this.applyValidatorMandatory = applyValidatorMandatory;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SETTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Set an attribute value.
     * <p>
     * The attribute is consider as non existent before the first call of this function. The JSonItemReader fills it.
     *
     * @param name
     *        The name of the attribute. Must be the same as in the ItemDefinition.
     * @param value
     *        The value of the Item.
     */
    @Override
    public void setAttribute(final String name, final String value) {
        setAttribute(
                name,
                value,
                applyInputModifiers == null ? applyInputModifiersByDefault : applyInputModifiers,
                applyValidators == null ? applyValidatorsByDefault : applyValidators);
    }

    @Override
    public void setAttribute(final String name, final Object value) {
        this.setAttribute(name, value != null ? value.toString() : null);
    }

    /**
     * Set a Date attribute value.
     * <p>
     * The attribute is consider as non existent before the first call of this function. The JSonItemReader fills it.
     *
     * @param name
     *        The name of the attribute. Must be the same as in the ItemDefinition.
     * @param value
     *        The value of the Item.
     */
    @Override
    public final void setAttribute(final String name, final Date value) {
        this.setAttribute(name, DateFormat.dateToSql(value));
    }

    /**
     * Set an attribute value.
     * <p>
     * The attribute is consider as non existent before the first call of this function. The JSonItemReader fills it.
     *
     * @param name
     *        The name of the attribute. Must be the same as in the ItemDefinition.
     * @param value
     *        The value of the Item.
     */
    @Override
    public final void setAttribute(final String name, final APIID value) {
        if (value != null) {
            this.setAttribute(name, value.toString());
        } else {
            setAttribute(name, (String) null);
        }
    }

    /**
     * Set an attribute value.
     * <p>
     * The attribute is consider as non existent before the first call of this function. The JSonItemReader fills it.
     *
     * @param name
     *        The name of the attribute. Must be the same as in the ItemDefinition.
     * @param value
     *        The value of the Item.
     */
    @Override
    public final void setAttribute(final String name, final String value, final boolean applyModifiers,
            final boolean applyValidators) {
        final ItemAttribute attribute = getItemDefinition().getAttribute(name);

        String realValue = value;
        if (attribute != null && applyModifiers) {
            realValue = ModifierEngine.modify(realValue, attribute.getInputModifiers());
        }

        attributes.put(name, realValue);
        if (applyValidators) {
            ValidatorEngine.validate(this,
                    applyValidatorMandatory == null ? applyValidatorMandatoryByDefault : applyValidatorMandatory);
        }
    }

    /**
     * Set an attribute value.
     * <p>
     * The attribute is consider as non existent before the first call of this function. The JSonItemReader fills it.
     *
     * @param name
     *        The name of the attribute. Must be the same as in the ItemDefinition.
     * @param value
     *        The value of the Item.
     */
    @Override
    public final void setAttribute(final String name, final Object value, final boolean applyModifiers,
            final boolean applyValidators) {
        if (value != null) {
            setAttribute(name, value.toString(), applyModifiers, applyValidators);
        } else {
            setAttribute(name, (String) null, applyModifiers, applyValidators);
        }
    }

    /**
     * /**
     * Set an attribute value.
     * <p>
     * The attribute is consider as non existent before the first call of this function. The JSonItemReader fills it.
     *
     * @param name
     *        The name of the attribute. Must be the same as in the ItemDefinition.
     * @param value
     *        The value of the Item.
     */
    @Override
    public final void setAttribute(final String name, final APIID value, final boolean applyModifiers,
            final boolean applyValidators) {
        if (value != null) {
            setAttribute(name, value.toString(), applyModifiers, applyValidators);
        } else {
            setAttribute(name, (String) null, applyModifiers, applyValidators);
        }
    }

    /**
     * Set a Date attribute value.
     * <p>
     * The attribute is consider as non existent before the first call of this function. The JSonItemReader fills it.
     *
     * @param name
     *        The name of the attribute. Must be the same as in the ItemDefinition.
     * @param value
     *        The value of the Item.
     */
    @Override
    public final void setAttribute(final String name, final Date value, final boolean applyModifiers,
            final boolean applyValidators) {
        setAttribute(name, DateFormat.dateToSql(value), applyModifiers, applyValidators);
    }

    /**
     * Set a deployed version of an attribute
     *
     * @param attributeName
     *        The name of the attribute to deploy
     * @param item
     *        The deployed version of the attribute
     */
    @Override
    public void setDeploy(final String attributeName, final IItem item) {
        deploys.put(attributeName, item);
    }

    /**
     * Remove a deployed version of an attribute
     *
     * @param attributeName
     *        The name of the attribute deploy to remove
     */
    @Override
    public final void removeDeploy(final String attributeName) {
        deploys.remove(attributeName);
    }

    /**
     * Set a counter value.
     *
     * @param counterName
     *        The name of the counter to set
     * @param value
     *        The value of the counter
     */
    // public final void setCounterValue(final String counterName, final Long value) {
    // this.counters.put(counterName, value);
    // }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GETTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Indicate if there are no attribute defined.
     *
     * @return This methods returns TRUE if there are no attributes, otherwise FALSE.
     */
    @Override
    public final boolean isEmpty() {
        return attributes.isEmpty();
    }

    /**
     * Indicate if the attribute exists even if its value is NULL or empty.
     *
     * @param name
     *        The name of the attribute to check.
     * @return This method returns TRUE if the attribute exists, otherwise FALSE.
     */
    @Override
    public final boolean hasAttribute(final String name) {
        return attributes.containsKey(name);
    }

    /**
     * Get the value of an attribute
     *
     * @param attributeName
     *        The name of the attribute
     * @param applyModifiers
     *        Indicate whether or not the output modifiers defined for this resource need to be apply.
     * @return This function returns the value of the attribute or the defaultValue set.
     */
    @Override
    public final String getAttributeValue(final String attributeName, final boolean applyModifiers) {

        // Detect deploy called using "thisAttributeToDeployName.deployedItemAttributeName"
        final String[] splittedAttribute = attributeName.split("\\.");

        // Read a deployed attribute
        if (splittedAttribute.length == 2) {
            final IItem deploy = getDeploy(splittedAttribute[0]);

            return deploy.getAttributeValue(splittedAttribute[1]);

        }

        // Read an id from a deployed attribute
        else if (deploys.containsKey(attributeName)) {

            final IItem deploy = getDeploy(attributeName);

            if (deploy == null) {
                return null;
            }

            return deploy.getId().toString();
        }

        // Read a local attribute
        else {

            String realValue = attributes.get(attributeName);

            if (this instanceof ItemHasDualName) {
                if (ItemHasDualName.ATTRIBUTE_DISPLAY_NAME.equals(attributeName) && StringUtil.isBlank(realValue)) {
                    realValue = this.getAttributeValue(ItemHasDualName.ATTRIBUTE_NAME);
                }
            } else if (this instanceof ItemHasDualDescription) {
                if (ItemHasDualDescription.ATTRIBUTE_DISPLAY_DESCRIPTION.equals(attributeName)
                        && StringUtil.isBlank(realValue)) {
                    realValue = this.getAttributeValue(ItemHasDualDescription.ATTRIBUTE_DESCRIPTION);
                }
            }

            return realValue;
        }
    }

    /**
     * Get the value of an attribute
     *
     * @param attributeName
     *        The name of the attribute
     * @return This function returns the value of the attribute or NULL if not set.
     */
    @Override
    public String getAttributeValue(final String attributeName) {
        return this.getAttributeValue(attributeName,
                applyOutputModifiers == null ? applyOutputModifiersByDefault : applyOutputModifiers);
    }

    /**
     * Get the value of an attribute
     *
     * @param itemAttribute
     *        The attribute
     * @return This function returns the value of the attribute or NULL if not set.
     */
    @Override
    public final String getAttributeValue(final ItemAttribute itemAttribute) {
        return this.getAttributeValue(itemAttribute.getName(),
                applyOutputModifiers == null ? applyOutputModifiersByDefault : applyOutputModifiers);
    }

    /**
     * Get the value of an attribute
     *
     * @param itemAttribute
     *        The name of the attribute
     * @param applyModifiers
     *        Indicate whether or not the output modifiers defined for this resource need to be apply.
     * @return This function returns the value of the attribute or the defaultValue set.
     */
    @Override
    public final String getAttributeValue(final ItemAttribute itemAttribute, final boolean applyModifiers) {
        return this.getAttributeValue(itemAttribute.getName(), applyModifiers);
    }

    // AS APIID

    @Override
    public final APIID getAttributeValueAsAPIID(final String attributeName, final boolean applyModifiers) {
        return APIID.makeAPIID(getAttributeValue(attributeName, applyModifiers));
    }

    @Override
    public final APIID getAttributeValueAsAPIID(final String attributeName) {
        return APIID.makeAPIID(getAttributeValue(attributeName));
    }

    @Override
    public final APIID getAttributeValueAsAPIID(final ItemAttribute itemAttribute, final boolean applyModifiers) {
        return APIID.makeAPIID(getAttributeValue(itemAttribute, applyModifiers));
    }

    @Override
    public final APIID getAttributeValueAsAPIID(final ItemAttribute itemAttribute) {
        return APIID.makeAPIID(getAttributeValue(itemAttribute));
    }

    // AS Date

    @Override
    public final Date getAttributeValueAsDate(final String attributeName, final boolean applyModifiers) {
        return DateFormat.sqlToDate(getAttributeValue(attributeName, applyModifiers));
    }

    @Override
    public final Date getAttributeValueAsDate(final String attributeName) {
        return DateFormat.sqlToDate(getAttributeValue(attributeName));
    }

    @Override
    public final Date getAttributeValueAsDate(final ItemAttribute itemAttribute, final boolean applyModifiers) {
        return DateFormat.sqlToDate(getAttributeValue(itemAttribute, applyModifiers));
    }

    @Override
    public final Date getAttributeValueAsDate(final ItemAttribute itemAttribute) {
        return DateFormat.sqlToDate(getAttributeValue(itemAttribute));
    }

    // AS Long

    public final Long getAttributeValueAsLong(final String attributeName, final boolean applyModifiers) {
        return Long.valueOf(getAttributeValue(attributeName, applyModifiers));
    }

    public final Long getAttributeValueAsLong(final String attributeName) {
        return Long.valueOf(getAttributeValue(attributeName));
    }

    public final Long getAttributeValueAsLong(final ItemAttribute itemAttribute, final boolean applyModifiers) {
        return Long.valueOf(getAttributeValue(itemAttribute, applyModifiers));
    }

    public final Long getAttributeValueAsLong(final ItemAttribute itemAttribute) {
        return Long.valueOf(getAttributeValue(itemAttribute));
    }

    // Get all

    @Override
    public final Map<String, String> getAttributes() {
        return this.getAttributes(applyOutputModifiers == null ? applyOutputModifiersByDefault : applyOutputModifiers);
    }

    @Override
    public final Map<String, String> getAttributes(final boolean applyModifiers) {
        final Map<String, String> results = new HashMap<>();

        for (final String attributeName : attributes.keySet()) {
            results.put(attributeName, this.getAttributeValue(attributeName, applyModifiers));
        }

        return results;
    }

    /**
     * Get a deployed version of an attribute
     *
     * @param attributeName
     *        The name of the attribute to deploy
     * @return This method returns the deployed version of an attribute if it's available, otherwise NULL.
     */
    @Override
    public final IItem getDeploy(final String attributeName) {
        // TODO If not deployed, automatically call the API to deploy.

        return deploys.get(attributeName);
    }

    @Override
    public Map<String, IItem> getDeploys() {
        return deploys;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ATTRIBUTES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public final ArrayList<String> getAttributeNames() {
        return new ArrayList<>(attributes.keySet());
    }

    @Override
    public final void setAttributes(final Map<String, String> attributes, final boolean applyModifiers,
            final boolean applyValidators) {
        if (attributes == null || attributes.size() == 0) {
            return;
        }

        for (final String attributeName : attributes.keySet()) {
            setAttribute(attributeName, attributes.get(attributeName), applyModifiers, false);
        }

        if (applyValidators) {
            ValidatorEngine.validate(this);
        }
    }

    @Override
    public final void setAttributes(final Map<String, String> attributes) {
        setAttributes(
                attributes,
                applyInputModifiers == null ? applyInputModifiersByDefault : applyInputModifiers,
                applyValidators == null ? applyValidatorsByDefault : applyValidators);
    }

    /**
     * Get the definition of an Item
     * <p>
     * This function must be overridden to return the definition corresponding to the Item type.b
     *
     * @return This function return an instance of ItemDefinition for the current Item type
     */
    @Override
    abstract public ItemDefinition<?> getItemDefinition();

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CONVERT
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final String key : attributes.keySet()) {
            final String rawValue = attributes.get(key);
            final String cleanValue = this.getAttributeValue(key);

            sb.append(key).append(" : ").append(rawValue);

            if (rawValue != null && !rawValue.equals(cleanValue)) {
                sb.append(" >> ").append(cleanValue);
            }
            sb.append("\r\n");
        }

        for (final Entry<String, IItem> entry : deploys.entrySet()) {
            sb.append(entry.getKey()).append(" : ").append(entry.getValue());
            sb.append("\r\n");
        }
        return sb.toString();
    }

    @Override
    public final String toJson() {

        final StringBuilder json = new StringBuilder().append("{");

        boolean first = true;
        for (final String attribute : getAttributeNames()) {
            if (deploys.containsKey(attribute)) {
                json.append(!first ? "," : "").append(JSonSerializer.quote(attribute)).append(":")
                        .append(JSonSerializer.serialize(deploys.get(attribute)));
            } else {
                json.append(!first ? "," : "").append(JSonSerializer.quote(attribute)).append(":")
                        .append(JSonSerializer.quote(this.getAttributeValue(attribute)));
            }
            first = false;
        }

        json.append("}");

        return json.toString();

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (applyInputModifiers == null ? 0 : applyInputModifiers.hashCode());
        result = prime * result + (applyOutputModifiers == null ? 0 : applyOutputModifiers.hashCode());
        result = prime * result + (applyValidatorMandatory == null ? 0 : applyValidatorMandatory.hashCode());
        result = prime * result + (applyValidators == null ? 0 : applyValidators.hashCode());
        result = prime * result + (attributes == null ? 0 : attributes.hashCode());
        result = prime * result + (deploys == null ? 0 : deploys.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Item other = (Item) obj;
        if (applyInputModifiers == null) {
            if (other.applyInputModifiers != null) {
                return false;
            }
        } else if (!applyInputModifiers.equals(other.applyInputModifiers)) {
            return false;
        }
        if (applyOutputModifiers == null) {
            if (other.applyOutputModifiers != null) {
                return false;
            }
        } else if (!applyOutputModifiers.equals(other.applyOutputModifiers)) {
            return false;
        }
        if (applyValidatorMandatory == null) {
            if (other.applyValidatorMandatory != null) {
                return false;
            }
        } else if (!applyValidatorMandatory.equals(other.applyValidatorMandatory)) {
            return false;
        }
        if (applyValidators == null) {
            if (other.applyValidators != null) {
                return false;
            }
        } else if (!applyValidators.equals(other.applyValidators)) {
            return false;
        }
        if (attributes == null) {
            if (other.attributes != null) {
                return false;
            }
        } else if (!attributes.equals(other.attributes)) {
            return false;
        }
        if (deploys == null) {
            return other.deploys == null;
        } else {
            return deploys.equals(other.deploys);
        }
    }

}
