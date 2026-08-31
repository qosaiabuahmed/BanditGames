package be.kdg.banditgamesbackend.achievement.port.out;

import java.util.UUID;

public interface DeleteTempUserAchievementPort {
    void delete(UUID id);
    void deleteByUserId(UUID userId);
}
