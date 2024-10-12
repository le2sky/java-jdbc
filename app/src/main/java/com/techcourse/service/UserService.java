package com.techcourse.service;

import javax.sql.DataSource;
import com.interface21.transaction.support.JdbcTransaction;
import com.interface21.transaction.support.JdbcTransactionManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final JdbcTransactionManager txManager;

    public UserService(
            final UserDao userDao,
            final UserHistoryDao userHistoryDao,
            final DataSource dataSource
    ) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.txManager = new JdbcTransactionManager(dataSource);
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var transaction = txManager.getTransaction();
        transaction.begin();
        try {
            changePassword(id, newPassword, createBy, transaction);
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        }

        transaction.commit();
    }

    private void changePassword(
            final long id,
            final String newPassword,
            final String createBy,
            final JdbcTransaction transaction
    ) {
        final var user = findById(id);
        user.changePassword(newPassword);
        userDao.update(user, transaction);
        userHistoryDao.log(new UserHistory(user, createBy), transaction);
    }
}
