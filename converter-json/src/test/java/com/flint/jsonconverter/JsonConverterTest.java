package com.flint.jsonconverter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import com.flint.core.ConversionResult;

import static org.junit.jupiter.api.Assertions.*;

class JsonConverterTest {

    @Mock
    private JsonConverterService jsonConverterService;

    @InjectMocks
    private JsonConverter jsonConverter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void toStandard_ShouldWrapJsonInDataField() throws Exception {
        String jsonContent = "{\"key\":\"value\"}";
        MockMultipartFile file = new MockMultipartFile("file", "test.json", "application/json",
                jsonContent.getBytes());

        JsonNode result = jsonConverter.toStandard(file);

        assertNotNull(result);
        assertTrue(result.has("data"));
        assertTrue(result.get("data").isArray());
        assertEquals(1, result.get("data").size());
        assertEquals("value", result.get("data").get(0).get("key").asText());
    }

    @Test
    void fromStandard_ShouldReturnCorrectJson() throws Exception {
        String jsonContent = "{\"data\":[{\"key\":\"value\"}]}";
        JsonNode jsonNode = objectMapper.readTree(jsonContent);

        ConversionResult result = jsonConverter.fromStandard(jsonNode);

        assertNotNull(result);
        assertEquals("application/json", result.getContentType());
        String jsonResult = new String(result.getData());
        assertTrue(jsonResult.contains("\"data\""));
        assertTrue(jsonResult.contains("{\"key\":\"value\"}"));
    }
}
