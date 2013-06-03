/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.test.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class BaseServicesBuilder {

    private static final String CONFIGURATION_FILES_PATH = "org/bonitasoft/engine/conf";

    private static final String BONITA_TEST_WORK = "bonita.test.work";

    private static final String BONITA_TEST_DOCUMENT_MODEL = "bonita.test.document.model";

    private static final String BONITA_TEST_PROFILE_MODEL = "bonita.test.profile.model";

    private static final String BONITA_TEST_PROFILE_SERVICE = "bonita.test.profile.service";

    private static final String BONITA_PERSISTENCE = "bonita-persistence-";

    private static final String BONITA_TEST_DB_VENDOR = "sysprop.bonita.db.vendor";

    private static final String BONITA_TEST_IDENTITY_SERVICE = "bonita.test.identity.service";

    private static final String BONITA_TEST_IDENTITY_MODEL = "bonita.test.identity.model";

    private static final String BONITA_TEST_PERSISTENCE = "bonita.test.persistence";

    private static final String BONITA_TEST_PERSISTENCE_TEST = "bonita.test.persistence.test";

    private static final String BONITA_TEST_LOG_SERVICE = "bonita.test.log.service";

    private static final String BONITA_TEST_LOG_MODEL = "bonita.test.log.model";

    private static final String BONITA_TEST_DEPENDENCY_SERVICE = "bonita.test.dependency.service";

    private static final String BONITA_TEST_DEPENDENCY_MODEL = "bonita.test.dependency.model";

    private static final String BONITA_TEST_DATA_SERVICE = "bonita.test.data.service";

    private static final String BONITA_TEST_DATA_MODEL = "bonita.test.data.model";

    private static final String BONITA_TEST_SCHEDULER = "bonita.test.scheduler";

    private static final String BONITA_TEST_MONITORING_SERVICE = "bonita.test.monitoring.service";

    private static final String BONITA_TEST_EVENT_SERVICE = "bonita.test.events.service";

    private static final String BONITA_TEST_EVENT_MODEL = "bonita.test.events.model";

    private static final String BONITA_TEST_EXCEPTIONS_MANAGER = "bonita.test.exceptions.manager";

    private static final String BONITA_TEST_TRANSACTION_SERVICE = "bonita.test.transaction.service";

    private static final String BONITA_TEST_PLATFORM_SERVICE = "bonita.test.platform.service";

    private static final String BONITA_TEST_PLATFORM_MODEL = "bonita.test.platform.model";

    private static final String BONITA_TEST_LOG_TECHNICAL = "bonita.test.log.technical";

    private static final String BONITA_TEST_ARCHIVE = "bonita.test.archive.service";

    private static final String BONITA_TEST_EXPRESSION_SERVICE = "bonita.test.expression.service";

    private static final String BONITA_TEST_EXPRESSION_MODEL = "bonita.test.expression.model";

    private static final String BONITA_TEST_EXPRESSION_CONTROL_SERVICE = "bonita.test.expression.control.service";

    private static final String BONITA_TEST_DATAINSTANCE_SERVICE = "bonita.test.data.instance.service";

    private static final String BONITA_TEST_DATAINSTANCE_MODEL = "bonita.test.data.instance.model";

    private static final String BONITA_TEST_DATADEFINITION_MODEL = "bonita.test.data.definition.model";

    private static final String BONITA_TEST_CACHE = "bonita.core.test.cache";

    private static final String BONITA_TEST_AUTHENTICATION = "bonita.test.authentication.service";

    private static final String BONITA_TEST_PLATFORM_AUTHENTICATION = "bonita.test.platform.authentication.service";

    private static final String BONITA_TEST_SESSION = "bonita.test.session.service";

    private static final String BONITA_TEST_SESSION_MODEL = "bonita.test.session.model";

    private static final String BONITA_TEST_PLATFORM_SESSION = "bonita.test.platform.session.service";

    private static final String BONITA_TEST_PLATFORM_SESSION_MODEL = "bonita.test.platform.session.model";

    private static final String BONITA_TEST_PLATFORM_LOGIN = "bonita.test.platform.login.service";

    private static final String BONITA_LOG_TECHNICAL_SLF4J = "bonita-log-technical-slf4j";

    private static final String BONITA_EXCEPTIONS_MANAGER_IMPL = "bonita-exceptions-api-impl";

    private static final String BONITA_TRANSACTION_API_IMPL = "bonita-transaction-api-impl";

    private static final String BONITA_IDENTITY_MODEL_IMPL = "bonita-identity-model-impl";

    private static final String BONITA_IDENTITY_IMPL = "bonita-identity-impl";

    private static final String BONITA_PERSISTENCE_HIBERNATE = BONITA_PERSISTENCE + "hibernate";

    private static final String BONITA_DELETE_IMPL = "bonita-delete-impl";

    private static final String BONITA_LOG_MODEL_IMPL = "bonita-log-model-impl";

    private static final String BONITA_LOG_IMPL = "bonita-log-impl";

    private static final String BONITA_DEPENDENCY_MODEL_IMPL = "bonita-dependency-model-impl";

    private static final String BONITA_DEPENDENCY_IMPL = "bonita-dependency-impl";

    private static final String BONITA_DATA_MODEL_IMPL = "bonita-data-model-impl";

    private static final String BONITA_DATA_IMPL = "bonita-data-impl";

    private static final String BONITA_SCHEDULER_IMPL = "bonita-scheduler-impl";

    private static final String BONITA_SCHEDULER_QUARTZ = "bonita-scheduler-quartz";

    private static final String BONITA_MONITORING_IMPL = "bonita-monitoring-api-impl";

    private static final String BONITA_EVENT_IMPL = "bonita-events-api-impl";

    private static final String BONITA_EVENT_MODEL_IMPL = "bonita-events-model-impl";

    private static final String BONITA_PLATFORM_MODEL_IMPL = "bonita-platform-model-impl";

    private static final String BONITA_PLATFORM_API_IMPL = "bonita-platform-api-impl";

    private static final String BONITA_CONNECTOR_IMPL = "bonita-connector-impl";

    private static final String BONITA_EXPRESSION_IMPL = "bonita-expression-api-impl";

    private static final String BONITA_EXPRESSION_MODEL_IMPL = "bonita-expression-model-impl";

    private static final String BONITA_EXPRESSION_CONTROL_IMPL = "bos-expression-control-api-impl";

    private static final String BONITA_DATAINSTANCE_IMPL = "bos-data-instance-api-impl";

    private static final String BONITA_DATAINSTANCE_MODEL_IMPL = "bos-data-instance-model-impl";

    private static final String BONITA_DATADEFINITION_MODEL_IMPL = "bos-data-definition-model-impl";

    private static final String BONITA_ARCHIVE_IMPL = "bonita-archive-impl";

    private static final String BONITA_CACHE = "bonita-cache-ehcache";

    private static final String BONITA_AUTHENTICATION_IMPL = "bonita-authentication-impl";

    private static final String BONITA_PLATFORM_AUTHENTICATION_IMPL = "bonita-platform-authentication-impl";

    private static final String BONITA_SESSSION_IMPL = "bonita-session-impl";

    private static final String BONITA_SESSSION_MODEL_IMPL = "bonita-session-model-impl";

    private static final String BONITA_PLATFORM_SESSSION_IMPL = "bonita-platform-session-impl";

    private static final String BONITA_PLATFORM_SESSSION_MODEL_IMPL = "bonita-platform-session-model-impl";

    private static final String BONITA_PLATFORM_LOGIN_IMPL = "bonita-platform-login-impl";

    private static final String BONITA_TEST_CATEGORY_SERVICE = "bonita.test.core.category.service";

    private static final String BONITA_CATEGORY_IMPL = "bos-category-impl";

    private static final String BONITA_TEST_CATEGORY_MODEL = "bonita.test.core.category.model";

    private static final String BONITA_CATEGORY_MODEL_IMPL = "bos-category-model-impl";

    private static final String BONITA_TEST_COMMAND_SERVICE = "bonita.test.core.command.service";

    private static final String BONITA_COMMAND_IMPL = "bos-command-api-impl";

    private static final String BONITA_TEST_COMMAND_MODEL = "bonita.test.core.command.model";

    private static final String BONITA_COMMAND_MODEL_IMPL = "bos-command-model-impl";

    private static final String BONITA_TEST_PLATFORM_COMMAND_SERVICE = "bonita.test.core.platform.command.service";

    private static final String BONITA_PLATFORM_COMMAND_IMPL = "bos-platform-command-api-impl";

    private static final String BONITA_TEST_PLATFORM_COMMAND_MODEL = "bonita.test.core.platform.command.model";

    private static final String BONITA_PLATFORM_COMMAND_MODEL_IMPL = "bos-platform-command-model-impl";

    private final String base = "classpath*:/";

    private static final String BONITA_TEST_DOCUMENT_SERVICE = "bonita.test.document.service";

    private static final String BONITA_DOCUMENT_CMIS = "bonita-document-opencmis";

    private static final String BONITA_SUPERVISOR_IMPL = "bos-supervisor-mapping-impl";

    private static final String BONITA_TEST_OPERATION_SERVICE = "bonita.test.operation.service";

    private static final String BONITA_TEST_OPERATION_MODEL = "bonita.test.operation.model";

    private static final String BONITA_OPERATION_IMPL = "bos-operation-api-impl";

    private static final String BONITA_OPERATION_MODEL_IMPL = "bos-operation-model-impl";

    private static final String BONITA_TEST_SUPERVISOR_MODEL = "bonita.test.core.supervisor.mapping.model";

    private static final String BONITA_EXTERNAL_IDENTITY_MAPPING_IMPL = "bos-external-identity-mapping-impl";

    private static final String BONITA_TEST_EXTERNAL_IDENTITY_MAPPING_MODEL = "bonita.test.external.identity.mapping.model";

    private static ClassPathXmlApplicationContext classPathXmlApplicationContext;

    public BaseServicesBuilder() {
        super();
    }

    public String getDbVendor() {
        return System.getProperty(BONITA_TEST_DB_VENDOR, "h2");
    }

    protected String getServiceImplementationConfigurationFile(final String category, final String serviceKey, final String defaultService) {
        final StringBuilder builder = new StringBuilder(base);
        builder.append(category).append("/").append("cfg-").append(System.getProperty(serviceKey, defaultService)).append(".xml");

        return builder.toString();
    }

    protected String getPersistenceServiceConfigurationFileForModule(final String category, final String serviceKey, final String defaultService,
            final String persistenceType) {
        final StringBuilder builder = new StringBuilder(base);
        builder.append(category).append("/").append("cfg-").append(System.getProperty(serviceKey, defaultService)).append("-").append(persistenceType)
                .append(".xml");

        return builder.toString();
    }

    protected String getBaseSpringFile() {
        final StringBuilder builder = new StringBuilder(base);
        builder.append("cfg.xml");

        return builder.toString();
    }

    protected ClassPathXmlApplicationContext getApplicationContext() {
        if (classPathXmlApplicationContext == null) {
            final List<String> resources = getResourceList();
            classPathXmlApplicationContext = new ClassPathXmlApplicationContext(resources.toArray(new String[resources.size()]));
        }
        return classPathXmlApplicationContext;
    }

    protected List<String> getResourceList() {
        final List<String> resources = new ArrayList<String>();
        resources.add(getBaseSpringFile());
        resources.add(base + CONFIGURATION_FILES_PATH + "/cfg-bonita-persistence-db.xml");// db scripts of the persistence
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_EVENT_SERVICE, BONITA_EVENT_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_EVENT_MODEL, BONITA_EVENT_MODEL_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_MONITORING_SERVICE, BONITA_MONITORING_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_EXCEPTIONS_MANAGER, BONITA_EXCEPTIONS_MANAGER_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_IDENTITY_MODEL, BONITA_IDENTITY_MODEL_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_IDENTITY_SERVICE, BONITA_IDENTITY_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_TRANSACTION_SERVICE, BONITA_TRANSACTION_API_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_PERSISTENCE, getDefaultPersistenceType()));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_PERSISTENCE_TEST, getDefaultPersistenceType() + "-test"));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_PLATFORM_SERVICE, BONITA_PLATFORM_API_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_PLATFORM_MODEL, BONITA_PLATFORM_MODEL_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_LOG_SERVICE, BONITA_LOG_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_LOG_MODEL, BONITA_LOG_MODEL_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_DEPENDENCY_SERVICE, BONITA_DEPENDENCY_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_DEPENDENCY_SERVICE, "bonita-dependency-platform-impl"));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_DEPENDENCY_MODEL, BONITA_DEPENDENCY_MODEL_IMPL));
        resources
                .add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_DEPENDENCY_MODEL, "bonita-dependency-platform-model-impl"));

        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_CATEGORY_SERVICE, BONITA_CATEGORY_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_COMMAND_SERVICE, BONITA_COMMAND_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_PLATFORM_COMMAND_SERVICE, BONITA_PLATFORM_COMMAND_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_PLATFORM_COMMAND_MODEL,
                BONITA_PLATFORM_COMMAND_MODEL_IMPL));

        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_DATA_SERVICE, BONITA_DATA_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_DATA_MODEL, BONITA_DATA_MODEL_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_CACHE, BONITA_CACHE));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_SCHEDULER, BONITA_SCHEDULER_QUARTZ));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_LOG_TECHNICAL, BONITA_LOG_TECHNICAL_SLF4J));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_ARCHIVE, BONITA_ARCHIVE_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_EXPRESSION_SERVICE, BONITA_EXPRESSION_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_EXPRESSION_MODEL, BONITA_EXPRESSION_MODEL_IMPL));
        resources.add(getServiceImplementationConfigurationFile("org/bonitasoft/engine/core/expression/control/api/impl",
                BONITA_TEST_EXPRESSION_CONTROL_SERVICE, BONITA_EXPRESSION_CONTROL_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_OPERATION_SERVICE, BONITA_OPERATION_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_OPERATION_MODEL, BONITA_OPERATION_MODEL_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_DATAINSTANCE_SERVICE, BONITA_DATAINSTANCE_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_DATAINSTANCE_MODEL, BONITA_DATAINSTANCE_MODEL_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_DATADEFINITION_MODEL, BONITA_DATADEFINITION_MODEL_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_AUTHENTICATION, BONITA_AUTHENTICATION_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_PLATFORM_AUTHENTICATION,
                BONITA_PLATFORM_AUTHENTICATION_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_SESSION, BONITA_SESSSION_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_SESSION_MODEL, BONITA_SESSSION_MODEL_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_PLATFORM_SESSION, BONITA_PLATFORM_SESSSION_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_PLATFORM_SESSION_MODEL,
                BONITA_PLATFORM_SESSSION_MODEL_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_PLATFORM_LOGIN, BONITA_PLATFORM_LOGIN_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_CONNECTOR_IMPL, BONITA_CONNECTOR_IMPL));

        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_PROFILE_SERVICE, "bos-profile-impl"));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_PROFILE_MODEL, "bos-profile-model-impl"));

        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, "bonita.test.external.identity.mapping.service",
                BONITA_EXTERNAL_IDENTITY_MAPPING_IMPL));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_DOCUMENT_MODEL, "bonita-document-model-impl"));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_WORK, "bonita-work-impl"));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, BONITA_TEST_DOCUMENT_SERVICE, BONITA_DOCUMENT_CMIS));
        resources.add(getServiceImplementationConfigurationFile(CONFIGURATION_FILES_PATH, "bonita.test.delete", BONITA_DELETE_IMPL));

        resources.add(base + "org/bonitasoft/engine/persistence/test/cfg-bonita-persistence-db.xml");// human db scripts

        final String property = System.getProperty(BONITA_TEST_PERSISTENCE, getDefaultPersistenceType());
        final String persistenceType = property.substring(BONITA_PERSISTENCE.length());
        resources.add(getPersistenceServiceConfigurationFileForModule(CONFIGURATION_FILES_PATH, BONITA_TEST_IDENTITY_MODEL, BONITA_IDENTITY_MODEL_IMPL,
                persistenceType));
        resources.add(getPersistenceServiceConfigurationFileForModule(CONFIGURATION_FILES_PATH, BONITA_TEST_PLATFORM_MODEL, BONITA_PLATFORM_MODEL_IMPL,
                persistenceType));
        resources.add(getPersistenceServiceConfigurationFileForModule(CONFIGURATION_FILES_PATH, BONITA_TEST_LOG_MODEL, BONITA_LOG_MODEL_IMPL, persistenceType));
        resources.add(getPersistenceServiceConfigurationFileForModule(CONFIGURATION_FILES_PATH, BONITA_TEST_DEPENDENCY_MODEL, BONITA_DEPENDENCY_MODEL_IMPL,
                persistenceType));
        resources.add(getPersistenceServiceConfigurationFileForModule(CONFIGURATION_FILES_PATH, BONITA_TEST_DEPENDENCY_MODEL,
                "bonita-dependency-platform-model-impl", persistenceType));
        resources
                .add(getPersistenceServiceConfigurationFileForModule(CONFIGURATION_FILES_PATH, BONITA_TEST_DATA_MODEL, BONITA_DATA_MODEL_IMPL, persistenceType));

        resources.add(getPersistenceServiceConfigurationFileForModule(CONFIGURATION_FILES_PATH, BONITA_TEST_SCHEDULER, BONITA_SCHEDULER_IMPL, persistenceType));
        resources.add(getPersistenceServiceConfigurationFileForModule(CONFIGURATION_FILES_PATH, BONITA_TEST_DATAINSTANCE_MODEL, BONITA_DATAINSTANCE_MODEL_IMPL,
                persistenceType));

        resources.add(getPersistenceServiceConfigurationFileForModule(CONFIGURATION_FILES_PATH, BONITA_TEST_CATEGORY_MODEL, BONITA_CATEGORY_MODEL_IMPL,
                persistenceType));
        resources.add(getPersistenceServiceConfigurationFileForModule(CONFIGURATION_FILES_PATH, BONITA_TEST_COMMAND_MODEL, BONITA_COMMAND_MODEL_IMPL,
                persistenceType));
        resources.add(getPersistenceServiceConfigurationFileForModule(CONFIGURATION_FILES_PATH, BONITA_TEST_PLATFORM_COMMAND_MODEL,
                BONITA_PLATFORM_COMMAND_MODEL_IMPL, persistenceType));
        resources.add(getPersistenceServiceConfigurationFileForModule(CONFIGURATION_FILES_PATH, BONITA_TEST_DOCUMENT_MODEL, "bonita-document-model-impl",
                persistenceType));
        resources.add(getPersistenceServiceConfigurationFileForModule(CONFIGURATION_FILES_PATH, BONITA_TEST_PROFILE_MODEL, "bos-profile-model-impl",
                persistenceType));
        resources.add(getPersistenceServiceConfigurationFileForModule(CONFIGURATION_FILES_PATH, BONITA_TEST_SUPERVISOR_MODEL, BONITA_SUPERVISOR_IMPL,
                persistenceType));

        resources.add(getPersistenceServiceConfigurationFileForModule(CONFIGURATION_FILES_PATH, BONITA_TEST_EXTERNAL_IDENTITY_MAPPING_MODEL,
                BONITA_EXTERNAL_IDENTITY_MAPPING_IMPL, persistenceType));

        return resources;
    }

    protected String getDefaultPersistenceType() {
        return BONITA_PERSISTENCE_HIBERNATE;
    }

    public <T> T getInstanceOf(final Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    public <T> Map<String, T> getInstancesOf(final Class<T> clazz) {
        return getApplicationContext().getBeansOfType(clazz);
    }

    public <T> T getInstanceOf(final String name, final Class<T> class1) {
        return getApplicationContext().getBean(name, class1);
    }

}
