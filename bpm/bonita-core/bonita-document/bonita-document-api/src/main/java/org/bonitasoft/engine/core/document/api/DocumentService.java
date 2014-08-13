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
import org.bonitasoft.engine.core.document.exception.SDocumentMappingDeletionException;
import org.bonitasoft.engine.core.document.model.SDocumentMapping;
import org.bonitasoft.engine.core.document.model.archive.SADocumentMapping;
import org.bonitasoft.engine.core.document.exception.SDocumentException;
import org.bonitasoft.engine.core.document.exception.SDocumentNotFoundException;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;

/**
 * @author Nicolas Chabanoles
 * @author Matthieu Chaffotte
 * @since 6.0
 */
public interface DocumentService {

    /**
     * Save a document
     * 
     * @param document
     *            the document to store. The process id of the document has to be set.
     * @return The document image from database
     * @throws SProcessDocumentCreationException
     *             when the storage has failed
     */
    SDocumentMapping attachDocumentToProcessInstance(SDocumentMapping document) throws SProcessDocumentCreationException;

    /**
     * Modify document information
     * 
     * @param document
     *            the document will be updated to
     * @return the updated SDocumentMapping object
     * @throws SProcessDocumentCreationException
     *             when the update has failed
     */
    SDocumentMapping updateDocumentOfProcessInstance(SDocumentMapping document) throws SProcessDocumentCreationException;

    /**
     * Store a document and its content
     * 
     * @param document
     *            The document information. The process id of the document has to be set.
     * @param documentContent
     *            The content of the document
     * @return The stored document
     * @throws SProcessDocumentCreationException
     */
    SDocumentMapping attachDocumentToProcessInstance(SDocumentMapping document, byte[] documentContent) throws SProcessDocumentCreationException;

    /**
     * update the specific document, set it content as the specific content
     * 
     * @param document
     *            document will be updated
     * @param documentContent
     *            value of document content
     * @return the updated object
     * @throws SProcessDocumentCreationException
     */
    SDocumentMapping updateDocumentOfProcessInstance(SDocumentMapping document, byte[] documentContent) throws SProcessDocumentCreationException;

    /**
     * remove the current version of the document but archive it before
     * 
     * @param document
     *            document will be updated
     * @return the updated object
     * @throws SProcessDocumentCreationException
     */
    void removeCurrentVersion(long processInstanceId, String documentName) throws SDocumentNotFoundException, SObjectModificationException;

    /**
     * Get document content by storage id
     * 
     * @param documentStorageId
     *            identifier of document storage
     * @return document content
     * @throws SProcessDocumentContentNotFoundException
     */
    byte[] getDocumentContent(String documentStorageId) throws SProcessDocumentContentNotFoundException;

    /**
     * Get document by its id
     * 
     * @param documentId
     *            identifier of document
     * @return an SDocumentMapping object with id corresponding to the parameter
     * @throws SDocumentNotFoundException
     */
    SDocumentMapping getDocument(long documentId) throws SDocumentNotFoundException;

    /**
     * Get document by its name in the specific process instance
     * 
     * @param processInstanceId
     *            identifier of process instance
     * @param documentName
     *            name of process document
     * @return the corresponding SDocumentMapping object
     * @throws SDocumentNotFoundException
     */
    SDocumentMapping getDocument(long processInstanceId, String documentName) throws SDocumentNotFoundException;

    /**
     * Get a list of documents for specific process instance, this can be used for pagination
     * 
     * @param processInstanceId
     *            identifier of process instance
     * @param fromIndex
     *            Index of the record to be retrieved from. First record has index 0
     * @param numberPerPage
     *            Number of result we want to get. Maximum number of result returned
     * @param order
     * @param field
     * @return a list of SDocumentMapping objects
     * @throws SDocumentException
     */
    List<SDocumentMapping> getDocumentsOfProcessInstance(long processInstanceId, int fromIndex, int numberPerPage, String field, OrderByType order)
            throws SDocumentException;

    /**
     * Get total number of documents in the specific process instance
     * 
     * @param processInstanceId
     *            identifier of process instance
     * @return
     *         number of documents in the process instance
     * @throws SDocumentException
     */
    long getNumberOfDocumentsOfProcessInstance(long processInstanceId) throws SDocumentException;

