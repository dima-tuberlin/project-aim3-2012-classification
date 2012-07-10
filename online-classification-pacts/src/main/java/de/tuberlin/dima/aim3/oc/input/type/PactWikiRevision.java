package de.tuberlin.dima.aim3.oc.input.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Serializable wrapper for {@link WikiRevision} instances.
 * 
 * @author Florian Feigenbutz <florian.feigenbutz@campus.tu-berlin.de>
 * 
 */
public class PactWikiRevision extends BaseValue {

  private WikiRevision revision;

  /**
   * Nullary constructor required by stratosphere.
   */
  public PactWikiRevision() {
    super();
    revision = null;
  }

  public PactWikiRevision(WikiRevision revision) {
    super();
    this.revision = revision;
  }

  public WikiRevision getRevision() {
    return revision;
  }

  @Override
  public void write(DataOutput out) throws IOException {
    writeUTFNullSafe(out, revision.getPage().getId());
    writeUTFNullSafe(out, revision.getPage().getTitle());

    writeUTFNullSafe(out, revision.getId());
    writeUTFNullSafe(out, revision.getTimestamp());
    out.writeBoolean(revision.getMinorChange());
    writeUTFNullSafe(out, revision.getComment());

    out.writeInt(revision.getTextLength());

    writeUTFNullSafe(out, revision.getUser().getUserid());
    writeUTFNullSafe(out, revision.getUser().getUsername());
    writeUTFNullSafe(out, revision.getUser().getUserip());
  }

  @Override
  public void read(DataInput in) throws IOException {
    String pageId = readUTFNullSafe(in);
    String pageTitle = readUTFNullSafe(in);

    String revisionId = readUTFNullSafe(in);
    String revisionTimestamp = readUTFNullSafe(in);
    boolean revisionMinorChange = in.readBoolean();
    String revisionComment = readUTFNullSafe(in);

    Integer revisionTextLength = in.readInt();

    String userid = readUTFNullSafe(in);
    String username = readUTFNullSafe(in);
    String userip = readUTFNullSafe(in);
    WikiUser user = new WikiUser(username, userid, userip);

    revision = new WikiRevision(pageId, pageTitle, revisionId,
        revisionTimestamp, user, revisionMinorChange, revisionComment,
        revisionTextLength);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(obj, this);
  }

  @Override
  public String toString() {
    return revision != null ? revision.toString() : "";
  }

}
