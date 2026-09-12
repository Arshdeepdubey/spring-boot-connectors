package com.example.connectors.common.fileconvert;

/** Output file formats a connector can convert transformed records into before shipping them. */
public enum FileFormat {
    CSV,
    JSON;

    public String fileExtension() {
        return this == CSV ? "csv" : "json";
    }

    public String contentType() {
        return this == CSV ? "text/csv" : "application/json";
    }
}
