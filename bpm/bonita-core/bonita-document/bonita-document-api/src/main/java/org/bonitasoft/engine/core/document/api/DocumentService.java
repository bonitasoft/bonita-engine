/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.core.document.api;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.core.document.exception.*;
import org.bonitasoft.engine.core.document.model.SDocument;
import org.bonitasoft.engine.core.document.model.SDocumentMapping;
import org.bonitasoft.engine.core.document.model.SLightDocument;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.document.model.archive.SADocumentMapping;
import org.bonitasoft.engine.core.document.model.archive.SAMappedDocument;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;

/**
 * @author Nicolas Chabanoles
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 * @since 6.0
 */
public interface DocumentService {


    String DOCUMENT = "DOCUMENT";
    String DOCUMENTMAPPING = "DOCUMENTMAPPING";
    String SUPERVISED_BY = "SupervisedBy";

    /**
     * Save a document
     *
     * @param document
     *        the document to store
     * @param processInstanceId
     *        the process instance id to attach the document to
     * @return The document image from database
     * @throws org.bonitasoft.engine.core.document.exception.SProcessDocumentCreationException
     *         when the storage has failed
     */
    SMappedDocument attachDocumentToProcessInstance(SDocument document, long processInstanceId) throws SProcessDocumentCreationException;

    /**
     * Modify document information
     *
     * @param document
     *        the document will be updated to
     * @return the updated SDocumentMapping object
     * @throws SProcessDocumentCreationException
     *         when the update has failed
     */
    SMappedDocument updateDocumentOfProcessInstance(SDocument document, long processInstanceId) throws SProcessDocumentCreationException;


    /**
     * remove the current version of the document but archive it before
     *
     * @param processInstanceId
     *          id of the process havind the document
     * @param documentName
     *          name of the document
     * @throws SDocumentNotFoundException
     * @throws SObjectModificationException
     */
    void removeCurrentVersion(long processInstanceId, String documentName) throws SDocumentNotFoundException, SObjectModificationException;

    /**
     * Get document content by document id
     *
     * @param documentId
     *        identifier of the document
     * @return document content
     * @throws SProcessDocumentContentNotFoundException
     */
    byte[] getDocumentContent(String documentId) throws SProcessDocumentContentNotFoundException;

    /**
     * Get document with mapping by its mapping id
     *
     * @param mappingId
     *        identifier of the mapping of the document
     * @return an SDocumentMapping object with id corresponding to the parameter
     * @throws SDocumentNotFoundException
     */
    SMappedDocument getMappedDocument(long mappingId) throws SDocumentNotFoundException, SBonitaReadException;

    /**
     * Get document by its id
     *
     * @param documentId
     *        identifier of document
     * @return an SDocumentMapping object with id corresponding to the parameter
     * @throws SDocumentNotFoundException
     */
    SLightDocument getDocument(long documentId) throws SDocumentNotFoundException, SBonitaReadException;

    /**
     * Get document with mapping by its name in the specific process instance
     *
     * @param processInstanceId
     *        identifier of process instance
     * @param documentName
     *        name of process document
     * @return the corresponding SDocumentMapping object
     * @throws SDocumentNotFoundException
     */
    SMappedDocument getMappedDocument(long processInstanceId, String documentName) throws SDocumentNotFoundException, SBonitaReadException;

    /**
     * Get a list of documents for specific process instance, this can be used for pagination
     *
     * @param processInstanceId
     *        identifier of process instance
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param numberPerPage
     *        Number of result we want to get. Maximum number of result returned
     * @param order
     * @param field
     * @return a list of SDocumentMapping objects
     * @throws SDocumentException
     */
    List<SMappedDocument> getDocumentsOfProcessInstance(long processInstanceId, int fromIndex, int numberPerPage, String field, OrderByType order)
            throws SDocumentException;

    /**
     * Get total number of documents in the specific process instance
     *
     * @param processInstanceId
     *        identifier of process instance
     * @return
     *         number of documents in the process instance
     * @throws SDocumentException
     */
    long getNumberOfDocumentsOfProcessInstance(long processInstanceId) throws SDocumentException;

    /**
     * Get name specified document archived in a certain time in the process instance
     *
     * @param processInstanceId
     *        identifier of process instance
     * @param documentName
     *        name of document
     * @param time
     *        the archived time of document
     * @return an SDocumentMapping object archived in the specific time or not archived
     * @throws SDocumentNotFoundException
     */
    SMappedDocument getMappedDocument(long processInstanceId, String documentName, long time) throws SDocumentNotFoundException, SBonitaReadException;

