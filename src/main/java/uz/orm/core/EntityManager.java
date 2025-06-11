package uz.orm.core;

import uz.orm.config.DBConfig;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EntityManager {
    public <T> void save(T entity) {
        Class<?> entityClass = entity.getClass();
        String tableName = entityClass.getSimpleName().toLowerCase();
        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        List<Object> values = new ArrayList<>();

        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                if (!columns.isEmpty()) {
                    columns.append(", ");
                    placeholders.append(", ");
                }
                columns.append(field.getName());
                placeholders.append("?");
                values.add(field.get(entity));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Could not access field: " + field.getName(), e);
            }
        }

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not persist entity: " + entity, e);
        }
    }

    public <T> T findById(Class<T> entityClass, Object id) {
        String tableName = entityClass.getSimpleName().toLowerCase();
        String sql = String.format("SELECT * FROM %s WHERE id = ?", tableName);

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                T entity = entityClass.getDeclaredConstructor().newInstance();
                for (Field field : entityClass.getDeclaredFields()) {
                    field.setAccessible(true);
                    field.set(entity, rs.getObject(field.getName()));
                }
                return entity;
            }
        } catch (SQLException | ReflectiveOperationException e) {
            throw new RuntimeException("Could not find entity: " + entityClass.getSimpleName(), e);
        }
        return null;
    }

    public <T> List<T> findAll(Class<T> entityClass) {
        List<T> result = new ArrayList<>();
        String tableName = entityClass.getSimpleName().toLowerCase();
        String sql = String.format("SELECT * FROM %s", tableName);

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                T entity = entityClass.getDeclaredConstructor().newInstance();
                for (Field field : entityClass.getDeclaredFields()) {
                    field.setAccessible(true);
                    field.set(entity, rs.getObject(field.getName()));
                }
                result.add(entity);
            }
        } catch (SQLException | ReflectiveOperationException e) {
            throw new RuntimeException("Could not find all entities: " + entityClass.getSimpleName(), e);
        }
        return result;
    }

    public <T> void update(T entity) {
        Class<?> entityClass = entity.getClass();
        String tableName = entityClass.getSimpleName().toLowerCase();
        StringBuilder setClause = new StringBuilder();
        List<Object> values = new ArrayList<>();
        Object id = null;

        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                if (field.getName().equals("id")) {
                    id = field.get(entity);
                } else {
                    if (!setClause.isEmpty()) {
                        setClause.append(", ");
                    }
                    setClause.append(field.getName()).append(" = ?");
                    values.add(field.get(entity));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Could not access field: " + field.getName(), e);
            }
        }

        values.add(id);
        String sql = String.format("UPDATE %s SET %s WHERE id = ?", tableName, setClause);
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not merge entity: " + entity, e);
        }
    }

    public <T> void remove(Class<T> entityClass, Object id) {
        String tableName = entityClass.getSimpleName().toLowerCase();
        String sql = String.format("DELETE FROM %s WHERE id = ?", tableName);
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not remove entity: " + entityClass.getSimpleName(), e);
        }
    }
}