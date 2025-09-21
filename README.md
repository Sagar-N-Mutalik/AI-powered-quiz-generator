# AI-Powered Quiz Generator

A full-stack web application that generates interactive quizzes using Google Gemini AI. Built with Spring Boot backend and React frontend.

## Features

- 🤖 **AI Quiz Generation**: Generate quizzes on any topic using Google Gemini AI
- 🔐 **User Authentication**: JWT-based secure authentication system
- 📊 **Interactive Quiz Player**: Beautiful, responsive quiz interface with timer
- 📈 **Results & Analytics**: Detailed quiz results and performance tracking
- 🎨 **Modern UI**: Clean, responsive design with smooth animations
- 🔒 **Secure Backend**: Spring Security with JWT tokens
- 📱 **Mobile Friendly**: Responsive design works on all devices

## Tech Stack

### Backend
- **Spring Boot 3.5.6** - Main framework
- **MongoDB** - Database
- **Spring Security** - Authentication & authorization
- **JWT** - Token-based authentication
- **Google Gemini AI** - Quiz generation
- **Lombok** - Code generation
- **Maven** - Build tool

### Frontend
- **React 18** - UI framework
- **React Router** - Client-side routing
- **CSS3** - Styling (no external UI libraries)
- **Create React App** - Build tool

## Project Structure

```
AI-powered-quiz-generator/
├── backendQuizApp/          # Spring Boot backend
│   ├── src/main/java/
│   │   └── com/quizApp/backendQuizApp/
│   │       ├── config/      # Configuration classes
│   │       ├── controller/  # REST controllers
│   │       ├── dto/         # Data Transfer Objects
│   │       ├── exception/   # Exception handling
│   │       ├── model/       # Entity models
│   │       ├── repository/  # Data repositories
│   │       ├── security/    # Security configuration
│   │       └── service/     # Business logic
│   └── src/main/resources/
│       └── application.yml  # Application configuration
└── frontend-quiz-app/       # React frontend
    ├── public/
    ├── src/
    │   ├── components/      # React components
    │   ├── context/         # React context
    │   └── App.js          # Main app component
    └── package.json
```

## Quick Start

### Prerequisites
- Java 21+
- Node.js 16+
- MongoDB (local or Atlas)
- Google Gemini API key

### Backend Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd AI-powered-quiz-generator/backendQuizApp
   ```

2. **Set environment variables**
   ```bash
   # Windows PowerShell
   $env:GEMINI_API_KEY = "your-gemini-api-key"
   $env:MONGODB_URI = "mongodb://localhost:27017/quiz_generator_db"
   
   # Linux/Mac
   export GEMINI_API_KEY="your-gemini-api-key"
   export MONGODB_URI="mongodb://localhost:27017/quiz_generator_db"
   ```

3. **Start MongoDB**
   - Local: Ensure MongoDB service is running
   - Atlas: Use your connection string in MONGODB_URI

4. **Run the backend**
   ```bash
   mvn spring-boot:run
   ```
   
   Backend will start at `http://localhost:8080`

### Frontend Setup

1. **Navigate to frontend directory**
   ```bash
   cd ../frontend-quiz-app
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Start the development server**
   ```bash
   npm start
   ```
   
   Frontend will start at `http://localhost:3000`

## API Endpoints

### Authentication
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - Login user
- `POST /api/v1/auth/refresh` - Refresh JWT token

### Quizzes
- `GET /api/v1/quizzes/public` - Get all public quizzes
- `GET /api/v1/quizzes/{id}` - Get quiz by ID
- `POST /api/v1/quizzes/generate` - Generate AI quiz (requires auth)
- `GET /api/v1/quizzes/my` - Get user's quizzes (requires auth)

### Quiz Attempts
- `POST /api/v1/attempts` - Submit quiz attempt (requires auth)
- `GET /api/v1/attempts/me` - Get user's attempts (requires auth)
- `GET /api/v1/attempts/quiz/{quizId}` - Get attempts for quiz (requires auth)

## Configuration

### Backend Configuration (`application.yml`)

Key configuration options:

```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/quiz_generator_db}

server:
  port: ${PORT:8080}
  servlet:
    context-path: /api/v1

app:
  jwt:
    secret: ${JWT_SECRET:your-secret-key}
    expiration: 86400000  # 24 hours
  
  gemini:
    api-key: ${GEMINI_API_KEY:your-api-key}
    base-url: https://generativelanguage.googleapis.com/v1beta
    model: gemini-1.5-flash
```

### Frontend Configuration

The frontend uses a proxy in development to connect to the backend. For production, set `REACT_APP_API_BASE_URL` environment variable.

## Deployment

### Backend (Render/Heroku)

1. **Environment Variables**:
   - `MONGODB_URI` - MongoDB connection string
   - `GEMINI_API_KEY` - Your Gemini API key
   - `JWT_SECRET` - Strong secret for JWT signing
   - `PORT` - Provided by platform
   - `FRONTEND_ORIGIN` - Your frontend URL for CORS

2. **Build Command**: `mvn clean package -DskipTests`
3. **Start Command**: `java -jar target/backendQuizApp-0.0.1-SNAPSHOT.jar`

### Frontend (Netlify/Vercel)

1. **Build Command**: `npm run build`
2. **Publish Directory**: `build`
3. **Environment Variables**:
   - `REACT_APP_API_BASE_URL` - Your backend URL

## Usage

1. **Register/Login**: Create an account or login
2. **Generate Quiz**: Click "Generate Quiz" and enter a topic
3. **Take Quiz**: Click "Play Quiz" on any available quiz
4. **View Results**: See your score and detailed results after completion

## Development

### Adding New Features

1. **Backend**: Add controllers in `controller/`, services in `service/`, and models in `model/`
2. **Frontend**: Add components in `components/` and update routing in `App.js`

### Database Schema

- **Users**: Authentication and profile data
- **Quizzes**: Quiz metadata and questions
- **QuizAttempts**: User quiz submissions and results
- **RefreshTokens**: JWT refresh token management

## Troubleshooting

### Common Issues

1. **MongoDB Connection**: Ensure MongoDB is running and URI is correct
2. **Gemini API**: Verify API key is valid and has quota
3. **CORS Issues**: Check frontend origin is allowed in backend CORS config
4. **JWT Errors**: Ensure JWT secret is properly set

### Logs

Backend logs are available in the console. Set logging levels in `application.yml`:

```yaml
logging:
  level:
    com.quizApp: DEBUG
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For issues and questions, please create an issue in the repository.
