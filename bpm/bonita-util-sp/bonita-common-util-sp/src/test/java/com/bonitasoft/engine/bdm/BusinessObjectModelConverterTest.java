package com.bonitasoft.engine.bdm;

import static com.bonitasoft.engine.bdm.BOMBuilder.aBOM;
import static com.bonitasoft.engine.bdm.model.builder.BusinessObjectBuilder.aBO;
import static com.bonitasoft.engine.bdm.model.builder.FieldBuilder.aBooleanField;
import static com.bonitasoft.engine.bdm.model.builder.FieldBuilder.aCompositionField;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.io.IOUtils;

public class BusinessObjectModelConverterTest {

    @Test
    public void zipThenUnzipBOMShouldReturnTheOriginalBOM() throws Exception {
        final BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        final BusinessObjectModel bom = new BOMBuilder().buildDefaultBOM();
        final byte[] zip = convertor.zip(bom);
        final BusinessObjectModel actual = convertor.unzip(zip);

        assertThat(actual).isEqualTo(bom);
    }

    @Test
    public void zipThenUnzipBOMShouldReturnTheOriginalBOMWithUniqueConstraint() throws Exception {
        final BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        final BusinessObjectModel bom = new BOMBuilder().buildBOMWithUniqueConstraint();
        final byte[] zip = convertor.zip(bom);
        final BusinessObjectModel actual = convertor.unzip(zip);

        assertThat(actual).isEqualTo(bom);
    }

    @Test
    public void zipThenUnzipBOMShouldReturnTheOriginalBOMWithQuery() throws Exception {
        final BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        final BusinessObjectModel bom = new BOMBuilder().buildBOMWithQuery();
        final byte[] zip = convertor.zip(bom);
        final BusinessObjectModel actual = convertor.unzip(zip);

        assertThat(actual).isEqualTo(bom);
    }

    @Test(expected = JAXBException.class)
    public void zipAnEmptyBOMShouldThrowAnException() throws Exception {
        final BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        final BusinessObjectModel bom = new BOMBuilder().buildEmptyBOM();
        convertor.zip(bom);
    }

    @Test(expected = JAXBException.class)
    public void zipAnBOMWithAnEmptyShouldThrowAnException() throws Exception {
        final BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        final BusinessObjectModel bom = new BOMBuilder().buildBOMWithAnEmptyEntity();
        convertor.zip(bom);
    }

    @Test(expected = JAXBException.class)
    public void zipAnBOMWithAnEmptyFieldShouldThrowAnException() throws Exception {
        final BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        final BusinessObjectModel bom = new BOMBuilder().buildBOMWithAnEmptyField();
        convertor.zip(bom);
    }

    @Test(expected = IOException.class)
    public void unzipADifferentZipThrowAnException() throws Exception {
        final byte[] zip = IOUtils.zip("bonita", "bpm".getBytes());
        final BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        convertor.unzip(zip);
    }

    @Test
    public void aBom_could_have_fields_with_relationships() throws Exception {
        BusinessObject compositeBO = aBO("compositeBO").withField(aBooleanField("boolean").build()).build();
        BusinessObject composedBO = aBO("composedBO").withField(aCompositionField("composite", compositeBO).build()).build();
        BusinessObjectModel model = aBOM().withBO(compositeBO).withBO(composedBO).build();

        BusinessObjectModel transformedModel = marshallUnmarshall(model);

        assertThat(transformedModel).isEqualTo(model);
    }

    @Test
    public void should_be_backward_compatible() throws Exception {
        byte[] xml = org.apache.commons.io.IOUtils.toByteArray(BusinessObjectModelConverterTest.class.getResourceAsStream("/bom_6.3.0.xml"));
        BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        convertor.unmarshall(xml);
        // expect no unmarshalling exception
    }
    
    private BusinessObjectModel marshallUnmarshall(BusinessObjectModel model) throws JAXBException, IOException, SAXException {
        BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        byte[] marshall = convertor.marshall(model);
        return convertor.unmarshall(marshall);
    }
}
