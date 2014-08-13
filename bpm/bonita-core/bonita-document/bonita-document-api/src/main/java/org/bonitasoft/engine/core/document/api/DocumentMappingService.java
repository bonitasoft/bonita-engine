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

import org.bonitasoft.engine.core.document.exception.SDocumentMappingAlreadyExistsException;
import org.bonitasoft.engine.core.document.exception.SDocumentMappingCreationException;
import org.bonitasoft.engine.core.document.exception.SDocumentMappingDeletionException;
import org.bonitasoft.engine.core.document.exception.SDocumentMappingException;
import org.bonitasoft.engine.core.document.exception.SDocumentMappingNotFoundException;
import org.bonitasoft.engine.core.document.model.SDocumentMapping;
import org.bonitasoft.engine.core.document.model.archive.SADocumentMapping;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;

/**
 * @author Nicolas Chabanoles
 * @author Matthieu Chaffotte
 * @since 6.0
 */
public interface DocumentMappingService {

    String DOCUMENTMAPPING = "DOCUMENTMAPPING";

    /**
     * Create document mapping in DB by given document mapping
     * 
     * @param docMapping
     *            the document mapping object
     * @return the new created document mapping object
     * @throws org.bonitasoft.engine.core.document.exception.SDocumentMappingAlreadyExistsException
     * @throws org.bonitasoft.engine.core.document.exception.SDocumentMappingCreationException
     */
    SDocumentMapping create(SDocumentMapping docMapping) throws SDocumentMappingAlreadyExistsException, SDocumentMappingCreationException;

    /**
     * Delete the specific document mapping
     * 
     * @param documentMapping
     *            the document mapping will be deleted
     * @throws org.bonitasoft.engine.core.document.exception.SDocumentMappingDeletionException
     */
    void delete(SDocumentMapping documentMapping) throws SDocumentMappingDeletionException;

    /**
     * Delete id specified document mapping
     * 
     * @param documentMappingId
     *            identifier of document mapping
     * @throws org.bonitasoft.engine.core.document.exception.SDocumentMappingDeletionException
     */
    void delete(long documentMappingId) throws SDocumentMappingDeletionException;

    /**
     * Get document mapping by its id
     * 
     * @param documentMappingId
     *            identifier of document mapping
     * @return the corresponding document
     * @throws org.bonitasoft.engine.core.document.exception.SDocumentMappingNotFoundException
     */
    SDocumentMapping get(long documentMappingId) throws SDocumentMappingNotFoundException;

    /**
     * Get document mappings for given process instance suit to the criteria
     * 
     * @param processInstanceId
     *            identifier of process instance
     * @param fromIndex
     *            Index of the record to be retrieved from. First record has index 0
     * @param maxResults
     *            Number of result we want to get. Maximum number of result returned
     * @param sortFieldName
     *            the name of filed used to do order
     * @param order
     *            ASC or DESC
     * @return a list of SDocumentMapping objects
     * @throws org.bonitasoft.engine.core.document.exception.SDocumentMappingException
     */
    List<SDocumentMapping> getDocumentMappingsForProcessInstance(long processInstanceId, int fromIndex, int maxResults, String sortFieldName, OrderByType order)
            throws SDocumentMappingException;
    
    /**
     * Get document mappings for given process instance ordered by id
     * 
     * @param processInstanceId
     *            identifier of process instance
     * @param fromIndex
     *            Index of the record to be retrieved from. First record has index 0
     * @param maxResults
     *            Number of result we want to get. Maximum number of result returned
     * @return a list of SDocumentMapping objects
     * @throws org.bonitasoft.engine.core.document.exception.SDocumentMappingException
     */
    List<SDocumentMapping> getDocumentMappingsForProcessInstanceOrderedById(long processInstanceId, int fromIndex, int maxResults)
            throws SDocumentMappingException;

    /**
     * Get document mapping by its name in a process instance
     * 
     * @param processInstanceId
     *            identifier of process instance
     * @param documentName
     *            name of document
     * @return the document
     * @throws org.bonitasoft.engine.core.document.exception.SDocumentMappingNotFoundException
     */
    SDocumentMapping get(long processInstanceId, String documentName) throws SDocumentMappingNotFoundException;

    /**
     * Get total number of document mappings in a specific process instance
     * 
     * @param processInstanceId
     *            identifier of process instance
     * @return number of document mappings in the specific process instance
     * @throws org.bonitasoft.engine.core.document.exception.SDocumentMappingException
     */
    long getNumberOfDocumentMappingsForProcessInstance(long processInstanceId) throws SDocumentMappingException;

    /**
     * Update the specific docMapping
     * 
     * @param docMapping
     *            the document mapping used to do update
     * @return the updated document mapping
     * @throws org.bonitasoft.engine.core.document.exception.SDocumentMappingException
     */
    SDocumentMapping update(SDocumentMapping docMapping) throws SDocumentMappingException;

