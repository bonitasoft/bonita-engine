/**
 * Copyright (C) 2016 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

package org.bonitasoft.engine.bpm.bar;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.Charset;

import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Baptiste Mesta
 */
public class BusinessArchiveFactoryTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private File barFile;

    @After
    public void after() throws Exception {
        setEncoding("UTF-8");
    }

    public void setEncoding(String encoding) throws Exception {
        System.setProperty("file.encoding", encoding);
        Field charset = Charset.class.getDeclaredField("defaultCharset");
        charset.setAccessible(true);
        charset.set(null, null);
    }


    @Before
    public void setUp() throws Exception {
        File barFile1 = temporaryFolder.newFile("myBar.bar");
        barFile1.delete();
        barFile = barFile1;
    }


    @Test
    public void should_write_non_UTF8_and_read_UTF8_BAR_works() throws Exception {
        //given
        BusinessArchive businessArchive = createBusinessArchive();
        setEncoding("windows-1252");
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, barFile);
        //when
        setEncoding("UTF-8");
        BusinessArchive deserializeBAR = BusinessArchiveFactory.readBusinessArchive(barFile);
        //then
        assertThat(deserializeBAR.getProcessDefinition().getName()).isEqualTo("说话_éé");
    }

    @Test
    public void should_write_UTF8_and_read_non_UTF8_BAR_works() throws Exception {
        //given
        BusinessArchive businessArchive = createBusinessArchive();
        setEncoding("UTF-8");
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, barFile);
        //when
        setEncoding("windows-1252");
        BusinessArchive deserializeBAR = BusinessArchiveFactory.readBusinessArchive(barFile);
        //then
        assertThat(deserializeBAR.getProcessDefinition().getName()).isEqualTo("说话_éé");
    }

    private BusinessArchive createBusinessArchive() throws InvalidBusinessArchiveFormatException, InvalidProcessDefinitionException {
        return new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(new ProcessDefinitionBuilder().createNewInstance("说话_éé", "1.0").addAutomaticTask("说话_éé_task").getProcess())
                .done();
    }


}