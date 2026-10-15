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
            "bmp");

    // ponytail: only extensions with a reliable magic number get a binary signature check below;
    // text-based formats (txt, csv, json) have none, so besides the declared Content-Type they also
    // accept application/octet-stream -- the generic fallback many OSes/browsers send when they don't
    // recognize the extension -- to avoid rejecting legitimate uploads on an already-weak check (content
    // of these formats can't be verified anyway: any byte sequence is "valid" text).
    private static final Map<String, List<String>> ALLOWED_CONTENT_TYPES_BY_EXTENSION = Map.ofEntries(
            Map.entry("pdf", List.of("application/pdf")),
            Map.entry("jpg", List.of("image/jpeg")),
            Map.entry("jpeg", List.of("image/jpeg")),
            Map.entry("png", List.of("image/png")),
            Map.entry("gif", List.of("image/gif")),
            Map.entry("bmp", List.of("image/bmp", "image/x-ms-bmp")),
            Map.entry("txt", List.of("text/plain", "application/octet-stream")),
            Map.entry("csv",
                    List.of("text/csv", "application/vnd.ms-excel", "text/plain", "application/octet-stream")),
            Map.entry("json", List.of("application/json", "text/plain", "application/octet-stream")),
            Map.entry("doc", List.of("application/msword")),
            Map.entry("docx", List.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
            Map.entry("xls", List.of("application/vnd.ms-excel")),
            Map.entry("xlsx", List.of("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
            Map.entry("ppt", List.of("application/vnd.ms-powerpoint")),
            Map.entry("pptx",
                    List.of("application/vnd.openxmlformats-officedocument.presentationml.presentation")));

    private static final Map<String, byte[]> MAGIC_BYTES_BY_EXTENSION = Map.ofEntries(
            Map.entry("pdf", new byte[] { 0x25, 0x50, 0x44, 0x46 }), // %PDF
            Map.entry("png", new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47 }),
            Map.entry("jpg", new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF }),
            Map.entry("jpeg", new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF }),
            Map.entry("gif", new byte[] { 0x47, 0x49, 0x46, 0x38 }), // GIF8
            Map.entry("bmp", new byte[] { 0x42, 0x4D }),
            // WBMP's only fixed header bytes: TypeField=0 (single-byte uint) + FixHeaderField=0
            Map.entry("wbmp", new byte[] { 0x00, 0x00 }),
            Map.entry("docx", new byte[] { 0x50, 0x4B, 0x03, 0x04 }), // OOXML files are zip archives
            Map.entry("xlsx", new byte[] { 0x50, 0x4B, 0x03, 0x04 }),
            Map.entry("pptx", new byte[] { 0x50, 0x4B, 0x03, 0x04 }),
            Map.entry("doc", new byte[] { (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0 }), // OLE compound file
            Map.entry("xls", new byte[] { (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0 }),
            Map.entry("ppt", new byte[] { (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0 }));

    // OOXML formats are all plain zip archives (see MAGIC_BYTES_BY_EXTENSION), so telling them apart
    // -- and rejecting an arbitrary zip/jar renamed to one of these extensions -- requires checking
    // for the top-level folder specific to each office document kind.
    private static final Map<String, String> OOXML_MARKER_BY_EXTENSION = Map.of(
            "docx", "word/",
            "xlsx", "xl/",
            "pptx", "ppt/");

    // TGA has no fixed magic number (its header is all variable fields), so it is excluded from
    // MAGIC_BYTES_BY_EXTENSION and gets a structural check instead: a real TGA's first two header
    // bytes (color map type, image type) are restricted to a handful of known values -- enough to
    // reject arbitrary content (HTML, scripts, other binary formats) renamed to .tga.
    private static final Set<Integer> VALID_TGA_IMAGE_TYPES = Set.of(0, 1, 2, 3, 9, 10, 11, 32, 33);

    /**
     * Cross-checks the file extension against the Content-Type declared by the client and the actual bytes of
     * the uploaded content, so that renaming a malicious file to a whitelisted extension (or spoofing the
     * Content-Type header) is rejected (see CVE-68). Extensions this validator has no rule for are considered
     * consistent: they are expected to already be rejected upstream by the extension whitelist check.
     */
    public static boolean isContentConsistentWithExtension(final String fileName, final String declaredContentType,
            final InputStream content) throws IOException {
        final String extension = FilenameUtils.getExtension(fileName).toLowerCase();

        final List<String> allowedContentTypes = ALLOWED_CONTENT_TYPES_BY_EXTENSION.get(extension);
        if (allowedContentTypes != null) {
            final String baseContentType = declaredContentType == null ? ""
                    : declaredContentType.split(";")[0].trim().toLowerCase();
            if (!allowedContentTypes.contains(baseContentType)) {
                return false;
            }
        }

        if ("tga".equals(extension)) {
            return hasPlausibleTgaHeader(content);
        }

        final byte[] expectedMagicBytes = MAGIC_BYTES_BY_EXTENSION.get(extension);
        if (expectedMagicBytes == null) {
            return true;
        }
        final byte[] header = new byte[expectedMagicBytes.length];
        final int read = content.readNBytes(header, 0, header.length);
        if (read != header.length || !Arrays.equals(header, expectedMagicBytes)) {
            return false;
        }

        final String ooxmlMarker = OOXML_MARKER_BY_EXTENSION.get(extension);
        if (ooxmlMarker != null) {
            // put the already-consumed magic bytes back in front of the stream for the zip scan below
            return hasZipEntryStartingWith(new SequenceInputStream(new ByteArrayInputStream(header), content),
                    ooxmlMarker);
        }
        // ponytail: doc/xls/ppt (OLE2) only get the container-level D0CF11E0 check above -- telling them
        // apart needs parsing the compound-file directory (e.g. via Apache POI, not a current dependency
        // of this module). Add if stricter validation of these legacy formats becomes a requirement.
        return true;
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
