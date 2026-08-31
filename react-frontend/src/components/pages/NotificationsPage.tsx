import React, { useState } from 'react';
import { useNotifications } from '../notifications/NotificationContent';
import NotificationItem from '../notifications/NotificationItem';
import { NotificationStatus } from '../../types/notification';

const NotificationsPage: React.FC = () => {
    const { notifications, unreadCount, loading, markAllAsRead } = useNotifications();
    const [filter, setFilter] = useState<'all' | 'unread'>('all');

    const filteredNotifications = filter === 'unread'
        ? notifications.filter((n) => n.status === NotificationStatus.UNREAD)
        : notifications;

    return (
        <div className="min-h-screen bg-gray-50">
            <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {/* Header */}
                <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
                    <div className="flex items-center justify-between">
                        <div>
                            <h1 className="text-2xl font-bold text-gray-900">Notifications</h1>
                            <p className="mt-1 text-sm text-gray-500">
                                {unreadCount > 0 ? `${unreadCount} unread notification${unreadCount > 1 ? 's' : ''}` : 'All caught up!'}
                            </p>
                        </div>

                        {unreadCount > 0 && (
                            <button
                                onClick={markAllAsRead}
                                className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
                            >
                                Mark all as read
                            </button>
                        )}
                    </div>

                    {/* Filter Tabs */}
                    <div className="mt-6 border-b border-gray-200">
                        <nav className="-mb-px flex space-x-8">
                            <button
                                onClick={() => setFilter('all')}
                                className={`
                  py-2 px-1 border-b-2 font-medium text-sm
                  ${filter === 'all'
                                    ? 'border-blue-500 text-blue-600'
                                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                                }
                `}
                            >
                                All ({notifications.length})
                            </button>
                            <button
                                onClick={() => setFilter('unread')}
                                className={`
                  py-2 px-1 border-b-2 font-medium text-sm
                  ${filter === 'unread'
                                    ? 'border-blue-500 text-blue-600'
                                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                                }
                `}
                            >
                                Unread ({unreadCount})
                            </button>
                        </nav>
                    </div>
                </div>

                {/* Notification List */}
                <div className="bg-white rounded-lg shadow-sm divide-y divide-gray-200">
                    {loading ? (
                        <div className="flex justify-center items-center h-64">
                            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
                        </div>
                    ) : filteredNotifications.length === 0 ? (
                        <div className="p-12 text-center">
                            <svg
                                className="mx-auto h-16 w-16 text-gray-400"
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
                            <h3 className="mt-4 text-lg font-medium text-gray-900">No notifications</h3>
                            <p className="mt-2 text-sm text-gray-500">
                                {filter === 'unread'
                                    ? "You're all caught up! No unread notifications."
                                    : "You don't have any notifications yet."
                                }
                            </p>
                        </div>
                    ) : (
                        filteredNotifications.map((notification) => (
                            <NotificationItem
                                key={notification.notificationId}
                                notification={notification}
                            />
                        ))
                    )}
                </div>
            </div>
        </div>
    );
};

export default NotificationsPage;