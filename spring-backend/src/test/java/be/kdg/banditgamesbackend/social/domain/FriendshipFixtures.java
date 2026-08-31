package be.kdg.banditgamesbackend.social.domain;

import be.kdg.banditgamesbackend.util.AbstractFixtureBuilder;
import org.instancio.Instancio;
import org.instancio.Model;

import java.time.LocalDateTime;
import java.util.UUID;


public class FriendshipFixtures {

    public static final Model<Friendship> FRIENDSHIP_MODEL =
            Instancio.of(Friendship.class)
                    .toModel();

    public static Friendship aFriendship() {
        return Instancio.create(Friendship.class);
    }

    public static Friendship aPendingFriendship() {
        UUID fromUser = UUID.randomUUID();
        UUID toUser = UUID.randomUUID();
        return Friendship.sendRequest(fromUser, toUser);
    }

    public static Friendship aPendingFriendshipBetween(UUID fromUser, UUID toUser) {
        return Friendship.sendRequest(fromUser, toUser);
    }

    public static FriendshipBuilder aFriendshipBuilder() {
        return new FriendshipBuilder();
    }


    public static class FriendshipBuilder extends AbstractFixtureBuilder<Friendship, FriendshipBuilder> {

        public FriendshipBuilder withUserId(UUID userId) {
            return setField(Friendship::getUserId, userId);
        }

        public FriendshipBuilder withFriendId(UUID friendId) {
            return setField(Friendship::getFriendId, friendId);
        }

        public FriendshipBuilder withStatus(FriendshipStatus status) {
            return setField(Friendship::getStatus, status);
        }

        public FriendshipBuilder withCreatedAt(LocalDateTime createdAt) {
            return setField(Friendship::getCreatedAt, createdAt);
        }

        public FriendshipBuilder pending() {
            return withStatus(FriendshipStatus.PENDING);
        }

        public FriendshipBuilder accepted() {
            return withStatus(FriendshipStatus.ACCEPTED);
        }

        @Override
        public Friendship build() {
            return buildInternal(FRIENDSHIP_MODEL);
        }

        @Override
        public FriendshipBuilder self() {
            return this;
        }
    }
}
