package se.ifmo.lab3web.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import se.ifmo.lab3web.entity.Hit;
import se.ifmo.lab3web.util.Messages;

import java.util.List;
import java.util.Optional;

@Named("hitRepository")
@ApplicationScoped
public class HitRepository implements Repository<Hit> {

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    @Override
    public Hit save(Hit entity) {
        try {
            em.persist(entity);
            return entity;
        } catch (Exception e) {
            throw new RuntimeException(Messages.get("repository.hit.save.error"), e);
        }
    }

    @Override
    public void delete(Long id) {
        try {
            Hit hit = em.find(Hit.class, id);
            if (hit != null) {
                em.remove(hit);
            }
        } catch (Exception e) {
            throw new RuntimeException(Messages.get("repository.hit.delete.error") + " " + id, e);
        }
    }

    @Override
    public void update(Hit entity) {
        try {
            em.merge(entity);
        } catch (Exception e) {
            throw new RuntimeException(Messages.get("repository.hit.update.error"), e);
        }
    }

    @Override
    public Optional<Hit> findById(Long id) {
        return Optional.ofNullable(em.find(Hit.class, id));
    }

    @Override
    public List<Hit> findAll() {
        return em.createQuery("SELECT h FROM Hit h", Hit.class).getResultList();
    }

    @Override
    public List<Hit> findAllByUserId(String userId) {
        String jpql = "SELECT h FROM Hit h WHERE h.userId = :userId ORDER BY h.id DESC";
        return em.createQuery(jpql, Hit.class)
                .setParameter("userId", userId)
                .getResultList();
    }


    @Override
    public int deleteAllByUserId(String userId) {
        try {
            String jpql = "DELETE FROM Hit h WHERE h.userId = :userId";
            Query query = em.createQuery(jpql);
            query.setParameter("userId", userId);

            int value = query.executeUpdate();
            return value;
        } catch (Exception e) {
            throw new RuntimeException(Messages.get("repository.hit.deleteAll.error") + " " + userId, e);
        }
    }
}