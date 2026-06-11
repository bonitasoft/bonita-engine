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
 * Validates that URL rewrite rules are properly configured for Spring MVC endpoints
 * that cannot be expressed with Tomcat servlet mapping wildcards.
 * When servlet patterns like /API/path/*&#47;operation are needed (wildcard in middle),
 * URL rewriting with /APISpringInternal must be used as a workaround.
 */
class UrlRewriteConfigurationTest {

    private static final String URLREWRITE_XML_PATH = "src/main/webapp/WEB-INF/urlrewrite.xml";

    @Test
    void should_have_urlrewrite_rule_for_process_design_endpoint() throws Exception {
        // Given
        File urlRewriteXml = new File(URLREWRITE_XML_PATH);
        assertThat(urlRewriteXml)
                .withFailMessage("urlrewrite.xml not found at: " + URLREWRITE_XML_PATH)
                .exists();

        // When
        List<UrlRewriteRule> rules = parseUrlRewriteRules(urlRewriteXml);

        // Then - Should have rule for process design endpoint
        boolean hasProcessDesignRule = rules.stream()
                .anyMatch(rule -> rule.from.contains("/API/bpm/process/")
                        && rule.from.contains("/design")
                        && rule.to.contains("/APISpringInternal/bpm/process/")
                        && rule.to.contains("/design"));

        assertThat(hasProcessDesignRule)
                .withFailMessage(
                        """
                                Missing URL rewrite rule for /API/bpm/process/{id}/design endpoint.

                                Expected rule:
                                  <from>^(/portal/custom-page)?/API/bpm/process/([^/]+)/design$</from>
                                  <to>/APISpringInternal/bpm/process/$2/design</to>

                                This rule is required because Tomcat servlet mappings don't support
                                wildcards in the middle of paths (/API/bpm/process/*/design).

                                See: doc/LEGACY_API_TO_SPRING_MVC_GUIDE.md - "Wildcard in the MIDDLE" box in Step 5""")
                .isTrue();
    }

    private List<UrlRewriteRule> parseUrlRewriteRules(File urlRewriteXml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(urlRewriteXml);
        doc.getDocumentElement().normalize();

        List<UrlRewriteRule> rules = new ArrayList<>();

        NodeList ruleNodes = doc.getElementsByTagName("rule");
        for (int i = 0; i < ruleNodes.getLength(); i++) {
            Element ruleElement = (Element) ruleNodes.item(i);

            String from = getTextContent(ruleElement, "from");
            String to = getTextContent(ruleElement, "to");

            if (from != null && to != null) {
                rules.add(new UrlRewriteRule(from, to));
            }
        }

        return rules;
    }

    private String getTextContent(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return null;
    }

    private record UrlRewriteRule(String from, String to) {}
}
