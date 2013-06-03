/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingBuilder;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAProcessInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SEndEventInstanceBuilder;
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
                filed = ConnectorImplementationDescriptor.ID;
                orderBy = OrderByType.ASC;
                break;
        }
        return new OrderAndField(orderBy, filed);
    }

    static OrderAndField getOrderAndFieldForProcessInstance(final ProcessInstanceCriterion criterion, final SAProcessInstanceBuilder modelBuilder) {
        String field = null;
        OrderByType order = null;
        switch (criterion) {
            case STATE_ASC:
                field = modelBuilder.getStateIdKey();
                order = OrderByType.ASC;
                break;
            case STATE_DESC:
                field = modelBuilder.getStateIdKey();
                order = OrderByType.DESC;
                break;
            case ARCHIVE_DATE_ASC:
                field = modelBuilder.getArchiveDateKey();
                order = OrderByType.ASC;
                break;
            case ARCHIVE_DATE_DESC:
                field = modelBuilder.getArchiveDateKey();
                order = OrderByType.DESC;
                break;
            case LAST_UPDATE_DESC:
                field = modelBuilder.getLastUpdateKey();
                order = OrderByType.DESC;
                break;
            case LAST_UPDATE_ASC:
                field = modelBuilder.getLastUpdateKey();
                order = OrderByType.ASC;
                break;
            case CREATION_DATE_ASC:
                field = modelBuilder.getStartDateKey();
                order = OrderByType.ASC;
                break;
            case CREATION_DATE_DESC:
            case DEFAULT:
                field = modelBuilder.getStartDateKey();
                order = OrderByType.DESC;
                break;
            case NAME_ASC:
                field = modelBuilder.getNameKey();
                order = OrderByType.ASC;
                break;
            case NAME_DESC:
                field = modelBuilder.getNameKey();
                order = OrderByType.DESC;
                break;
            default:
                break;
        }
        return new OrderAndField(order, field);
    }

    static OrderAndField getOrderAndFieldForEvent(final EventCriterion sortingType, final SEndEventInstanceBuilder eventInstanceBuilder) {
        OrderByType orderByType = null;
        String fieldName = null;
        switch (sortingType) {
            case NAME_DESC:
                orderByType = OrderByType.DESC;
                fieldName = eventInstanceBuilder.getNameKey();
                break;
            default:
                orderByType = OrderByType.ASC;
                fieldName = eventInstanceBuilder.getNameKey();
                break;
        }
        return new OrderAndField(orderByType, fieldName);
    }

    static OrderAndField getOrderAndFieldForActivityInstance(ActivityInstanceCriterion pagingCriterion, final SUserTaskInstanceBuilder modelBuilder) {
        String field = null;
        OrderByType order = null;
        if (pagingCriterion == null) {
            pagingCriterion = ActivityInstanceCriterion.DEFAULT;
        }
        switch (pagingCriterion) {
            case DEFAULT:
                field = modelBuilder.getPriorityKey();
                order = OrderByType.DESC;
                break;
            case NAME_DESC:
                field = modelBuilder.getNameKey();
                order = OrderByType.DESC;
                break;
            case NAME_ASC:
                field = modelBuilder.getNameKey();
                order = OrderByType.ASC;
                break;
            case LAST_UPDATE_ASC:
                field = modelBuilder.getLastUpdateDateKey();
                order = OrderByType.ASC;
                break;
            case LAST_UPDATE_DESC:
                field = modelBuilder.getLastUpdateDateKey();
                order = OrderByType.DESC;
                break;
            case PRIORITY_ASC:
                field = modelBuilder.getPriorityKey();
                order = OrderByType.ASC;
                break;
            case PRIORITY_DESC:
                field = modelBuilder.getPriorityKey();
                order = OrderByType.DESC;
                break;
            case REACHED_STATE_DATE_ASC:
                field = modelBuilder.getReachStateDateKey();
                order = OrderByType.ASC;
                break;
            case REACHED_STATE_DATE_DESC:
                field = modelBuilder.getReachStateDateKey();
                order = OrderByType.DESC;
                break;
            case EXPECTED_END_DATE_ASC:
                field = modelBuilder.getExpectedEndDateKey();
                order = OrderByType.ASC;
                break;
            case EXPECTED_END_DATE_DESC:
                field = modelBuilder.getExpectedEndDateKey();
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
    public static OrderAndField getOrderAndFieldForDocument(DocumentCriterion pagingCriterion, final SDocumentMappingBuilder builder) {
        String field = null;
        OrderByType order = null;
        if (pagingCriterion == null) {
            pagingCriterion = DocumentCriterion.DEFAULT;
        }
        switch (pagingCriterion) {
            case DEFAULT:
                field = builder.getDocumentCreationDateKey();
                order = OrderByType.DESC;
                break;
            case AUTHOR_ASC:
                field = builder.getDocumentAuthorKey();
                order = OrderByType.ASC;
                break;
            case AUTHOR_DESC:
                field = builder.getDocumentAuthorKey();
                order = OrderByType.DESC;
                break;
            case FILENAME_ASC:
                field = builder.getDocumentContentFileNameKey();
                order = OrderByType.ASC;
                break;
            case FILENAME_DESC:
                field = builder.getDocumentContentFileNameKey();
                order = OrderByType.DESC;
                break;
            case MIMETYPE_ASC:
                field = builder.getDocumentContentMimeTypeKey();
                order = OrderByType.ASC;
                break;
            case MIMETYPE_DESC:
                field = builder.getDocumentContentMimeTypeKey();
                order = OrderByType.DESC;
                break;
            case CREATION_DATE_ASC:
                field = builder.getDocumentCreationDateKey();
                order = OrderByType.ASC;
                break;
            case CREATION_DATE_DESC:
                field = builder.getDocumentCreationDateKey();
                order = OrderByType.DESC;
                break;
            case NAME_ASC:
                field = builder.getDocumentNameKey();
                order = OrderByType.ASC;
                break;
            case NAME_DESC:
                field = builder.getDocumentNameKey();
                order = OrderByType.DESC;
                break;
            case URL_ASC:
                field = builder.getDocumentURLKey();
                order = OrderByType.ASC;
                break;
            case URL_DESC:
                field = builder.getDocumentURLKey();
                order = OrderByType.DESC;
                break;
        }
        return new OrderAndField(order, field);
    }
}
