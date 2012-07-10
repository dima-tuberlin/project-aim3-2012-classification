package de.tuberlin.dima.aim3.oc.input.type;


public class WikiPage {

  String title;

  String id;

  public WikiPage() {
  }

  public WikiPage(String title, String id) {
    super();
    this.title = title;
    this.id = id;
  }

  @Override
  public int hashCode() {
    return 31 * (title.hashCode() + id.hashCode());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof WikiPage) {
      WikiPage other = (WikiPage) obj;
      if (other != null) {
        if (other.title != null && title != null && other.title.equals(title)) {
          if (other.id != null && id != null && other.id.equals(id)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return title + " (" + id + ")";
  }

  public String getTitle() {
    return title;
  }

  public String getId() {
    return id;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setId(String id) {
    this.id = id;
  }

}
