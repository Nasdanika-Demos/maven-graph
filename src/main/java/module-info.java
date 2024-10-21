import org.nasdanika.capability.CapabilityFactory;
import org.nasdanika.launcher.demo.ModuleGraphCommandFactory;
import org.nasdanika.launcher.demo.rules.DemoRuleSetCapabilityFactory;
import org.nasdanika.launcher.demo.rules.InspectYamlCommandFactory;
import org.nasdanika.launcher.demo.rules.ListInspectableRulesCommandFactory;
import org.nasdanika.launcher.demo.rules.ListRulesCommandFactory;
import org.nasdanika.launcher.demo.rules.inspectors.ReflectiveInspectorFactory;

module org.nasdanika.demos.maven.graph {
	
	requires org.nasdanika.models.echarts.graph;
	requires org.nasdanika.models.maven;
		
}