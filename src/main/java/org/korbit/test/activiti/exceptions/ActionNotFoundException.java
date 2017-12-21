package org.korbit.test.activiti.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ActionNotFoundException extends RuntimeException {
    public ActionNotFoundException( String type) {
        super("Action Type "+ type + " not defined" );
    }
}
