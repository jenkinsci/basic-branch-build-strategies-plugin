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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.TaskListener;
import hudson.util.LogTaskListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.branch.BranchBuildStrategy;
import jenkins.branch.BranchBuildStrategyDescriptor;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.mixin.ChangeRequestSCMHead;
import jenkins.scm.api.mixin.TagSCMHead;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A {@link BranchBuildStrategy} that builds things that are neither tags nor change requests.
 *
 * @since 1.0.0
 */
public class BranchBuildStrategyImpl extends BranchBuildStrategy {
    /**
     * Our constructor.
     */
    @DataBoundConstructor
    public BranchBuildStrategyImpl() {}

    /**
     * {@inheritDoc}
     */
    @Deprecated
    @Override
    public boolean isAutomaticBuild(
            @NonNull SCMSource source,
            @NonNull SCMHead head,
            @NonNull SCMRevision currRevision,
            @CheckForNull SCMRevision prevRevision) {
        return isAutomaticBuild(
                source,
                head,
                currRevision,
                prevRevision,
                new LogTaskListener(Logger.getLogger(getClass().getName()), Level.INFO));
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    @Override
    public boolean isAutomaticBuild(
            @NonNull SCMSource source,
            @NonNull SCMHead head,
            @NonNull SCMRevision currRevision,
            @CheckForNull SCMRevision prevRevision,
            @NonNull TaskListener taskListener) {
        return isAutomaticBuild(source, head, currRevision, prevRevision, prevRevision, taskListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAutomaticBuild(
            @NonNull SCMSource source,
            @NonNull SCMHead head,
            @NonNull SCMRevision currRevision,
            @CheckForNull SCMRevision lastBuiltRevision,
            @CheckForNull SCMRevision lastSeenRevision,
            @NonNull TaskListener taskListener) {
        if (head instanceof ChangeRequestSCMHead) {
            return false;
        }
        if (head instanceof TagSCMHead) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return BranchBuildStrategyImpl.class.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj != null && getClass().equals(obj.getClass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "BranchBuildStrategyImpl{}";
    }

    /**
     * Our descriptor.
     */
    @Symbol("buildRegularBranches")
    @Extension
    public static class DescriptorImpl extends BranchBuildStrategyDescriptor {
        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.BranchBuildStrategyImpl_displayName();
        }
    }
}
