import { useState, useEffect, useRef } from 'react';
import Message from './Message';
import LoadingIndicator from './LoadingIndicator';
import ChatInput from './ChatInput';
import styles from './Chatbot.module.css';
import type { Message as MessageType } from '../../types/chatbot';
import { ChatbotService } from '../../services/chatbotService';

export default function Chatbot() {
  const [messages, setMessages] = useState<MessageType[]>([
    {
      content: `<strong>Welcome!</strong> I'm your board game assistant. I can help you with:
        <ul style="margin-top: 10px; padding-left: 20px;">
          <li>Game rules and strategies</li>
          <li>Platform navigation</li>
          <li>Gameplay questions</li>
        </ul>
        <p style="margin-top: 10px;">What would you like to know?</p>`,
      isUser: false,
      meta: ''
    }
  ]);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, isLoading]);

  const handleSendMessage = async (question: string): Promise<void> => {
    if (!question.trim()) return;

    setMessages(prev => [...prev, {
      content: question,
      isUser: true,
      meta: ''
    }]);

    setIsLoading(true);

    try {
      const data = await ChatbotService.sendMessage(question, true, 3);

      // Add bot response
      const meta = `${data.cached ? '⚡ Cached' : '🤖 Generated'} • ${(data.processing_time * 1000).toFixed(0)}ms`;

      setMessages(prev => [...prev, {
        content: data.response,
        isUser: false,
        meta: meta
      }]);

    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Unknown error occurred';

      setMessages(prev => [...prev, {
        content: `Sorry, I encountered an error: ${errorMessage}. Please make sure the server is running on port 7777.`,
        isUser: false,
        meta: ''
      }]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={styles.chatContainer}>
      <div className={styles.chatHeader}>
        <h1>Board Game Platform Chatbot</h1>
        <p>Ask me about game rules and platform features!</p>
      </div>

      <div className={styles.chatMessages}>
        {messages.map((message, index) => (
          <Message
            key={index}
            content={message.content}
            isUser={message.isUser}
            meta={message.meta}
          />
        ))}
        {isLoading && <LoadingIndicator />}
        <div ref={messagesEndRef} />
      </div>

      <ChatInput onSendMessage={handleSendMessage} disabled={isLoading} />
    </div>
  );
}
