package com.smarttravel.common.exception;

/**
 * Individual field validation error details.
 */
public class ValidationError {

    private String field;
    private Object rejectedValue;
    private String message;

    public ValidationError() {
    }

    public ValidationError(String field, Object rejectedValue, String message) {
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.message = message;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String field;
        private Object rejectedValue;
        private String message;

        public Builder field(String field) {
            this.field = field;
            return this;
        }

        public Builder rejectedValue(Object rejectedValue) {
            this.rejectedValue = rejectedValue;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public ValidationError build() {
            return new ValidationError(field, rejectedValue, message);
        }
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }

    public void setRejectedValue(Object rejectedValue) {
        this.rejectedValue = rejectedValue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
