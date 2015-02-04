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
package org.bonitasoft.engine.api;

import java.util.List;

import org.bonitasoft.engine.bpm.document.ArchivedDocument;
import org.bonitasoft.engine.bpm.document.ArchivedDocumentNotFoundException;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentAttachmentException;
import org.bonitasoft.engine.bpm.document.DocumentCriterion;
import org.bonitasoft.engine.bpm.document.DocumentException;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;

/**
 * Manipulate documents that are attached to a process instance.
 * <p>
 * A document can be stored directly with the process instance, or by reference. If you store a document by reference, the process instance contains a document
 * object that has metadata describing the document: its name, content MimeType, the name of the file of the document, and a URL giving the location of the
 * document. The choice of direct local storage or storage by reference depends on the performance profile of document access within an instance of the process.
 * If you require frequent and rapid access to update the document and the document is not large, use direct storage. If the document is large or is not
 * accessed frequently within a process instance, or is not updated by the process, store it by reference.
 * <p>
 * Multiple versions of a document can be stored. You can retrieve the latest version or the version that was current at a given milestone (for example process
 * instantiation, or activity completion).
 *
 * @author Emmanuel Duchastenier
 * @author Baptiste Mesta
 */
public interface DocumentAPI {

    /**
     * Attach a document by reference to the specified process instance.
     * <p>
     * The document itself does not contain content but is a reference to external content specified by its URL.
     * </p>
     *
     * @param processInstanceId
     *        The identifier of the process instance
     * @param documentName
     *        The name of the document
     * @param fileName
     *        The filename of the document content
     * @param mimeType
     *        The MimeType of the document content (optional)
     * @param url
     *        The URL of the document content
     * @return a document object
     * @throws ProcessInstanceNotFoundException
     *         If the identifier does not refer to an existing process instance.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws DocumentAttachmentException
     *         when an error occurs while attaching the document
     * @since 6.0
     */
    Document attachDocument(long processInstanceId, String documentName, String fileName, String mimeType, String url) throws ProcessInstanceNotFoundException,
            DocumentAttachmentException;

    /**
     * Attach a new document version to a process instance.
     * <p>
     * Depending on the DocumentValue given the document will be internal (with content) or external (with url).
     * The document state is archived and is then updated to the new version
     * </p>
     *
     * @param documentId
     *        The identifier of the document to update
     * @param documentValue
     *        The value of the document
     * @return a document object
     * @throws ProcessInstanceNotFoundException
     *         If the identifier does not refer to an existing process instance.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws DocumentAttachmentException
     *         when an error occurs while attaching the document
     * @throws org.bonitasoft.engine.exception.AlreadyExistsException
     *         If the document already exists.
     * @since 6.4.0
     */
    Document updateDocument(long documentId, DocumentValue documentValue) throws ProcessInstanceNotFoundException,
            DocumentAttachmentException, AlreadyExistsException;

    /**
     * Attach a new document to a process instance.
     * <br>
     * Depending on the DocumentValue given the document will be internal (with content) or external (with url).
     *  <ol>
     *      <li>If the target document is a list of document then we append it to the list</li>
     *      <li>If the target document is a list of document and the index is set on the document value then we insert the element in the list at the specified
     *      index</li>
     *      <li>If the target single document or is non existent in the definition we create it</li>
     *      <li>If the target single document and is already existent an exception is thrown</li>
     *  </ol>
     *
     * @param processInstanceId
     *        The identifier of the process instance
     * @param documentName
     *        The name of the document
     * @param description
     *        The description of the document
     * @param documentValue
     *        The value of the document
     * @return a document object
     * @throws ProcessInstanceNotFoundException
     *         If the identifier does not refer to an existing process instance.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws DocumentAttachmentException
     *         when an error occurs while attaching the document
     * @throws org.bonitasoft.engine.exception.AlreadyExistsException
     *         If the document already exists.
     * @since 6.4.0
     */
    Document addDocument(long processInstanceId, String documentName, String description, DocumentValue documentValue)
            throws ProcessInstanceNotFoundException, DocumentAttachmentException, AlreadyExistsException;

    /**
     * Attach the given document to the specified process instance.
     * <p>
     * The content is stored to enable later retrieval.
     * </p>
     *
     * @param processInstanceId
     *        The identifier of the process instance
     * @param documentName
     *        The name of the document
     * @param fileName
     *        The name of the file containing the document
     * @param mimeType
     *        The MimeType of the document content (optional)
     * @param documentContent
     *        The content of the document
     * @return a document object
     * @throws ProcessInstanceNotFoundException
     **         If the identifier does not refer to an existing process instance.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws DocumentAttachmentException
     *         when an error occurs while attaching the document
     * @since 6.0
     */
    Document attachDocument(long processInstanceId, String documentName, String fileName, String mimeType, byte[] documentContent)
            throws ProcessInstanceNotFoundException, DocumentAttachmentException;

