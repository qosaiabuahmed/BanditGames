package be.kdg.banditgamesbackend.social.adapter.out.persistence;

import be.kdg.banditgamesbackend.common.dto.UserBasicInfo;
import be.kdg.banditgamesbackend.social.adapter.in.dto.FriendDto;
import be.kdg.banditgamesbackend.social.domain.Friendship;
import be.kdg.banditgamesbackend.social.port.out.UserLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FriendshipMapper {

    private final UserLookupPort userLookupPort;

    public FriendDto toFriendDto(Friendship friendship, UUID currentUserId) {
        UUID friendId = getFriendId(friendship, currentUserId);
        UserBasicInfo userInfo = getUserInfo(friendId);

        return new FriendDto(
                friendship.getFriendshipId(),
                friendId,
                userInfo.username(),
                userInfo.playerTag(),
                friendship.getStatus().name(),
                friendship.getCreatedAt(),
                friendship.getAcceptedAt()
        );
    }

    public FriendDto toPendingRequestDto(Friendship friendship) {
        UserBasicInfo senderInfo = getUserInfo(friendship.getUserId());

        return new FriendDto(
                friendship.getFriendshipId(),
                friendship.getUserId(),
                senderInfo.username(),
                senderInfo.playerTag(),
                friendship.getStatus().name(),
                friendship.getCreatedAt(),
                null
        );
    }

    public FriendDto toSentRequestDto(Friendship friendship) {
        UserBasicInfo recipientInfo = getUserInfo(friendship.getFriendId());

        return new FriendDto(
                friendship.getFriendshipId(),
                friendship.getFriendId(),
                recipientInfo.username(),
                recipientInfo.playerTag(),
                friendship.getStatus().name(),
                friendship.getCreatedAt(),
                null
        );
    }

    Friendship toDomain(FriendshipJpaEntity entity) {
        return Friendship.hydrate(
                entity.getFriendshipId(),
                entity.getUserId(),
                entity.getFriendId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getAcceptedAt()
        );
    }

    FriendshipJpaEntity toJpaEntity(Friendship friendship) {
        return new FriendshipJpaEntity(
                friendship.getFriendshipId(),
                friendship.getUserId(),
                friendship.getFriendId(),
                friendship.getStatus(),
                friendship.getCreatedAt(),
                friendship.getAcceptedAt()
        );
    }

    private UUID getFriendId(Friendship friendship, UUID currentUserId) {
        return friendship.getUserId().equals(currentUserId)
                ? friendship.getFriendId()
                : friendship.getUserId();
    }

    private UserBasicInfo getUserInfo(UUID userId) {
        return userLookupPort.findUserByUserId(userId)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("User not found: %s", userId)
                ));
    }

}