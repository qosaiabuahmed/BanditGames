package be.kdg.banditgamesbackend.social.adapter.in.web;

import be.kdg.banditgamesbackend.security.JwtUtils;
import be.kdg.banditgamesbackend.social.adapter.in.dto.SendFriendRequestDto;
import be.kdg.banditgamesbackend.social.port.in.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static be.kdg.banditgamesbackend.social.adapter.in.web.SendFriendRequestRequestFixtures.aSendFriendRequestDto;
import static be.kdg.banditgamesbackend.social.adapter.in.web.SendFriendRequestRequestFixtures.anEmptySendFriendRequestDto;
import static be.kdg.banditgamesbackend.util.TestJwtHelper.mockJwtWithEmail;
import static be.kdg.banditgamesbackend.util.TestJwtHelper.mockJwtWithUserId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class FriendshipControllerTest {

    @Autowired
    MockMvcTester mockMvcTester;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    SendFriendRequestUseCase sendFriendRequestUseCase;

    @MockitoBean
    AcceptFriendRequestUseCase acceptFriendRequestUseCase;

    @MockitoBean
    DeclineFriendRequestUseCase declineFriendRequestUseCase;

    @MockitoBean
    GetFriendsQuery getFriendsQuery;

    @MockitoBean
    JwtUtils jwtUtils;

    @Captor
    ArgumentCaptor<SendFriendRequestCommand> commandArgumentCaptor;

    @Test
    void sendFriendRequest_withValidRequest_returnsCreated() throws Exception {
        SendFriendRequestDto request = aSendFriendRequestDto();
        String testEmail = "sender@test.com";
        UUID senderId = UUID.randomUUID();

        when(jwtUtils.extractUserId(any())).thenReturn(UUID.fromString(senderId.toString()));
        doNothing().when(sendFriendRequestUseCase).sendFriendRequest(any());

        assertThat(
                mockMvcTester.post()
                        .uri("/api/social/friendships/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(mockJwtWithEmail(testEmail, senderId.toString()))
        ).hasStatus(CREATED);

        verify(sendFriendRequestUseCase).sendFriendRequest(commandArgumentCaptor.capture());
        assertThat(commandArgumentCaptor.getValue().toUserId())
                .isEqualTo(request.toUserId());
    }

    @TestFactory
    Stream<DynamicTest> sendFriendRequest_withInvalidRequest_returnsBadRequest() {
        return Stream.of(
                anEmptySendFriendRequestDto()
        ).map(req -> dynamicTest(
                "POST to /api/social/friendship/requests with request %s returns HTTP400".formatted(req),
                () -> {
                    when(jwtUtils.extractUserId(any())).thenReturn(UUID.fromString(UUID.randomUUID().toString()));

                    assertThat(mockMvcTester.post()
                            .uri("/api/social/friendships/requests")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
                            .with(mockJwtWithEmail())
                    ).hasStatus(BAD_REQUEST);

                    verifyNoInteractions(sendFriendRequestUseCase);
                }
        ));
    }

    @TestFactory
    Stream<DynamicTest> sendFriendRequest_withServiceThrows_returnsBadRequest() {
        SendFriendRequestDto request = aSendFriendRequestDto();

        return Stream.of(
                new IllegalArgumentException("Sender user not found"),
                new IllegalArgumentException("Recipient user not found"),
                new IllegalStateException("Friend request already exists")
        ).map(ex -> dynamicTest(
                "When service throws %s, controller returns expected status".formatted(
                        ex.getClass().getSimpleName()
                ), () -> {
                    when(jwtUtils.extractUserId(any())).thenReturn(UUID.fromString(UUID.randomUUID().toString()));
                    doThrow(ex).when(sendFriendRequestUseCase)
                            .sendFriendRequest(any(SendFriendRequestCommand.class));

                    var expectedStatus = ex instanceof IllegalArgumentException ? BAD_REQUEST : CONFLICT;

                    assertThat(
                            mockMvcTester.post()
                                    .uri("/api/social/friendships/requests")
                                    .contentType(APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                                    .with(mockJwtWithEmail())
                    ).hasStatus(expectedStatus);
                }
        ));
    }

    @Test
    void acceptFriendRequest_withValidId_returnsOk() {
        UUID friendshipId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(jwtUtils.extractUserId(any())).thenReturn(UUID.fromString(userId.toString()));
        doNothing().when(acceptFriendRequestUseCase).acceptFriendRequest(any());

        assertThat(
                mockMvcTester.put()
                        .uri("/api/social/friendships/{friendshipId}/accept", friendshipId)
                        .with(mockJwtWithUserId(userId))
        ).hasStatus(OK);

        verify(acceptFriendRequestUseCase).acceptFriendRequest(any());
    }

    @Test
    void acceptFriendRequest_whenNotFound_returnsNotFound() {
        UUID friendshipId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(jwtUtils.extractUserId(any())).thenReturn(UUID.fromString(userId.toString()));
        doThrow(new IllegalArgumentException("Friendship not Found"))
                .when(acceptFriendRequestUseCase).acceptFriendRequest(any());

        assertThat(
                mockMvcTester.put()
                        .uri("/api/social/friendships/{friendshipId}/accept", friendshipId)
                        .with(mockJwtWithUserId(userId))
        ).hasStatus(NOT_FOUND);
    }

    @Test
    void getFriends_returnsOk() {
        UUID userId = UUID.randomUUID();

        when(jwtUtils.extractUserId(any())).thenReturn(UUID.fromString(userId.toString()));
        when(getFriendsQuery.getFriends(any())).thenReturn(List.of());

        assertThat(
                mockMvcTester.get()
                        .uri("/api/social/friendships/friends")
                        .with(mockJwtWithUserId(userId))
        ).hasStatus(OK);

        verify(getFriendsQuery).getFriends(any());
    }

    @Test
    void getPendingRequests_returnsOk() {
        UUID userId = UUID.randomUUID();

        when(jwtUtils.extractUserId(any())).thenReturn(UUID.fromString(userId.toString()));
        when(getFriendsQuery.getPendingRequests(any())).thenReturn(List.of());

        assertThat(
                mockMvcTester.get()
                        .uri("/api/social/friendships/requests/pending")
                        .with(mockJwtWithUserId(userId))
        ).hasStatus(OK);

        verify(getFriendsQuery).getPendingRequests(userId);
    }
}
