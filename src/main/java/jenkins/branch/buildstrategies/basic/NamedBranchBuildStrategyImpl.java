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
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.Nonnull;
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
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * A {@link BranchBuildStrategy} that builds branches with specific names.
 *
 * @since 1.0.1
 */
public class NamedBranchBuildStrategyImpl extends BranchBuildStrategy {

    /**
     * The list of filters.
     */
    @NonNull
    private final List<NameFilter> filters;

    /**
     * Our constructor.
     * @param filters the filters to apply.
     */
    @DataBoundConstructor
    public NamedBranchBuildStrategyImpl(List<NameFilter> filters) {
        this.filters = new ArrayList<>(Util.fixNull(filters));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAutomaticBuild(@NonNull SCMSource source, @NonNull SCMHead head, @NonNull SCMRevision currRevision,
                                    SCMRevision prevRevision) {
        if (head instanceof ChangeRequestSCMHead) {
            return false;
        }
        if (head instanceof TagSCMHead) {
            return false;
        }
        String name = head.getName();
        for (NameFilter filter: filters) {
            if (filter.isMatch(name)) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    public List<NameFilter> getFilters() {
        return Collections.unmodifiableList(filters);
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

        NamedBranchBuildStrategyImpl that = (NamedBranchBuildStrategyImpl) o;

        return filters.equals(that.filters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return filters.hashCode();
    }

    @Override
    public String toString() {
        return "NamedBranchBuildStrategyImpl{" +
                "filters=" + filters +
                '}';
    }


    /**
     * Our descriptor.
     */
    @Symbol("buildNamedBranches")
    @Extension
    public static class DescriptorImpl extends BranchBuildStrategyDescriptor {
        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.NamedBranchBuildStrategyImpl_displayName();
        }
    }

    public static abstract class NameFilter extends AbstractDescribableImpl<NameFilter> {
        public abstract boolean isMatch(@NonNull String name);

        @Override
        public abstract int hashCode();

        @Override
        public abstract boolean equals(Object obj);

        @Override
        public abstract String toString();
    }

    public static abstract class NameFilterDescriptor extends Descriptor<NameFilter> {

    }

    public static class ExactNameFilter extends NameFilter {
        @NonNull
        private final String name;
        private final boolean caseSensitive;

        @DataBoundConstructor
        public ExactNameFilter(@CheckForNull String name, boolean caseSensitive) {
            this.name = Util.fixNull(name);
            this.caseSensitive = caseSensitive;
        }

        @NonNull
        public String getName() {
            return name;
        }

        public boolean isCaseSensitive() {
            return caseSensitive;
        }

        @Override
        public boolean isMatch(@NonNull String name) {
            return this.caseSensitive ? this.name.equals(name) : this.name.equalsIgnoreCase(name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ExactNameFilter that = (ExactNameFilter) o;

            if (caseSensitive != that.caseSensitive) {
                return false;
            }
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + (caseSensitive ? 1 : 0);
            return result;
        }

        @Override
        public String toString() {
            return "ExactNameFilter{" +
                    "name='" + name + '\'' +
                    ", caseSensitive=" + caseSensitive +
                    '}';
        }

        @Symbol("exact")
        @Extension
        public static class DescriptorImpl extends NameFilterDescriptor {
            @Nonnull
            @Override
            public String getDisplayName() {
                return Messages.NamedBranchBuildStrategyImpl_exactDisplayName();
            }
        }

    }

    public static class RegexNameFilter extends NameFilter {
        @NonNull
        private final String regex;
        private final boolean caseSensitive;
        private transient Pattern pattern;

        @DataBoundConstructor
        public RegexNameFilter(@CheckForNull String regex, boolean caseSensitive) {
            this.regex = StringUtils.defaultIfBlank(regex, "^.*$");
            this.caseSensitive = caseSensitive;
            pattern = Pattern.compile(this.regex, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
        }

        @NonNull
        public String getRegex() {
            return regex;
        }

        public boolean isCaseSensitive() {
            return caseSensitive;
        }

        @Override
        public boolean isMatch(@NonNull String name) {
            if (pattern == null) {
                pattern = Pattern.compile(regex, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
            }
            return pattern.matcher(name).matches();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            RegexNameFilter that = (RegexNameFilter) o;

            if (caseSensitive != that.caseSensitive) {
                return false;
            }
            return regex.equals(that.regex);
        }

        @Override
        public int hashCode() {
            int result = regex.hashCode();
            result = 31 * result + (caseSensitive ? 1 : 0);
            return result;
        }

        @Override
        public String toString() {
            return "RegexNameFilter{" +
                    "regex=/" + regex + '/' +
                    ", caseSensitive=" + caseSensitive +
                    '}';
        }

        @Symbol("regex")
        @Extension
        public static class DescriptorImpl extends NameFilterDescriptor {
            @Nonnull
            @Override
            public String getDisplayName() {
                return Messages.NamedBranchBuildStrategyImpl_regexDisplayName();
            }

            /**
             * Form validation for the regular expression.
             *
             * @param value the regular expression.
             * @return the validation results.
             */
            @Restricted(NoExternalUse.class) // stapler
            public FormValidation doCheckRegex(@QueryParameter String value) {
                try {
                    Pattern.compile(value);
                    return FormValidation.ok();
                } catch (PatternSyntaxException e) {
                    return FormValidation.error(e.getMessage());
                }
            }
        }

    }

    public static class WildcardsNameFilter extends NameFilter {
        @NonNull
        private final String includes;
        @NonNull
        private final String excludes;
        private final boolean caseSensitive;
        private transient Pattern includePattern;
        private transient Pattern excludePattern;

        @DataBoundConstructor
        public WildcardsNameFilter(@CheckForNull String includes, @CheckForNull String excludes, boolean caseSensitive) {
            this.includes = StringUtils.defaultIfBlank(includes, "*");
            this.excludes = StringUtils.defaultIfBlank(excludes, "");
            this.caseSensitive = caseSensitive;
            includePattern = Pattern.compile(getPattern(this.includes), caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
            excludePattern = Pattern.compile(getPattern(this.excludes), caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
        }

        @NonNull
        public String getIncludes() {
            return includes;
        }

        @NonNull
        public String getExcludes() {
            return excludes;
        }

        public boolean isCaseSensitive() {
            return caseSensitive;
        }

        @Override
        public boolean isMatch(@NonNull String name) {
            if (includePattern == null) {
                includePattern = Pattern.compile(getPattern(includes), caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
            }
            if (!includePattern.matcher(name).matches()) {
                return false;
            }
            if (StringUtils.isBlank(excludes)) {
                return true;
            }
            if (excludePattern == null) {
                excludePattern = Pattern.compile(getPattern(excludes), caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
            }
            return !excludePattern.matcher(name).matches();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            WildcardsNameFilter that = (WildcardsNameFilter) o;

            if (caseSensitive != that.caseSensitive) {
                return false;
            }
            if (!includes.equals(that.includes)) {
                return false;
            }
            return excludes.equals(that.excludes);
        }

        @Override
        public int hashCode() {
            int result = includes.hashCode();
            result = 31 * result + excludes.hashCode();
            result = 31 * result + (caseSensitive ? 1 : 0);
            return result;
        }

        @Override
        public String toString() {
            return "WildcardsNameFilter{" +
                    "includes='" + includes + '\'' +
                    ", excludes='" + excludes + '\'' +
                    ", caseSensitive=" + caseSensitive +
                    '}';
        }

        /**
         * Returns the pattern corresponding to the branches containing wildcards.
         *
         * @param names the names of branches to create a pattern for
         * @return pattern corresponding to the branches containing wildcards
         */
        private String getPattern(String names) {
            StringBuilder quotedBranches = new StringBuilder();
            for (String wildcard : names.split(" ")) {
                StringBuilder quotedBranch = new StringBuilder();
                for (String branch : wildcard.split("(?=[*])|(?<=[*])")) {
                    if (branch.equals("*")) {
                        quotedBranch.append(".*");
                    } else if (!branch.isEmpty()) {
                        quotedBranch.append(Pattern.quote(branch));
                    }
                }
                if (quotedBranches.length() > 0) {
                    quotedBranches.append("|");
                }
                quotedBranches.append(quotedBranch);
            }
            return quotedBranches.toString();
        }

        @Symbol("wildcards")
        @Extension
        public static class DescriptorImpl extends NameFilterDescriptor {
            @Nonnull
            @Override
            public String getDisplayName() {
                return Messages.NamedBranchBuildStrategyImpl_wildcardDisplayName();
            }
        }

    }

}
