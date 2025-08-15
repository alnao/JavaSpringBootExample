package com.alnao.guessgame.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "game.scoring")
public class GameScoringConfig {
    private int guessCorrect;
    private int targetFound;
    private int guessWrongPenalty;

    // Limite tentativi per 24h
    private int maxGuessesPerDay = 6;

    public int getGuessCorrect() {
        return guessCorrect;
    }
    public void setGuessCorrect(int guessCorrect) {
        this.guessCorrect = guessCorrect;
    }
    public int getTargetFound() {
        return targetFound;
    }
    public void setTargetFound(int targetFound) {
        this.targetFound = targetFound;
    }
    public int getGuessWrongPenalty() {
        return guessWrongPenalty;
    }
    public void setGuessWrongPenalty(int guessWrongPenalty) {
        this.guessWrongPenalty = guessWrongPenalty;
    }
    public int getMaxGuessesPerDay() {
        return maxGuessesPerDay;
    }
    public void setMaxGuessesPerDay(int maxGuessesPerDay) {
        this.maxGuessesPerDay = maxGuessesPerDay;
    }
}
