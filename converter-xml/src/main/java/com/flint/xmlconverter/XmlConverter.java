package com.flint.xmlconverter;

import java.util.List;
import com.flint.core.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class XmlConverter implements Converter {

  private final XmlConverterService xmlConverterService;

  private final XmlMapper xmlMapper = new XmlMapper();
  private final ObjectMapper jsonMapper = new ObjectMapper();

  public XmlConverter(XmlConverterService xmlConverterService) {
    this.xmlConverterService = xmlConverterService;
  }

  @GetMapping("/alive")
  public Boolean alive() {
    return true;
  }

  @Override
  public String getFormatName() {
    return "xml";
  }

  @Override
  public List<String> getSupportedContentTypes() {
    return List.of("application/xml", "text/xml");
  }

  @Override
  @PostMapping("/std")
  public JsonNode toStandard(MultipartFile file) throws Exception {
    JsonNode incomingData = xmlMapper.readTree(file.getInputStream());

    ObjectMapper jsonMapper = new ObjectMapper();
    ObjectNode responseNode = jsonMapper.createObjectNode();

    if (incomingData.isArray()) {
      responseNode.set("data", incomingData);
    } else {
      ArrayNode dataArray = jsonMapper.createArrayNode();
      dataArray.add(incomingData);
      responseNode.set("data", dataArray);
    }

    return responseNode;
  }

  @Override
  @PostMapping("/out")
  public ConversionResult fromStandard(@RequestBody JsonNode standardData) throws Exception {
    byte[] xmlData;

    if (standardData.isArray()) {
      ObjectNode wrapper = jsonMapper.createObjectNode();

      wrapper.set("item", standardData);

      xmlData = xmlMapper.writer().withRootName("data").writeValueAsBytes(wrapper);

    } else {
      xmlData = xmlMapper.writeValueAsBytes(standardData);
    }

    return new ConversionResult(xmlData, "application/xml");
  }
}