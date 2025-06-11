package uz.orm.core;


import org.reflections.Reflections;
import uz.orm.annotations.Column;
import uz.orm.annotations.Entity;
import uz.orm.config.DBConfig;
import uz.orm.exceptions.ORMSynchronizerException;
import uz.orm.utils.TypeMapper;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ORMSynchronizer {
    public static void execute(String basePackage) {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> entityClasses = reflections.getTypesAnnotatedWith(Entity.class);

        for (Class<?> clazz : entityClasses) {
            ORMSynchronizer.scanTable(clazz);
        }
    }

    private static void scanTable(Class<?> entityClass) {
        if (!entityClass.isAnnotationPresent(Entity.class)) return;
        Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
        String tableName = getEffectiveName(entityAnnotation.name(), entityClass.getSimpleName());
        Map<String, String> columns = new LinkedHashMap<>();
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column col = field.getAnnotation(Column.class);
                String columnName = getEffectiveName(col.name(), field.getName());
                String columnType = TypeMapper.mapJavaTypeToSQL(field.getType());
                if (columnType == null) continue;

                StringBuilder columnDef = new StringBuilder(columnName + " " + columnType);
                if (!col.nullable()) columnDef.append(" NOT NULL");
                if (col.unique()) columnDef.append(" UNIQUE");
                columns.put(columnName, columnDef.toString());
            }
        }

        try (Connection conn = DBConfig.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getTables(null, null, tableName, null);
            if (!rs.next()) {
                StringBuilder createSQL = new StringBuilder("CREATE TABLE " + tableName + " (");
                createSQL.append(String.join(", ", columns.values()));
                createSQL.append(");");

                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(createSQL.toString());
                    System.out.println("Table created: " + tableName);
                }
            } else {
                ResultSet existingColumns = meta.getColumns(null, null, tableName, null);
                Set<String> existing = new HashSet<>();
                while (existingColumns.next()) {
                    existing.add(existingColumns.getString("COLUMN_NAME"));
                }
                for (String column : columns.keySet()) {
                    if (!existing.contains(column)) {
                        String alter = "ALTER TABLE " + tableName + " ADD COLUMN " + columns.get(column);
                        try (Statement stmt = conn.createStatement()) {
                            stmt.executeUpdate(alter);
                            System.out.println("Added column: " + column);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new ORMSynchronizerException("Schema sync failed");
        }
    }

    private static String getEffectiveName(String annotatedName, String defaultName) {
        if (annotatedName == null) return defaultName;
        if (annotatedName.isEmpty()) {
            return camelToSnake(defaultName);
        }
        return annotatedName;
    }

    private static String camelToSnake(String input) {
        return input.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }
}

