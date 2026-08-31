package be.kdg.banditgamesbackend.match.adapter.out.persistence.friendship;

import be.kdg.banditgamesbackend.match.domain.FriendshipProjection;
import be.kdg.banditgamesbackend.match.port.out.LoadFriendshipProjectionPort;
import be.kdg.banditgamesbackend.match.port.out.SaveFriendshipProjectionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FriendshipProjectionAdapter implements LoadFriendshipProjectionPort, SaveFriendshipProjectionPort {

    private final FriendshipProjectionJpaRepository jpaRepository;

    @Override
    public boolean areFriends(UUID userId1, UUID userId2) {
        return jpaRepository.findFriendship(userId1, userId2).isPresent();
    }

    @Override
    public Optional<FriendshipProjection> findFriendship(UUID userId1, UUID userId2) {
        return jpaRepository.findFriendship(userId1, userId2)
                .map(entity -> new FriendshipProjection(
                        entity.getUserId1(),
                        entity.getUserId2(),
                        entity.getCreatedAt()
                ));
    }

    @Override
    @Transactional
    public void save(FriendshipProjection projection) {
        FriendshipProjectionJpaEntity entity = new FriendshipProjectionJpaEntity(
                projection.getUserId1(),
                projection.getUserId2(),
                projection.getCreatedAt()
        );
        jpaRepository.save(entity);
    }

    @Override
    @Transactional
    public void deleteFriendship(UUID userId1, UUID userId2) {
        jpaRepository.deleteFriendship(userId1, userId2);
    }
}