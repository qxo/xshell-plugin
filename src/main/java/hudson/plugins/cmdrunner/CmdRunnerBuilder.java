package hudson.plugins.cmdrunner;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.tasks.Builder;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * CmdRunner Builder Plugin.
 * 
 * @author Marco Ambu
 */
public final class CmdRunnerBuilder extends Builder {

	private static final Logger LOG = Logger.getLogger(CmdRunnerBuilder.class
			.getName());

	@Extension
	public static final CmdRunnerDescriptor DESCRIPTOR = new CmdRunnerDescriptor();

	/**
	 * Command line.
	 */
	private final String commandLine;

	/**
	 * Specify if command is executed from working dir.
	 */
	private final Boolean executeFromWorkingDir;

	public String getCommandLine() {
		return commandLine;
	}

	public Boolean getExecuteFromWorkingDir() {
		return executeFromWorkingDir;
	}

	@DataBoundConstructor
	public CmdRunnerBuilder(final String commandLine,
			final Boolean executeFromWorkingDir) {
		this.commandLine = Util.fixEmptyAndTrim(commandLine);
		this.executeFromWorkingDir = executeFromWorkingDir;
	}

	@Override
	public Descriptor<Builder> getDescriptor() {
		return DESCRIPTOR;
	}

	@Override
	public boolean perform(final AbstractBuild<?, ?> build,
			final Launcher launcher, final BuildListener listener)
			throws InterruptedException, IOException {

		LOG.log(Level.FINE, "Unmodified command line: " + commandLine);

		String cmdLine = commandLine;// convertSeparator(commandLine,
										// (launcher.isUnix() ? UNIX_SEP :
										// WINDOWS_SEP));
		LOG.log(Level.FINE, "File separators sanitized: " + cmdLine);

		EnvVars env = build.getEnvironment(listener);
		env.putAll(build.getBuildVariables());

		cmdLine = env.expand(cmdLine);
		LOG.log(Level.FINEST, "Environment variables: "
				+ env.entrySet().toString());
		// LOG.log(Level.FINE, "Command line: " + args.toStringWithQuote());
		LOG.log(Level.FINE, "Working directory: " + build.getWorkspace());
		//
		// if (launcher.isUnix()) {
		// cmdLine = convertEnvVarsToUnix(cmdLine);
		// cmdLine = convertEnvVarsToWindows(cmdLine);
		// } else {
		// cmdLine = convertEnvVarsToWindows(cmdLine);
		// cmdLine = convertEnvVarsToUnix(cmdLine);
		// }
		LOG.log(Level.FINE, "Environment variables sanitized: " + cmdLine);

		// ArgumentListBuilder args = new ArgumentListBuilder();
		// if (cmdLine != null) {
		// args.addTokenized((launcher.isUnix() && executeFromWorkingDir) ? "./"
		// + cmdLine : cmdLine);
		// LOG.log(Level.FINE, "Execute from working directory: " +
		// args.toStringWithQuote());
		// }
		//
		// if (!launcher.isUnix()) {
		// args = args.toWindowsCommand();
		// LOG.log(Level.FINE, "Windows command: " + args.toStringWithQuote());
		// }

		try {

			listener.getLogger().println("cmdLine:" + cmdLine);
			ProcStarter procBuilder = launcher.decorateFor(build.getBuiltOn())
					.launch();


			Object cmd = new CmdRunner(!launcher.isUnix()).parseCmd(cmdLine, true);
			listener.getLogger().println("cmdList:" + cmd);
			if (cmd instanceof List) {
				procBuilder.cmds((List) cmd);
			} else {
				procBuilder.cmds((String) cmd);
			}

			// launch.cmdAsSingleString(cmdLine);
			final int result = procBuilder.envs(env).stdout(listener)
					.pwd(build.getWorkspace()).join();
			return result == 0;
		} catch (final IOException e) {
			Util.displayIOException(e, listener);
			final String errorMessage = Messages.CmdRunner_ExecFailed();
			e.printStackTrace(listener.fatalError(errorMessage));
			return false;
		}
	}

}
