package uz.orm.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {
    /**
     * Agar null bo'lsa, class nomi camelCase dan snake_case ga o'tkaziladi
     */
    String name() default "";
}
