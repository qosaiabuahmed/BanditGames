package be.kdg.banditgamesbackend.gamemetadata.core;

import be.kdg.banditgamesbackend.common.messaging.PlatformEventPublisher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class GameService {

    private final PlatformEventPublisher platformEventPublisher;
}
