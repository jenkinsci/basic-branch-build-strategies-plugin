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

import java.util.Arrays;
import java.util.Collections;
import jenkins.scm.api.SCMHeadOrigin;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import jenkins.scm.impl.mock.MockChangeRequestSCMHead;
import jenkins.scm.impl.mock.MockChangeRequestSCMRevision;
import jenkins.scm.impl.mock.MockSCMController;
import jenkins.scm.impl.mock.MockSCMHead;
import jenkins.scm.impl.mock.MockSCMRevision;
import jenkins.scm.impl.mock.MockSCMSource;
import jenkins.scm.impl.mock.MockTagSCMHead;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class NamedBranchBuildStrategyImplTest {
    @Test
    public void given__regular_head__when__isAutomaticBuild__then__returns_true() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockSCMHead("master");
            assertThat(
                    new NamedBranchBuildStrategyImpl(Collections.<NamedBranchBuildStrategyImpl.NameFilter>singletonList(
                            new NamedBranchBuildStrategyImpl.ExactNameFilter("master", false))
                    ).isAutomaticBuild(
                            new MockSCMSource(c, "dummy"),
                            head,
                            new MockSCMRevision(head, "dummy"),
                            null,
                            null
                    ),
                    is(true)
            );
        }
    }

    @Test
    public void given__tag_head__when__isAutomaticBuild__then__returns_false() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockTagSCMHead("master", System.currentTimeMillis());
            assertThat(
                    new NamedBranchBuildStrategyImpl(Collections.<NamedBranchBuildStrategyImpl.NameFilter>singletonList(
                            new NamedBranchBuildStrategyImpl.ExactNameFilter("master", false))
                    ).isAutomaticBuild(
                            new MockSCMSource(c, "dummy"),
                            head,
                            new MockSCMRevision(head, "dummy"),
                            null,
                            null
                    ),
                    is(false)
            );
        }
    }

    @Test
    public void given__cr_head__when__isAutomaticBuild__then__returns_false() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockChangeRequestSCMHead head = new MockChangeRequestSCMHead(SCMHeadOrigin.DEFAULT, 1, "master",
                    ChangeRequestCheckoutStrategy.MERGE, true);
            assertThat(
                    new NamedBranchBuildStrategyImpl(Collections.<NamedBranchBuildStrategyImpl.NameFilter>singletonList(
                            new NamedBranchBuildStrategyImpl.RegexNameFilter("^.*$", false))
                    ).isAutomaticBuild(
                            new MockSCMSource(c, "dummy"),
                            head,
                            new MockChangeRequestSCMRevision(head,
                                    new MockSCMRevision(new MockSCMHead("master"), "dummy"), "dummy"),
                            null,
                            null
                    ),
                    is(false)
            );
        }
    }

    @Test
    public void given__regular_head__when__non_match__then__isAutomaticBuild_returns_false() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockSCMHead("master");
            assertThat(
                    new NamedBranchBuildStrategyImpl(Collections.<NamedBranchBuildStrategyImpl.NameFilter>singletonList(
                            new NamedBranchBuildStrategyImpl.ExactNameFilter("feature/1", false))
                    ).isAutomaticBuild(
                            new MockSCMSource(c, "dummy"),
                            head,
                            new MockSCMRevision(head, "dummy"),
                            null,
                            null
                    ),
                    is(false)
            );
        }
    }

    @Test
    public void given__regular_head__when__non_match_any__then__isAutomaticBuild_returns_false() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockSCMHead("feature");
            assertThat(
                    new NamedBranchBuildStrategyImpl(Arrays.<NamedBranchBuildStrategyImpl.NameFilter>asList(
                            new NamedBranchBuildStrategyImpl.ExactNameFilter("master", false),
                            new NamedBranchBuildStrategyImpl.ExactNameFilter("production", false),
                            new NamedBranchBuildStrategyImpl.RegexNameFilter("^staging-.*$", false),
                            new NamedBranchBuildStrategyImpl.WildcardsNameFilter("feature/*", "feature",false)
                    )
                    ).isAutomaticBuild(
                            new MockSCMSource(c, "dummy"),
                            head,
                            new MockSCMRevision(head, "dummy"),
                            null,
                            null
                    ),
                    is(false)
            );
        }
    }

    @Test
    public void given__regular_head__when__non_match_first__then__isAutomaticBuild_returns_true() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockSCMHead("master");
            assertThat(
                    new NamedBranchBuildStrategyImpl(Arrays.<NamedBranchBuildStrategyImpl.NameFilter>asList(
                            new NamedBranchBuildStrategyImpl.ExactNameFilter("master", false),
                            new NamedBranchBuildStrategyImpl.ExactNameFilter("production", false),
                            new NamedBranchBuildStrategyImpl.RegexNameFilter("^staging-.*$", false),
                            new NamedBranchBuildStrategyImpl.WildcardsNameFilter("feature/*", "feature",false)
                    )
                    ).isAutomaticBuild(
                            new MockSCMSource(c, "dummy"),
                            head,
                            new MockSCMRevision(head, "dummy"),
                            null,
                            null
                    ),
                    is(true)
            );
        }
    }

    @Test
    public void given__regular_head__when__non_match_last__then__isAutomaticBuild_returns_true() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockSCMHead("feature/1");
            assertThat(
                    new NamedBranchBuildStrategyImpl(Arrays.<NamedBranchBuildStrategyImpl.NameFilter>asList(
                            new NamedBranchBuildStrategyImpl.ExactNameFilter("master", false),
                            new NamedBranchBuildStrategyImpl.ExactNameFilter("production", false),
                            new NamedBranchBuildStrategyImpl.RegexNameFilter("^staging-.*$", false),
                            new NamedBranchBuildStrategyImpl.WildcardsNameFilter("feature/*", "feature",false)
                    )
                    ).isAutomaticBuild(
                            new MockSCMSource(c, "dummy"),
                            head,
                            new MockSCMRevision(head, "dummy"),
                            null,
                            null
                    ),
                    is(true)
            );
        }
    }

}
