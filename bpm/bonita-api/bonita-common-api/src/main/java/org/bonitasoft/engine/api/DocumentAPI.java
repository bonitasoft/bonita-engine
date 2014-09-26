/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
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
     * Attach a document by reference to the specified process instance.<br/>
     * The document itself does not contain content but is a reference to external content specified by its URL.
     *
     * @param processInstanceId
     *            The identifier of the process instance
     * @param documentName
     *            The name of the document
     * @param fileName
     *            The filename of the document content
     * @param mimeType
     *            The MimeType of the document content (optional)
     * @param url
     *            The URL of the document content
     * @return a document object
     * @throws ProcessInstanceNotFoundException
     *             when the processInstanceId does not refer to an existing process instance
     * @throws InvalidSessionException
     *             when the session is not valid
     * @throws DocumentAttachmentException
     *             when an error occurs while attaching the document
     * @since 6.0
     */
    Document attachDocument(long processInstanceId, String documentName, String fileName, String mimeType, String url) throws ProcessInstanceNotFoundException,
            DocumentAttachmentException;

    /**
     * Attach a document by reference to the specified process instance.<br/>
     * The document itself does not contain content but is a reference to external content specified by its URL.
     *
     * @param processInstanceId
     *            The identifier of the process instance
     * @param documentName
     *            The name of the document
     * @param fileName
     *            The filename of the document content
     * @param mimeType
     *            The MimeType of the document content (optional)
     * @param url
     *            The URL of the document content
     * @param description
     *              The description for the document
     * @return a document object
     * @throws ProcessInstanceNotFoundException
     *             when the processInstanceId does not refer to an existing process instance
     * @throws InvalidSessionException
     *             when the session is not valid
     * @throws DocumentAttachmentException
     *             when an error occurs while attaching the document
     * @since 6.4
     */
    Document attachDocument(long processInstanceId, String documentName, String fileName, String mimeType, String url, String description) throws ProcessInstanceNotFoundException,
            DocumentAttachmentException;

    /**
     * Attach the given document to the specified process instance.<br />
     * The content is stored to enable later retrieval.
     *
     * @param processInstanceId
     *            The identifier of the process instance
     * @param documentName
     *            The name of the document
     * @param fileName
     *            The name of the file containing the document
     * @param mimeType
     *            The MimeType of the document content (optional)
     * @param documentContent
     *            The content of the document
     * @return a document object
     * @throws ProcessInstanceNotFoundException
     **             when the processInstanceId does not refer to an existing process instance
     * @throws InvalidSessionException
     *             when the session is not valid
     * @throws DocumentAttachmentException
     *             when an error occurs while attaching the document
     * @since 6.0
     */
    Document attachDocument(long processInstanceId, String documentName, String fileName, String mimeType, byte[] documentContent)
            throws ProcessInstanceNotFoundException, DocumentAttachmentException;
    /**
     * Attach the given document to the specified process instance.<br />
     * The content is stored to enable later retrieval.
     *
     * @param processInstanceId
     *            The identifier of the process instance
     * @param documentName
     *            The name of the document
     * @param fileName
     *            The name of the file containing the document
     * @param mimeType
     *            The MimeType of the document content (optional)
     * @param documentContent
     *            The content of the document
     * @param description
     *              The description for the document
     * @return a document object
     * @throws ProcessInstanceNotFoundException
     **             when the processInstanceId does not refer to an existing process instance
     * @throws InvalidSessionException
     *             when the session is not valid
     * @throws DocumentAttachmentException
     *             when an error occurs while attaching the document
     * @since 6.4
     */
    Document attachDocument(long processInstanceId, String documentName, String fileName, String mimeType, byte[] documentContent, String description)
            throws ProcessInstanceNotFoundException, DocumentAttachmentException;


    /**
     * Attach a new version of a document by reference to the specified process instance. The referenced document is
     * a new version of the named document.
     *
     * @param processInstanceId
     *            The identifier of the process instance
     * @param documentName
     *            The name of the document
     * @param fileName
     *            The name of the file containing the document
     * @param mimeType
     *            The MimeType of the document content (optional)
     * @param url
     *            The URL of the document content
     * @return a document object
     * @throws InvalidSessionException
     *             when the session is not valid
     * @throws DocumentAttachmentException
     *             when an error occurs while attaching the new version of the document
     * @since 6.0
     */
    Document attachNewDocumentVersion(long processInstanceId, String documentName, String fileName, String mimeType, String url)
            throws DocumentAttachmentException;
    /**
     * Attach a new version of a document by reference to the specified process instance. The referenced document is
     * a new version of the named document.
     * 
     * @param processInstanceId
     *            The identifier of the process instance
     * @param documentName
     *            The name of the document
     * @param fileName
     *            The name of the file containing the document
     * @param mimeType
     *            The MimeType of the document content (optional)
     * @param url
     *            The URL of the document content
     * @param description
     *              The description for the document
     * @return a document object
     * @throws InvalidSessionException
     *             when the session is not valid
     * @throws DocumentAttachmentException
     *             when an error occurs while attaching the new version of the document
     * @since 6.4
     */
    Document attachNewDocumentVersion(long processInstanceId, String documentName, String fileName, String mimeType, String url, String description)
            throws DocumentAttachmentException;

    /**
     * Attach a new document version to the specified process instance. The document is a new version of the named document.<br />
     * The content is stored to enable later retrieval.
     *
     * @param processInstanceId
     *            The identifier of the process instance
     * @param documentName
     *            The name of the document
     * @param contentFileName
     *            The name of the file containing the content of the document
     * @param contentMimeType
     *            The MimeType of the document content (optional)
     * @param documentContent
     *            The content of the document
     * @return a document object
     * @throws InvalidSessionException
     *             when the session is not valid
     * @throws DocumentAttachmentException
     *             when an error occurs while attaching the new version of the document
     * @since 6.0
     */
    Document attachNewDocumentVersion(long processInstanceId, String documentName, String contentFileName, String contentMimeType, byte[] documentContent)
            throws DocumentAttachmentException;
    /**
     * Attach a new document version to the specified process instance. The document is a new version of the named document.<br />
     * The content is stored to enable later retrieval.
     *
     * @param processInstanceId
     *            The identifier of the process instance
     * @param documentName
     *            The name of the document
     * @param contentFileName
     *            The name of the file containing the content of the document
     * @param contentMimeType
     *            The MimeType of the document content (optional)
     * @param documentContent
     *            The content of the document
     * @param description
     *              The description for the document
     * @return a document object
     * @throws InvalidSessionException
     *             when the session is not valid
     * @throws DocumentAttachmentException
     *             when an error occurs while attaching the new version of the document
     * @since 6.4
     */
    Document attachNewDocumentVersion(long processInstanceId, String documentName, String contentFileName, String contentMimeType, byte[] documentContent, String description)
            throws DocumentAttachmentException;

    /**
     * Get the document with the specified identifier.
     *
     * @param documentId
     *            The identifier of the document to retrieve
     * @return a document object
     * @throws DocumentNotFoundException
     *             when the document identifier does not refer to an existing document
     * @throws InvalidSessionException
     *             when the session is not valid
     * @since 6.4
     */
    Document getDocument(long documentId) throws DocumentNotFoundException;

    /**
     * Remove the document with the specified identifier and returns it.
     *
     * @param documentId
     *            The identifier of the document to retrieve
     * @return a document object
     * @throws DocumentNotFoundException
     *             when the document identifier does not refer to an existing document
     * @throws InvalidSessionException
     *             when the session is not valid
     * @since 6.0
     */
    Document removeDocument(long documentId) throws DocumentNotFoundException, DeletionException;

    /**
     * Get the latest version of all documents attached to the specified process instance.
     * 
     * @param processInstanceId
     *            The identifier of the process instance
     * @param pageIndex
     *            The index of the page
     * @param numberPerPage
     *            The number of documents to list per page
     * @param pagingCriterion
     *            the sort criterion for the returned list
     * @return the matching list of documents
     *         a paginated list of the latest version of each document attached to the process instance
     * @throws ProcessInstanceNotFoundException
     *             when the specified processInstanceId does not refer to an existing process instance
     * @throws InvalidSessionException
     *             when the session is not valid
     * @throws DocumentException
     *             when any other error occurs during document handling
     * @since 6.0
     */
    List<Document> getLastVersionOfDocuments(long processInstanceId, int pageIndex, int numberPerPage, DocumentCriterion pagingCriterion)
            throws ProcessInstanceNotFoundException, DocumentException;

    /**
     * Get content of the document with the specified identifier.
     * 
     * @param storageId
     *            the id of the document to retrieve the content from
     * @return document content as a byte array
     * @throws DocumentNotFoundException
     *             when the specified documentId does not refer to an existing document
     * @throws InvalidSessionException
     *             when the session is note valid
     * @since 6.0
     */
    byte[] getDocumentContent(String storageId) throws DocumentNotFoundException;

    /**
     * Get the last version of the named document for the specified process instance.
     * 
     * @param processInstanceId
     *            The identifier of the process instance that the document is attached to
     * @param documentName
     *            The name of the document
     * @return a document object
     * @throws InvalidSessionException
     *             when the session is not valid
     * @throws DocumentNotFoundException
     *             when the specified documentName does not refer to an existing document attached to this process instance
     * @since 6.0
     */
    Document getLastDocument(long processInstanceId, String documentName) throws DocumentNotFoundException;

    /**
     * Get the version of the named document that was current when the specified process instance is instantiated.
     * 
     * @param processInstanceId
     *            The identifier of the process instance
     * @param documentName
     *            The name of the document
     * @return a document object
     * @throws InvalidSessionException
     *             when the session is not valid
     * @throws DocumentNotFoundException
     *             when the specified documentName does not refer to a document attached to the specified process instance
     * @since 6.0
     */
    Document getDocumentAtProcessInstantiation(long processInstanceId, String documentName) throws DocumentNotFoundException;

    /**
     * Get the version of the named document when the specified activity completed.
     * 
     * @param activityInstanceId
     *            The identifier of the activity instance
     * @param documentName
     *            The name of the document
     * @return a document object
     * @throws DocumentNotFoundException
     *             when the specified documentName does not refer to an existing document attached to the process instance that contains the activity
     * @throws InvalidSessionException
     *             when the session is not valid
     * @since 6.0
     */
    Document getDocumentAtActivityInstanceCompletion(long activityInstanceId, String documentName) throws DocumentNotFoundException;

    /**
     * Get the number of documents attached to the specified process instance. A document with multiple versions is counted once.
     * 
     * @param processInstanceId
     *            The process instance identifier
     * @return the number of documents in the specified process instance
     * @throws InvalidSessionException
     *             when the session is not valid
     * @throws DocumentException
     *             when an error occurs during document handling
     * @since 6.0
     */
    long getNumberOfDocuments(long processInstanceId) throws DocumentException;

    /**
     * Search for documents that match the search options.
     * 
     * @param searchOptions
     *            A {@link SearchOptions} object defining the search options
     * @return the matching document list and its total number
     * @throws InvalidSessionException
     *             when the session is not valid
     * @throws SearchException
     *             when an error occurs during the search
     * @since 6.0
     */
    SearchResult<Document> searchDocuments(SearchOptions searchOptions) throws SearchException;

    /**
     * Search for documents that match the search options and are supervised by the specified user.
     * 
     * @param userId
     *            The identifier of the supervising user
     * @param searchOptions
     *            A {@link SearchOptions} object defining the search options
     * @return the list of matching documents and the number of such documents
     * @throws InvalidSessionException
     *             when the session is not valid
     * @throws UserNotFoundException
     *             when the specified userId does not refer to an existing user
     * @throws SearchException
     *             when an error occurs during the search
     * @since 6.0
     */
    SearchResult<Document> searchDocumentsSupervisedBy(long userId, SearchOptions searchOptions) throws UserNotFoundException, SearchException;

    /**
     * Search for archived documents that meet the search options. An archived document is a document that is not the latest version.
     * 
     * @param searchOptions
     *            A {@link SearchOptions} object defining the search options
     * @return the matching archived document list and its total number
     * @throws InvalidSessionException
     *             when the session is not valid
     * @throws SearchException
     *             when an error occurs during the search
     * @since 6.0
     */
    SearchResult<ArchivedDocument> searchArchivedDocuments(SearchOptions searchOptions) throws SearchException;

    /**
     * Search for archived documents that match the search options and are supervised by the specified user. An archived document is a document that is not the
     * latest version.
     * 
     * @param userId
     *            The identifier of the supervising user
     * @param searchOptions
     *            A {@link SearchOptions} object defining the search options
     * @return the matching archived document list and its total number
     * @throws InvalidSessionException
     *             when the session is not valid
     * @throws UserNotFoundException
     *             when the specified userId does not refer to an existing user
     * @throws SearchException
     *             when an error occurs during the search
     * @since 6.0
     */
    SearchResult<ArchivedDocument> searchArchivedDocumentsSupervisedBy(long userId, SearchOptions searchOptions) throws UserNotFoundException, SearchException;

    /**
     * Get an ArchivedDocument based on it's id.
     * 
     * @param sourceObjectId
     *            The identifier of the document
     * @return an archived document
     * @throws InvalidSessionException
     *             when the session is not valid
     * @throws ArchivedDocumentNotFoundException
     *             when the specified identifier does not refer to an archived document
     * @since 6.0
     */
    ArchivedDocument getArchivedProcessDocument(final long sourceObjectId) throws ArchivedDocumentNotFoundException;

    /**
     * Get the original version of the document with the specified identifier.
     * 
     * @param sourceObjectId
     *            The identifier of the document
     * @return an archived document
     * @throws InvalidSessionException
     *             when the session is not valid
     * @throws ArchivedDocumentNotFoundException
     *             when the specified identifier does not refer to an archived document
     * @since 6.0
     */
    ArchivedDocument getArchivedVersionOfProcessDocument(long sourceObjectId) throws ArchivedDocumentNotFoundException;


    List<Document> getDocumentList(long processInstanceId, String name) throws DocumentNotFoundException;

}
