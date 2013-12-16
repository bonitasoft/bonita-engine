package com.bonitasoft.engine.business.data.impl;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class JPABusinessDataRepositoryImplTest {

    @Test
    public void testGetPersistenceFileContentFor() throws Exception {
        // given
        JPABusinessDataRepositoryImpl bdrService = new JPABusinessDataRepositoryImpl();
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
        // System.out.println(persistenceXMLFileAsString);
    }
}
