package com.longlongdouble.persistence.longstory.view;

import com.longlongdouble.persistence.longstory.model.Book;
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
import javax.transaction.Transactional;
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
public class BookBean implements Serializable {

  // ======================================
  // =             Attributes             =
  // ======================================

  private Long id;

  private Book book;

  private int page;
  private long count;
  private List<Book> pageItems;

  private Book example = new Book();

  private Book add = new Book();

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
      book = example;
    } else {
      book = findById(getId());
    }
  }

  public Book findById(Long id) {

    return entityManager.find(Book.class, id);
  }

   /*
    * Support updating and deleting Book entities
    */

  public String update() {
    conversation.end();

    try {
      if (id == null) {
        entityManager.persist(book);
        return "search?faces-redirect=true";
      } else {
        entityManager.merge(book);
        return "view?faces-redirect=true&id=" + book.getId();
      }
    } catch (Exception e) {
      FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
      return null;
    }
  }

  public String delete() {
    conversation.end();

    try {
      Book deletableEntity = findById(getId());

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

    StringBuilder sb = new StringBuilder("SELECT count(b) FROM Book b");
    sb.append(getWhereClause());
    TypedQuery<Long> countCriteria = entityManager.createQuery(sb.toString(), Long.class);
    count = countCriteria.getSingleResult();

    // Populate pageItems

    sb = new StringBuilder("SELECT b FROM Book b");
    sb.append(getWhereClause());
    TypedQuery<Book> query = entityManager.createQuery(sb.toString(), Book.class);
    query.setFirstResult(page * getPageSize()).setMaxResults(getPageSize());
    pageItems = query.getResultList();
  }

  private String getWhereClause() {

    final StringBuilder sb = new StringBuilder(" WHERE 1 = 1");

    String title = example.getTitle();
    if (title != null && !"".equals(title)) {
      sb.append(" AND LOWER(b.title) LIKE '%" + title.toLowerCase() + "%'");
    }
    String description = example.getDescription();
    if (description != null && !"".equals(description)) {
      sb.append(" AND LOWER(b.description) LIKE '%" + description.toLowerCase() + "%'");
    }
    String isbn = example.getIsbn();
    if (isbn != null && !"".equals(isbn)) {
      sb.append(" AND LOWER(b.isbn) LIKE '%" + isbn.toLowerCase() + "%'");
    }

    return sb.toString();
  }

  public List<Book> getAll() {

    return entityManager.createNamedQuery(Book.FIND_ALL, Book.class).getResultList();
  }

  // ======================================
  // =             Inner Class            =
  // ======================================

  public Converter getConverter() {

    final BookBean ejbProxy = sessionContext.getBusinessObject(BookBean.class);

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

        return String.valueOf(((Book) value).getId());
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

  public Book getBook() {
    return this.book;
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

  public Book getExample() {
    return this.example;
  }

  public void setExample(Book example) {
    this.example = example;
  }

  public List<Book> getPageItems() {
    return this.pageItems;
  }

  public long getCount() {
    return this.count;
  }

  public Book getAdd() {
    return this.add;
  }

  public Book getAdded() {
    Book added = this.add;
    this.add = new Book();
    return added;
  }
}