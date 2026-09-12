package com.example.connectors.common.fileconvert;

import java.util.List;
import java.util.Map;

/** Converts transformed records into the on-the-wire file format sent to the target system. */
public interface FileConverterService {

    /**
     * @param records ordered rows, each a flat map of column name -&gt; value
     * @param columns column order to use for CSV; ignored for JSON
     */
    byte[] convert(List<Map<String, Object>> records, FileFormat format, List<String> columns);

    /** Parses a previously converted file back into row maps (used by connectors that read files, e.g. s3-to-rest). */
    List<Map<String, Object>> parse(byte[] content, FileFormat format);
}
