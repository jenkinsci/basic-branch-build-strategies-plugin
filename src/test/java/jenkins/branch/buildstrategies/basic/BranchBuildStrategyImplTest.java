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

import jenkins.scm.api.SCMHeadOrigin;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import jenkins.scm.impl.mock.MockChangeRequestSCMHead;
import jenkins.scm.impl.mock.MockChangeRequestSCMRevision;
import jenkins.scm.impl.mock.MockSCMController;
import jenkins.scm.impl.mock.MockSCMHead;
import jenkins.scm.impl.mock.MockSCMRevision;
import jenkins.scm.impl.mock.MockSCMSource;
import jenkins.scm.impl.mock.MockTagSCMHead;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class BranchBuildStrategyImplTest {

    @Test
    void given__regular_head__when__isAutomaticBuild__then__returns_true() {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockSCMHead("master");
            assertThat(
                    new BranchBuildStrategyImpl()
                            .isAutomaticBuild(
                                    new MockSCMSource(c, "dummy"),
                                    head,
                                    new MockSCMRevision(head, "dummy"),
                                    null,
                                    null,
                                    null),
                    is(true));
        }
    }

    @Test
    void given__tag_head__when__isAutomaticBuild__then__returns_false() {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockTagSCMHead("master", System.currentTimeMillis());
            assertThat(
                    new BranchBuildStrategyImpl()
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
    void given__cr_head__when__isAutomaticBuild__then__returns_false() {
        try (MockSCMController c = MockSCMController.create()) {
            MockChangeRequestSCMHead head = new MockChangeRequestSCMHead(
                    SCMHeadOrigin.DEFAULT, 1, "master", ChangeRequestCheckoutStrategy.MERGE, true);
            assertThat(
                    new BranchBuildStrategyImpl()
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
    void equalsContract() {
        EqualsVerifier.forClass(BranchBuildStrategyImpl.class).usingGetClass().verify();
    }
}
