package com.longlongdouble.persistence.longstory.view;

import com.longlongdouble.persistence.longstory.model.Author;
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
public class AuthorBean implements Serializable {

  // ======================================
  // =             Attributes             =
  // ======================================

  private Long id;

  private Author author;

  private int page;
  private long count;
  private List<Author> pageItems;

  private Author example = new Author();

  private Author add = new Author();

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
      author = example;
    } else {
      author = findById(getId());
    }
  }

  public Author findById(Long id) {

    return entityManager.find(Author.class, id);
  }

   /*
    * Support updating and deleting Author entities
    */

  public String update() {
    conversation.end();

    try {
      if (id == null) {
        entityManager.persist(author);
        return "search?faces-redirect=true";
      } else {
        entityManager.merge(author);
        return "view?faces-redirect=true&id=" + author.getId();
      }
    } catch (Exception e) {
      FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
      return null;
    }
  }

  public String delete() {
    conversation.end();

    try {
      Author deletableEntity = findById(getId());

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

    StringBuilder sb = new StringBuilder("SELECT count(a) FROM Author a");
    sb.append(getWhereClause());
    TypedQuery<Long> countCriteria = entityManager.createQuery(sb.toString(), Long.class);
    count = countCriteria.getSingleResult();

    // Populate pageItems

    sb = new StringBuilder("SELECT a FROM Author a");
    sb.append(getWhereClause());
    TypedQuery<Author> query = entityManager.createQuery(sb.toString(), Author.class);
    query.setFirstResult(page * getPageSize()).setMaxResults(getPageSize());
    pageItems = query.getResultList();
  }

  private String getWhereClause() {

    final StringBuilder sb = new StringBuilder(" WHERE 1 = 1");

    String firstName = example.getFirstName();
    if (firstName != null && !"".equals(firstName)) {
      sb.append(" AND LOWER(a.firstName) LIKE '%" + firstName.toLowerCase() + "%'");
    }
    String lastName = example.getLastName();
    if (lastName != null && !"".equals(lastName)) {
      sb.append(" AND LOWER(a.lastName) LIKE '%" + lastName.toLowerCase() + "%'");
    }
    String bio = example.getBio();
    if (bio != null && !"".equals(bio)) {
      sb.append(" AND LOWER(a.bio) LIKE '%" + bio.toLowerCase() + "%'");
    }

    return sb.toString();
  }

  public List<Author> getAll() {

    return entityManager.createNamedQuery(Author.FIND_ALL, Author.class).getResultList();
  }

  // ======================================
  // =             Inner Class            =
  // ======================================

  public Converter getConverter() {

    final AuthorBean ejbProxy = sessionContext.getBusinessObject(AuthorBean.class);

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

        return String.valueOf(((Author) value).getId());
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

  public Author getAuthor() {
    return this.author;
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

  public Author getExample() {
    return this.example;
  }

  public void setExample(Author example) {
    this.example = example;
  }

  public List<Author> getPageItems() {
    return this.pageItems;
  }

  public long getCount() {
    return this.count;
  }

  public Author getAdd() {
    return this.add;
  }

  public Author getAdded() {
    Author added = this.add;
    this.add = new Author();
    return added;
  }
}