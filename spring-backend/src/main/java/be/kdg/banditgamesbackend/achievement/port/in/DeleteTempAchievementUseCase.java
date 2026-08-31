package be.kdg.banditgamesbackend.achievement.port.in;

import java.util.UUID;

public interface DeleteTempAchievementUseCase {
    void delete(UUID id);
    void deleteByUserId(UUID userId);
}
