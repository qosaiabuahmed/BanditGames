import React, { useState } from 'react';
import styles from './Message.module.css';

interface MessageProps {
  content: string;
  isUser: boolean;
  meta?: string;
}

const Message: React.FC<MessageProps> = ({ content, isUser, meta }) => {
  const [isSpeaking, setIsSpeaking] = useState(false);

  const speakText = () => {
    if ('speechSynthesis' in window) {
      window.speechSynthesis.cancel();

      if (isSpeaking) {
        setIsSpeaking(false);
        return;
      }

      const tempDiv = document.createElement('div');
      tempDiv.innerHTML = content;
      const textContent = tempDiv.textContent || tempDiv.innerText || '';

      const utterance = new SpeechSynthesisUtterance(textContent);

      utterance.onstart = () => setIsSpeaking(true);
      utterance.onend = () => setIsSpeaking(false);
      utterance.onerror = () => setIsSpeaking(false);

      window.speechSynthesis.speak(utterance);
    } else {
      alert('Text-to-speech is not supported in your browser.');
    }
  };

  return (
    <div className={`${styles.message} ${isUser ? styles.user : styles.bot}`}>
      <div className={styles.messageContent}>
        <div className={styles.messageHeader}>
          <div dangerouslySetInnerHTML={{ __html: content }} />
          {!isUser && (
            <button
              className={styles.speakerButton}
              onClick={speakText}
              title={isSpeaking ? "Stop speaking" : "Read aloud"}
              aria-label={isSpeaking ? "Stop speaking" : "Read aloud"}
            >
              {isSpeaking ? '⏸️' : '🔊'}
            </button>
          )}
        </div>
        {meta && <div className={styles.messageMeta}>{meta}</div>}
      </div>
    </div>
  );
};

export default Message;
