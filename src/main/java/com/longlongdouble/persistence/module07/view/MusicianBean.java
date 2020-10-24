package com.longlongdouble.persistence.longstory.view;

import com.longlongdouble.persistence.longstory.model.Musician;
import com.longlongdouble.persistence.longstory.util.Loggable;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.TypedQuery;
import java.io.Serializable;
import java.util.List;

/**
 * @author Antonio Goncalves
 *         http://www.antoniogoncalves.org
 *         --
 */
@Named
@Stateful
@ConversationScoped
@Loggable
public class MusicianBean implements Serializable {

  // ======================================
  // =             Attributes             =
  // ======================================

  private Long id;

  private Musician musician;

  private int page;
  private long count;
  private List<Musician> pageItems;

  private Musician example = new Musician();

  private Musician add = new Musician();

  @Inject
  private Conversation conversation;

  @PersistenceContext(unitName = "longstoryPU", type = PersistenceContextType.EXTENDED)
  private EntityManager entityManager;

  @Resource
  private SessionContext sessionContext;

  // ======================================
  // =           Public Methods           =
  // ======================================

  public String create() {

    conversation.begin();
    return "create?faces-redirect=true";
  }

  public void retrieve() {

    if (FacesContext.getCurrentInstance().isPostback()) {
      return;
    }

    if (conversation.isTransient()) {
      conversation.begin();
    }

    if (id == null) {
      musician = example;
    } else {
      musician = findById(getId());
    }
  }

  public Musician findById(Long id) {

    return entityManager.find(Musician.class, id);
  }

   /*
    * Support updating and deleting Musician entities
    */

  public String update() {
    conversation.end();

    try {
      if (id == null) {
        entityManager.persist(musician);
        return "search?faces-redirect=true";
      } else {
        entityManager.merge(musician);
        return "view?faces-redirect=true&id=" + musician.getId();
      }
    } catch (Exception e) {
      FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
      return null;
    }
  }

  public String delete() {
    conversation.end();

    try {
      Musician deletableEntity = findById(getId());

      entityManager.remove(deletableEntity);
      entityManager.flush();
      return "search?faces-redirect=true";
    } catch (Exception e) {
      FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
      return null;
    }
  }

  public void search() {
    page = 0;
  }

  public void paginate() {

    // Populate count

    StringBuilder sb = new StringBuilder("SELECT count(m) FROM Musician m");
    sb.append(getWhereClause());
    TypedQuery<Long> countCriteria = entityManager.createQuery(sb.toString(), Long.class);
    count = countCriteria.getSingleResult();

    // Populate pageItems

    sb = new StringBuilder("SELECT m FROM Musician m");
    sb.append(getWhereClause());
    TypedQuery<Musician> query = entityManager.createQuery(sb.toString(), Musician.class);
    query.setFirstResult(page * getPageSize()).setMaxResults(getPageSize());
    pageItems = query.getResultList();
  }

  private String getWhereClause() {

    final StringBuilder sb = new StringBuilder(" WHERE 1 = 1");

    String firstName = example.getFirstName();
    if (firstName != null && !"".equals(firstName)) {
      sb.append(" AND LOWER(m.firstName) LIKE '%" + firstName.toLowerCase() + "%'");
    }
    String lastName = example.getLastName();
    if (lastName != null && !"".equals(lastName)) {
      sb.append(" AND LOWER(m.lastName) LIKE '%" + lastName.toLowerCase() + "%'");
    }
    String bio = example.getBio();
    if (bio != null && !"".equals(bio)) {
      sb.append(" AND LOWER(m.bio) LIKE '%" + bio.toLowerCase() + "%'");
    }
    String preferredInstrument = example.getPreferredInstrument();
    if (preferredInstrument != null && !"".equals(preferredInstrument)) {
      sb.append(" AND LOWER(m.preferredInstrument) LIKE '%" + preferredInstrument.toLowerCase() + "%'");
    }

    return sb.toString();
  }

  public List<Musician> getAll() {

    return entityManager.createNamedQuery(Musician.FIND_ALL, Musician.class).getResultList();
  }

  // ======================================
  // =             Inner Class            =
  // ======================================

  public Converter getConverter() {

    final MusicianBean ejbProxy = sessionContext.getBusinessObject(MusicianBean.class);

    return new Converter() {

      @Override
      public Object getAsObject(FacesContext context,
                                UIComponent component, String value) {

        return ejbProxy.findById(Long.valueOf(value));
      }

      @Override
      public String getAsString(FacesContext context,
                                UIComponent component, Object value) {

        if (value == null) {
          return "";
        }

        return String.valueOf(((Musician) value).getId());
      }
    };
  }

  // ======================================
  // =          Getters & Setters         =
  // ======================================

  public Long getId() {
    return this.id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Musician getMusician() {
    return this.musician;
  }

  public int getPage() {
    return this.page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public int getPageSize() {
    return 10;
  }

  public Musician getExample() {
    return this.example;
  }

  public void setExample(Musician example) {
    this.example = example;
  }

  public List<Musician> getPageItems() {
    return this.pageItems;
  }

  public long getCount() {
    return this.count;
  }

  public Musician getAdd() {
    return this.add;
  }

  public Musician getAdded() {
    Musician added = this.add;
    this.add = new Musician();
    return added;
  }
}