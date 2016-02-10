/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.expression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Map;

import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BusinessObjectDAOExpressionStrategyTest {

    public static final String ORG_BONITASOFT_ENGINE_EXPRESSION_DUMMY_SERVER_DAO = "org.bonitasoft.engine.expression.DummyServerDAO";
    @Mock
    private BusinessDataRepository businessDataRepository;

    @InjectMocks
    private BusinessObjectDAOExpressionStrategy businessObjectDAOExpressionStrategy;

    @Test
    public void instantiateDAOShouldWorkAndInjectBizDataRepo() throws Exception {
        DummyServerDAO daoInstance = (DummyServerDAO) businessObjectDAOExpressionStrategy.instantiateDAO(ORG_BONITASOFT_ENGINE_EXPRESSION_DUMMY_SERVER_DAO);

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
        doReturn(ORG_BONITASOFT_ENGINE_EXPRESSION_DUMMY_SERVER_DAO).when(spy).getDAOServerImplementationFromInterface(daoClassName);

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
        String daoImplClassName = ORG_BONITASOFT_ENGINE_EXPRESSION_DUMMY_SERVER_DAO;
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
