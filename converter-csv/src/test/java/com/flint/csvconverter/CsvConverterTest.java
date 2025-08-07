package com.flint.csvconverter;

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

class CsvConverterTest {

    @Mock
    private CsvConverterService csvConverterService;

    @InjectMocks
    private CsvConverter csvConverter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void toStandard_ShouldConvertCsvToJson() throws Exception {
        String csvContent = "header1,header2\nvalue1,value2";
        MockMultipartFile file =  new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes());

        JsonNode result = csvConverter.toStandard(file);

        assertNotNull(result);
        assertTrue(result.has("data"));
        assertTrue(result.get("data").isArray());
        assertEquals(1, result.get("data").size());
        assertEquals("value1", result.get("data").get(0).get("header1").asText());
        assertEquals("value2", result.get("data").get(0).get("header2").asText());
    }

    @Test
    void fromStandard_ShouldConvertJsonToCsv() throws Exception {
        String jsonContent = "{\"data\":[{\"header1\":\"value1\",\"header2\":\"value2\"}]}";
        JsonNode jsonNode = objectMapper.readTree(jsonContent);

        ConversionResult result = csvConverter.fromStandard(jsonNode);

        assertNotNull(result);
        assertEquals("text/csv", result.getContentType());
        String csvResult = new String(result.getData());
        assertTrue(csvResult.contains("\"header1\",\"header2\""));
        assertTrue(csvResult.contains("\"value1\",\"value2\""));
    }
}
