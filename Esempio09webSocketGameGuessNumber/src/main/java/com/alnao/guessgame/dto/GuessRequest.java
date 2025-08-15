package com.alnao.guessgame.dto;

/**
 * DTO for guess request
 */
public class GuessRequest {
    private Integer guess;
    
    public GuessRequest() {}
    
    public GuessRequest(Integer guess) {
        this.guess = guess;
    }
    
    public Integer getGuess() {
        return guess;
    }
    
    public void setGuess(Integer guess) {
        this.guess = guess;
    }
}
