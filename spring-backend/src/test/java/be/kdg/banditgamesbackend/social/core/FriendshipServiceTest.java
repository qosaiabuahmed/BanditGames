package be.kdg.banditgamesbackend.social.core;

import be.kdg.banditgamesbackend.social.domain.Friendship;
import be.kdg.banditgamesbackend.social.domain.FriendshipStatus;
import be.kdg.banditgamesbackend.social.port.in.AcceptFriendRequestCommand;
import be.kdg.banditgamesbackend.social.port.in.SendFriendRequestCommand;
import be.kdg.banditgamesbackend.social.port.out.FriendshipRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static be.kdg.banditgamesbackend.social.domain.FriendshipFixtures.aPendingFriendshipBetween;
import static be.kdg.banditgamesbackend.social.port.in.SendFriendRequestCommandFixtures.aSendFriendRequestCommand;
import static be.kdg.banditgamesbackend.social.port.in.SendFriendRequestCommandFixtures.aSendFriendRequestCommandWithBothUsers;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceTest {

    @InjectMocks
    FriendshipService friendshipService;

    @Mock
    FriendshipRepository friendshipRepository;

    @Mock
    FriendshipValidator validator;

    @Captor
    ArgumentCaptor<Friendship> friendshipCaptor;

    @Test
    void sendFriendRequest_whenSenderNotFound_throwsExpected() {
        SendFriendRequestCommand command = aSendFriendRequestCommand();

        doThrow(new IllegalArgumentException("Sender user not found: " + command.fromUserId()))
                .when(validator).validateSendFriendRequest(command);

        assertThatThrownBy(() -> friendshipService.sendFriendRequest(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sender user not found: " + command.fromUserId());

        verifyNoInteractions(friendshipRepository);
    }

    @Test
    void sendFriendRequest_whenRecipientNotFound_throwsExpected() {
        SendFriendRequestCommand command = aSendFriendRequestCommand();

        doThrow(new IllegalArgumentException("Receiver user not found: " + command.toUserId()))
                .when(validator).validateSendFriendRequest(command);

        assertThatThrownBy(() -> friendshipService.sendFriendRequest(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Receiver user not found: " + command.toUserId());

        verifyNoInteractions(friendshipRepository);
    }

    @Test
    void sendFriendRequest_whenFriendshipAlreadyExists_throwsExpected() {
        SendFriendRequestCommand command = aSendFriendRequestCommand();

        doThrow(new IllegalStateException("Friend request already exists: " + command.toUserId()))
                .when(validator).validateSendFriendRequest(command);

        assertThatThrownBy(() -> friendshipService.sendFriendRequest(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Friend request already exists: " + command.toUserId());

        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void sendFriendRequest_whenReverseFriendshipExists_throwsExpected() {
        SendFriendRequestCommand command = aSendFriendRequestCommand();

        doThrow(new IllegalStateException("already sent you a friend request"))
                .when(validator).validateSendFriendRequest(command);

        assertThatThrownBy(() -> friendshipService.sendFriendRequest(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already sent you a friend request");
    }

    @Test
    void sendFriendRequest_whenValid_savesFriendship() {
        UUID fromUser = UUID.randomUUID();
        UUID toUser = UUID.randomUUID();
        SendFriendRequestCommand command = aSendFriendRequestCommandWithBothUsers(fromUser, toUser);

        doNothing().when(validator).validateSendFriendRequest(command);
        when(friendshipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatCode(() -> friendshipService.sendFriendRequest(command))
                .doesNotThrowAnyException();

        verify(friendshipRepository).save(friendshipCaptor.capture());
        assertThat(friendshipCaptor.getValue())
                .returns(fromUser, Friendship::getUserId)
                .returns(toUser, Friendship::getFriendId)
                .returns(FriendshipStatus.PENDING, Friendship::getStatus);
    }


    @Test
    void acceptFriendRequest_whenFriendshipNotFound_throwsExpected() {
        UUID friendshipId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AcceptFriendRequestCommand command = new AcceptFriendRequestCommand(friendshipId, userId);

        when(friendshipRepository.findById(friendshipId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendshipService.acceptFriendRequest(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Friendship not found: " + friendshipId);
    }

    @Test
    void acceptFriendRequest_whenNotRecipient_throwsExpected() {
        UUID fromUser = UUID.randomUUID();
        UUID toUser = UUID.randomUUID();
        UUID wrongUser = UUID.randomUUID();

        Friendship friendship = aPendingFriendshipBetween(fromUser, toUser);
        AcceptFriendRequestCommand command = new AcceptFriendRequestCommand(
                friendship.getFriendshipId(), wrongUser);

        when(friendshipRepository.findById(friendship.getFriendshipId()))
                .thenReturn(Optional.of(friendship));

        doThrow(new IllegalStateException("You can only accept friends requests sent to you."))
                .when(validator).validateFriendshipAuthorization(friendship, wrongUser, "accept");

        assertThatThrownBy(() -> friendshipService.acceptFriendRequest(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("You can only accept friends requests sent to you.");
    }
}
