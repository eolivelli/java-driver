/*
 * Copyright DataStax, Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.driver.api.core.graph.statement;

import com.datastax.dse.driver.api.core.DseSession;
import com.datastax.dse.driver.api.core.graph.GraphTestSupport;
import com.datastax.dse.driver.api.core.graph.SampleGraphScripts;
import com.datastax.dse.driver.api.core.graph.ScriptGraphStatement;
import com.datastax.dse.driver.api.testinfra.session.DseSessionRule;
import com.datastax.oss.driver.api.testinfra.DseRequirement;
import com.datastax.oss.driver.api.testinfra.ccm.CustomCcmRule;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

@DseRequirement(min = "6.0", description = "DSE 6.0 required for BatchGraphStatement.")
public class ClassicGraphTraversalBatchIT extends GraphTraversalBatchITBase {

  private static final CustomCcmRule CCM_RULE = GraphTestSupport.GRAPH_CCM_RULE_BUILDER.build();

  private static final DseSessionRule SESSION_RULE =
      GraphTestSupport.getClassicGraphSessionBuilder(CCM_RULE).build();

  @ClassRule
  public static final TestRule CHAIN = RuleChain.outerRule(CCM_RULE).around(SESSION_RULE);

  private final GraphTraversalSource g = EmptyGraph.instance().traversal();

  @BeforeClass
  public static void setupSchema() {
    SESSION_RULE
        .session()
        .execute(ScriptGraphStatement.newInstance(SampleGraphScripts.ALLOW_SCANS));
    SESSION_RULE
        .session()
        .execute(ScriptGraphStatement.newInstance(SampleGraphScripts.MAKE_NOT_STRICT));
  }

  @Override
  protected DseSession session() {
    return SESSION_RULE.session();
  }

  @Override
  protected boolean isGraphBinary() {
    return false;
  }

  @Override
  protected CustomCcmRule ccmRule() {
    return CCM_RULE;
  }

  @Override
  protected GraphTraversalSource graphTraversalSource() {
    return g;
  }
}