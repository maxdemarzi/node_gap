# Node Gap

Find the gaps in the data

Based on http://stackoverflow.com/questions/37505590/cypher-pattern-matching/


Instructions
------------

1. Build it:

        mvn clean package

2. Copy target/node_gap-1.0-SNAPSHOT.jar to the plugins/ directory of your Neo4j server.

3. Configure Neo4j by adding a line to conf/neo4j-server.properties:

        org.neo4j.server.thirdparty_jaxrs_classes=com.maxdemarzi=/v1

4. Download and copy additional jar to the plugins/ directory of your Neo4j server.

        wget http://central.maven.org/maven2/org/roaringbitmap/RoaringBitmap/0.6.18/RoaringBitmap-0.6.18.jar

5. Start Neo4j server.

6. Check that it is installed correctly over HTTP:

        :GET /v1/service/helloworld

7. Warm up the database (optional)

        :GET /v1/service/warmup

8. Run it:

        :GET /v1/service/gaps

Results should look like this:

        [
        {Points=[{id=b11}, {id=b12}, {id=b13}, {id=b14}], Start Road={id=a1}, End Road={id=a2}},
        {Points=[{id=b11}, {id=b21}, {id=b22}, {id=b23}, {id=b24}, {id=b14}], Start Road={id=a1}, End Road={id=a2}},
        {Points=[{id=b31}, {id=b41}, {id=b42}], Start Road={id=a3}, End Road={id=a4}}
        ]
