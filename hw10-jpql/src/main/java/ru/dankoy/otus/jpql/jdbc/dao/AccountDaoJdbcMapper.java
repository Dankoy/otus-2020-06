package ru.dankoy.otus.jpql.jdbc.dao;

import ru.dankoy.otus.jpql.core.dao.AccountDao;
import ru.dankoy.otus.jpql.core.dao.UserDaoException;
import ru.dankoy.otus.jpql.core.model.Account;
import ru.dankoy.otus.jpql.core.sessionmanager.SessionManager;
import ru.dankoy.otus.jpql.jdbc.mapper.JdbcMapper;
import ru.dankoy.otus.jpql.jdbc.sessionmanager.SessionManagerJdbc;

import java.util.Optional;

public class AccountDaoJdbcMapper implements AccountDao {

    private final SessionManagerJdbc sessionManager;
    private final JdbcMapper<Account> jdbcMapper;

    public AccountDaoJdbcMapper(SessionManagerJdbc sessionManager, JdbcMapper<Account> jdbcMapper) {
        this.sessionManager = sessionManager;
        this.jdbcMapper = jdbcMapper;
    }

    @Override
    public Optional<Account> findById(long id) {

        Optional<Account> account = Optional.ofNullable(jdbcMapper.findById(id, Account.class));

        return account;
    }

    @Override
    public long insertAccount(Account account) {

        try {
            return jdbcMapper.insert(account);
        } catch (Exception e) {
            throw new UserDaoException(e);
        }

    }

    @Override
    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
