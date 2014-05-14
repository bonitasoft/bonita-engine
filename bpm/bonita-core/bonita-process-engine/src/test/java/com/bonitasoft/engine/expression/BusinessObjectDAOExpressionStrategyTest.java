package com.bonitasoft.engine.expression;

import static org.assertj.core.api.Assertions.assertThat;

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
    public void getDAOServerImplementationShouldReturnProperImplemNameFromInterface() throws Exception {
        String implemClassName = businessObjectDAOExpressionStrategy.getDAOServerImplementationFromInterface("org.package.MyObjectDAO");

        assertThat(implemClassName).isEqualTo("org.package.server.MyObjectDAOImpl");
    }

    @Test
    public void getDAOServerImplementationShouldReturnProperImplemNameForDefaultPackageName() throws Exception {
        String implemClassName = businessObjectDAOExpressionStrategy.getDAOServerImplementationFromInterface("BusinessDAO");

        assertThat(implemClassName).isEqualTo("server.BusinessDAOImpl");
    }

}
