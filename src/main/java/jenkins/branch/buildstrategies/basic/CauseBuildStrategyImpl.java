package jenkins.branch.buildstrategies.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Cause;
import hudson.model.Cause.UserIdCause;
import hudson.model.TaskListener;
import hudson.util.LogTaskListener;
import jenkins.branch.BranchBuildStrategy;
import jenkins.branch.BranchBuildStrategyDescriptor;
import jenkins.branch.MultiBranchProject;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Zhenlei Huang
 */
public class CauseBuildStrategyImpl extends BranchBuildStrategy {

	/**
	 * Our constructor.
	 */
	@DataBoundConstructor
	public CauseBuildStrategyImpl() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	@Override
	public boolean isAutomaticBuild(@NonNull SCMSource source, @NonNull SCMHead head, @NonNull SCMRevision currRevision,
	                                @javax.annotation.CheckForNull SCMRevision prevRevision) {
		return isAutomaticBuild(source,head, currRevision, prevRevision, new LogTaskListener(Logger.getLogger(getClass().getName()), Level.INFO));
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	@Override
	public boolean isAutomaticBuild(@NonNull SCMSource source, @NonNull SCMHead head, @NonNull SCMRevision currRevision,
	                                @javax.annotation.CheckForNull SCMRevision prevRevision, @NonNull TaskListener taskListener) {
		return isAutomaticBuild(source,head, currRevision, prevRevision, prevRevision, taskListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	@Override
	public boolean isAutomaticBuild(@NonNull SCMSource source, @NonNull SCMHead head, @NonNull SCMRevision currRevision,
	                                @javax.annotation.CheckForNull SCMRevision lastBuiltRevision, @javax.annotation.CheckForNull SCMRevision lastSeenRevision, @NonNull TaskListener taskListener) {

		return isAutomaticBuild(source, head, currRevision, lastBuiltRevision, lastSeenRevision, taskListener, new Cause[0]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAutomaticBuild(@NonNull SCMSource source,
	                                @NonNull SCMHead head,
	                                @NonNull SCMRevision currRevision,
	                                @CheckForNull SCMRevision lastBuiltRevision,
	                                @CheckForNull SCMRevision lastSeenRevision,
	                                @NonNull TaskListener listener,
	                                @NonNull Cause[] causes) {
		//List<Cause> causes = getCauses(source);

		for (Cause cause : causes) {
			listener.getLogger().println("Cause: " + cause);
			if (cause instanceof UserIdCause) {
				listener.getLogger().println("skip build because of [" + cause + "]");
				return false;
			}
		}
		return true;
	}

	private List<Cause> getParentCauses(SCMSource source) {
		List<Cause> causes = new ArrayList<>();
		if (source.getOwner() instanceof MultiBranchProject) {
			MultiBranchProject project = (MultiBranchProject) source.getOwner();
			causes.addAll(project.getComputation().getCauses());
		}
		return causes;
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
			return Messages.CauseBuildStrategyImpl_displayName();
		}

	}
}
