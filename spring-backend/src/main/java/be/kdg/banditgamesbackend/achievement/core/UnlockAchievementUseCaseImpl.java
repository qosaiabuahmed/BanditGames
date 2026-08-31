package be.kdg.banditgamesbackend.achievement.core;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.kdg.banditgamesbackend.achievement.domain.UserAchievement;
import be.kdg.banditgamesbackend.achievement.port.in.UnlockAchievementCommand;
import be.kdg.banditgamesbackend.achievement.port.in.UnlockAchievementUseCase;
import be.kdg.banditgamesbackend.achievement.port.out.LoadUserAchievementPort;
import be.kdg.banditgamesbackend.achievement.port.out.SaveUserAchievementPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnlockAchievementUseCaseImpl implements UnlockAchievementUseCase{

    private final SaveUserAchievementPort savePort;
    private final LoadUserAchievementPort loadPort;

    @Override
    @Transactional
    public void unlock(UnlockAchievementCommand command) {
        if (loadPort.hasUnlocked(command.userId(), command.gameId(), command.achievementCode())) {
            log.info("Achievement already unlocked: user={}, game={}, code={}", 
                command.userId(), command.gameId(), command.achievementCode());
            return;
        }
        try {
            UserAchievement userAchievement = UserAchievement.unlock(
                command.userId(),
                command.gameId(),
                command.matchId(),
                command.achievementCode(),
                command.unlockedAt()
            );

            savePort.save(userAchievement);
            log.info("Achievement unlocked: user={}, game={}, code={}", 
                command.userId(), command.gameId(), command.achievementCode());
        } catch (Exception e) {
            log.error("Failed to unlock achievement: user={}, game={}, code={}", 
                command.userId(), command.gameId(), command.achievementCode(), e);
        }
    }
}
