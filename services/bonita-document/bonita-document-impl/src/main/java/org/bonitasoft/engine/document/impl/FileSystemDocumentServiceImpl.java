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
package org.bonitasoft.engine.document.impl;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.IOUtil;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.document.DocumentService;
import org.bonitasoft.engine.document.SDocumentContentNotFoundException;
import org.bonitasoft.engine.document.SDocumentDeletionException;
import org.bonitasoft.engine.document.SDocumentException;
import org.bonitasoft.engine.document.SDocumentNotFoundException;
import org.bonitasoft.engine.document.SDocumentStorageException;
import org.bonitasoft.engine.document.model.SDocument;

/**
 * @author Zhao Na
 * @author Celine Souchet
 */
public class FileSystemDocumentServiceImpl implements DocumentService {

    private final String storePath;

    public FileSystemDocumentServiceImpl(final String storePath) {
        this.storePath = storePath;
    }

    @Override
    public byte[] getContent(final String documentId) throws SDocumentNotFoundException, SDocumentException {
        NullCheckingUtil.checkArgsNotNull(documentId);
        final String processesFolder = storePath;
        final File file = new File(processesFolder);
        if (file.exists()) {
            final File docFile = new File(processesFolder, documentId);
            if (docFile.exists()) {
                try {
                    return IOUtil.getAllContentFrom(docFile);// read(docFile);
                } catch (final IOException e) {
                    throw new SDocumentContentNotFoundException("Cannot get the DocumentContent with documentID:" + documentId, e);
                }
            } else {
                throw new SDocumentContentNotFoundException("the Document with documentID:" + documentId + "doesn't exist.");
            }
        } else {
            throw new SDocumentContentNotFoundException("the folder that Document with documentID:" + documentId + " belongs to doesn't exist.");
        }
    }

    @Override
    public SDocument storeDocumentContent(final SDocument sDocument, final byte[] documentContent) throws SDocumentStorageException {
        NullCheckingUtil.checkArgsNotNull(sDocument);

        final String documentId = String.valueOf(Math.abs(UUID.randomUUID().getLeastSignificantBits()));
        final String processesFolder = storePath;
        final File file = new File(processesFolder);
        if (!file.exists()) {
            file.mkdirs();
        }

        final String desFName = documentId + "";
        final File docFile = new File(processesFolder, desFName);
        if (!docFile.exists()) {
            try {
                docFile.createNewFile();
            } catch (final IOException e) {
                throw new SDocumentStorageException(e);
            }
        }
        try {
            IOUtil.write(docFile, documentContent);
        } catch (final IOException e1) {
            throw new SDocumentStorageException(e1);
        }
        try {
            ClassReflector.invokeSetter(sDocument, "setId", String.class, documentId);
        } catch (final Throwable e) {
            throw new SDocumentStorageException(e);
        }
        return sDocument;
    }

    @Override
    public void deleteDocumentContent(final String documentId) throws SDocumentDeletionException, SDocumentContentNotFoundException {
        // NullCheckingUtil.checkArgsNotNull(sDocument);
        NullCheckingUtil.checkArgsNotNull(documentId);
        final String processesFolder = storePath;
        final File file = new File(processesFolder);
        // final String documentId = sDocument.getStorageId();

        if (file.exists()) {
            final File docFile = new File(processesFolder, documentId);
            if (docFile.exists()) {
                if (!docFile.delete()) {
                    throw new SDocumentDeletionException("Cannot delete the DocumentContent with documentID :" + documentId);
                }
            } else {
                throw new SDocumentContentNotFoundException("the Document with documentID:" + documentId + "doesn't exist.");
            }
        } else {
            throw new SDocumentContentNotFoundException("the folder that Document with documentID:" + documentId + " belongs to doesn't exist.");
        }
    }

}
