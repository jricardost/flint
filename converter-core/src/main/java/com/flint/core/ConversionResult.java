package com.flint.core;

public class ConversionResult {
    private byte[] data;
    private String contentType;

    public ConversionResult() {
        this.data = "vazio".getBytes();
        this.contentType = "application/octet-stream";
    }

    public ConversionResult(byte[] data) {
        this.data = data;
        this.contentType = "application/octet-stream";
    }

    public ConversionResult(byte[] data, String contentType) {
        this.data = data;
        this.contentType = contentType;
    }

    public byte[] getData() {
        return data;
    }

    public String getContentType() {
        return contentType;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
