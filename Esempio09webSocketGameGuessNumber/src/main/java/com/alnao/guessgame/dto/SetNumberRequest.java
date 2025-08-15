package com.alnao.guessgame.dto;

/**
 * DTO for setting a number
 */
public class SetNumberRequest {
    private Integer number;
    
    public SetNumberRequest() {}
    
    public SetNumberRequest(Integer number) {
        this.number = number;
    }
    
    public Integer getNumber() {
        return number;
    }
    
    public void setNumber(Integer number) {
        this.number = number;
    }
}
