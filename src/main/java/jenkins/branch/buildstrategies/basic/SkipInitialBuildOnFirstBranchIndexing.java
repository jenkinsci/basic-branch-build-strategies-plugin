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

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.TaskListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.branch.BranchBuildStrategy;
import jenkins.branch.BranchBuildStrategyDescriptor;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;

import edu.umd.cs.findbugs.annotations.CheckForNull;


@Restricted(NoExternalUse.class)
@Extension
public class SkipInitialBuildOnFirstBranchIndexing extends BranchBuildStrategy {

    private static final Logger LOGGER = Logger.getLogger(SkipInitialBuildOnFirstBranchIndexing.class.getName());

    @DataBoundConstructor
    public SkipInitialBuildOnFirstBranchIndexing() {

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
    @Override
    public boolean isAutomaticBuild(@NonNull SCMSource source, @NonNull SCMHead head, @NonNull SCMRevision currRevision,
                                    @CheckForNull SCMRevision lastBuiltRevision, @CheckForNull SCMRevision lastSeenRevision, @NonNull TaskListener taskListener) {
        LOGGER.log(Level.INFO, "lastSeenRevision: {0}, currRevision: {1}",
                new Object[]{
                    lastSeenRevision, currRevision
                }
        );
        if (lastSeenRevision != null && !lastSeenRevision.equals(currRevision)) {
            return true;
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
            return Messages.SkipInitialBuildOnFirstBranchIndexing_displayName();
        }

    }

}
