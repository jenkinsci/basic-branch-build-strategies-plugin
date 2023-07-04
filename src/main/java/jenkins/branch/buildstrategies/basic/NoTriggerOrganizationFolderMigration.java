package jenkins.branch.buildstrategies.basic;

import hudson.BulkChange;
import hudson.Extension;
import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.branch.NoTriggerOrganizationFolderProperty;
import jenkins.branch.OrganizationFolder;
import jenkins.branch.PropertyMigration;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Restricted(NoExternalUse.class)
@Extension
public class NoTriggerOrganizationFolderMigration
        extends PropertyMigration.Migrator<OrganizationFolder, NoTriggerOrganizationFolderProperty> {

    private static final Logger LOGGER = Logger.getLogger(NoTriggerOrganizationFolderMigration.class.getName());

    public NoTriggerOrganizationFolderMigration() {
        super(OrganizationFolder.class, NoTriggerOrganizationFolderProperty.class);
    }

    @Override
    public void apply(OrganizationFolder folder, NoTriggerOrganizationFolderProperty property) {
        BulkChange bc = new BulkChange(folder);
        try {
            folder.getBuildStrategies()
                    .add(new NamedBranchBuildStrategyImpl(
                            Collections.<NamedBranchBuildStrategyImpl.NameFilter>singletonList(
                                    new NamedBranchBuildStrategyImpl.RegexNameFilter(property.getBranches(), true))));
            folder.getProperties().remove(property);
            try {
                bc.commit();
            } catch (IOException e) {
                LOGGER.log(
                        Level.WARNING,
                        "Could not persist configuration migration for " + folder.getFullName()
                                + ", will retry next restart",
                        e);
            }
        } catch (RuntimeException | Error e) {
            bc.abort();
            throw e;
        }
    }
}
