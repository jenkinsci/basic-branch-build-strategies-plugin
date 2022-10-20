/*
 * The MIT License
 *
 * Copyright (c) 2018, CloudBees, Inc.
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

import hudson.model.Computer;
import hudson.model.Executor;
import hudson.model.TopLevelItem;
import java.util.Arrays;
import java.util.Collections;
import jenkins.branch.BranchBuildStrategy;
import jenkins.branch.BranchSource;
import jenkins.branch.buildstrategies.basic.harness.BasicMultiBranchProject;
import jenkins.scm.impl.mock.MockSCMController;
import jenkins.scm.impl.mock.MockSCMDiscoverBranches;
import jenkins.scm.impl.mock.MockSCMDiscoverChangeRequests;
import jenkins.scm.impl.mock.MockSCMDiscoverTags;
import jenkins.scm.impl.mock.MockSCMSource;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.MatcherAssert.assertThat;

@Ignore
public class FormBindingTest {
    /**
     * All tests in this class only create items and do not affect other global configuration, thus we trade test
     * execution time for the restriction on only touching items.
     */
    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    @Before
    public void cleanOutAllItems() throws Exception {
        for (TopLevelItem i : j.getInstance().getItems()) {
            i.delete();
        }
        for (Computer comp : j.jenkins.getComputers()) {
            for (Executor e : comp.getExecutors()) {
                if (e.getCauseOfDeath() != null) {
                    e.doYank();
                }
            }
            for (Executor e : comp.getOneOffExecutors()) {
                if (e.getCauseOfDeath() != null) {
                    e.doYank();
                }
            }
        }
    }

    @Test
    public void branch() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("foo");
            BasicMultiBranchProject prj = j.jenkins.createProject(BasicMultiBranchProject.class, "foo");
            prj.setCriteria(null);
            BranchSource source = new BranchSource(new MockSCMSource(c, "foo", new MockSCMDiscoverBranches(),
                    new MockSCMDiscoverTags(), new MockSCMDiscoverChangeRequests()));
            source.setBuildStrategies(Collections.<BranchBuildStrategy>singletonList(new BranchBuildStrategyImpl()));
            prj.getSourcesList().add(source);
            j.configRoundtrip(prj);
            assertThat(prj.getSources().get(0).getBuildStrategies(),
                    contains((BranchBuildStrategy) new BranchBuildStrategyImpl()));
        }
    }

    @Test
    public void namedBranch() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("foo");
            BasicMultiBranchProject prj = j.jenkins.createProject(BasicMultiBranchProject.class, "foo");
            prj.setCriteria(null);
            BranchSource source = new BranchSource(new MockSCMSource(c, "foo", new MockSCMDiscoverBranches(),
                    new MockSCMDiscoverTags(), new MockSCMDiscoverChangeRequests()));
            source.setBuildStrategies(Collections.<BranchBuildStrategy>singletonList(new NamedBranchBuildStrategyImpl(
                    Arrays.asList(new NamedBranchBuildStrategyImpl.ExactNameFilter("master", false),
                            new NamedBranchBuildStrategyImpl.ExactNameFilter("production", false),
                            new NamedBranchBuildStrategyImpl.RegexNameFilter("^staging-.*$", false),
                            new NamedBranchBuildStrategyImpl.WildcardsNameFilter("feature/*", "feature", false)
                    ))));
            prj.getSourcesList().add(source);
            j.configRoundtrip(prj);
            assertThat(prj.getSources().get(0).getBuildStrategies(),
                    contains((BranchBuildStrategy) new NamedBranchBuildStrategyImpl(
                            Arrays.asList(new NamedBranchBuildStrategyImpl.ExactNameFilter("master", false),
                                    new NamedBranchBuildStrategyImpl.ExactNameFilter("production", false),
                                    new NamedBranchBuildStrategyImpl.RegexNameFilter("^staging-.*$", false),
                                    new NamedBranchBuildStrategyImpl.WildcardsNameFilter("feature/*", "feature", false)
                            ))));
        }
    }

    @Test
    public void changeRequest1() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("foo");
            BasicMultiBranchProject prj = j.jenkins.createProject(BasicMultiBranchProject.class, "foo");
            prj.setCriteria(null);
            BranchSource source = new BranchSource(new MockSCMSource(c, "foo", new MockSCMDiscoverBranches(),
                    new MockSCMDiscoverTags(), new MockSCMDiscoverChangeRequests()));
            source.setBuildStrategies(
                    Collections.<BranchBuildStrategy>singletonList(new ChangeRequestBuildStrategyImpl(false, false)));
            prj.getSourcesList().add(source);
            j.configRoundtrip(prj);
            assertThat(prj.getSources().get(0).getBuildStrategies(),
                    contains((BranchBuildStrategy) new ChangeRequestBuildStrategyImpl(false, false)));
        }
    }

    @Test
    public void changeRequest2() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("foo");
            BasicMultiBranchProject prj = j.jenkins.createProject(BasicMultiBranchProject.class, "foo");
            prj.setCriteria(null);
            BranchSource source = new BranchSource(new MockSCMSource(c, "foo", new MockSCMDiscoverBranches(),
                    new MockSCMDiscoverTags(), new MockSCMDiscoverChangeRequests()));
            source.setBuildStrategies(
                    Collections.<BranchBuildStrategy>singletonList(new ChangeRequestBuildStrategyImpl(true, false)));
            prj.getSourcesList().add(source);
            j.configRoundtrip(prj);
            assertThat(prj.getSources().get(0).getBuildStrategies(),
                    contains((BranchBuildStrategy) new ChangeRequestBuildStrategyImpl(true, false)));
        }
    }

    @Test
    public void tag____() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("foo");
            BasicMultiBranchProject prj = j.jenkins.createProject(BasicMultiBranchProject.class, "foo");
            prj.setCriteria(null);
            BranchSource source = new BranchSource(new MockSCMSource(c, "foo", new MockSCMDiscoverBranches(),
                    new MockSCMDiscoverTags(), new MockSCMDiscoverChangeRequests()));
            source.setBuildStrategies(
                    Collections.<BranchBuildStrategy>singletonList(new TagBuildStrategyImpl("", "")));
            prj.getSourcesList().add(source);
            j.configRoundtrip(prj);
            assertThat(prj.getSources().get(0).getBuildStrategies(),
                    contains((BranchBuildStrategy) new TagBuildStrategyImpl("", "")));
        }
    }

    @Test
    public void tag_0__() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("foo");
            BasicMultiBranchProject prj = j.jenkins.createProject(BasicMultiBranchProject.class, "foo");
            prj.setCriteria(null);
            BranchSource source = new BranchSource(new MockSCMSource(c, "foo", new MockSCMDiscoverBranches(),
                    new MockSCMDiscoverTags(), new MockSCMDiscoverChangeRequests()));
            source.setBuildStrategies(
                    Collections.<BranchBuildStrategy>singletonList(new TagBuildStrategyImpl("0", "")));
            prj.getSourcesList().add(source);
            j.configRoundtrip(prj);
            assertThat(prj.getSources().get(0).getBuildStrategies(),
                    contains((BranchBuildStrategy) new TagBuildStrategyImpl("0", "")));
        }
    }

    @Test
    public void tagp1__() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("foo");
            BasicMultiBranchProject prj = j.jenkins.createProject(BasicMultiBranchProject.class, "foo");
            prj.setCriteria(null);
            BranchSource source = new BranchSource(new MockSCMSource(c, "foo", new MockSCMDiscoverBranches(),
                    new MockSCMDiscoverTags(), new MockSCMDiscoverChangeRequests()));
            source.setBuildStrategies(
                    Collections.<BranchBuildStrategy>singletonList(new TagBuildStrategyImpl("1", "")));
            prj.getSourcesList().add(source);
            j.configRoundtrip(prj);
            assertThat(prj.getSources().get(0).getBuildStrategies(),
                    contains((BranchBuildStrategy) new TagBuildStrategyImpl("1", "")));
        }
    }

    @Test
    public void tagn1__() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("foo");
            BasicMultiBranchProject prj = j.jenkins.createProject(BasicMultiBranchProject.class, "foo");
            prj.setCriteria(null);
            BranchSource source = new BranchSource(new MockSCMSource(c, "foo", new MockSCMDiscoverBranches(),
                    new MockSCMDiscoverTags(), new MockSCMDiscoverChangeRequests()));
            source.setBuildStrategies(
                    Collections.<BranchBuildStrategy>singletonList(new TagBuildStrategyImpl("-1", "")));
            prj.getSourcesList().add(source);
            j.configRoundtrip(prj);
            assertThat(prj.getSources().get(0).getBuildStrategies(),
                    contains((BranchBuildStrategy) new TagBuildStrategyImpl("", "")));
        }
    }

    @Test
    public void tag___0() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("foo");
            BasicMultiBranchProject prj = j.jenkins.createProject(BasicMultiBranchProject.class, "foo");
            prj.setCriteria(null);
            BranchSource source = new BranchSource(new MockSCMSource(c, "foo", new MockSCMDiscoverBranches(),
                    new MockSCMDiscoverTags(), new MockSCMDiscoverChangeRequests()));
            source.setBuildStrategies(
                    Collections.<BranchBuildStrategy>singletonList(new TagBuildStrategyImpl("", "0")));
            prj.getSourcesList().add(source);
            j.configRoundtrip(prj);
            assertThat(prj.getSources().get(0).getBuildStrategies(),
                    contains((BranchBuildStrategy) new TagBuildStrategyImpl("", "0")));
        }
    }

    @Test
    public void tag__p1() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("foo");
            BasicMultiBranchProject prj = j.jenkins.createProject(BasicMultiBranchProject.class, "foo");
            prj.setCriteria(null);
            BranchSource source = new BranchSource(new MockSCMSource(c, "foo", new MockSCMDiscoverBranches(),
                    new MockSCMDiscoverTags(), new MockSCMDiscoverChangeRequests()));
            source.setBuildStrategies(
                    Collections.<BranchBuildStrategy>singletonList(new TagBuildStrategyImpl("", "1")));
            prj.getSourcesList().add(source);
            j.configRoundtrip(prj);
            assertThat(prj.getSources().get(0).getBuildStrategies(),
                    contains((BranchBuildStrategy) new TagBuildStrategyImpl("", "1")));
        }
    }

    @Test
    public void tag__n1() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("foo");
            BasicMultiBranchProject prj = j.jenkins.createProject(BasicMultiBranchProject.class, "foo");
            prj.setCriteria(null);
            BranchSource source = new BranchSource(new MockSCMSource(c, "foo", new MockSCMDiscoverBranches(),
                    new MockSCMDiscoverTags(), new MockSCMDiscoverChangeRequests()));
            source.setBuildStrategies(
                    Collections.<BranchBuildStrategy>singletonList(new TagBuildStrategyImpl("", "-1")));
            prj.getSourcesList().add(source);
            j.configRoundtrip(prj);
            assertThat(prj.getSources().get(0).getBuildStrategies(),
                    contains((BranchBuildStrategy) new TagBuildStrategyImpl("", "")));
        }
    }
}
