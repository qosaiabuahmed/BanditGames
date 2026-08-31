import React, { useEffect, useState } from 'react';
import { useNotifications } from './NotificationContent.tsx';
import type {NotificationDto} from '../../types/notification';

const NotificationToast: React.FC = () => {
    const { notifications } = useNotifications();
    const [visibleToasts, setVisibleToasts] = useState<NotificationDto[]>([]);

    useEffect(() => {
        // Show only the 3 most recent unread notifications as toasts
        const recentUnread = notifications
            .filter((n) => n.status === 'UNREAD')
            .slice(0, 3);

        setVisibleToasts(recentUnread);

        // Auto-hide after 5 seconds
        if (recentUnread.length > 0) {
            const timer = setTimeout(() => {
                setVisibleToasts([]);
            }, 5000);

            return () => clearTimeout(timer);
        }
    }, [notifications]);

    const handleDismiss = (id: string) => {
        setVisibleToasts((prev) => prev.filter((toast) => toast.notificationId !== id));
    };

    if (visibleToasts.length === 0) return null;

    return (
        <div
            style={{
                position: 'fixed',
                bottom: '1rem',
                right: '1rem',
                display: 'flex',
                flexDirection: 'column',
                gap: '0.5rem',
                zIndex: 9998
            }}
        >
            {visibleToasts.map((notification) => (
                <div
                    key={notification.notificationId}
                    className="animate-slide-in-right"
                    style={{
                        backgroundColor: 'white',
                        borderRadius: '12px',
                        boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
                        border: '1px solid #e5e7eb',
                        padding: '1rem',
                        maxWidth: '350px',
                        minWidth: '300px'
                    }}
                >
                    <div style={{ display: 'flex', alignItems: 'start', gap: '0.75rem' }}>
                        <div style={{ flex: 1 }}>
                            <p style={{
                                fontSize: '0.875rem',
                                fontWeight: '600',
                                color: '#111827',
                                margin: 0,
                                marginBottom: '0.25rem'
                            }}>
                                {notification.title}
                            </p>
                            <p style={{
                                fontSize: '0.875rem',
                                color: '#6b7280',
                                margin: 0
                            }}>
                                {notification.message}
                            </p>
                        </div>
                        <button
                            onClick={() => handleDismiss(notification.notificationId)}
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
                                e.currentTarget.style.backgroundColor = '#f3f4f6';
                                e.currentTarget.style.color = '#4b5563';
                            }}
                            onMouseOut={(e) => {
                                e.currentTarget.style.backgroundColor = 'transparent';
                                e.currentTarget.style.color = '#9ca3af';
                            }}
                        >
                            <svg style={{ width: '20px', height: '20px' }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </button>
                    </div>
                </div>
            ))}
        </div>
    );
};

export default NotificationToast;