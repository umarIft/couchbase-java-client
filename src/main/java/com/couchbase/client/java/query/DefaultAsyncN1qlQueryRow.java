package com.couchbase.client.java.query;

import java.io.IOException;

import com.couchbase.client.deps.com.fasterxml.jackson.databind.ObjectMapper;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.TranscodingException;

public class DefaultAsyncN1qlQueryRow implements AsyncN1qlQueryRow {

    private JsonObject value = null;
    private final byte[] byteValue;
    private final ObjectMapper objectMapper;

    public DefaultAsyncN1qlQueryRow(byte[] value, ObjectMapper mapper) {
        this.byteValue = value;
        this.objectMapper = mapper;
    }

    @Override
    public byte[] byteValue() {
        return byteValue;
    }

    /**
     * Return the {@link JsonObject} representation of the JSON corresponding to this row.
     * The {@link JsonObject} is lazily created from {@link #byteValue()} the first time it is requested.
     *
     * @return the JsonObject representation of the value.
     * @throws TranscodingException if the lazy deserialization couldn't be performed due to a Jackson error.
     */
    @Override
    public JsonObject value() {
        if (byteValue == null) {
            return null;
        } else if (value == null) {
            try {
                value = objectMapper.readValue(byteValue, JsonObject.class);
                return value;
            } catch (IOException e) {
                throw new TranscodingException("Error deserializing row value from bytes to JsonObject", e);
            }
        } else {
            return value;
        }
    }

    @Override
    public String toString() {
        return value().toString();
    }
}
