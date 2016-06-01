package com.maxdemarzi;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.BranchState;

import java.util.Collections;

public class ToExpander implements PathExpander {
    private boolean search;

    public ToExpander() {
        search = true;
    }

    @Override
    public Iterable<Relationship> expand(Path path, BranchState branchState) {
        if (search) {

            if (path.length() > 1 && path.lastRelationship().isType(RelationshipTypes.roadnamed)) {
                search = false;
                return Collections.emptyList();
            }

            if (path.endNode().hasRelationship(RelationshipTypes.roadnamed)) {
                return path.endNode().getRelationships(RelationshipTypes.roadnamed);
            }
            return path.endNode().getRelationships(
                    Direction.OUTGOING, RelationshipTypes.line);
        }
        return Collections.emptyList();
    }

    @Override
    public PathExpander reverse() {
        throw new UnsupportedOperationException();
    }
}
