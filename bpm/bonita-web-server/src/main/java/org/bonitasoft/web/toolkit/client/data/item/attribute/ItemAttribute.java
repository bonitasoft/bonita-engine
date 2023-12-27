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
package org.bonitasoft.web.toolkit.client.data.item.attribute;

import java.util.List;

import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.attribute.modifier.DefaultValueModifier;
import org.bonitasoft.web.toolkit.client.data.item.attribute.modifier.Modifier;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.FileIsImageValidator;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.FileIsNoScript;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.IsBooleanValidator;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.IsIntegerValidator;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.IsNumericValidator;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.MandatoryValidator;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.StringFormatColorValidator;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.StringFormatEmailValidator;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.StringFormatURLValidator;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.StringMaxLengthValidator;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.StringSingleLineValidator;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.Validator;

/**
 * This class represents an attribute of an {@link Item}
 *
 * @author Séverin Moussel
 */
public final class ItemAttribute implements Validable, ModifiableInput {

    /**
     * The maximum length of a color string (#FDB975).
     */
    public static final int MAX_LENGTH_COLOR = 7;

    /**
     * The maximum length of a simple String (varchar in database).
     */
    public static final int MAX_LENGTH_STRING = 255;

    /**
     * The maximum length of a text string (text in database).
     */
    public static final int MAX_LENGTH_TEXT = 2000;

    /**
     * The maximum length of a URL
     */
    public static final int MAX_LENGTH_URL = 1024;

    /**
     * The type of value an attribute can get.
     *
     * @author Séverin Moussel
     */
    public enum TYPE {
        /**
         * A single line String
         * <ul>
         * <li>Validate the string is a single line</li>
         * <li>Validate max length of {@value #MAX_LENGTH_STRING}</li>
         * </ul>
         */
        STRING,

        /**
         * A multiline String
         * <ul>
         * <li>Validate max length of {@value #MAX_LENGTH_TEXT}</li>
         * </ul>
         */
        TEXT,

        /**
         * An image path.
         * <ul>
         * <li>Validate the extension of the file is a valid image</li>
         * <li>Validate max length of {@value #MAX_LENGTH_STRING}</li>
         * </ul>
         */
        IMAGE,

        PASSWORD, FILE, BOOLEAN, ENUM, NUMERIC, INTEGER, DATE, TIME, DATETIME, COLOR,

        /**
         * An email address.
         * <ul>
         * <li>Validate the email format xxx@xxx.xxx</li>
         * <li>Validate max length of 255</li>
         * </ul>
         */
        EMAIL,

        /**
         * A URL
         * <ul>
         * <li>Validate max length of {@value #MAX_LENGTH_URL}</li>
         * </ul>
         */
        URL,

        ITEM_ID
    }

    private final String name;

    private final TYPE type;

    private final String defaultValue = null;

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS + INIT
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Default constructor
     *
     * @param name
     *        The name of the DataSource variable
     * @param type
     *        The type of the attribute datas choosen in ItemAttribute.TYPE_XXX
     */
    public ItemAttribute(final String name, final TYPE type) {
        this.name = name;
        this.type = type;
        initType();
    }

