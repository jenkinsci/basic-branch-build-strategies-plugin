package jenkins.branch.buildstrategies.basic;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Cause;
import hudson.model.TaskListener;
import jenkins.branch.BranchBuildStrategy;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;

/**
 * @author Zhenlei Huang
 */
public class AlwaysBranchBuildStrategyImpl extends BranchBuildStrategy {

	@Override
	public boolean isAutomaticBuild(@NonNull SCMSource source, @NonNull SCMHead head, @NonNull SCMRevision currRevision, SCMRevision lastBuiltRevision, SCMRevision lastSeenRevision, @NonNull TaskListener listener, @NonNull Cause[] causes) {
		return true;
	}
}
