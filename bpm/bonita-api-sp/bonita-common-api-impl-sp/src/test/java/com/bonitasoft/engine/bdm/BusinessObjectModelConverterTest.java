package com.bonitasoft.engine.bdm;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.bonitasoft.engine.exception.CreationException;
import org.junit.Test;

import com.bonitasoft.engine.io.IOUtils;

public class BusinessObjectModelConverterTest {

    @Test
    public void zipThenUnzipBOMShouldReturnTheOriginalBOM() throws IOException, CreationException {
        final BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        final BusinessObjectModel bom = new BOMBuilder().buildDefaultBOM();
        final byte[] zip = convertor.zip(bom);
        final BusinessObjectModel actual = convertor.unzip(zip);

        assertThat(actual).isEqualTo(bom);
    }

    @Test(expected = CreationException.class)
    public void zipAnEmptyBOMShouldThrowAnException() throws IOException, CreationException {
        final BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        final BusinessObjectModel bom = new BOMBuilder().buildEmptyBOM();
        convertor.zip(bom);
    }

    @Test(expected = CreationException.class)
    public void zipAnBOMWithAnEmptyShouldThrowAnException() throws IOException, CreationException {
        final BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        final BusinessObjectModel bom = new BOMBuilder().buildBOMWithAnEmptyEntity();
        convertor.zip(bom);
    }

    @Test(expected = CreationException.class)
    public void zipAnBOMWithAnEmptyFieldShouldThrowAnException() throws IOException, CreationException {
        final BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        final BusinessObjectModel bom = new BOMBuilder().buildBOMWithAnEmptyField();
        convertor.zip(bom);
    }

    @Test(expected = CreationException.class)
    public void unzipADifferentZipThrowAnException() throws IOException, CreationException {
        final byte[] zip = IOUtils.zip("bonita", "bpm".getBytes());
        final BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        convertor.unzip(zip);
    }

}
