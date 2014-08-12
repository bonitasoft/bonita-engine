/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.document.api;

import java.util.List;

import org.bonitasoft.engine.core.process.document.model.SProcessDocument;
import org.bonitasoft.engine.persistence.OrderByType;

/**
 * @author Emmanuel Duchastenier
 */
public interface SProcessDocumentService {

    SProcessDocument createDocumentForProcessDefinition(long processDefinitionId, String author, String fileName, String mimeType, byte[] content)
            throws SProcessDocumentCreationException, SProcessDocumentAlreadyExistsException, SProcessDocumentException;

    SProcessDocument createDocumentForProcessInstance(long processDefinitionId, long processInstanceId, String author, String fileName, String mimeType,
            byte[] content) throws SProcessDocumentCreationException, SProcessDocumentAlreadyExistsException, SProcessDocumentException;

    // The 2 following methods go into the BOS database to attach an existing document to another process or process definition. They do not call the doc
    // service:
    void attachDocumentToProcessDefinition(long processDefinitionId, String documentId) throws SProcessDocumentAlreadyExistsException,
            SProcessDocumentCreationException;

    void attachDocumentToProcessInstance(long processDefinitionId, long processInstanceId, String documentId) throws SProcessDocumentCreationException,
            SProcessDocumentAlreadyExistsException;

    // SearchResult search(DocumentSearchBuilder builder, int fromIndex, int maxResults);

    SProcessDocument createNewVersion(String documentId, String author, String fileName, String mimeType, byte[] content)
            throws SProcessDocumentCreationException, SProcessDocumentNotFoundException;

    void deleteDocument(String documentId) throws SProcessDocumentDeletionException, SProcessDocumentNotFoundException;

    // void deleteDocumentSeries(String documentId) throws SProcessDocumentDeletionException;

    SProcessDocument getDocument(String documentId) throws SProcessDocumentNotFoundException, SProcessDocumentException;

    /**
     * Retrieve the different versions of a document. Does not retrieve the Process definition, process instance, activity information as a document can be
     * attached to several ones.
     * 
     * @param documentId
     *            the Id of the document to retrieve
     * @param fromIndex
     *            initial index
     * @param maxResults
     *            max number of results to get
     * @param sortFieldName
     *            field name to execute the sort
     * @param order
     *            the sort order 'ASC' or 'DESC'
     * @return
     * @throws SProcessDocumentNotFoundException
     * @throws SProcessDocumentException
     */
    List<SProcessDocument> getVersionsInSeries(String documentId, int fromIndex, int maxResults, String sortFieldName, OrderByType order)
            throws SProcessDocumentNotFoundException, SProcessDocumentException;

    List<SProcessDocument> getLatestDocuments(int fromIndex, int maxResults, String sortFieldName, OrderByType order) throws SProcessDocumentException;

    List<SProcessDocument> getDocumentsForProcessDefinition(long processDefinitionId, int fromIndex, int maxResults, String sortFieldName, OrderByType order)
            throws SProcessDocumentException;

    List<SProcessDocument> getDocumentsForProcessInstance(long processDefinitionId, long processInstanceId, int fromIndex, int maxResults,
            String sortFieldName, OrderByType order) throws SProcessDocumentException;

    // Not used for the moment:
    // List<SProcessDocument> getDocumentsForActivity(long processDefinitionId, long processInstanceId, long activityId, int fromIndex, int maxResults,
    // String sortFieldName, OrderByType order) throws SProcessDocumentException;

    // The Web layer already limits the max size of the file upload:
    byte[] getDocumentContents(String documentId) throws SProcessDocumentNotFoundException, SProcessDocumentException;

    long getNumberOfDocuments() throws SProcessDocumentException;

}