    /**
     * Get name specified document archived in a certain time in the process instance
     * 
     * @param processInstanceId
     *            identifier of process instance
     * @param documentName
     *            name of document
     * @param time
     *            the archived time of document
     * @return an SDocumentMapping object archived in the specific time or not archived
     * @throws SDocumentNotFoundException
     */
    SDocumentMapping getDocument(long processInstanceId, String documentName, long time) throws SDocumentNotFoundException;

    /**
     * Get total number of document according to the query criteria
     * 
     * @param queryOptions
     *            a QueryOptions object containing some query conditions
     * @return number of document satisfied to the query criteria
     * @throws SBonitaSearchException
     */
    long getNumberOfDocuments(QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Search all documents according to the query criteria
     * 
     * @param queryOptions
     *            a QueryOptions object containing some query conditions
     * @return a list of SDocumentMapping objects
     * @throws SBonitaSearchException
     */
    List<SDocumentMapping> searchDocuments(QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Get total number of documents for the specific supervisor
     * 
     * @param userId
     *            identifier of supervisor user
     * @param queryOptions
     *            a QueryOptions object containing some query conditions
     * @return number of documents for the specific supervisor
     * @throws SBonitaSearchException
     */
    long getNumberOfDocumentsSupervisedBy(long userId, QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Search all documents for the specific supervisor
     * 
     * @param userId
     *            identifier of supervisor user
     * @param queryOptions
     *            a QueryOptions object containing some query conditions
     * @return a list of SDocumentMapping objects
     * @throws SBonitaSearchException
     */
    List<SDocumentMapping> searchDocumentsSupervisedBy(long userId, QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Get total number of archived documents according to the query criteria
     * 
     * @param queryOptions
     *            a QueryOptions object containing some query conditions
     * @return number of archived documents
     * @throws SBonitaSearchException
     */
    long getNumberOfArchivedDocuments(QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Search all archived documents according to the query criteria.
     * 
     * @param queryOptions
     *            a QueryOptions object containing some query conditions
     * @return a list of SADocumentMapping objects
     * @throws SBonitaSearchException
     */
    List<SADocumentMapping> searchArchivedDocuments(QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Get total number of archived documents for the specific supervisor
     * 
     * @param userId
     *            identifier of supervisor user
     * @param queryOptions
     *            a QueryOptions object containing some query conditions
     * @return number of archived documents for the specific supervisor
     * @throws SBonitaSearchException
     */
    long getNumberOfArchivedDocumentsSupervisedBy(long userId, QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Search all archived documents for the specific supervisor
     * 
     * @param userId
     *            identifier of supervisor user
     * @param queryOptions
     *            a QueryOptions object containing some query conditions
     * @return a list of SADocumentMapping objects
     * @throws SBonitaSearchException
     */
    List<SADocumentMapping> searchArchivedDocumentsSupervisedBy(long userId, QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Get the archived version corresponding to a document
     * 
     * @param documentId
     *            identifier of process document
     * @return the archive of the corresponding document
     * @throws SDocumentNotFoundException
     *             when the document does not exist
     */
    SADocumentMapping getArchivedVersionOfProcessDocument(long documentId) throws SDocumentNotFoundException;

    String generateDocumentURL(String name, String contentStorageId);

    /**
     * Retrieve an archived document
     * 
     * @param archivedProcessDocumentId
     *            the id of the archived document
     * @return the corresponding archive
     * @throws SDocumentNotFoundException
     *             when the archive does not exist
     */
    SADocumentMapping getArchivedDocument(long archivedProcessDocumentId) throws SDocumentNotFoundException;

    /**
     * Remove documents
     * 
     * @param sProcessDocuments
     * @throws SProcessDocumentDeletionException
     */
    void removeDocuments(List<SDocumentMapping> sProcessDocuments) throws SProcessDocumentDeletionException;

    /**
     * Remove document
     * 
     * @param sProcessDocument
     * @throws SProcessDocumentDeletionException
     */
    void removeDocument(SDocumentMapping sProcessDocument) throws SProcessDocumentDeletionException;

    /**
     * Delete documents from a specified process instance
     * 
     * @param processInstanceId
     * @throws SDocumentException
     * @throws SProcessDocumentDeletionException
     * @since 6.1
     */
    void deleteDocumentsFromProcessInstance(final Long processInstanceId) throws SDocumentException, SProcessDocumentDeletionException;

    /**
     * 
     * @param instanceId
     * @throws SDocumentMappingDeletionException
     * @since 6.0
     */
    void deleteArchivedDocuments(long instanceId) throws SDocumentMappingDeletionException;

}
