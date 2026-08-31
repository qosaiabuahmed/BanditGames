package be.kdg.banditgamesbackend.user.port.in;

import be.kdg.banditgamesbackend.user.domain.User;

public interface UpdateUserProfileUseCase {
    User updateUserProfile(UpdateUserProfileCommand command);
}