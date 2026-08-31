export const NotificationType = {
    FRIEND_REQUEST_RECEIVED: 'FRIEND_REQUEST_RECEIVED',
    FRIEND_REQUEST_ACCEPTED: 'FRIEND_REQUEST_ACCEPTED',
    FRIEND_REQUEST_DECLINED: 'FRIEND_REQUEST_DECLINED',
    GAME_INVITATION_RECEIVED: 'GAME_INVITATION_RECEIVED',
    MATCH_STARTED: 'MATCH_STARTED',
    ACHIEVEMENT_UNLOCKED: 'ACHIEVEMENT_UNLOCKED',
    SYSTEM_ANNOUNCEMENT: 'SYSTEM_ANNOUNCEMENT'
} as const;

export type NotificationType = typeof NotificationType[keyof typeof NotificationType];

export const NotificationStatus = {
    UNREAD: 'UNREAD',
    READ: 'READ',
    DELETED: 'DELETED'
} as const;

export type NotificationStatus = typeof NotificationStatus[keyof typeof NotificationStatus];

export const NotificationPriority = {
    LOW: 'LOW',
    NORMAL: 'NORMAL',
    HIGH: 'HIGH',
    URGENT: 'URGENT'
} as const;

export type NotificationPriority = typeof NotificationPriority[keyof typeof NotificationPriority];

export interface NotificationDto {
    notificationId: string;
    type: NotificationType;
    title: string;
    message: string;
    status: NotificationStatus;
    priority: NotificationPriority;
    createdAt: string;
    readAt: string | null;
    metadata: Record<string, string>;
}

export interface NotificationCountDto {
    count: number;
}

export interface WebSocketNotification {
    notificationId: string;
    type: NotificationType;
    title: string;
    message: string;
    priority: NotificationPriority;
    createdAt: string;
    metadata: Record<string, string>;
}

