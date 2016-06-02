/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010-2016, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.richfaces.tests.qa.plugin.ensurer.browser.jenkins;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import org.richfaces.tests.qa.plugin.ensurer.browser.BrowserEnsurer;
import org.richfaces.tests.qa.plugin.properties.PropertiesProvider;
import org.richfaces.tests.qa.plugin.utils.JenkinsFirefoxConfiguration;
import org.richfaces.tests.qa.plugin.utils.Servant;
import org.richfaces.tests.qa.plugin.utils.Version;

import com.google.common.base.Strings;
import com.google.inject.Inject;

/**
 * @author <a href="mailto:jstefek@redhat.com">Jiri Stefek</a>
 */
public class JenkinsFirefoxEnsurer implements BrowserEnsurer {

    private static final String FIREFOX = "firefox";
    private static final String FIREFOX_EXE = "firefox.exe";
    private static final String FIREFOX_JENKINS_VERSION_MINIMAL = "firefoxJenkinsVersionMinimal";
    private static final String FIREFOX_JENKINS_VERSION_OPTIMAL = "firefoxJenkinsVersionOptimal";
    private static final String MAC_FIREFOX_SUBDIR = "Contents/MacOS/firefox";
    private final JenkinsFirefoxDirectoryFinder finder;
    private final PropertiesProvider pp;
    private final Servant servant;

    @Inject
    public JenkinsFirefoxEnsurer(PropertiesProvider pp, Servant servant, JenkinsFirefoxDirectoryFinder finder) {
        this.pp = pp;
        this.servant = servant;
        this.finder = finder;
    }

    @Override
    public void ensure() {
        File firefoxBin = getFirefoxBin();
        if (!firefoxBin.exists()) {
            throw new RuntimeException(MessageFormat.format("Firefox binary does not exist at <{0}>.", firefoxBin.getAbsolutePath()));
        }
        servant.setProjectProperty(pp.getFirefoxBinPropertyName(), firefoxBin);
    }

    protected File getFirefoxBaseDirectory() {
        String toolsDirectory = System.getenv("NATIVE_TOOLS");
        if (toolsDirectory != null && !toolsDirectory.isEmpty() && new File(toolsDirectory).exists()) {
            return new File(toolsDirectory);
        }
        throw new UnsupportedOperationException("Variable <NATIVE_TOOLS> was not specified!");
    }

    protected File getFirefoxBin() {
        File firefoxDir;
        if (pp.getBrowser().isUnknownVersion()) {
            pp.getLog().info("Firefox version not specified.");
            JenkinsFirefoxConfiguration config = isCurrentOsInSpecificConfigurations(pp.getJenkinsFirefoxConfigurations(), servant);
            if (config != null) {
                pp.getLog().info("Found specific configuration for this os/arch/version.");
                return handleSpecificConfiguration(config, servant);
            }

            pp.getLog().info(MessageFormat.format("Using the optimal or minimal version specified"
                + " with attributes <{0}>, <{1}>. Which are set to <{2}> and <{3}>.", FIREFOX_JENKINS_VERSION_OPTIMAL,
                FIREFOX_JENKINS_VERSION_MINIMAL,
                pp.getJenkinsFirefoxVersionOptimal(),
                pp.getJenkinsFirefoxVersionMinimal())
            );

            firefoxDir = finder.getOptimalOrMinimalVersion(getFirefoxBaseDirectory().listFiles(),
                pp.getJenkinsFirefoxVersionOptimal(), pp.getJenkinsFirefoxVersionMinimal());
        } else {
            pp.getLog().info(MessageFormat.format("Firefox version specified <{0}>.", pp.getBrowser().getVersion().getMajorMinorMicroSpecifierFormat()));
            firefoxDir = finder.getSpecificVersion(getFirefoxBaseDirectory().listFiles(), pp.getBrowser().getVersion());
        }
        return new File(firefoxDir, pp.isOnWindows() ? FIREFOX_EXE : pp.isOnLinux() || pp.isOnSolaris() ? FIREFOX : MAC_FIREFOX_SUBDIR);
    }

    protected File handleSpecificConfiguration(JenkinsFirefoxConfiguration config, Servant servant) {
        // binary has higher priority than version
        // handle binary
        String firefoxBin = config.getFirefoxBin();
        if (!Strings.isNullOrEmpty(firefoxBin)) {
            return new File(firefoxBin);
        }
        // handle version
        String firefoxVersion = config.getFirefoxVersion();
        if (!Strings.isNullOrEmpty(firefoxVersion)) {
            Version v = Version.parseVersion(firefoxVersion);
            if (!v.equals(Version.UNKNOWN_VERSION)) {
                return new File(finder.getSpecificVersion(getFirefoxBaseDirectory().listFiles(), v),
                    pp.isOnWindows() ? FIREFOX_EXE : FIREFOX);
            }
        }
        throw new RuntimeException(MessageFormat.format("Could not handle config <{0}>.", config));
    }

    protected JenkinsFirefoxConfiguration isCurrentOsInSpecificConfigurations(List<JenkinsFirefoxConfiguration> configs, Servant servant) {
        if (configs == null || configs.isEmpty()) {
            return null;
        }
        boolean check;
        for (JenkinsFirefoxConfiguration config : configs) {
            check = true;
            for (JenkinsBrowserProps browserProperty : JenkinsBrowserProps.values()) {
                check &= browserProperty.check(config, pp);
            }
            if (check) {
                return config;
            }
        }
        return null;
    }

    private enum JenkinsBrowserProps {

        version {
                @Override
                public boolean check(JenkinsFirefoxConfiguration c, PropertiesProvider pp) {
                    String currentValue = c.getOsVersion();
                    if (Strings.isNullOrEmpty(currentValue)) {
                        return Boolean.TRUE;
                    } else {
                        return pp.getOSVersion().toLowerCase().contains(currentValue.toLowerCase());
                    }
                }
            }, name {
                @Override
                public boolean check(JenkinsFirefoxConfiguration c, PropertiesProvider pp) {
                    String currentValue = c.getOsName();
                    if (Strings.isNullOrEmpty(currentValue)) {
                        return Boolean.TRUE;
                    } else {
                        return pp.getOSName().toLowerCase().contains(currentValue.toLowerCase());
                    }
                }
            }, arch {
                @Override
                public boolean check(JenkinsFirefoxConfiguration c, PropertiesProvider pp) {
                    String currentValue = c.getOsArch();
                    if (Strings.isNullOrEmpty(currentValue)) {
                        return Boolean.TRUE;
                    } else {
                        return pp.getOsArch().toLowerCase().contains(currentValue.toLowerCase());
                    }
                }
            };

        public abstract boolean check(JenkinsFirefoxConfiguration c, PropertiesProvider pp);
    }
}