    /**
     * Attach a new version of a document by reference to the specified process instance. The referenced document is
     * a new version of the named document.
     *
     * @param processInstanceId
     *        The identifier of the process instance
     * @param documentName
     *        The name of the document
     * @param fileName
     *        The name of the file containing the document
     * @param mimeType
     *        The MimeType of the document content (optional)
     * @param url
     *        The URL of the document content
     * @return a document object
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws DocumentAttachmentException
     *         when an error occurs while attaching the new version of the document
     * @since 6.0
     */
    Document attachNewDocumentVersion(long processInstanceId, String documentName, String fileName, String mimeType, String url)
            throws DocumentAttachmentException;

    /**
     * Attach a new document version to the specified process instance. The document is a new version of the named document.
     * <p>
     * The content is stored to enable later retrieval.
     * </p>
     *
     * @param processInstanceId
     *        The identifier of the process instance
     * @param documentName
     *        The name of the document
     * @param contentFileName
     *        The name of the file containing the content of the document
     * @param contentMimeType
     *        The MimeType of the document content (optional)
     * @param documentContent
     *        The content of the document
     * @return a document object
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws DocumentAttachmentException
     *         when an error occurs while attaching the new version of the document
     * @since 6.0
     */
    Document attachNewDocumentVersion(long processInstanceId, String documentName, String contentFileName, String contentMimeType, byte[] documentContent)
            throws DocumentAttachmentException;

    /**
     * Get the document with the specified identifier.
     *
     * @param documentId
     *        The identifier of the document to retrieve
     * @return a document object
     * @throws DocumentNotFoundException
     *         If the specified identifier does not refer to an existing document.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @since 6.0
     */
    Document getDocument(long documentId) throws DocumentNotFoundException;

    /**
     * Remove the document with the specified identifier and returns it.
     * <p>
     * this archive and delete mapping on the process, i.e. the content of the document itself will be kept in database, use
     * {@link #deleteContentOfArchivedDocument} to delete the content
     * </p>
     *
     * @param documentId
     *        The identifier of the document to retrieve
     * @return the removed document object
     * @throws DocumentNotFoundException
     *         If the specified identifier does not refer to an existing document.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @since 6.4.0
     */
    Document removeDocument(long documentId) throws DocumentNotFoundException, DeletionException;

    /**
     * Get the latest version of all documents attached to the specified process instance.
     *
     * @param processInstanceId
     *        The identifier of the process instance
     * @param pageIndex
     *        The index of the page
     * @param numberPerPage
     *        The number of documents to list per page
     * @param pagingCriterion
     *        The sort criterion for the returned list
     * @return the matching list of documents
     *         a paginated list of the latest version of each document attached to the process instance
     * @throws ProcessInstanceNotFoundException
     *         If the identifier does not refer to an existing process instance.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws DocumentException
     *         when any other error occurs during document handling
     * @since 6.0
     */
    List<Document> getLastVersionOfDocuments(long processInstanceId, int pageIndex, int numberPerPage, DocumentCriterion pagingCriterion)
            throws ProcessInstanceNotFoundException, DocumentException;

    /**
     * Get content of the document with the specified identifier.
     *
     * @param storageId
     *        The identifier of the document to retrieve the content from
     * @return document content as a byte array
     * @throws DocumentNotFoundException
     *         If the specified identifier does not refer to an existing document.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         when the session is note valid
     * @since 6.0
     */
    byte[] getDocumentContent(String storageId) throws DocumentNotFoundException;

    /**
     * Get the last version of the named document for the specified process instance.
     *
     * @param processInstanceId
     *        The identifier of the process instance that the document is attached to
     * @param documentName
     *        The name of the document
     * @return a document object
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws DocumentNotFoundException
     *         If the specified documentName does not refer to an existing document attached to this process instance
     * @since 6.0
     */
    Document getLastDocument(long processInstanceId, String documentName) throws DocumentNotFoundException;

    /**
     * Get the version of the named document that was current when the specified process instance is instantiated.
     *
     * @param processInstanceId
     *        The identifier of the process instance
     * @param documentName
     *        The name of the document
     * @return a document object
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws DocumentNotFoundException
     *         If the specified documentName does not refer to a document attached to the specified process instance.
     * @since 6.0
     */
    Document getDocumentAtProcessInstantiation(long processInstanceId, String documentName) throws DocumentNotFoundException;

    /**
     * Get the version of the named document when the specified activity completed.
     *
     * @param activityInstanceId
     *        The identifier of the activity instance
     * @param documentName
     *        The name of the document
     * @return a document object
     * @throws DocumentNotFoundException
     *         If the specified documentName does not refer to an existing document attached to the process instance that contains the activity.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @since 6.0
     */
    Document getDocumentAtActivityInstanceCompletion(long activityInstanceId, String documentName) throws DocumentNotFoundException;

