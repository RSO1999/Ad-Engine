// src/App.tsx

import './App.css';
import { AnalysisForm } from './components/AnalysisForm';
import { ResultsDisplay } from './components/ResultsDisplay';
import { ErrorMessage } from './components/ErrorMessage';
import { useAdAnalyzer } from './hooks/useAdAnalyzer';

function App() {
  const { result, isLoading, error, analyzeUrl } = useAdAnalyzer();

  return (
    <div className="App">
      <header className="App-header">
        <h1>Contextual Ad Simulator</h1>
        <p>Enter the URL of an article to find the most relevant ad.</p>
      </header>
      
      <main>
        <AnalysisForm onAnalyze={analyzeUrl} isLoading={isLoading} />
        <ErrorMessage error={error} />
        <ResultsDisplay result={result} isLoading={isLoading} />
      </main>
    </div>
  );
}

export default App;