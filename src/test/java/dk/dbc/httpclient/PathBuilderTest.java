package dk.dbc.httpclient;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PathBuilderTest {
    private static final String PATH_TEMPLATE = "{id1}/test/{id2}/{id1}/test/{id2}";

    @Test
    public void constructor_pathTemplateArgIsNull_throws() {
        assertThrows(NullPointerException.class, () -> new PathBuilder(null));
    }

    @Test
    public void pathBuilder_noValuesBound_returnsPathTemplateUnchanged() {
        final PathBuilder pathBuilder = new PathBuilder(PATH_TEMPLATE);
        assertThat(pathBuilder.build(), is(PATH_TEMPLATE.split(PathBuilder.PATH_SEPARATOR)));
    }

    @Test
    public void pathBuilder_whenValuesMatchPathVariables_returnsInterpolatedPath() {
        final String expectedPath = "%2F%3Fval1/test/%24val2/%2F%3Fval1/test/%24val2";
        final PathBuilder pathBuilder = new PathBuilder(PATH_TEMPLATE);
        pathBuilder.bind("id1", "/?val1");
        pathBuilder.bind("id2", "$val2");
        assertThat(pathBuilder.build(), is(expectedPath.split(PathBuilder.PATH_SEPARATOR)));
    }
}
