package by.dimadl.ormlib.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * Class methods are useful for working with annotations.
 * .
 */
public class AnnotationsUtil {

    /**
     * Private constructor since it is a static only class .
     */
    private AnnotationsUtil() {

    }

    /**
     * The method returns the fields of a certain class annotated specific
     * annotation.
     *
     * @param clazz
     *            The class for search annotated fields.
     * @param annotation
     *            Specific annotation for search field in class.
     * @return Annotated fields.
     */
    public static List<Field> getAnnotatedFields(Class<?> clazz,
                                                 Class<? extends Annotation> annotation) {
        Field[] allFields = clazz.getDeclaredFields();
        List<Field> fields = new LinkedList<>();

        for (int i = 0; i < allFields.length; i++) {

            if (allFields[i].isAnnotationPresent(annotation)) {

                fields.add(allFields[i]);

            }

        }
        return fields;
    }

}