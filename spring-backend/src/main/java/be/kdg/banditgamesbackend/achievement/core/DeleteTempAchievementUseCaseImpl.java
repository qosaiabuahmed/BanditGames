package be.kdg.banditgamesbackend.achievement.core;

import be.kdg.banditgamesbackend.achievement.port.in.DeleteTempAchievementUseCase;
import be.kdg.banditgamesbackend.achievement.port.out.DeleteTempUserAchievementPort;
import be.kdg.banditgamesbackend.common.validation.Validators;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteTempAchievementUseCaseImpl implements DeleteTempAchievementUseCase {

    private final DeleteTempUserAchievementPort deletePort;

    @Override
    public void delete(UUID id) {
        log.info("Deleting temporary user achievement with id: {}", id);
        Validators.requireNonNull(id, "Temporary Achievement Id");
        deletePort.delete(id);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        log.info("Deleting temporary user achievement with guestUserId: {}", userId);
        Validators.requireNonNull(userId, "Guest User Id");
        deletePort.deleteByUserId(userId);
    }
}
