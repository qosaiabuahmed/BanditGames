package be.kdg.banditgamesbackend.social;

import be.kdg.banditgamesbackend.config.TestcontainersConfiguration;
import be.kdg.banditgamesbackend.social.adapter.in.dto.SendFriendRequestDto;
import be.kdg.banditgamesbackend.social.adapter.out.persistence.FriendshipJpaRepository;
import be.kdg.banditgamesbackend.social.adapter.out.persistence.SocialUserProjectionRepository;
import be.kdg.banditgamesbackend.user.adapter.out.persistence.UserJpaRepository;
import be.kdg.banditgamesbackend.util.TestUserFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static be.kdg.banditgamesbackend.util.TestJwtHelper.jwtWithUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@Import(TestcontainersConfiguration.class)
class FriendshipIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    FriendshipJpaRepository friendshipRepository;

    @Autowired
    SocialUserProjectionRepository userProjectionRepository;

    @Autowired
    UserJpaRepository  userRepository;

    private UUID user1Id;
    private UUID user2Id;
    private static final String USER1_EMAIL = "user1@test.com";
    private static final String USER2_EMAIL = "user2@test.com";

    @BeforeEach
    void setUp() {
        friendshipRepository.deleteAll();
        userProjectionRepository.deleteAll();
        userRepository.deleteAll();

        user1Id = UUID.randomUUID();
        user2Id = UUID.randomUUID();

        userRepository.save(TestUserFactory.createUser(user1Id, "user1", USER1_EMAIL));
        userRepository.save(TestUserFactory.createUser(user2Id, "user2", USER2_EMAIL));

        userProjectionRepository.save(
                TestUserFactory.createSocialProjection(user1Id, "user1", USER1_EMAIL)
        );
        userProjectionRepository.save(
                TestUserFactory.createSocialProjection(user2Id, "user2", USER2_EMAIL)
        );
    }

    @Test
    void sendAndAcceptFriendRequest_fullFlow_success() throws Exception {
        SendFriendRequestDto request = new SendFriendRequestDto(user2Id);

        mockMvc.perform(post("/api/social/friendships/requests")
                        .with(jwtWithUser(USER1_EMAIL, user1Id.toString()))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Friend request sent successfully"));

        assertThat(friendshipRepository.findAll()).hasSize(1);
        var friendship = friendshipRepository.findAll().getFirst();
        assertThat(friendship.getUserId()).isEqualTo(user1Id);
        assertThat(friendship.getFriendId()).isEqualTo(user2Id);
        assertThat(friendship.getStatus()).hasToString("PENDING");

        mockMvc.perform(get("/api/social/friendships/requests/pending")
                        .with(jwtWithUser(USER2_EMAIL, user2Id.toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId").value(user1Id.toString()));

        UUID friendshipId = friendship.getFriendshipId();
        mockMvc.perform(put("/api/social/friendships/{friendshipId}/accept", friendshipId)
                        .with(jwtWithUser(USER2_EMAIL, user2Id.toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Friend request accepted"));

        mockMvc.perform(get("/api/social/friendships/friends")
                        .with(jwtWithUser(USER1_EMAIL, user1Id.toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(user2Id.toString()));

        mockMvc.perform(get("/api/social/friendships/friends")
                        .with(jwtWithUser(USER2_EMAIL, user2Id.toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(user1Id.toString()));
    }
}
