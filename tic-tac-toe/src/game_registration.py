import pika
import json
import os
from datetime import datetime


def register_with_platform():
    try:
        rabbit_host = os.getenv("RABBIT_HOST", "localhost")
        rabbit_port = int(os.getenv("RABBIT_PORT", "5672"))
        rabbit_user = os.getenv("RABBIT_USER", "guest")
        rabbit_password = os.getenv("RABBIT_PASSWORD", "guest")
        game_id = os.getenv("GAME_REGISTRATION_ID")
        frontend_url = os.getenv("GAME_FRONTEND_URL", "http://localhost:8189")
        exchange = os.getenv("PLATFORM_GAME_EXCHANGE", "game.events.exchange")

        if not game_id:
            print("  GAME_REGISTRATION_ID not set - running in standalone mode")
            print("   (Game will work normally, just won't register with platform)")
            return

        print("=" * 60)
        print(" TIC-TAC-TOE - Platform Registration")
        print("=" * 60)

        # Platform expects achievements with just code and description
        achievements = [
            {
                "code": "first_win_ttt",
                "description": "Win your first Tic-Tac-Toe game"
            },
            {
                "code": "perfect_game_ttt",
                "description": "Win in minimum moves (5 moves for X, 6 for O)"
            },
            {
                "code": "diagonal_master",
                "description": "Win with a diagonal line"
            },
            {
                "code": "speedster_ttt",
                "description": "Win within 30 seconds"
            },
            {
                "code": "center_control",
                "description": "Win while controlling the center square"
            }
        ]

        now = datetime.now()
        registration_event = {
            "eventType": "GameRegisteredEvent",
            "timestamp": now.isoformat(),
            "gameId": game_id,
            "name": "Tic-Tac-Toe",
            "description": "Classic 3x3 Tic-Tac-Toe with AI opponents. Get three in a row to win!",
            "status": None,
            "registeredAt": now.isoformat(),
            "rules": {
                "rulesText": "Get three marks in a row to win. Players alternate placing X or O on empty squares. First to get three in a row (horizontal, vertical, or diagonal) wins. Draw if board fills with no winner.",
                "rulesUrl": f"{frontend_url}/rules",
                "summary": "Three in a row wins"
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
                "designer": "Unknown",
                "publisher": "Traditional",
                "releaseYear": 1952,
                "estimatedDurationMinutes": 5,
                "complexity": "Easy",
                "frontendUrl": frontend_url,
                "pictureUrl": None
            }
        }

        print(f" Connecting to RabbitMQ at {rabbit_host}:{rabbit_port}...")

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

        # Declare the exchange
        channel.exchange_declare(
            exchange=exchange,
            exchange_type='topic',
            durable=True
        )

        # Publish registration event
        routing_key = "game.tictactoe.registered"

        channel.basic_publish(
            exchange=exchange,
            routing_key=routing_key,
            body=json.dumps(registration_event),
            properties=pika.BasicProperties(
                delivery_mode=2,
                content_type='application/json'
            )
        )

        print(" Successfully registered Tic-Tac-Toe with platform!")
        print(f"   Exchange: {exchange}")
        print(f"   Routing Key: {routing_key}")
        print(f"   Frontend URL: {frontend_url}")
        print("=" * 60)

        connection.close()

    except pika.exceptions.AMQPConnectionError as e:
        print("=" * 60)
        print("  Could not connect to RabbitMQ for platform registration")
        print(f"   Reason: {e}")
        print("   Game will run in STANDALONE mode")
        print("   All game features work normally")
        print("   Just won't appear in platform catalog")
        print("=" * 60)

    except Exception as e:
        print("=" * 60)
        print("  Game registration failed (unexpected error)")
        print(f"   Reason: {e}")
        print("   Game will run in STANDALONE mode")
        print("=" * 60)
