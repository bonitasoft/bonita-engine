package org.bonitasoft.engine.identity.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.xml.XMLWriter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ExportOrganizationTest {
    
    private static final int MAX_RESULTS = 2;

    @Mock
    private IdentityService identityService;
    
    @Mock
    private XMLWriter xmlWriter;
    
    @Mock
    private SCustomUserInfoDefinition userInfoDef1;

    @Mock
    private SCustomUserInfoDefinition userInfoDef2;
    
    @Mock
    private SCustomUserInfoDefinition userInfoDef3;
    
    private ExportOrganization exportOrganization;
    
    @Before
    public void setUp() throws Exception {
        exportOrganization = new ExportOrganization(xmlWriter, identityService, MAX_RESULTS);
    }
    
    @Test
    public void getAllCustomUserInfoDefinitions_return_all_custom_user_info_definition_from_service() throws Exception {
        //given
        given(identityService.getCustomUserInfoDefinitions(0, MAX_RESULTS)).willReturn(Arrays.asList(userInfoDef1, userInfoDef2));
        given(identityService.getCustomUserInfoDefinitions(2, MAX_RESULTS)).willReturn(Arrays.asList(userInfoDef3));
        
        //when
        List<SCustomUserInfoDefinition> allCustomUserInfoDefinitions = exportOrganization.getAllCustomUserInfoDefinitions();

        //then
        assertThat(allCustomUserInfoDefinitions).isEqualTo(Arrays.asList(userInfoDef1, userInfoDef2, userInfoDef3));
    }

}
