/*
 * The MIT License
 *
 * Copyright (c) 2019, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.branch.buildstrategies.basic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import hudson.model.FreeStyleProject;
import java.util.Collections;
import jenkins.branch.BranchBuildStrategy;
import jenkins.branch.BranchSource;
import jenkins.branch.buildstrategies.basic.harness.BasicMultiBranchProject;
import jenkins.scm.api.SCMEvent;
import jenkins.scm.api.SCMEvents;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.impl.mock.MockSCMController;
import jenkins.scm.impl.mock.MockSCMDiscoverBranches;
import jenkins.scm.impl.mock.MockSCMHead;
import jenkins.scm.impl.mock.MockSCMHeadEvent;
import jenkins.scm.impl.mock.MockSCMRevision;
import jenkins.scm.impl.mock.MockSCMSource;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class SkipInitialBuildOnFirstBranchIndexingTest {

    /**
     * All tests in this class only create items and do not affect other global configuration, thus we trade test
     * execution time for the restriction on only touching items.
     */
    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    @Test
    public void given__no__scm__lastSeenRevision__isAutomaticBuild__then__returns_false() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockSCMHead("master");
            assertThat(
                    new SkipInitialBuildOnFirstBranchIndexing()
                            .isAutomaticBuild(
                                    new MockSCMSource(c, "dummy"),
                                    head,
                                    new MockSCMRevision(head, "dummy"),
                                    null,
                                    null,
                                    null),
                    is(false));
        }
    }

    @Test
    public void given__scm__lastSeenRevision__but__not__equal__to__currRevision__isAutomaticBuild__then__returns_true()
            throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockSCMHead("master");
            assertThat(
                    new SkipInitialBuildOnFirstBranchIndexing()
                            .isAutomaticBuild(
                                    new MockSCMSource(c, "dummy"),
                                    head,
                                    new MockSCMRevision(head, "dummy"),
                                    null,
                                    new MockSCMRevision(head, "bar"),
                                    null),
                    is(true));
        }
    }

    @Test
    public void given__scm__lastSeenRevision__equal__to__currRevision__isAutomaticBuild__then__returns_false()
            throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockSCMHead("master");
            MockSCMRevision revision = new MockSCMRevision(head, "dummy");
            assertThat(
                    new SkipInitialBuildOnFirstBranchIndexing()
                            .isAutomaticBuild(new MockSCMSource(c, "dummy"), head, revision, null, revision, null),
                    is(false));
        }
    }

    @Test
    public void if__first__branch__indexing__isAutomaticBuild__then__returns__true() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("foo");
            BasicMultiBranchProject prj = j.jenkins.createProject(BasicMultiBranchProject.class, "project");
            prj.setCriteria(null);
            BranchSource source = new BranchSource(new MockSCMSource(c, "foo", new MockSCMDiscoverBranches()));
            source.setBuildStrategies(
                    Collections.<BranchBuildStrategy>singletonList(new SkipInitialBuildOnFirstBranchIndexing()));
            prj.getSourcesList().add(source);
            fire(new MockSCMHeadEvent(SCMEvent.Type.CREATED, c, "foo", "master", c.getRevision("foo", "master")));
            FreeStyleProject master = prj.getItem("master");
            j.waitUntilNoActivity();
            assertThat("The master branch was built", master.getLastBuild(), nullValue());
            c.addFile("foo", "master", "adding file", "file", new byte[0]);
            fire(new MockSCMHeadEvent(SCMEvent.Type.UPDATED, c, "foo", "master", c.getRevision("foo", "master")));
            j.waitUntilNoActivity();
            assertThat("The master branch was built", master.getLastBuild(), notNullValue());
            assertThat("The master branch was built", master.getLastBuild().getNumber(), is(1));
            c.addFile("foo", "master", "adding file", "file", new byte[0]);
            fire(new MockSCMHeadEvent(SCMEvent.Type.UPDATED, c, "foo", "master", c.getRevision("foo", "master")));
            j.waitUntilNoActivity();
            assertThat("The master branch was built", master.getLastBuild(), notNullValue());
            assertThat("The master branch was built", master.getLastBuild().getNumber(), is(2));
        }
    }

    @Test
    public void if__skipInitialBuildOnFirstBranchIndexing__is__disabled__first__branch__indexing__triggers__the__build()
            throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("foo");
            BasicMultiBranchProject prj = j.jenkins.createProject(BasicMultiBranchProject.class, "my-project");
            prj.setCriteria(null);
            prj.getSourcesList().add(new BranchSource(new MockSCMSource(c, "foo", new MockSCMDiscoverBranches())));
            fire(new MockSCMHeadEvent(SCMEvent.Type.CREATED, c, "foo", "master", c.getRevision("foo", "master")));
            FreeStyleProject master = prj.getItem("master");
            j.waitUntilNoActivity();
            assertThat("The master branch was built", master.getLastBuild().getNumber(), is(1));
            c.addFile("foo", "master", "adding file", "file", new byte[0]);
            fire(new MockSCMHeadEvent(SCMEvent.Type.UPDATED, c, "foo", "master", c.getRevision("foo", "master")));
            j.waitUntilNoActivity();
            assertThat("The master branch was built", master.getLastBuild(), notNullValue());
            assertThat("The master branch was built", master.getLastBuild().getNumber(), is(2));
        }
    }

    private void fire(MockSCMHeadEvent event) throws Exception {
        long watermark = SCMEvents.getWatermark();
        SCMHeadEvent.fireNow(event);
        SCMEvents.awaitAll(watermark);
        j.waitUntilNoActivity();
    }
}
