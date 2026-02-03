// src/components/ErrorMessage.tsx

interface ErrorMessageProps {
  error: string | null;
}

export const ErrorMessage = ({ error }: ErrorMessageProps) => {
  if (!error) {
    return null;
  }
  return <p className="error-message">{error}</p>;
};