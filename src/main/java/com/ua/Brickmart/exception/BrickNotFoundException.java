package com.ua.Brickmart.exception;

public class BrickNotFoundException extends RuntimeException {
    public BrickNotFoundException(Long id) {
        super("Цеглину з id=" + id + " не знайдено");
    }
}
