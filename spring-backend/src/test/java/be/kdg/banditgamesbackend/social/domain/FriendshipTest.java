package be.kdg.banditgamesbackend.social.domain;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class FriendshipTest {

    @Test
    void sendRequest_ShouldCreatePendingFriendship() {
        UUID from = UUID.randomUUID();
        UUID to = UUID.randomUUID();

        Friendship friendship = Friendship.sendRequest(from, to);

        assertThat(friendship.getUserId()).isEqualTo(from);
        assertThat(friendship.getFriendId()).isEqualTo(to);
        assertThat(friendship.getStatus()).isEqualTo(FriendshipStatus.PENDING);
        assertThat(friendship.isPending()).isTrue();
    }

    @Test
    void sendRequest_ToSelf_ShouldThrowException() {
        UUID userId = UUID.randomUUID();
        assertThatThrownBy(() -> Friendship.sendRequest(userId, userId))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void accept_WhenPending_ShouldUpdateStatus() {
        Friendship friendship = Friendship.sendRequest(UUID.randomUUID(), UUID.randomUUID());
        
        friendship.accept();

        assertThat(friendship.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
        assertThat(friendship.isAccepted()).isTrue();
        assertThat(friendship.getAcceptedAt()).isNotNull();
    }

    @Test
    void accept_WhenAlreadyAccepted_ShouldThrowException() {
        Friendship friendship = Friendship.sendRequest(UUID.randomUUID(), UUID.randomUUID());
        friendship.accept();

        assertThatThrownBy(friendship::accept).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void decline_WhenPending_ShouldUpdateStatus() {
        Friendship friendship = Friendship.sendRequest(UUID.randomUUID(), UUID.randomUUID());
        
        friendship.decline();

        assertThat(friendship.getStatus()).isEqualTo(FriendshipStatus.DECLINED);
    }

    @Test
    void block_ShouldUpdateStatus() {
        Friendship friendship = Friendship.sendRequest(UUID.randomUUID(), UUID.randomUUID());
        
        friendship.block();

        assertThat(friendship.getStatus()).isEqualTo(FriendshipStatus.BLOCKED);
    }
}
