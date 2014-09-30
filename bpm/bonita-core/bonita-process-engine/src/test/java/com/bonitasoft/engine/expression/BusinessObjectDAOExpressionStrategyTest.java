/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.expression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Map;

import org.bonitasoft.engine.expression.ExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.business.data.BusinessDataRepository;

@RunWith(MockitoJUnitRunner.class)
public class BusinessObjectDAOExpressionStrategyTest {

    @Mock
    private BusinessDataRepository businessDataRepository;

    @InjectMocks
    private BusinessObjectDAOExpressionStrategy businessObjectDAOExpressionStrategy;

    @Test
    public void instantiateDAOShouldWorkAndInjectBizDataRepo() throws Exception {
        DummyServerDAO daoInstance = (DummyServerDAO) businessObjectDAOExpressionStrategy.instantiateDAO("com.bonitasoft.engine.expression.DummyServerDAO");

        assertThat(daoInstance).isInstanceOf(DummyServerDAO.class);
        assertThat(daoInstance.getBusinessDataRepository()).isNotNull();
    }

    @Test
    public void getDAOServerImplementationShouldReturnProperImplemNameFromInterface() {
        String implemClassName = businessObjectDAOExpressionStrategy.getDAOServerImplementationFromInterface("org.package.MyObjectDAO");

        assertThat(implemClassName).isEqualTo("org.package.server.MyObjectDAOImpl");
    }

    @Test
    public void getDAOServerImplementationShouldReturnProperImplemNameForDefaultPackageName() {
        String implemClassName = businessObjectDAOExpressionStrategy.getDAOServerImplementationFromInterface("BusinessDAO");

        assertThat(implemClassName).isEqualTo("server.BusinessDAOImpl");
    }

    @Test
    public void evaluateShouldComputeImplementationClassName() throws Exception {
        // given:
        String daoClassName = "SomeDAOInterfaceName";
        SExpressionImpl expression = new SExpressionImpl("name", "myDAO", "dummyExpressionType", daoClassName, null, null);
        Map<String, Object> context = Collections.emptyMap();
        BusinessObjectDAOExpressionStrategy spy = spy(businessObjectDAOExpressionStrategy);
        doReturn("com.bonitasoft.engine.expression.DummyServerDAO").when(spy).getDAOServerImplementationFromInterface(daoClassName);

        // when:
        spy.evaluate(expression, context, null, null);

        // then:
        verify(spy).getDAOServerImplementationFromInterface(daoClassName);
    }

    @Test
    public void evaluateShouldInstantiateDAOImplementationClassName() throws Exception {
        // given:
        String daoClassName = "SomeDAOInterfaceName";
        SExpressionImpl expression = new SExpressionImpl("name", "myDAO", "dummyExpressionType", daoClassName, null, null);
        Map<String, Object> context = Collections.emptyMap();
        BusinessObjectDAOExpressionStrategy spy = spy(businessObjectDAOExpressionStrategy);
        String daoImplClassName = "com.bonitasoft.engine.expression.DummyServerDAO";
        doReturn(daoImplClassName).when(spy).getDAOServerImplementationFromInterface(daoClassName);

        // when:
        spy.evaluate(expression, context, null, null);

        // then:
        verify(spy).instantiateDAO(daoImplClassName);
    }

    @Test
    public void getExpressionKindShouldReturn_BUSINESS_OBJECT_DAO_kind() {
        assertThat(businessObjectDAOExpressionStrategy.getExpressionKind()).isEqualTo(ExpressionExecutorStrategy.KIND_BUSINESS_OBJECT_DAO);
    }
}
