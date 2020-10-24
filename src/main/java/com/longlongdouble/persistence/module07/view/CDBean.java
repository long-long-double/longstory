package com.longlongdouble.persistence.longstory.view;

import com.longlongdouble.persistence.longstory.model.CD;
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
@Named("cdBean")
@Stateful
@ConversationScoped
@Loggable
public class CDBean implements Serializable {

  // ======================================
  // =             Attributes             =
  // ======================================

  private Long id;

  private CD cd;

  private int page;
  private long count;
  private List<CD> pageItems;

  private CD example = new CD();

  private CD add = new CD();

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
      cd = example;
    } else {
      cd = findById(getId());
    }
  }

  public CD findById(Long id) {

    return entityManager.find(CD.class, id);
  }

   /*
    * Support updating and deleting CD entities
    */

  public String update() {
    conversation.end();

    try {
      if (id == null) {
        entityManager.persist(cd);
        return "search?faces-redirect=true";
      } else {
        entityManager.merge(cd);
        return "view?faces-redirect=true&id=" + cd.getId();
      }
    } catch (Exception e) {
      FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
      return null;
    }
  }

  public String delete() {
    conversation.end();

    try {
      CD deletableEntity = findById(getId());

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

    StringBuilder sb = new StringBuilder("SELECT count(c) FROM CD c");
    sb.append(getWhereClause());
    TypedQuery<Long> countCriteria = entityManager.createQuery(sb.toString(), Long.class);
    count = countCriteria.getSingleResult();

    // Populate pageItems

    sb = new StringBuilder("SELECT c FROM CD c");
    sb.append(getWhereClause());
    TypedQuery<CD> query = entityManager.createQuery(sb.toString(), CD.class);
    query.setFirstResult(page * getPageSize()).setMaxResults(getPageSize());
    pageItems = query.getResultList();
  }

  private String getWhereClause() {

    final StringBuilder sb = new StringBuilder(" WHERE 1 = 1");

    String title = example.getTitle();
    if (title != null && !"".equals(title)) {
      sb.append(" AND LOWER(c.title) LIKE '%" + title.toLowerCase() + "%'");
    }
    String description = example.getDescription();
    if (description != null && !"".equals(description)) {
      sb.append(" AND LOWER(c.description) LIKE '%" + description.toLowerCase() + "%'");
    }
    String musicCompany = example.getMusicCompany();
    if (musicCompany != null && !"".equals(musicCompany)) {
      sb.append(" AND LOWER(c.musicCompany) LIKE '%" + musicCompany.toLowerCase() + "%'");
    }
    String genre = example.getGenre();
    if (genre != null && !"".equals(genre)) {
      sb.append(" AND LOWER(c.genre) LIKE '%" + genre.toLowerCase() + "%'");
    }

    return sb.toString();
  }

  public List<CD> getAll() {

    return entityManager.createNamedQuery(CD.FIND_ALL, CD.class).getResultList();
  }

  // ======================================
  // =             Inner Class            =
  // ======================================

  public Converter getConverter() {

    final CDBean ejbProxy = sessionContext.getBusinessObject(CDBean.class);

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

        return String.valueOf(((CD) value).getId());
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

  public CD getCD() {
    return this.cd;
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

  public CD getExample() {
    return this.example;
  }

  public void setExample(CD example) {
    this.example = example;
  }

  public List<CD> getPageItems() {
    return this.pageItems;
  }

  public long getCount() {
    return this.count;
  }

  public CD getAdd() {
    return this.add;
  }

  public CD getAdded() {
    CD added = this.add;
    this.add = new CD();
    return added;
  }
}