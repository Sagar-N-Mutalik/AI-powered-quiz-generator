import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Dashboard.css';

function Dashboard() {
  const [quizzes, setQuizzes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showGenerator, setShowGenerator] = useState(false);
  const [generatorData, setGeneratorData] = useState({
    topic: '',
    numberOfQuestions: 10,
    difficulty: 'MEDIUM',
    timeLimitMinutes: 10
  });
  const { user, token, logout } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }
    fetchQuizzes();
  }, [user, navigate]);

  const fetchQuizzes = async () => {
    try {
      const response = await fetch('/api/v1/quizzes/public');
      if (response.ok) {
        const data = await response.json();
        setQuizzes(data);
      }
    } catch (error) {
      console.error('Error fetching quizzes:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateQuiz = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await fetch('/api/v1/quizzes/generate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(generatorData)
      });

      if (response.ok) {
        const newQuiz = await response.json();
        setQuizzes([newQuiz, ...quizzes]);
        setShowGenerator(false);
        setGeneratorData({
          topic: '',
          numberOfQuestions: 10,
          difficulty: 'MEDIUM',
          timeLimitMinutes: 10
        });
      }
    } catch (error) {
      console.error('Error generating quiz:', error);
    } finally {
      setLoading(false);
    }
  };

  if (!user) {
    return null;
  }

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <h1>AI Quiz Generator</h1>
        <div className="header-actions">
          <span>Welcome, {user.username}!</span>
          <button onClick={() => setShowGenerator(true)} className="generate-btn">
            Generate Quiz
          </button>
          <button onClick={logout} className="logout-btn">
            Logout
          </button>
        </div>
      </header>

      {showGenerator && (
        <div className="modal-overlay">
          <div className="modal">
            <h3>Generate AI Quiz</h3>
            <form onSubmit={handleGenerateQuiz}>
              <div className="form-group">
                <label>Topic</label>
                <input
                  type="text"
                  value={generatorData.topic}
                  onChange={(e) => setGeneratorData({...generatorData, topic: e.target.value})}
                  placeholder="e.g., JavaScript, History, Science"
                  required
                />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Number of Questions</label>
                  <select
                    value={generatorData.numberOfQuestions}
                    onChange={(e) => setGeneratorData({...generatorData, numberOfQuestions: parseInt(e.target.value)})}
                  >
                    <option value={5}>5</option>
                    <option value={10}>10</option>
                    <option value={15}>15</option>
                    <option value={20}>20</option>
                  </select>
                </div>
                <div className="form-group">
                  <label>Difficulty</label>
                  <select
                    value={generatorData.difficulty}
                    onChange={(e) => setGeneratorData({...generatorData, difficulty: e.target.value})}
                  >
                    <option value="EASY">Easy</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HARD">Hard</option>
                  </select>
                </div>
              </div>
              <div className="modal-actions">
                <button type="button" onClick={() => setShowGenerator(false)}>Cancel</button>
                <button type="submit" disabled={loading}>
                  {loading ? 'Generating...' : 'Generate Quiz'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <main className="dashboard-content">
        <h2>Available Quizzes</h2>
        {loading ? (
          <div className="loading">Loading quizzes...</div>
        ) : (
          <div className="quiz-grid">
            {quizzes.map((quiz) => (
              <div key={quiz.id} className="quiz-card">
                <h3>{quiz.title}</h3>
                <p>{quiz.description}</p>
                <div className="quiz-meta">
                  <span className="difficulty">{quiz.difficulty}</span>
                  <span className="questions">{quiz.totalQuestions} questions</span>
                  <span className="time">{quiz.timeLimitMinutes} min</span>
                </div>
                <Link to={`/quiz/${quiz.id}`} className="play-btn">
                  Play Quiz
                </Link>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}

export default Dashboard;
