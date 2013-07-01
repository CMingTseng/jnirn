package com.natpryce.approvals;

import org.junit.ComparisonFailure;

import java.io.File;

public class ApprovalError extends AssertionError {
    public final File approvedFile;
    public final String approvedContents; // Can be null, should split into two exception types
    public final File receivedFile;
    public final String receivedContents;

    public ApprovalError(File approvedFile, String approvedContents, File receivedFile, String receivedContents) {
        super("approval required: approved file=" + approvedFile + ", received file=" + receivedFile);

        this.approvedFile = approvedFile;
        this.approvedContents = approvedContents;
        this.receivedFile = receivedFile;
        this.receivedContents = receivedContents;
    }

    public ApprovalError(File approvedFile, File receivedFile, String receivedContents) {
        this(approvedFile, null, receivedFile, receivedContents);
    }
}
