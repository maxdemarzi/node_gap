package com.maxdemarzi;

import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;

public class GapsTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(MODEL_STATEMENT)
            .withExtension("/v1", Service.class);

    public static final String MODEL_STATEMENT =
            new StringBuilder()
                    .append("CREATE (a1:road {id: 'a1'})")
                    .append("CREATE (a2:road {id: 'a2'})")
                    .append("CREATE (a3:road {id: 'a3'})")
                    .append("CREATE (a4:road {id: 'a4'})")
                    .append("CREATE (b11:point {id: 'b11'})")
                    .append("CREATE (b12:point {id: 'b12'})")
                    .append("CREATE (b13:point {id: 'b13'})")
                    .append("CREATE (b14:point {id: 'b14'})")
                    .append("CREATE (b21:point {id: 'b21'})")
                    .append("CREATE (b22:point {id: 'b22'})")
                    .append("CREATE (b23:point {id: 'b23'})")
                    .append("CREATE (b24:point {id: 'b24'})")
                    .append("CREATE (b31:point {id: 'b31'})")
                    .append("CREATE (b41:point {id: 'b41'})")
                    .append("CREATE (b42:point {id: 'b42'})")

                    .append("CREATE (b11)-[:roadnamed]->(a1)")
                    .append("CREATE (b14)-[:roadnamed]->(a2)")
                    .append("CREATE (b31)-[:roadnamed]->(a3)")
                    .append("CREATE (b42)-[:roadnamed]->(a4)")

                    .append("CREATE (b11)-[:line]->(b12)")
                    .append("CREATE (b11)-[:line]->(b21)")
                    .append("CREATE (b12)-[:line]->(b13)")
                    .append("CREATE (b13)-[:line]->(b14)")
                    .append("CREATE (b14)-[:line]->(b31)")
                    .append("CREATE (b21)-[:line]->(b22)")
                    .append("CREATE (b22)-[:line]->(b23)")
                    .append("CREATE (b23)-[:line]->(b24)")
                    .append("CREATE (b24)-[:line]->(b14)")
                    .append("CREATE (b31)-[:line]->(b41)")
                    .append("CREATE (b41)-[:line]->(b42)")

                    .toString();

    @Test
    public void shouldRespondToGetGapsMethod() {
        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/service/gaps").toString());
        ArrayList actual = response.content();
        System.out.println(actual);
        assertTrue(actual.equals(expected));
    }

    private static final ArrayList<HashMap<String, Object>> expected = new ArrayList<HashMap<String, Object>>() {
        {
            add(new HashMap<String, Object>() {{
                put("Start Road", new HashMap<String, Object>() {{
                    put("id", "a1");
                }});
                put("End Road", new HashMap<String, Object>() {{
                    put("id", "a2");
                }});

                put("Points", new ArrayList<HashMap<String,Object>>() {{
                    add(new HashMap<String, Object>() {{ put("id", "b11"); }});
                    add(new HashMap<String, Object>() {{ put("id", "b12"); }});
                    add(new HashMap<String, Object>() {{ put("id", "b13"); }});
                    add(new HashMap<String, Object>() {{ put("id", "b14"); }});
                    }});
                }}
            );

            add(new HashMap<String, Object>() {{
                    put("Start Road", new HashMap<String, Object>() {{
                        put("id", "a1");
                    }});
                    put("End Road", new HashMap<String, Object>() {{
                        put("id", "a2");
                    }});

                    put("Points", new ArrayList<HashMap<String,Object>>() {{
                        add(new HashMap<String, Object>() {{ put("id", "b11"); }});
                        add(new HashMap<String, Object>() {{ put("id", "b21"); }});
                        add(new HashMap<String, Object>() {{ put("id", "b22"); }});
                        add(new HashMap<String, Object>() {{ put("id", "b23"); }});
                        add(new HashMap<String, Object>() {{ put("id", "b24"); }});
                        add(new HashMap<String, Object>() {{ put("id", "b14"); }});
                    }});
                }}
            );

            add(new HashMap<String, Object>() {{
                    put("Start Road", new HashMap<String, Object>() {{
                        put("id", "a3");
                    }});
                    put("End Road", new HashMap<String, Object>() {{
                        put("id", "a4");
                    }});

                    put("Points", new ArrayList<HashMap<String,Object>>() {{
                        add(new HashMap<String, Object>() {{ put("id", "b31"); }});
                        add(new HashMap<String, Object>() {{ put("id", "b41"); }});
                        add(new HashMap<String, Object>() {{ put("id", "b42"); }});
                    }});
                }}
            );

        }
    };
}
