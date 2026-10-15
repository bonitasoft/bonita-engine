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
package org.bonitasoft.web.rest.model.application;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.bonitasoft.web.toolkit.client.common.TreeIndexed;
import org.bonitasoft.web.toolkit.client.common.TreeLeaf;
import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.DiscriminatedItemDefinitionHelper;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ValidationError;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ValidationException;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.FileIsImageOrServletPathValidator;

/**
 * Item definition for a Bonita Living Application for the REST API (either legacy or link).
 */
public class AbstractApplicationDefinition<ITEM extends AbstractApplicationItem> extends ItemDefinition<ITEM> {

    public static final String TOKEN = "abstractApplication";

    @Override
    protected String defineToken() {
        return TOKEN;
    }

    @Override
    protected String defineAPIUrl() {
        return "../API/living/application";
    }

    @Override
    protected void defineAttributes() {
        createAttribute(AbstractApplicationItem.ATTRIBUTE_ID, ItemAttribute.TYPE.ITEM_ID);
        createAttribute(AbstractApplicationItem.ATTRIBUTE_LINK, ItemAttribute.TYPE.BOOLEAN);
        createAttribute(AbstractApplicationItem.ATTRIBUTE_TOKEN, ItemAttribute.TYPE.STRING);
        createAttribute(AbstractApplicationItem.ATTRIBUTE_DISPLAY_NAME, ItemAttribute.TYPE.STRING);
        createAttribute(AbstractApplicationItem.ATTRIBUTE_PROFILE_ID, ItemAttribute.TYPE.ITEM_ID);
        createAttribute(AbstractApplicationItem.ATTRIBUTE_VERSION, ItemAttribute.TYPE.STRING);
        createAttribute(AbstractApplicationItem.ATTRIBUTE_DESCRIPTION, ItemAttribute.TYPE.TEXT);
        createAttribute(AbstractApplicationItem.ATTRIBUTE_ICON, ItemAttribute.TYPE.STRING)
                .addValidator(new FileIsImageOrServletPathValidator(ApplicationItem.ICON_PATH_API_PREFIX));
        createAttribute(AbstractApplicationItem.ATTRIBUTE_CREATION_DATE, ItemAttribute.TYPE.STRING);
        createAttribute(AbstractApplicationItem.ATTRIBUTE_CREATED_BY, ItemAttribute.TYPE.ITEM_ID);
        createAttribute(AbstractApplicationItem.ATTRIBUTE_LAST_UPDATE_DATE, ItemAttribute.TYPE.STRING);
        createAttribute(AbstractApplicationItem.ATTRIBUTE_UPDATED_BY, ItemAttribute.TYPE.ITEM_ID);
        createAttribute(AbstractApplicationItem.ATTRIBUTE_STATE, ItemAttribute.TYPE.STRING);
        createAttribute(AbstractApplicationItem.ATTRIBUTE_VISIBILITY, ItemAttribute.TYPE.STRING);
        createAttribute(ApplicationItem.ATTRIBUTE_EDITABLE, ItemAttribute.TYPE.BOOLEAN);
    }

    @Override
    protected void definePrimaryKeys() {
        setPrimaryKeys(AbstractApplicationItem.ATTRIBUTE_ID);
    }

    public static AbstractApplicationDefinition<? extends AbstractApplicationItem> get() {
        return (AbstractApplicationDefinition<? extends AbstractApplicationItem>) Definitions.get(TOKEN);
    }

    @Override
    protected ITEM _createItem() {
        // this must not be called by deprecated PUT and POST methods which need to discriminate on "link".
        throw new ValidationException(Collections.singletonList(
                new ValidationError("link", "%attribute% is mandatory to discriminate the application type")));
    }

    @Override
    public Optional<DiscriminatedItemDefinitionHelper<ITEM>> getDiscriminatedHelper() {
        if (AbstractApplicationDefinition.class.equals(getClass())) {
            return Optional.of(new DiscriminatedItemDefinitionHelper<ITEM>() {

                @Override
                @SuppressWarnings("unchecked")
                public Supplier<ITEM> findItemCreator(Map<String, String> attributes) {
                    // We need the "link" attribute to discriminate between legacy application and application link.
                    boolean isLink = attributes != null
                            && Boolean.parseBoolean(attributes.get(AbstractApplicationItem.ATTRIBUTE_LINK));
                    return isLink ? () -> (ITEM) ApplicationLinkDefinition.get()._createItem()
                            : () -> (ITEM) ApplicationDefinition.get()._createItem();
                }

                @Override
                public Supplier<? extends ITEM> findItemCreator(TreeIndexed<String> tree) {
                    // We need the "link" attribute to discriminate between legacy application and application link.
                    boolean isLink;
                    if (tree != null && tree.get(AbstractApplicationItem.ATTRIBUTE_LINK) instanceof TreeLeaf<?> v) {
                        isLink = Optional.ofNullable(v.getValue()).map(Object::toString).map(Boolean::parseBoolean)
                                .orElse(Boolean.FALSE);
                    } else {
                        isLink = false;
                    }
                    return isLink ? () -> (ITEM) ApplicationLinkDefinition.get()._createItem()
                            : () -> (ITEM) ApplicationDefinition.get()._createItem();
                }
            });
        } else {
            // subclasses do not need the helper and use a concrete implementation
            return Optional.empty();
        }
    }

}
