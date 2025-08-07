package com.flint.converterservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flint.core.ConversionResult;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
class ConversionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void convertFile_ShouldReturnConvertedFile() throws Exception {
        // Mocking RestTemplate calls
        when(restTemplate.getForObject(eq("http://converter-csv/alive"), eq(Boolean.class))).thenReturn(true);
        when(restTemplate.getForObject(eq("http://converter-json/alive"), eq(Boolean.class))).thenReturn(true);

        // Mocking CSV to Standard
        ObjectNode standardData = objectMapper.createObjectNode();
        standardData.putArray("data").addObject().put("header", "value");
        when(restTemplate.postForObject(eq("http://converter-csv/std"), any(), any(Class.class))).thenReturn(standardData);

        // Mocking Standard to JSON
        byte[] finalJson = "{\"converted\":true}".getBytes();
        ConversionResult conversionResult = new ConversionResult(finalJson, "application/json");
        when(restTemplate.postForObject(eq("http://converter-json/out"), any(ObjectNode.class), eq(ConversionResult.class)))
            .thenReturn(conversionResult);


        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            "header\nvalue".getBytes()
        );

        mockMvc.perform(multipart("/api/convert")
                .file(file)
                .param("to", "json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"converted.json\""))
                .andExpect(jsonPath("$.converted").value(true));
    }
}