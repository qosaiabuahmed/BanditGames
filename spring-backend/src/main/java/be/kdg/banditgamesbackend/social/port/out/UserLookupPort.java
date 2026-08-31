package be.kdg.banditgamesbackend.social.port.out;

import be.kdg.banditgamesbackend.common.dto.UserBasicInfo;
import be.kdg.banditgamesbackend.social.port.in.UserSearchDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserLookupPort {
    Optional<UserBasicInfo> findUserByUserId(UUID userId);
    List<UserBasicInfo> findUsersByIds(List<UUID> userIds);
    boolean userExists(UUID userId);
    List<UserSearchDto> searchUsers(String query);
    Optional<UUID> findUserIdByEmail(String email);
}
