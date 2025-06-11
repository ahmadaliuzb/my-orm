package uz.orm.utils;


import java.util.HashMap;
import java.util.Map;

public class TypeMapper {

    private static final Map<Class<?>, String> typeMap = new HashMap<>();

    static {
        typeMap.put(String.class, "VARCHAR");
        typeMap.put(int.class, "INTEGER");
        typeMap.put(Integer.class, "INTEGER");
        typeMap.put(long.class, "BIGINT");
        typeMap.put(Long.class, "BIGINT");
        typeMap.put(boolean.class, "BOOLEAN");
        typeMap.put(Boolean.class, "BOOLEAN");
        typeMap.put(double.class, "DOUBLE PRECISION");
        typeMap.put(Float.class, "REAL");
    }

    public static String mapJavaTypeToSQL(Class<?> type) {
        return typeMap.get(type);
    }


}
