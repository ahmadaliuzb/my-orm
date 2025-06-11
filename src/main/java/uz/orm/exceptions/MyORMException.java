package uz.orm.exceptions;

public class MyORMException extends RuntimeException {
    String message;

    public MyORMException(String message) {
        this.message = message;
    }
}
