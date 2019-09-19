package uk.gov.pay.api.app.config;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(JUnitParamsRunner.class)
public class StringToListConverterTest {

    private StringToListConverter converter;

    @Before
    public void setUp() {
        converter = new StringToListConverter();
    }

    @Test
    @Parameters
    public void convertsStringInputToListOfStrings(String input, List<String> expectedOutput) {
        assertThat(converter.convert(input), is(expectedOutput));
    }

    public Object[] parametersForConvertsStringInputToListOfStrings() {
        return new Object[]{
                new Object[]{null, Collections.emptyList()},
                new Object[]{"", Collections.emptyList()},
                new Object[]{", ,   ,", Collections.emptyList()},
                new Object[]{"a", List.of("a")},
                new Object[]{"a, b, b", List.of("a", "b", "b")},
                new Object[]{"a, , b", List.of("a", "b")}
        };
    }
}
