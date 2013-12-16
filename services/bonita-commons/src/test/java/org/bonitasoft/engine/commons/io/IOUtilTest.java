package org.bonitasoft.engine.commons.io;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;

public class IOUtilTest {

	@Test
	public void testGetClassNameList() throws Exception {
		// given:
		byte[] jarContent = IOUtil.getAllContentFrom(new File(IOUtilTest.class.getResource("bdr-jar.bak").getFile()));

		// when:
		List<String> classNameList = IOUtil.getClassNameList(jarContent);

		// then:
		assertThat(classNameList).containsOnly("org.bonita.pojo.Employee");
	}


	@Test(expected=IllegalArgumentException.class)
	public void shouldToByteArray_ThrowIlllegalArgumentException_ForNullDocument() throws Exception {
		Document document = null;
		IOUtil.toByteArray(document);
	}

	@Test
	public void shouldToByteArray_ForDocumentReturnAByteArray() throws Exception {
		final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setValidating(false);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

		InputStream is = null ;
		Document document = null;
		try{
			is = IOUtilTest.class.getResourceAsStream("persistence.xml");
			document = documentBuilder.parse(is);
		}finally{
			is.close();
		}
		byte[] byteArray = IOUtil.toByteArray(document);
		assertThat(byteArray).isNotNull();
	}
}
