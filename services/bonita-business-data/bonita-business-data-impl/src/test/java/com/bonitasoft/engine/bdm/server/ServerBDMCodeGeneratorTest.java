package com.bonitasoft.engine.bdm.server;

import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.server.ServerBDMCodeGenerator;
import com.sun.codemodel.JDefinedClass;

/**
 * @author Romain Bioteau
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class ServerBDMCodeGeneratorTest {

    private ServerBDMCodeGenerator serverBDMCodeGenerator;

    @Mock
    private BusinessObject bo;

    @Mock
    private JDefinedClass entity;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        BusinessObjectModel bom = new BusinessObjectModel();
        serverBDMCodeGenerator = new ServerBDMCodeGenerator(bom);
    }

    @Test
    public void should_addDao_donothing() throws Exception {
        serverBDMCodeGenerator.addDAO(bo, entity);

        verifyZeroInteractions(bo);
        verifyZeroInteractions(entity);
    }

}
