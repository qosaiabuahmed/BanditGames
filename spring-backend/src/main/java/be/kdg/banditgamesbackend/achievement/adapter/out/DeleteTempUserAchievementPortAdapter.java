package be.kdg.banditgamesbackend.achievement.adapter.out;

import be.kdg.banditgamesbackend.achievement.port.out.DeleteTempUserAchievementPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeleteTempUserAchievementPortAdapter implements DeleteTempUserAchievementPort {

    private final TempAchievementJpaRepository repository;

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        repository.deleteByGuestUserId(userId);
    }
}
