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
package org.bonitasoft.console.common.server.login.filter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Test helper comparing the servlet mappings of a web.xml with the permissions declared in the
 * resources-permissions-mapping files of platform-resources.
 * A permission declared for a URL is only enforced when {@link RestAPIAuthorizationFilter} runs on that URL:
 * a servlet URL with a declared permission but outside the filter mapping is open to any logged-in user.
 * It also lists the exact /portal/ servlet URLs outside the filter mapping, whether they declare a permission or not.
 */
public class AuthorizationFilterMapping {

    private static final String AUTHORIZATION_FILTER_NAME = "RestAPIAuthorizationFilter";

    /** Location in the community repository, then in the repository that contains it. */
    private static final List<String> PLATFORM_RESOURCES_DIRS = List.of(
            "platform/platform-resources/src/main/resources",
            "community/platform/platform-resources/src/main/resources");

    private static final Set<String> PERMISSION_MAPPING_FILE_NAMES = Set.of(
            "resources-permissions-mapping.properties",
            "resources-permissions-mapping-internal.properties");

    private final Set<String> servletPathsWithDeclaredPermission = new LinkedHashSet<>();

    private final List<String> servletPathsNotCoveredByAuthorizationFilter = new ArrayList<>();

    private final List<String> portalServletPathsNotCoveredByAuthorizationFilter = new ArrayList<>();

    private AuthorizationFilterMapping() {
    }

    /**
     * Reads the given web.xml. The permission mapping files are looked up from the working directory, which is
     * the module directory when Gradle runs the tests.
     */
    public static AuthorizationFilterMapping read(Path webXml) throws Exception {
        Set<String> declaredPaths = readPathsWithDeclaredPermission(findPlatformResourcesDir());
        Document webApp = parse(webXml);
        Set<String> filteredUrlPatterns = new LinkedHashSet<>();
        Set<String> filteredServletNames = new LinkedHashSet<>();
        for (Element filterMapping : getElements(webApp, "filter-mapping")) {
            if (getChildTexts(filterMapping, "filter-name").contains(AUTHORIZATION_FILTER_NAME)) {
                filteredUrlPatterns.addAll(getChildTexts(filterMapping, "url-pattern"));
                filteredServletNames.addAll(getChildTexts(filterMapping, "servlet-name"));
            }
        }
        AuthorizationFilterMapping mapping = new AuthorizationFilterMapping();
        for (Element servletMapping : getElements(webApp, "servlet-mapping")) {
            String servletName = getChildTexts(servletMapping, "servlet-name").get(0);
            for (String urlPattern : getChildTexts(servletMapping, "url-pattern")) {
                boolean filtered = filteredServletNames.contains(servletName)
                        || isCovered(urlPattern, filteredUrlPatterns);
                String description = urlPattern + " (servlet " + servletName + ")";
                if (declaredPaths.contains(urlPattern)) {
                    mapping.servletPathsWithDeclaredPermission.add(urlPattern);
                    if (!filtered) {
                        mapping.servletPathsNotCoveredByAuthorizationFilter.add(description);
                    }
                }
                if (isExactPortalPath(urlPattern) && !filtered) {
                    mapping.portalServletPathsNotCoveredByAuthorizationFilter.add(description);
                }
            }
        }
        return mapping;
    }

    /**
     * @return the exact servlet url-patterns that have a permission declared for at least one HTTP method
     */
    public Set<String> getServletPathsWithDeclaredPermission() {
        return servletPathsWithDeclaredPermission;
    }

    /**
     * @return the servlet url-patterns with a declared permission that RestAPIAuthorizationFilter does not cover,
     *         as "url-pattern (servlet name)"
     */
    public List<String> getServletPathsNotCoveredByAuthorizationFilter() {
        return servletPathsNotCoveredByAuthorizationFilter;
    }

    /**
     * A servlet mapped on an exact /portal/ path is an authenticated endpoint: it must run RestAPIAuthorizationFilter,
     * which denies access to a URL without a declared permission, so that a new servlet cannot be left open by
     * omission. Wildcard /portal/ mappings (page and form servlets) check access themselves.
     *
     * @return the exact /portal/ servlet url-patterns that RestAPIAuthorizationFilter does not cover,
     *         as "url-pattern (servlet name)"
     */
    public List<String> getPortalServletPathsNotCoveredByAuthorizationFilter() {
        return portalServletPathsNotCoveredByAuthorizationFilter;
    }

    private static boolean isExactPortalPath(String urlPattern) {
        return urlPattern.startsWith("/portal/") && !urlPattern.contains("*");
    }

    private static Path findPlatformResourcesDir() {
        Path workingDir = Paths.get("").toAbsolutePath();
        for (Path dir = workingDir; dir != null; dir = dir.getParent()) {
            for (String candidate : PLATFORM_RESOURCES_DIRS) {
                Path platformResourcesDir = dir.resolve(candidate);
                if (Files.isDirectory(platformResourcesDir)) {
                    return platformResourcesDir;
                }
            }
        }
        throw new IllegalStateException("platform-resources not found from " + workingDir);
    }

    private static Set<String> readPathsWithDeclaredPermission(Path platformResourcesDir) throws IOException {
        List<Path> mappingFiles;
        try (Stream<Path> files = Files.walk(platformResourcesDir)) {
            mappingFiles = files.filter(file -> PERMISSION_MAPPING_FILE_NAMES.contains(file.getFileName().toString()))
                    .collect(Collectors.toList());
        }
        if (mappingFiles.isEmpty()) {
            throw new IllegalStateException("No resources-permissions-mapping file in " + platformResourcesDir);
        }
        Set<String> paths = new LinkedHashSet<>();
        for (Path mappingFile : mappingFiles) {
            Properties permissions = new Properties();
            try (InputStream inputStream = Files.newInputStream(mappingFile)) {
                permissions.load(inputStream);
            }
            for (String key : permissions.stringPropertyNames()) {
                // keys are like GET|portal/formsDocumentImage
                int separatorIndex = key.indexOf('|');
                if (separatorIndex > 0) {
                    paths.add("/" + key.substring(separatorIndex + 1));
                }
            }
        }
        return paths;
    }

    /** Servlet specification mapping rules: exact match, path prefix (/path/*) or extension (*.ext). */
    private static boolean isCovered(String path, Set<String> urlPatterns) {
        for (String urlPattern : urlPatterns) {
            if (urlPattern.equals(path) || urlPattern.equals("/*")) {
                return true;
            }
            if (urlPattern.endsWith("/*")) {
                String prefix = urlPattern.substring(0, urlPattern.length() - 2);
                if (path.equals(prefix) || path.startsWith(prefix + "/")) {
                    return true;
                }
            }
            if (urlPattern.startsWith("*.") && path.endsWith(urlPattern.substring(1))) {
                return true;
            }
        }
        return false;
    }

    private static Document parse(Path webXml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        try (InputStream inputStream = Files.newInputStream(webXml)) {
            return factory.newDocumentBuilder().parse(inputStream);
        }
    }

    private static List<Element> getElements(Document document, String tagName) {
        NodeList nodes = document.getElementsByTagName(tagName);
        List<Element> elements = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            elements.add((Element) nodes.item(i));
        }
        return elements;
    }

    private static List<String> getChildTexts(Element parent, String tagName) {
        List<String> texts = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && tagName.equals(child.getNodeName())) {
                texts.add(child.getTextContent().trim());
            }
        }
        return texts;
    }
}
