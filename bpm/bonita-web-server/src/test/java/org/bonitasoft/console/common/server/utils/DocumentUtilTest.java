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
import java.util.Locale;
import java.util.zip.CRC32;
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
        assertThat(DocumentUtil.isContentConsistentWithExtension("data.xml", "application/octet-stream",
                new ByteArrayInputStream("<a/>".getBytes()))).isTrue();
    }

    @Test
    public void isContentConsistentWithExtension_should_accept_xml_declared_with_xml_content_types()
            throws Exception {
        //when-then
        assertThat(DocumentUtil.isContentConsistentWithExtension("data.xml", "application/xml",
                new ByteArrayInputStream("<a/>".getBytes()))).isTrue();
        assertThat(DocumentUtil.isContentConsistentWithExtension("data.xml", "text/xml; charset=UTF-8",
                new ByteArrayInputStream("<a/>".getBytes()))).isTrue();
    }

    @Test
    public void isContentConsistentWithExtension_should_reject_xml_declared_as_html() throws Exception {
        //when-then
        assertThat(DocumentUtil.isContentConsistentWithExtension("data.xml", "text/html",
                new ByteArrayInputStream("<html><body>evil</body></html>".getBytes()))).isFalse();
    }

    @Test
    public void isContentConsistentWithExtension_should_accept_real_binary_files_without_a_specific_content_type()
            throws Exception {
        //given: what Java HTTP clients, scripts and machines without Office send
        final byte[] pdfBytes = { 0x25, 0x50, 0x44, 0x46, '-', '1', '.', '4' };
        final byte[] pngBytes = { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A };
        final byte[] xlsBytes = { (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, 0x00 };
        final byte[] docxBytes = zipWithEntries("word/document.xml");

        //when-then
        assertThat(DocumentUtil.isContentConsistentWithExtension("payslip.pdf", "application/octet-stream",
                new ByteArrayInputStream(pdfBytes))).isTrue();
        assertThat(DocumentUtil.isContentConsistentWithExtension("payslip.pdf", null,
                new ByteArrayInputStream(pdfBytes))).isTrue();
        assertThat(DocumentUtil.isContentConsistentWithExtension("photo.png", "",
                new ByteArrayInputStream(pngBytes))).isTrue();
        assertThat(DocumentUtil.isContentConsistentWithExtension("budget.xls", "application/octet-stream",
                new ByteArrayInputStream(xlsBytes))).isTrue();
        assertThat(DocumentUtil.isContentConsistentWithExtension("report.docx", "application/octet-stream",
                new ByteArrayInputStream(docxBytes))).isTrue();
    }

    @Test
    public void isContentConsistentWithExtension_should_accept_common_image_content_type_aliases() throws Exception {
        //given
        final byte[] jpegBytes = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0 };
        final byte[] pngBytes = { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A };

        //when-then
        assertThat(DocumentUtil.isContentConsistentWithExtension("photo.jpg", "image/jpg",
                new ByteArrayInputStream(jpegBytes))).isTrue();
        assertThat(DocumentUtil.isContentConsistentWithExtension("photo.jpeg", "image/pjpeg",
                new ByteArrayInputStream(jpegBytes))).isTrue();
        assertThat(DocumentUtil.isContentConsistentWithExtension("photo.png", "image/x-png",
                new ByteArrayInputStream(pngBytes))).isTrue();
    }

    @Test
    public void isContentConsistentWithExtension_should_reject_html_named_pdf_declared_as_octet_stream()
            throws Exception {
        //when-then: accepting octet-stream must not skip the signature check
        assertThat(DocumentUtil.isContentConsistentWithExtension("payslip.pdf", "application/octet-stream",
                new ByteArrayInputStream("<html><body>evil</body></html>".getBytes()))).isFalse();
    }

    @Test
    public void isContentConsistentWithExtension_should_reject_plain_zip_named_docx_declared_as_octet_stream()
            throws Exception {
        //when-then
        assertThat(DocumentUtil.isContentConsistentWithExtension("report.docx", "application/octet-stream",
                new ByteArrayInputStream(zipWithEntries("META-INF/MANIFEST.MF")))).isFalse();
    }

    @Test
    public void resolveContentType_should_return_usual_type_when_client_sent_no_specific_type() {
        //when-then
        assertThat(DocumentUtil.resolveContentType("payslip.pdf", "application/octet-stream"))
                .isEqualTo("application/pdf");
        assertThat(DocumentUtil.resolveContentType("payslip.PDF", null)).isEqualTo("application/pdf");
        assertThat(DocumentUtil.resolveContentType("report.docx", ""))
                .isEqualTo("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        assertThat(DocumentUtil.resolveContentType("notes.txt", "application/octet-stream; charset=binary"))
                .isEqualTo("text/plain");
    }

    @Test
    public void resolveContentType_should_keep_specific_declared_type() {
        //when-then
        assertThat(DocumentUtil.resolveContentType("data.csv", "application/vnd.ms-excel"))
                .isEqualTo("application/vnd.ms-excel");
        assertThat(DocumentUtil.resolveContentType("data.xml", "text/xml; charset=UTF-8"))
                .isEqualTo("text/xml; charset=UTF-8");
        assertThat(DocumentUtil.resolveContentType("letter.rtf", "application/rtf")).isEqualTo("application/rtf");
    }

    @Test
    public void resolveContentType_should_return_usual_type_when_declared_type_is_not_allowed_for_the_extension() {
        //when-then: safe even if called without isContentConsistentWithExtension
        assertThat(DocumentUtil.resolveContentType("letter.odt", "text/html"))
                .isEqualTo("application/vnd.oasis.opendocument.text");
        assertThat(DocumentUtil.resolveContentType("payslip.pdf", "image/svg+xml")).isEqualTo("application/pdf");
    }

    @Test
    public void isContentConsistentWithExtension_should_accept_real_emails_scans_libreoffice_files_and_archives()
            throws Exception {
        //given
        final byte[] oleBytes = { (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, 0x00 };
        final byte[] littleEndianTiffBytes = { 0x49, 0x49, 0x2A, 0x00, 0x08 };
        final byte[] bigEndianTiffBytes = { 0x4D, 0x4D, 0x00, 0x2A, 0x00 };

        //when-then
        assertThat(DocumentUtil.isContentConsistentWithExtension("mail.msg", "application/vnd.ms-outlook",
                new ByteArrayInputStream(oleBytes))).isTrue();
        assertThat(DocumentUtil.isContentConsistentWithExtension("mail.eml", "message/rfc822",
                new ByteArrayInputStream("Subject: hello".getBytes()))).isTrue();
        assertThat(DocumentUtil.isContentConsistentWithExtension("scan.tif", "image/tiff",
                new ByteArrayInputStream(littleEndianTiffBytes))).isTrue();
        assertThat(DocumentUtil.isContentConsistentWithExtension("scan.tiff", "application/octet-stream",
                new ByteArrayInputStream(bigEndianTiffBytes))).isTrue();
        assertThat(
                DocumentUtil.isContentConsistentWithExtension("letter.odt", "application/vnd.oasis.opendocument.text",
                        new ByteArrayInputStream(zipWithEntries("mimetype", "content.xml"))))
                .isTrue();
        assertThat(DocumentUtil.isContentConsistentWithExtension("budget.ods",
                "application/vnd.oasis.opendocument.spreadsheet",
                new ByteArrayInputStream(zipWithEntries("mimetype", "content.xml")))).isTrue();
        assertThat(DocumentUtil.isContentConsistentWithExtension("slides.odp", null,
                new ByteArrayInputStream(zipWithEntries("mimetype", "content.xml")))).isTrue();
        assertThat(DocumentUtil.isContentConsistentWithExtension("archive.zip", "application/x-zip-compressed",
                new ByteArrayInputStream(zipWithEntries("data/report.txt")))).isTrue();
        assertThat(DocumentUtil.isContentConsistentWithExtension("empty.zip", "application/zip",
                new ByteArrayInputStream(zipWithEntries()))).isTrue();
    }

    @Test
    public void isContentConsistentWithExtension_should_reject_html_renamed_to_email_scan_libreoffice_or_archive()
            throws Exception {
        //given
        final byte[] htmlBytes = "<html><body>evil</body></html>".getBytes();

        //when-then
        for (final String fileName : new String[] { "mail.msg", "scan.tif", "scan.tiff", "letter.odt",
                "archive.zip" }) {
            assertThat(DocumentUtil.isContentConsistentWithExtension(fileName, "application/octet-stream",
                    new ByteArrayInputStream(htmlBytes))).as(fileName).isFalse();
        }
    }

    @Test
    public void isContentConsistentWithExtension_should_reject_plain_zip_renamed_to_odt() throws Exception {
        //when-then: every OpenDocument file starts with a "mimetype" entry
        assertThat(DocumentUtil.isContentConsistentWithExtension("letter.odt", "application/octet-stream",
                new ByteArrayInputStream(zipWithEntries("data/report.txt")))).isFalse();
    }

    @Test
    public void isContentConsistentWithExtension_should_accept_file_with_a_signature_declared_with_an_unlisted_type()
            throws Exception {
        //given: e.g. browsers in Citrix sessions declare JPEG and PNG files with their own types
        final byte[] jpegBytes = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0 };
        final byte[] pngBytes = { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A };
        final byte[] pdfBytes = { 0x25, 0x50, 0x44, 0x46, '-', '1', '.', '4' };

        //when-then
        assertThat(DocumentUtil.isContentConsistentWithExtension("photo.jpg", "image/x-citrix-jpeg",
                new ByteArrayInputStream(jpegBytes))).isTrue();
        assertThat(DocumentUtil.isContentConsistentWithExtension("photo.png", "image/x-citrix-png",
                new ByteArrayInputStream(pngBytes))).isTrue();
        assertThat(DocumentUtil.isContentConsistentWithExtension("payslip.pdf", "application/x-pdf",
                new ByteArrayInputStream(pdfBytes))).isTrue();
        assertThat(DocumentUtil.isContentConsistentWithExtension("archive.zip", "application/zip-compressed",
                new ByteArrayInputStream(zipWithEntries("data/report.txt")))).isTrue();
    }

    @Test
    public void isContentConsistentWithExtension_should_reject_fake_content_declared_with_an_unlisted_type()
            throws Exception {
        //when-then: accepting any declared type must not skip the signature check
        assertThat(DocumentUtil.isContentConsistentWithExtension("photo.jpg", "image/x-citrix-jpeg",
                new ByteArrayInputStream("<html><body>evil</body></html>".getBytes()))).isFalse();
    }

    @Test
    public void isContentConsistentWithExtension_should_accept_password_protected_office_files() throws Exception {
        //given: a password-protected docx/xlsx/pptx is an OLE2 compound file, not a zip archive
        final byte[] oleBytes = { (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, 0x00 };

        //when-then
        assertThat(DocumentUtil.isContentConsistentWithExtension("payslip.docx", "application/octet-stream",
                new ByteArrayInputStream(oleBytes))).isTrue();
        assertThat(DocumentUtil.isContentConsistentWithExtension("budget.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new ByteArrayInputStream(oleBytes))).isTrue();
        assertThat(DocumentUtil.isContentConsistentWithExtension("slides.pptx", null,
                new ByteArrayInputStream(oleBytes))).isTrue();
    }

    @Test
    public void isContentConsistentWithExtension_should_reject_open_document_whose_first_entry_is_not_mimetype()
            throws Exception {
        //when-then
        assertThat(DocumentUtil.isContentConsistentWithExtension("letter.odt", "application/octet-stream",
                new ByteArrayInputStream(zipWithEntries("docs/readme.txt", "mimetype.txt")))).isFalse();
        assertThat(DocumentUtil.isContentConsistentWithExtension("letter.odt", "application/octet-stream",
                new ByteArrayInputStream(zipWithEntries("content.xml", "mimetype")))).isFalse();
    }

    @Test
    public void isContentConsistentWithExtension_should_accept_open_document_with_a_stored_mimetype_entry()
            throws Exception {
        //given: as LibreOffice writes it, "mimetype" is the first entry and is stored uncompressed
        final byte[] mimetype = "application/vnd.oasis.opendocument.text".getBytes();
        final CRC32 mimetypeCrc = new CRC32();
        mimetypeCrc.update(mimetype);
        final ZipEntry mimetypeEntry = new ZipEntry("mimetype");
        mimetypeEntry.setMethod(ZipEntry.STORED);
        mimetypeEntry.setSize(mimetype.length);
        mimetypeEntry.setCrc(mimetypeCrc.getValue());
        final ByteArrayOutputStream odtBytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(odtBytes)) {
            zip.putNextEntry(mimetypeEntry);
            zip.write(mimetype);
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("content.xml"));
            zip.write("<office:document-content/>".getBytes());
            zip.closeEntry();
        }

        //when-then
        assertThat(DocumentUtil.isContentConsistentWithExtension("letter.odt",
                "application/vnd.oasis.opendocument.text",
                new ByteArrayInputStream(odtBytes.toByteArray()))).isTrue();
    }

    @Test
    public void isContentConsistentWithExtension_should_check_content_whatever_the_default_locale() throws Exception {
        //given: in Turkish, "GIF".toLowerCase() is "gıf" (dotless i)
        final Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(new Locale("tr", "TR"));
        try {
            //when-then
            assertThat(DocumentUtil.isContentConsistentWithExtension("PHOTO.GIF", "image/gif",
                    new ByteArrayInputStream("<html><body>evil</body></html>".getBytes()))).isFalse();
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }

    @Test
    public void isContentConsistentWithExtension_should_reject_file_with_a_signature_declared_with_a_script_type()
            throws Exception {
        //given
        final byte[] jpegBytes = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0 };
        final byte[] pngBytes = { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A };

        //when-then
        assertThat(DocumentUtil.isContentConsistentWithExtension("photo.png", "image/svg+xml",
                new ByteArrayInputStream(pngBytes))).isFalse();
        assertThat(DocumentUtil.isContentConsistentWithExtension("photo.jpg", "text/javascript",
                new ByteArrayInputStream(jpegBytes))).isFalse();
        assertThat(DocumentUtil.isContentConsistentWithExtension("archive.zip", "application/xhtml+xml",
                new ByteArrayInputStream(zipWithEntries("data/report.txt")))).isFalse();
    }

    @Test
    public void isContentConsistentWithExtension_should_reject_text_file_declared_with_an_unlisted_type()
            throws Exception {
        //when-then: text formats have no signature, so their declared type is the only check
        assertThat(DocumentUtil.isContentConsistentWithExtension("notes.txt", "image/png",
                new ByteArrayInputStream("hello".getBytes()))).isFalse();
        assertThat(DocumentUtil.isContentConsistentWithExtension("data.json", "application/x-msdownload",
                new ByteArrayInputStream("{}".getBytes()))).isFalse();
    }

    @Test
    public void resolveContentType_should_return_usual_type_for_file_with_a_signature_declared_with_an_unlisted_type() {
        //when-then
        assertThat(DocumentUtil.resolveContentType("photo.jpg", "image/x-citrix-jpeg")).isEqualTo("image/jpeg");
        assertThat(DocumentUtil.resolveContentType("photo.png", "image/x-citrix-png")).isEqualTo("image/png");
    }

    @Test
    public void resolveContentType_should_return_usual_type_of_new_default_formats() {
        //when-then
        assertThat(DocumentUtil.resolveContentType("archive.zip", "application/octet-stream"))
                .isEqualTo("application/zip");
        assertThat(DocumentUtil.resolveContentType("scan.tif", null)).isEqualTo("image/tiff");
        assertThat(DocumentUtil.resolveContentType("mail.msg", "")).isEqualTo("application/vnd.ms-outlook");
        assertThat(DocumentUtil.resolveContentType("letter.odt", "application/octet-stream"))
                .isEqualTo("application/vnd.oasis.opendocument.text");
    }

    @Test
    public void resolveContentType_should_return_octet_stream_for_script_capable_type_of_extension_without_rule() {
        //when-then: a browser could run script from these types if a page displayed the stored file
        assertThat(DocumentUtil.resolveContentType("letter.rtf", "text/html")).isEqualTo("application/octet-stream");
        assertThat(DocumentUtil.resolveContentType("page.html", "TEXT/HTML; charset=UTF-8"))
                .isEqualTo("application/octet-stream");
        assertThat(DocumentUtil.resolveContentType("logo.svg", "image/svg+xml"))
                .isEqualTo("application/octet-stream");
        assertThat(DocumentUtil.resolveContentType("feed.atom", "application/atom+xml"))
                .isEqualTo("application/octet-stream");
        assertThat(DocumentUtil.resolveContentType("app.js", "text/javascript"))
                .isEqualTo("application/octet-stream");
    }
}
