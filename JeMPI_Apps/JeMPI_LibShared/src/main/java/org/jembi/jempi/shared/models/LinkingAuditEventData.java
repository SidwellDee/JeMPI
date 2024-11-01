package org.jembi.jempi.shared.models;


public record LinkingAuditEventData(
        String message,
        String interactionID,
        String goldenID,
        float score,
        LinkingRule linkingRule
) {
}
