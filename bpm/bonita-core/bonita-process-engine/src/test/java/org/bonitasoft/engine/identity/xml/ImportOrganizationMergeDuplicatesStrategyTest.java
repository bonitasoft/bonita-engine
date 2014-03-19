package org.bonitasoft.engine.identity.xml;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.identity.CustomUserInfoDefinitionCreator;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoDefinitionUpdateBuilder;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoDefinitionUpdateBuilderFactory;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ImportOrganizationMergeDuplicatesStrategyTest {
    
    @Mock
    private IdentityService identityService;
    
    @InjectMocks
    private ImportOrganizationMergeDuplicatesStrategy strategy;
    
    @Test
    public void foundExistingCustomUserInfoDefinition_should_call_service_to_update_element() throws Exception {
        //given
        String newDescription = "updated description";
        SCustomUserInfoDefinition existingUserInfoDefinition = mock(SCustomUserInfoDefinition.class);
        CustomUserInfoDefinitionCreator creator = new CustomUserInfoDefinitionCreator("name", newDescription);
        EntityUpdateDescriptor updateDescriptor = getUpdateDescriptor(newDescription);
        
        //when
        strategy.foundExistingCustomUserInfoDefinition(existingUserInfoDefinition, creator);

        //then
        verify(identityService, times(1)).updateCustomUserInfoDefinition(existingUserInfoDefinition, updateDescriptor);
    }

    private EntityUpdateDescriptor getUpdateDescriptor(String newDescription) {
        SCustomUserInfoDefinitionUpdateBuilder builder = BuilderFactory.get(SCustomUserInfoDefinitionUpdateBuilderFactory.class).createNewInstance();
        builder.updateDescription(newDescription);
        EntityUpdateDescriptor updateDescriptor = builder.done();
        return updateDescriptor;
    }

}
