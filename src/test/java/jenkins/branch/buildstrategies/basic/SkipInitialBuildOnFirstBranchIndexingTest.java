package jenkins.branch.buildstrategies.basic;

import jenkins.scm.impl.mock.MockSCMController;
import jenkins.scm.impl.mock.MockSCMHead;
import jenkins.scm.impl.mock.MockSCMRevision;
import jenkins.scm.impl.mock.MockSCMSource;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;


public class SkipInitialBuildOnFirstBranchIndexingTest {
    @Test
    public void given__no__scm__prev_revision__isAutomaticBuild__then__returns_false() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockSCMHead("master");
            assertThat(
                    new SkipInitialBuildOnFirstBranchIndexing().isAutomaticBuild(
                            new MockSCMSource(c, "dummy"),
                            head,
                            new MockSCMRevision(head, "dummy"),
                            null
                    ),
                    is(false)
            );
        }
    }

    @Test
    public void given__scm__prev_revision__isAutomaticBuild__then__returns_true() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockSCMHead("master");
            assertThat(
                    new SkipInitialBuildOnFirstBranchIndexing().isAutomaticBuild(
                            new MockSCMSource(c, "dummy"),
                            head,
                            new MockSCMRevision(head, "dummy"),
                            new MockSCMRevision(head, "dummy")
                    ),
                    is(true)
            );
        }
    }

}