package tests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class JsonTest {
    private final ClassLoader cl = JsonTest.class.getClassLoader();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final List<String> SPONSORS = Arrays.asList("NASA", "ESA", "Roscosmos");
    private static final List<String> CREW = Arrays.asList("Astronaut", "Engineer", "Pilot");

    @Test
    void JsonParsingTest() throws Exception {

        try (Reader reader = new InputStreamReader(
                Objects.requireNonNull(cl.getResourceAsStream("jsonTestFile.json")), StandardCharsets.UTF_8)) {
            JsonNode actualFile = objectMapper.readTree(reader);

            assertNotNull(actualFile, "JSON-файл пустой или невалидный");
            assertFalse(actualFile.isEmpty(), "JSON содержит пустой объект");
            assertNotNull(actualFile.get("sponsors"), "Ключ sponsors отсутствует");
            assertNotNull(actualFile.get("launchYear"), "Ключ launchYear отсутствует");
            assertNotNull(actualFile.get("mission"), "Ключ mission отсутствует");
            assertNotNull(actualFile.get("spacecraft"), "Ключ spacecraft отсутствует");
            assertNotNull(actualFile.get("goals"), "Ключ goals отсутствует");

            List<String> actualSponsors = new ArrayList<>();
            for (JsonNode node : actualFile.get("sponsors")) {
                actualSponsors.add(node.asText());
            }

            List<String> actualCrew = new ArrayList<>();
            for (JsonNode node : actualFile.get("spacecraft").get("crew")) {
                actualCrew.add(node.asText());
            }

            assertAll(
                    () -> assertEquals("Exploration of Mars", actualFile.get("mission").asText(), "Название миссии не совпадает"),
                    () -> assertEquals(2028, actualFile.get("launchYear").asInt(), "Год запуска не совпадает"),
                    () -> assertEquals("Mars Explorer 1", actualFile.get("spacecraft").get("name").asText(), "Название корабля не совпадает"),
                    () -> assertEquals(SPONSORS, actualSponsors, "Список спонсоров не совпадает - ожидали: " + SPONSORS + ", получили: " + actualSponsors),
                    () -> assertEquals(CREW, actualCrew, "Команда не совпадает - ожидали: " + CREW + ", получили: " + actualCrew),
                    () -> assertEquals("Search for signs of past life", actualFile.get("goals").get(0).asText(),
                            "Первая цель не совпадает"),
                    () -> assertEquals("Study climate and geology", actualFile.get("goals").get(1).asText(),
                            "Вторая цель не совпадает"),
                    () -> assertEquals("Test new technologies for future missions", actualFile.get("goals").get(2).asText(),
                            "Третья цель не совпадает")
            );
        }
    }
}
