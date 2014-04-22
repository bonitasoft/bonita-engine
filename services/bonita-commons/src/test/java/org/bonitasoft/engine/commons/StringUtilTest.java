package org.bonitasoft.engine.commons;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class StringUtilTest {

    @Test(expected = IllegalArgumentException.class)
    public void firstCharToUpperCaseUpdateTheFirstLetterOfANullWordThrowsAnException() throws Exception {
        StringUtil.firstCharToUpperCase(null);
    }

    @Test
    public void firstCharToUpperCaseDoNotUpdateAnEmptyWord() throws Exception {
        final String actual = StringUtil.firstCharToUpperCase("");
        assertThat(actual).isEqualTo("");
    }

    @Test
    public void firstCharToUpperCaseUpdateAnWordWithASingleCharacter() throws Exception {
        final String actual = StringUtil.firstCharToUpperCase("a");
        assertThat(actual).isEqualTo("A");
    }

    @Test
    public void firstCharToUpperCaseUpdateTheFirstLetterOfTheWord() throws Exception {
        final String actual = StringUtil.firstCharToUpperCase("namE");
        assertThat(actual).isEqualTo("NamE");
    }

    @Test
    public void firstCharToUpperCaseUpdateTheFirstLetterOfTheWordEvenIfItIsAlreadyInUpperCase() throws Exception {
        final String actual = StringUtil.firstCharToUpperCase("Name");
        assertThat(actual).isEqualTo("Name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void firstCharToLowerCaseUpdateTheFirstLetterOfANullWordThrowsAnException() throws Exception {
        StringUtil.firstCharToLowerCase(null);
    }

    @Test
    public void firstCharToLowerCaseDoNotUpdateAnEmptyWord() throws Exception {
        final String actual = StringUtil.firstCharToLowerCase("");
        assertThat(actual).isEqualTo("");
    }

    @Test
    public void firstCharToLowerCaseUpdateAnWordWithASingleCharacter() throws Exception {
        final String actual = StringUtil.firstCharToLowerCase("A");
        assertThat(actual).isEqualTo("a");
    }

    @Test
    public void firstCharToLowerCaseUpdateTheFirstLetterOfTheWord() throws Exception {
        final String actual = StringUtil.firstCharToLowerCase("namE");
        assertThat(actual).isEqualTo("namE");
    }

    @Test
    public void firstCharToLowerCaseUpdateTheFirstLetterOfTheWordEvenIfItIsAlreadyInUpperCase() throws Exception {
        final String actual = StringUtil.firstCharToLowerCase("Name");
        assertThat(actual).isEqualTo("name");
    }

}
