package com.prince.order_management_api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException{
    
    private final HttpStatus httpStatus;
    
    public ApiException(String mssg, HttpStatus status){
        super(mssg);
        this.httpStatus = status;
    }
}
