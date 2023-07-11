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

import static org.bonitasoft.engine.bpm.contract.InputDefinition.FILE_INPUT_ID;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.bpm.contract.*;
import org.bonitasoft.engine.bpm.contract.impl.ContractDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.InputDefinitionImpl;
import org.bonitasoft.engine.bpm.document.DocumentException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.io.FileContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Anthony Birembaut
 */

public class ContractTypeConverter {

    /**
     * Logger
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(ContractTypeConverter.class.getName());

    public static final String[] ISO_8601_DATE_PATTERNS = new String[] { "yyyy-MM-dd", "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" };

    static final String CONTENT_TYPE = "contentType";
    static final String FILE_TEMP_PATH = "tempPath";
    static final String TEMP_PATH_DESCRIPTION = "file name in the temporary upload directory";

    protected BonitaHomeFolderAccessor bonitaHomeFolderAccessor = new BonitaHomeFolderAccessor();

    private final ConvertUtilsBean convertUtilsBean;

    private long maxSizeForTenant;

    public ContractTypeConverter(final String[] datePatterns) {
        convertUtilsBean = new ConvertUtilsBean();
        convertUtilsBean.register(true, false, 0);
        final DateConverter dateConverter = new DateConverter();
        dateConverter.setPatterns(datePatterns);
        dateConverter.setTimeZone(TimeZone.getTimeZone("GMT"));
        convertUtilsBean.register(dateConverter, Date.class);
    }

    Object convertToType(final Type type, final Serializable parameterValue) {
        final Class<? extends Serializable> clazz = getClassFromType(type);
        Serializable preprocessedParameterValue = preprocessInputs(type, parameterValue);
        return convertToType(clazz, preprocessedParameterValue);
    }

    private Serializable preprocessInputs(final Type type, final Serializable parameterValue) {
        //Also support Integer as DATE contract input (as deserialization is handled by jackson it can be mapped to an integer instead of a long when it is a small number)
        if (Type.DATE.equals(type) && parameterValue instanceof Integer) {
            return Long.valueOf((Integer) parameterValue);
        }
        return parameterValue;
    }

    public Map<String, Serializable> getProcessedInput(final ContractDefinition processContract,
            final Map<String, Serializable> inputs,
            final long maxSizeForTenant) throws FileNotFoundException {
        this.maxSizeForTenant = maxSizeForTenant;
        final Map<String, Serializable> processedInputs = new HashMap<>();
        final Map<String, Serializable> contractDefinitionMap = processContract == null ? Collections.emptyMap()
                : createContractInputMap(processContract.getInputs());
        if (inputs != null) {
            for (final Entry<String, Serializable> inputEntry : inputs.entrySet()) {
                processedInputs.put(inputEntry.getKey(),
                        convertInputToExpectedType(inputEntry.getValue(),
                                contractDefinitionMap.get(inputEntry.getKey())));
            }
        }
        return processedInputs;
    }

    public void deleteTemporaryFiles(Map<String, Serializable> inputs) {
        if (inputs != null) {
            deleteTemporaryFilesInternal((Serializable) inputs);
        }
    }

    private void deleteTemporaryFilesInternal(Serializable inputValue) {
        if (inputValue == null) {
            return;
        }
        if (inputValue instanceof List) {
            for (Object element : ((List) inputValue)) {
                deleteTemporaryFilesInternal(((Serializable) element));
            }
        } else if (inputValue instanceof Map) {
            for (Map.Entry<String, Serializable> element : ((Map<String, Serializable>) inputValue).entrySet()) {
                if (element.getKey().equals(FILE_TEMP_PATH) && element.getValue() != null) {
                    String path = (String) element.getValue();
                    bonitaHomeFolderAccessor.removeUploadedTempContent(path);
                } else {
                    deleteTemporaryFilesInternal(element.getValue());
                }
            }
        }

    }

    private Serializable convertInputToExpectedType(final Serializable inputValue, final Serializable inputDefinition)
            throws FileNotFoundException {
        if (inputValue == null) {
            return null;
        } else if (inputValue instanceof List) {
            return convertMultipleInputToExpectedType(inputValue, inputDefinition);
        } else {
            return convertSingleInputToExpectedType(inputValue, inputDefinition);
        }
    }

    private Serializable convertMultipleInputToExpectedType(final Serializable inputValue,
            final Serializable inputDefinition)
            throws FileNotFoundException {
        @SuppressWarnings("unchecked")
        final List<Serializable> listOfValues = (List<Serializable>) inputValue;
        final List<Serializable> convertedListOfValues = new ArrayList<>();
        for (final Serializable value : listOfValues) {
            Serializable convertedValue = null;
            if (value != null) {
                convertedValue = convertSingleInputToExpectedType(value, inputDefinition);
            }
            convertedListOfValues.add(convertedValue);
        }
        return (Serializable) convertedListOfValues;
    }

    private Serializable convertSingleInputToExpectedType(final Serializable inputValue,
            final Serializable inputDefinition)
            throws FileNotFoundException {
        if (inputDefinition == null) {
            return inputValue;
        } else if (inputDefinition instanceof Map) {
            @SuppressWarnings("unchecked")
            final Map<String, Serializable> mapOfInputDefinition = (Map<String, Serializable>) inputDefinition;
            return convertComplexInputToExpectedType(inputValue, mapOfInputDefinition);
        } else {
            final InputDefinition simpleInputDefinition = (InputDefinition) inputDefinition;
            if (Type.FILE.equals(simpleInputDefinition.getType())) {
                return convertFileInputToExpectedType(inputValue);
            } else {
                return (Serializable) convertToType(simpleInputDefinition.getType(), inputValue);
            }
        }
    }

    private Serializable convertComplexInputToExpectedType(final Serializable inputValue,
            final Map<String, Serializable> mapOfInputDefinition) throws FileNotFoundException {
        if (inputValue instanceof Map) {
            @SuppressWarnings("unchecked")
            final Map<String, Serializable> mapOfValues = (Map<String, Serializable>) inputValue;
            final Map<String, Serializable> convertedMapOfValues = new HashMap<>();
            for (final Entry<String, Serializable> valueEntry : mapOfValues.entrySet()) {
                final Serializable childInputDefinition = mapOfInputDefinition.get(valueEntry.getKey());
                final Serializable convertedValue = convertInputToExpectedType(valueEntry.getValue(),
                        childInputDefinition);
                convertedMapOfValues.put(valueEntry.getKey(), convertedValue);
            }
            return (Serializable) convertedMapOfValues;
        } else {
            return inputValue;
        }
    }

    private Serializable convertFileInputToExpectedType(final Serializable inputValue) throws FileNotFoundException {
        if (inputValue instanceof Map) {
            @SuppressWarnings("unchecked")
            final Map<String, Serializable> mapOfValues = (Map<String, Serializable>) inputValue;
            if (mapOfValues.containsKey(InputDefinition.FILE_INPUT_FILENAME)
                    && mapOfValues.containsKey(FILE_TEMP_PATH)) {
                final String filename = (String) mapOfValues.get(InputDefinition.FILE_INPUT_FILENAME);
                return new FileInputValue(
                        DocumentUtil.sanitizeFilename(filename),
                        (String) mapOfValues.get(CONTENT_TYPE),
                        retrieveFileAndGetContent((String) mapOfValues.get(FILE_TEMP_PATH)),
                        (String) mapOfValues.get(FILE_INPUT_ID));
            }
        }
        return inputValue;
    }

    private byte[] retrieveFileAndGetContent(final String fileTempPath) throws FileNotFoundException {
        byte[] fileContent = null;
        if (fileTempPath != null) {
            try {
                final FileContent sourceFile = bonitaHomeFolderAccessor.retrieveUploadedTempContent(fileTempPath);
                try (InputStream inputStream = sourceFile.getInputStream()) {
                    fileContent = getFileContent(inputStream, sourceFile.getSize());
                }
            } catch (final BonitaException e) {
                throw new FileNotFoundException("Cannot find " + fileTempPath + " temp file.");
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
        return fileContent;
    }

    private byte[] getFileContent(final InputStream inputStream, long size)
            throws DocumentException, IOException {
        byte[] fileContent;
        if (size > maxSizeForTenant * 1048576 || size > Integer.MAX_VALUE) { // more than 2 GB
            final String errorMessage = "This document is exceeded configured max size (" + maxSizeForTenant
                    + "Mb) or 2GB";
            throw new DocumentException(errorMessage);
        }
        return IOUtils.toByteArray(inputStream);
    }

    private Map<String, Serializable> createContractInputMap(final List<InputDefinition> inputDefinitions) {
        final Map<String, Serializable> contractDefinitionMap = new HashMap<>();
        for (final InputDefinition inputDefinition : inputDefinitions) {
            if (inputDefinition.hasChildren() && !Type.FILE.equals(inputDefinition.getType())) {
                contractDefinitionMap.put(inputDefinition.getName(),
                        (Serializable) createContractInputMap(inputDefinition.getInputs()));
            } else {
                contractDefinitionMap.put(inputDefinition.getName(), inputDefinition);
            }
        }
        return contractDefinitionMap;
    }

    public ContractDefinition getAdaptedContractDefinition(final ContractDefinition contract) {
        if (contract == null) {
            return null;
        }
        final List<ConstraintDefinition> constraints = contract.getConstraints();
        final List<InputDefinition> inputDefinitions = adaptContractInputList(contract.getInputs());
        return getContractDefinition(constraints, inputDefinitions);
    }

    private List<InputDefinition> adaptContractInputList(final List<InputDefinition> inputDefinitions) {
        final List<InputDefinition> contractDefinition = new ArrayList<>();
        for (final InputDefinition inputDefinition : inputDefinitions) {
            List<InputDefinition> childInputDefinitions;
            if (Type.FILE.equals(inputDefinition.getType())) {
                childInputDefinitions = getFileChildInputDefinitions(inputDefinition);
            } else if (inputDefinition.hasChildren()) {
                childInputDefinitions = adaptContractInputList(inputDefinition.getInputs());
            } else {
                childInputDefinitions = new ArrayList<>();
            }
            final InputDefinition newInputDefinition = new InputDefinitionImpl(inputDefinition.getName(),
                    inputDefinition.getDescription(),
                    inputDefinition.isMultiple(), inputDefinition.getType(), childInputDefinitions);
            contractDefinition.add(newInputDefinition);
        }
        return contractDefinition;
    }

    private List<InputDefinition> getFileChildInputDefinitions(final InputDefinition inputDefinition) {
        List<InputDefinition> childInputDefinitions;
        childInputDefinitions = new ArrayList<>();
        for (final InputDefinition childInputDefinition : inputDefinition.getInputs()) {
            if (Type.BYTE_ARRAY.equals(childInputDefinition.getType())) {
                childInputDefinitions.add(new InputDefinitionImpl(FILE_TEMP_PATH, TEMP_PATH_DESCRIPTION, false,
                        Type.TEXT, new ArrayList<>()));
            } else {
                childInputDefinitions.add(childInputDefinition);
            }
        }
        return childInputDefinitions;
    }

    private ContractDefinitionImpl getContractDefinition(final List<ConstraintDefinition> constraints,
            final List<InputDefinition> inputDefinitions) {
        final ContractDefinitionImpl contractDefinition = new ContractDefinitionImpl();
        for (final ConstraintDefinition constraint : constraints) {
            contractDefinition.addConstraint(constraint);
        }

        for (final InputDefinition input : inputDefinitions) {
            contractDefinition.addInput(input);
        }
        return contractDefinition;
    }

    private Object convertToType(final Class<? extends Serializable> clazz, final Serializable parameterValue) {
        if (parameterValue == null) {
            return null;
        }
        String paramValueString = parameterValue.toString();
        try {
            if (clazz == LocalDate.class) {
                //We drop useless info received from the widget ex: 2010-12-04T18:42:10Z, we drop T18:42:10Z to allow conversion
                if (paramValueString.length() > 10) {
                    LOGGER.debug("The string " + paramValueString
                            + " contains information that will be dropped to convert it to a LocalDate (most likely time and timezone information which are not relevant).");
                    paramValueString = paramValueString.substring(0, 10);
                }
                return LocalDate.parse(paramValueString);
            } else if (clazz == LocalDateTime.class) {
                try {
                    return LocalDateTime.parse(paramValueString);
                } catch (DateTimeParseException e) {
                    LOGGER.debug("The string " + paramValueString
                            + " contains information that will be dropped to convert it to a LocalDateTime (most likely time and timezone information which are not relevant).");
                    //We drop the timezone info from the String:
                    return ZonedDateTime.parse(paramValueString).toLocalDateTime();
                }
            } else if (clazz == OffsetDateTime.class) {
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(paramValueString);
                return zonedDateTime.toOffsetDateTime();
            } else {
                return convertUtilsBean.convert(parameterValue, clazz);
            }
        } catch (final ConversionException | DateTimeParseException e) {
            LOGGER.info("unable to parse '" + parameterValue + "' to type " + clazz.getName(), e);
            return parameterValue;
        }
    }

    private Class<? extends Serializable> getClassFromType(final Type type) {
        switch (type) {
            case BOOLEAN:
                return Boolean.class;
            case DATE:
                return Date.class;
            case INTEGER:
                return Integer.class;
            case DECIMAL:
                return Double.class;
            case BYTE_ARRAY:
                return Byte[].class;
            case LONG:
                return Long.class;
            case LOCALDATE:
                return LocalDate.class;
            case LOCALDATETIME:
                return LocalDateTime.class;
            case OFFSETDATETIME:
                return OffsetDateTime.class;
            default:
                return String.class;
        }
    }
}