    /**
     * Get total number of document according to the query criteria
     *
     * @param queryOptions
     *        a QueryOptions object containing some query conditions
     * @return number of document satisfied to the query criteria
     * @throws SBonitaSearchException
     */
    long getNumberOfDocuments(QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Search all documents according to the query criteria
     *
     * @param queryOptions
     *        a QueryOptions object containing some query conditions
     * @return a list of SDocumentMapping objects
     * @throws SBonitaSearchException
     */
    List<SMappedDocument> searchDocuments(QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Get total number of documents for the specific supervisor
     *
     * @param userId
     *        identifier of supervisor user
     * @param queryOptions
     *        a QueryOptions object containing some query conditions
     * @return number of documents for the specific supervisor
     * @throws SBonitaSearchException
     */
    long getNumberOfDocumentsSupervisedBy(long userId, QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Search all documents for the specific supervisor
     *
     * @param userId
     *        identifier of supervisor user
     * @param queryOptions
     *        a QueryOptions object containing some query conditions
     * @return a list of SDocumentMapping objects
     * @throws SBonitaSearchException
     */
    List<SMappedDocument> searchDocumentsSupervisedBy(long userId, QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Get total number of archived documents according to the query criteria
     *
     * @param queryOptions
     *        a QueryOptions object containing some query conditions
     * @return number of archived documents
     * @throws SBonitaSearchException
     */
    long getNumberOfArchivedDocuments(QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Search all archived documents according to the query criteria.
     *
     * @param queryOptions
     *        a QueryOptions object containing some query conditions
     * @return a list of SADocumentMapping objects
     * @throws SBonitaSearchException
     */
    List<SAMappedDocument> searchArchivedDocuments(QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Get total number of archived documents for the specific supervisor
     *
     * @param userId
     *        identifier of supervisor user
     * @param queryOptions
     *        a QueryOptions object containing some query conditions
     * @return number of archived documents for the specific supervisor
     * @throws SBonitaSearchException
     */
    long getNumberOfArchivedDocumentsSupervisedBy(long userId, QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Search all archived documents for the specific supervisor
     *
     * @param userId
     *        identifier of supervisor user
     * @param queryOptions
     *        a QueryOptions object containing some query conditions
     * @return a list of SADocumentMapping objects
     * @throws SBonitaSearchException
     */
    List<SAMappedDocument> searchArchivedDocumentsSupervisedBy(long userId, QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Get the archived version corresponding to a document
     *
     * @param documentId
     *        identifier of process document
     * @return the archive of the corresponding document
     * @throws SDocumentNotFoundException
     *         when the document does not exist
     */
    SAMappedDocument getArchivedVersionOfProcessDocument(long documentId) throws SDocumentNotFoundException;

    String generateDocumentURL(String name, String contentStorageId);

    /**
     * Retrieve an archived document
     *
     * @param archivedProcessDocumentId
     *        the id of the archived document
     * @return the corresponding archive
     * @throws SDocumentNotFoundException
     *         when the archive does not exist
     */
    SAMappedDocument getArchivedDocument(long archivedProcessDocumentId) throws SDocumentNotFoundException;


    void deleteDocument(SLightDocument document) throws SProcessDocumentDeletionException;

    /**
     * Remove document
     *
     * @param sProcessDocument
     * @throws SProcessDocumentDeletionException
     */
    void removeDocument(SMappedDocument sProcessDocument) throws SProcessDocumentDeletionException;

    /**
     * Delete documents from a specified process instance
     *
     * @param processInstanceId
     * @throws SDocumentException
     * @throws SProcessDocumentDeletionException
     * @since 6.1
     */
    void deleteDocumentsFromProcessInstance(final Long processInstanceId) throws SDocumentException, SProcessDocumentDeletionException, SBonitaReadException;

    /**
     * @param instanceId
     * @throws SDocumentMappingDeletionException
     * @since 6.0
     */
    void deleteArchivedDocuments(long instanceId) throws SDocumentMappingDeletionException;


    /**
     * archive the specific document mapping in the archive date
     *
     * @param documentMapping
     *            document mapping will be archived
     * @param archiveDate
     *            the archive time
     * @throws org.bonitasoft.engine.core.document.exception.SDocumentMappingException
     * @since 6.4.0
     */
    void archive(SDocumentMapping documentMapping, long archiveDate) throws SDocumentMappingException;




}
