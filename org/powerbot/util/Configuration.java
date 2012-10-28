package org.powerbot.util;

import org.powerbot.util.io.IOHelper;
import org.powerbot.util.io.Resources;

import java.io.File;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.zip.Adler32;

/**
 * @author Paris
 */
public class Configuration {
    public static final String NAME = "RSBot";
    public static final boolean FROMJAR;
    public static boolean DEVMODE = true;
    public static final boolean SUPERDEV, MULTIPROCESS = true;
    public static final int VERSION = 4025;
    public static final OperatingSystem OS;

    public enum OperatingSystem {
        MAC, WINDOWS, LINUX, UNKNOWN
    }

    public interface URLs {
        public static final String DOMAIN = "powerbot.org";
        public static final String CONTROL = "http://links." + DOMAIN + "/control";

        public static final String GAME = "runescape.com";
    }

    static {
        FROMJAR = Configuration.class.getClassLoader().getResource(Resources.Paths.ICON) != null;
        SUPERDEV = !Configuration.FROMJAR && new File(Resources.Paths.SERVER).exists();

        final String os = System.getProperty("os.name");
        if (os.contains("Mac")) {
            OS = OperatingSystem.MAC;
        } else if (os.contains("Windows")) {
            OS = OperatingSystem.WINDOWS;
        } else if (os.contains("Linux")) {
            OS = OperatingSystem.LINUX;
        } else {
            OS = OperatingSystem.UNKNOWN;
        }
    }

    public static boolean isServerOS() {
        // Windows Server
        if (System.getProperty("os.name").indexOf("erver") != -1) {
            return true;
        } else if (OS == OperatingSystem.LINUX) {
            File f;
            String s;

            // OpenVZ
            f = new File("/etc/sysconfig/network");
            if (f.exists() && f.canRead()) {
                s = IOHelper.readString(f);
                if (s != null && s.indexOf("venet") != -1) {
                    return true;
                }
            }

            // Linode
            try {
                s = IOHelper.readString(Runtime.getRuntime().exec("/bin/sh uname -r").getInputStream());
                if (s != null && s.indexOf("linode") != -1) {
                    return true;
                }
            } catch (final IOException ignored) {
            }

            // Xen
            try {
                s = IOHelper.readString(Runtime.getRuntime().exec("/bin/sh dmesg | egrep -i 'xen|front'| wc -l").getInputStream());
                if (s != null && !s.isEmpty() && !s.trim().equals("0")) {
                    return true;
                }
            } catch (final IOException ignored) {
            }
        }

        return false;
    }

    public static long getUID() {
        final Adler32 c = new Adler32();
        c.update(StringUtil.getBytesUtf8(Configuration.NAME));
        try {
            final Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                final byte[] a = e.nextElement().getHardwareAddress();
                if (a != null) {
                    c.update(a);
                }
            }
        } catch (final SocketException ignored) {
        }
        return c.getValue();
    }
}
