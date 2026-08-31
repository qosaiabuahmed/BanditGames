import React, { useState, useRef, useEffect } from 'react';
import { useNotifications } from './NotificationContent.tsx';
import NotificationList from './NotificationList';

const NotificationBell: React.FC = () => {
    const { unreadCount, isConnected } = useNotifications();
    const [isOpen, setIsOpen] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);
    const previousCountRef = useRef<number>(0);

    // Track unread count changes
    useEffect(() => {
        previousCountRef.current = unreadCount;
    }, [unreadCount]);

    // Close dropdown when clicking outside
    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
                setIsOpen(false);
            }
        };

        if (isOpen) {
            document.addEventListener('mousedown', handleClickOutside);
        }

        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [isOpen]);

    return (
        <div style={{ position: 'relative' }} ref={dropdownRef}>
            {/* Bell Icon Button */}
            <button
                onClick={() => setIsOpen(!isOpen)}
                style={{
                    position: 'relative',
                    padding: '0.5rem',
                    backgroundColor: 'white',
                    border: '2px solid #e5e7eb',
                    borderRadius: '8px',
                    cursor: 'pointer',
                    transition: 'all 0.2s',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center'
                }}
                onMouseOver={(e) => {
                    e.currentTarget.style.borderColor = '#667eea';
                    e.currentTarget.style.backgroundColor = '#f0f4ff';
                }}
                onMouseOut={(e) => {
                    e.currentTarget.style.borderColor = '#e5e7eb';
                    e.currentTarget.style.backgroundColor = 'white';
                }}
                aria-label="Notifications"
                title="Notifications"
            >
                {/* Bell SVG Icon */}
                <svg
                    style={{ width: '24px', height: '24px', color: '#4b5563' }}
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

                {/* Unread Badge */}
                {unreadCount > 0 && (
                    <span style={{
                        position: 'absolute',
                        top: '-6px',
                        right: '-6px',
                        display: 'inline-flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        padding: '0.15rem 0.4rem',
                        fontSize: '0.75rem',
                        fontWeight: 'bold',
                        color: 'white',
                        backgroundColor: '#ef4444',
                        borderRadius: '10px',
                        minWidth: '20px',
                        border: '2px solid white'
                    }}>
                        {unreadCount > 99 ? '99+' : unreadCount}
                    </span>
                )}

                {/* Connection Status Indicator */}
                {!isConnected && (
                    <span
                        style={{
                            position: 'absolute',
                            bottom: '-3px',
                            right: '-3px',
                            width: '12px',
                            height: '12px',
                            backgroundColor: '#fbbf24',
                            border: '2px solid white',
                            borderRadius: '50%'
                        }}
                        title="Reconnecting..."
                        className="animate-pulse"
                    />
                )}
            </button>

            {/* Dropdown */}
            {isOpen && (
                <div
                    style={{
                        position: 'absolute',
                        right: 0,
                        marginTop: '0.5rem',
                        width: '400px',
                        backgroundColor: 'white',
                        borderRadius: '12px',
                        boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
                        border: '1px solid #e5e7eb',
                        maxHeight: '600px',
                        overflow: 'hidden',
                        zIndex: 9999
                    }}
                >
                    <NotificationList onClose={() => setIsOpen(false)} />
                </div>
            )}
        </div>
    );
};

export default NotificationBell;
