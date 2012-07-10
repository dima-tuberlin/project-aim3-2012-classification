package de.tuberlin.dima.aim3.oc.input.jaxb;

import java.io.Reader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mediawiki.xml.export_0.RevisionType;

import de.tuberlin.dima.aim3.oc.input.IterativeWikiDumpParser;
import de.tuberlin.dima.aim3.oc.input.jaxb.conversion.NodeConverter;
import de.tuberlin.dima.aim3.oc.input.jaxb.conversion.RevisionConverterHandler;
import de.tuberlin.dima.aim3.oc.input.type.WikiRevision;
import eu.stratosphere.pact.common.io.FileInputFormat;

/**
 * Parser for Wikipedia XML dumps based on JAXB and StAX which iteratively reads
 * and parses an input file and returns Java instances for every successfully
 * parsed node.
 * 
 * This parser is optimized for stratosphere's {@link FileInputFormat} API and
 * therefore allows simple repetitive parsing of XML to Java instances. It holds
 * an internal pointer to the XML file and returns the next Java instance for
 * every call to {@link #readNextWikiDumpPage()}. It furthermore supports the
 * {@link FileInputFormat#reachedEnd()} method by checking its internal pointer.
 * 
 * @author Florian Feigenbutz <florian.feigenbutz@campus.tu-berlin.de>
 * 
 */
public class WikiDumpStaxJaxbParser implements IterativeWikiDumpParser {

  private static final Log LOG = LogFactory
      .getLog(WikiDumpStaxJaxbParser.class);

  @SuppressWarnings("rawtypes")
  private static final Class PARSED_CLASS = RevisionType.class;

  private final Reader xmlReader;

  private XMLEventReader staxReader;

  private XMLEventReader staxFiltRd;

  private JAXBContext jaxbContext;

  private Unmarshaller unmarshaller;

  private final NodeConverter nodeConverter;

  private boolean lookingForNewPageId;

  private boolean lookingForNewPageTitle;

  private String currentPageId;

  private String currentPageTitle;

  // ----------------------------------- //

  public WikiDumpStaxJaxbParser(Reader xmlReader) {
    this.xmlReader = xmlReader;
    nodeConverter = new RevisionConverterHandler();

    lookingForNewPageId = false;
    lookingForNewPageTitle = false;

    try {
      init();
    } catch (Throwable e) {
      throw new RuntimeException("Error while initializing Wiki Dump parser", e);
    }
  }

  protected void init() throws XMLStreamException, JAXBException {

    // StAX:
    EventFilter startElementFilter = new EventFilter() {

      @Override
      public boolean accept(XMLEvent event) {
        if (event.isStartElement()) {
          QName eventName = event.asStartElement().getName();
          if (!isSupportedNamespace(eventName)) {
            LOG.warn("Cannot handle event with unknown namespace URI '"
                + eventName.getNamespaceURI() + "'");
            return false;
          }

          if ("revision".equals(eventName.getLocalPart())) {
            LOG.debug("Found start of revision node");
            return true;
          } else if ("page".equals(eventName.getLocalPart())) {
            LOG.debug("Found start of page node");
            return false;
          } else if ("title".equals(eventName.getLocalPart())) {
            LOG.debug("Found title of page node");
            lookingForNewPageTitle = true;
            return false;
          } else if ("id".equals(eventName.getLocalPart())) {
            LOG.debug("Found id of page node");
            lookingForNewPageId = true;
            return false;
          }

          LOG.debug("Rejected XML node of type: " + eventName);
          return false;

        } else if (event.isCharacters()) {
          if (lookingForNewPageId) {
            currentPageId = event.asCharacters().toString();
            lookingForNewPageId = false;
            LOG.info("Found new page with ID '" + currentPageId + "'");
          }
          if (lookingForNewPageTitle) {
            currentPageTitle = event.asCharacters().toString();
            lookingForNewPageTitle = false;
            LOG.info("Found new page with title '" + currentPageTitle + "'");
          }
        }
        LOG.trace("Rejected unknown XML node type: "
            + (event.toString() != null ? event.toString() : "NULL"));
        return false;
      }

      private boolean isSupportedNamespace(QName eventName) {
        return eventName.getNamespaceURI().equals(
            "http://www.mediawiki.org/xml/export-0.7/");
      }
    };

    XMLInputFactory staxFactory = XMLInputFactory.newInstance();
    staxReader = staxFactory.createXMLEventReader(xmlReader);
    staxFiltRd = staxFactory.createFilteredReader(staxReader,
        startElementFilter);

    // JAXB:
    jaxbContext = JAXBContext.newInstance(PARSED_CLASS);
    unmarshaller = jaxbContext.createUnmarshaller();
  }

  // ----------- IterativeWikiDumpParser API ---------- //

  @Override
  public WikiRevision readNextWikiDumpRevision() {
    return readNextWikiDumpRevision(0);
  }

  private WikiRevision readNextWikiDumpRevision(int numberOfRetries) {
    try {
      if (staxFiltRd.peek() != null) {
        @SuppressWarnings("unchecked")
        Object node = unmarshaller.unmarshal(staxReader, PARSED_CLASS);
        if (node instanceof JAXBElement
            && ((JAXBElement<?>) node).getValue() != null) {
          node = ((JAXBElement<?>) node).getValue();
        }
        WikiRevision wikiRevision = (WikiRevision) nodeConverter.convert(node,
            currentPageId, currentPageTitle);
        // the first element might be null so give it a single retry
        if (wikiRevision == null && numberOfRetries < 1) {
          numberOfRetries++;
          wikiRevision = readNextWikiDumpRevision(numberOfRetries);
        }
        return wikiRevision;
      }
    } catch (XMLStreamException e) {
      // FIXME Auto-generated catch block
      e.printStackTrace();
    } catch (JAXBException e) {
      // FIXME Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public boolean reachedEnd() {
    return !staxFiltRd.hasNext();
  }

}
