package app.core.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class JsonIdTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void serializesOnlyAnnotatedLongValuesAsStrings() {
        String json = jsonMapper.writeValueAsString(new SampleDto(42L, 7L));

        assertThat(json).isEqualTo("{\"id\":\"42\",\"counter\":7}");
    }

    @Test
    void preservesNullAndAcceptsStringIdentifiers() {
        assertThat(jsonMapper.writeValueAsString(new SampleDto(null, 7L)))
                .isEqualTo("{\"id\":null,\"counter\":7}");
        assertThat(jsonMapper.readValue("{\"id\":\"42\",\"counter\":7}", SampleDto.class))
                .isEqualTo(new SampleDto(42L, 7L));
    }

    private record SampleDto(@JsonId Long id, Long counter) {
    }
}
