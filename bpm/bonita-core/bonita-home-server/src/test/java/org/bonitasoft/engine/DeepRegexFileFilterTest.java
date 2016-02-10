/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Test;

public class DeepRegexFileFilterTest {

    @Test
    public void acceptShouldWorkForMatchingPattern() {
        final String pattern = "folder/sub/.*\\.txt";
        final String parentPatternPathname = "/media/drive/some_folder";
        assertThat(new DeepRegexFileFilter(new File(parentPatternPathname), pattern).accept(new File("/media/drive/some_folder/folder/sub/matchingFile.txt")))
                .isTrue();
    }

    @Test
    public void acceptShouldWorkForMatchingPatternOnFolderWithTrailingSlash() {
        final String pattern = "folder/sub/.*\\.txt";
        final String parentPatternPathname = "/home/some_folder/";
        assertThat(new DeepRegexFileFilter(new File(parentPatternPathname), pattern).accept(new File("/home/some_folder/folder/sub/matchingFile.txt")))
                .isTrue();
    }

    @Test
    public void acceptShouldRejectNonMatchingFile() {
        final String pattern = "folder/sub/.*\\.txt";
        final String parentPatternPathname = "/home/some_folder/";
        assertThat(new DeepRegexFileFilter(new File(parentPatternPathname), pattern).accept(new File("/home/some_folder/folder/sub/someReport.pdf")))
                .isFalse();
    }

    @Test
    public void acceptShouldMatchDeepSubFolders() {
        final String pattern = "folder/.*\\.txt";
        final String parentPatternPathname = "/home";
        assertThat(new DeepRegexFileFilter(new File(parentPatternPathname), pattern).accept(new File("/home/folder/sub/sub2/fileHiddenInDeepFolder.txt")))
                .isTrue();
    }

    @Test
    public void acceptShouldNotMatchSlashBeginingPatterns() {
        final String pattern = "/folder/.*\\.txt";
        final String parentPatternPathname = "/home";
        assertThat(new DeepRegexFileFilter(new File(parentPatternPathname), pattern).accept(new File("/home/folder/sub/sub2/fileHiddenInDeepFolder.txt")))
                .isFalse();
    }

}
