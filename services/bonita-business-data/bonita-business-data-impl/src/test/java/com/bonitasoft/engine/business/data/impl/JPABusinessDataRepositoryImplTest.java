package com.bonitasoft.engine.business.data.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.bonitasoft.engine.bdm.BusinessObject;
import com.bonitasoft.engine.bdm.BusinessObjectModel;
import com.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import com.bonitasoft.engine.bdm.Field;
import com.bonitasoft.engine.bdm.FieldType;

public class JPABusinessDataRepositoryImplTest {

    private DependencyService dependencyService;

    @Before
    public void setUp() throws Exception {
        dependencyService = mock(DependencyService.class);
    }

    @Test
    public void testGetPersistenceFileContentFor() throws Exception {
        // given
        final JPABusinessDataRepositoryImpl bdrService = new JPABusinessDataRepositoryImpl(dependencyService, Collections.<String, Object> emptyMap());
        final String classname1 = "com.worldcompany.biz.Auction";
        final String classname2 = "com.worldcompany.biz.StakeHolder";
        final String classname3 = "com.worldcompany.legal.Benefits";

        // when:
        final byte[] persistenceFileContent = bdrService.getPersistenceFileContentFor(Arrays.asList(classname1, classname2, classname3));

        // then:
        final String persistenceXMLFileAsString = new String(persistenceFileContent);
        assertTrue("missing bean 1", persistenceXMLFileAsString.contains("<class>" + classname1 + "</class>"));
        assertTrue("missing bean 2", persistenceXMLFileAsString.contains("<class>" + classname2 + "</class>"));
        assertTrue("missing bean 3", persistenceXMLFileAsString.contains("<class>" + classname3 + "</class>"));
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void shouldTransformBDRArchive_ThrowIllegalArgumentExceptionIfPersistenceXMLAlreadyExists() throws Exception {
        final JPABusinessDataRepositoryImpl bdrService = new JPABusinessDataRepositoryImpl(dependencyService, Collections.<String, Object> emptyMap());
        final byte[] bdrArchive = IOUtil.getAllContentFrom(JPABusinessDataRepositoryImplTest.class.getResourceAsStream("bdr-jar.bak"));
        bdrService.buildBDMJAR(bdrArchive);
    }

    @Test
    @Ignore
    public void shouldTransformBDRArchive_ReturnAJarWithAValidPersistenceXMLEntry() throws Exception {
        final JPABusinessDataRepositoryImpl bdrService = new JPABusinessDataRepositoryImpl(dependencyService, Collections.<String, Object> emptyMap());
        final byte[] bdrArchive = IOUtil.getAllContentFrom(JPABusinessDataRepositoryImplTest.class
                .getResourceAsStream("bdr-with-relations-without-persistence-jar.bak"));
        final byte[] updatedJar = bdrService.buildBDMJAR(bdrArchive);
        assertThat(updatedJar).isNotNull();
        final ByteArrayInputStream bais = new ByteArrayInputStream(updatedJar);
        final JarInputStream jis = new JarInputStream(bais);
        JarEntry entry = null;
        final Map<String, byte[]> entryNames = new HashMap<String, byte[]>();
        final byte[] buffer = new byte[4096];
        while ((entry = jis.getNextJarEntry()) != null) {
            if (!entry.isDirectory()) {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int len;
                while ((len = jis.read(buffer)) > 0) {
                    baos.write(buffer, 0, len);
                }
                baos.close();
                entryNames.put(entry.getName(), baos.toByteArray());
            }
        }
        jis.close();
        assertThat(entryNames.keySet()).contains("META-INF/persistence.xml");
        final byte[] persistenceXMLContent = entryNames.get("META-INF/persistence.xml");

        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(false);
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final InputStream is = new ByteArrayInputStream(persistenceXMLContent);
        final Document document = documentBuilder.parse(is);
        is.close();
        assertThat(document).isNotNull();
        assertThat(document.getElementsByTagName("class").getLength()).isEqualTo(3);
        final NodeList nodeList = document.getElementsByTagName("class");
        final Set<String> classnames = new HashSet<String>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            classnames.add(nodeList.item(i).getTextContent());
        }
        assertThat(classnames).containsOnly("org.bonitasoft.hr.LeaveRequest", "org.bonitasoft.hr.LeaveItem", "org.bonitasoft.hr.Employee");
    }

    @Test
    @Ignore
    public void shouldDeploy_AddATenantDependency() throws Exception {
        final JPABusinessDataRepositoryImpl bdrService = spy(new JPABusinessDataRepositoryImpl(dependencyService, Collections.<String, Object> emptyMap()));
        final SDependency sDependency = mock(SDependency.class);
        final SDependencyMapping dependencyMapping = mock(SDependencyMapping.class);
        doReturn(sDependency).when(bdrService).createSDependency(any(byte[].class));
        doReturn(dependencyMapping).when(bdrService).createDependencyMapping(1, sDependency);
        final byte[] bdrArchive = IOUtil.getAllContentFrom(JPABusinessDataRepositoryImplTest.class.getResourceAsStream("bdr-without-persistence-jar.bak"));
        bdrService.deploy(bdrArchive, 1);

        verify(dependencyService).createDependency(sDependency);
        verify(bdrService).createDependencyMapping(1, sDependency);
        verify(dependencyService).createDependencyMapping(dependencyMapping);
    }

    @Test
    public void shouldBuildBDMJAR_ReturnAByteArray() throws Exception {
        final BusinessObjectModel bom = new BusinessObjectModel();
        final BusinessObject businessObject = new BusinessObject();
        businessObject.setQualifiedName("org.bonitasoft.pojo.Employee");
        final Field name = new Field();
        name.setName("name");
        name.setType(FieldType.STRING);
        businessObject.addField(name);
        bom.addBusinessObject(businessObject);

        final JPABusinessDataRepositoryImpl bdrService = new JPABusinessDataRepositoryImpl(dependencyService, Collections.<String, Object> emptyMap());
        assertThat(bdrService.buildBDMJAR(new BusinessObjectModelConverter().zip(bom))).isNotEmpty();
    }

}
