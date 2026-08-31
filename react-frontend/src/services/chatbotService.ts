import type { ApiRequest, ApiResponse } from '../types/chatbot';

const CHATBOT_API_URL = import.meta.env.VITE_CHATBOT_API_URL || 'http://localhost:7777/chat';

export class ChatbotService {
  /**
   * Send a question to the chatbot API and get a response
   */
  static async sendMessage(question: string, useCache: boolean = true, nResults: number = 3): Promise<ApiResponse> {
    const requestBody: ApiRequest = {
      question: question,
      use_cache: useCache,
      n_results: nResults
    };

    const response = await fetch(CHATBOT_API_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(requestBody)
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return response.json();
  }
}
