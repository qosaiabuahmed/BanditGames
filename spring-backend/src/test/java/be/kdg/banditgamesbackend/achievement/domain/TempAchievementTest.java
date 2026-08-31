package be.kdg.banditgamesbackend.achievement.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TempAchievementTest {

    @Test
    void constructor_ShouldInitializeFields() {
        UUID guestId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID matchId = UUID.randomUUID();
        String code = "FIRST_WIN";
        LocalDateTime now = LocalDateTime.now();

        TempAchievement temp = new TempAchievement(guestId, code, gameId, matchId, now);

        assertThat(temp.getId()).isNotNull();
        assertThat(temp.getGuestUserId()).isEqualTo(guestId);
        assertThat(temp.getAchievementCode()).isEqualTo(code);
        assertThat(temp.getGameId()).isEqualTo(gameId);
        assertThat(temp.getMatchId()).isEqualTo(matchId);
        assertThat(temp.getUnlockedAt()).isEqualTo(now);
    }
}
