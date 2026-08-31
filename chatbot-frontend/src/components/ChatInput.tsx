import React, { useState, useRef } from 'react';
import styles from './ChatInput.module.css';

interface ChatInputProps {
  onSendMessage: (message: string) => void;
  disabled: boolean;
}

// Type definition for browser speech recognition
interface SpeechRecognitionEvent extends Event {
  results: SpeechRecognitionResultList;
  resultIndex: number;
}

interface SpeechRecognitionResultList {
  length: number;
  item(index: number): SpeechRecognitionResult;
  [index: number]: SpeechRecognitionResult;
}

interface SpeechRecognitionResult {
  isFinal: boolean;
  length: number;
  item(index: number): SpeechRecognitionAlternative;
  [index: number]: SpeechRecognitionAlternative;
}

interface SpeechRecognitionAlternative {
  transcript: string;
  confidence: number;
}

const ChatInput: React.FC<ChatInputProps> = ({ onSendMessage, disabled }) => {
  const [message, setMessage] = useState<string>('');
  const [isListening, setIsListening] = useState<boolean>(false);
  const recognitionRef = useRef<any>(null);

  const suggestions = [
    'What are the rules of Connect Four?',
    'How do I win at Connect Four?',
    'How do I start a game?'
  ];

  const suggestionLabels: Record<string, string> = {
    'What are the rules of Connect Four?': 'Connect Four Rules',
    'How do I win at Connect Four?': 'Winning Strategies',
    'How do I start a game?': 'Start a Game'
  };

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (message.trim() && !disabled) {
      onSendMessage(message);
      setMessage('');
    }
  };

  const handleSuggestionClick = (suggestion: string) => {
    if (!disabled) {
      onSendMessage(suggestion);
    }
  };

  const toggleVoiceInput = () => {
    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;

    if (!SpeechRecognition) {
      alert('Speech recognition is not supported in your browser. Please try Chrome or Edge.');
      return;
    }

    if (isListening) {
      // Stop listening
      if (recognitionRef.current) {
        recognitionRef.current.stop();
      }
      setIsListening(false);
      return;
    }

    // Start listening
    const recognition = new SpeechRecognition();
    recognition.continuous = false;
    recognition.interimResults = true;
    recognition.lang = 'en-US';

    recognition.onstart = () => {
      setIsListening(true);
    };

    recognition.onresult = (event: SpeechRecognitionEvent) => {
      let interimTranscript = '';
      let finalTranscript = '';

      for (let i = event.resultIndex; i < event.results.length; i++) {
        const transcript = event.results[i][0].transcript;
        if (event.results[i].isFinal) {
          finalTranscript += transcript + ' ';
        } else {
          interimTranscript += transcript;
        }
      }

      setMessage((finalTranscript || interimTranscript).trim());
    };

    recognition.onerror = (event: any) => {
      console.error('Speech recognition error:', event.error);
      setIsListening(false);
    };

    recognition.onend = () => {
      setIsListening(false);
    };

    recognitionRef.current = recognition;
    recognition.start();
  };

  return (
    <div className={styles.chatInputContainer}>
      <div className={styles.suggestions}>
        {suggestions.map((suggestion, index) => (
          <div
            key={index}
            className={styles.suggestionChip}
            onClick={() => handleSuggestionClick(suggestion)}
          >
            {suggestionLabels[suggestion]}
          </div>
        ))}
      </div>

      <form className={styles.chatInputForm} onSubmit={handleSubmit}>
        <button
          type="button"
          className={`${styles.micButton} ${isListening ? styles.listening : ''}`}
          onClick={toggleVoiceInput}
          disabled={disabled}
          title={isListening ? "Stop listening" : "Voice input"}
          aria-label={isListening ? "Stop listening" : "Voice input"}
        >
          {isListening ? '⏹️' : '🎤'}
        </button>
        <input
          type="text"
          className={styles.chatInput}
          placeholder="Type your question here..."
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          disabled={disabled}
          autoComplete="off"
        />
        <button
          type="submit"
          className={styles.sendButton}
          disabled={disabled || !message.trim()}
        >
          Send
        </button>
      </form>
    </div>
  );
};

export default ChatInput;