    /**
     * Get archive document mapping archived after specific time by its name in a process instance
     * 
     * @param processInstanceId
     *            identifier of process instance
     * @param documentName
     *            name of document
     * @param time
     *            the archive time
     * @return the archived document mapping suit to the criteria
     * @throws org.bonitasoft.engine.core.document.exception.SDocumentMappingNotFoundException
     */
    SADocumentMapping get(long processInstanceId, String documentName, long time) throws SDocumentMappingNotFoundException;

    /**
     * archive the specific document mapping in the archive date
     * 
     * @param docMapping
     *            document mapping will be archived
     * @param archiveDate
     *            the archive time
     * @throws org.bonitasoft.engine.core.document.exception.SDocumentMappingException
     */
    void archive(SDocumentMapping docMapping, long archiveDate) throws SDocumentMappingException;

    /**
     * Get number of document mappings suit to the criteria
     * 
     * @param queryOptions
     *            a QueryOptions object containing some query conditions
     * @return the number of document mappings suit to the criteria
     * @throws org.bonitasoft.engine.persistence.SBonitaSearchException
     */
    long getNumberOfDocuments(QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Search a list of document mapping suit to query criteria
     * 
     * @param queryOptions
     *            a QueryOptions object containing some query conditions
     * @return a list of SDocumentMapping objects
     * @throws org.bonitasoft.engine.persistence.SBonitaSearchException
     */
    List<SDocumentMapping> searchDocuments(QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Get number of document mappings supervised by the specific user
     * 
     * @param userId
     *            identifier of user
     * @param queryOptions
     *            a QueryOptions object containing some query conditions
     * @return number of document mappings supervised by the specific user and suit to the query criteria
     * @throws org.bonitasoft.engine.persistence.SBonitaSearchException
     */
    long getNumberOfDocumentsSupervisedBy(long userId, QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Search all document mappings supervised by the specific user
     * 
     * @param userId
     *            identifier of user
     * @param queryOptions
     *            a QueryOptions object containing some query conditions
     * @return a list of SDocumentMapping objects
     * @throws org.bonitasoft.engine.persistence.SBonitaSearchException
     */
    List<SDocumentMapping> searchDocumentsSupervisedBy(long userId, QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Get number of archived documents suit to the query criteria
     * 
     * @param queryOptions
     *            a QueryOptions object containing some query conditions
     * @param persistenceService
     *            persistenceService used to do the search
     * @return number of archived documents suit to the query criteria
     * @throws org.bonitasoft.engine.persistence.SBonitaSearchException
     */
    long getNumberOfArchivedDocuments(QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Search all archived documents suit to the query criteria
     * 
     * @param queryOptions
     *            a QueryOptions object containing some query conditions
     * @return a list of SDocumentMapping objects
     * @throws org.bonitasoft.engine.persistence.SBonitaSearchException
     */
    List<SADocumentMapping> searchArchivedDocuments(QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Get number of archived documents supervised by the specific user and suit to the query criteria
     * 
     * @param userId
     *            identifier of user
     * @param queryOptions
     *            a QueryOptions object containing some query conditions
     * @return number of archived documents suit to the query criteria
     * @throws org.bonitasoft.engine.persistence.SBonitaSearchException
     */
    long getNumberOfArchivedDocumentsSupervisedBy(long userId, QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Search all archived documents supervised by the specific user and suit to the query criteria
     * 
     * @param userId
     *            identifier of user
     * @param queryOptions
     *            a QueryOptions object containing some query conditions
     * @return a list of SDocumentMapping objects
     * @throws org.bonitasoft.engine.persistence.SBonitaSearchException
     */
    List<SADocumentMapping> searchArchivedDocumentsSupervisedBy(long userId, QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Get archived document mapping by its id
     * 
     * @param documentId
     *            identifier of the archived document mapping
     * @return the archived document mapping object
     * @throws org.bonitasoft.engine.core.document.exception.SDocumentMappingNotFoundException
     */
    SADocumentMapping getArchivedDocument(long documentId) throws SDocumentMappingNotFoundException;

    /**
     * Retrieve the archive of a version of a document
     * 
     * @param documentId
     *            the document id of the document to retrieve
     * @return the corresponding document mapping
     * @throws org.bonitasoft.engine.core.document.exception.SDocumentMappingNotFoundException
     *             when the document does not exists
     */
    SADocumentMapping getArchivedVersionOfDocument(long documentId) throws SDocumentMappingNotFoundException;

    /**
     * Delete the specific document mapping
     * 
     * @param documentMapping
     *            the document mapping will be deleted
     * @throws org.bonitasoft.engine.core.document.exception.SDocumentMappingDeletionException
     */
    void delete(SADocumentMapping documentMapping) throws SDocumentMappingDeletionException;

}
