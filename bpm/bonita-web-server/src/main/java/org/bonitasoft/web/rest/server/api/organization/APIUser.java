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
package org.bonitasoft.web.rest.server.api.organization;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.console.common.server.preferences.properties.PropertiesFactory;
import org.bonitasoft.web.rest.model.identity.UserDefinition;
import org.bonitasoft.web.rest.model.identity.UserItem;
import org.bonitasoft.web.rest.server.api.ConsoleAPI;
import org.bonitasoft.web.rest.server.datastore.bpm.flownode.HumanTaskDatastore;
import org.bonitasoft.web.rest.server.datastore.organization.PersonalContactDataDatastore;
import org.bonitasoft.web.rest.server.datastore.organization.ProfessionalContactDataDatastore;
import org.bonitasoft.web.rest.server.datastore.organization.UserDatastore;
import org.bonitasoft.web.rest.server.framework.api.*;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.util.MapUtil;
import org.bonitasoft.web.toolkit.client.common.util.StringUtil;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ValidationError;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ValidationException;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.AbstractStringValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Séverin Moussel
 */
// TODO : implements APIhasFile
public class APIUser extends ConsoleAPI<UserItem> implements APIHasAdd<UserItem>, APIHasDelete, APIHasUpdate<UserItem>,
        APIHasGet<UserItem>, APIHasSearch<UserItem> {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(APIUser.class.getName());

    @Override
    protected ItemDefinition<UserItem> defineItemDefinition() {
        return UserDefinition.get();
    }

    @Override
    public String defineDefaultSearchOrder() {
        return UserItem.ATTRIBUTE_LASTNAME;
    }

    @Override
    public UserItem add(final UserItem item) {

        // Finish the upload of the icon
        if (StringUtil.isBlank(item.getPassword())) {
            throw new ValidationException(
                    Collections.singletonList(new ValidationError("Password", "%attribute% is mandatory")));
        }
        checkPasswordRobustness(item.getPassword());

        // Add
        return super.add(item);

    }

    @Override
    protected Datastore defineDefaultDatastore() {
        return new UserDatastore(getEngineSession());
    }

    private void checkPasswordRobustness(final String password) {
        try {
            final Class<?> validatorClass = Class.forName(getValidatorClassName());
            Object instanceClass;
            try {
                instanceClass = validatorClass.newInstance();
                final AbstractStringValidator validator = (AbstractStringValidator) instanceClass;
                validator.setLocale(getLocale());
                validator.check(password);
                if (!validator.getErrors().isEmpty()) {
                    throw new ValidationException(validator.getErrors());
                }
            } catch (final InstantiationException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error while instantiating the class", e);
                }
                e.printStackTrace();
            } catch (final IllegalAccessException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Illegal access with the file ", e);
                }
                e.printStackTrace();
            }
        } catch (final ClassNotFoundException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Class not found", e);
            }
            e.printStackTrace();
        }
    }

    String getValidatorClassName() {
        return PropertiesFactory.getSecurityProperties().getPasswordValidator();
    }

    @Override
    public UserItem update(final APIID id, final Map<String, String> item) {
        // Do not update password if not set
        MapUtil.removeIfBlank(item, UserItem.ATTRIBUTE_PASSWORD);
        if (item.get(UserItem.ATTRIBUTE_PASSWORD) != null) {
            checkPasswordRobustness(item.get(UserItem.ATTRIBUTE_PASSWORD));
        }
        return super.update(id, item);
    }

    @Override
    public UserItem get(final APIID id) {
        final UserItem item = super.get(id);
        if (item != null) {

            // Do not let the password output from the API
            item.setPassword(null);
            final String iconPath = item.getIcon();
            if (iconPath == null || iconPath.isEmpty()) {
                item.setIcon(UserItem.DEFAULT_USER_ICON);
            }
        }

        return item;
    }

    @Override
    public ItemSearchResult<UserItem> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {

        final ItemSearchResult<UserItem> results = super.search(page, resultsByPage, search, orders, filters);

        for (final UserItem item : results.getResults()) {
            if (item != null) {
                // Do not let the password output from the API
                item.setPassword(null);
            }
        }

        return results;
    }

    @Override
    public void delete(final List<APIID> ids) {
        super.delete(ids);
    }

    @Override
    protected void fillDeploys(final UserItem item, final List<String> deploys) {
        if (isDeployable(UserItem.ATTRIBUTE_MANAGER_ID, deploys, item)) {
            item.setDeploy(UserItem.ATTRIBUTE_MANAGER_ID,
                    ((UserDatastore) getDefaultDatastore()).get(item.getManagerId()));
        }

        if (isDeployable(UserItem.ATTRIBUTE_CREATED_BY_USER_ID, deploys, item)) {
            item.setDeploy(UserItem.ATTRIBUTE_CREATED_BY_USER_ID,
                    ((UserDatastore) getDefaultDatastore()).get(item.getCreatedByUserId()));
        }

        if (deploys.contains(UserItem.DEPLOY_PERSONAL_DATA)) {
            item.setDeploy(UserItem.DEPLOY_PERSONAL_DATA,
                    new PersonalContactDataDatastore(getEngineSession()).get(item.getId()));

            // not a real deploy. force attribute to fix json conversion (Item#toJson)
            item.setAttribute(UserItem.DEPLOY_PERSONAL_DATA, (String) null);
        }

        if (deploys.contains(UserItem.DEPLOY_PROFESSIONAL_DATA)) {
            item.setDeploy(UserItem.DEPLOY_PROFESSIONAL_DATA,
                    new ProfessionalContactDataDatastore(getEngineSession()).get(item.getId()));

            // not a real deploy. force attribute to fix json conversion (Item#toJson)
            item.setAttribute(UserItem.DEPLOY_PROFESSIONAL_DATA, (String) null);
        }

    }

    @Override
    protected void fillCounters(final UserItem item, final List<String> counters) {

        if (counters.contains(UserItem.COUNTER_OPEN_TASKS)) {
            item.setAttribute(UserItem.COUNTER_OPEN_TASKS,
                    new HumanTaskDatastore(getEngineSession()).getNumberOfOpenTasks(item.getId()));
        }

        if (counters.contains(UserItem.COUNTER_OVERDUE_TASKS)) {
            item.setAttribute(UserItem.COUNTER_OVERDUE_TASKS,
                    new HumanTaskDatastore(getEngineSession()).getNumberOfOverdueOpenTasks(item.getId()));
        }
    }

}
