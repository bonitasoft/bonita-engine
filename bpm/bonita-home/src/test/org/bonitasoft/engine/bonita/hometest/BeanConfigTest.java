package org.bonitasoft.engine.bonita.hometest;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.junit.Test;

/**
 * @author Laurent Leseigneur
 */
public class BeanConfigTest {

    public static final String HIBERNATE_CURRENT_SESSION_CONTEXT_CLASS = "hibernate.current_session_context_class";
    public static final String THREAD = "thread";

    @Test
    public void should_businessDataRepository_be_configured_at_thred_level() throws Exception {
        // given
        final Document document = getDocument("/bonita-tenant-community.xml");
        final String beanId = "businessDataRepository";
        final String xpathExpression = new StringBuilder()
                .append("//b:bean[@id='")
                .append(beanId)
                .append("']/b:constructor-arg[@name='")
                .append("configuration")
                .append("']/b:map/b:entry[@key='")
                .append(HIBERNATE_CURRENT_SESSION_CONTEXT_CLASS)
                .append("']").toString();

        //when
        final Node node1 = getConfigNode(document, xpathExpression);

        //then
        assertThat(node1.selectSingleNode("@value").getStringValue()).isEqualTo(THREAD);

    }

    @Test
    public void should_communityHbmConfigurationProviderProperties_be_configured_at_thread_level() throws Exception {
        // given
        final Document document = getDocument("/bonita-platform-community.xml");
        final String beanId = "communityHbmConfigurationProviderProperties";
        final String xpathExpression = new StringBuilder()
                .append("//b:bean[@id='")
                .append(beanId)
                .append("']/b:property/b:map")
                .append("/b:entry[@key='")
                .append(HIBERNATE_CURRENT_SESSION_CONTEXT_CLASS)
                .append("']").toString();

        //when
        final Node node1 = getConfigNode(document, xpathExpression);

        //then
        assertThat(node1.selectSingleNode("@value").getStringValue()).isEqualTo(THREAD);

    }

    private Node getConfigNode(Document document, String xpathExpression) {
        Map uris = new HashMap();
        uris.put("b", "http://www.springframework.org/schema/beans");
        XPath xpath = document.createXPath(
                xpathExpression);
        xpath.setNamespaceURIs(uris);
        List<Node> nodes = xpath.selectNodes(document);

        assertThat(nodes).hasSize(1);
        return nodes.get(0);
    }

    private Document getDocument(String configFile) throws IOException, DocumentException {
        InputStream inputStream = this.getClass().getResourceAsStream(configFile);
        final byte[] byteArray = IOUtils.toByteArray(inputStream);
        return DocumentHelper.parseText(new String(byteArray));
    }

}
