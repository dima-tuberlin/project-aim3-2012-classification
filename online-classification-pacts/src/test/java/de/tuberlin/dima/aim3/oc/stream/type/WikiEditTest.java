package de.tuberlin.dima.aim3.oc.stream.type;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.tuberlin.dima.aim3.oc.stream.type.WikiEdit.RevisionChange;

public class WikiEditTest {

  private WikiEdit sut;

  @Before
  public void setUp() {
    sut = new WikiEdit();
  }

  @Test
  public void testAnalyzeChanges_differentUsers_similarCharsNo() {
    String page = "Test Page";
    String wikipedia = "en#wikipedia";
    String flag = "M";
    Integer myDelta = 100;
    Integer otherDelta = -99;
    String myUser = "TestUser1";
    String otherUser = "TestUser2";
    String myRevision = "1";
    String otherRevision = "200";
    sut = new WikiEdit(page, wikipedia, flag, myDelta, myUser, myRevision);

    WikiEdit other = new WikiEdit(page, wikipedia, flag, otherDelta, otherUser,
        otherRevision);
    Assert.assertSame(RevisionChange.DIFFERENTUSER_CONTENTREVERT, sut.analyzeChanges(other));
  }

  @Test
  public void testAnalyzeChanges_differentUsers_bothAdditions() {
    String page = "Test Page";
    String wikipedia = "en#wikipedia";
    String flag = "M";
    Integer myDelta = 100;
    Integer otherDelta = 20;
    String myUser = "TestUser1";
    String otherUser = "TestUser2";
    String myRevision = "1";
    String otherRevision = "200";
    sut = new WikiEdit(page, wikipedia, flag, myDelta, myUser, myRevision);

    WikiEdit other = new WikiEdit(page, wikipedia, flag, otherDelta, otherUser,
        otherRevision);
    Assert.assertSame(RevisionChange.DIFFERENTUSER_CONTENTCHANGE,
        sut.analyzeChanges(other));
  }

  @Test
  public void testAnalyzeChanges_differentUsers_varyingCharsNo() {
    String page = "Test Page";
    String wikipedia = "en#wikipedia";
    String flag = "M";
    Integer myDelta = 100;
    Integer otherDelta = -20;
    String myUser = "TestUser1";
    String otherUser = "TestUser2";
    String myRevision = "1";
    String otherRevision = "200";
    sut = new WikiEdit(page, wikipedia, flag, myDelta, myUser, myRevision);

    WikiEdit other = new WikiEdit(page, wikipedia, flag, otherDelta, otherUser,
        otherRevision);
    Assert.assertSame(RevisionChange.DIFFERENTUSER_CONTENTCHANGE,
        sut.analyzeChanges(other));
  }

  @Test
  public void testAnalyzeChanges_sameUser_similarCharsNo() {
    String page = "Test Page";
    String wikipedia = "en#wikipedia";
    String flag = "M";
    Integer myDelta = 100;
    Integer otherDelta = -99;
    String myUser = "TestUser1";
    String otherUser = myUser;
    String myRevision = "1";
    String otherRevision = "200";
    sut = new WikiEdit(page, wikipedia, flag, myDelta, myUser, myRevision);

    WikiEdit other = new WikiEdit(page, wikipedia, flag, otherDelta, otherUser,
        otherRevision);
    Assert.assertSame(RevisionChange.SAMEUSER_CONTENTREVERT,
        sut.analyzeChanges(other));
  }

  @Test
  public void testAnalyzeChanges_sameUser_varyingCharsNo() {
    String page = "Test Page";
    String wikipedia = "en#wikipedia";
    String flag = "M";
    Integer myDelta = 100;
    Integer otherDelta = 81;
    String myUser = "TestUser1";
    String otherUser = myUser;
    String myRevision = "1";
    String otherRevision = "200";
    sut = new WikiEdit(page, wikipedia, flag, myDelta, myUser, myRevision);

    WikiEdit other = new WikiEdit(page, wikipedia, flag, otherDelta, otherUser,
        otherRevision);
    Assert.assertSame(RevisionChange.SAMEUSER_CONTENTCHANGE,
        sut.analyzeChanges(other));
  }
}
