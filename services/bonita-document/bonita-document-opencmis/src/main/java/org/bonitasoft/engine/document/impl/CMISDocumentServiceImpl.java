/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.document.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.document.CmisUserProvider;
import org.bonitasoft.engine.document.DocumentService;
import org.bonitasoft.engine.document.SDocumentDeletionException;
import org.bonitasoft.engine.document.SDocumentException;
import org.bonitasoft.engine.document.SDocumentNotFoundException;
import org.bonitasoft.engine.document.SDocumentStorageException;
import org.bonitasoft.engine.document.model.SDocument;
import org.bonitasoft.engine.document.model.SDocumentBuilder;
import org.bonitasoft.engine.document.model.SDocumentBuilderFactory;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionIdNotSetException;

/**
 * @author Emmanuel Duchastenier
 * @author Nicolas Chabanoles
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class CMISDocumentServiceImpl implements DocumentService {

    private final SessionAccessor sessionAccessor;

    private final SessionService sessionService;

    private final String cmisServerUrl;

    private final String repositoryId;

    private final CmisUserProvider cmisUserProvider;

    /**
     * a Map to store the session per user
     */
    private final Map<String, Session> sessionsMap = new HashMap<String, Session>();

    private static final String ROOT_PATH = "/";

    private static String rootFolderId = null;

    /**
     * @param tenandId
     *            one instance of the service per tenant
     * @param cmisServerUrl
     *            Url of the CMIS
     * @param repositoryId
     * @param cmisUserProvider
     * @param categoryFolder
     * @throws SessionNotFoundException
     */
    public CMISDocumentServiceImpl(final SessionAccessor sessionAccessor, final SessionService sessionService, final String cmisServerUrl,
            final String repositoryId, final CmisUserProvider cmisUserProvider) {
        super();
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
        this.cmisServerUrl = cmisServerUrl;
        this.repositoryId = repositoryId;
        this.cmisUserProvider = cmisUserProvider;
        // FIXME cmisUserProvider no more usefull (was here only to have bonita authors on xcmis)

        final AuthAwareCookieManager cm = new AuthAwareCookieManager(null, java.net.CookiePolicy.ACCEPT_ALL);
        java.net.CookieHandler.setDefault(cm);
    }

    @Override
    public SDocument storeDocumentContent(final SDocument sDocument, final byte[] documentContent) throws SDocumentStorageException {
        checkMimeType(sDocument);
        final ContentStream contentStream = buildContentStream(sDocument, documentContent);
        final Document cmisDocument = uploadDocumentAndItsContentOnServer(sDocument, contentStream);
        // FIXME do not use server metadata as document properties, use ours
        return toSDocument(cmisDocument);
    }

    @Override
    public void deleteDocumentContent(final String documentId) throws SDocumentNotFoundException, SDocumentDeletionException {
        // final Session session = getSession(String.valueOf(sDocument.getAuthor()));
        // final Folder newFolder = createFolder(session, Long.toString(UUID.randomUUID().getMostSignificantBits()), rootFolderId);
        Session session;
        try {
            session = getSession();
            // deleteDocument(session, sDocument.getStorageId(), true);
            deleteDocument(session, documentId, true);
        } catch (SSessionNotFoundException e) {
            throw new SDocumentDeletionException(e);
        } catch (SessionIdNotSetException e) {
            throw new SDocumentDeletionException(e);
        }
    }

    private Document uploadDocumentAndItsContentOnServer(final SDocument sDocument, final ContentStream contentStream) throws SDocumentStorageException {
        try {
            final Map<String, String> newDocProps = buildDocumentProperties(sDocument);
            final Session session = getSession(String.valueOf(sDocument.getAuthor()));
            final Folder newFolder = createFolder(session, Long.toString(UUID.randomUUID().getMostSignificantBits()), rootFolderId);
            return newFolder.createDocument(newDocProps, contentStream, null, null, null, null, session.getDefaultContext());
        } catch (final CmisBaseException e) {
            throw new SDocumentStorageException("Can't create a document named: " + sDocument.getContentFileName() + "", e);
        }
    }

    private Map<String, String> buildDocumentProperties(final SDocument sDocument) {
        final Map<String, String> newDocProps = new HashMap<String, String>(3);
        newDocProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        newDocProps.put(PropertyIds.CONTENT_STREAM_FILE_NAME, sDocument.getContentFileName());
        newDocProps.put(PropertyIds.NAME, sDocument.getContentFileName());
        return newDocProps;
    }

    private ContentStream buildContentStream(final SDocument sDocument, final byte[] documentContent) throws SDocumentStorageException {
        ContentStream contentStream = null;
        if (documentContent != null && documentContent.length > 0) {
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(documentContent);
            try {
                contentStream = new ContentStreamImpl(sDocument.getContentFileName(), BigInteger.valueOf(documentContent.length),
                        sDocument.getContentMimeType(), byteArrayInputStream);
            } catch (final CmisBaseException e) {
                throw new SDocumentStorageException("Can't create the content of the document " + sDocument.getContentFileName() + "", e);
            } finally {
                try {
                    byteArrayInputStream.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return contentStream;
    }

    private void checkMimeType(final SDocument sDocument) throws SDocumentStorageException {
        if (sDocument.getContentMimeType() != null) {
            try {
                new MimeType(sDocument.getContentMimeType());
            } catch (final MimeTypeParseException e) {
                throw new SDocumentStorageException("Document MIME type not valid", e);
            }
        }
    }

    private void deleteDocument(final Session session, final String documentId, final boolean allVersions) throws SDocumentNotFoundException {
        try {
            session.getBinding().getObjectService().deleteObject(repositoryId, documentId, allVersions, null);
        } catch (final CmisObjectNotFoundException e) {
            throw new SDocumentNotFoundException(documentId);
        }
    }

    @Override
    public byte[] getContent(final String documentId) throws SDocumentException {
        try {
            final Session session = getSession();
            final Document doc = (Document) session.getObject(session.createObjectId(documentId));
            return getDocumentContent(doc);
        } catch (final CmisBaseException e) {
            throw new SDocumentNotFoundException(documentId);
        } catch (final SSessionNotFoundException e) {
            throw new SDocumentException("Error accessing session before retrieving document content with ID " + documentId, e);
        } catch (final SessionIdNotSetException e) {
            throw new SDocumentException("Error accessing session before retrieving document content with ID " + documentId, e);
        }
    }

    private byte[] getDocumentContent(final Document doc) throws SDocumentException {
        if (doc.getContentStreamLength() == 0) {
            return new byte[0]; // no contents
        }
        final ContentStream contentStream = doc.getContentStream();
        if (contentStream != null) {
            final InputStream stream = contentStream.getStream();
            try {
                return IOUtils.toByteArray(stream);
            } catch (final IOException e) {
                throw new SDocumentException("Error accessing session before retrieving document content with ID " + doc.getId(), e);
            } finally {
                try {
                    stream.close();
                } catch (final IOException e) {
                    // Nothing more can be done here
                }
            }
        }
        return new byte[0];
    }

    /**
     * @param session
     *            the CMIS session
     * @param folderName
     *            the new folder name to create
     * @param parentFolderId
     *            the ID of the parent folder where to create the folder into
     * @return the folderId just created
     * @throws SDocumentException
     */

    private Folder createFolder(final Session session, final String folderName, final String parentFolderId) throws SDocumentStorageException {
        final Folder folder;
        try {
            folder = (Folder) session.getObject(session.createObjectId(parentFolderId));
            final Map<String, Object> properties = buildFolderProperties(folderName, parentFolderId);
            return folder.createFolder(properties, null, null, null, session.getDefaultContext());
        } catch (final CmisRuntimeException e) {
            throw new SDocumentStorageException("Folder creation Exception for " + folderName, e);
        }
    }

    private Map<String, Object> buildFolderProperties(final String folderName, final String parentFolderId) {
        final Map<String, Object> properties;
        properties = new HashMap<String, Object>(3);
        properties.put(PropertyIds.NAME, folderName);
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        properties.put(PropertyIds.PARENT_ID, parentFolderId);
        return properties;
    }

    public synchronized Session createSessionById(final String repositoryId, final String userId) {
        final SessionFactory f = SessionFactoryImpl.newInstance();
        // userId cannot be null here:
        Session session = sessionsMap.get(userId);
        if (session == null) {
            final Map<String, String> parameter = setParameters(cmisUserProvider.getUser(userId), cmisUserProvider.getPassword(userId));
            parameter.put(SessionParameter.REPOSITORY_ID, repositoryId);
            session = f.createSession(parameter);
            if (rootFolderId == null) {
                final CmisObject rootFolder = session.getObjectByPath(ROOT_PATH);
                rootFolderId = rootFolder.getId();
            }
            sessionsMap.put(userId, session);
        }
        return session;
    }

    public synchronized Session getSession() throws SSessionNotFoundException, SessionIdNotSetException {
        return getSession(sessionService.getSession(sessionAccessor.getSessionId()).getUserName());
    }

    public synchronized Session getSession(final String userId) {
        return createSessionById(repositoryId, userId);
    }

    protected Map<String, String> setParameters(final String username, final String password) {
        final Map<String, String> parameter = new HashMap<String, String>(6);
        parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
        parameter.put(SessionParameter.ATOMPUB_URL, cmisServerUrl);
        parameter.put(SessionParameter.USER, username);
        parameter.put(SessionParameter.PASSWORD, password);
        parameter.put(SessionParameter.CACHE_SIZE_OBJECTS, "0");
        parameter.put(SessionParameter.AUTHENTICATION_PROVIDER_CLASS, StandardAuthenticationProviderWithUserInHeaders.class.getName());
        return parameter;
    }

    private SDocument toSDocument(final Document document) {
        final SDocumentBuilder docBuilder = BuilderFactory.get(SDocumentBuilderFactory.class).createNewInstance();
        docBuilder.setAuthor(Long.valueOf(document.getCreatedBy()));
        docBuilder.setCreationDate(convertDate(document.getCreationDate()));
        docBuilder.setContentMimeType(document.getContentStreamMimeType());
        docBuilder.setContentFileName(document.getContentStreamFileName());
        docBuilder.setContentSize(document.getContentStreamLength());
        docBuilder.setDocumentId(document.getId());
        return docBuilder.done();
    }

    private long convertDate(final GregorianCalendar creationDate) {
        long convertedDate;
        if (creationDate != null) {
            convertedDate = creationDate.getTimeInMillis();
        } else {
            convertedDate = -1L;
        }
        return convertedDate;
    }

    public String getRootFolderId() throws SDocumentException, SSessionNotFoundException, SessionIdNotSetException {
        final Session session = getSession();
        return getRootFolderId(session);
    }

    /**
     * Get the ID of the root path
     * 
     * @param session
     *            the CMIS session to act on
     * @return the root path ID in CMIS
     * @throws SDocumentException
     *             in case a CMIS problem occurs
     */
    public String getRootFolderId(final Session session) throws SDocumentException {
        Folder folder;
        try {
            folder = (Folder) session.getObject(session.createObjectId(ROOT_PATH));
            return folder.getId();
        } catch (final CmisObjectNotFoundException e) {
            throw new SDocumentException("Error getting CMIS Root path", e);
        }
    }

    public void clear() throws SDocumentException {
        try {
            final Session session = getSession();
            clear(session, (Folder) session.getObject(rootFolderId));
        } catch (final SSessionNotFoundException e) {
            throw new SDocumentException("Error clearing the whole repository " + repositoryId, e);
        } catch (final SessionIdNotSetException e) {
            throw new SDocumentException("Error clearing the whole repository " + repositoryId, e);
        }
    }

    /**
     * Clears the whole subFolder hierarchy
     * 
     * @param session
     *            the CMIS Session
     * @param folder
     *            the folder to delete (with its children)
     * @throws SDocumentException
     */
    private void clear(final Session session, final Folder folder) throws SDocumentException {
        Folder cmisFolder = null;
        try {
            for (final Folder subFolder : getChildrenFolder(session, folder.getId())) {
                cmisFolder = (Folder) session.getObject(session.createObjectId(subFolder.getId()));
                cmisFolder.deleteTree(true, null, true);
            }

            // Delete direct children documents:
            for (final Document doc : getChildrenDocuments(folder)) {
                deleteDocument(session, doc.getId(), true);
            }

        } catch (final CmisBaseException e) {
            throw new SDocumentException("Error deleting folder tree for Folder " + folder.getId());
        }
    }

    private List<Folder> getChildrenFolder(final Session session, final String folderId) {
        final Folder folder = (Folder) session.getObject(session.createObjectId(folderId));
        final List<Folder> subFolders = new ArrayList<Folder>();
        for (final CmisObject child : folder.getChildren()) {
            if (child instanceof Folder) {
                subFolders.add((Folder) child);
            }
        }
        return subFolders;
    }

    private List<Document> getChildrenDocuments(final Folder folder) {
        final List<Document> docs = new ArrayList<Document>();
        for (final CmisObject child : folder.getChildren()) {
            if (child instanceof Document) {
                docs.add((Document) child);
            }
        }
        return docs;
    }

}
