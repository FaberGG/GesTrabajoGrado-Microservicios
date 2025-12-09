package co.unicauca.submission.domain.specification;

/**
 * Interfaz base para el patrón Specification.
 * Permite encapsular reglas de negocio de forma reutilizable y combinable.
 *
 * @param <T> Tipo de entidad sobre la que aplica la especificación
 */
public interface Specification<T> {

    /**
     * Verifica si la entidad cumple con la especificación.
     *
     * @param entity Entidad a validar
     * @return true si cumple, false en caso contrario
     */
    boolean isSatisfiedBy(T entity);

    /**
     * Obtiene la razón por la cual la especificación no se cumple.
     *
     * @param entity Entidad que no cumple
     * @return Mensaje descriptivo de por qué no cumple
     */
    String getRazonRechazo(T entity);

    /**
     * Combina esta especificación con otra usando AND lógico.
     */
    default Specification<T> and(Specification<T> other) {
        return new AndSpecification<>(this, other);
    }

    /**
     * Combina esta especificación con otra usando OR lógico.
     */
    default Specification<T> or(Specification<T> other) {
        return new OrSpecification<>(this, other);
    }

    /**
     * Niega esta especificación.
     */
    default Specification<T> not() {
        return new NotSpecification<>(this);
    }
}

/**
 * Specification que combina dos specifications con AND.
 */
class AndSpecification<T> implements Specification<T> {

    private final Specification<T> left;
    private final Specification<T> right;

    public AndSpecification(Specification<T> left, Specification<T> right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean isSatisfiedBy(T entity) {
        return left.isSatisfiedBy(entity) && right.isSatisfiedBy(entity);
    }

    @Override
    public String getRazonRechazo(T entity) {
        if (!left.isSatisfiedBy(entity)) {
            return left.getRazonRechazo(entity);
        }
        if (!right.isSatisfiedBy(entity)) {
            return right.getRazonRechazo(entity);
        }
        return null;
    }
}

/**
 * Specification que combina dos specifications con OR.
 */
class OrSpecification<T> implements Specification<T> {

    private final Specification<T> left;
    private final Specification<T> right;

    public OrSpecification(Specification<T> left, Specification<T> right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean isSatisfiedBy(T entity) {
        return left.isSatisfiedBy(entity) || right.isSatisfiedBy(entity);
    }

    @Override
    public String getRazonRechazo(T entity) {
        return "No cumple ninguna de las condiciones: " +
               left.getRazonRechazo(entity) + " O " + right.getRazonRechazo(entity);
    }
}

/**
 * Specification que niega otra specification.
 */
class NotSpecification<T> implements Specification<T> {

    private final Specification<T> spec;

    public NotSpecification(Specification<T> spec) {
        this.spec = spec;
    }

    @Override
    public boolean isSatisfiedBy(T entity) {
        return !spec.isSatisfiedBy(entity);
    }

    @Override
    public String getRazonRechazo(T entity) {
        return "No debe cumplir: " + spec.getRazonRechazo(entity);
    }
}

