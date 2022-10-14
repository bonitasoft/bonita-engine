/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.bonitasoft.console.common.server.utils.ContractTypeConverter.ISO_8601_DATE_PATTERNS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.FileInputValue;
import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.ContractDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.InputDefinitionImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ContractTypeConverterTest {

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

    public static final long DATE_01_01_1970_13H_AS_LONG_GMT = 46800000L;

    public static final String DATE_01_01_1970_13H_AS_STRING_GMT = "1970-01-01T13:00:00.000Z";

    public static final LocalDate testLocalDate = LocalDate.of(2012, 4, 21);

    public static final LocalDateTime testLocalDateTime = LocalDateTime.of(2012, 4, 21, 17, 42, 29);

    @Mock
    ContractDefinition contractDefinition;

    @Mock
    BonitaHomeFolderAccessor bonitaHomeFolderAccessor;

    long maxSizeForTenant = 1000L;

    String filename = "file.txt";

    String fileContentString = "content";

    @Spy
    @InjectMocks
    ContractTypeConverter contractTypeConverter = new ContractTypeConverter(ISO_8601_DATE_PATTERNS);

    private File generateTempFile() throws IOException {
        final File tempFile = File.createTempFile(this.getClass().getName(), null);
        tempFile.deleteOnExit();
        FileUtils.writeByteArrayToFile(tempFile, fileContentString.getBytes("UTF-8"));
        return tempFile;
    }

    @Test
    public void getProcessedInputs_with_empty_contract_should_return_unmodified_inputs() throws Exception {
        when(contractDefinition.getInputs()).thenReturn(Collections.<InputDefinition> emptyList());
        final Map<String, Serializable> input = new HashMap<>();
        input.put("input1", "value1");
        input.put("input2", "value2");

        final Map<String, Serializable> processedInput = contractTypeConverter.getProcessedInput(contractDefinition,
                input, maxSizeForTenant);

        assertThat(processedInput).isEqualTo(input);
    }

    @Test
    public void getProcessedInput_when_no_contract() throws Exception {
        final Map<String, Serializable> input = new HashMap<>();

        final Map<String, Serializable> processedInput = contractTypeConverter.getProcessedInput(null, input,
                maxSizeForTenant);

        assertThat(processedInput).isEqualTo(input);
    }

    @Test
    public void getProcessedInput_when_null_input() throws Exception {
        when(contractDefinition.getInputs()).thenReturn(Collections.<InputDefinition> emptyList());

        final Map<String, Serializable> processedInput = contractTypeConverter.getProcessedInput(contractDefinition,
                null, maxSizeForTenant);

        assertThat(processedInput).isEqualTo(new HashMap<>());
    }

    @Test
    public void getProcessedInputs_with_invalid_inputs_should_return_unmodified_inputs() throws Exception {
        final List<InputDefinition> inputDefinition = generateSimpleInputDefinition(false);
        when(contractDefinition.getInputs()).thenReturn(inputDefinition);
        final Map<String, Serializable> input = generateInvalidInputMap();

        final Map<String, Serializable> processedInput = contractTypeConverter.getProcessedInput(contractDefinition,
                input, maxSizeForTenant);

        assertThat(processedInput).containsOnly(entry("inputText", "0"), entry("inputBoolean", "hello"),
                entry("inputDate", "0"),
                entry("inputInteger", "hello"), entry("inputDecimal", "hello"), entry("inputLong", "hello"));
    }

    @Test
    public void getProcessedInputs_with_simple_input_should_return_processed_input() throws Exception {
        final List<InputDefinition> inputDefinition = generateSimpleInputDefinition(true);
        when(contractDefinition.getInputs()).thenReturn(inputDefinition);
        final String tempFilePath = "tempFile";
        final File tempFile = generateTempFile();
        doReturn(tempFile).when(bonitaHomeFolderAccessor).getTempFile(tempFilePath);
        final Map<String, Serializable> input = generateInputMap(tempFilePath);

        final Map<String, Serializable> processedInput = contractTypeConverter.getProcessedInput(contractDefinition,
                input, maxSizeForTenant);

        assertThat(processedInput).containsOnly(entry("inputText", "text"), entry("inputBoolean", true),
                entry("inputDate", new Date(DATE_01_01_1970_13H_AS_LONG_GMT)),
                entry("inputInteger", 125686181), entry("inputDecimal", 12.8), entry("inputLong", Long.MAX_VALUE),
                entry("inputLocalDate", testLocalDate),
                entry("inputLocalDateTime", testLocalDateTime),
                entry("inputFile", new FileInputValue(filename, "", fileContentString.getBytes("UTF-8"))));
    }

    @Test
    public void getProcessedInputs_with_complex_input_should_return_processed_input() throws Exception {
        final List<InputDefinition> inputDefinition = generateComplexInputDefinition();
        when(contractDefinition.getInputs()).thenReturn(inputDefinition);
        final Map<String, Serializable> input = new HashMap<>();
        final String tempFilePath = "tempFile";
        final File tempFile = generateTempFile();
        doReturn(tempFile).when(bonitaHomeFolderAccessor).getTempFile(tempFilePath);
        final Map<String, Serializable> complexInput = generateInputMap(tempFilePath);
        input.put("inputComplex", (Serializable) complexInput);

        final Map<String, Serializable> processedInput = contractTypeConverter.getProcessedInput(contractDefinition,
                input, maxSizeForTenant);
        assertThat(processedInput).containsKey("inputComplex");
        final Map<String, Serializable> processedComplexInput = (Map<String, Serializable>) processedInput
                .get("inputComplex");
        assertThat(processedComplexInput).containsOnly(entry("inputText", "text"), entry("inputBoolean", true),
                entry("inputDate", new Date(DATE_01_01_1970_13H_AS_LONG_GMT)),
                entry("inputInteger", 125686181), entry("inputDecimal", 12.8), entry("inputLong", Long.MAX_VALUE),
                entry("inputLocalDate", testLocalDate),
                entry("inputLocalDateTime", testLocalDateTime),
                entry("inputFile", new FileInputValue(filename, "", fileContentString.getBytes("UTF-8"))));
    }

    @Test
    public void getProcessedInputs_with_multiple_complex_input_should_return_processed_input() throws Exception {
        final List<InputDefinition> inputDefinition = generateComplexInputDefinition();
        when(contractDefinition.getInputs()).thenReturn(inputDefinition);
        final Map<String, Serializable> input = new HashMap<>();
        final Map<String, Serializable> complexInput = generateInputMapWithFile("tempFile");
        final Map<String, Serializable> complexInput2 = generateInputMapWithFile("tempFile2");
        final List<Serializable> multipleComplexInput = new ArrayList<>();
        multipleComplexInput.add((Serializable) complexInput);
        multipleComplexInput.add((Serializable) complexInput2);
        input.put("inputComplex", (Serializable) multipleComplexInput);

        final Map<String, Serializable> processedInput = contractTypeConverter.getProcessedInput(contractDefinition,
                input, maxSizeForTenant);
        assertThat(processedInput).containsKey("inputComplex");
        final List<Serializable> processedMultipleComplexInput = (List<Serializable>) processedInput
                .get("inputComplex");
        assertThat(processedMultipleComplexInput).hasSize(2);
        for (final Serializable processedComplexInput : processedMultipleComplexInput) {
            final Map<String, Serializable> processedComplexInputMap = (Map<String, Serializable>) processedComplexInput;
            assertThat(processedComplexInputMap).containsOnly(entry("inputText", "text"), entry("inputBoolean", true),
                    entry("inputDate", new Date(DATE_01_01_1970_13H_AS_LONG_GMT)),
                    entry("inputInteger", 125686181), entry("inputDecimal", 12.8), entry("inputLong", Long.MAX_VALUE),
                    entry("inputLocalDate", testLocalDate),
                    entry("inputLocalDateTime", testLocalDateTime),
                    entry("inputFile", new FileInputValue(filename, "", fileContentString.getBytes("UTF-8"))));
        }
    }

    @Test
    public void getProcessedInputs_with_simple_input_should_return_processed_input_with_null() throws Exception {
        final List<InputDefinition> inputDefinition = generateSimpleInputDefinition(true);
        when(contractDefinition.getInputs()).thenReturn(inputDefinition);
        final Map<String, Serializable> input = generateInputMapWithNull();

        final Map<String, Serializable> processedInput = contractTypeConverter.getProcessedInput(contractDefinition,
                input, maxSizeForTenant);

        assertThat(processedInput).containsOnly(entry("inputText", null), entry("inputBoolean", null),
                entry("inputDate", null),
                entry("inputInteger", null), entry("inputDecimal", null), entry("inputFile", null));
    }

    @Test
    public void getProcessedInputs_with_simple_input_should_return_processed_input_with_empty_map() throws Exception {
        final List<InputDefinition> inputDefinition = generateSimpleInputDefinition(true);
        when(contractDefinition.getInputs()).thenReturn(inputDefinition);
        final Map<String, Serializable> input = generateInputMapWithEmptyFileInput();

        final Map<String, Serializable> processedInput = contractTypeConverter.getProcessedInput(contractDefinition,
                input, maxSizeForTenant);

        assertThat(processedInput).containsOnly(entry("inputText", null), entry("inputBoolean", null),
                entry("inputDate", null),
                entry("inputInteger", null), entry("inputDecimal", null),
                entry("inputFile", new HashMap<String, Serializable>()), entry("inputLong", null));
    }

    @Test
    public void getProcessedInputs_with_multiple_complex_input_should_return_processed_input_with_null()
            throws Exception {
        final List<InputDefinition> inputDefinition = generateComplexInputDefinition();
        when(contractDefinition.getInputs()).thenReturn(inputDefinition);
        final Map<String, Serializable> input = new HashMap<>();
        final Map<String, Serializable> complexInput = generateInputMapWithFile("tempFile");
        final Map<String, Serializable> complexInput2 = generateInputMapWithNull();
        final Map<String, Serializable> complexInput3 = null;
        final List<Serializable> multipleComplexInput = new ArrayList<>();
        multipleComplexInput.add((Serializable) complexInput);
        multipleComplexInput.add((Serializable) complexInput2);
        multipleComplexInput.add((Serializable) complexInput3);
        input.put("inputComplex", (Serializable) multipleComplexInput);

        final Map<String, Serializable> processedInput = contractTypeConverter.getProcessedInput(contractDefinition,
                input, maxSizeForTenant);
        assertThat(processedInput).containsKey("inputComplex");
        final List<Serializable> processedMultipleComplexInput = (List<Serializable>) processedInput
                .get("inputComplex");
        assertThat(processedMultipleComplexInput).hasSize(3);
        final Serializable processedComplexInput1 = processedMultipleComplexInput.get(0);
        final Map<String, Serializable> processedComplexInputMap1 = (Map<String, Serializable>) processedComplexInput1;
        assertThat(processedComplexInputMap1).containsOnly(entry("inputText", "text"), entry("inputBoolean", true),
                entry("inputDate", new Date(DATE_01_01_1970_13H_AS_LONG_GMT)),
                entry("inputInteger", 125686181), entry("inputDecimal", 12.8), entry("inputLong", Long.MAX_VALUE),
                entry("inputLocalDate", testLocalDate),
                entry("inputLocalDateTime", testLocalDateTime),
                entry("inputFile", new FileInputValue(filename, "", fileContentString.getBytes("UTF-8"))));
        final Serializable processedComplexInput2 = processedMultipleComplexInput.get(1);
        final Map<String, Serializable> processedComplexInputMap2 = (Map<String, Serializable>) processedComplexInput2;
        assertThat(processedComplexInputMap2).containsOnly(entry("inputText", null), entry("inputBoolean", null),
                entry("inputDate", null),
                entry("inputInteger", null), entry("inputDecimal", null), entry("inputFile", null));
        assertThat(processedMultipleComplexInput.get(2)).isNull();
    }

    @Test
    public void should_not_delete_temporary_files_when_processing_input() throws Exception {
        final List<InputDefinition> inputDefinition = generateSimpleInputDefinition(true);
        when(contractDefinition.getInputs()).thenReturn(inputDefinition);
        final String tempFilePath = "tempFile";
        final File tempFile = generateTempFile();
        doReturn(tempFile).when(bonitaHomeFolderAccessor).getTempFile(tempFilePath);
        final Map<String, Serializable> input = generateInputMap(tempFilePath);

        contractTypeConverter.getProcessedInput(contractDefinition, input, maxSizeForTenant);

        //files should not have been deleted
        verify(contractTypeConverter, times(0)).deleteFile(any(File.class));
    }

    @Test
    public void should_delete_temporary_files_of_contract_input() throws Exception {
        final List<InputDefinition> inputDefinition = generateSimpleInputDefinition(true);
        when(contractDefinition.getInputs()).thenReturn(inputDefinition);
        final String tempFilePath = "tempFile";
        final File tempFile = generateTempFile();
        doReturn(tempFile).when(bonitaHomeFolderAccessor).getTempFile(tempFilePath);
        final Map<String, Serializable> input = generateInputMap(tempFilePath);

        contractTypeConverter.deleteTemporaryFiles(input);

        verify(contractTypeConverter).deleteFile(tempFile);
    }

    @Test
    public void getAdaptedContractDefinition_should_return_a_converter_contract() throws IOException {
        //given
        final ContractDefinitionImpl processContract = new ContractDefinitionImpl();
        final List<InputDefinition> inputDefinitions = new ArrayList<>();
        inputDefinitions.add(
                new InputDefinitionImpl(InputDefinition.FILE_INPUT_FILENAME, Type.TEXT, "Name of the file", false));
        inputDefinitions.add(new InputDefinitionImpl(InputDefinition.FILE_INPUT_CONTENT, Type.BYTE_ARRAY,
                "Content of the file", false));
        processContract.addInput(
                new InputDefinitionImpl("inputFile", "this is a input file", false, Type.FILE, inputDefinitions));

        //when
        final ContractDefinition adaptedContractDefinition = contractTypeConverter
                .getAdaptedContractDefinition(processContract);

        //assert
        final InputDefinition tempPathFileInputDefinition = adaptedContractDefinition.getInputs().get(0).getInputs()
                .get(1);
        assertThat(tempPathFileInputDefinition.getType()).isEqualTo(Type.TEXT);
        assertThat(tempPathFileInputDefinition.getName()).isEqualTo(ContractTypeConverter.FILE_TEMP_PATH);
        assertThat(tempPathFileInputDefinition.getDescription()).isEqualTo(ContractTypeConverter.TEMP_PATH_DESCRIPTION);
    }

    @Test
    public void getAdaptedContractDefinition_should_return_null_on_null_contract() {
        //when
        final ContractDefinition adaptedContractDefinition = contractTypeConverter.getAdaptedContractDefinition(null);

        //assert
        assertThat(adaptedContractDefinition).isNull();
    }

    @Test
    public void should_be_able_to_convert_Integer_to_Date() throws Exception {

        Object conversionResult = contractTypeConverter.convertToType(Type.DATE, 86400000);
        assertThat(conversionResult).isNotNull();
        assertThat(conversionResult).isInstanceOf(Date.class);
        assertThat(((Date) conversionResult).getTime()).isEqualTo(86400000L);
    }

    private Map<String, Serializable> generateInputMapWithFile(final String tempFilePath) throws IOException {
        final File tempFile = generateTempFile();
        doReturn(tempFile).when(bonitaHomeFolderAccessor).getTempFile(tempFilePath);
        return generateInputMap(tempFilePath);
    }

    private Map<String, Serializable> generateInputMap(final String tempFilePath) {
        final Map<String, Serializable> inputMap = new HashMap<>();
        inputMap.put("inputText", "text");
        inputMap.put("inputBoolean", "true");
        inputMap.put("inputDate", DATE_01_01_1970_13H_AS_STRING_GMT);
        inputMap.put("inputInteger", "125686181");
        inputMap.put("inputDecimal", "12.8");
        inputMap.put("inputLong", "9223372036854775807");
        inputMap.put("inputLocalDate", testLocalDate.toString());
        inputMap.put("inputLocalDateTime", testLocalDateTime.toString());
        final Map<String, Serializable> fileMap = new HashMap<>();
        fileMap.put(InputDefinition.FILE_INPUT_FILENAME, filename);
        fileMap.put(ContractTypeConverter.FILE_TEMP_PATH, tempFilePath);
        fileMap.put(ContractTypeConverter.CONTENT_TYPE, "contentType");
        inputMap.put("inputFile", (Serializable) fileMap);
        return inputMap;
    }

    private Map<String, Serializable> generateInvalidInputMap() {
        final Map<String, Serializable> inputMap = new HashMap<>();
        inputMap.put("inputText", 0);
        inputMap.put("inputBoolean", "hello");
        inputMap.put("inputDate", "0");
        inputMap.put("inputInteger", "hello");
        inputMap.put("inputDecimal", "hello");
        inputMap.put("inputLong", "hello");
        return inputMap;
    }

    private Map<String, Serializable> generateInputMapWithNull() {
        final Map<String, Serializable> inputMap = new HashMap<>();
        inputMap.put("inputText", null);
        inputMap.put("inputBoolean", null);
        inputMap.put("inputDate", null);
        inputMap.put("inputInteger", null);
        inputMap.put("inputDecimal", null);
        inputMap.put("inputFile", null);
        return inputMap;
    }

    private Map<String, Serializable> generateInputMapWithEmptyFileInput() {
        final Map<String, Serializable> inputMap = new HashMap<>();
        inputMap.put("inputText", null);
        inputMap.put("inputBoolean", null);
        inputMap.put("inputDate", null);
        inputMap.put("inputInteger", null);
        inputMap.put("inputDecimal", null);
        inputMap.put("inputLong", null);
        inputMap.put("inputFile", new HashMap<String, Serializable>());
        return inputMap;
    }

    private List<InputDefinition> generateSimpleInputDefinition(final boolean withFile) {
        final List<InputDefinition> inputDefinitions = generateSimpleInputDefinition();
        if (withFile) {
            final InputDefinition fileInputDefinition = mock(InputDefinitionImpl.class);
            when(fileInputDefinition.getType()).thenReturn(Type.FILE);
            when(fileInputDefinition.getName()).thenReturn("inputFile");
            inputDefinitions.add(fileInputDefinition);
        }
        return inputDefinitions;
    }

    private List<InputDefinition> generateSimpleInputDefinition() {
        final List<InputDefinition> inputDefinitions = new ArrayList<>();
        final InputDefinition textInputDefinition = mock(InputDefinitionImpl.class);
        when(textInputDefinition.getType()).thenReturn(Type.TEXT);
        when(textInputDefinition.getName()).thenReturn("inputText");
        inputDefinitions.add(textInputDefinition);
        final InputDefinition booleanInputDefinition = mock(InputDefinitionImpl.class);
        when(booleanInputDefinition.getType()).thenReturn(Type.BOOLEAN);
        when(booleanInputDefinition.getName()).thenReturn("inputBoolean");
        inputDefinitions.add(booleanInputDefinition);
        final InputDefinition dateInputDefinition = mock(InputDefinitionImpl.class);
        when(dateInputDefinition.getType()).thenReturn(Type.DATE);
        when(dateInputDefinition.getName()).thenReturn("inputDate");
        inputDefinitions.add(dateInputDefinition);
        final InputDefinition integerInputDefinition = mock(InputDefinitionImpl.class);
        when(integerInputDefinition.getType()).thenReturn(Type.INTEGER);
        when(integerInputDefinition.getName()).thenReturn("inputInteger");
        inputDefinitions.add(integerInputDefinition);
        final InputDefinition decimalInputDefinition = mock(InputDefinitionImpl.class);
        when(decimalInputDefinition.getType()).thenReturn(Type.DECIMAL);
        when(decimalInputDefinition.getName()).thenReturn("inputDecimal");
        inputDefinitions.add(decimalInputDefinition);
        final InputDefinition longInputDefinition = mock(InputDefinitionImpl.class);
        when(longInputDefinition.getType()).thenReturn(Type.LONG);
        when(longInputDefinition.getName()).thenReturn("inputLong");
        inputDefinitions.add(longInputDefinition);
        final InputDefinition localDateInputDefinition = mock(InputDefinitionImpl.class);
        when(localDateInputDefinition.getType()).thenReturn(Type.LOCALDATE);
        when(localDateInputDefinition.getName()).thenReturn("inputLocalDate");
        inputDefinitions.add(localDateInputDefinition);
        final InputDefinition localDateTimeInputDefinition = mock(InputDefinitionImpl.class);
        when(localDateTimeInputDefinition.getType()).thenReturn(Type.LOCALDATETIME);
        when(localDateTimeInputDefinition.getName()).thenReturn("inputLocalDateTime");
        inputDefinitions.add(localDateTimeInputDefinition);
        return inputDefinitions;
    }

    private List<InputDefinition> generateComplexInputDefinition() {
        final List<InputDefinition> inputDefinitions = new ArrayList<>();
        final InputDefinition inputDefinition = mock(InputDefinition.class);
        when(inputDefinition.getName()).thenReturn("inputComplex");
        when(inputDefinition.hasChildren()).thenReturn(true);
        final List<InputDefinition> childInputDefinitions = generateSimpleInputDefinition(true);
        when(inputDefinition.getInputs()).thenReturn(childInputDefinitions);
        inputDefinitions.add(inputDefinition);
        return inputDefinitions;
    }

    @Test
    public void convertToType_should_correctly_parse_incoming_objects() {
        Object date = new ContractTypeConverter(ISO_8601_DATE_PATTERNS).convertToType(Type.DATE, "2017-12-25");
        assertThat(date.getClass()).isEqualTo(Date.class);
        Object validLocalDate = new ContractTypeConverter(ISO_8601_DATE_PATTERNS).convertToType(Type.LOCALDATE,
                "2017-12-25");
        assertThat(validLocalDate).isNotNull().isEqualTo(LocalDate.of(2017, 12, 25));
        Object validLocalDateTime = new ContractTypeConverter(ISO_8601_DATE_PATTERNS).convertToType(Type.LOCALDATETIME,
                "2017-12-17T21:42:57");
        assertThat(validLocalDateTime).isEqualTo(LocalDateTime.of(2017, 12, 17, 21, 42, 57));
        final String invalidDateTime = "2017-22-37T21:42:57";
        Object invalidDateFormat = new ContractTypeConverter(ISO_8601_DATE_PATTERNS).convertToType(Type.LOCALDATETIME,
                invalidDateTime);
        assertThat(invalidDateFormat).isEqualTo(invalidDateTime);
    }

    @Test
    public void convertToType_with_null_localDate_value_should_silently_return_null() throws Exception {
        // when:
        final Object nullLocalDate = new ContractTypeConverter(ISO_8601_DATE_PATTERNS).convertToType(Type.LOCALDATE,
                null);

        // then:
        assertThat(nullLocalDate).isNull();
    }

    @Test
    public void convertToType_with_null_localDateTime_value_should_silently_return_null() throws Exception {
        // when:
        final Object nullLocalDateTime = new ContractTypeConverter(ISO_8601_DATE_PATTERNS)
                .convertToType(Type.LOCALDATETIME, null);

        // then:
        assertThat(nullLocalDateTime).isNull();
    }

    @Test
    public void convertToType_with_null_String_value_should_silently_return_null() throws Exception {
        // when:
        final Object nullString = new ContractTypeConverter(ISO_8601_DATE_PATTERNS).convertToType(Type.TEXT, null);

        // then:
        assertThat(nullString).isNull();
    }

    @Test
    public void convertToType_should_ignore_extra_characters_in_String_for_LocalDates() {
        ContractTypeConverter converter = new ContractTypeConverter(ISO_8601_DATE_PATTERNS);

        final Object localDateString = converter.convertToType(Type.LOCALDATE, "1987-12-12T10:00:00");

        assertThat(localDateString).isEqualTo(LocalDate.of(1987, 12, 12));

    }

    @Test
    public void convertToType_should_ignore_extra_characters_in_String_for_LocalDatesTime() {
        ContractTypeConverter converter = new ContractTypeConverter(ISO_8601_DATE_PATTERNS);
        Map<String, Object> conversionMap = new HashMap<>();
        conversionMap.put("1987-12-12T10:00Z", LocalDateTime.of(1987, 12, 12, 10, 0, 0, 0));
        conversionMap.put("1987-12-12T10:00:00Z", LocalDateTime.of(1987, 12, 12, 10, 0, 0, 0));
        conversionMap.put("2017-03-09T12:20:20.258Z", LocalDateTime.of(2017, 3, 9, 12, 20, 20, 258000000));
        conversionMap.put("2017-03-09T12:20:20.258854753Z", LocalDateTime.of(2017, 3, 9, 12, 20, 20, 258854753));
        conversionMap.put("2017-03-09T12:20:20+01:00", LocalDateTime.of(2017, 3, 9, 12, 20, 20));
        conversionMap.put("2017-03-09T12:20:20.258+01:00", LocalDateTime.of(2017, 3, 9, 12, 20, 20, 258000000));
        conversionMap.put("2017-03-09T12:20:20.258854753+01:00", LocalDateTime.of(2017, 3, 9, 12, 20, 20, 258854753));
        conversionMap.put("2017-03-09T12:20:20+01:00[Europe/Berlin]", LocalDateTime.of(2017, 3, 9, 12, 20, 20));
        conversionMap.put("2017-03-09T12:20:20.258+01:00[Europe/Berlin]",
                LocalDateTime.of(2017, 3, 9, 12, 20, 20, 258000000));
        conversionMap.put("2017-03-09T12:20:20.258854753+01:00[Europe/Berlin]",
                LocalDateTime.of(2017, 3, 9, 12, 20, 20, 258854753));
        conversionMap.put("2017-03-09T12:20:20.258854753-01:00", LocalDateTime.of(2017, 3, 9, 12, 20, 20, 258854753));

        for (Map.Entry<String, Object> entry : conversionMap.entrySet()) {
            assertThat(converter.convertToType(Type.LOCALDATETIME, entry.getKey())).isEqualTo(entry.getValue());
        }
    }

    @Test
    public void convertToType_should_convert_to_OffsetDateTime_every_input_format_string() {
        ContractTypeConverter converter = new ContractTypeConverter(ISO_8601_DATE_PATTERNS);
        Map<String, Object> conversionMap = new HashMap<>();
        conversionMap.put("1987-12-12T10:00:00Z", OffsetDateTime.of(1987, 12, 12, 10, 0, 0, 0, ZoneOffset.UTC));
        conversionMap.put("2017-03-09T12:20:20.258Z",
                OffsetDateTime.of(2017, 3, 9, 12, 20, 20, 258000000, ZoneOffset.UTC));
        conversionMap.put("2017-03-09T12:20:20.258854753Z",
                OffsetDateTime.of(2017, 3, 9, 12, 20, 20, 258854753, ZoneOffset.UTC));
        conversionMap.put("2017-03-09T12:20:20+01:00",
                OffsetDateTime.of(2017, 3, 9, 12, 20, 20, 0, ZoneOffset.ofHours(1)));
        conversionMap.put("2017-03-09T12:20:20.258+01:00",
                OffsetDateTime.of(2017, 3, 9, 12, 20, 20, 258000000, ZoneOffset.ofHours(1)));
        conversionMap.put("2017-03-09T12:20:20.258854753+01:00",
                OffsetDateTime.of(2017, 3, 9, 12, 20, 20, 258854753, ZoneOffset.ofHours(1)));
        conversionMap.put("2017-03-09T12:20:20+01:00[Europe/Berlin]",
                OffsetDateTime.of(2017, 3, 9, 12, 20, 20, 0, ZoneOffset.ofHours(1)));
        conversionMap.put("2017-03-09T12:20:20.258+01:00[Europe/Berlin]",
                OffsetDateTime.of(2017, 3, 9, 12, 20, 20, 258000000, ZoneOffset.ofHours(1)));
        conversionMap.put("2017-03-09T12:20:20.258854753+01:00[Europe/Berlin]",
                OffsetDateTime.of(2017, 3, 9, 12, 20, 20, 258854753, ZoneOffset.ofHours(1)));
        conversionMap.put("2017-03-09T12:20:20.258854753-01:00",
                OffsetDateTime.of(2017, 3, 9, 12, 20, 20, 258854753, ZoneOffset.ofHours(-1)));
        for (Map.Entry<String, Object> entry : conversionMap.entrySet()) {
            assertThat(converter.convertToType(Type.OFFSETDATETIME, entry.getKey())).isEqualTo(entry.getValue());
        }
    }

    @Test
    public void convertToType_should_not_convert_into_an_OffsetDate_a_String_that_is_too_short() {
        ContractTypeConverter converter = new ContractTypeConverter(ISO_8601_DATE_PATTERNS);

        final Object unparseableString = converter.convertToType(Type.OFFSETDATETIME, "1987-12");

        assertThat(unparseableString).isEqualTo("1987-12");
    }

    @Test
    public void convertToType_should_not_convert_into_a_LocalDate_a_String_that_is_too_short() {
        ContractTypeConverter converter = spy(new ContractTypeConverter(ISO_8601_DATE_PATTERNS));

        systemOutRule.clearLog();
        final Object localDateString = converter.convertToType(Type.LOCALDATE, "1987-12");

        assertThat(localDateString).isEqualTo("1987-12");
        assertThat(systemOutRule.getLog()).contains("unable to parse '1987-12' to type " + LocalDate.class.getName());
    }

    @Test
    public void convertToType_should_not_convert_into_a_LocalDateTime_a_String_that_is_too_short() {

        ContractTypeConverter converter = new ContractTypeConverter(ISO_8601_DATE_PATTERNS);

        final String year = "1987";
        final Object localDateTimeString1 = converter.convertToType(Type.LOCALDATETIME, year);
        final String yearAndHour = "1987-10-11T19";
        final Object localDateTimeString2 = converter.convertToType(Type.LOCALDATETIME, yearAndHour);
        final String invalidDateAndTime = "1987-10-11T19:32:4";
        final Object localDateTimeString3 = converter.convertToType(Type.LOCALDATETIME, invalidDateAndTime);

        assertThat(localDateTimeString1).isEqualTo(year);
        assertThat(localDateTimeString2).isEqualTo(yearAndHour);
        assertThat(localDateTimeString3).isEqualTo(invalidDateAndTime);
    }

    @Test
    public void convertToType_should_convert_a_String_without_seconds_into_a_LocalDateTime_objects() {
        ContractTypeConverter converter = new ContractTypeConverter(ISO_8601_DATE_PATTERNS);

        final Object localDateTimeString = converter.convertToType(Type.LOCALDATETIME, "1987-10-11T19:32");

        assertThat(localDateTimeString).isEqualTo(LocalDateTime.of(1987, 10, 11, 19, 32));
    }

    @Test
    public void convertToType_should_convert_a_LongString_to_a_LocalDate() {
        ContractTypeConverter converter = new ContractTypeConverter(ISO_8601_DATE_PATTERNS);

        final Object localDate = converter.convertToType(Type.LOCALDATE, "1987-10-11T19:32");

        assertThat(localDate).isEqualTo(LocalDate.of(1987, 10, 11));
    }

    @Test
    public void convertToType_should_log_if_value_is_truncated() {
        ContractTypeConverter converter = spy(new ContractTypeConverter(ISO_8601_DATE_PATTERNS));

        systemOutRule.clearLog();
        converter.convertToType(Type.LOCALDATE, "1987-10-11T19:32");

        assertThat(systemOutRule.getLog())
                .contains("The string 1987-10-11T19:32 contains information that will be dropped " +
                        "to convert it to a LocalDate (most likely time and timezone information which are not relevant).");

        converter.convertToType(Type.LOCALDATETIME, "1987-10-11T19:32Z");

        assertThat(systemOutRule.getLog())
                .contains("The string 1987-10-11T19:32Z contains information that will be dropped to " +
                        "convert it to a LocalDateTime (most likely time and timezone information which are not relevant).");
    }
}
