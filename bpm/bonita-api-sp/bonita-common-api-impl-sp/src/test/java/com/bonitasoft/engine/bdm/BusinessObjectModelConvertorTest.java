package com.bonitasoft.engine.bdm;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.bonitasoft.engine.exception.CreationException;
import org.junit.Test;

public class BusinessObjectModelConvertorTest {

    @Test
    public void zipThenUnzipBOMShouldReturnTheOriginalBOM() throws IOException, CreationException {
        final BusinessObjectModelConvertor convertor = new BusinessObjectModelConvertor();
        final BusinessObjectModel bom = new BOMBuilder().buildDefaultBOM();
        final byte[] zip = convertor.zip(bom);
        final BusinessObjectModel actual = convertor.unzip(zip);

        assertThat(actual).isEqualTo(bom);
    }

}
