package be.kdg.banditgamesbackend.user;

import be.kdg.banditgamesbackend.achievement.adapter.out.TempAchievementJpaEntity;
import be.kdg.banditgamesbackend.achievement.adapter.out.TempAchievementJpaRepository;
import be.kdg.banditgamesbackend.achievement.adapter.out.UserAchievementJpaRepository;
import be.kdg.banditgamesbackend.user.adapter.out.persistence.UserJpaRepository;
import be.kdg.banditgamesbackend.user.adapter.out.persistence.UserMapper;
import be.kdg.banditgamesbackend.user.domain.User;
import be.kdg.banditgamesbackend.user.port.in.ConvertGuestUserCommand;
import be.kdg.banditgamesbackend.user.port.in.ConvertGuestUserUseCase;
import be.kdg.banditgamesbackend.user.port.in.RegisterGuestUserCommand;
import be.kdg.banditgamesbackend.user.port.in.RegisterGuestUserUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("integration")
class GuestUserFlowIntegrationTest {

    @Autowired
    private RegisterGuestUserUseCase registerGuestUserUseCase;

    @Autowired
    private ConvertGuestUserUseCase convertGuestUserUseCase;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private TempAchievementJpaRepository tempAchievementRepository;

    @Autowired
    private UserAchievementJpaRepository achievementRepository;

    @Test
    void completeGuestUserFlow() {
        // 1. Auto-register guest
        UUID guestId = UUID.randomUUID();
        registerGuestUserUseCase.registerGuest(new RegisterGuestUserCommand(
                guestId, "TestPlayer"
        ));

        User guest = userRepository.findById(guestId).map(UserMapper::toDomain).orElseThrow();
        assertThat(guest.isGuest()).isTrue();

        UUID gameId = UUID.randomUUID();
        UUID matchId = UUID.randomUUID();
        
        // 2. Unlock achievement as guest
        tempAchievementRepository.save(new TempAchievementJpaEntity(
                UUID.randomUUID(), guestId, gameId, "FIRST_WIN", matchId, LocalDateTime.now()
        ));

        assertThat(tempAchievementRepository.findByGuestUserId(guestId)).hasSize(1);
        assertThat(achievementRepository.findByUserId(guestId)).isEmpty();

        // 3. Convert to registered
        convertGuestUserUseCase.convertToRegistered(new ConvertGuestUserCommand(
                guestId, "testuser", "test@example.com", "password123"
        ));

        // 4. Verify conversion
        User registered = userRepository.findById(guestId).map(UserMapper::toDomain).orElseThrow();
        assertThat(registered.isGuest()).isFalse();
        assertThat(registered.getEmail().address()).isEqualTo("test@example.com");

        // 5. Verify achievement migration (triggered by event listener)
        await().atMost(2, SECONDS).untilAsserted(() -> {
            assertThat(tempAchievementRepository.findByGuestUserId(guestId)).isEmpty();
            assertThat(achievementRepository.findByUserId(guestId)).hasSize(1);
        });
    }
}

