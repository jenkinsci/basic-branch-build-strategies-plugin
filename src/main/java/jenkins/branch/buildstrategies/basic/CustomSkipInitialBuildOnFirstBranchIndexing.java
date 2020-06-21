/*
 * The MIT License
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

import javax.annotation.CheckForNull;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Cause;
import hudson.model.TaskListener;
import jenkins.branch.BranchBuildStrategy;
import jenkins.branch.BranchBuildStrategyDescriptor;
import jenkins.branch.BranchIndexingCause;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Zhenlei Huang
 */
public class CustomSkipInitialBuildOnFirstBranchIndexing extends BranchBuildStrategy {

    @DataBoundConstructor
    public CustomSkipInitialBuildOnFirstBranchIndexing() {
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public boolean isAutomaticBuild(@NonNull SCMSource source, @NonNull SCMHead head, @NonNull SCMRevision currRevision,
                                    @CheckForNull SCMRevision prevRevision, TaskListener taskListener) {
        return isAutomaticBuild(source,head, currRevision, prevRevision, prevRevision, taskListener);
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    @Override
    public boolean isAutomaticBuild(@NonNull SCMSource source, @NonNull SCMHead head, @NonNull SCMRevision currRevision,
                                    @CheckForNull SCMRevision lastBuiltRevision, @CheckForNull SCMRevision lastSeenRevision, @NonNull TaskListener taskListener) {
        return isAutomaticBuild(source, head, currRevision, lastBuiltRevision, lastSeenRevision, taskListener, new Cause[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAutomaticBuild(@NonNull SCMSource source, @NonNull SCMHead head, @NonNull SCMRevision currRevision,
                                    @CheckForNull SCMRevision lastBuiltRevision, @CheckForNull SCMRevision lastSeenRevision,
                                    @NonNull TaskListener taskListener, @NonNull Cause[] causes) {
        // skip build on first branch indexing
        if (lastSeenRevision == null && hasBranchIndexingCause(causes)) {
            return false;
        }
        if (currRevision.equals(lastSeenRevision)) {
            return false;
        }
        return true;
    }

    private boolean hasBranchIndexingCause(@NonNull Cause[] causes) {
        for (Cause cause : causes) {
            if (cause instanceof BranchIndexingCause) {
                return true;
            }
        }
        return false;
    }

    /**
     * Our descriptor.
     */
    @Extension
    public static class DescriptorImpl extends BranchBuildStrategyDescriptor {
        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.CustomSkipInitialBuildOnFirstBranchIndexing_displayName();
        }

    }

}
