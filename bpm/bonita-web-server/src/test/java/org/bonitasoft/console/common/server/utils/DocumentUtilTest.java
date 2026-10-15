/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Test;

public class DocumentUtilTest {

    @Test
    public void isContentConsistentWithExtension_should_accept_matching_pdf() throws Exception {
        //given
        byte[] pdfBytes = { 0x25, 0x50, 0x44, 0x46, '-', '1', '.', '4' };

        //when-then
        assertThat(DocumentUtil.isContentConsistentWithExtension("payslip.pdf", "application/pdf",
                new ByteArrayInputStream(pdfBytes))).isTrue();
    }

    @Test
    public void isContentConsistentWithExtension_should_accept_pdf_even_when_stream_only_yields_one_byte_per_read()
            throws Exception {
        //given
        byte[] pdfBytes = { 0x25, 0x50, 0x44, 0x46, '-', '1', '.', '4' };

        //when-then
        assertThat(DocumentUtil.isContentConsistentWithExtension("payslip.pdf", "application/pdf",
                new OneByteAtATimeInputStream(pdfBytes))).isTrue();
    }

    @Test
    public void isContentConsistentWithExtension_should_accept_real_docx() throws Exception {
        //given
        byte[] docxBytes = zipWithEntries("word/document.xml");

        //when-then
        assertThat(DocumentUtil.isContentConsistentWithExtension("report.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                new ByteArrayInputStream(docxBytes))).isTrue();
    }

    @Test
    public void isContentConsistentWithExtension_should_reject_plain_zip_renamed_to_docx() throws Exception {
        //given: same zip signature and a spoofed Content-Type, but none of a real docx's internal entries
        byte[] zipBytes = zipWithEntries("META-INF/MANIFEST.MF");

        //when-then
        assertThat(DocumentUtil.isContentConsistentWithExtension("report.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                new ByteArrayInputStream(zipBytes))).isFalse();
    }

    @Test
    public void isContentConsistentWithExtension_should_reject_truncated_zip_renamed_to_docx_without_throwing()
            throws Exception {
        //given: valid zip signature, but the archive is cut off right after it -- not a parseable zip
        byte[] truncatedZipBytes = { 0x50, 0x4B, 0x03, 0x04, 0x01, 0x02 };

        //when-then
        assertThat(DocumentUtil.isContentConsistentWithExtension("report.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                new ByteArrayInputStream(truncatedZipBytes))).isFalse();
    }

    private static byte[] zipWithEntries(String... entryNames) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (String entryName : entryNames) {
                zip.putNextEntry(new ZipEntry(entryName));
                zip.write("content".getBytes());
                zip.closeEntry();
            }
        }
        return bytes.toByteArray();
    }

    private static class OneByteAtATimeInputStream extends InputStream {

        private final byte[] content;
        private int position = 0;

        private OneByteAtATimeInputStream(byte[] content) {
            this.content = content;
        }

        @Override
        public int read() {
            return position < content.length ? content[position++] & 0xFF : -1;
        }

        @Override
        public int read(byte[] b, int off, int len) {
            if (position >= content.length) {
                return -1;
            }
            b[off] = content[position++];
            return 1;
        }
    }

    @Test
    public void isContentConsistentWithExtension_should_reject_html_renamed_to_pdf() throws Exception {
        //given
        byte[] htmlBytes = "<html><body>evil</body></html>".getBytes();

        //when-then
        assertThat(DocumentUtil.isContentConsistentWithExtension("payslip.pdf", "application/pdf",
                new ByteArrayInputStream(htmlBytes))).isFalse();
    }

    @Test
    public void isContentConsistentWithExtension_should_reject_spoofed_declared_content_type() throws Exception {
        //given
        byte[] pdfBytes = { 0x25, 0x50, 0x44, 0x46 };

        //when-then
        assertThat(DocumentUtil.isContentConsistentWithExtension("payslip.pdf", "text/html",
                new ByteArrayInputStream(pdfBytes))).isFalse();
    }

    @Test
    public void isContentConsistentWithExtension_should_accept_unmanaged_extension() throws Exception {
        //when-then (extension whitelist, not this check, is responsible for rejecting unknown extensions)
        assertThat(DocumentUtil.isContentConsistentWithExtension("app.bar", "application/octet-stream",
                new ByteArrayInputStream("anything".getBytes()))).isTrue();
    }

    @Test
    public void isContentConsistentWithExtension_should_accept_matching_wbmp() throws Exception {
        //given
        byte[] wbmpBytes = { 0x00, 0x00, 0x16, 0x16 };

        //when-then
        assertThat(DocumentUtil.isContentConsistentWithExtension("icon.wbmp", "image/vnd.wap.wbmp",
                new ByteArrayInputStream(wbmpBytes))).isTrue();
    }

    @Test
    public void isContentConsistentWithExtension_should_reject_html_renamed_to_wbmp() throws Exception {
        //given
        byte[] htmlBytes = "<html><body>evil</body></html>".getBytes();

        //when-then
        assertThat(DocumentUtil.isContentConsistentWithExtension("icon.wbmp", "image/vnd.wap.wbmp",
                new ByteArrayInputStream(htmlBytes))).isFalse();
    }

    @Test
    public void isContentConsistentWithExtension_should_accept_plausible_tga_header() throws Exception {
        //given: colorMapType=0, imageType=2 (uncompressed true-color), rest of the header is irrelevant here
        byte[] tgaBytes = { 0x00, 0x02, 0x00, 0x00, 0x00, 0x00 };

        //when-then
        assertThat(DocumentUtil.isContentConsistentWithExtension("photo.tga", "image/x-tga",
                new ByteArrayInputStream(tgaBytes))).isTrue();
    }

    @Test
    public void isContentConsistentWithExtension_should_reject_html_renamed_to_tga() throws Exception {
        //given: '<' (0x3C) and 'h' (0x68) are not valid TGA colorMapType/imageType values
        byte[] htmlBytes = "<html><body>evil</body></html>".getBytes();

        //when-then
        assertThat(DocumentUtil.isContentConsistentWithExtension("photo.tga", "image/x-tga",
                new ByteArrayInputStream(htmlBytes))).isFalse();
    }

    @Test
    public void isContentConsistentWithExtension_should_accept_text_formats_declared_as_octet_stream()
            throws Exception {
        //when-then: generic browser/OS fallback when the extension isn't registered locally
        assertThat(DocumentUtil.isContentConsistentWithExtension("notes.txt", "application/octet-stream",
                new ByteArrayInputStream("hello".getBytes()))).isTrue();
        assertThat(DocumentUtil.isContentConsistentWithExtension("data.csv", "application/octet-stream",
                new ByteArrayInputStream("a,b,c".getBytes()))).isTrue();
        assertThat(DocumentUtil.isContentConsistentWithExtension("data.json", "application/octet-stream",
                new ByteArrayInputStream("{}".getBytes()))).isTrue();
    }
}
