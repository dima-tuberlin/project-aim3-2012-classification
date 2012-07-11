package de.tuberlin.dima.aim3.oc;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import de.tuberlin.dima.aim3.oc.WikipediaRevisionStatisticsCreator.PageRevisionCounter;

/**
 * Tests the class {@link PageRevisionCounter}.
 * 
 * @author Florian Feigenbutz <florian.feigenbutz@campus.tu-berlin.de>
 */
public class PageRevisionCounterTest {

  private PageRevisionCounter sut;

  @Before
  public void setup() {
    sut = new PageRevisionCounter();
  }

  @Test
  public void testCalculateAvgTextLength_firstRound() {
    int numberOfRevisions = 0;
    double averageTextLength = 0;
    int revisionTextLength = 1000;

    double result = sut.calculateAvgTextLength(numberOfRevisions,
        averageTextLength, revisionTextLength);

    Assert.assertEquals(1000d, result);
  }

  @Test
  public void testCalculateAvgTextLength_secondRound() {
    int numberOfRevisions = 1;
    double averageTextLength = 1000;
    int revisionTextLength = 500;

    double result = sut.calculateAvgTextLength(numberOfRevisions,
        averageTextLength, revisionTextLength);

    Assert.assertEquals(750d, result);
  }

  @Test
  public void testCalculateAvgTextLength_thirdRound() {
    int numberOfRevisions = 2;
    double averageTextLength = 750;
    int revisionTextLength = 750;

    double result = sut.calculateAvgTextLength(numberOfRevisions,
        averageTextLength, revisionTextLength);

    Assert.assertEquals(750d, result);
  }

  @Test
  public void testCalculateAvgTextLengthChange_firstRound() {
    int numberOfRevisions = 0;
    double averageTextLengthChange = 0;
    int revisionTextLengthChange = 1000;

    double result = sut.calculateAvgTextLengthChange(numberOfRevisions,
        averageTextLengthChange, revisionTextLengthChange);

    Assert.assertEquals(1000d, result);
  }

  @Test
  public void testCalculateAvgTextLengthChange_secondRound() {
    int numberOfRevisions = 1;
    double averageTextLengthChange = 1000;
    int revisionTextLengthChange = 500;

    double result = sut.calculateAvgTextLengthChange(numberOfRevisions,
        averageTextLengthChange, revisionTextLengthChange);

    Assert.assertEquals(750d, result);
  }

  @Test
  public void testCalculateAvgTextLengthChange_charsRemoved() {
    int numberOfRevisions = 1;
    double averageTextLengthChange = 1000;
    int revisionTextLengthChange = -500;

    double result = sut.calculateAvgTextLengthChange(numberOfRevisions,
        averageTextLengthChange, revisionTextLengthChange);

    Assert.assertEquals(250d, result);
  }

  @Test
  public void testUnsignedTextLengthChange_charsRemoved() {
    int lastTextLength = 1000;
    int revisionTextLength = 50;

    int result = sut.getUnsignedTextLengthChange(lastTextLength,
        revisionTextLength);

    Assert.assertEquals(950, result);
  }

  @Test
  public void testUnsignedTextLengthChange_charsAdded() {
    int lastTextLength = 500;
    int revisionTextLength = 600;

    int result = sut.getUnsignedTextLengthChange(lastTextLength,
        revisionTextLength);

    Assert.assertEquals(100, result);
  }
}
