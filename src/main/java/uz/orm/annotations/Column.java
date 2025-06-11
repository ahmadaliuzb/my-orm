package uz.orm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    /**
     * Agar null bo'lsa, field nomi camelCase dan snake_case ga o'tkaziladi
     */
    String name() default "";

    boolean nullable() default true;

    boolean unique() default false;
}