    /**
     * Define the default validators using the provided type
     */
    private void initType() {
        switch (this.type) {
            case PASSWORD:
            case STRING:
                // Because Text inputs are not multiline
                addValidator(new StringSingleLineValidator());
                // Because databases varchar are limited to 255
                addValidator(new StringMaxLengthValidator(MAX_LENGTH_STRING));
                break;
            case IMAGE:
                addValidator(new FileIsImageValidator());
                addValidator(new StringMaxLengthValidator(MAX_LENGTH_STRING));
                break;
            case NUMERIC:
                addValidator(new IsNumericValidator());
                break;
            case INTEGER:
                addValidator(new IsIntegerValidator());
                break;
            case TEXT:
                addValidator(new StringMaxLengthValidator(MAX_LENGTH_TEXT));
                break;
            case BOOLEAN:
                addValidator(new IsBooleanValidator());
                break;
            case FILE:
                addValidator(new FileIsNoScript());
                addValidator(new StringMaxLengthValidator(MAX_LENGTH_STRING));
                break;
            case ENUM:
                break;
            case EMAIL:
                addValidator(new StringFormatEmailValidator());
                addValidator(new StringMaxLengthValidator(MAX_LENGTH_STRING));
                break;
            case URL:
                addValidator(new StringFormatURLValidator());
                addValidator(new StringMaxLengthValidator(MAX_LENGTH_URL));
                break;
            case COLOR:
                addValidator(new StringFormatColorValidator());
                addValidator(new StringMaxLengthValidator(MAX_LENGTH_COLOR));
                break;
            default:
                addValidator(new StringMaxLengthValidator(MAX_LENGTH_URL));
                break;
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GETTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String getName() {
        return this.name;
    }

    public TYPE getType() {
        return this.type;
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    // // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // VALIDATORS AND MODIFIERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final ModifiersList inputModifiers = new ModifiersList();

    private final ValidatorsList validators = new ValidatorsList();

    /**
     * @see org.bonitasoft.web.toolkit.client.data.item.attribute.ModifiersList#getModifiers()
     */
    @Override
    public List<Modifier> getInputModifiers() {
        return this.inputModifiers.getModifiers();
    }

    /**
     * @param modifier
     * @see org.bonitasoft.web.toolkit.client.data.item.attribute.ModifiersList#addModifier(org.bonitasoft.web.toolkit.client.data.item.attribute.modifier.Modifier)
     */
    @Override
    public void addInputModifier(final Modifier modifier) {
        this.inputModifiers.addModifier(modifier);
    }

    /**
     * @see org.bonitasoft.web.toolkit.client.data.item.attribute.ValidatorsList#getValidators()
     */
    @Override
    public List<Validator> getValidators() {
        return this.validators.getValidators();
    }

    @Override
    /**
     * @param validator
     * @see org.bonitasoft.web.toolkit.client.data.item.attribute.ValidatorsList#addValidator(org.bonitasoft.web.toolkit.client.data.item.attribute.validator.Validator)
     */
    public ItemAttribute addValidator(final Validator validator) {
        validator.setAttributeName(this.name);

        this.validators.addValidator(validator);
        return this;
    }

    /**
     * @param validators
     * @see org.bonitasoft.web.toolkit.client.data.item.attribute.ValidatorsList#addValidators(java.util.List)
     */
    @Override
    public ItemAttribute addValidators(final List<Validator> validators) {
        this.validators.addValidators(validators);
        return this;
    }

    /**
     * @param validatorClassName
     * @see org.bonitasoft.web.toolkit.client.data.item.attribute.ValidatorsList#removeValidator(java.lang.String)
     */
    @Override
    public ItemAttribute removeValidator(final String validatorClassName) {
        this.validators.removeValidator(validatorClassName);
        return this;
    }

    /**
     * @param validatorClassName
     * @see org.bonitasoft.web.toolkit.client.data.item.attribute.ValidatorsList#hasValidator(java.lang.String)
     */
    @Override
    public boolean hasValidator(final String validatorClassName) {
        return this.validators.hasValidator(validatorClassName);
    }

    /**
     * @param validatorClassName
     * @see org.bonitasoft.web.toolkit.client.data.item.attribute.ValidatorsList#getValidator(java.lang.String)
     */
    @Override
    public Validator getValidator(final String validatorClassName) {
        return this.validators.getValidator(validatorClassName);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // MODIFIERS / VALIDATORS EASY MAPPING
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Set the default value if no value is define for this attribute.<br />
     * This method add a DefaultValueModifier to the attribute
     *
     * @param value
     */
    public ItemAttribute setDefaultValue(final String value) {
        addInputModifier(new DefaultValueModifier(value));
        return this;
    }

    /**
     * Define if this attribute is mandatory (mustn't be empty)<br />
     * This method add a DefaultValueModifier to the attribute
     *
     * @param isMandatory
     */
    public ItemAttribute isMandatory(final boolean isMandatory) {
        if (isMandatory) {
            addValidator(new MandatoryValidator());
        } else {
            removeValidator(MandatoryValidator.class.getName());
        }
        return this;
    }

    /**
     * Define if this attribute is mandatory (mustn't be empty)<br />
     * This method add a DefaultValueModifier to the attribute
     */
    public ItemAttribute isMandatory() {
        return isMandatory(true);
    }
}
