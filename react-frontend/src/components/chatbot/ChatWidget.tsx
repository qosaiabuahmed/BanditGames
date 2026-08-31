import React, { useState } from 'react';
import Chatbot from './Chatbot';
import styles from './ChatWidget.module.css';

const ChatWidget: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <>
      <button
        className={styles.chatWidgetButton}
        onClick={() => setIsOpen(!isOpen)}
        aria-label="Open chat"
      >
        {isOpen ? '✕' : '💬'}
      </button>

      {isOpen && (
        <div className={styles.chatWidgetContainer}>
          <Chatbot />
        </div>
      )}
    </>
  );
};

export default ChatWidget;