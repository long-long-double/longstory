package com.longlongdouble.persistence.longstory.rest;

import com.longlongdouble.persistence.longstory.model.Author;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import java.util.List;

/**
 * @author Antonio Goncalves
 *         http://www.antoniogoncalves.org
 *         --
 */
@Stateless
@Path("/authors")
public class AuthorEndpoint {

  // ======================================
  // =             Attributes             =
  // ======================================

  @PersistenceContext(unitName = "longstoryPU")
  private EntityManager em;

  // ======================================
  // =           Public Methods           =
  // ======================================

  @POST
  @Consumes("application/xml")
  public Response create(Author entity) {
    em.persist(entity);
    return Response.created(UriBuilder.fromResource(AuthorEndpoint.class).path(String.valueOf(entity.getId())).build()).build();
  }

  @DELETE
  @Path("/{id:[0-9][0-9]*}")
  public Response deleteById(@PathParam("id") Long id) {
    Author entity = em.find(Author.class, id);
    if (entity == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    em.remove(entity);
    return Response.noContent().build();
  }

  @GET
  @Path("/{id:[0-9][0-9]*}")
  @Produces("application/xml")
  public Response findById(@PathParam("id") Long id) {
    TypedQuery<Author> findByIdQuery = em.createQuery("SELECT DISTINCT a FROM Author a WHERE a.id = :entityId ORDER BY a.id", Author.class);
    findByIdQuery.setParameter("entityId", id);
    Author entity;
    try {
      entity = findByIdQuery.getSingleResult();
    } catch (NoResultException nre) {
      entity = null;
    }
    if (entity == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok(entity).build();
  }

  @GET
  @Produces("application/xml")
  public List<Author> listAll(@QueryParam("start") Integer startPosition, @QueryParam("max") Integer maxResult) {
    TypedQuery<Author> findAllQuery = em.createQuery("SELECT DISTINCT a FROM Author a ORDER BY a.id", Author.class);
    if (startPosition != null) {
      findAllQuery.setFirstResult(startPosition);
    }
    if (maxResult != null) {
      findAllQuery.setMaxResults(maxResult);
    }
    final List<Author> results = findAllQuery.getResultList();
    return results;
  }

  @PUT
  @Path("/{id:[0-9][0-9]*}")
  @Consumes("application/xml")
  public Response update(Author entity) {
    entity = em.merge(entity);
    return Response.noContent().build();
  }
}