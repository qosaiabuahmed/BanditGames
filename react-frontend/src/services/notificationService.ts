import type { NotificationDto, NotificationCountDto } from "../types/notification.ts";
import { getAuthHeaders } from '../utils/tokenUtils';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

export class NotificationService {

    static async getNotifications(status?: 'unread' | 'read' | 'deleted'): Promise<NotificationDto[]> {
        const url = status
            ? `${API_BASE_URL}/notifications?status=${status}`
            : `${API_BASE_URL}/notifications`;

        const headers = await getAuthHeaders();
        const response = await fetch(url, {
            method: 'GET',
            headers,
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to fetch notifications');
        }
        return response.json();
    }

    static async getUnreadCount(): Promise<number> {
        const headers = await getAuthHeaders();
        const response = await fetch(`${API_BASE_URL}/notifications/count`, {
            method: 'GET',
            headers,
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to fetch notification count');
        }

        const data: NotificationCountDto = await response.json();
        return data.count;
    }

    static async markAsRead(notificationId: string): Promise<void> {
        const headers = await getAuthHeaders();
        const response = await fetch(`${API_BASE_URL}/notifications/${notificationId}/read`, {
            method: 'PUT',
            headers,
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to mark as read');
        }
    }

    static async markAllAsRead(): Promise<void> {
        const headers = await getAuthHeaders();
        const response = await fetch(`${API_BASE_URL}/notifications/read-all`, {
            method: 'PUT',
            headers,
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to mark all as read');
        }
    }

    static async deleteNotification(notificationId: string): Promise<void> {
        const headers = await getAuthHeaders();
        const response = await fetch(`${API_BASE_URL}/notifications/${notificationId}`, {
            method: 'DELETE',
            headers,
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to delete notification');
        }
    }
}
