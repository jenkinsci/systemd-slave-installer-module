package org.jenkinsci.modules.systemd_slave_installer;

import hudson.Extension;
import hudson.Util;
import hudson.remoting.Channel;
import org.apache.commons.codec.binary.Base64;
import org.jenkinsci.main.modules.instance_identity.InstanceIdentity;
import org.jenkinsci.modules.slave_installer.SlaveInstaller;
import org.jenkinsci.modules.slave_installer.SlaveInstallerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.security.MasterToSlaveCallable;

/**
 * {@link SlaveInstallerFactory} for systemd.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class SlaveInstallerFactoryImpl extends SlaveInstallerFactory {
    @Inject
    InstanceIdentity id;

    @Override
    public SlaveInstaller createIfApplicable(Channel c) throws IOException, InterruptedException {
        if (c.call(new HasSystemd())) {
            // TODO instance-identity 2.3 use getEncodedPublicKey
            RSAPublicKey key = id.getPublic();
            String instanceId = Util.getDigestOf(new String(Base64.encodeBase64(key.getEncoded()), StandardCharsets.UTF_8)).substring(0,8);
            return new SystemdSlaveInstaller(instanceId);
        }
        return null;
    }

    private static class HasSystemd extends MasterToSlaveCallable<Boolean, IOException> {
        @Override
        public Boolean call() throws IOException {
            try {
                if (!new File("/etc/systemd/system").isDirectory())
                    return false;   // this is where we write service files

                // make sure systemd is actually running
                Process p = new ProcessBuilder("systemctl", "list-units").redirectErrorStream(true).start();
                p.getOutputStream().close();
                drain(p.getInputStream());
                return p.waitFor()==0;
            } catch (IOException e) {
                // if systemctl is not present it fails with "No such file or directory",
                // so this is to be expected. Leaving this in the log anyway just in case we want to understand why
                LOGGER.log(Level.FINE, "doesn't look like you have systemd but here is the details",e);
                return false;
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }

        private void drain(InputStream in) throws IOException {
            byte[] buf = new byte[4096];
            while (true) {
                int len = in.read(buf);
                if (len<0)  break;
            }
            in.close();
        }

        private static final long serialVersionUID = 1L;

        private static final Logger LOGGER = Logger.getLogger(HasSystemd.class.getName());
    }
}
