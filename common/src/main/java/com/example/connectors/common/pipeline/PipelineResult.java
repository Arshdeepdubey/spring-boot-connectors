package com.example.connectors.common.pipeline;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Summary returned by every connector's "execute" endpoint: how many records were
 * read, how many passed validation, where the output landed, and what (if anything)
 * went wrong for individual records. Partial failures are recorded here rather than
 * aborting the whole run, so one bad record does not block the rest of the batch.
 */
public class PipelineResult {

    private final Instant startedAt = Instant.now();
    private Instant finishedAt;
    private int recordsRead;
    private int recordsValid;
    private int recordsInvalid;
    private int recordsDelivered;
    private int recordsFailed;
    private String sourceLocation;
    private String targetLocation;
    private PipelineStatus status = PipelineStatus.SUCCESS;
    private final List<String> errors = new ArrayList<>();

    public void addError(String error) {
        this.errors.add(error);
    }

    public void finish() {
        this.finishedAt = Instant.now();
        if (recordsRead == 0) {
            status = PipelineStatus.SUCCESS;
            return;
        }
        int problems = recordsInvalid + recordsFailed;
        if (problems == 0) {
            status = PipelineStatus.SUCCESS;
        } else if (recordsDelivered > 0) {
            status = PipelineStatus.PARTIAL_SUCCESS;
        } else {
            status = PipelineStatus.FAILED;
        }
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public int getRecordsRead() {
        return recordsRead;
    }

    public void setRecordsRead(int recordsRead) {
        this.recordsRead = recordsRead;
    }

    public int getRecordsValid() {
        return recordsValid;
    }

    public void setRecordsValid(int recordsValid) {
        this.recordsValid = recordsValid;
    }

    public int getRecordsInvalid() {
        return recordsInvalid;
    }

    public void setRecordsInvalid(int recordsInvalid) {
        this.recordsInvalid = recordsInvalid;
    }

    public int getRecordsDelivered() {
        return recordsDelivered;
    }

    public void setRecordsDelivered(int recordsDelivered) {
        this.recordsDelivered = recordsDelivered;
    }

    public void incrementDelivered() {
        this.recordsDelivered++;
    }

    public int getRecordsFailed() {
        return recordsFailed;
    }

    public void incrementFailed() {
        this.recordsFailed++;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(String sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public String getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(String targetLocation) {
        this.targetLocation = targetLocation;
    }

    public PipelineStatus getStatus() {
        return status;
    }

    public List<String> getErrors() {
        return errors;
    }
}
