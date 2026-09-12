package com.example.connectors.common.http;

/**
 * Generic outbound REST client used by every connector to talk to a source or
 * target API. A single {@link #exchange(RestCallRequest, Class)} method covers
 * GET / POST / PUT / PATCH / DELETE, since the HTTP method is just a field on
 * the request.
 */
public interface RestApiClient {

    <T> T exchange(RestCallRequest request, Class<T> responseType);
}
