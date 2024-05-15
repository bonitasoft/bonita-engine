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

import static org.mockito.Mockito.doReturn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CharSequenceInputStream;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MultiReadHttpServletRequestTest {

    @Mock
    HttpServletRequest request;

    @Test
    public void should_getInputStream_work_when_called_twice() throws Exception {

        ServletInputStream fakeInputStream = null;
        try {
            fakeInputStream = new FakeServletInputStream();
            doReturn(fakeInputStream).when(request).getInputStream();
            final MultiReadHttpServletRequest multiReadHttpServletRequest = new MultiReadHttpServletRequest(request);

            final InputStream inputStream = multiReadHttpServletRequest.getInputStream();
            Assert.assertEquals("body content", IOUtils.toString(inputStream, StandardCharsets.UTF_8));

            final InputStream inputStream2 = multiReadHttpServletRequest.getInputStream();
            Assert.assertEquals("body content", IOUtils.toString(inputStream2, StandardCharsets.UTF_8));
        } finally {
            if (fakeInputStream != null) {
                fakeInputStream.close();
            }
        }
    }

    @Test
    public void should_getInputStream_work_when_called_twice_for_multipart() throws Exception {

        doReturn("POST").when(request).getMethod();
        doReturn("multipart/form-data").when(request).getContentType();
        ServletInputStream fakeInputStream = null;
        try {
            fakeInputStream = new FakeServletInputStream();
            doReturn(fakeInputStream).when(request).getInputStream();
            final MultiReadHttpServletRequest multiReadHttpServletRequest = new MultiReadHttpServletRequest(request);

            final InputStream inputStream = multiReadHttpServletRequest.getInputStream();
            Assert.assertEquals("body content", IOUtils.toString(inputStream, StandardCharsets.UTF_8));

            final InputStream inputStream2 = multiReadHttpServletRequest.getInputStream();
            Assert.assertEquals("body content", IOUtils.toString(inputStream2, StandardCharsets.UTF_8));
        } finally {
            if (fakeInputStream != null) {
                fakeInputStream.close();
            }
        }
    }

    @Test
    public void should_getReader_work_when_called_twice() throws Exception {

        ServletInputStream fakeInputStream = null;
        try {
            fakeInputStream = new FakeServletInputStream();
            doReturn(fakeInputStream).when(request).getInputStream();
            final MultiReadHttpServletRequest multiReadHttpServletRequest = new MultiReadHttpServletRequest(request);

            final BufferedReader bufferedReader = multiReadHttpServletRequest.getReader();
            Assert.assertEquals("body content", IOUtils.toString(bufferedReader));

            final BufferedReader bufferedReader2 = multiReadHttpServletRequest.getReader();
            Assert.assertEquals("body content", IOUtils.toString(bufferedReader2));
        } finally {
            if (fakeInputStream != null) {
                fakeInputStream.close();
            }
        }
    }

    class FakeServletInputStream extends ServletInputStream {

        private final CharSequenceInputStream inputStream = new CharSequenceInputStream("body content",
                StandardCharsets.UTF_8);

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public int read(final byte[] b) throws IOException {
            return inputStream.read(b);
        }

        @Override
        public void close() throws IOException {
            inputStream.close();
            super.close();
        }

        @Override
        public boolean isFinished() {
            try {
                return inputStream.available() == 0;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean isReady() {
            return !isFinished();
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new RuntimeException("Not implemented");
        }

    }
}
