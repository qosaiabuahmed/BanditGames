"""
Game Registration Module - Connect Four
Publishes registration event to platform on startup.

SAFETY:
- Wrapped in try-catch - if registration fails, game continues normally
- Game works standalone even if platform is unavailable
- No changes to existing game logic
"""
import pika
import json
import os
from datetime import datetime


def register_with_platform():
    """
    Publish game registration event to platform via RabbitMQ.
    Called once on application startup.

    SAFETY: If this fails, the game still works normally.
    """
    try:
        # Get config from environment variables
        rabbit_host = os.getenv("RABBIT_HOST", "localhost")
        rabbit_port = int(os.getenv("RABBIT_PORT", "5672"))
        rabbit_user = os.getenv("RABBIT_USER", "guest")
        rabbit_password = os.getenv("RABBIT_PASSWORD", "guest")
        game_id = os.getenv("GAME_REGISTRATION_ID")
        frontend_url = os.getenv("GAME_FRONTEND_URL", "http://localhost:8000")
        exchange = os.getenv("PLATFORM_GAME_EXCHANGE", "game.events.exchange")

        # If no registration ID is set, skip registration (run standalone)
        if not game_id:
            print("GAME_REGISTRATION_ID not set - running in standalone mode")
            print("   (Game will work normally, just won't register with platform)")
            return

        print("=" * 60)
        print("CONNECT FOUR - Platform Registration")
        print("=" * 60)

        # Platform expects achievements with just code and description
        achievements = [
            {
                "code": "first_win_c4",
                "description": "Win your first Connect Four game"
            },
            {
                "code": "perfect_game_c4",
                "description": "Win without opponent scoring any points"
            },
            {
                "code": "comeback_king",
                "description": "Win after being behind by 10+ points"
            },
            {
                "code": "speedster",
                "description": "Win in under 15 moves"
            },
            {
                "code": "diagonal_master_c4",
                "description": "Win with a diagonal connection"
            }
        ]

        # Create registration event matching platform format
        now = datetime.now()
        registration_event = {
            "eventType": "GameRegisteredEvent",
            "timestamp": now.isoformat(),
            "gameId": game_id,
            "name": "Connect Four",
            "description": "Classic Connect Four game with AI opponents and ML predictions. Drop pieces into columns to connect four in a row!",
            "status": None,
            "registeredAt": now.isoformat(),
            "rules": {
                "rulesText": "Connect four pieces in a row (horizontally, vertically, or diagonally) to win. Players take turns dropping pieces into columns. The game uses a scoring system where connecting pieces earns points.",
                "rulesUrl": f"{frontend_url}/rules",
                "summary": "Connect four in a row to win"
            },
            "achievements": achievements,
            "playerConfigurations": {
                "minPlayers": 2,
                "maxPlayers": 2,
                "supportedPlayerTypes": ["HUMAN", "AI"]
            },
            "renderType": "EXTERNAL_IFRAME",
            "metaData": {
                "category": "Strategy",
                "theme": "Classic",
                "designer": "Howard Wexler",
                "publisher": "Milton Bradley",
                "releaseYear": 1974,
                "estimatedDurationMinutes": 10,
                "complexity": "Easy",
                "frontendUrl": frontend_url,
                "pictureUrl": None
            }
        }

        print(f"Connecting to RabbitMQ at {rabbit_host}:{rabbit_port}...")

        # Connect to RabbitMQ
        credentials = pika.PlainCredentials(rabbit_user, rabbit_password)
        parameters = pika.ConnectionParameters(
            host=rabbit_host,
            port=rabbit_port,
            credentials=credentials,
            heartbeat=600,
            blocked_connection_timeout=300
        )

        connection = pika.BlockingConnection(parameters)
        channel = connection.channel()

        # Declare the exchange (idempotent - safe to call multiple times)
        channel.exchange_declare(
            exchange=exchange,
            exchange_type='topic',
            durable=True
        )

        # Publish registration event
        routing_key = "game.connectfour.registered"

        channel.basic_publish(
            exchange=exchange,
            routing_key=routing_key,
            body=json.dumps(registration_event),
            properties=pika.BasicProperties(
                delivery_mode=2,  # Persistent message
                content_type='application/json'
            )
        )

        print("Successfully registered Connect Four with platform!")
        print(f"   Exchange: {exchange}")
        print(f"   Routing Key: {routing_key}")
        print(f"   Frontend URL: {frontend_url}")
        print("=" * 60)

        connection.close()

    except pika.exceptions.AMQPConnectionError as e:
        # RabbitMQ not available - this is OK, game works standalone
        print("=" * 60)
        print("  Could not connect to RabbitMQ for platform registration")
        print(f"   Reason: {e}")
        print("   ✓ Game will run in STANDALONE mode")
        print("   ✓ All game features work normally")
        print("   ✓ Just won't appear in platform catalog")
        print("=" * 60)

    except Exception as e:
        # Any other error - log it but continue
        print("=" * 60)
        print(" Game registration failed (unexpected error)")
        print(f"   Reason: {e}")
        print("   ✓ Game will run in STANDALONE mode")
        print("=" * 60)