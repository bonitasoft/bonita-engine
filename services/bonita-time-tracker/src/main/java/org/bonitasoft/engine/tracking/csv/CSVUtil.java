package org.bonitasoft.engine.tracking.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;

public class CSVUtil {

    public static List<List<String>> readCSV(final boolean excludeHeader, final File csvFile, final String csvSeparator) throws FileNotFoundException {
        final List<List<String>> array = new ArrayList<List<String>>();
        final InputStream inputStream = new FileInputStream(csvFile);
        final Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            final List<String> lineElements = new ArrayList<String>();
            lineElements.addAll(Arrays.asList(line.split(csvSeparator)));
            array.add(lineElements);
        }
        scanner.close();
        if (excludeHeader) {
            array.remove(0);
        }
        return array;
    }

    public static void writeCSV(final File file, final List<List<String>> array, final String csvSeparator) throws IOException {
        final FileWriter writer = new FileWriter(file);
        for (final List<String> row : array) {
            boolean first = true;
            for (final String value : row) {
                if (first) {
                    writer.append(value);
                    first = false;
                } else {
                    writer.append(csvSeparator);
                    writer.append(value);
                }
            }
            writer.append("\n");
            writer.flush();
        }
        writer.close();
    }

    public static String getFileTimestamp(final long time) {
        final StringBuilder fileSuffix = new StringBuilder();
        final GregorianCalendar c = new GregorianCalendar();
        c.setTimeInMillis(time);

        fileSuffix.append(getIntOnTwoNumbers(c.get(Calendar.YEAR)));
        fileSuffix.append("_");
        fileSuffix.append(getIntOnTwoNumbers(c.get(Calendar.MONTH) + 1));
        fileSuffix.append("_");
        fileSuffix.append(getIntOnTwoNumbers(c.get(Calendar.DAY_OF_MONTH)));
        fileSuffix.append("_");
        fileSuffix.append(getIntOnTwoNumbers(c.get(Calendar.HOUR_OF_DAY)));
        fileSuffix.append("h");
        fileSuffix.append(getIntOnTwoNumbers(c.get(Calendar.MINUTE)));
        fileSuffix.append("m");
        fileSuffix.append(getIntOnTwoNumbers(c.get(Calendar.SECOND)));
        fileSuffix.append("s");
        return fileSuffix.toString();
    }

    public static String getIntOnTwoNumbers(final int i) {
        if (i < 10) {
            return "0" + i;
        }
        return Integer.toString(i);
    }

}
