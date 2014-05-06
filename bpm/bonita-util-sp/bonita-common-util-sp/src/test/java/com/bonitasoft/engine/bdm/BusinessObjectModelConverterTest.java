package com.bonitasoft.engine.bdm;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;

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

}
