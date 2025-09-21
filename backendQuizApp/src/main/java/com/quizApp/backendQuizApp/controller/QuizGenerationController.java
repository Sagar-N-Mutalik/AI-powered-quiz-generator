package com.quizApp.backendQuizApp.controller;

import com.quizApp.backendQuizApp.dto.quiz.QuizGenerationRequest;
import com.quizApp.backendQuizApp.model.Quiz;
import com.quizApp.backendQuizApp.model.User;
import com.quizApp.backendQuizApp.service.AutoQuizGeneratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/quiz-generator")
@RequiredArgsConstructor
public class QuizGenerationController {

    private final AutoQuizGeneratorService autoQuizGeneratorService;

    /**
     * Generate a custom quiz based on user requirements
     */
    @PostMapping("/custom")
    public ResponseEntity<Quiz> generateCustomQuiz(
            @RequestBody @Valid QuizGenerationRequest request,
            @AuthenticationPrincipal User user) {
        
        Quiz quiz = autoQuizGeneratorService.generateCustomQuiz(request, user);
        return ResponseEntity.ok(quiz);
    }

    /**
     * Generate a random quiz automatically
     */
    @PostMapping("/random")
    public ResponseEntity<CompletableFuture<Quiz>> generateRandomQuiz(@AuthenticationPrincipal User user) {
        CompletableFuture<Quiz> futureQuiz = autoQuizGeneratorService.generateRandomQuiz();
        return ResponseEntity.ok(futureQuiz);
    }

    /**
     * Generate multiple random quizzes
     */
    @PostMapping("/random/batch")
    public ResponseEntity<CompletableFuture<List<Quiz>>> generateMultipleRandomQuizzes(
            @RequestParam(defaultValue = "5") int count,
            @AuthenticationPrincipal User user) {
        
        if (count > 10) count = 10; // Limit to prevent abuse
        
        CompletableFuture<List<Quiz>> futureQuizzes = autoQuizGeneratorService.generateMultipleRandomQuizzes(count);
        return ResponseEntity.ok(futureQuizzes);
    }

    /**
     * Generate a quiz on trending topics
     */
    @PostMapping("/trending")
    public ResponseEntity<Quiz> generateTrendingTopicQuiz(@AuthenticationPrincipal User user) {
        Quiz quiz = autoQuizGeneratorService.generateTrendingTopicQuiz(user);
        return ResponseEntity.ok(quiz);
    }

    /**
     * Quick quiz generation with minimal parameters
     */
    @PostMapping("/quick")
    public ResponseEntity<Quiz> generateQuickQuiz(
            @RequestParam String topic,
            @RequestParam(defaultValue = "MEDIUM") String difficulty,
            @RequestParam(defaultValue = "10") int questions,
            @AuthenticationPrincipal User user) {
        
        QuizGenerationRequest request = new QuizGenerationRequest();
        request.setTopic(topic);
        request.setDifficulty(com.quizApp.backendQuizApp.model.Question.DifficultyLevel.valueOf(difficulty.toUpperCase()));
        request.setNumberOfQuestions(questions);
        
        Quiz quiz = autoQuizGeneratorService.generateCustomQuiz(request, user);
        return ResponseEntity.ok(quiz);
    }

    /**
     * Get popular topics for quiz generation
     */
    @GetMapping("/topics/popular")
    public ResponseEntity<Map<String, Object>> getPopularTopics() {
        List<String> topics = List.of(
            "JavaScript Programming", "Python Basics", "Java Fundamentals", "React Development",
            "Data Structures", "Algorithms", "Database Management", "Web Development",
            "Machine Learning", "Artificial Intelligence", "Cybersecurity", "Cloud Computing",
            "World History", "Geography", "Science Facts", "Mathematics", "Physics",
            "Chemistry", "Biology", "Literature", "Current Affairs", "Sports",
            "Movies and Entertainment", "Technology Trends", "Space and Astronomy"
        );
        
        List<String> categories = List.of(
            "Technology", "Science", "History", "Geography", "Literature", 
            "Mathematics", "Sports", "Entertainment", "General Knowledge"
        );
        
        Map<String, Object> response = Map.of(
            "topics", topics,
            "categories", categories,
            "difficulties", List.of("EASY", "MEDIUM", "HARD"),
            "questionCounts", List.of(5, 10, 15, 20)
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Generate quiz suggestions based on user's previous quizzes
     */
    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getQuizSuggestions(@AuthenticationPrincipal User user) {
        // This could be enhanced to analyze user's quiz history
        List<String> suggestions = List.of(
            "Advanced " + "JavaScript",
            "Data Science Fundamentals",
            "Modern Web Development",
            "Cloud Computing Basics",
            "Machine Learning Concepts"
        );
        
        return ResponseEntity.ok(suggestions);
    }
}
