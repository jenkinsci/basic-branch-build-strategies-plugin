package jenkins.branch.buildstrategies.basic;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import jenkins.branch.BranchBuildStrategy;
import jenkins.branch.BranchBuildStrategyDescriptor;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;


@Restricted(NoExternalUse.class)
@Extension
public class SkipInitialBuildOnFirstBranchIndexing extends BranchBuildStrategy {

    @DataBoundConstructor
    public SkipInitialBuildOnFirstBranchIndexing() {

    }

    @Override
    public boolean isAutomaticBuild(@NonNull SCMSource scmSource, @NonNull SCMHead scmHead, @NonNull SCMRevision currRevision, SCMRevision prevRevision) {
        if (prevRevision != null) {
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
