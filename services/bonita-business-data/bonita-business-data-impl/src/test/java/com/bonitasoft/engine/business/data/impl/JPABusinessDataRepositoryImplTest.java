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
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class JPABusinessDataRepositoryImplTest {

	private DependencyService dependencyService;

	@Before
	public void setUp() throws Exception {
		dependencyService = mock(DependencyService.class);
	}

	@Test
	public void testGetPersistenceFileContentFor() throws Exception {
		// given
		JPABusinessDataRepositoryImpl bdrService = new JPABusinessDataRepositoryImpl(dependencyService);
		String classname1 = "com.worldcompany.biz.Auction";
		String classname2 = "com.worldcompany.biz.StakeHolder";
		String classname3 = "com.worldcompany.legal.Benefits";

		// when:
		byte[] persistenceFileContent = bdrService.getPersistenceFileContentFor(Arrays.asList(classname1, classname2, classname3));

		// then:
		String persistenceXMLFileAsString = new String(persistenceFileContent);
		assertTrue("missing bean 1", persistenceXMLFileAsString.contains("<class>" + classname1 + "</class>"));
		assertTrue("missing bean 2", persistenceXMLFileAsString.contains("<class>" + classname2 + "</class>"));
		assertTrue("missing bean 3", persistenceXMLFileAsString.contains("<class>" + classname3 + "</class>"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldTransformBDRArchive_ThrowIllegalArgumentExceptionIfPersistenceXMLAlreadyExists() throws Exception {
		JPABusinessDataRepositoryImpl bdrService = new JPABusinessDataRepositoryImpl(dependencyService);
		byte[] bdrArchive = IOUtil.getAllContentFrom(JPABusinessDataRepositoryImplTest.class.getResourceAsStream("bdr-jar.bak"));
		bdrService.transformBDRArchive(bdrArchive);
	}

	@Test
	public void shouldTransformBDRArchive_ReturnAJarWithAValidPersistenceXMLEntry() throws Exception {
		JPABusinessDataRepositoryImpl bdrService = new JPABusinessDataRepositoryImpl(dependencyService);
		byte[] bdrArchive = IOUtil.getAllContentFrom(JPABusinessDataRepositoryImplTest.class.getResourceAsStream("bdr-with-relations-without-persistence-jar.bak"));
		byte[] updatedJar = bdrService.transformBDRArchive(bdrArchive);
		assertThat(updatedJar).isNotNull();
		ByteArrayInputStream bais = new ByteArrayInputStream(updatedJar);
		JarInputStream jis = new JarInputStream(bais);
		JarEntry entry = null;
		Map<String,byte[]> entryNames = new HashMap<String, byte[]>();
		byte[] buffer = new byte[4096];
		while ((entry = jis.getNextJarEntry()) != null) {
			if(!entry.isDirectory()){
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int len;
				while ((len=jis.read(buffer))>0) {
					baos.write(buffer, 0, len);
				}
				baos.close();
				entryNames.put(entry.getName(),baos.toByteArray());
			}
		}
		jis.close();
		assertThat(entryNames.keySet()).contains("META-INF/persistence.xml");
		byte[] persistenceXMLContent = entryNames.get("META-INF/persistence.xml");

		final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setValidating(false);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		InputStream is = new ByteArrayInputStream(persistenceXMLContent);
		Document document = documentBuilder.parse(is);
		is.close();
		assertThat(document).isNotNull();
		assertThat(document.getElementsByTagName("class").getLength()).isEqualTo(3);
		NodeList nodeList = document.getElementsByTagName("class");
		Set<String> classnames = new HashSet<String>();
		for(int i = 0 ; i<nodeList.getLength();i++){
			classnames.add(nodeList.item(i).getTextContent());
		}
		assertThat(classnames).containsOnly("org.bonitasoft.hr.LeaveRequest","org.bonitasoft.hr.LeaveItem","org.bonitasoft.hr.Employee");
	}

	@Test
	public void shouldDeploy_AddATenantDependency() throws Exception {
		JPABusinessDataRepositoryImpl bdrService = spy(new JPABusinessDataRepositoryImpl(dependencyService));
		SDependency sDependency = mock(SDependency.class);
		SDependencyMapping dependencyMapping = mock(SDependencyMapping.class);
		doReturn(sDependency).when(bdrService).createSDependency(any(byte[].class));
		doReturn(dependencyMapping).when(bdrService).createDependencyMapping(1, sDependency);
		byte[] bdrArchive = IOUtil.getAllContentFrom(JPABusinessDataRepositoryImplTest.class.getResourceAsStream("bdr-without-persistence-jar.bak"));
		bdrService.deploy(bdrArchive, 1);

		verify(dependencyService).createDependency(sDependency);
		verify(bdrService).createDependencyMapping(1, sDependency);
		verify(dependencyService).createDependencyMapping(dependencyMapping);
	}
}
