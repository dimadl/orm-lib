package by.dimadl.ormlib.dto;

import java.io.Serializable;

/**
 * The Class Entity.
 */
public abstract class Entity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7375323344297786949L;

    /** The id. */
    private Long id;

    /**
     * Instantiates a new dto.
     */
    public Entity() {

    }

    /**
     * Instantiates a new dto.
     *
     * @param id
     *            the id
     */
    public Entity(Long id) {

        this.id = id;

    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id
     *            the new id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Entity other = (Entity) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}