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
import hudson.Extension;
import hudson.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hudson.model.TaskListener;
import jenkins.branch.BranchBuildStrategy;
import jenkins.branch.BranchBuildStrategyDescriptor;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A {@link BranchBuildStrategy} that builds branches based on the results of all sub strategies matching.
 *
 * @since 1.0.1
 */
public class AllBranchBuildStrategyImpl extends BranchBuildStrategy {

    /**
     * The list of filters.
     */
    @NonNull
    private final List<BranchBuildStrategy> strategies;

    /**
     * Our constructor.
     * @param strategies the strategies to apply.
     */
    @DataBoundConstructor
    public AllBranchBuildStrategyImpl(List<BranchBuildStrategy> strategies) {
        this.strategies = new ArrayList<>(Util.fixNull(strategies));
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public boolean isAutomaticBuild(@NonNull SCMSource source, @NonNull SCMHead head, @NonNull SCMRevision currRevision,
                                    SCMRevision prevRevision) {
        return isAutomaticBuild(source,head, currRevision, prevRevision, null);
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public boolean isAutomaticBuild(@NonNull SCMSource source, @NonNull SCMHead head, @NonNull SCMRevision currRevision,
                                    SCMRevision prevRevision, TaskListener taskListener) {
        return isAutomaticBuild(source,head, currRevision, prevRevision, prevRevision, taskListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAutomaticBuild(@NonNull SCMSource source, @NonNull SCMHead head, @NonNull SCMRevision currRevision,
                                    SCMRevision lastBuiltRevision, SCMRevision lastSeenRevision, TaskListener taskListener) {

        if(strategies.isEmpty()){
            return false;
        }

        for (BranchBuildStrategy strategy: strategies) {
            if(!strategy.automaticBuild(
                source,
                head,
                currRevision,
                lastBuiltRevision,
                lastSeenRevision,
                taskListener
            )){
                return false;
            };

        }
        return true;
    }

    @NonNull
    public List<BranchBuildStrategy> getStrategies() {
        return Collections.unmodifiableList(strategies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AllBranchBuildStrategyImpl that = (AllBranchBuildStrategyImpl) o;

        return strategies.equals(that.strategies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return strategies.hashCode();
    }

    @Override
    public String toString() {
        return "AllBranchBuildStrategyImpl{" +
                "strategies=" + strategies +
                '}';
    }


    /**
     * Our descriptor.
     */
    @Symbol("buildAllBranches")
    @Extension
    public static class DescriptorImpl extends BranchBuildStrategyDescriptor {
        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.AllBranchBuildStrategyImpl_displayName();
        }
    }

}
