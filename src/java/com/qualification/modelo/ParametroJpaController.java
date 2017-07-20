/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qualification.modelo;

import com.qualification.modelo.exceptions.NonexistentEntityException;
import com.qualification.modelo.exceptions.PreexistingEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author Desarrollo
 */
public class ParametroJpaController implements Serializable {

    public ParametroJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Parametro parametro) throws PreexistingEntityException, Exception {
        if (parametro.getInterroganteList() == null) {
            parametro.setInterroganteList(new ArrayList<Interrogante>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Interrogante> attachedInterroganteList = new ArrayList<Interrogante>();
            for (Interrogante interroganteListInterroganteToAttach : parametro.getInterroganteList()) {
                interroganteListInterroganteToAttach = em.getReference(interroganteListInterroganteToAttach.getClass(), interroganteListInterroganteToAttach.getIdinterrogante());
                attachedInterroganteList.add(interroganteListInterroganteToAttach);
            }
            parametro.setInterroganteList(attachedInterroganteList);
            em.persist(parametro);
            for (Interrogante interroganteListInterrogante : parametro.getInterroganteList()) {
                interroganteListInterrogante.getParametroList().add(parametro);
                interroganteListInterrogante = em.merge(interroganteListInterrogante);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findParametro(parametro.getIdparametro()) != null) {
                throw new PreexistingEntityException("Parametro " + parametro + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Parametro parametro) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Parametro persistentParametro = em.find(Parametro.class, parametro.getIdparametro());
            List<Interrogante> interroganteListOld = persistentParametro.getInterroganteList();
            List<Interrogante> interroganteListNew = parametro.getInterroganteList();
            List<Interrogante> attachedInterroganteListNew = new ArrayList<Interrogante>();
            for (Interrogante interroganteListNewInterroganteToAttach : interroganteListNew) {
                interroganteListNewInterroganteToAttach = em.getReference(interroganteListNewInterroganteToAttach.getClass(), interroganteListNewInterroganteToAttach.getIdinterrogante());
                attachedInterroganteListNew.add(interroganteListNewInterroganteToAttach);
            }
            interroganteListNew = attachedInterroganteListNew;
            parametro.setInterroganteList(interroganteListNew);
            parametro = em.merge(parametro);
            for (Interrogante interroganteListOldInterrogante : interroganteListOld) {
                if (!interroganteListNew.contains(interroganteListOldInterrogante)) {
                    interroganteListOldInterrogante.getParametroList().remove(parametro);
                    interroganteListOldInterrogante = em.merge(interroganteListOldInterrogante);
                }
            }
            for (Interrogante interroganteListNewInterrogante : interroganteListNew) {
                if (!interroganteListOld.contains(interroganteListNewInterrogante)) {
                    interroganteListNewInterrogante.getParametroList().add(parametro);
                    interroganteListNewInterrogante = em.merge(interroganteListNewInterrogante);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = parametro.getIdparametro();
                if (findParametro(id) == null) {
                    throw new NonexistentEntityException("The parametro with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(String id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Parametro parametro;
            try {
                parametro = em.getReference(Parametro.class, id);
                parametro.getIdparametro();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The parametro with id " + id + " no longer exists.", enfe);
            }
            List<Interrogante> interroganteList = parametro.getInterroganteList();
            for (Interrogante interroganteListInterrogante : interroganteList) {
                interroganteListInterrogante.getParametroList().remove(parametro);
                interroganteListInterrogante = em.merge(interroganteListInterrogante);
            }
            em.remove(parametro);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Parametro> findParametroEntities() {
        return findParametroEntities(true, -1, -1);
    }

    public List<Parametro> findParametroEntities(int maxResults, int firstResult) {
        return findParametroEntities(false, maxResults, firstResult);
    }

    private List<Parametro> findParametroEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Parametro.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Parametro findParametro(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Parametro.class, id);
        } finally {
            em.close();
        }
    }

    public int getParametroCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Parametro> rt = cq.from(Parametro.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
