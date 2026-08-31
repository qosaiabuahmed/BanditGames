import React, { useState } from 'react';
import { NotificationType } from '../../types/notification';

interface NotificationPreference {
    type: NotificationType;
    label: string;
    description: string;
    enabled: boolean;
    browserNotification: boolean;
}

const NotificationSettingsPage: React.FC = () => {
    const [preferences, setPreferences] = useState<NotificationPreference[]>([
        {
            type: NotificationType.FRIEND_REQUEST_RECEIVED,
            label: 'Friend Requests',
            description: 'Get notified when someone sends you a friend request',
            enabled: true,
            browserNotification: true,
        },
        {
            type: NotificationType.FRIEND_REQUEST_ACCEPTED,
            label: 'Friend Request Accepted',
            description: 'Get notified when someone accepts your friend request',
            enabled: true,
            browserNotification: true,
        },
        {
            type: NotificationType.GAME_INVITATION_RECEIVED,
            label: 'Game Invitations',
            description: 'Get notified when someone invites you to play a game',
            enabled: true,
            browserNotification: true,
        },
        {
            type: NotificationType.MATCH_STARTED,
            label: 'Match Started',
            description: 'Get notified when your match is ready to start',
            enabled: true,
            browserNotification: true,
        },
        {
            type: NotificationType.ACHIEVEMENT_UNLOCKED,
            label: 'Achievements',
            description: 'Get notified when you unlock achievements',
            enabled: true,
            browserNotification: false,
        },
    ]);

    const [browserNotificationsEnabled, setBrowserNotificationsEnabled] = useState(
        Notification.permission === 'granted'
    );

    const handleToggleEnabled = (type: NotificationType) => {
        setPreferences((prev) =>
            prev.map((pref) =>
                pref.type === type ? { ...pref, enabled: !pref.enabled } : pref
            )
        );
    };

    const handleToggleBrowserNotification = (type: NotificationType) => {
        setPreferences((prev) =>
            prev.map((pref) =>
                pref.type === type
                    ? { ...pref, browserNotification: !pref.browserNotification }
                    : pref
            )
        );
    };

    const handleEnableBrowserNotifications = async () => {
        const permission = await Notification.requestPermission();
        setBrowserNotificationsEnabled(permission === 'granted');
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {/* Header */}
                <div className="mb-6">
                    <h1 className="text-2xl font-bold text-gray-900">Notification Settings</h1>
                    <p className="mt-1 text-sm text-gray-500">
                        Manage how you receive notifications
                    </p>
                </div>

                {/* Browser Notifications */}
                <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
                    <div className="flex items-start justify-between">
                        <div className="flex-1">
                            <h3 className="text-lg font-medium text-gray-900">
                                Browser Notifications
                            </h3>
                            <p className="mt-1 text-sm text-gray-500">
                                Receive desktop notifications even when you're not on this page
                            </p>
                        </div>
                        {browserNotificationsEnabled ? (
                            <span className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-green-100 text-green-800">
                Enabled
              </span>
                        ) : (
                            <button
                                onClick={handleEnableBrowserNotifications}
                                className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700"
                            >
                                Enable
                            </button>
                        )}
                    </div>
                </div>

                {/* Notification Preferences */}
                <div className="bg-white rounded-lg shadow-sm divide-y divide-gray-200">
                    {preferences.map((pref) => (
                        <div key={pref.type} className="p-6">
                            <div className="flex items-start justify-between">
                                <div className="flex-1">
                                    <h4 className="text-base font-medium text-gray-900">
                                        {pref.label}
                                    </h4>
                                    <p className="mt-1 text-sm text-gray-500">
                                        {pref.description}
                                    </p>

                                    {/* Browser notification toggle (only if main toggle is enabled) */}
                                    {pref.enabled && browserNotificationsEnabled && (
                                        <div className="mt-3 flex items-center">
                                            <input
                                                type="checkbox"
                                                checked={pref.browserNotification}
                                                onChange={() => handleToggleBrowserNotification(pref.type)}
                                                className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                                            />
                                            <label className="ml-2 text-sm text-gray-600">
                                                Show browser notifications
                                            </label>
                                        </div>
                                    )}
                                </div>

                                {/* Main toggle */}
                                <button
                                    onClick={() => handleToggleEnabled(pref.type)}
                                    className={`
                    relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent 
                    transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2
                    ${pref.enabled ? 'bg-blue-600' : 'bg-gray-200'}
                  `}
                                >
                  <span
                      className={`
                      pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 
                      transition duration-200 ease-in-out
                      ${pref.enabled ? 'translate-x-5' : 'translate-x-0'}
                    `}
                  />
                                </button>
                            </div>
                        </div>
                    ))}
                </div>

                {/* Save Button */}
                <div className="mt-6 flex justify-end">
                    <button
                        onClick={() => {
                            // TODO: Save preferences to backend
                            alert('Notification preferences saved!');
                        }}
                        className="px-6 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    >
                        Save Preferences
                    </button>
                </div>
            </div>
        </div>
    );
};

export default NotificationSettingsPage;