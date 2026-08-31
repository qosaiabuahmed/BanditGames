package be.kdg.banditgamesbackend.social.port.in;

import org.instancio.Instancio;
import org.instancio.Model;

import java.util.UUID;

import static org.instancio.Select.field;

public class SendFriendRequestCommandFixtures {

    public static final Model<SendFriendRequestCommand> SEND_FRIEND_REQUEST_MODEL =
            Instancio.of(SendFriendRequestCommand.class)
                    .toModel();

    public static SendFriendRequestCommand aSendFriendRequestCommand() {
        return Instancio.create(SEND_FRIEND_REQUEST_MODEL);
    }

    public static SendFriendRequestCommand aSendFriendRequestCommandWithFromUserId(UUID fromUserId) {
        return Instancio.of(SEND_FRIEND_REQUEST_MODEL)
                .set(field(SendFriendRequestCommand::fromUserId), fromUserId)
                .create();
    }

    public static SendFriendRequestCommand aSendFriendRequestCommandWithToUserId(UUID toUserId) {
        return Instancio.of(SEND_FRIEND_REQUEST_MODEL)
                .set(field(SendFriendRequestCommand::toUserId), toUserId)
                .create();
    }

    public static SendFriendRequestCommand aSendFriendRequestCommandWithBothUsers(UUID fromUserId, UUID toUserId) {
        return Instancio.of(SEND_FRIEND_REQUEST_MODEL)
                .set(field(SendFriendRequestCommand::fromUserId), fromUserId)
                .set(field(SendFriendRequestCommand::toUserId), toUserId)
                .create();
    }

    public static SendFriendRequestCommand anEmptySendFriendRequestCommand() {
        return new SendFriendRequestCommand(null, null);
    }
}
