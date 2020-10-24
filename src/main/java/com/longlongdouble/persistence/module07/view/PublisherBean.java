package com.longlongdouble.persistence.longstory.view;

import com.longlongdouble.persistence.longstory.model.Publisher;
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
public class PublisherBean implements Serializable {

  // ======================================
  // =             Attributes             =
  // ======================================

  private Long id;

  private Publisher publisher;

  private int page;
  private long count;
  private List<Publisher> pageItems;

  private Publisher example = new Publisher();

  private Publisher add = new Publisher();

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
      publisher = example;
    } else {
      publisher = findById(getId());
    }
  }

  public Publisher findById(Long id) {

    return entityManager.find(Publisher.class, id);
  }

   /*
    * Support updating and deleting Publisher entities
    */

  public String update() {
    conversation.end();

    try {
      if (id == null) {
        entityManager.persist(publisher);
        return "search?faces-redirect=true";
      } else {
        entityManager.merge(publisher);
        return "view?faces-redirect=true&id=" + publisher.getId();
      }
    } catch (Exception e) {
      FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
      return null;
    }
  }

  public String delete() {
    conversation.end();

    try {
      Publisher deletableEntity = findById(getId());

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

    StringBuilder sb = new StringBuilder("SELECT count(p) FROM Publisher p");
    sb.append(getWhereClause());
    TypedQuery<Long> countCriteria = entityManager.createQuery(sb.toString(), Long.class);
    count = countCriteria.getSingleResult();

    // Populate pageItems

    sb = new StringBuilder("SELECT p FROM Publisher p");
    sb.append(getWhereClause());
    TypedQuery<Publisher> query = entityManager.createQuery(sb.toString(), Publisher.class);
    query.setFirstResult(page * getPageSize()).setMaxResults(getPageSize());
    pageItems = query.getResultList();
  }

  private String getWhereClause() {

    final StringBuilder sb = new StringBuilder(" WHERE 1 = 1");

    String name = example.getName();
    if (name != null && !"".equals(name)) {
      sb.append(" AND LOWER(p.name) LIKE '%" + name.toLowerCase() + "%'");
    }

    return sb.toString();
  }

  public List<Publisher> getAll() {

    return entityManager.createNamedQuery(Publisher.FIND_ALL, Publisher.class).getResultList();
  }

  // ======================================
  // =             Inner Class            =
  // ======================================

  public Converter getConverter() {

    final PublisherBean ejbProxy = sessionContext.getBusinessObject(PublisherBean.class);

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

        return String.valueOf(((Publisher) value).getId());
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

  public Publisher getPublisher() {
    return this.publisher;
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

  public Publisher getExample() {
    return this.example;
  }

  public void setExample(Publisher example) {
    this.example = example;
  }

  public List<Publisher> getPageItems() {
    return this.pageItems;
  }

  public long getCount() {
    return this.count;
  }

  public Publisher getAdd() {
    return this.add;
  }

  public Publisher getAdded() {
    Publisher added = this.add;
    this.add = new Publisher();
    return added;
  }
}