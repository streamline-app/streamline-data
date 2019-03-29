package org.thomaschen.streamlinedata.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class InvalidArithmeticException extends RuntimeException {

    private String resourceName;
    private String valueName;
    private Object fieldValue;

    public InvalidArithmeticException(String resourceName, String valueName, Object fieldValue) {
        super(String.format("%s has invalid %s of %s", resourceName, valueName, fieldValue));
        this.resourceName = resourceName;
        this.valueName = valueName;
        this.fieldValue = fieldValue;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getValueName() {
        return valueName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}
