package hudson.plugins.cmdrunner;

import hudson.model.Descriptor;
import hudson.tasks.Builder;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

/**
 * Descriptor for CmdRunner.
 * 
 * @author Marco Ambu
 */
public final class CmdRunnerDescriptor extends Descriptor<Builder> {

    public CmdRunnerDescriptor() {
        super(CmdRunnerBuilder.class);
        load();
    }

    @Override
    public boolean configure(final StaplerRequest req, final JSONObject formData) {
        save();
        return true;
    }

    @Override
    public String getHelpFile() {
        return "/plugin/cmdrunner/help.html";
    }

    @Override
    public String getDisplayName() {
        return Messages.CmdRunner_DisplayName();
    }

    @Override
    public CmdRunnerBuilder newInstance(final StaplerRequest req, final JSONObject formData) throws FormException {
        return req.bindJSON(CmdRunnerBuilder.class, formData);
    }

}
