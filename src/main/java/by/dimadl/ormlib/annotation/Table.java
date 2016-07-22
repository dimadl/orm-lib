package by.dimadl.ormlib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a class as a table of database.
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
 * @see Column
 *
 */
@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Table {

    String name();

}