    /**
     * Get the number of documents attached to the specified process instance. A document with multiple versions is counted once.
     *
     * @param processInstanceId
     *        The process instance identifier
     * @return the number of documents in the specified process instance
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws DocumentException
     *         when an error occurs during document handling
     * @since 6.0
     */
    long getNumberOfDocuments(long processInstanceId) throws DocumentException;

    /**
     * Search for documents that match the search options.
     *
     * @param searchOptions
     *        A {@link SearchOptions} object defining the search options
     * @return the matching document list and its total number
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws SearchException
     *         when an error occurs during the search
     * @since 6.0
     */
    SearchResult<Document> searchDocuments(SearchOptions searchOptions) throws SearchException;

    /**
     * Search for documents that match the search options and are supervised by the specified user.
     *
     * @param userId
     *        The identifier of the supervising user
     * @param searchOptions
     *        A {@link SearchOptions} object defining the search options
     * @return the list of matching documents and the number of such documents
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws UserNotFoundException
     *         when the specified userId does not refer to an existing user
     * @throws SearchException
     *         when an error occurs during the search
     * @since 6.0
     */
    SearchResult<Document> searchDocumentsSupervisedBy(long userId, SearchOptions searchOptions) throws UserNotFoundException, SearchException;

    /**
     * Search for archived documents that meet the search options. An archived document is a document that is not the latest version.
     *
     * @param searchOptions
     *        A {@link SearchOptions} object defining the search options
     * @return the matching archived document list and its total number
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws SearchException
     *         when an error occurs during the search
     * @since 6.0
     */
    SearchResult<ArchivedDocument> searchArchivedDocuments(SearchOptions searchOptions) throws SearchException;

    /**
     * Search for archived documents that match the search options and are supervised by the specified user. An archived document is a document that is not the
     * latest version.
     *
     * @param userId
     *        The identifier of the supervising user
     * @param searchOptions
     *        A {@link SearchOptions} object defining the search options
     * @return the matching archived document list and its total number
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws UserNotFoundException
     *         when the specified userId does not refer to an existing user
     * @throws SearchException
     *         when an error occurs during the search
     * @since 6.0
     */
    SearchResult<ArchivedDocument> searchArchivedDocumentsSupervisedBy(long userId, SearchOptions searchOptions) throws UserNotFoundException, SearchException;

    /**
     * Get an ArchivedDocument based on it's id.
     *
     * @param sourceObjectId
     *        The identifier of the document
     * @return an archived document
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ArchivedDocumentNotFoundException
     *         when the specified identifier does not refer to an archived document
     * @since 6.0
     */
    ArchivedDocument getArchivedProcessDocument(final long sourceObjectId) throws ArchivedDocumentNotFoundException;

    /**
     * Get the original version of the document with the specified identifier.
     *
     * @param sourceObjectId
     *        The identifier of the document
     * @return an archived document
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ArchivedDocumentNotFoundException
     *         If the identifier does not refer to any existing archived document.
     * @since 6.0
     */
    ArchivedDocument getArchivedVersionOfProcessDocument(long sourceObjectId) throws ArchivedDocumentNotFoundException;

    /**
     * Get a document list that have the specified name on the process
     *
     * @param processInstanceId
     *        The identifier of the process instance that contains the list
     * @param name
     *        The name of the document
     * @param fromIndex
     *        The index of the first element to be retrieved (it starts from zero)
     * @param numberOfResult
     *        The max number of result to get
     * @return
     *         The document list
     * @throws DocumentNotFoundException
     * @since 6.4.0
     */
    List<Document> getDocumentList(long processInstanceId, String name, int fromIndex, int numberOfResult) throws DocumentNotFoundException;

    /**
     * Get a document list that have the specified name on the process
     *
     * @param processInstanceId
     *        The identifier of the process instance that contains the list
     * @param name
     *        The name of the document list
     * @param documentsValues the values to set the list with
     * @throws DocumentException
     *         If an error occurs
     * @see org.bonitasoft.engine.bpm.process.ProcessInstance#getId()
     * @since 6.4.0
     */
    void setDocumentList(long processInstanceId, String name, List<DocumentValue> documentsValues) throws DocumentNotFoundException, DocumentException;

    /**
     * Remove the content of an archived document while keeping it's metadata.
     * <p>
     * After calling this method you will not be able to retrieve the content of the document since it will be erased from the database.
     * This method can be useful for keeping history of a document without overloading the database.
     * </p>
     *
     * @param archivedDocumentId
     *        The identifier of the archived document to remove content on
     * @throws DocumentNotFoundException
     *         If the identifier does not refer to any existing archived document.
     * @throws DocumentException
     *         If an error occurs
     * @see ArchivedDocument#getId()
     * @since 6.4.0
     */
    void deleteContentOfArchivedDocument(long archivedDocumentId) throws DocumentException, DocumentNotFoundException;
}
