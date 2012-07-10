package de.tuberlin.dima.aim3.oc.input.jaxb.conversion;

import java.util.ArrayList;
import java.util.List;

import org.mediawiki.xml.export_0.PageType;
import org.mediawiki.xml.export_0.RevisionType;

import de.tuberlin.dima.aim3.oc.input.type.WikiPage;
import de.tuberlin.dima.aim3.oc.input.type.WikiRevision;

public class PageConverterHandler extends BaseConverterHandler {

  private final RevisionConverterHandler revisionConverterHandler;

  public PageConverterHandler() {
    super();
    revisionConverterHandler = new RevisionConverterHandler();
  }

  @Override
  public Object convert(Object node, String... args) {
    if (!(node instanceof PageType)) {
      throw new IllegalArgumentException(
          "Node to be converted must be of type " + PageType.class.getName()
              + " - was " + node.getClass().getName());
    }

    PageType inputPage = (PageType) node;

    // skip empty results which happen e.g. during errornous xml input
    if (inputPage.getId() == null || inputPage.getTitle() == null) {
      return null;
    }

    List<WikiRevision> outputRevisions = new ArrayList<WikiRevision>(inputPage
        .getRevisionOrUpload().size());
    for (Object revisionOrUploadOrLogitem : inputPage.getRevisionOrUpload()) {
      if (revisionOrUploadOrLogitem instanceof RevisionType) {
        WikiRevision outputRevision = (WikiRevision) revisionConverterHandler
            .convert(revisionOrUploadOrLogitem);
        outputRevisions.add(outputRevision);
      }
    }

    WikiPage outputPage = new WikiPage(inputPage.getTitle(), inputPage.getId()
        .toString());
    return outputPage;
  }
}
