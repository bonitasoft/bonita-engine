/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.bpm.connector.ConnectorCriterion;
import org.bonitasoft.engine.bpm.connector.ConnectorImplementationDescriptor;
import org.bonitasoft.engine.bpm.document.DocumentCriterion;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.EventCriterion;
import org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.document.model.builder.SDocumentBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAProcessInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SEndEventInstanceBuilderFactory;
import org.bonitasoft.engine.persistence.OrderAndField;
import org.bonitasoft.engine.persistence.OrderByType;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class OrderAndFields {

    static OrderAndField getOrderAndFieldForConnectorImplementation(final ConnectorCriterion pagingCriterion) {
        String filed = null;
        OrderByType orderBy = null;
        ConnectorCriterion criterion = pagingCriterion;
        if (criterion == null) {
            criterion = ConnectorCriterion.DEFAULT;
        }
        switch (criterion) {
            case DEFINITION_ID_ASC:
                filed = ConnectorImplementationDescriptor.DEFINITION_ID;
                orderBy = OrderByType.ASC;
                break;
            case DEFINITION_ID_DESC:
                filed = ConnectorImplementationDescriptor.DEFINITION_ID;
                orderBy = OrderByType.DESC;
                break;
            case DEFINITION_VERSION_ASC:
                filed = ConnectorImplementationDescriptor.DEFINITION_VERSION;
                orderBy = OrderByType.ASC;
                break;
            case DEFINITION_VERSION_DESC:
                filed = ConnectorImplementationDescriptor.DEFINITION_VERSION;
                orderBy = OrderByType.DESC;
                break;
            case IMPLEMENTATION_VERSION_ASC:
                filed = ConnectorImplementationDescriptor.VERSIOIN;
                orderBy = OrderByType.ASC;
                break;
            case IMPLEMENTATIONN_VERSION_DESC:
                filed = ConnectorImplementationDescriptor.VERSIOIN;
                orderBy = OrderByType.DESC;
                break;
            case IMPLEMENTATIONN_CLASS_NAME_ACS:
                filed = ConnectorImplementationDescriptor.IMPLEMENTATION_CLASS_NAME;
                orderBy = OrderByType.ASC;
                break;
            case IMPLEMENTATIONN_CLASS_NAME_DESC:
                filed = ConnectorImplementationDescriptor.IMPLEMENTATION_CLASS_NAME;
                orderBy = OrderByType.DESC;
                break;
            case IMPLEMENTATION_ID_DESC:
                filed = ConnectorImplementationDescriptor.ID;
                orderBy = OrderByType.DESC;
                break;
            case IMPLEMENTATION_ID_ASC:
            case DEFAULT:
            default:
                filed = ConnectorImplementationDescriptor.ID;
                orderBy = OrderByType.ASC;
                break;

        }
        return new OrderAndField(orderBy, filed);
    }

    static OrderAndField getOrderAndFieldForProcessInstance(final ProcessInstanceCriterion criterion) {
        final SAProcessInstanceBuilderFactory keyProvider = BuilderFactory.get(SAProcessInstanceBuilderFactory.class);
        String field = null;
        OrderByType order = null;
        switch (criterion) {
            case STATE_ASC:
                field = keyProvider.getStateIdKey();
                order = OrderByType.ASC;
                break;
            case STATE_DESC:
                field = keyProvider.getStateIdKey();
                order = OrderByType.DESC;
                break;
            case ARCHIVE_DATE_ASC:
                field = keyProvider.getArchiveDateKey();
                order = OrderByType.ASC;
                break;
            case ARCHIVE_DATE_DESC:
                field = keyProvider.getArchiveDateKey();
                order = OrderByType.DESC;
                break;
            case LAST_UPDATE_DESC:
                field = keyProvider.getLastUpdateKey();
                order = OrderByType.DESC;
                break;
            case LAST_UPDATE_ASC:
                field = keyProvider.getLastUpdateKey();
                order = OrderByType.ASC;
                break;
            case CREATION_DATE_ASC:
                field = keyProvider.getStartDateKey();
                order = OrderByType.ASC;
                break;
            case CREATION_DATE_DESC:
            case DEFAULT:
                field = keyProvider.getStartDateKey();
                order = OrderByType.DESC;
                break;
            case NAME_ASC:
                field = keyProvider.getNameKey();
                order = OrderByType.ASC;
                break;
            case NAME_DESC:
                field = keyProvider.getNameKey();
                order = OrderByType.DESC;
                break;
            default:
                break;
        }
        return new OrderAndField(order, field);
    }

    static OrderAndField getOrderAndFieldForEvent(final EventCriterion sortingType) {
        final SEndEventInstanceBuilderFactory keyProvider = BuilderFactory.get(SEndEventInstanceBuilderFactory.class);
        OrderByType orderByType = null;
        String fieldName = null;
        switch (sortingType) {
            case NAME_DESC:
                orderByType = OrderByType.DESC;
                fieldName = keyProvider.getNameKey();
                break;
            default:
                orderByType = OrderByType.ASC;
                fieldName = keyProvider.getNameKey();
                break;
        }
        return new OrderAndField(orderByType, fieldName);
    }

    static OrderAndField getOrderAndFieldForActivityInstance(ActivityInstanceCriterion pagingCriterion) {
        final SUserTaskInstanceBuilderFactory keyProvider = BuilderFactory.get(SUserTaskInstanceBuilderFactory.class);
        String field = null;
        OrderByType order = null;
        if (pagingCriterion == null) {
            pagingCriterion = ActivityInstanceCriterion.DEFAULT;
        }
        switch (pagingCriterion) {
            case DEFAULT:
                field = keyProvider.getPriorityKey();
                order = OrderByType.DESC;
                break;
            case NAME_DESC:
                field = keyProvider.getNameKey();
                order = OrderByType.DESC;
                break;
            case NAME_ASC:
                field = keyProvider.getNameKey();
                order = OrderByType.ASC;
                break;
            case LAST_UPDATE_ASC:
                field = keyProvider.getLastUpdateDateKey();
                order = OrderByType.ASC;
                break;
            case LAST_UPDATE_DESC:
                field = keyProvider.getLastUpdateDateKey();
                order = OrderByType.DESC;
                break;
            case PRIORITY_ASC:
                field = keyProvider.getPriorityKey();
                order = OrderByType.ASC;
                break;
            case PRIORITY_DESC:
                field = keyProvider.getPriorityKey();
                order = OrderByType.DESC;
                break;
            case REACHED_STATE_DATE_ASC:
                field = keyProvider.getReachStateDateKey();
                order = OrderByType.ASC;
                break;
            case REACHED_STATE_DATE_DESC:
                field = keyProvider.getReachStateDateKey();
                order = OrderByType.DESC;
                break;
            case EXPECTED_END_DATE_ASC:
                field = keyProvider.getExpectedEndDateKey();
                order = OrderByType.ASC;
                break;
            case EXPECTED_END_DATE_DESC:
                field = keyProvider.getExpectedEndDateKey();
                order = OrderByType.DESC;
                break;
            default:
                break;
        }
        return new OrderAndField(order, field);
    }

    /**
     * @param pagingCriterion
     * @param builder
     * @return
     */
    public static OrderAndField getOrderAndFieldForDocument(DocumentCriterion pagingCriterion) {
        final SDocumentBuilderFactory fact = BuilderFactory.get(SDocumentBuilderFactory.class);
        String field;
        OrderByType order;
        if (pagingCriterion == null) {
            pagingCriterion = DocumentCriterion.DEFAULT;
        }
        switch (pagingCriterion) {
            case DEFAULT:
                field = "document." + fact.getCreationDateKey();
                order = OrderByType.DESC;
                break;
            case AUTHOR_ASC:
                field = "document." + fact.getAuthorKey();
                order = OrderByType.ASC;
                break;
            case AUTHOR_DESC:
                field = "document." + fact.getAuthorKey();
                order = OrderByType.DESC;
                break;
            case FILENAME_ASC:
                field = "document." + fact.getFileNameKey();
                order = OrderByType.ASC;
                break;
            case FILENAME_DESC:
                field = "document." + fact.getFileNameKey();
                order = OrderByType.DESC;
                break;
            case MIMETYPE_ASC:
                field = "document." + fact.getMimeTypeKey();
                order = OrderByType.ASC;
                break;
            case MIMETYPE_DESC:
                field = "document." + fact.getMimeTypeKey();
                order = OrderByType.DESC;
                break;
            case CREATION_DATE_ASC:
                field = "document." + fact.getCreationDateKey();
                order = OrderByType.ASC;
                break;
            case CREATION_DATE_DESC:
                field = "document." + fact.getCreationDateKey();
                order = OrderByType.DESC;
                break;
            case NAME_ASC:
                field = fact.getNameKey();
                order = OrderByType.ASC;
                break;
            case NAME_DESC:
                field = fact.getNameKey();
                order = OrderByType.DESC;
                break;
            case URL_ASC:
                field = "document." + fact.getURLKey();
                order = OrderByType.ASC;
                break;
            case URL_DESC:
                field = "document." + fact.getURLKey();
                order = OrderByType.DESC;
                break;
            default:
                throw new IllegalStateException();
        }
        return new OrderAndField(order, field);
    }
}
