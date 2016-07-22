package by.dimadl.ormlib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Mark a field as a column of database table.
 *
 * <pre class="code"><code class="java">
 *
 * Example:
 *
 * 	&#064;Table(name = "dto")
 * 	public class Entity {
 *
 * 		&#064;Column(name="description") private String description;
 *
 * 	}
 *
 * Annotation parameters used in the generation queries for table.
 *
 * </code></pre>
 *
 *
 */
@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
@Inherited
public @interface Column {

    String name();

}