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
package org.bonitasoft.web.rest.server;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Validates that servlet URL patterns in web.xml follow Tomcat constraints.
 * Tomcat servlet mappings do NOT support wildcards in the middle of paths:
 * - ❌ /API/bpm/process/*&#47;design (wildcard in middle)
 * - ✅ /API/bpm/process/* (wildcard at end)
 * - ✅ *.jsp (extension mapping)
 * When wildcards in the middle are needed, use URL rewriting with /APISpringInternal
 * (see doc/LEGACY_API_TO_SPRING_MVC_GUIDE.md - "Wildcard in the MIDDLE" box in Step 5)
 */
class ServletMappingValidationTest {

    private static final String WEB_XML_PATH = "src/main/webapp/WEB-INF/web.xml";

    @Test
    void should_not_have_wildcards_in_middle_of_SpringRest_servlet_patterns() throws Exception {
        // Given
        File webXmlFile = new File(WEB_XML_PATH);
        assertThat(webXmlFile)
                .withFailMessage("web.xml not found at: " + WEB_XML_PATH)
                .exists();

        // When
        List<String> springRestPatterns = parseUrlPatternsForServlet(webXmlFile, "SpringRest");

        // Then
        List<String> invalidPatterns = new ArrayList<>();
        for (String pattern : springRestPatterns) {
            if (hasWildcardInMiddle(pattern)) {
                invalidPatterns.add(pattern);
            }
        }

        assertThat(invalidPatterns)
                .withFailMessage(
                        "Found servlet URL patterns with wildcards in the middle (not supported by Tomcat):\n" +
                                String.join("\n", invalidPatterns) +
                                "\n\nTomcat only supports:\n" +
                                "  - Wildcards at the end: /API/path/*\n" +
                                "  - Extension mappings: *.jsp\n" +
                                "\nFor patterns like /API/path/*/operation, use URL rewriting with /APISpringInternal\n"
                                +
                                "See: doc/LEGACY_API_TO_SPRING_MVC_GUIDE.md (Wildcard in the MIDDLE box, Step 5)")
                .isEmpty();
    }

    /**
     * Checks if a URL pattern has a wildcard in the middle of the path.
     * Valid patterns:
     * - /path/* (wildcard at end)
     * - *.ext (extension mapping)
     * - /path (no wildcard)
     * Invalid patterns:
     * - /path/*&#47;something (wildcard in middle)
     * - /path/*&#47;*&#47;something (multiple wildcards)
     */
    private boolean hasWildcardInMiddle(String pattern) {
        if (!pattern.contains("*")) {
            return false; // No wildcard at all
        }

        // Extension mapping (*.jsp) is valid
        if (pattern.startsWith("*.")) {
            return false;
        }

        // Wildcard at the end (/path/*) is valid
        return !pattern.endsWith("/*");

        // Wildcard anywhere else is in the middle
    }

    private List<String> parseUrlPatternsForServlet(File webXml, String servletName) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(webXml);
        doc.getDocumentElement().normalize();

        List<String> patterns = new ArrayList<>();

        // Find servlet-mapping elements
        NodeList servletMappings = doc.getElementsByTagName("servlet-mapping");
        for (int i = 0; i < servletMappings.getLength(); i++) {
            Element mapping = (Element) servletMappings.item(i);

            // Check if this mapping is for the target servlet
            String mappingServletName = getTextContent(mapping, "servlet-name");
            if (servletName.equals(mappingServletName)) {
                // Get all url-pattern elements for this servlet
                NodeList urlPatterns = mapping.getElementsByTagName("url-pattern");
                for (int j = 0; j < urlPatterns.getLength(); j++) {
                    String pattern = urlPatterns.item(j).getTextContent().trim();
                    patterns.add(pattern);
                }
            }
        }

        return patterns;
    }

    private String getTextContent(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return null;
    }
}
