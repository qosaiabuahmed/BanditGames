import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import {type NotificationDto, NotificationStatus } from '../../types/notification.ts';
import { NotificationService } from '../../services/notificationService.ts';
import { useAuth } from '../../context/AuthContext';
import { useWebSocket } from '../../hooks/useWebSocket.ts';

interface WebSocketNotificationMessage {
    notificationId: string;
    type: string;
    title: string;
    message: string;
    priority: string;
    createdAt: string;
    metadata: Record<string, string>;
}

interface NotificationContextType {
    notifications: NotificationDto[];
    unreadCount: number;
    loading: boolean;
    isConnected: boolean;
    markAsRead: (notificationId: string) => Promise<void>;
    markAllAsRead: () => Promise<void>;
    deleteNotification: (notificationId: string) => Promise<void>;
    refreshNotifications: () => Promise<void>;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

export const NotificationProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [notifications, setNotifications] = useState<NotificationDto[]>([]);
    const [unreadCount, setUnreadCount] = useState<number>(0);
    const [loading, setLoading] = useState<boolean>(true);
    const [isConnected, setIsConnected] = useState<boolean>(false);
    const { isAuthenticated } = useAuth();

    const fetchNotifications = useCallback(async () => {
        if (!isAuthenticated) return;

        try {
            setLoading(true);
            const [notifs, count] = await Promise.all([
                NotificationService.getNotifications('unread'), // Only fetch unread notifications
                NotificationService.getUnreadCount(),
            ]);
            setNotifications(notifs);
            setUnreadCount(count);
        } catch (error) {
            console.error('Failed to fetch notifications:', error);
        } finally {
            setLoading(false);
        }
    }, [isAuthenticated]);

    const handleWebSocketMessage = useCallback((wsNotification: WebSocketNotificationMessage) => {
        const newNotification: NotificationDto = {
            ...wsNotification,
            type: wsNotification.type as any,
            priority: wsNotification.priority as any,
            status: NotificationStatus.UNREAD,
            readAt: null,
        };

        setNotifications((prev) => [newNotification, ...prev]);
        setUnreadCount((prev) => prev + 1);

        if (Notification.permission === 'granted') {
            new Notification(newNotification.title, {
                body: newNotification.message,
                icon: '../../assets/logo.jpeg',
            });
        }
    }, []);

    useWebSocket({
        enabled: isAuthenticated, // Only connect if authenticated
        onMessage: handleWebSocketMessage,
        onConnect: () => {
            console.log('Notification WebSocket connected');
            setIsConnected(true);
            fetchNotifications();
        },
        onDisconnect: () => {
            console.log('Notification WebSocket disconnected');
            setIsConnected(false);
        },
        onError: (error) => {
            console.error('Notification WebSocket error:', error);
        },
    });

    const markAsRead = useCallback(async (notificationId: string) => {
        try {
            await NotificationService.markAsRead(notificationId);

            // Remove from list since we only show unread notifications
            setNotifications((prev) =>
                prev.filter((notif) => notif.notificationId !== notificationId)
            );
            setUnreadCount((prev) => Math.max(0, prev - 1));
        } catch (error) {
            console.error('Failed to mark notification as read:', error);
        }
    }, []);

    const markAllAsRead = useCallback(async () => {
        try {
            await NotificationService.markAllAsRead();

            // Clear all notifications since we only show unread
            setNotifications([]);
            setUnreadCount(0);
        } catch (error) {
            console.error('Failed to mark all notifications as read:', error);
        }
    }, []);

    const deleteNotification = useCallback(async (notificationId: string) => {
        try {
            await NotificationService.deleteNotification(notificationId);

            // Update local state
            setNotifications((prev) => {
                const deletedNotif = prev.find((n) => n.notificationId === notificationId);
                if (deletedNotif && deletedNotif.status === NotificationStatus.UNREAD) {
                    setUnreadCount((count) => Math.max(0, count - 1));
                }
                return prev.filter((notif) => notif.notificationId !== notificationId);
            });
        } catch (error) {
            console.error('Failed to delete notification:', error);
        }
    }, []);

    useEffect(() => {
        if (isAuthenticated) {
            fetchNotifications();

            // Request browser notification permission
            if ('Notification' in window && Notification.permission === 'default') {
                Notification.requestPermission();
            }
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isAuthenticated]); // Re-fetch when authentication status changes

    const value: NotificationContextType = {
        notifications,
        unreadCount,
        loading,
        isConnected,
        markAsRead,
        markAllAsRead,
        deleteNotification,
        refreshNotifications: fetchNotifications,
    };

    return <NotificationContext.Provider value={value}>{children}</NotificationContext.Provider>;
};

export const useNotifications = (): NotificationContextType => {
    const context = useContext(NotificationContext);
    if (!context) {
        throw new Error('useNotifications must be used within NotificationProvider');
    }
    return context;
};
