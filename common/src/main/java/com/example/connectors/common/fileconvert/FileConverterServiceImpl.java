package com.example.connectors.common.fileconvert;

import com.example.connectors.common.exception.TransformationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileConverterServiceImpl implements FileConverterService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] convert(List<Map<String, Object>> records, FileFormat format, List<String> columns) {
        try {
            return switch (format) {
                case CSV -> toCsv(records, columns);
                case JSON -> objectMapper.writeValueAsBytes(records);
            };
        } catch (IOException ex) {
            throw new TransformationException("Failed to convert " + records.size() + " record(s) to " + format, ex);
        }
    }

    @Override
    public List<Map<String, Object>> parse(byte[] content, FileFormat format) {
        try {
            return switch (format) {
                case CSV -> fromCsv(content);
                case JSON -> objectMapper.readValue(content, new TypeReference<List<Map<String, Object>>>() {
                });
            };
        } catch (IOException ex) {
            throw new TransformationException("Failed to parse " + format + " content", ex);
        }
    }

    private byte[] toCsv(List<Map<String, Object>> records, List<String> columns) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CSVFormat format = CSVFormat.DEFAULT.builder().setHeader(columns.toArray(new String[0])).build();
        try (CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(out, StandardCharsets.UTF_8), format)) {
            for (Map<String, Object> record : records) {
                List<Object> row = new ArrayList<>(columns.size());
                for (String column : columns) {
                    row.add(record.get(column));
                }
                printer.printRecord(row);
            }
        }
        return out.toByteArray();
    }

    private List<Map<String, Object>> fromCsv(byte[] content) throws IOException {
        List<Map<String, Object>> rows = new ArrayList<>();
        CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build();
        try (CSVParser parser = new CSVParser(
                new InputStreamReader(new ByteArrayInputStream(content), StandardCharsets.UTF_8), format)) {
            for (CSVRecord csvRecord : parser) {
                Map<String, Object> row = new LinkedHashMap<>();
                csvRecord.toMap().forEach(row::put);
                rows.add(row);
            }
        }
        return rows;
    }
}
