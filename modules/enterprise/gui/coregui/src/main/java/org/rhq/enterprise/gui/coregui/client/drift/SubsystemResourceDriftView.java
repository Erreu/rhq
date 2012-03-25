/*
 * RHQ Management Platform
 * Copyright (C) 2011 Red Hat, Inc.
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
package org.rhq.enterprise.gui.coregui.client.drift;

import com.smartgwt.client.widgets.grid.ListGridRecord;
import org.rhq.core.domain.common.EntityContext;
import org.rhq.enterprise.gui.coregui.client.components.ExportModalWindow;
import org.rhq.enterprise.gui.coregui.client.components.table.TableAction;

public class SubsystemResourceDriftView extends DriftHistoryView {
    public SubsystemResourceDriftView(String locatorId, boolean hasWriteAccess) {
        super(locatorId, MSG.common_title_recent_drifts(), EntityContext.forSubsystemView(), hasWriteAccess);
    }

    @Override
    protected void configureTable() {
        super.configureTable();
        addExportAction();
    }

    private void addExportAction() {
        addTableAction("Export", "Export", new TableAction() {
            @Override
            public boolean isEnabled(ListGridRecord[] selection) {
                return true;
            }

            @Override
            public void executeAction(ListGridRecord[] selection, Object actionValue) {
                ExportModalWindow exportModalWindow = ExportModalWindow.createStandardExportWindow("recentDrift");
                exportModalWindow.show();
                refreshTableInfo();
            }

        });

    }
}
