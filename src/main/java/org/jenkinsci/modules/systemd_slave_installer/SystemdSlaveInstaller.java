package org.jenkinsci.modules.systemd_slave_installer;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.TaskListener;
import hudson.os.SU;
import hudson.util.StreamTaskListener;
import java.io.File;
import java.io.IOException;
import jenkins.security.NotReallyRoleSensitiveCallable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.modules.slave_installer.AbstractUnixSlaveInstaller;
import org.jenkinsci.modules.slave_installer.InstallationException;
import org.jenkinsci.modules.slave_installer.LaunchConfiguration;
import org.jenkinsci.modules.slave_installer.Prompter;
import org.jvnet.localizer.Localizable;

/**
 * Performs slave installation via systemd.
 *
 * @author Kohsuke Kawaguchi
 */
public class SystemdSlaveInstaller extends AbstractUnixSlaveInstaller {
    private final String instanceId;

    public SystemdSlaveInstaller(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public Localizable getConfirmationText() {
        return Messages._SystemdSlaveInstaller_ConfirmationText();
    }

    @SuppressFBWarnings(value = "DM_EXIT", justification = "yup")
    @Override
    public void install(LaunchConfiguration params, Prompter prompter) throws InstallationException, IOException, InterruptedException {
        final File srcSlaveJar = params.getJarFile();
        final String args = params.buildRunnerArguments().toStringWithQuote();
        final File rootDir = params.getStorage().getAbsoluteFile();
        final StreamTaskListener listener = StreamTaskListener.fromStdout();
        final String java = System.getProperty("java.home") + "/bin/java";
        final String userName = getCurrentUnixUserName();

        String rootUser = prompter.prompt("Specify the super user name to 'sudo' to","root");
        String rootPassword = prompter.promptPassword("Specify your password for sudo (or empty if you can sudo without password)");

        SU.execute(listener, rootUser, rootPassword, new Install(instanceId, rootDir, srcSlaveJar, userName, java, args, listener));

        System.exit(0);
    }

    private static class Install extends NotReallyRoleSensitiveCallable<Void, IOException> {
        private final String instanceId;
        private final File rootDir;
        private final File srcSlaveJar;
        private final String userName;
        private final String java;
        private final String args;
        private final TaskListener listener;
        Install(String instanceId, File rootDir, File srcSlaveJar, String userName, String java, String args, TaskListener listener) {
            this.instanceId = instanceId;
            this.rootDir = rootDir;
            this.srcSlaveJar = srcSlaveJar;
            this.userName = userName;
            this.java = java;
            this.args = args;
            this.listener = listener;
        }
        @Override
        public Void call() throws IOException {
            try {
                File slaveJar = new File(rootDir, "slave.jar");
                FileUtils.copyFile(srcSlaveJar, slaveJar);

                String conf = IOUtils.toString(getClass().getResourceAsStream("jenkins-slave.service"));
                conf = conf
                    .replace("{username}", userName)
                    .replace("{java}", java)
                    .replace("{jar}", slaveJar.getAbsolutePath())
                    .replace("{args}", args);

                final String name = "jenkins-slave-" + instanceId;  // service name
                FileUtils.writeStringToFile(new File("/etc/systemd/system/" + name + ".service"), conf);

                Process p = new ProcessBuilder("systemctl", "start", name).redirectErrorStream(true).start();
                p.getOutputStream().close();
                IOUtils.copy(p.getInputStream(), listener.getLogger());

                int r = p.waitFor();
                if (r != 0) { // error, but too late to recover
                    throw new IOException("Failed to launch  a service: " + r);
                }

                return null;
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }
    }
}
