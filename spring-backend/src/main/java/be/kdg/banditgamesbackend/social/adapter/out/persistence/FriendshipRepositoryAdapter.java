package be.kdg.banditgamesbackend.social.adapter.out.persistence;

import be.kdg.banditgamesbackend.social.domain.Friendship;
import be.kdg.banditgamesbackend.social.port.out.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FriendshipRepositoryAdapter implements FriendshipRepository {

    private final FriendshipJpaRepository jpaRepository;
    private final FriendshipMapper mapper;

    @Override
    public Friendship save(Friendship friendship) {
        FriendshipJpaEntity entity = mapper.toJpaEntity(friendship);
        FriendshipJpaEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Friendship> findById(UUID friendshipId) {
        return jpaRepository.findById(friendshipId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Friendship> findByUserAndFriend(UUID userId, UUID friendId) {
        return jpaRepository.findByUserAndFriend(userId, friendId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Friendship> findFriendsByUserId(UUID userId) {
        return jpaRepository.findAcceptedFriendshipsByUserId(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Friendship> findPendingRequestsByUserId(UUID userId) {
        return jpaRepository.findPendingRequestsForUser(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Friendship> findSentRequestsByUserId(UUID userId) {
        return jpaRepository.findSendRequestsByUserId(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean areFriends(UUID userId, UUID friendId) {
        return jpaRepository.areFriends(userId, friendId);
    }

    @Override
    public void delete(UUID friendshipId) {
        jpaRepository.deleteById(friendshipId);
    }
}
