package com.github.nameserver.constant;

public class HttpConstants {

    public enum HttpStatus{

        OK("200", "OK"),
        /**
         * HTTP Status-Code 404: Not Found.
         */
        HTTP_NOT_FOUND("404", "Not Found"),

        /**
         * HTTP Status-Code 405: Method Not Allowed.
         */
       HTTP_BAD_METHOD("405", "Method Not Allowed"),
        ;
        private String code;
        private String message;

        HttpStatus(String code, String message){
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
