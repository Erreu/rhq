/*
 * RHQ Management Platform
 * Copyright (C) 2005-2010 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2, as
 * published by the Free Software Foundation, and/or the GNU Lesser
 * General Public License, version 2.1, also as published by the Free
 * Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License and the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * and the GNU Lesser General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.rhq.coregui.client.bundle.tree;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.widgets.grid.events.DataArrivedEvent;
import com.smartgwt.client.widgets.grid.events.DataArrivedHandler;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeNode;
import com.smartgwt.client.widgets.tree.events.NodeClickEvent;
import com.smartgwt.client.widgets.tree.events.NodeClickHandler;

import org.rhq.coregui.client.CoreGUI;
import org.rhq.coregui.client.ViewId;
import org.rhq.coregui.client.ViewPath;

/**
 * @author Greg Hinkle
 * @author John Mazzitelli
 */
public class BundleTreeView extends TreeGrid {

    // We may need to wait for tree data to be fetched before we can complete processing the selected path.
    // If so, hold it here and retry after the datasource pulls the data.
    private ViewPath pendingPath = null;

    private Integer currentlySelectedBundleGroupId;

    public BundleTreeView() {
        super();
        setWidth100();
        setHeight100();
        setLeaveScrollbarGap(false);
        // fetch the top bundle group nodes at the inital onDraw()
        setAutoFetchData(true);
        setAnimateFolders(false);
        setSelectionType(SelectionStyle.SINGLE);
        setShowRollOver(false);
        setSortField(BundleTreeDataSource.FIELD_SORT_VALUE);
        setSortDirection(SortDirection.ASCENDING);
        setShowHeader(false);

        setDataSource(new BundleTreeDataSource());

        addNodeClickHandler(new NodeClickHandler() {
            public void onNodeClick(NodeClickEvent event) {
                TreeNode node = event.getNode();
                String path = node.getAttribute("id").replaceAll("_", "/");

                // the node ID is the path the form <bundleGroupId>_<the rest of the node ID>
                String[] splitPath = path.split("/", 2);

                try {
                    currentlySelectedBundleGroupId = Integer.parseInt(splitPath[0]);
                } catch (NumberFormatException e) {
                    CoreGUI.getErrorHandler().handleError("Cannot determine the selected bundle group: " + path);
                }

                if (splitPath.length == 1) {
                    CoreGUI.goToView("Bundles/BundleGroup/" + currentlySelectedBundleGroupId);
                } else {
                    CoreGUI.goToView("Bundles/Bundle/" + splitPath[1]);
                }
            }
        });
    }

    @Override
    protected void onInit() {
        super.onInit();

        // We may need to wait for tree data to be fetched before we can complete processing the selected path.
        // When the datasource pulls data keep processing the selectedPath if necessary.
        this.addDataArrivedHandler(new DataArrivedHandler() {

            public void onDataArrived(DataArrivedEvent dataArrivedEvent) {
                if (null != pendingPath) {
                    selectPath(pendingPath);
                }
            }
        });
    }

    public void selectPath(ViewPath viewPath) {
        Tree theTree = getTree();

        if (viewPath.viewsLeft() > 0) {
            String key = "";
            for (ViewId view : viewPath.getViewPath().subList(2, viewPath.getViewPath().size())) {
                if (key.length() > 0) {
                    key += "_";
                } else {
                    key = String.valueOf(currentlySelectedBundleGroupId) + '_'; // all keys start with the parent group ID
                }

                key += view.getPath();

                TreeNode node = theTree.findById(key);

                // special case code to handle a "deployments" path. the path structure does not mirror the
                // tree structure, so we may have to manually force the "destinations" folder to open.
                if (node == null) {
                    if (key.endsWith("deployments")) {
                        String tempKey = key.replace("deployments", "destinations");
                        node = theTree.findById(tempKey);
                    }
                }

                if (node != null) {
                    // open the node, this will force a fetch of child data if necessary
                    theTree.openFolder(node);

                    // special case code to handle a "deployments" path. the path structure does not mirror the
                    // tree structure, so we may have to manually force the "destinations" folder to open, and
                    // then its children (deployment nodes)
                    if (key.endsWith("deployments")) {
                        theTree.openFolders(theTree.getChildren(node));
                    }
                } else {
                    // wait for data to get loaded...
                    pendingPath = new ViewPath(viewPath.toString());
                    return;
                }
            }

            // we found the node, so keep going
            pendingPath = null;

            final String finalKey = key;
            GWT.runAsync(new RunAsyncCallback() {
                public void onFailure(Throwable reason) {
                }

                public void onSuccess() {
                    TreeNode node = getTree().findById(finalKey);
                    if (node != null) {
                        deselectAllRecords();
                        selectRecord(node);
                    }
                }
            });
        } else {
            deselectAllRecords();
        }
    }

    public void refresh() {
        invalidateCache();
    }

}
