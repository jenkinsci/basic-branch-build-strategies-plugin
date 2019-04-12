/*
 * The MIT License
 *
 * Copyright (c) 2018-2019, Bodybuilding.com, CloudBees, Inc.
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

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import jenkins.branch.BranchBuildStrategy;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.impl.mock.MockSCMController;
import jenkins.scm.impl.mock.MockSCMHead;
import jenkins.scm.impl.mock.MockSCMRevision;
import jenkins.scm.impl.mock.MockSCMSource;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class AnyBranchBuildStrategyImplTest {
    @Test
    public void given__no_strategies__when__isAutomaticBuild__then__returns_false() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockSCMHead("master");
            assertThat(
                new AnyBranchBuildStrategyImpl(Collections.emptyList()
                ).isAutomaticBuild(
                    new MockSCMSource(c, "dummy"),
                    head,
                    new MockSCMRevision(head, "dummy"),
                    null,
                        null,
                        null
                ),
                is(false)
            );
        }
    }


    @Test
    public void given__true_strategies__when__isAutomaticBuild__then__returns_true() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockSCMHead("master");
            assertThat(
                new AnyBranchBuildStrategyImpl(Arrays.asList(
                    new BranchBuildStrategy() {
                        @Override
                        public boolean isAutomaticBuild(@NonNull SCMSource scmSource, @NonNull SCMHead scmHead, @NonNull SCMRevision scmRevision, SCMRevision scmRevision1, TaskListener taskListener, SCMRevision lastSeenRevision) {
                            return true;
                        }
                    },
                    new BranchBuildStrategy() {
                        @Override
                        public boolean isAutomaticBuild(@NonNull SCMSource scmSource, @NonNull SCMHead scmHead, @NonNull SCMRevision scmRevision, SCMRevision scmRevision1, TaskListener taskListener, SCMRevision lastSeenRevision) {
                            return true;
                        }
                    }
                )).isAutomaticBuild(
                    new MockSCMSource(c, "dummy"),
                    head,
                    new MockSCMRevision(head, "dummy"),
                    null,
                        null,
                        null
                ),
                is(true)
            );
        }
    }

    @Test
    public void given__false_strategies__when__isAutomaticBuild__then__returns_false() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockSCMHead("master");
            assertThat(
                new AnyBranchBuildStrategyImpl(Arrays.asList(
                    new BranchBuildStrategy() {
                        @Override
                        public boolean isAutomaticBuild(@NonNull SCMSource scmSource, @NonNull SCMHead scmHead, @NonNull SCMRevision scmRevision, SCMRevision scmRevision1, TaskListener taskListener, SCMRevision lastSeenRevision) {
                            return false;
                        }
                    },
                    new BranchBuildStrategy() {
                        @Override
                        public boolean isAutomaticBuild(@NonNull SCMSource scmSource, @NonNull SCMHead scmHead, @NonNull SCMRevision scmRevision, SCMRevision scmRevision1, TaskListener taskListener, SCMRevision lastSeenRevision) {
                            return false;
                        }
                    }
                )).isAutomaticBuild(
                    new MockSCMSource(c, "dummy"),
                    head,
                    new MockSCMRevision(head, "dummy"),
                    null,
                        null,
                        null
                ),
                is(false)
            );
        }
    }


    @Test
    public void given__true_and_false_strategies__when__isAutomaticBuild__then__returns_true_with_short_circuit() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockSCMHead("master");
            assertThat(
                new AnyBranchBuildStrategyImpl(Arrays.asList(
                    new BranchBuildStrategy() {
                        @Override
                        public boolean isAutomaticBuild(@NonNull SCMSource scmSource, @NonNull SCMHead scmHead, @NonNull SCMRevision scmRevision, SCMRevision scmRevision1, TaskListener taskListener, SCMRevision lastSeenRevision) {
                            return true;
                        }
                    },
                    new BranchBuildStrategy() {
                        @Override
                        public boolean isAutomaticBuild(@NonNull SCMSource scmSource, @NonNull SCMHead scmHead, @NonNull SCMRevision scmRevision, SCMRevision scmRevision1, TaskListener taskListener, SCMRevision lastSeenRevision) {
                            fail("strategy evaluation must short circuit");
                            return false;
                        }
                    }
                )).isAutomaticBuild(
                    new MockSCMSource(c, "dummy"),
                    head,
                    new MockSCMRevision(head, "dummy"),
                    null,
                    null,
                        null
                ),
                is(true)
            );
        }
    }

}
