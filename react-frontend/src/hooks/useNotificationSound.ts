import { useEffect, useRef } from 'react';
import { useNotifications } from '../components/notifications/NotificationContent';

export const useNotificationSound = (soundUrl: string = '/notification.mp3') => {
    const { notifications } = useNotifications();
    const audioRef = useRef<HTMLAudioElement | null>(null);
    const previousCountRef = useRef<number>(0);

    useEffect(() => {
        // Initialize audio element
        audioRef.current = new Audio(soundUrl);
        audioRef.current.volume = 0.5;
    }, [soundUrl]);

    useEffect(() => {
        const currentCount = notifications.filter((n) => n.status === 'UNREAD').length;

        // Play sound only when count increases (new notification)
        if (currentCount > previousCountRef.current && audioRef.current) {
            audioRef.current.play().catch((error) => {
                console.error('Failed to play notification sound:', error);
            });
        }

        previousCountRef.current = currentCount;
    }, [notifications]);
};