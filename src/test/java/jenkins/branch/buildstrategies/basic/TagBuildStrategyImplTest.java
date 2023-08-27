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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.concurrent.TimeUnit;
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

public class TagBuildStrategyImplTest {
    @Test
    public void given__regular_head__when__isAutomaticBuild__then__returns_false() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockSCMHead("master");
            TagBuildStrategyImpl strategy = new TagBuildStrategyImpl(null, null);
            assertThat(
                    strategy.isAutomaticBuild(
                            new MockSCMSource(c, "dummy"), head, new MockSCMRevision(head, "dummy"), null),
                    is(false));
            assertThat(strategy.toString(), is("TagBuildStrategyImpl{atLeast=n/a, atMost=n/a}"));
            assertThat(strategy.hashCode(), is(strategy.hashCode())); // same object has same hash
            assertThat(strategy, is(strategy)); // same object is equal to itself
            assertThat(strategy, is(not(head))); // object of different class is not equal
            assertThat(strategy.getAtLeastMillis(), is(strategy.getAtMostMillis()));
        }
    }

    @Test
    public void given__tag_head__when__isAutomaticBuild__then__returns_true() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockTagSCMHead("master", System.currentTimeMillis());
            TagBuildStrategyImpl strategy = new TagBuildStrategyImpl(null, null);
            assertThat(
                    strategy.isAutomaticBuild(
                            new MockSCMSource(c, "dummy"), head, new MockSCMRevision(head, "dummy"), null, null),
                    is(true));
            assertThat(strategy.toString(), is("TagBuildStrategyImpl{atLeast=n/a, atMost=n/a}"));
        }
    }

    @Test
    public void given__tag_head__when__tag_newer_than_atMostDays__then__isAutomaticBuild_returns_true()
            throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockTagSCMHead("master", System.currentTimeMillis());
            TagBuildStrategyImpl strategy = new TagBuildStrategyImpl(null, "1");
            assertThat(
                    strategy.isAutomaticBuild(
                            new MockSCMSource(c, "dummy"), head, new MockSCMRevision(head, "dummy"), null, null, null),
                    is(true));
            assertThat(strategy.toString(), is("TagBuildStrategyImpl{atLeast=n/a, atMost=1 day 0 hr}"));
        }
    }

    @Test
    public void given__tag_head__when__tag_older_than_atMostDays__then__isAutomaticBuild_returns_false()
            throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockTagSCMHead("master", System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2));
            TagBuildStrategyImpl strategy = new TagBuildStrategyImpl(null, "1");
            assertThat(
                    strategy.isAutomaticBuild(
                            new MockSCMSource(c, "dummy"), head, new MockSCMRevision(head, "dummy"), null),
                    is(false));
            assertThat(strategy.toString(), is("TagBuildStrategyImpl{atLeast=n/a, atMost=1 day 0 hr}"));
        }
    }

    @Test
    public void given__tag_head__when__tag_newer_than_atLeastDays__then__isAutomaticBuild_returns_false()
            throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockTagSCMHead("master", System.currentTimeMillis());
            TagBuildStrategyImpl strategy = new TagBuildStrategyImpl("1", null);
            assertThat(
                    strategy.isAutomaticBuild(
                            new MockSCMSource(c, "dummy"), head, new MockSCMRevision(head, "dummy"), null, null),
                    is(false));
            assertThat(strategy.toString(), is("TagBuildStrategyImpl{atLeast=1 day 0 hr, atMost=n/a}"));
        }
    }

    @Test
    public void given__tag_head__when__tag_older_than_atLeastDays__then__isAutomaticBuild_returns_true()
            throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockTagSCMHead("master", System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2));
            TagBuildStrategyImpl strategy = new TagBuildStrategyImpl("1", null);
            assertThat(
                    strategy.isAutomaticBuild(
                            new MockSCMSource(c, "dummy"), head, new MockSCMRevision(head, "dummy"), null, null, null),
                    is(true));
            assertThat(strategy.toString(), is("TagBuildStrategyImpl{atLeast=1 day 0 hr, atMost=n/a}"));
        }
    }

    @Test
    public void given__tag_head__when__atLeastDays_invalid_with_atMostDays__then__isAutomaticBuild_returns_false()
            throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            for (int offset = 0; offset <= 4; offset++) {
                MockSCMHead head =
                        new MockTagSCMHead("master", System.currentTimeMillis() - TimeUnit.DAYS.toMillis(offset));
                TagBuildStrategyImpl strategy = new TagBuildStrategyImpl("3", "1");
                assertThat(
                        strategy.isAutomaticBuild(
                                new MockSCMSource(c, "dummy"),
                                head,
                                new MockSCMRevision(head, "dummy"),
                                null,
                                null,
                                null),
                        is(false));
                assertThat(strategy.toString(), is("TagBuildStrategyImpl{atLeast=3 days 0 hr, atMost=1 day 0 hr}"));
            }
        }
    }

    @Test
    public void given__tag_head__when__atLeastDays_and_atMostDays__then__isAutomaticBuild_returns_false_before_atLeast()
            throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockTagSCMHead("master", System.currentTimeMillis());
            TagBuildStrategyImpl strategy = new TagBuildStrategyImpl("1", "3");
            assertThat(
                    strategy.isAutomaticBuild(
                            new MockSCMSource(c, "dummy"), head, new MockSCMRevision(head, "dummy"), null),
                    is(false));
            assertThat(strategy.toString(), is("TagBuildStrategyImpl{atLeast=1 day 0 hr, atMost=3 days 0 hr}"));
        }
    }

    @Test
    public void given__tag_head__when__atLeastDays_and_atMostDays__then__isAutomaticBuild_returns_true_between()
            throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockTagSCMHead("master", System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2));
            TagBuildStrategyImpl strategy = new TagBuildStrategyImpl("1", "3");
            assertThat(
                    strategy.isAutomaticBuild(
                            new MockSCMSource(c, "dummy"), head, new MockSCMRevision(head, "dummy"), null, null),
                    is(true));
            assertThat(strategy.toString(), is("TagBuildStrategyImpl{atLeast=1 day 0 hr, atMost=3 days 0 hr}"));
        }
    }

    @Test
    public void given__tag_head__when__atLeastDays_and_atMostDays__then__isAutomaticBuild_returns_false_after_atMost()
            throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockTagSCMHead("master", System.currentTimeMillis() - TimeUnit.DAYS.toMillis(4));
            TagBuildStrategyImpl strategy = new TagBuildStrategyImpl("1", "3");
            assertThat(
                    strategy.isAutomaticBuild(
                            new MockSCMSource(c, "dummy"), head, new MockSCMRevision(head, "dummy"), null, null, null),
                    is(false));
            assertThat(strategy.toString(), is("TagBuildStrategyImpl{atLeast=1 day 0 hr, atMost=3 days 0 hr}"));
        }
    }

    @Test
    public void given__cr_head__when__isAutomaticBuild__then__returns_false() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockChangeRequestSCMHead head = new MockChangeRequestSCMHead(
                    SCMHeadOrigin.DEFAULT, 1, "master", ChangeRequestCheckoutStrategy.MERGE, true);
            assertThat(
                    new TagBuildStrategyImpl(null, null)
                            .isAutomaticBuild(
                                    new MockSCMSource(c, "dummy"),
                                    head,
                                    new MockChangeRequestSCMRevision(
                                            head, new MockSCMRevision(new MockSCMHead("master"), "dummy"), "dummy"),
                                    null,
                                    null,
                                    null),
                    is(false));
        }
    }
}
