import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './QuizPlayer.css';

function QuizPlayer() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, token } = useAuth();
  const [quiz, setQuiz] = useState(null);
  const [currentQuestion, setCurrentQuestion] = useState(0);
  const [answers, setAnswers] = useState({});
  const [timeLeft, setTimeLeft] = useState(0);
  const [loading, setLoading] = useState(true);
  const [submitted, setSubmitted] = useState(false);
  const [results, setResults] = useState(null);

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }
    fetchQuiz();
  }, [id, user, navigate]);

  useEffect(() => {
    if (quiz && timeLeft > 0) {
      const timer = setTimeout(() => setTimeLeft(timeLeft - 1), 1000);
      return () => clearTimeout(timer);
    } else if (timeLeft === 0 && quiz && !submitted) {
      handleSubmit();
    }
  }, [timeLeft, quiz, submitted]);

  const fetchQuiz = async () => {
    try {
      const response = await fetch(`/api/v1/quizzes/${id}`);
      if (response.ok) {
        const data = await response.json();
        setQuiz(data);
        setTimeLeft(data.timeLimitMinutes * 60);
      } else {
        navigate('/dashboard');
      }
    } catch (error) {
      console.error('Error fetching quiz:', error);
      navigate('/dashboard');
    } finally {
      setLoading(false);
    }
  };

  const handleAnswerSelect = (answer) => {
    setAnswers({
      ...answers,
      [currentQuestion]: answer
    });
  };

  const handleNext = () => {
    if (currentQuestion < quiz.questions.length - 1) {
      setCurrentQuestion(currentQuestion + 1);
    }
  };

  const handlePrevious = () => {
    if (currentQuestion > 0) {
      setCurrentQuestion(currentQuestion - 1);
    }
  };

  const handleSubmit = async () => {
    if (submitted) return;
    
    setSubmitted(true);
    const submissionData = {
      quizId: id,
      answers: Object.entries(answers).map(([questionIndex, answer]) => ({
        questionIndex: parseInt(questionIndex),
        answer
      }))
    };

    try {
      const response = await fetch('/api/v1/attempts', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(submissionData)
      });

      if (response.ok) {
        const result = await response.json();
        setResults(result);
      }
    } catch (error) {
      console.error('Error submitting quiz:', error);
    }
  };

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  if (loading) {
    return <div className="loading">Loading quiz...</div>;
  }

  if (!quiz) {
    return <div className="error">Quiz not found</div>;
  }

  if (results) {
    return (
      <div className="quiz-results">
        <div className="results-card">
          <h2>Quiz Complete!</h2>
          <div className="score">
            <span className="score-value">{results.scorePercentage?.toFixed(1) || 0}%</span>
            <p>{results.correctAnswers} out of {results.totalQuestions} correct</p>
          </div>
          <div className="results-details">
            <p>Time taken: {Math.floor((results.timeTakenSeconds || 0) / 60)} minutes</p>
            <p>Points earned: {results.earnedPoints} / {results.totalPoints}</p>
          </div>
          <button onClick={() => navigate('/dashboard')} className="back-btn">
            Back to Dashboard
          </button>
        </div>
      </div>
    );
  }

  const question = quiz.questions[currentQuestion];
  const progress = ((currentQuestion + 1) / quiz.questions.length) * 100;

  return (
    <div className="quiz-player">
      <div className="quiz-header">
        <h2>{quiz.title}</h2>
        <div className="quiz-info">
          <span className="timer">⏱️ {formatTime(timeLeft)}</span>
          <span className="progress">
            Question {currentQuestion + 1} of {quiz.questions.length}
          </span>
        </div>
      </div>

      <div className="progress-bar">
        <div className="progress-fill" style={{ width: `${progress}%` }}></div>
      </div>

      <div className="question-container">
        <h3>{question.questionText}</h3>
        <div className="options">
          {question.options.map((option, index) => (
            <button
              key={index}
              className={`option ${answers[currentQuestion] === option ? 'selected' : ''}`}
              onClick={() => handleAnswerSelect(option)}
            >
              {option}
            </button>
          ))}
        </div>
      </div>

      <div className="quiz-navigation">
        <button 
          onClick={handlePrevious} 
          disabled={currentQuestion === 0}
          className="nav-btn"
        >
          Previous
        </button>
        
        {currentQuestion === quiz.questions.length - 1 ? (
          <button onClick={handleSubmit} className="submit-btn">
            Submit Quiz
          </button>
        ) : (
          <button onClick={handleNext} className="nav-btn">
            Next
          </button>
        )}
      </div>
    </div>
  );
}

export default QuizPlayer;
