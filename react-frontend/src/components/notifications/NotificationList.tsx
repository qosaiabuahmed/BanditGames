import React from 'react';
import { useNotifications } from './NotificationContent.tsx';
import NotificationItem from './NotificationItem';

interface NotificationListProps {
    onClose?: () => void;
}

const NotificationList: React.FC<NotificationListProps> = ({ onClose }) => {
    const { notifications, unreadCount, loading, markAllAsRead } =
        useNotifications();

    const handleMarkAllAsRead = async () => {
        await markAllAsRead();
    };

    if (loading) {
        return (
            <div style={{ padding: '2rem', textAlign: 'center' }}>
                <div
                    className="animate-spin"
                    style={{
                        width: '32px',
                        height: '32px',
                        border: '3px solid #e5e7eb',
                        borderTop: '3px solid #667eea',
                        borderRadius: '50%',
                        margin: '0 auto',
                    }}
                />
            </div>
        );
    }

    const isEmpty = notifications.length === 0;
    const displayUnreadCount = unreadCount > 0;

    return (
        <div style={{ display: 'flex', flexDirection: 'column', maxHeight: '600px' }}>
            {/* Header */}
            <div
                style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    padding: '1rem 1.25rem',
                    borderBottom: '1px solid #e5e7eb',
                    backgroundColor: '#f9fafb'
                }}
            >
                <h3 style={{ fontSize: '1.125rem', fontWeight: '700', color: '#111827', margin: 0 }}>
                    Notifications
                    {displayUnreadCount && (
                        <span style={{ marginLeft: '0.5rem', fontSize: '0.875rem', fontWeight: 'normal', color: '#6b7280' }}>
                            ({unreadCount} unread)
                        </span>
                    )}
                </h3>
                {displayUnreadCount && (
                    <button
                        onClick={handleMarkAllAsRead}
                        style={{
                            fontSize: '0.875rem',
                            color: '#667eea',
                            fontWeight: '600',
                            background: 'none',
                            border: 'none',
                            cursor: 'pointer',
                            padding: '0.25rem 0.5rem',
                            borderRadius: '4px',
                            transition: 'all 0.2s'
                        }}
                        onMouseOver={(e) => {
                            e.currentTarget.style.backgroundColor = '#ede9fe';
                        }}
                        onMouseOut={(e) => {
                            e.currentTarget.style.backgroundColor = 'transparent';
                        }}
                    >
                        Mark all read
                    </button>
                )}
            </div>

            {/* Notification List */}
            <div style={{ overflowY: 'auto', maxHeight: '500px' }}>
                {isEmpty ? (
                    <div style={{ padding: '3rem 2rem', textAlign: 'center' }}>
                        <svg
                            style={{
                                width: '48px',
                                height: '48px',
                                color: '#9ca3af',
                                margin: '0 auto',
                                marginBottom: '1rem'
                            }}
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                        >
                            <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth={2}
                                d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
                            />
                        </svg>
                        <p style={{ marginTop: '0.5rem', fontSize: '0.875rem', color: '#6b7280' }}>No notifications yet</p>
                    </div>
                ) : (
                    notifications.map((notification) => (
                        <NotificationItem
                            key={notification.notificationId}
                            notification={notification}
                            onClose={onClose}
                        />
                    ))
                )}
            </div>

            {/* Footer */}
            {!isEmpty && (
                <div
                    style={{
                        padding: '0.75rem',
                        borderTop: '1px solid #e5e7eb',
                        backgroundColor: '#f9fafb'
                    }}
                >
                    <button
                        onClick={onClose}
                        style={{
                            width: '100%',
                            textAlign: 'center',
                            fontSize: '0.875rem',
                            color: '#667eea',
                            fontWeight: '600',
                            background: 'none',
                            border: 'none',
                            cursor: 'pointer',
                            padding: '0.5rem',
                            borderRadius: '6px',
                            transition: 'all 0.2s'
                        }}
                        onMouseOver={(e) => {
                            e.currentTarget.style.backgroundColor = '#ede9fe';
                        }}
                        onMouseOut={(e) => {
                            e.currentTarget.style.backgroundColor = 'transparent';
                        }}
                    >
                        Close
                    </button>
                </div>
            )}
        </div>
    );
};

export default NotificationList;
