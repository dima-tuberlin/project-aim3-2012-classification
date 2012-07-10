package de.tuberlin.dima.aim3.oc.input.jaxb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;

import de.tuberlin.dima.aim3.oc.input.type.WikiRevision;
import de.tuberlin.dima.aim3.oc.util.Constants;

/**
 * Tests the class {@link WikiDumpStaxJaxbParser}.
 * 
 * @author Florian Feigenbutz <florian.feigenbutz@campus.tu-berlin.de>
 * 
 */
public class WikiDumpStaxJaxbParserTest {

  private WikiDumpStaxJaxbParser sut;

  @Test
  public void testReadNextWikiDumpRevision_multiplePagesAndRevisions()
      throws Exception {
    createSut("testDump_multiplePages.xml");

    // Read revision no 1
    WikiRevision revision = sut.readNextWikiDumpRevision();

    Assert.assertNotNull(revision);
    Assert.assertEquals("9942", revision.getPage().getId());
    Assert.assertEquals("Erwin Schrödinger", revision.getPage().getTitle());
    Assert.assertEquals("107333", revision.getId());
    Assert.assertEquals("2002-02-25T16:51:15+0100", revision.getTimestamp());
    Assert.assertEquals("Automated conversion", revision.getComment());
    Assert
        .assertEquals(
            "[[Austria|Austrian]] [[physicist]] ([[August 12]] [[1887]] - [[January 4]] [[1961]]) famous for his contributions to [[quantum mechanics]], especially the [[Schrodinger wave equation|Schr&ouml;dinger wave equation]]. \n\nHe is the owner of [[Schrodingers cat|Schrödinger's cat]].\n\n----\n'''External links:'''\n* O'Connor, Robertson, \"<nowiki>MacTutor</nowiki> biography of Erwin Schr&ouml;dinger\", http://www-groups.dcs.st-andrews.ac.uk/~history/Mathematicians/Schrodinger.html"
                .length(), revision.getTextLength().intValue());

    Assert.assertEquals("0", revision.getUser().getUserid());
    Assert.assertEquals("Conversion script", revision.getUser().getUsername());
    Assert.assertNull(revision.getUser().getUserip());

    // Read revision no 15
    for (int i = 0; i < 14; i++) {
      revision = sut.readNextWikiDumpRevision();
    }

    Assert.assertNotNull(revision);
    Assert.assertEquals("9942", revision.getPage().getId());
    Assert.assertEquals("Erwin Schrödinger", revision.getPage().getTitle());
    Assert.assertEquals("361695", revision.getId());
    Assert.assertEquals("2002-10-16T05:05:39+0200", revision.getTimestamp());
    Assert.assertEquals("lots of minor changes - still needs work",
        revision.getComment());
    Assert.assertEquals("4635", revision.getUser().getUserid());
    Assert.assertEquals("Tim Starling", revision.getUser().getUsername());
    Assert.assertNull(revision.getUser().getUserip());

    // Read revision no 16
    revision = sut.readNextWikiDumpRevision();

    Assert.assertNotNull(revision);
    Assert.assertEquals("9944", revision.getPage().getId());
    Assert.assertEquals("Episome", revision.getPage().getTitle());
    Assert.assertEquals("15907794", revision.getId());
    Assert.assertEquals("2002-02-25T16:43:11+0100", revision.getTimestamp());
    Assert.assertEquals("Automated conversion", revision.getComment());
    Assert.assertEquals("0", revision.getUser().getUserid());
    Assert.assertEquals("Conversion script", revision.getUser().getUsername());
    Assert.assertNull(revision.getUser().getUserip());

    // Read revision no 33
    for (int i = 0; i < 17; i++) {
      revision = sut.readNextWikiDumpRevision();
    }

    Assert.assertNotNull(revision);
    Assert.assertEquals("9945", revision.getPage().getId());
    Assert.assertEquals("EasyWriter", revision.getPage().getTitle());
    Assert.assertEquals("51730982", revision.getId());
    Assert.assertEquals("2006-05-05T22:17:46+0200", revision.getTimestamp());
    Assert.assertNull(revision.getComment());
    Assert.assertEquals("1174403", revision.getUser().getUserid());
    Assert.assertEquals("Talamus", revision.getUser().getUsername());
    Assert.assertNull(revision.getUser().getUserip());

    Assert.assertTrue(sut.reachedEnd());
  }

  /**
   * Tests the parser with stub-meta-history content. These files only contain
   * meta data for text such as the number of bytes and the revision author.
   * 
   * @throws Exception
   */
  @Test
  public void testReadNextWikiDumpRevision_stubMetaHistory() throws Exception {
    createSut("testDump_stub-meta-history.xml");

    // Read revision no 1
    WikiRevision revision = sut.readNextWikiDumpRevision();

    Assert.assertNotNull(revision);
    Assert.assertEquals("10", revision.getPage().getId());
    Assert.assertEquals("AccessibleComputing", revision.getPage().getTitle());
    Assert.assertEquals("233192", revision.getId());
    Assert.assertEquals("2001-01-21T03:12:21+0100", revision.getTimestamp());
    Assert.assertEquals("*", revision.getComment());
    Assert.assertEquals(124, revision.getTextLength().intValue());

    Assert.assertEquals("99", revision.getUser().getUserid());
    Assert.assertEquals("RoseParks", revision.getUser().getUsername());
    Assert.assertNull(revision.getUser().getUserip());

    // Read revision no 2
    revision = sut.readNextWikiDumpRevision();

    Assert.assertNotNull(revision);
    Assert.assertEquals("10", revision.getPage().getId());
    Assert.assertEquals("AccessibleComputing", revision.getPage().getTitle());
    Assert.assertEquals("862220", revision.getId());
    Assert.assertEquals("2002-02-25T16:43:11+0100", revision.getTimestamp());
    Assert.assertEquals("Automated conversion", revision.getComment());
    Assert.assertEquals(35, revision.getTextLength().intValue());

    Assert.assertEquals("0", revision.getUser().getUserid());
    Assert.assertEquals("Conversion script", revision.getUser().getUsername());
    Assert.assertNull(revision.getUser().getUserip());

    // Read revision no 9
    for (int i = 0; i < 7; i++) {
      revision = sut.readNextWikiDumpRevision();
    }

    Assert.assertNotNull(revision);
    Assert.assertEquals("10", revision.getPage().getId());
    Assert.assertEquals("AccessibleComputing", revision.getPage().getTitle());
    Assert.assertEquals("381202555", revision.getId());
    Assert.assertEquals("2010-08-27T00:38:36+0200", revision.getTimestamp());
    Assert
        .assertEquals(
            "[[Help:Reverting|Reverted]] edits by [[Special:Contributions/76.28.186.133|76.28.186.133]] ([[User talk:76.28.186.133|talk]]) to last version by Gurch",
            revision.getComment());
    Assert.assertEquals(57, revision.getTextLength().intValue());

    Assert.assertEquals("7181920", revision.getUser().getUserid());
    Assert.assertEquals("OlEnglish", revision.getUser().getUsername());
    Assert.assertNull(revision.getUser().getUserip());

    Assert.assertTrue(sut.reachedEnd());
  }

  private void createSut(String testFileName) throws FileNotFoundException {
    URL testFile = getClass().getClassLoader().getResource(testFileName);
    Reader xmlReader = createReader(testFile);
    sut = new WikiDumpStaxJaxbParser(xmlReader);
  }

  private Reader createReader(URL testFile) throws FileNotFoundException {
    Reader xmlReader = new InputStreamReader(new FileInputStream(new File(
        testFile.getFile())), Constants.CHARSET);
    return xmlReader;
  }
}
