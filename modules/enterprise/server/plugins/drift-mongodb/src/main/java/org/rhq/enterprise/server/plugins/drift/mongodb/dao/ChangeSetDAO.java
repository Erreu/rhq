/*
 * RHQ Management Platform
 * Copyright (C) 2011 Red Hat, Inc.
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

package org.rhq.enterprise.server.plugins.drift.mongodb.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.google.code.morphia.Morphia;
import com.google.code.morphia.dao.BasicDAO;
import com.google.code.morphia.query.Query;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

import org.bson.types.ObjectId;

import org.rhq.core.domain.criteria.DriftChangeSetCriteria;
import org.rhq.core.domain.criteria.DriftCriteria;
import org.rhq.core.domain.util.PageOrdering;
import org.rhq.enterprise.server.plugins.drift.mongodb.entities.MongoDBChangeSet;
import org.rhq.enterprise.server.plugins.drift.mongodb.entities.MongoDBChangeSetEntry;

import static java.util.Arrays.asList;

public class ChangeSetDAO extends BasicDAO<MongoDBChangeSet, ObjectId> {

    private Morphia morphia;

    public ChangeSetDAO(Morphia morphia, Mongo mongo, String db) {
        super(mongo, morphia, db);
        this.morphia = morphia;
    }
    
    public List<MongoDBChangeSet> findByChangeSetCritiera(DriftChangeSetCriteria criteria) {
        Query<MongoDBChangeSet> query = createQuery();
        
        if (criteria.getFilterId() != null) {
            // There is no need to apply any additional filters if the id filter is specified
            return query.field("id").equal(new ObjectId(criteria.getFilterId())).asList();
        }
        
        if (criteria.getFilterResourceId() != null) {
            query.field("resourceId").equal(criteria.getFilterResourceId());
        }

        if (criteria.getFilterDriftDefinitionId() != null) {
            query.field("driftDefId").equal(criteria.getFilterDriftDefinitionId());
        }
        
        if (criteria.getFilterVersion() != null) {
            query.field("version").equal(Integer.parseInt(criteria.getFilterVersion()));
        }
        
        if (criteria.getFilterStartVersion() != null) {
            query.field("version").greaterThanOrEq(Integer.parseInt(criteria.getFilterStartVersion()));
        }
        
        if (criteria.getFilterEndVersion() != null) {
            query.field("version").lessThanOrEq(Integer.parseInt(criteria.getFilterEndVersion()));
        }

        if (criteria.getFilterCreatedAfter() != null) {
            query.field("ctime").greaterThanOrEq(criteria.getFilterCreatedAfter());
        }

        if (criteria.getFilterCreatedBefore() != null) {
            query.field("ctime").lessThan(criteria.getFilterCreatedBefore());
        }

        if (criteria.getFilterDriftPath() != null) {
            query.field("files.path").equal(Pattern.compile(".*" + criteria.getFilterDriftPath() + ".*"));
        }
        
        if (criteria.getFilterDriftDirectory() != null) {
            query.field("files.directory").equal(criteria.getFilterDriftDirectory());
        }

        if (criteria.getFilterCategory() != null) {
            query.field("category").equal(criteria.getFilterCategory());
        }
        
        if (criteria.getFilterDriftCategories() != null) {
            query.field("files.category").in(criteria.getFilterDriftCategories());
        }

        if (!criteria.isFetchDrifts()) {
            query.retrievedFields(false, "files");
        }

        if (criteria.getSortVersion() != null) {
            if (criteria.getSortVersion() == PageOrdering.ASC) {
                query.order("version");
            } else {
                query.order("-version");
            }
        }

        return query.asList();
    }

    public List<MongoDBChangeSet> findByDriftCriteria(DriftCriteria criteria) {
        Query<MongoDBChangeSet> query = createQuery();

        if (criteria.getFilterId() != null) {
            // TODO use the $slice operator
            // The slice operator will allow us to return only the requested entry in the
            // change set document. Morphia does not yet support the $slice operator so
            // we will hae to drop down to the mongo driver to issue a query.
            String[] ids = criteria.getFilterId().split(":");
            ObjectId changeSetId = new ObjectId(ids[0]);
            query.field("id").equal(changeSetId);

            // If the id filter is set, there is no need to process other filters since the id
            // filter uniquely identifies both the change set and the drift entry.
            return query.asList();
        }

        if (criteria.getFilterChangeSetId() != null && !criteria.getFilterChangeSetId().isEmpty()) {
            query.field("id").equal(new ObjectId(criteria.getFilterChangeSetId()));
            return query.asList();
        }

        if (criteria.getFilterResourceIds() != null && criteria.getFilterResourceIds().length > 0) {
            query.field("resourceId").in(asList(criteria.getFilterResourceIds()));
        }

        if (criteria.getFilterCategories() != null && criteria.getFilterCategories().length > 0) {
            query.field("files.category").in(asList(criteria.getFilterCategories()));
        }

        if (criteria.getFilterStartTime() != null) {
            query.field("files.ctime").greaterThanOrEq(criteria.getFilterStartTime());
        }

        if (criteria.getFilterEndTime() != null) {
            query.field("files.ctime").lessThanOrEq(criteria.getFilterEndTime());
        }

        if (criteria.getFilterPath() != null && !criteria.getFilterPath().isEmpty()) {
            query.field("files.path").equal(criteria.getFilterPath());
        }

        return query.asList();
    }

    public List<MongoDBChangeSetEntry> findEntries(DriftCriteria criteria) {
        // TODO Add support for driftHandlingModes filter

        if (criteria.getFilterId() != null) {
            String[] ids = criteria.getFilterId().split(":");
            ObjectId changeSetId = new ObjectId(ids[0]);
            return asList(findEntryById(changeSetId, ids[1]));
        }

        Query<MongoDBChangeSet> query = createQuery();
        boolean changeSetsFiltered = false;
        boolean entriesFiltered = false;

        ChangeSetEntryFilters filters = new ChangeSetEntryFilters();
        
        if (criteria.getFilterChangeSetId() != null) {
            query.field("id").equal(new ObjectId(criteria.getFilterChangeSetId()));
            changeSetsFiltered = true;
        }

        if (criteria.getFilterResourceIds().length > 0) {
            query.field("resourceId").in(asList(criteria.getFilterResourceIds()));
            changeSetsFiltered = true;
        }
        
        if (criteria.getFilterDriftDefinitionId() != null) {
            query.field("driftDefId").equal(criteria.getFilterDriftDefinitionId());
            changeSetsFiltered = true;
        }

        if (criteria.getFilterChangeSetStartVersion() != null) {
            query.field("version").greaterThanOrEq(criteria.getFilterChangeSetStartVersion());
            changeSetsFiltered = true;
        }

        if (criteria.getFilterChangeSetEndVersion() != null) {
            query.field("version").lessThanOrEq(criteria.getFilterChangeSetEndVersion());
            changeSetsFiltered = true;
        }

        if (criteria.getFilterCategories().length > 0) {
            query.field("files.category").in(asList(criteria.getFilterCategories()));
            entriesFiltered = true;
            filters.add(new CategoryFilter(criteria.getFilterCategories()));
        }

        if (criteria.getFilterEndTime() != null) {
            query.field("files.ctime").lessThanOrEq(criteria.getFilterEndTime());
            entriesFiltered = true;
            filters.add(new CreatedBeforeFilter(criteria.getFilterEndTime()));
        }

        if (criteria.getFilterStartTime() != null) {
            query.field("files.ctime").greaterThanOrEq(criteria.getFilterStartTime());
            entriesFiltered = true;
            filters.add(new CreatedAfterFilter(criteria.getFilterStartTime()));
        }

        if (criteria.getFilterPath() != null && !criteria.getFilterPath().isEmpty()) {
            query.field("files.path").equal(criteria.getFilterPath());
            entriesFiltered = true;
            filters.add(new PathFilter(criteria.getFilterPath()));
        }
        
        if (criteria.getFilterDirectory() != null) {
            query.field("files.directory").equal(criteria.getFilterDirectory());
            entriesFiltered = true;
            filters.add(new DirectoryFilter(criteria.getFilterDirectory()));
        }

        List<MongoDBChangeSetEntry> entries = new ArrayList<MongoDBChangeSetEntry>();

        if (changeSetsFiltered && !entriesFiltered) {
            // If the query only filters on change set fields, we do not need to do any
            // additional filtering since it was already done by the database.
            for (MongoDBChangeSet c : query.asList()) {
                entries.addAll(c.getDrifts());
            }
        } else {
            for (MongoDBChangeSet c : query.asList()) {
                for (MongoDBChangeSetEntry e : c.getDrifts()) {
                    if (filters.matchesAll(e)) {
                        entries.add(e);
                    }
                }
            }
        }

        return entries;
    }

    public MongoDBChangeSetEntry findEntryById(ObjectId changeSetId, String entryId) {
        BasicDBObject query = new BasicDBObject();
        query.put("_id", changeSetId);

        BasicDBObject keys = new BasicDBObject();
        keys.put("files", new BasicDBObject().append("$slice", new Integer[] {Integer.valueOf(entryId), 1}));

        DBObject result = getCollection().findOne(query, keys);

        if (result == null) {
            return null;
        }

        MongoDBChangeSet changeSet = morphia.fromDBObject(MongoDBChangeSet.class, result);
        if (!changeSet.getDrifts().isEmpty()) {
            return changeSet.getDrifts().iterator().next();
        }
        return null;
    }
    
    public void deleteChangeSets(int resourceId, String driftDefName) {
        deleteByQuery(createQuery()
                .field("resourceId").equal(resourceId)
                .field("driftDefName").equal(driftDefName));
    }

}
