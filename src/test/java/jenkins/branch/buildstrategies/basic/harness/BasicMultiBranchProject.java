/*
 * The MIT License
 *
 * Copyright (c) 2016 CloudBees, Inc.
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
 *
 */
package jenkins.branch.buildstrategies.basic.harness;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;
import java.util.logging.Logger;
import jenkins.branch.BranchProjectFactory;
import jenkins.branch.MultiBranchProject;
import jenkins.branch.MultiBranchProjectDescriptor;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;

public class BasicMultiBranchProject extends MultiBranchProject<FreeStyleProject, FreeStyleBuild> {

    private static final Logger LOGGER = Logger.getLogger(BasicMultiBranchProject.class.getName());

    private SCMSourceCriteria criteria;

    public BasicMultiBranchProject(ItemGroup parent, String name) {
        super(parent, name);
    }

    @Override
    protected BranchProjectFactory<FreeStyleProject, FreeStyleBuild> newProjectFactory() {
        return new BasicBranchProjectFactory();
    }

    @Override
    public SCMSourceCriteria getSCMSourceCriteria(@NonNull SCMSource source) {
        return criteria;
    }

    public void setCriteria(SCMSourceCriteria criteria) {
        this.criteria = criteria;
    }

    public SCMSourceCriteria getCriteria() {
        return criteria;
    }

    @Override
    public boolean scheduleBuild() {
        LOGGER.info("Indexing multibranch project: " + getDisplayName());
        return super.scheduleBuild();
    }

    @Extension
    public static class DescriptorImpl extends MultiBranchProjectDescriptor {

        @Override
        public String getDisplayName() {
            return "BasicMultiBranchProject";
        }

        @Override
        public TopLevelItem newInstance(ItemGroup parent, String name) {
            return new BasicMultiBranchProject(parent, name);
        }
    }
}
