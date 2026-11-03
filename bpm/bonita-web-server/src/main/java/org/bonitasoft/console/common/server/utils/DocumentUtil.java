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
package org.bonitasoft.console.common.server.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FilenameUtils;

/**
 * @author Yongtao Guo
 */
public class DocumentUtil {

    /**
     * Whitelist of file extensions accepted by generic file upload widgets/forms when no other whitelist
     * applies (see CVE-68). Single source of truth shared by {@code ConsoleProperties} (tenant-configurable
     * default) and {@code FileUploadServlet} (platform servlet with no tenant {@code ConsoleProperties}).
     */
    public static final List<String> DEFAULT_ALLOWED_EXTENSIONS = List.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "json", "jpg", "jpeg", "png", "gif",
            "bmp", "xml", "msg", "eml", "tif", "tiff", "odt", "ods", "odp", "zip");

    /**
     * Content-Type sent by clients that do not know a file's type (Java HTTP clients, scripts, machines without
     * the matching software). Like a missing Content-Type, it is accepted for every extension of
     * {@link #ALLOWED_CONTENT_TYPES_BY_EXTENSION}: binary formats are checked against their signature, and text
     * formats cannot be verified anyway.
     */
    private static final String UNKNOWN_CONTENT_TYPE = "application/octet-stream";

    // Content-Types expected per extension. A text format (no signature in MAGIC_BYTES_BY_EXTENSION) must be
    // declared with one of them. A format with a signature proves its type by its content, so it may be declared
    // with any type a browser cannot run as script. The first one is the usual type, stored when the declared type
    // is not one of them (see resolveContentType).
    private static final Map<String, List<String>> ALLOWED_CONTENT_TYPES_BY_EXTENSION = Map.ofEntries(
            Map.entry("pdf", List.of("application/pdf")),
            Map.entry("jpg", List.of("image/jpeg", "image/jpg", "image/pjpeg")),
            Map.entry("jpeg", List.of("image/jpeg", "image/jpg", "image/pjpeg")),
            Map.entry("png", List.of("image/png", "image/x-png")),
            Map.entry("gif", List.of("image/gif")),
            Map.entry("bmp", List.of("image/bmp", "image/x-ms-bmp")),
            Map.entry("txt", List.of("text/plain")),
            Map.entry("csv", List.of("text/csv", "application/vnd.ms-excel", "text/plain")),
            Map.entry("json", List.of("application/json", "text/plain")),
            Map.entry("xml", List.of("application/xml", "text/xml", "text/plain")),
            Map.entry("doc", List.of("application/msword")),
            Map.entry("docx", List.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
            Map.entry("xls", List.of("application/vnd.ms-excel")),
            Map.entry("xlsx", List.of("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
            Map.entry("ppt", List.of("application/vnd.ms-powerpoint")),
            Map.entry("pptx",
                    List.of("application/vnd.openxmlformats-officedocument.presentationml.presentation")),
            Map.entry("msg", List.of("application/vnd.ms-outlook")),
            Map.entry("eml", List.of("message/rfc822", "text/plain")),
            Map.entry("tif", List.of("image/tiff")),
            Map.entry("tiff", List.of("image/tiff")),
            Map.entry("odt", List.of("application/vnd.oasis.opendocument.text")),
            Map.entry("ods", List.of("application/vnd.oasis.opendocument.spreadsheet")),
            Map.entry("odp", List.of("application/vnd.oasis.opendocument.presentation")),
            Map.entry("zip", List.of("application/zip", "application/x-zip-compressed", "application/x-zip")));

    private static final byte[] ZIP_SIGNATURE = { 0x50, 0x4B, 0x03, 0x04 };

    private static final byte[] EMPTY_ZIP_SIGNATURE = { 0x50, 0x4B, 0x05, 0x06 };

    private static final byte[] OLE_SIGNATURE = { (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0 }; // compound file

    private static final byte[] TIFF_LITTLE_ENDIAN_SIGNATURE = { 0x49, 0x49, 0x2A, 0x00 }; // II*

    private static final byte[] TIFF_BIG_ENDIAN_SIGNATURE = { 0x4D, 0x4D, 0x00, 0x2A }; // MM*

    // Accepted first bytes per extension: the content must start with one of them
    private static final Map<String, List<byte[]>> MAGIC_BYTES_BY_EXTENSION = Map.ofEntries(
            Map.entry("pdf", List.of(new byte[] { 0x25, 0x50, 0x44, 0x46 })), // %PDF
            Map.entry("png", List.of(new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47 })),
            Map.entry("jpg", List.of(new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF })),
            Map.entry("jpeg", List.of(new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF })),
            Map.entry("gif", List.of(new byte[] { 0x47, 0x49, 0x46, 0x38 })), // GIF8
            Map.entry("bmp", List.of(new byte[] { 0x42, 0x4D })),
            // WBMP's only fixed header bytes: TypeField=0 (single-byte uint) + FixHeaderField=0
            Map.entry("wbmp", List.of(new byte[] { 0x00, 0x00 })),
            Map.entry("tif", List.of(TIFF_LITTLE_ENDIAN_SIGNATURE, TIFF_BIG_ENDIAN_SIGNATURE)),
            Map.entry("tiff", List.of(TIFF_LITTLE_ENDIAN_SIGNATURE, TIFF_BIG_ENDIAN_SIGNATURE)),
            // OOXML and OpenDocument files are zip archives, password-protected OOXML files are OLE2 compound files
            Map.entry("docx", List.of(ZIP_SIGNATURE, OLE_SIGNATURE)),
            Map.entry("xlsx", List.of(ZIP_SIGNATURE, OLE_SIGNATURE)),
            Map.entry("pptx", List.of(ZIP_SIGNATURE, OLE_SIGNATURE)),
            Map.entry("odt", List.of(ZIP_SIGNATURE)),
            Map.entry("ods", List.of(ZIP_SIGNATURE)),
            Map.entry("odp", List.of(ZIP_SIGNATURE)),
            Map.entry("zip", List.of(ZIP_SIGNATURE, EMPTY_ZIP_SIGNATURE)),
            Map.entry("doc", List.of(OLE_SIGNATURE)),
            Map.entry("xls", List.of(OLE_SIGNATURE)),
            Map.entry("ppt", List.of(OLE_SIGNATURE)),
            Map.entry("msg", List.of(OLE_SIGNATURE)));

    // OOXML and OpenDocument formats are plain zip archives (see MAGIC_BYTES_BY_EXTENSION), so telling them apart
    // -- and rejecting an arbitrary zip/jar renamed to one of these extensions -- requires checking for an entry
    // specific to each document kind: the top-level folder of OOXML files...
    private static final Map<String, String> OOXML_MARKER_BY_EXTENSION = Map.of(
            "docx", "word/",
            "xlsx", "xl/",
            "pptx", "ppt/");

    // ... and the "mimetype" entry that every OpenDocument file starts with
    private static final Set<String> OPEN_DOCUMENT_EXTENSIONS = Set.of("odt", "ods", "odp");

    // TGA has no fixed magic number (its header is all variable fields), so it is excluded from
    // MAGIC_BYTES_BY_EXTENSION and gets a structural check instead: a real TGA's first two header
    // bytes (color map type, image type) are restricted to a handful of known values -- enough to
    // reject arbitrary content (HTML, scripts, other binary formats) renamed to .tga.
    private static final Set<Integer> VALID_TGA_IMAGE_TYPES = Set.of(0, 1, 2, 3, 9, 10, 11, 32, 33);

    // Types a browser can run script from when it displays them, besides any "+xml" type (XHTML, SVG, ...)
    private static final Set<String> SCRIPT_CAPABLE_CONTENT_TYPES = Set.of("text/html", "text/xml",
            "application/xml", "text/xsl", "text/javascript", "text/ecmascript", "application/javascript",
            "application/x-javascript", "application/ecmascript");

    /**
     * Cross-checks the file extension against the Content-Type declared by the client and the actual bytes of
     * the uploaded content, so that renaming a malicious file to a whitelisted extension (or spoofing the
     * Content-Type header) is rejected (see CVE-68). A format with a signature may be declared with any type a
     * browser cannot run as script, since its content proves its type. Extensions this validator has no rule for
     * are considered consistent: only the extension allow-list applies to them.
     */
    public static boolean isContentConsistentWithExtension(final String fileName, final String declaredContentType,
            final InputStream content) throws IOException {
        final String extension = FilenameUtils.getExtension(fileName).toLowerCase(Locale.ROOT);

        final List<String> allowedContentTypes = ALLOWED_CONTENT_TYPES_BY_EXTENSION.get(extension);
        final String baseContentType = getBaseContentType(declaredContentType);
        if (allowedContentTypes != null
                && !isDeclaredContentTypeAccepted(extension, allowedContentTypes, baseContentType)) {
            return false;
        }

        if ("tga".equals(extension)) {
            return hasPlausibleTgaHeader(content);
        }

        final List<byte[]> expectedMagicBytes = MAGIC_BYTES_BY_EXTENSION.get(extension);
        if (expectedMagicBytes == null) {
            return true;
        }
        final byte[] header = new byte[expectedMagicBytes.stream().mapToInt(magicBytes -> magicBytes.length).max()
                .orElse(0)];
        final int read = content.readNBytes(header, 0, header.length);
        if (expectedMagicBytes.stream().noneMatch(magicBytes -> startsWith(header, read, magicBytes))) {
            return false;
        }

        if (!startsWith(header, read, ZIP_SIGNATURE)) {
            // not a zip: the signature check above is the whole check. For OLE2 files (doc/xls/ppt/msg and
            // password-protected docx/xlsx/pptx), telling them apart would need parsing the compound-file
            // directory (e.g. with Apache POI, not a dependency of this module).
            return true;
        }
        // put the already-consumed magic bytes back in front of the stream for the zip checks below
        final InputStream zipContent = new SequenceInputStream(new ByteArrayInputStream(header, 0, read), content);
        if (OPEN_DOCUMENT_EXTENSIONS.contains(extension)) {
            return isFirstZipEntry(zipContent, "mimetype");
        }
        final String ooxmlMarker = OOXML_MARKER_BY_EXTENSION.get(extension);
        return ooxmlMarker == null || hasZipEntryStartingWith(zipContent, ooxmlMarker);
    }

    private static boolean startsWith(final byte[] header, final int read, final byte[] signature) {
        return read >= signature.length && Arrays.equals(header, 0, signature.length, signature, 0, signature.length);
    }

    /**
     * Content type to store, and return in the upload response, for an uploaded file that
     * {@link #isContentConsistentWithExtension} accepted (see CVE-68):
     * <ul>
     * <li>extension with a rule: the declared type when it is one of the allowed ones, else (none,
     * {@code application/octet-stream}...) the extension's usual type</li>
     * <li>other extension: {@code application/octet-stream} when the declared type could run script in a browser
     * (HTML, XML, SVG, JavaScript), else the declared type</li>
     * </ul>
     */
    public static String resolveContentType(final String fileName, final String declaredContentType) {
        final String extension = FilenameUtils.getExtension(fileName).toLowerCase(Locale.ROOT);
        final String baseContentType = getBaseContentType(declaredContentType);
        final List<String> allowedContentTypes = ALLOWED_CONTENT_TYPES_BY_EXTENSION.get(extension);
        if (allowedContentTypes != null) {
            return allowedContentTypes.contains(baseContentType) ? declaredContentType : allowedContentTypes.get(0);
        }
        if (isScriptCapable(baseContentType)) {
            return UNKNOWN_CONTENT_TYPE;
        }
        return declaredContentType;
    }

    private static boolean isDeclaredContentTypeAccepted(final String extension,
            final List<String> allowedContentTypes, final String baseContentType) {
        if (isUnknownContentType(baseContentType) || allowedContentTypes.contains(baseContentType)) {
            return true;
        }
        // e.g. image/x-citrix-jpeg: the content is checked against the signature instead, and resolveContentType
        // stores the usual type of the extension
        return MAGIC_BYTES_BY_EXTENSION.containsKey(extension) && !isScriptCapable(baseContentType);
    }

    private static boolean isScriptCapable(final String baseContentType) {
        return SCRIPT_CAPABLE_CONTENT_TYPES.contains(baseContentType) || baseContentType.endsWith("+xml");
    }

    private static String getBaseContentType(final String contentType) {
        return contentType == null ? "" : contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isUnknownContentType(final String baseContentType) {
        return baseContentType.isEmpty() || UNKNOWN_CONTENT_TYPE.equals(baseContentType);
    }

    private static boolean hasPlausibleTgaHeader(final InputStream content) throws IOException {
        final byte[] header = new byte[2];
        if (content.readNBytes(header, 0, header.length) != header.length) {
            return false;
        }
        final int colorMapType = header[0] & 0xFF;
        final int imageType = header[1] & 0xFF;
        return (colorMapType == 0 || colorMapType == 1) && VALID_TGA_IMAGE_TYPES.contains(imageType);
    }

    private static boolean hasZipEntryStartingWith(final InputStream zipContent, final String prefix)
            throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(zipContent)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.getName().startsWith(prefix)) {
                    return true;
                }
            }
        } catch (IOException e) {
            // malformed/truncated zip (e.g. a corrupt file merely renamed to .docx/.xlsx/.pptx):
            // not a valid OOXML document, same verdict as a well-formed zip missing the marker entry
            return false;
        }
        return false;
    }

    private static boolean isFirstZipEntry(final InputStream zipContent, final String name) {
        // only the first local header is read, so a large first entry is never inflated
        try (ZipInputStream zipInputStream = new ZipInputStream(zipContent)) {
            final ZipEntry firstEntry = zipInputStream.getNextEntry();
            return firstEntry != null && name.equals(firstEntry.getName());
        } catch (IOException e) {
            return false;
        }
    }

    public static byte[] getArrayByte(final InputStream input, final int estimatedSize) throws IOException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream(estimatedSize);
        try (output) {
            final byte[] buf = new byte[8192];
            int len;

            while ((len = input.read(buf)) >= 0) {
                output.write(buf, 0, len);
            }
        }
        return output.toByteArray();
    }

    public static byte[] getArrayByteFromFile(final File f) throws IOException {
        final long length = f.length();
        if (length > Integer.MAX_VALUE) { // more than 2 GB
            throw new IOException("File too big");
        }

        try (FileInputStream input = new FileInputStream(f)) {
            return getArrayByte(input, (int) length);
        }
    }

    public static String sanitizeFilename(String filename) {
        if (filename != null) {
            return filename.replaceAll("[<>\"]+", "_").trim();
        }
        return null;
    }
}
