package ru.dankoy.otus.core.dao;

public class AccountDaoException extends RuntimeException {
    public AccountDaoException(Exception ex) {
        super(ex);
    }
}
