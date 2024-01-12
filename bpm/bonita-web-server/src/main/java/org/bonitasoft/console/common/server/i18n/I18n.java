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
package org.bonitasoft.console.common.server.i18n;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n;
import org.bonitasoft.web.toolkit.client.common.texttemplate.Arg;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * @author Paul AMAR, Vincent Elcrin
 */
public class I18n extends AbstractI18n {

    /**
     * Property that can be set to override the default i18n *.po files, or to provide translations for new languages.
     * Points to a folder that contains .po files
     */
    public static final String I18N_CUSTOM_DIR_PROPERTY = "org.bonitasoft.i18n.folder";

    private static File I18N_CUSTOM_DIR = getI18nCustomDirectory();

    private I18n() {
        // Singleton
    }

    public static I18n getInstance() {
        if (I18N_instance == null) {
            I18N_instance = new I18n();
        }
        return (I18n) I18N_instance;
    }

    // For test matters only:
    public static void setInstance(I18n instance) {
        I18N_instance = instance;
    }

    // For test matters only:
    public void refresh() {
        I18N_CUSTOM_DIR = getI18nCustomDirectory();
    }

    private static File getI18nCustomDirectory() {
        String customI18nFolder = System.getProperty(I18N_CUSTOM_DIR_PROPERTY);
        return customI18nFolder != null ? new File(customI18nFolder) : null;
    }

    @Override
    public void loadLocale(final LOCALE locale) {
        Map<String, String> results = loadLocale(getStreams(locale));
        if (I18N_CUSTOM_DIR != null) {
            results.putAll(loadLocale(locale, FileUtils.listDir(I18N_CUSTOM_DIR)));
        }
        setLocale(locale, results);
    }

    private Map<String, String> loadLocale(List<InputStream> streams) {
        TreeMap<String, String> treeMap = new TreeMap<>();
        for (InputStream stream : streams) {
            treeMap.putAll(parsePoResource(stream));
        }
        return treeMap;
    }

    private TreeMap<String, String> loadLocale(final LOCALE locale, List<File> files) {
        TreeMap<String, String> treeMap = new TreeMap<>();
        for (File file : getLocaleFiles(locale, files)) {
            treeMap.putAll(parsePoFile(file));
        }
        return treeMap;
    }

    public List<InputStream> getStreams(LOCALE locale) {
        ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = patternResolver.getResources("classpath*:i18n/" + getLocaleRegexForResource(locale));
            List<InputStream> streams = new ArrayList<>(resources.length);
            for (Resource resource : resources) {
                InputStream stream = resource.getInputStream();
                streams.add(stream);
            }
            return streams;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private Map<String, String> parsePoFile(final File file) {
        return POParser.parse(file);
    }

    private Map<String, String> parsePoResource(final InputStream stream) {
        return POParser.parsePOFromStream(stream);
    }

    private List<File> getLocaleFiles(final LOCALE locale, List<File> files) {
        return FileUtils.getMatchingFiles(makeLocaleRegex(locale), files);
    }

    private String makeLocaleRegex(final LOCALE locale) {
        return "(.*)" + locale.toString().trim() + ".po";
    }

    private String getLocaleRegexForResource(final LOCALE locale) {
        return "*" + locale.toString().trim() + ".po";
    }

    public Map<String, String> getAvailableLocalesFor(String application) {
        final Map<String, String> results = new LinkedHashMap<>();
        final Map<String, String> locales = getLocales();

        // Add English by default since _en.po files have been removed
        results.put("en", "English");
        for (final String locale : locales.keySet()) {
            if (localeExists(locale, application)) {
                results.put(locale, locales.get(locale));
            }
        }

        return results;
    }

    /**
     * @return available locale file for a specified application (i.e application_locale.po)
     */
    private static boolean localeExists(String locale, String application) {
        final String fileName = application + "_" + locale + ".po";
        final File file = new File(I18N_CUSTOM_DIR, fileName);
        return file.exists() || I18n.class.getResource("/i18n/" + fileName) != null;
    }

    @Override
    protected String getText(String string) {
        return string;
        //        throw new RuntimeException("On server side, we absolutely need to pass locale");
    }

    @Override
    protected String getText(String string, Arg... args) {
        return string;
        //        throw new RuntimeException("On server side, we absolutely need to pass locale");
    }
}
