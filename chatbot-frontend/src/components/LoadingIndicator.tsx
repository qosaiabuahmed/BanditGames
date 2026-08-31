import React from 'react';
import styles from './LoadingIndicator.module.css';

const LoadingIndicator: React.FC = () => {
  return (
    <div className={styles.message}>
      <div className={styles.loading}>
        <div className={styles.loadingDots}>
          <span>●</span>
          <span>●</span>
          <span>●</span>
        </div>
      </div>
    </div>
  );
};

export default LoadingIndicator;
