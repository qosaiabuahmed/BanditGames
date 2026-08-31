import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useNotifications } from './NotificationContent.tsx';
import {
    type NotificationDto,
    NotificationType,
    NotificationStatus,
    NotificationPriority,
} from '../../types/notification';

interface NotificationItemProps {
    notification: NotificationDto;
    onClose?: () => void;
}

const NotificationItem: React.FC<NotificationItemProps> = ({
                                                               notification,
                                                               onClose,
                                                           }) => {
    const navigate = useNavigate();
    const { markAsRead, deleteNotification } = useNotifications();
    const [isHovered, setIsHovered] = useState(false);

    const handleClick = async () => {
        if (notification.status === NotificationStatus.UNREAD) {
            await markAsRead(notification.notificationId);
        }

        const action = notification.metadata?.action;
        const actionUrl = notification.metadata?.actionUrl;

        if (actionUrl) {
            navigate(actionUrl);
            onClose?.();
        } else if (action === 'view_friend_requests' || action === 'view_friends') {
            navigate('/friends');
            onClose?.();
        } else if (action === 'join_lobby') {
            const lobbyId = notification.metadata?.lobbyId;
            if (lobbyId) {
                navigate(`/lobby/${lobbyId}`);
                onClose?.();
            }
        } else if (action === 'join_match') {
            const matchId = notification.metadata?.matchId;
            if (matchId) {
                navigate(`/match/${matchId}`);
                onClose?.();
            }
        } else if (action === 'view_achievements') {
            navigate('/profile');
            onClose?.();
        }
    };

    const handleDelete = async (e: React.MouseEvent) => {
        e.stopPropagation();
        await deleteNotification(notification.notificationId);
    };

    const getIcon = () => {
        const iconStyle = { width: '24px', height: '24px' };

        switch (notification.type) {
            case NotificationType.FRIEND_REQUEST_RECEIVED:
                return (
                    <svg style={{ ...iconStyle, color: '#3b82f6' }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                );
            case NotificationType.FRIEND_REQUEST_ACCEPTED:
                return (
                    <svg style={{ ...iconStyle, color: '#10b981' }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                );
            case NotificationType.GAME_INVITATION_RECEIVED:
                return (
                    <svg style={{ ...iconStyle, color: '#8b5cf6' }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 4a2 2 0 114 0v1a1 1 0 001 1h3a1 1 0 011 1v3a1 1 0 01-1 1h-1a2 2 0 100 4h1a1 1 0 011 1v3a1 1 0 01-1 1h-3a1 1 0 01-1-1v-1a2 2 0 10-4 0v1a1 1 0 01-1 1H7a1 1 0 01-1-1v-3a1 1 0 00-1-1H4a2 2 0 110-4h1a1 1 0 001-1V7a1 1 0 011-1h3a1 1 0 001-1V4z" />
                    </svg>
                );
            case NotificationType.MATCH_STARTED:
                return (
                    <svg style={{ ...iconStyle, color: '#f97316' }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                    </svg>
                );
            case NotificationType.ACHIEVEMENT_UNLOCKED:
                return (
                    <svg style={{ ...iconStyle, color: '#eab308' }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z" />
                    </svg>
                );
            default:
                return (
                    <svg style={{ ...iconStyle, color: '#6b7280' }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                );
        }
    };

    const formatTime = (timestamp: string) => {
        const date = new Date(timestamp);
        const now = new Date();
        const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

        if (diffInSeconds < 60) return 'just now';
        if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}m ago`;
        if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)}h ago`;
        if (diffInSeconds < 604800) return `${Math.floor(diffInSeconds / 86400)}d ago`;

        return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    };

    const isUnread = notification.status === NotificationStatus.UNREAD;
    const isHighPriority = notification.priority === NotificationPriority.HIGH || notification.priority === NotificationPriority.URGENT;

    return (
        <div
            onClick={handleClick}
            onMouseEnter={() => setIsHovered(true)}
            onMouseLeave={() => setIsHovered(false)}
            style={{
                position: 'relative',
                padding: '1rem',
                backgroundColor: isHovered ? '#f3f4f6' : (isUnread ? '#eff6ff' : 'white'),
                cursor: 'pointer',
                transition: 'all 0.2s',
                borderLeft: isHighPriority ? '4px solid #f97316' : '4px solid transparent',
                borderBottom: '1px solid #f3f4f6'
            }}
        >
            <div style={{ display: 'flex', alignItems: 'start', gap: '0.75rem' }}>
                {/* Icon */}
                <div style={{ flexShrink: 0, marginTop: '0.25rem' }}>
                    {getIcon()}
                </div>

                {/* Content */}
                <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ display: 'flex', alignItems: 'start', justifyContent: 'space-between', marginBottom: '0.25rem' }}>
                        <p style={{
                            fontSize: '0.875rem',
                            fontWeight: isUnread ? '700' : '600',
                            color: '#111827',
                            margin: 0
                        }}>
                            {notification.title}
                        </p>

                        {/* Delete button */}
                        <button
                            onClick={handleDelete}
                            style={{
                                marginLeft: '0.5rem',
                                flexShrink: 0,
                                color: '#9ca3af',
                                background: 'none',
                                border: 'none',
                                cursor: 'pointer',
                                padding: '0.25rem',
                                borderRadius: '4px',
                                transition: 'all 0.2s',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center'
                            }}
                            onMouseOver={(e) => {
                                e.currentTarget.style.backgroundColor = '#fee2e2';
                                e.currentTarget.style.color = '#dc2626';
                            }}
                            onMouseOut={(e) => {
                                e.currentTarget.style.backgroundColor = 'transparent';
                                e.currentTarget.style.color = '#9ca3af';
                            }}
                            aria-label="Delete notification"
                        >
                            <svg style={{ width: '16px', height: '16px' }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </button>
                    </div>

                    <p style={{ marginTop: '0.25rem', fontSize: '0.875rem', color: '#4b5563', margin: 0 }}>
                        {notification.message}
                    </p>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginTop: '0.5rem' }}>
                        <p style={{ fontSize: '0.75rem', color: '#9ca3af', margin: 0 }}>
                            {formatTime(notification.createdAt)}
                        </p>

                        {isUnread && (
                            <span style={{
                                display: 'inline-block',
                                width: '8px',
                                height: '8px',
                                backgroundColor: '#3b82f6',
                                borderRadius: '50%'
                            }} />
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default NotificationItem;