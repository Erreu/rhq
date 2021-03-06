/*
 * RHQ Management Platform
 * Copyright (C) 2005-2010 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.rhq.core.pluginapi.bundle;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

import org.rhq.core.domain.bundle.BundleDestination;
import org.rhq.core.domain.bundle.BundleResourceDeployment;
import org.rhq.core.domain.content.PackageVersion;

/**
 * A request to deploy a bundle.
 *
 * @since 3.0
 * @author John Mazzitelli
 */
public class BundleDeployRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private BundleResourceDeployment resourceDeployment;
    private BundleManagerProvider bundleManagerProvider;
    private File bundleFilesLocation;
    private Map<PackageVersion, File> packageVersionFiles;
    private boolean isCleanDeployment = false;
    private boolean isRevert = false;
    private File absDestDir;

    public BundleDeployRequest() {
    }

    /**
     * Returns the full, absolute directory as found on the local machine's file system
     * where the bundle should be deployed. This is the bundle destination's
     * {@link BundleDestination#getDeployDir() relative destination directory} under the
     * {@link BundleDestination#getDestinationBaseDirectoryName() destination base directory}.
     */
    public File getAbsoluteDestinationDirectory() {
        return this.absDestDir;
    }

    public void setAbsoluteDestinationDirectory(File absoluteDestDir) {
        this.absDestDir = absoluteDestDir;
    }

    /**
     * This returns the location where the plugin container has downloaded the bundle files.
     * 
     * @return the location where the bundle files have been downloaded
     */
    public File getBundleFilesLocation() {
        return this.bundleFilesLocation;
    }

    public void setBundleFilesLocation(File bundleFilesLocation) {
        this.bundleFilesLocation = bundleFilesLocation;
    }

    /**
     * Maps all the package versions associated with the bundle and the locations
     * where the package version contents can be found. These will all be located
     * somewhere under the {@link #getBundleFilesLocation() bundle files location}.
     * 
     * @return packages and their locations on the local file system
     */
    public Map<PackageVersion, File> getPackageVersionFiles() {
        return this.packageVersionFiles;
    }

    public void setPackageVersionFiles(Map<PackageVersion, File> packageVersionFiles) {
        this.packageVersionFiles = packageVersionFiles;
    }

    public BundleResourceDeployment getResourceDeployment() {
        return resourceDeployment;
    }

    public void setResourceDeployment(BundleResourceDeployment resourceDeployment) {
        this.resourceDeployment = resourceDeployment;
    }

    public BundleManagerProvider getBundleManagerProvider() {
        return this.bundleManagerProvider;
    }

    public void setBundleManagerProvider(BundleManagerProvider provider) {
        this.bundleManagerProvider = provider;
    }

    /**
     * @return flag to indicate if the deployment directory should have all of its files deleted
     *         prior to deploying the new files. All files in the deployment directory should be cleaned,
     *         including files/directories that are marked to be "ignored".
     */
    public boolean isCleanDeployment() {
        return isCleanDeployment;
    }

    public void setCleanDeployment(boolean isCleanDeployment) {
        this.isCleanDeployment = isCleanDeployment;
    }

    /**
     * @return flag to indicate if this bundle deployment request should revert a deployment back to
     *         a previous state. Reverting means that any files backed up from the last deployment
     *         are reverted to their original state.
     */
    public boolean isRevert() {
        return isRevert;
    }

    public void setRevert(boolean isRevert) {
        this.isRevert = isRevert;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(this.getClass() + ": ");
        str.append("deployment=[").append(resourceDeployment.toString()).append("], ");
        str.append("full-deploy-directory=[").append(absDestDir.toString()).append("], ");
        str.append("clean=[").append(isCleanDeployment).append("], ");
        str.append("revert=[").append(isRevert).append("]");
        return str.toString();
    }
}
