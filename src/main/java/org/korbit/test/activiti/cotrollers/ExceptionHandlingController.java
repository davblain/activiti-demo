package org.korbit.test.activiti.cotrollers;

import org.korbit.test.activiti.dto.ExceptionResponse;
import org.korbit.test.activiti.exceptions.ActionNotFoundException;
import org.korbit.test.activiti.exceptions.TaskNotFoundException;
import org.korbit.test.activiti.exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionHandlingController {
    @ExceptionHandler({TaskNotFoundException.class, ActionNotFoundException.class, UserNotFoundException.class})
    public ResponseEntity<ExceptionResponse> resourceNotFound(RuntimeException ex) {
        ExceptionResponse response = new ExceptionResponse();
        response.setErrorCode("Not Found");
        response.setErrorMessage(ex.getMessage());
        return new ResponseEntity<ExceptionResponse>(response, HttpStatus.NOT_FOUND);
    }
}
