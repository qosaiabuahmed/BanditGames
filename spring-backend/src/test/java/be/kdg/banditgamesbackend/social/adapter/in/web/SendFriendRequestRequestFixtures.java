package be.kdg.banditgamesbackend.social.adapter.in.web;

import be.kdg.banditgamesbackend.social.adapter.in.dto.SendFriendRequestDto;
import org.instancio.Instancio;
import org.instancio.Model;

import java.util.UUID;

import static org.instancio.Select.field;

public class SendFriendRequestRequestFixtures {

    public static final Model<SendFriendRequestDto> SEND_FRIEND_REQUEST_DTO_MODEL =
            Instancio.of(SendFriendRequestDto.class)
                    .toModel();

    public static SendFriendRequestDto aSendFriendRequestDto() {
        return Instancio.create(SEND_FRIEND_REQUEST_DTO_MODEL);
    }

    public static SendFriendRequestDto aSendFriendRequestWithToUserId(UUID toUserId) {
        return Instancio.of(SEND_FRIEND_REQUEST_DTO_MODEL)
                .set(field(SendFriendRequestDto::toUserId), toUserId)
                .create();
    }

    public static SendFriendRequestDto anEmptySendFriendRequestDto() {
        return new SendFriendRequestDto(null);
    }
}
