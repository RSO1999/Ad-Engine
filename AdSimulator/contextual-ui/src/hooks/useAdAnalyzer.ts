// src/hooks/useAdAnalyzer.ts

import { useState } from 'react';
import axios from 'axios';

const API_ENDPOINT = 'http://localhost:8080/api/analyze';

export interface AdCreative {
  id: number;
  name: string;
  imageUrl: string;
}

export interface AnalysisResponse {
  dominantCategory: string;
  matchedKeywords: string[];
  matchedAd: AdCreative;
}

export const useAdAnalyzer = () => {
  const [result, setResult] = useState<AnalysisResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const analyzeUrl = async (url: string) => {
    if (!url.trim()) {
      setError('Please enter a URL.');
      return;
    }

    setIsLoading(true);
    setError(null);
    setResult(null);

    try {
      const response = await axios.post<AnalysisResponse>(API_ENDPOINT, { url });
      setResult(response.data);
    } catch (err) { // <-- CORRECTED BLOCK
      if (axios.isAxiosError(err) && err.response?.status === 404) {
        setError('Analysis complete, but no relevant ad was found for this content.');
      } else {
        setError('An unexpected error occurred. Please check the backend server and try again.');
      }
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  return { result, isLoading, error, analyzeUrl };
};