package com.example.connectors.common.http;

/** Supported authentication schemes a connector can use against a source or target REST API. */
public enum AuthType {
    NONE,
    BASIC,
    API_KEY,
    BEARER
}
