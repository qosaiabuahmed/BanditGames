package be.kdg.banditgamesbackend.social.adapter.out;

import be.kdg.banditgamesbackend.common.dto.UserBasicInfo;
import be.kdg.banditgamesbackend.social.adapter.out.persistence.SocialUserProjectionJpaEntity;
import be.kdg.banditgamesbackend.social.adapter.out.persistence.SocialUserProjectionRepository;
import be.kdg.banditgamesbackend.social.port.in.UserSearchDto;
import be.kdg.banditgamesbackend.social.port.out.UserLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserLookupAdapter implements UserLookupPort {

    private final SocialUserProjectionRepository projectionRepository;

    @Override
    public Optional<UserBasicInfo> findUserByUserId(UUID userId) {
        return projectionRepository.findById(userId)
                .map(entity -> new UserBasicInfo(
                        entity.getUserId(),
                        entity.getUsername(),
                        entity.getEmail()
                ));
    }

    @Override
    public List<UserBasicInfo> findUsersByIds(List<UUID> userIds) {
        return List.of();
    }

    @Override
    public boolean userExists(UUID userId) {
        return projectionRepository.existsById(userId);
    }

    @Override
    public List<UserSearchDto> searchUsers(String query) {
        List<SocialUserProjectionJpaEntity> entities = projectionRepository
                .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
        return entities.stream()
                .map(entity -> new UserSearchDto(
                        entity.getUserId(),
                        entity.getUsername(),
                        entity.getEmail(),
                        null
                )).toList();
    }

    @Override
    public Optional<UUID> findUserIdByEmail(String email) {
        return projectionRepository.findByEmail(email)
                .map(SocialUserProjectionJpaEntity::getUserId);
    }
}
