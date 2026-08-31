package be.kdg.banditgamesbackend.achievement.core;

import be.kdg.banditgamesbackend.achievement.domain.TempAchievement;
import be.kdg.banditgamesbackend.achievement.port.in.SaveTempAchievementCommand;
import be.kdg.banditgamesbackend.achievement.port.in.SaveTempAchievementUseCase;
import be.kdg.banditgamesbackend.achievement.port.out.SaveTempUserAchievementPort;
import be.kdg.banditgamesbackend.common.validation.Validators;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaveTempAchievementUseCaseImpl implements SaveTempAchievementUseCase {

    private final SaveTempUserAchievementPort savePort;

    @Override
    public void save(SaveTempAchievementCommand command) {
        log.info("Saving temporary achievement {} for guest {}", command.achievementCode(), command.guestUserId());

        validateAchievement(command);

        TempAchievement achievement = new TempAchievement(
                command.guestUserId(),
                command.achievementCode(),
                command.gameId(),
                command.matchId(),
                command.unlockedAt()
        );

        savePort.save(achievement);
    }

    private void validateAchievement(SaveTempAchievementCommand command) {
        Validators.requireNonNull(command.guestUserId(), "Guest User Id");
        Validators.requireNonBlank(command.achievementCode(), "Achievement Code");
        Validators.requireNonNull(command.gameId(), "Game Id");
        Validators.requireNonNull(command.matchId(), "Match Id");
        Validators.requireNonNull(command.unlockedAt(), "Unlocked At");
    }
}
