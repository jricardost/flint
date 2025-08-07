package com.flint.xmlconverter;

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

class XmlConverterTest {

    @Mock
    private XmlConverterService xmlConverterService;

    @InjectMocks
    private XmlConverter xmlConverter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void toStandard_ShouldConvertXmlToJson() throws Exception {
        String xmlContent = "<root><item><name>test</name></item></root>";
        MockMultipartFile file = new MockMultipartFile("file", "test.xml", "application/xml", xmlContent.getBytes());

        JsonNode result = xmlConverter.toStandard(file);

        assertNotNull(result);
        assertTrue(result.has("data"));
        assertTrue(result.get("data").isArray());
        JsonNode itemNode = result.get("data").get(0).get("item");
		assertNotNull(itemNode);
		assertEquals("test", itemNode.get("name").asText());
    }

    @Test
    void fromStandard_ShouldConvertJsonToXml() throws Exception {
        String jsonContent = "{\"data\":[{\"person\":{\"name\":\"John\",\"age\":\"30\"}}]}";
        JsonNode jsonNode = objectMapper.readTree(jsonContent);

        ConversionResult result = xmlConverter.fromStandard(jsonNode.get("data"));

        assertNotNull(result);
        assertEquals("application/xml", result.getContentType());
        String xmlResult = new String(result.getData());
        assertTrue(xmlResult.contains("<data>"));
        assertTrue(xmlResult.contains("<item>"));
        assertTrue(xmlResult.contains("<person>"));
        assertTrue(xmlResult.contains("<name>John</name>"));
        assertTrue(xmlResult.contains("<age>30</age>"));
    }
}