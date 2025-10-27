package co.unicauca.review.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record NotificationEventDTO(
    String eventType,
    Long documentId,
    String documentTitle,
    String documentType,
    String decision,
    String evaluatorName,
    String evaluatorRole,
    String observaciones,
    List<String> recipients,
    LocalDateTime timestamp
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String eventType;
        private Long documentId;
        private String documentTitle;
        private String documentType;
        private String decision;
        private String evaluatorName;
        private String evaluatorRole;
        private String observaciones;
        private List<String> recipients;
        private LocalDateTime timestamp;

        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder documentId(Long documentId) {
            this.documentId = documentId;
            return this;
        }

        public Builder documentTitle(String documentTitle) {
            this.documentTitle = documentTitle;
            return this;
        }

        public Builder documentType(String documentType) {
            this.documentType = documentType;
            return this;
        }

        public Builder decision(String decision) {
            this.decision = decision;
            return this;
        }

        public Builder evaluatorName(String evaluatorName) {
            this.evaluatorName = evaluatorName;
            return this;
        }

        public Builder evaluatorRole(String evaluatorRole) {
            this.evaluatorRole = evaluatorRole;
            return this;
        }

        public Builder observaciones(String observaciones) {
            this.observaciones = observaciones;
            return this;
        }

        public Builder recipients(List<String> recipients) {
            this.recipients = recipients;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public NotificationEventDTO build() {
            return new NotificationEventDTO(
                eventType, documentId, documentTitle, documentType,
                decision, evaluatorName, evaluatorRole, observaciones,
                recipients, timestamp
            );
        }
    }
}

