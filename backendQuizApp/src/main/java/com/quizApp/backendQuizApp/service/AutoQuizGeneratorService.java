package com.quizApp.backendQuizApp.service;

import com.quizApp.backendQuizApp.dto.quiz.QuizGenerationRequest;
import com.quizApp.backendQuizApp.model.Question;
import com.quizApp.backendQuizApp.model.Quiz;
import com.quizApp.backendQuizApp.model.User;
import com.quizApp.backendQuizApp.repository.QuizRepository;
import com.quizApp.backendQuizApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoQuizGeneratorService {

    private final QuizService quizService;
    private final GeminiAiService geminiAiService;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;

    // Popular topics for automatic quiz generation
    private final List<String> POPULAR_TOPICS = Arrays.asList(
        "JavaScript Programming", "Python Basics", "Java Fundamentals", "React Development",
        "Data Structures", "Algorithms", "Database Management", "Web Development",
        "Machine Learning", "Artificial Intelligence", "Cybersecurity", "Cloud Computing",
        "World History", "Geography", "Science Facts", "Mathematics", "Physics",
        "Chemistry", "Biology", "Literature", "Current Affairs", "Sports",
        "Movies and Entertainment", "Technology Trends", "Space and Astronomy",
        "Environmental Science", "Health and Medicine", "Psychology", "Philosophy"
    );

    private final List<String> CATEGORIES = Arrays.asList(
        "Technology", "Science", "History", "Geography", "Literature", 
        "Mathematics", "Sports", "Entertainment", "General Knowledge"
    );

    private final Random random = new Random();

    /**
     * Generate a quiz based on user requirements
     */
    public Quiz generateCustomQuiz(QuizGenerationRequest request, User creator) {
        log.info("Generating custom quiz for topic: {} by user: {}", request.getTopic(), creator.getUsername());
        
        // Enhance the request with additional context
        enhanceQuizRequest(request);
        
        try {
            List<Question> questions = geminiAiService.generateQuestions(request);
            
            Quiz quiz = Quiz.builder()
                    .title(generateQuizTitle(request.getTopic(), request.getDifficulty()))
                    .description(generateQuizDescription(request))
                    .topic(request.getTopic())
                    .creatorId(creator.getId())
                    .creatorUsername(creator.getUsername())
                    .questions(questions)
                    .difficulty(request.getDifficulty())
                    .timeLimitMinutes(calculateTimeLimit(request.getNumberOfQuestions(), request.getDifficulty()))
                    .isPublic(true)
                    .isActive(true)
                    .category(request.getCategory() != null ? request.getCategory() : categorizeTopicAutomatically(request.getTopic()))
                    .tags(request.getTags())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .aiPrompt(buildEnhancedPrompt(request))
                    .aiModel("Gemini-1.5-Flash")
                    .aiGeneratedAt(LocalDateTime.now())
                    .totalQuestions(questions.size())
                    .totalPoints(questions.stream().mapToInt(q -> q.getPoints() != null ? q.getPoints() : 1).sum())
                    .totalAttempts(0)
                    .build();

            Quiz savedQuiz = quizRepository.save(quiz);
            log.info("Successfully generated quiz: {} with {} questions", savedQuiz.getTitle(), questions.size());
            return savedQuiz;
            
        } catch (Exception e) {
            log.error("Error generating custom quiz for topic: {}", request.getTopic(), e);
            throw new RuntimeException("Failed to generate quiz: " + e.getMessage());
        }
    }

    /**
     * Generate a random quiz automatically
     */
    @Async
    public CompletableFuture<Quiz> generateRandomQuiz() {
        try {
            // Create a system user for auto-generated quizzes
            User systemUser = getOrCreateSystemUser();
            
            // Generate random quiz parameters
            String topic = POPULAR_TOPICS.get(random.nextInt(POPULAR_TOPICS.size()));
            Question.DifficultyLevel difficulty = Question.DifficultyLevel.values()[random.nextInt(Question.DifficultyLevel.values().length)];
            int numberOfQuestions = 5 + random.nextInt(16); // 5-20 questions
            
            QuizGenerationRequest request = new QuizGenerationRequest();
            request.setTopic(topic);
            request.setDifficulty(difficulty);
            request.setNumberOfQuestions(numberOfQuestions);
            request.setCategory(categorizeTopicAutomatically(topic));
            request.setTimeLimitMinutes(calculateTimeLimit(numberOfQuestions, difficulty));
            
            Quiz quiz = generateCustomQuiz(request, systemUser);
            log.info("Auto-generated random quiz: {}", quiz.getTitle());
            
            return CompletableFuture.completedFuture(quiz);
            
        } catch (Exception e) {
            log.error("Error generating random quiz", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Generate multiple random quizzes
     */
    @Async
    public CompletableFuture<List<Quiz>> generateMultipleRandomQuizzes(int count) {
        try {
            List<CompletableFuture<Quiz>> futures = new java.util.ArrayList<>();
            
            for (int i = 0; i < count; i++) {
                futures.add(generateRandomQuiz());
            }
            
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            
            return allFutures.thenApply(v -> 
                futures.stream()
                    .map(CompletableFuture::join)
                    .toList()
            );
            
        } catch (Exception e) {
            log.error("Error generating multiple random quizzes", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Generate quiz based on trending topics
     */
    public Quiz generateTrendingTopicQuiz(User creator) {
        List<String> trendingTopics = Arrays.asList(
            "ChatGPT and AI Tools", "Cryptocurrency Basics", "Climate Change", 
            "Space Exploration 2024", "Sustainable Technology", "Remote Work Culture",
            "Electric Vehicles", "Quantum Computing", "Metaverse", "5G Technology"
        );
        
        String topic = trendingTopics.get(random.nextInt(trendingTopics.size()));
        
        QuizGenerationRequest request = new QuizGenerationRequest();
        request.setTopic(topic);
        request.setDifficulty(Question.DifficultyLevel.MEDIUM);
        request.setNumberOfQuestions(10);
        request.setCategory("Technology");
        request.setTimeLimitMinutes(15);
        
        return generateCustomQuiz(request, creator);
    }

    // Helper methods
    private void enhanceQuizRequest(QuizGenerationRequest request) {
        if (request.getTimeLimitMinutes() == null) {
            request.setTimeLimitMinutes(calculateTimeLimit(request.getNumberOfQuestions(), request.getDifficulty()));
        }
        
        if (request.getCategory() == null) {
            request.setCategory(categorizeTopicAutomatically(request.getTopic()));
        }
    }

    private String generateQuizTitle(String topic, Question.DifficultyLevel difficulty) {
        String[] prefixes = {"Master", "Test Your Knowledge of", "Challenge:", "Quiz on", "Explore"};
        String prefix = prefixes[random.nextInt(prefixes.length)];
        return String.format("%s %s (%s Level)", prefix, topic, difficulty.toString().toLowerCase());
    }

    private String generateQuizDescription(QuizGenerationRequest request) {
        return String.format(
            "An AI-generated %s level quiz on %s with %d questions. " +
            "Test your knowledge and learn something new! Time limit: %d minutes.",
            request.getDifficulty().toString().toLowerCase(),
            request.getTopic(),
            request.getNumberOfQuestions(),
            request.getTimeLimitMinutes()
        );
    }

    private Integer calculateTimeLimit(Integer numberOfQuestions, Question.DifficultyLevel difficulty) {
        int baseTimePerQuestion = switch (difficulty) {
            case EASY -> 45; // 45 seconds per question
            case MEDIUM -> 60; // 1 minute per question
            case HARD -> 90; // 1.5 minutes per question
        };
        
        return Math.max(5, (numberOfQuestions * baseTimePerQuestion) / 60); // At least 5 minutes
    }

    private String categorizeTopicAutomatically(String topic) {
        String lowerTopic = topic.toLowerCase();
        
        if (lowerTopic.contains("programming") || lowerTopic.contains("coding") || 
            lowerTopic.contains("javascript") || lowerTopic.contains("python") || 
            lowerTopic.contains("java") || lowerTopic.contains("react") ||
            lowerTopic.contains("technology") || lowerTopic.contains("computer")) {
            return "Technology";
        } else if (lowerTopic.contains("history") || lowerTopic.contains("war") || 
                   lowerTopic.contains("ancient") || lowerTopic.contains("medieval")) {
            return "History";
        } else if (lowerTopic.contains("science") || lowerTopic.contains("physics") || 
                   lowerTopic.contains("chemistry") || lowerTopic.contains("biology")) {
            return "Science";
        } else if (lowerTopic.contains("geography") || lowerTopic.contains("country") || 
                   lowerTopic.contains("capital") || lowerTopic.contains("continent")) {
            return "Geography";
        } else if (lowerTopic.contains("math") || lowerTopic.contains("algebra") || 
                   lowerTopic.contains("geometry") || lowerTopic.contains("calculus")) {
            return "Mathematics";
        } else if (lowerTopic.contains("sport") || lowerTopic.contains("football") || 
                   lowerTopic.contains("basketball") || lowerTopic.contains("olympic")) {
            return "Sports";
        } else if (lowerTopic.contains("movie") || lowerTopic.contains("music") || 
                   lowerTopic.contains("celebrity") || lowerTopic.contains("entertainment")) {
            return "Entertainment";
        } else {
            return "General Knowledge";
        }
    }

    private String buildEnhancedPrompt(QuizGenerationRequest request) {
        return String.format(
            "Generate a comprehensive %s level quiz on '%s' with %d questions. " +
            "Category: %s. Make questions engaging and educational. " +
            "Include varied question types and ensure answers are accurate.",
            request.getDifficulty(),
            request.getTopic(),
            request.getNumberOfQuestions(),
            request.getCategory()
        );
    }

    private User getOrCreateSystemUser() {
        return userRepository.findByUsername("system")
                .orElseGet(() -> {
                    User systemUser = User.builder()
                            .username("system")
                            .email("system@quizapp.com")
                            .password("$2a$10$encoded.password.here") // Encoded password
                            .firstName("System")
                            .lastName("Generator")
                            .role(User.Role.ADMIN)
                            .enabled(true)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return userRepository.save(systemUser);
                });
    }
}
