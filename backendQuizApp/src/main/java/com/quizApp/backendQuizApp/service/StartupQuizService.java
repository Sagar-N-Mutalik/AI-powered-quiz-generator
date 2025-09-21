package com.quizApp.backendQuizApp.service;

import com.quizApp.backendQuizApp.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StartupQuizService {

    private final AutoQuizGeneratorService autoQuizGeneratorService;
    private final QuizRepository quizRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void generateInitialQuizzes() {
        try {
            long existingQuizCount = quizRepository.count();
            
            if (existingQuizCount < 5) {
                log.info("Generating initial sample quizzes...");
                
                // Generate 3 sample quizzes asynchronously
                autoQuizGeneratorService.generateMultipleRandomQuizzes(3)
                    .thenAccept(quizzes -> {
                        log.info("Successfully generated {} initial quizzes", quizzes.size());
                        quizzes.forEach(quiz -> 
                            log.info("Generated quiz: {}", quiz.getTitle())
                        );
                    })
                    .exceptionally(throwable -> {
                        log.error("Failed to generate initial quizzes", throwable);
                        return null;
                    });
            } else {
                log.info("Found {} existing quizzes, skipping initial generation", existingQuizCount);
            }
        } catch (Exception e) {
            log.error("Error during startup quiz generation", e);
        }
    }
}
