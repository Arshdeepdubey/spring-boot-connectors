package com.example.connectors.common.transform;

/** Business transformation contract: turns a validated source record into its output shape. */
public interface RecordTransformer<I, O> {

    O transform(I input);
}
