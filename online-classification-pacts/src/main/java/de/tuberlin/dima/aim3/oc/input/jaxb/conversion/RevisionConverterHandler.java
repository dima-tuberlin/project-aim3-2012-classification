package de.tuberlin.dima.aim3.oc.input.jaxb.conversion;

import java.math.BigInteger;

import org.mediawiki.xml.export_0.ContributorType;
import org.mediawiki.xml.export_0.RevisionType;
import org.mediawiki.xml.export_0.TextType;

import de.tuberlin.dima.aim3.oc.input.type.WikiRevision;
import de.tuberlin.dima.aim3.oc.input.type.WikiUser;

public class RevisionConverterHandler extends BaseConverterHandler {

  @Override
  public Object convert(Object node, String... args) {
    if (!(node instanceof RevisionType)) {
      throw new IllegalArgumentException(
          "Node to be converted must be of type "
              + RevisionType.class.getName() + " - was "
              + node.getClass().getName());
    }

    if (args == null || args.length != 2) {
      throw new RuntimeException(
          "Missing arguments for id and name of current page. This is likely to be a bug in the calling parser implementation.");
    }
    String pageId = args[0];
    String pageTitle = args[1];

    RevisionType inputRevision = (RevisionType) node;

    // skip empty results which happen e.g. during errornous xml input
    if (inputRevision.getId() == null) {
      return null;
    }

    ContributorType inputUser = inputRevision.getContributor();
    String userId = getStringValue(inputUser.getId());
    WikiUser outputUser = new WikiUser(inputUser.getUsername(), userId,
        inputUser.getIp());

    boolean isMinorChange = inputRevision.getMinor() != null;
    String formattedTimestamp = dateFormatter.format(inputRevision
        .getTimestamp().toGregorianCalendar().getTime());
    String comment = getStringValue(inputRevision.getComment());
    Integer textLength = parseTextLength(inputRevision);

    WikiRevision outputRevision = new WikiRevision(pageId, pageTitle,
        inputRevision.getId().toString(), formattedTimestamp, outputUser,
        isMinorChange, comment, textLength);
    return outputRevision;
  }

  private Integer parseTextLength(RevisionType inputRevision) {
    TextType revisionText = inputRevision.getText();
    // if there is any text measure its length
    String revisionString = getStringValue(revisionText);
    if (revisionString != null && !revisionString.equals("")) {
      return revisionString.length();
    } else {
      // use "bytes" attribute
      BigInteger textBytes = revisionText.getBytes();
      return textBytes == null ? 0 : textBytes.intValue();
    }
  }
}