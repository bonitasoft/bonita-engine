/**
 * Copyright (C) 2022 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.console.common.server.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.bonitasoft.console.common.server.preferences.constants.WebBonitaConstantsUtils;

public class MultiReadHttpServletRequest extends HttpServletRequestWrapper {

    private File tempFile;
    private ByteArrayOutputStream readBytes;
    private boolean isMultipart;

    public MultiReadHttpServletRequest(final HttpServletRequest request) {
        super(request);
        isMultipart = ServletFileUpload.isMultipartContent(request);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (!isMultipart && readBytes == null
                || isMultipart && (tempFile == null || !tempFile.exists())) {
            readInputStream();
        }
        return new CachedServletInputStream();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        String enc = getCharacterEncoding();
        if (enc == null) {
            enc = "UTF-8";
        }
        return new BufferedReader(new InputStreamReader(getInputStream(), enc));
    }

    private void readInputStream() throws IOException {
        if (!isMultipart) {
            readBytes = new ByteArrayOutputStream();
            IOUtils.copy(super.getInputStream(), readBytes);
        } else {
            File tempDir = WebBonitaConstantsUtils.getPlatformInstance().getTempFolder();
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            tempFile = File.createTempFile("tmp_", ".part", tempDir);
            tempFile.deleteOnExit();
            try (FileOutputStream fileOutput = new FileOutputStream(tempFile)) {
                IOUtils.copy(super.getInputStream(), fileOutput);
            }
        }
    }

    public void cleanMultipartTempContent() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
        // clean recursively if there are several layers of wrapped requests
        if (getRequest() instanceof MultiReadHttpServletRequest) {
            ((MultiReadHttpServletRequest) getRequest()).cleanMultipartTempContent();
        }
    }

    class CachedServletInputStream extends ServletInputStream {

        private final InputStream input;
        private ReadListener readListener;

        public CachedServletInputStream() throws IOException {
            if (!isMultipart) {
                input = new ByteArrayInputStream(readBytes.toByteArray());
            } else {
                input = new FileInputStream(tempFile);
            }
            readListener = null;
        }

        @Override
        public int read() throws IOException {
            int nextByte = input.read();
            if (nextByte == -1) {
                onAllDataRead();
            }
            return nextByte;
        }

        @Override
        public int read(final byte[] b) throws IOException {
            int numberOfBytesRead = input.read(b);
            if (numberOfBytesRead == -1) {
                onAllDataRead();
            }
            return numberOfBytesRead;
        }

        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            int numberOfBytesRead = input.read(b, off, len);
            if (numberOfBytesRead == -1) {
                onAllDataRead();
            }
            return numberOfBytesRead;
        }

        @Override
        public void close() throws IOException {
            input.close();
            super.close();
        }

        @Override
        public boolean isFinished() {
            try {
                return input.available() == 0;
            } catch (IOException e) {
                // stream has been closed
                return true;
            }
        }

        @Override
        public boolean isReady() {
            return !isFinished();
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            this.readListener = readListener;
            if (!isFinished()) {
                try {
                    readListener.onDataAvailable();
                } catch (IOException e) {
                    readListener.onError(e);
                }
            } else {
                onAllDataRead();
            }
        }

        private void onAllDataRead() {
            if (readListener != null) {
                try {
                    readListener.onAllDataRead();
                } catch (IOException e) {
                    readListener.onError(e);
                }
            }
        }
    }
}
