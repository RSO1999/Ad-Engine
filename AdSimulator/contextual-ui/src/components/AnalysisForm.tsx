// src/components/AnalysisForm.tsx

import { useState, type FormEvent } from 'react';

interface AnalysisFormProps {
  onAnalyze: (url: string) => void;
  isLoading: boolean;
}

export const AnalysisForm = ({ onAnalyze, isLoading }: AnalysisFormProps) => {
  const [url, setUrl] = useState('');

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    onAnalyze(url);
  };

  return (
    <form className="input-container" onSubmit={handleSubmit}>
      <input
        type="url"
        value={url}
        onChange={(e) => setUrl(e.target.value)}
        placeholder="https://www.example.com/your-article-here"
        disabled={isLoading}
        required
      />
      <button type="submit" disabled={isLoading}>
        {isLoading ? 'Analyzing...' : 'Analyze'}
      </button>
    </form>
  );
};