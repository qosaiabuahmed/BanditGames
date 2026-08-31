import {useCallback, useEffect, useRef} from "react";
import SockJS from 'sockjs-client';
import {Client, type StompSubscription} from "@stomp/stompjs";
import {getAccessToken} from "../utils/tokenUtils";

const WS_URL = import.meta.env.VITE_API_BASE_URL?.replace('/api', '/ws');

interface UseWebSocketProps {
    enabled?: boolean; // Only connect if enabled
    onMessage: (message: WebSocketNotificationMessage) => void;
    onConnect?: () => void;
    onDisconnect?: () => void;
    onError?: (error: Error | StompFrame) => void;
}

interface WebSocketNotificationMessage {
    notificationId: string;
    type: string;
    title: string;
    message: string;
    priority: string;
    createdAt: string;
    metadata: Record<string, string>;
}

interface StompFrame {
    command: string;
    headers: Record<string, string>;
    body: string;
}

export const useWebSocket = ({
                                 enabled = true,
                                 onMessage,
                                 onConnect,
                                 onDisconnect,
                                 onError,
                             }: UseWebSocketProps) => {
    const clientRef = useRef<Client | null>(null);
    const subscriptionRef = useRef<StompSubscription | null>(null);
    const reconnectTimeoutRef = useRef<number | null>(null);

    const connect = useCallback(async () => {
        const token = await getAccessToken();

        if (!token) {
            console.log('No token available, skipping WebSocket connection...');
            return;
        }

        const socket = new SockJS(WS_URL);

        const client = new Client({
            webSocketFactory: () => socket as any,
            connectHeaders: {
                Authorization: `Bearer ${token}`,
            },
            debug: (str) => {
                if (import.meta.env.DEV) {
                    console.log('[STOMP]', str);
                }
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        client.onConnect = () => {
            console.log('Websocket Connected!');
            onConnect?.();

            subscriptionRef.current = client.subscribe(
                '/user/queue/notifications',
                (message) => {
                    try {
                        const notification: WebSocketNotificationMessage = JSON.parse(message.body);
                        console.log('Received notification: ', notification);
                        onMessage(notification);
                    } catch (e) {
                        console.error('failed to parse notification: ', e);
                    }
                });
        };

        client.onStompError = (frame) => {
            console.error('STOMP error frame: ', frame);
            onError?.(new Error(`STOMP error: ${frame.body || 'no body'}`));
        };

        client.onWebSocketError = (event) => {
            console.error('WebSocket error:', event);
            onError?.(event as Error);
        };

        client.onWebSocketClose = () => {
            console.log('WebSocket closed');
            onDisconnect?.();
        };

        client.activate();
        clientRef.current = client;
    }, [onMessage, onConnect, onDisconnect, onError]);

    const disconnect = useCallback(() => {
        if (subscriptionRef.current) {
            subscriptionRef.current.unsubscribe();
            subscriptionRef.current = null;
        }

        if (clientRef.current) {
            clientRef.current.deactivate().catch((err) => {
                console.error('Error deactivating WebSocket: ', err)
            });
            clientRef.current = null;
        }

        if (reconnectTimeoutRef.current) {
            clearTimeout(reconnectTimeoutRef.current);
            reconnectTimeoutRef.current = null;
        }
    }, []);

    useEffect(() => {
        if (!enabled) {
            console.log('WebSocket disabled - skipping connection');
            return;
        }

        connect();

        return () => {
            disconnect();
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [enabled]); // Connect when enabled changes

    return {disconnect, reconnect: connect}
};