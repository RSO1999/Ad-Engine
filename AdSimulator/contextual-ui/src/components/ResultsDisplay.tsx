import type { AnalysisResponse } from '../hooks/useAdAnalyzer';
import spinner from '../assets/spinner.svg';
import './ResultsDisplay.css'; // This will now work correctly

interface ResultsDisplayProps {
  result: AnalysisResponse | null;
  isLoading: boolean;
}

export const ResultsDisplay = ({ result, isLoading }: ResultsDisplayProps) => {
  // Show spinner when loading
  if (isLoading) {
    return (
      <div className="results-card loading">
        <img src={spinner} alt="Loading..." />
      </div>
    );
  }

  // Show nothing if there is no result and not loading
  if (!result) {
    return null;
  }

  // Show the results card when data is available
  return (
    <div className="results-card">
      <h2>Analysis Complete</h2>
      <div className="results-content">
        <div className="analysis-details">
          <h3>Contextual Analysis</h3>
          <p>
            <strong>Dominant Category:</strong> {result.dominantCategory}
          </p>
          <h4>Matched Keywords:</h4>
          <ul>
            {result.matchedKeywords.map((keyword, index) => (
              <li key={index}>{keyword}</li>
            ))}
          </ul>
        </div>
        <div className="ad-display">
          <h3>Suggested Ad</h3>
          <p>{result.matchedAd.name}</p>
          <a href={result.matchedAd.imageUrl} target="_blank" rel="noopener noreferrer">
            <img src={result.matchedAd.imageUrl} alt={result.matchedAd.name} className="ad-image" />
          </a>
        </div>
      </div>
    </div>
  );
};