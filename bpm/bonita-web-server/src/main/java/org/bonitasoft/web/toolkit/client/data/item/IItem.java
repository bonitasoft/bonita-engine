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
import java.util.List;
import java.util.Map;

import org.bonitasoft.web.toolkit.client.common.json.JsonSerializable;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;

/**
 * @author Séverin Moussel
 */
public interface IItem extends JsonSerializable {

    List<String> getAPIIDOrder();

    void setId(final APIID id);

    APIID getId();

    /**
     * @param applyOutputModifiers
     *        the applyOutputModifiers to set
     */
    void setApplyOutputModifiers(final boolean applyOutputModifiers);

    /**
     * @param applyInputModifiers
     *        the applyInputModifiers to set
     */
    void setApplyInputModifiers(final boolean applyInputModifiers);

    /**
     * @param applyValidators
     *        the applyValidators to set
     */
    void setApplyValidators(final boolean applyValidators);

    /**
     * @param applyValidatorMandatory
     *        the applyValidatorMandatory to set
     */
    void setApplyValidatorMandatory(final boolean applyValidatorMandatory);

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
    void setAttribute(final String name, final String value);

    void setAttribute(final String name, final Object value);

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
    void setAttribute(final String name, final Date value);

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
    void setAttribute(final String name, final APIID value);

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
    void setAttribute(final String name, final String value, final boolean applyModifiers,
            final boolean applyValidators);

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
    void setAttribute(final String name, final Object value, final boolean applyModifiers,
            final boolean applyValidators);

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
    void setAttribute(final String name, final APIID value, final boolean applyModifiers,
            final boolean applyValidators);

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
    void setAttribute(final String name, final Date value, final boolean applyModifiers, final boolean applyValidators);

    /**
     * Set a deployed version of an attribute
     *
     * @param attributeName
     *        The name of the attribute to deploy
     * @param item
     *        The deployed version of the attribute
     */
    void setDeploy(final String attributeName, final IItem item);

    /**
     * Remove a deployed version of an attribute
     *
     * @param attributeName
     *        The name of the attribute deploy to remove
     */
    void removeDeploy(final String attributeName);

    /**
     * Indicate if there are no attribute defined.
     *
     * @return This methods returns TRUE if there are no attributes, otherwise FALSE.
     */
    boolean isEmpty();

    /**
     * Indicate if the attribute exists even if its value is NULL or empty.
     *
     * @param name
     *        The name of the attribute to check.
     * @return This method returns TRUE if the attribute exists, otherwise FALSE.
     */
    boolean hasAttribute(final String name);

    /**
     * Get the value of an attribute
     *
     * @param attributeName
     *        The name of the attribute
     * @param applyModifiers
     *        Indicate whether or not the output modifiers defined for this resource need to be apply.
     * @return This function returns the value of the attribute or the defaultValue set.
     */
    String getAttributeValue(final String attributeName, final boolean applyModifiers);

    /**
     * Get the value of an attribute
     *
     * @param attributeName
     *        The name of the attribute
     * @return This function returns the value of the attribute or NULL if not set.
     */
    String getAttributeValue(final String attributeName);

    /**
     * Get the value of an attribute
     *
     * @param itemAttribute
     *        The attribute
     * @return This function returns the value of the attribute or NULL if not set.
     */
    String getAttributeValue(final ItemAttribute itemAttribute);

    /**
     * Get the value of an attribute
     *
     * @param itemAttribute
     *        The name of the attribute
     * @param applyModifiers
     *        Indicate whether or not the output modifiers defined for this resource need to be apply.
     * @return This function returns the value of the attribute or the defaultValue set.
     */
    String getAttributeValue(final ItemAttribute itemAttribute, final boolean applyModifiers);

    APIID getAttributeValueAsAPIID(final String attributeName, final boolean applyModifiers);

    APIID getAttributeValueAsAPIID(final String attributeName);

    APIID getAttributeValueAsAPIID(final ItemAttribute itemAttribute, final boolean applyModifiers);

    APIID getAttributeValueAsAPIID(final ItemAttribute itemAttribute);

    Date getAttributeValueAsDate(final String attributeName, final boolean applyModifiers);

    Date getAttributeValueAsDate(final String attributeName);

    Date getAttributeValueAsDate(final ItemAttribute itemAttribute, final boolean applyModifiers);

    Date getAttributeValueAsDate(final ItemAttribute itemAttribute);

    Map<String, String> getAttributes();

    Map<String, String> getAttributes(final boolean applyModifiers);

    /**
     * Get a deployed version of an attribute
     *
     * @param attributeName
     *        The name of the attribute to deploy
     * @return This method returns the deployed version of an attribute if it's available, otherwise NULL.
     */
    IItem getDeploy(final String attributeName);

    ArrayList<String> getAttributeNames();

    void setAttributes(final Map<String, String> attributes, final boolean applyModifiers,
            final boolean applyValidators);

    void setAttributes(final Map<String, String> attributes);

    /**
     * Get the definition of an Item
     * <p>
     * This function must be overridden to return the definition corresponding to the IItem type.b
     *
     * @return This function return an instance of ItemDefinition for the current IItem type
     */
    ItemDefinition<?> getItemDefinition();

    @Override
    String toString();

    Map<String, IItem> getDeploys();

}
