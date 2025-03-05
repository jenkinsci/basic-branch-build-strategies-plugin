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

import java.io.IOException;
import jenkins.scm.api.SCMHeadOrigin;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import jenkins.scm.impl.mock.MockChangeRequestFlags;
import jenkins.scm.impl.mock.MockChangeRequestSCMHead;
import jenkins.scm.impl.mock.MockChangeRequestSCMRevision;
import jenkins.scm.impl.mock.MockFailure;
import jenkins.scm.impl.mock.MockRepositoryFlags;
import jenkins.scm.impl.mock.MockSCMController;
import jenkins.scm.impl.mock.MockSCMHead;
import jenkins.scm.impl.mock.MockSCMRevision;
import jenkins.scm.impl.mock.MockSCMSource;
import jenkins.scm.impl.mock.MockTagSCMHead;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ChangeRequestBuildStrategyImplTest {

    @Test
    void given__regular_head__when__isAutomaticBuild__then__returns_false() {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockSCMHead("master");
            assertThat(
                    new ChangeRequestBuildStrategyImpl(false, false)
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
    void given__tag_head__when__isAutomaticBuild__then__returns_false() {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockTagSCMHead("master", System.currentTimeMillis());
            assertThat(
                    new ChangeRequestBuildStrategyImpl(false, false)
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
    void given__cr_head__when__isAutomaticBuild__then__returns_true() {
        try (MockSCMController c = MockSCMController.create()) {
            MockChangeRequestSCMHead head = new MockChangeRequestSCMHead(
                    SCMHeadOrigin.DEFAULT, 1, "master", ChangeRequestCheckoutStrategy.MERGE, true);
            assertThat(
                    new ChangeRequestBuildStrategyImpl(false, false)
                            .isAutomaticBuild(
                                    new MockSCMSource(c, "dummy"),
                                    head,
                                    new MockChangeRequestSCMRevision(
                                            head, new MockSCMRevision(new MockSCMHead("master"), "dummy"), "dummy"),
                                    null,
                                    null,
                                    null),
                    is(true));
        }
    }

    @Test
    void given__cr_head_ignoring_untrusted_changes_when__trusted_revision__then__isAutomaticBuild_returns_true()
            throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("dummy", MockRepositoryFlags.TRUST_AWARE);
            Integer crNum = c.openChangeRequest("dummy", "master");

            MockChangeRequestSCMHead head = new MockChangeRequestSCMHead(
                    SCMHeadOrigin.DEFAULT, crNum, "master", ChangeRequestCheckoutStrategy.MERGE, true);
            assertThat(
                    new ChangeRequestBuildStrategyImpl(false, true)
                            .isAutomaticBuild(
                                    new MockSCMSource(c, "dummy"),
                                    head,
                                    new MockChangeRequestSCMRevision(
                                            head, new MockSCMRevision(new MockSCMHead("master"), "dummy"), "dummy"),
                                    null,
                                    null,
                                    null),
                    is(true));
        }
    }

    @Test
    void given__cr_head_ignoring_untrusted_changes_when__trusted_unavailable__then__isAutomaticBuild_returns_false()
            throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("dummy", MockRepositoryFlags.TRUST_AWARE);
            Integer crNum = c.openChangeRequest("dummy", "master");

            MockChangeRequestSCMHead head = new MockChangeRequestSCMHead(
                    SCMHeadOrigin.DEFAULT, crNum, "master", ChangeRequestCheckoutStrategy.MERGE, true);

            c.addFault(new MockFailure() {
                @Override
                public void check(String repository, String branchOrCR, String revision, boolean actions)
                        throws IOException {
                    throw new IOException("Fail");
                }
            });
            assertThat(
                    new ChangeRequestBuildStrategyImpl(false, true)
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

    @Test
    void given__cr_head_ignoring_untrusted_changes_when__untrusted_revision__then__isAutomaticBuild_returns_false()
            throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            c.createRepository("dummy", MockRepositoryFlags.TRUST_AWARE);
            Integer crNum = c.openChangeRequest("dummy", "master", MockChangeRequestFlags.UNTRUSTED);

            MockChangeRequestSCMHead head = new MockChangeRequestSCMHead(
                    SCMHeadOrigin.DEFAULT, crNum, "master", ChangeRequestCheckoutStrategy.MERGE, true);
            assertThat(
                    new ChangeRequestBuildStrategyImpl(false, true)
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

    @Test
    void given__cr_head_ignoring_target_changes__when__first_build__then__isAutomaticBuild_returns_true() {
        try (MockSCMController c = MockSCMController.create()) {
            MockChangeRequestSCMHead head = new MockChangeRequestSCMHead(
                    SCMHeadOrigin.DEFAULT, 1, "master", ChangeRequestCheckoutStrategy.MERGE, true);
            assertThat(
                    new ChangeRequestBuildStrategyImpl(true, false)
                            .isAutomaticBuild(
                                    new MockSCMSource(c, "dummy"),
                                    head,
                                    new MockChangeRequestSCMRevision(
                                            head, new MockSCMRevision(new MockSCMHead("master"), "dummy"), "dummy"),
                                    null,
                                    null,
                                    null),
                    is(true));
        }
    }

    @Test
    void given__cr_head_ignoring_target_changes__when__origin_change__then__isAutomaticBuild_returns_true() {
        try (MockSCMController c = MockSCMController.create()) {
            MockChangeRequestSCMHead head = new MockChangeRequestSCMHead(
                    SCMHeadOrigin.DEFAULT, 1, "master", ChangeRequestCheckoutStrategy.MERGE, true);
            assertThat(
                    new ChangeRequestBuildStrategyImpl(true, false)
                            .isAutomaticBuild(
                                    new MockSCMSource(c, "dummy"),
                                    head,
                                    new MockChangeRequestSCMRevision(
                                            head, new MockSCMRevision(new MockSCMHead("master"), "dummy"), "new-dummy"),
                                    new MockChangeRequestSCMRevision(
                                            head, new MockSCMRevision(new MockSCMHead("master"), "dummy"), "dummy")),
                    is(true));
        }
    }

    @Test
    void given__cr_head_ignoring_target_changes__when__both_change__then__isAutomaticBuild_returns_true() {
        try (MockSCMController c = MockSCMController.create()) {
            MockChangeRequestSCMHead head = new MockChangeRequestSCMHead(
                    SCMHeadOrigin.DEFAULT, 1, "master", ChangeRequestCheckoutStrategy.MERGE, true);
            assertThat(
                    new ChangeRequestBuildStrategyImpl(true, false)
                            .isAutomaticBuild(
                                    new MockSCMSource(c, "dummy"),
                                    head,
                                    new MockChangeRequestSCMRevision(
                                            head, new MockSCMRevision(new MockSCMHead("master"), "dummy"), "new-dummy"),
                                    new MockChangeRequestSCMRevision(
                                            head, new MockSCMRevision(new MockSCMHead("master"), "old-dummy"), "dummy"),
                                    null,
                                    null),
                    is(true));
        }
    }

    @Test
    void given__cr_head_ignoring_target_changes__when__target_change__then__isAutomaticBuild_returns_false() {
        try (MockSCMController c = MockSCMController.create()) {
            MockChangeRequestSCMHead head = new MockChangeRequestSCMHead(
                    SCMHeadOrigin.DEFAULT, 1, "master", ChangeRequestCheckoutStrategy.MERGE, true);
            assertThat(
                    new ChangeRequestBuildStrategyImpl(true, false)
                            .isAutomaticBuild(
                                    new MockSCMSource(c, "dummy"),
                                    head,
                                    new MockChangeRequestSCMRevision(
                                            head, new MockSCMRevision(new MockSCMHead("master"), "dummy"), "dummy"),
                                    new MockChangeRequestSCMRevision(
                                            head, new MockSCMRevision(new MockSCMHead("master"), "old-dummy"), "dummy"),
                                    null,
                                    null),
                    is(false));
        }
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(ChangeRequestBuildStrategyImpl.class)
                .usingGetClass()
                .verify();
    }
}
