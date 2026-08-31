package be.kdg.banditgamesbackend.achievement;

import be.kdg.banditgamesbackend.achievement.adapter.out.TempAchievementJpaRepository;
import be.kdg.banditgamesbackend.achievement.domain.UserAchievement;
import be.kdg.banditgamesbackend.achievement.port.out.LoadUserAchievementPort;
import be.kdg.banditgamesbackend.common.events.AchievementUnlockedEvent;
import be.kdg.banditgamesbackend.user.api.UserLookupService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("integration")
class AchievementUnlockIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private LoadUserAchievementPort loadUserAchievementPort;

    @Autowired
    private TempAchievementJpaRepository tempAchievementRepository;

    @MockitoBean
    private UserLookupService userLookupService;

    private UUID regularUserId;
    private UUID guestUserId;
    private UUID gameId;
    private UUID matchId;

    @BeforeEach
    void setUp() {
        regularUserId = UUID.randomUUID();
        guestUserId = UUID.randomUUID();
        gameId = UUID.fromString("8496c496-a884-48ed-9bb3-7c3aa50fb8ca"); // Chess
        matchId = UUID.randomUUID();

        when(userLookupService.isGuestUser(regularUserId)).thenReturn(false);
        when(userLookupService.isGuestUser(guestUserId)).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        tempAchievementRepository.deleteAll();
    }

    @Test
    void shouldUnlockAchievementForRegularUser() {
        AchievementUnlockedEvent event = new AchievementUnlockedEvent(
                gameId,
                matchId,
                regularUserId,
                "testUser",
                "FIRST_BLOOD",
                "Captured opponent's first piece",
                LocalDateTime.now()
        );

        rabbitTemplate.convertAndSend(
                "game.events.exchange",
                "achievement.unlocked",
                event
        );

        await().atMost(5, SECONDS).untilAsserted(() -> {
            List<UserAchievement> achievements = loadUserAchievementPort
                    .findByUserId(regularUserId);

            assertThat(achievements)
                    .hasSize(1)
                    .extracting(UserAchievement::getAchievementCode)
                    .containsExactly("FIRST_BLOOD");

            UserAchievement achievement = achievements.getFirst();
            assertThat(achievement.getGameId()).isEqualTo(gameId);
            assertThat(achievement.getMatchId()).isEqualTo(matchId);
            assertThat(achievement.getUserId()).isEqualTo(regularUserId);
        });

        assertThat(tempAchievementRepository.findByGuestUserId(regularUserId))
                .isEmpty();
    }

    @Test
    void shouldSaveTemporaryAchievementForGuestUser() {
        AchievementUnlockedEvent event = new AchievementUnlockedEvent(
                gameId,
                matchId,
                guestUserId,
                "guest_player",
                "PAWN_POWER",
                "Promoted a pawn",
                LocalDateTime.now()
        );

        rabbitTemplate.convertAndSend(
                "game.events.exchange",
                "achievement.unlocked",
                event
        );

        await().atMost(5, SECONDS).untilAsserted(() -> {
            var tempAchievements = tempAchievementRepository.findByGuestUserId(guestUserId);

            assertThat(tempAchievements)
                    .hasSize(1)
                    .first()
                    .satisfies(temp -> {
                        assertThat(temp.getGuestUserId()).isEqualTo(guestUserId);
                        assertThat(temp.getGameId()).isEqualTo(gameId);
                        assertThat(temp.getMatchId()).isEqualTo(matchId);
                        assertThat(temp.getAchievementCode()).isEqualTo("PAWN_POWER");
                        assertThat(temp.getUnlockedAt()).isNotNull();
                    });
        });

        List<UserAchievement> permanentAchievements = loadUserAchievementPort
                .findByUserId(guestUserId);
        assertThat(permanentAchievements).isEmpty();
    }

    @Test
    void shouldUnlockMultipleAchievementsForSameUser() {
        AchievementUnlockedEvent event1 = new AchievementUnlockedEvent(
                gameId, matchId, regularUserId, "testUser",
                "FIRST_BLOOD", "Captured first piece", LocalDateTime.now()
        );

        AchievementUnlockedEvent event2 = new AchievementUnlockedEvent(
                gameId, matchId, regularUserId, "testUser",
                "CASTLE_TIME", "Castled kingside", LocalDateTime.now()
        );

        rabbitTemplate.convertAndSend("game.events.exchange", "achievement.unlocked", event1);
        rabbitTemplate.convertAndSend("game.events.exchange", "achievement.unlocked", event2);

        await().atMost(5, SECONDS).untilAsserted(() -> {
            List<UserAchievement> achievements = loadUserAchievementPort
                    .findByUserId(regularUserId);

            assertThat(achievements)
                    .hasSize(2)
                    .extracting(UserAchievement::getAchievementCode)
                    .containsExactlyInAnyOrder("FIRST_BLOOD", "CASTLE_TIME");
        });
    }

    @Test
    void shouldHandleDuplicateAchievementGracefully() {
        AchievementUnlockedEvent event = new AchievementUnlockedEvent(
                gameId, matchId, regularUserId, "testUser",
                "FIRST_BLOOD", "Captured first piece", LocalDateTime.now()
        );

        rabbitTemplate.convertAndSend("game.events.exchange", "achievement.unlocked", event);

        await().atMost(3, SECONDS).untilAsserted(() -> assertThat(loadUserAchievementPort.findByUserId(regularUserId)).hasSize(1));

        rabbitTemplate.convertAndSend("game.events.exchange", "achievement.unlocked", event);

        await().atMost(3, SECONDS).untilAsserted(() -> {
            List<UserAchievement> achievements = loadUserAchievementPort
                    .findByUserId(regularUserId);

            assertThat(achievements).hasSize(1);
        });
    }

    @Test
    void shouldRejectInvalidEvent() {
        AchievementUnlockedEvent invalidEvent = new AchievementUnlockedEvent(
                gameId, matchId, null, null,
                "FIRST_BLOOD", "Captured first piece", LocalDateTime.now()
        );

        rabbitTemplate.convertAndSend("game.events.exchange", "achievement.unlocked", invalidEvent);

        await().pollDelay(2, SECONDS).atMost(3, SECONDS).untilAsserted(() -> assertThat(tempAchievementRepository.findAll()).isEmpty());
    }
}
