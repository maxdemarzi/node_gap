package com.maxdemarzi;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.roaringbitmap.RoaringBitmap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Path("/service")
public class Service {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GET
    @Path("/helloworld")
    public Response helloWorld() throws IOException {
        Map<String, String> results = new HashMap<String,String>(){{
            put("hello","world");
        }};
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @GET
    @Path("/warmup")
    public Response warmUp(@Context GraphDatabaseService db) throws IOException {
        Map<String, String> results = new HashMap<String,String>(){{
            put("warmed","up");
        }};

        try (Transaction tx = db.beginTx()) {
            for (Node n : db.getAllNodes()) {
                n.getPropertyKeys();
                for (Relationship relationship : n.getRelationships()) {
                    relationship.getPropertyKeys();
                    relationship.getStartNode();
                }
            }

            for (Relationship relationship : db.getAllRelationships()) {
                relationship.getPropertyKeys();
                relationship.getNodes();
            }
        }

        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @GET
    @Path("/gaps")
    public Response gaps(@Context GraphDatabaseService db) throws IOException {

        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException
            {
                JsonGenerator jg = objectMapper.getJsonFactory().createJsonGenerator( os, JsonEncoding.UTF8 );
                jg.writeStartArray();

                RoaringBitmap Roads = new RoaringBitmap();
                RoaringBitmap UnAttachedPoints = new RoaringBitmap();
                RoaringBitmap IncludedPoints = new RoaringBitmap();

                try (Transaction tx = db.beginTx()) {
                    for (Node n : db.getAllNodes()) {
                        int node_id = ((Number) n.getId()).intValue();
                        if (n.hasLabel(Labels.road)) {
                            Roads.add(node_id);
                        } else {
                            if (!n.hasRelationship(Direction.OUTGOING, RelationshipTypes.roadnamed)){
                                UnAttachedPoints.add(node_id);
                            }
                        }
                    }
                    tx.success();
                }

                try (Transaction tx = db.beginTx()) {

                    FromExpander fromExpander = new FromExpander();
                    ToExpander toExpander = new ToExpander();

                    TraversalDescription fromRoad = db.traversalDescription()
                            .breadthFirst()
                            .expand(fromExpander)
                            .uniqueness(Uniqueness.NODE_GLOBAL)
                            .evaluator(Evaluators.includeWhereLastRelationshipTypeIs(RelationshipTypes.roadnamed));

                    TraversalDescription toRoad = db.traversalDescription()
                            .breadthFirst()
                            .expand(toExpander)
                            .uniqueness(Uniqueness.NODE_GLOBAL)
                            .evaluator(Evaluators.includeWhereLastRelationshipTypeIs(RelationshipTypes.roadnamed));

                    UnAttachedPoints.forEach((ThrowingConsumer) value -> {

                        // If we didn't already find you via a different way
                        if (!IncludedPoints.contains(value)) {
                            fromExpander.search = true;
                            toExpander.search = true;

                            jg.writeStartObject();

                            ArrayList<Map<String, Object>> points = new ArrayList<>();

                            Node point = db.getNodeById(value);

                            // Let's find the nearest Road in an Incoming Direction
                            for (org.neo4j.graphdb.Path position : fromRoad.traverse(point)) {
                                jg.writeObjectField("Start Road", position.endNode().getAllProperties());

                                position.reverseNodes().forEach(node -> {
                                    // Skip the roads, we just want points
                                    if (!Roads.contains(((Number) node.getId()).intValue())) {
                                        points.add(node.getAllProperties());
                                        IncludedPoints.add(((Number) node.getId()).intValue());
                                    }

                                });
                            }
                            // Let's find the nearest Road in an Outgoing Direction
                            for (org.neo4j.graphdb.Path position : toRoad.traverse(point)) {
                                jg.writeObjectField("End Road", position.endNode().getAllProperties());
                                position.nodes().forEach(node -> {
                                    // Skip the point since we already have it
                                    if (!node.equals(point)) {
                                        if (!Roads.contains(((Number) node.getId()).intValue())) {
                                            points.add(node.getAllProperties());
                                            IncludedPoints.add(((Number) node.getId()).intValue());
                                        }
                                    }
                                });
                            }

                            jg.writeObjectField("Points", points);
                            jg.writeEndObject();

                        }
                    });

                    tx.success();
                }

            jg.writeEndArray();
            jg.flush();
            jg.close();
        }
    };

    return Response.ok().entity(stream).type(MediaType.APPLICATION_JSON).build();
    }

}
