package no.entur.uttu.model;

import static no.entur.uttu.model.ModelTestUtil.assertCheckPersistableFails;

import org.junit.Test;

public class NoticeTest {

  @Test
  public void checkPersistable_notice_success() {
    new Notice().withText("This is a good notice.").checkPersistable();
  }

  @Test
  public void checkPersistable_noticeWithoutText_givesException() {
    assertCheckPersistableFails(new Notice());
    assertCheckPersistableFails(new Notice().withText(""));
  }
}
