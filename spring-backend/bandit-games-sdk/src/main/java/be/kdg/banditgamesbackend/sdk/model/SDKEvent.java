package be.kdg.banditgamesbackend.sdk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SDKEvent {
    private UUID eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private UUID aggregateId;
    private String aggregateType;
    private Map<String, Object> payload;
    private Map<String, Object> metadata;

    public static SDKEvent create(String type, UUID aggregateId, String aggregateType, Map<String, Object> payload) {
        return SDKEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(type)
                .timestamp(LocalDateTime.now())
                .aggregateId(aggregateId)
                .aggregateType(aggregateType)
                .payload(payload)
                .build();
    }
}
