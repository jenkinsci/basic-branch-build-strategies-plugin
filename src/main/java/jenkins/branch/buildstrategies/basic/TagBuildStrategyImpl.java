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
import hudson.Util;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import hudson.model.TaskListener;
import hudson.util.LogTaskListener;
import jenkins.branch.BranchBuildStrategy;
import jenkins.branch.BranchBuildStrategyDescriptor;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.mixin.ChangeRequestSCMHead;
import jenkins.scm.api.mixin.TagSCMHead;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.stapler.DataBoundConstructor;

import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;

/**
 * A {@link BranchBuildStrategy} that builds tags.
 *
 * @since 1.0.0
 */
public class TagBuildStrategyImpl extends BranchBuildStrategy {
    private final long atLeastMillis;
    private final long atMostMillis;

    /**
     * Our constructor.
     *
     * @param atLeastDays the number of days old that the tag must be before it is considered for automatic build
     * @param atMostDays the number of days old that the tag must be after which it is no longer considered for automatic build.
     */
    @DataBoundConstructor
    public TagBuildStrategyImpl(@CheckForNull String atLeastDays, @CheckForNull String atMostDays) {
        this(
                TimeUnit.DAYS,
                Long.parseLong(StringUtils.defaultIfBlank(atLeastDays, "-1")),
                Long.parseLong(StringUtils.defaultIfBlank(atMostDays, "-1"))
        );
    }

    /**
     * Constructor for testing.
     *
     * @param unit    the time units.
     * @param atLeast {@code null} or {@code -1L} to disable filtering by minimum age, otherwise the minimum age
     *                            expressed in the supplied time units.
     * @param atMost  {@code null} or {@code -1L} to disable filtering by maximum age, otherwise the maximum age
     *                expressed in the supplied time units.
     */
    public TagBuildStrategyImpl(@NonNull TimeUnit unit, @CheckForNull Number atLeast, @CheckForNull Number atMost) {
        this.atLeastMillis = atLeast == null || atLeast.longValue() < 0L ? -1L : unit.toMillis(atLeast.longValue());
        this.atMostMillis = atMost == null || atMost.longValue() < 0L ? -1L : unit.toMillis(atMost.longValue());
    }

    @Restricted(DoNotUse.class) // stapler form binding only
    @NonNull
    public String getAtLeastDays() {
        return atLeastMillis >= 0L ? Long.toString(TimeUnit.MILLISECONDS.toDays(atLeastMillis)) : "";
    }

    @Restricted(DoNotUse.class) // stapler form binding only
    @NonNull
    public String getAtMostDays() {
        return atMostMillis >= 0L ? Long.toString(TimeUnit.MILLISECONDS.toDays(atMostMillis)) : "";
    }

    public long getAtLeastMillis() {
        return atLeastMillis;
    }

    public long getAtMostMillis() {
        return atMostMillis;
    }

    @CheckForNull
    public Long getAtLeast(@NonNull TimeUnit unit) {
        return atLeastMillis >= 0L ? unit.convert(atLeastMillis, TimeUnit.MILLISECONDS) : null;
    }

    @CheckForNull
    public Long getAtMost(@NonNull TimeUnit unit) {
        return atMostMillis >= 0L ? unit.convert(atMostMillis, TimeUnit.MILLISECONDS) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    @Override
    public boolean isAutomaticBuild(@NonNull SCMSource source, @NonNull SCMHead head, @NonNull SCMRevision currRevision,
                                    SCMRevision prevRevision) {
        return isAutomaticBuild(source, head, currRevision, prevRevision, new LogTaskListener(LOGGER, Level.INFO));
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    @Override
    public boolean isAutomaticBuild(@NonNull SCMSource source, @NonNull SCMHead head, @NonNull SCMRevision currRevision,
                                    SCMRevision prevRevision, TaskListener taskListener) {
        return isAutomaticBuild(source, head, currRevision, prevRevision, taskListener, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAutomaticBuild(@NonNull SCMSource source, @NonNull SCMHead head, @NonNull SCMRevision currRevision,
                                    @CheckForNull SCMRevision prevRevision, TaskListener taskListener, SCMRevision lastSeenRevision) {
        if (!(head instanceof TagSCMHead)) {
            return false;
        }
        if (atLeastMillis >= 0L || atMostMillis >= 0L) {
            if (atMostMillis >= 0L && atLeastMillis > atMostMillis) {
                // stupid configuration that corresponds to never building anything, why did the user add it against
                // our advice?
                return false;
            }
            long tagAge = System.currentTimeMillis() - ((TagSCMHead)head).getTimestamp();
            if (atMostMillis >= 0L && tagAge > atMostMillis) {
                return false;
            }
            if (atLeastMillis >= 0L && tagAge < atLeastMillis) {
                return false;
            }
        }
        return true;
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

        TagBuildStrategyImpl that = (TagBuildStrategyImpl) o;

        if (atLeastMillis != that.atLeastMillis) {
            return false;
        }
        return atMostMillis == that.atMostMillis;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = (int) (atLeastMillis ^ (atLeastMillis >>> 32));
        result = 31 * result + (int) (atMostMillis ^ (atMostMillis >>> 32));
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "TagBuildStrategyImpl{" +
                "atLeast=" + (atLeastMillis >= 0L ? Util.getPastTimeString(atLeastMillis) : "n/a") +
                ", atMost=" + (atMostMillis >= 0L ? Util.getPastTimeString(atMostMillis) : "n/a")+
                '}';
    }

    /**
     * Our descriptor.
     */
    @Symbol("buildTags")
    @Extension
    public static class DescriptorImpl extends BranchBuildStrategyDescriptor {
        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.TagBuildStrategyImpl_displayName();
        }
    }
}
