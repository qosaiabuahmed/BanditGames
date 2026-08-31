package be.kdg.banditgamesbackend.social.port.in;


import be.kdg.banditgamesbackend.social.adapter.in.dto.FriendDto;

import java.util.List;
import java.util.UUID;

public interface GetFriendsQuery {
    List<FriendDto> getFriends(UUID userId);
    List<FriendDto> getPendingRequests(UUID userId);
    List<FriendDto> getSentRequests(UUID userId);
}
