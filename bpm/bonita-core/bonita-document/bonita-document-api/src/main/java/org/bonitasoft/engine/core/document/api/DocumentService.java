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
package org.bonitasoft.engine.core.document.api;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.document.model.SDocument;
import org.bonitasoft.engine.core.document.model.SDocumentMapping;
import org.bonitasoft.engine.core.document.model.SLightDocument;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.document.model.archive.SAMappedDocument;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.SRecorderException;

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
     * @param document the document to store
     * @param processInstanceId the process instance id to attach the document to
     * @param name
     * @param description
     * @return The document image from database
     * @throws SObjectCreationException when the storage has failed
     */
    SMappedDocument attachDocumentToProcessInstance(SDocument document, long processInstanceId, String name, String description)
            throws SObjectCreationException;

    /**
     * Save a document
     *
     * @param document the document to store
     * @param processInstanceId the process instance id to attach the document to
     * @param name
     * @param description
     * @param index the index in the list of document
     * @return The document image from database
     * @throws SObjectCreationException when the storage has failed
     */
    SMappedDocument attachDocumentToProcessInstance(SDocument document, long processInstanceId, String name, String description, int index)
            throws SObjectCreationException, SObjectAlreadyExistsException;

    /**
     * Remove this document.
     * <p>
     * this archive and delete mapping on the process, i.e. the content of the document itself will be kept in database, use
     * {@link #deleteContentOfArchivedDocument(long)} to delete the content
     * </p>
     *
     * @param document the document mapping to remove
     * @throws SObjectModificationException
     */
    void removeCurrentVersion(SMappedDocument document) throws SObjectModificationException;

    /**
     * Remove the document with the specified process instance and name
     * <p>
     * this archive and delete mapping on the process, i.e. the content of the document itself will be kept in database, use
     * {@link #deleteContentOfArchivedDocument(long)} to delete the content
     * </p>
     *
     * @param processInstanceId id of the process having the document
     * @param documentName name of the document
     * @throws SObjectNotFoundException
     * @throws SObjectModificationException
     */
    void removeCurrentVersion(long processInstanceId, String documentName) throws SObjectNotFoundException, SObjectModificationException;

    /**
     * Get document content by document id
     *
     * @param documentId identifier of the document
     * @return document content
     * @throws SObjectNotFoundException
     */
    byte[] getDocumentContent(String documentId) throws SObjectNotFoundException;

    /**
     * Get document with mapping by its mapping id
     *
     * @param mappingId identifier of the mapping of the document
     * @return an SDocumentMapping object with id corresponding to the parameter
     * @throws SObjectNotFoundException
     */
    SMappedDocument getMappedDocument(long mappingId) throws SObjectNotFoundException, SBonitaReadException;

    /**
     * Get document by its id
     *
     * @param documentId identifier of document
     * @return an SDocumentMapping object with id corresponding to the parameter
     * @throws SObjectNotFoundException
     */
    SLightDocument getDocument(long documentId) throws SObjectNotFoundException, SBonitaReadException;

    /**
     * Get document with mapping by its name in the specific process instance
     *
     * @param processInstanceId identifier of process instance
     * @param documentName name of process document
     * @return the corresponding SDocumentMapping object
     * @throws SObjectNotFoundException
     */
    SMappedDocument getMappedDocument(long processInstanceId, String documentName) throws SObjectNotFoundException, SBonitaReadException;

    /**
     * Get a list of documents for specific process instance, this can be used for pagination
     *
     * @param processInstanceId identifier of process instance
     * @param fromIndex Index of the record to be retrieved from. First record has index 0
     * @param numberPerPage Number of result we want to get. Maximum number of result returned
     * @param order
     * @param field
     * @return a list of SDocumentMapping objects
     */
    List<SMappedDocument> getDocumentsOfProcessInstance(long processInstanceId, int fromIndex, int numberPerPage, String field, OrderByType order)
            throws SBonitaReadException;

    /**
     * Get total number of documents in the specific process instance
     *
     * @param processInstanceId identifier of process instance
     * @return number of documents in the process instance
     * @throws org.bonitasoft.engine.persistence.SBonitaReadException
     */
    long getNumberOfDocumentsOfProcessInstance(long processInstanceId) throws SBonitaReadException;

    /**
     * Get name specified document archived in a certain time in the process instance
     *
     * @param processInstanceId identifier of process instance
     * @param documentName name of document
     * @param time the archived time of document
     * @return an SDocumentMapping object archived in the specific time or not archived
     * @throws SObjectNotFoundException
     */
    SMappedDocument getMappedDocument(long processInstanceId, String documentName, long time) throws SObjectNotFoundException, SBonitaReadException;

    /**
     * Get total number of document according to the query criteria
     *
     * @param queryOptions a QueryOptions object containing some query conditions
     * @return number of document satisfied to the query criteria
     * @throws SBonitaReadException
     */
    long getNumberOfDocuments(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Search all documents according to the query criteria
     *
     * @param queryOptions a QueryOptions object containing some query conditions
     * @return a list of SDocumentMapping objects
     * @throws SBonitaReadException
     */
    List<SMappedDocument> searchDocuments(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Get total number of documents for the specific supervisor
     *
     * @param userId identifier of supervisor user
     * @param queryOptions a QueryOptions object containing some query conditions
     * @return number of documents for the specific supervisor
     * @throws SBonitaReadException
     */
    long getNumberOfDocumentsSupervisedBy(long userId, QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Search all documents for the specific supervisor
     *
     * @param userId identifier of supervisor user
     * @param queryOptions a QueryOptions object containing some query conditions
     * @return a list of SDocumentMapping objects
     * @throws SBonitaReadException
     */
    List<SMappedDocument> searchDocumentsSupervisedBy(long userId, QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Get total number of archived documents according to the query criteria
     *
     * @param queryOptions a QueryOptions object containing some query conditions
     * @return number of archived documents
     * @throws SBonitaReadException
     */
    long getNumberOfArchivedDocuments(QueryOptions queryOptions) throws SBonitaReadException;

    void removeDocument(SMappedDocument mappedDocument) throws SObjectModificationException;

    /**
     * Search all archived documents according to the query criteria.
     *
     * @param queryOptions a QueryOptions object containing some query conditions
     * @return a list of SADocumentMapping objects
     * @throws SBonitaReadException
     */
    List<SAMappedDocument> searchArchivedDocuments(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Get total number of archived documents for the specific supervisor
     *
     * @param userId identifier of supervisor user
     * @param queryOptions a QueryOptions object containing some query conditions
     * @return number of archived documents for the specific supervisor
     * @throws SBonitaReadException
     */
    long getNumberOfArchivedDocumentsSupervisedBy(long userId, QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Search all archived documents for the specific supervisor
     *
     * @param userId identifier of supervisor user
     * @param queryOptions a QueryOptions object containing some query conditions
     * @return a list of SADocumentMapping objects
     * @throws SBonitaReadException
     */
    List<SAMappedDocument> searchArchivedDocumentsSupervisedBy(long userId, QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Get the archived version corresponding to a document
     *
     * @param documentId identifier of process document
     * @return the archive of the corresponding document
     * @throws SObjectNotFoundException when the document does not exist
     */
    SAMappedDocument getArchivedVersionOfProcessDocument(long documentId) throws SObjectNotFoundException;

    String generateDocumentURL(String name, String contentStorageId);

    /**
     * Retrieve an archived document
     *
     * @param archivedProcessDocumentId the id of the archived document
     * @return the corresponding archive
     * @throws SObjectNotFoundException when the archive does not exist
     */
    SAMappedDocument getArchivedDocument(long archivedProcessDocumentId) throws SObjectNotFoundException;

    void deleteDocument(SLightDocument document) throws SObjectModificationException;

    /**
     * Delete documents from a specified process instance
     *
     * @param processInstanceId
     * @throws org.bonitasoft.engine.commons.exceptions.SObjectModificationException
     * @throws SBonitaReadException
     * @since 6.1
     */
    void deleteDocumentsFromProcessInstance(final Long processInstanceId) throws SBonitaReadException, SObjectModificationException;

    /**
     * @param instanceId
     * @throws SObjectModificationException
     * @since 6.0
     */
    void deleteArchivedDocuments(long instanceId) throws SObjectModificationException;

    /**
     * archive the specific document mapping in the archive date
     *
     * @param documentMapping document mapping will be archived
     * @param archiveDate the archive time
     * @throws org.bonitasoft.engine.commons.exceptions.SObjectModificationException
     * @since 6.4.0
     */
    void archive(SDocumentMapping documentMapping, long archiveDate) throws SObjectModificationException;

    /**
     * @param mappedDocument the document to update
     * @param document the new content
     * @param index the new index
     * @throws org.bonitasoft.engine.commons.exceptions.SObjectModificationException
     * @since 6.4.0
     */
    void updateDocumentOfList(final SMappedDocument mappedDocument, final SDocument document, int index) throws SObjectModificationException;

    /**
     * update the index of a document inside the list
     *
     * @param mappedDocument the document to update
     * @param index the new index
     * @throws org.bonitasoft.engine.commons.exceptions.SObjectModificationException
     * @since 6.4.0
     */
    void updateDocumentIndex(final SMappedDocument mappedDocument, int index) throws SObjectModificationException;

    /**
     * Get a list of document. if there is no document in the list returns an empty list
     *
     * @param documentName the name of the document list
     * @param processInstanceId the id of the process instance that contains the list
     * @param fromIndex pagination parameter
     * @param numberOfResult pagination parameter
     * @return the list of document
     * @since 6.4.0
     */
    List<SMappedDocument> getDocumentList(String documentName, long processInstanceId, int fromIndex, int numberOfResult) throws SBonitaReadException;

    /**
     * @param documentToUpdate the document mapping to udpate
     * @param sDocument the value to set th emapping with
     * @return the updated document mapping
     * @throws SRecorderException
     * @throws org.bonitasoft.engine.commons.exceptions.SObjectModificationException
     */
    SMappedDocument updateDocument(SDocumentMapping documentToUpdate, SDocument sDocument) throws SObjectModificationException;

    /**
     * Get a list of document at a given time. if there is no document in the list returns an empty list.
     * <p>
     * elements are taken from archive and from non archived mapping if the process is still running
     * </p>
     *
     * @param documentName the name of the document list
     * @param processInstanceId the id of the process instance that contains the list
     * @param time time when the list was like that
     * @return the list of document
     * @since 6.4.0
     */
    List<SMappedDocument> getDocumentList(String documentName, long processInstanceId, long time) throws SBonitaReadException;

    /**
     * Remove the content of an archived document while keeping it's metadata.
     * <p>
     * After calling this method you will not be able to retrieve the content of the document since it will be erased from the database.
     * This method can be useful for keeping history of a document without overloading the database.
     * </p>
     *
     * @param documentId the id of the archived document to remove content on
     * @throws SObjectNotFoundException
     * @since 6.4.0
     */
    void deleteContentOfArchivedDocument(long archivedDocumentId) throws SObjectNotFoundException, SBonitaReadException, SRecorderException;

    /**
     * update the document having the documentId with this new version
     * 
     * @param documentId the id of the document to update
     * @param sDocument the new version of the document @return
     */
    SMappedDocument updateDocument(long documentId, SDocument sDocument) throws SObjectNotFoundException, SObjectModificationException, SBonitaReadException;
}
