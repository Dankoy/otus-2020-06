package ru.dankoy.otus.jpql.jdbc.mapper;

/**
 * Сохратяет объект в базу, читает объект из базы
 * @param <T>
 */
public interface JdbcMapper<T> {
    long insert(T objectData);

    void update(T objectData);

    void insertOrUpdate(T objectData);

    T findById(Object id, Class<T> clazz);
}
