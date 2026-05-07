package facade;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Locale;

@Stateless
public abstract class AbstractFacade {
    protected static final Locale TURKISH = Locale.forLanguageTag("tr-TR");
    @PersistenceContext(unitName = "CloudSystemPu")
    protected EntityManager entityManager;

}
