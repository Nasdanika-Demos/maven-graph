package org.nasdanika.launcher.demo;

import java.io.File;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Requires;
import java.nio.file.Files;
import java.util.Optional;

import org.icepear.echarts.charts.graph.GraphEdgeLineStyle;
import org.icepear.echarts.charts.graph.GraphEmphasis;
import org.icepear.echarts.charts.graph.GraphSeries;
import org.icepear.echarts.components.series.SeriesLabel;
import org.icepear.echarts.render.Engine;
import org.jgrapht.alg.drawing.FRLayoutAlgorithm2D;
import org.nasdanika.cli.CommandBase;
import org.nasdanika.cli.ParentCommands;
import org.nasdanika.cli.RootCommand;
import org.nasdanika.common.Context;
import org.nasdanika.models.echarts.graph.Graph;
import org.nasdanika.models.echarts.graph.GraphFactory;
import org.nasdanika.models.echarts.graph.Item;
import org.nasdanika.models.echarts.graph.Node;
import org.nasdanika.models.echarts.graph.util.GraphUtil;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
		description = "Generates module dependency graph",
		name = "module-graph",
		versionProvider = ModuleVersionProvider.class,
		mixinStandardHelpOptions = true)
@ParentCommands(RootCommand.class)
public class ModuleGraphCommand extends CommandBase {
	
	private static final String SINGLE_SUFFIX = ".*";
	private static final String DOUBLE_SUFFIX = ".**";
	
	@Parameters(description = "Output file")
	File output;	
		
	@Option(
			names = {"-e", "--exclude-modules"}, 
			description = {
					"Modules to exclude",
					"Supports .* and .** patterns"					
			})
	private String[] excludeModules;	
		
	@Option(
			names = {"-i", "--include-modules"}, 
			description = {
					"Modules to include",
					"Supports .* and .** patterns"					
			})
	private String[] includeModules;
	
	@Option(
			names = {"-t", "--template"}, 
			description = "HTML page template")
	private File template;	
		
	@Option(
			names = {"-w", "--width"}, 
			description = "Layout width, defaults to 2000")
	private double width = 2000;	
	
	@Option(
			names = {"-h", "--height"}, 
			description = "Layout height, defaults to 1500")
	private double height = 1600;	
	
	// TODO - categories map name to pattern list
		
	final static String GRAPH_TEMPLATE = 
			"""
			<html>
				<head>
				    <title>Module dependency</title>
				    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootswatch/4.5.2/cerulean/bootstrap.min.css" id="nsd-bootstrap-theme-stylesheet">
				    <meta charset="utf-8">
				    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
				    <script src="https://code.jquery.com/jquery-3.3.1.min.js"></script>
				    <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.1/dist/umd/popper.min.js"></script>
				    <script src="https://cdn.jsdelivr.net/npm/bootstrap@4.5.2/dist/js/bootstrap.min.js"></script>
				    <meta charset="utf-8">
				    <link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/Nasdanika/html@master/model/app.gen/web-resources/css/app.css">
				    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.1/css/all.min.css">
				    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/jstree@3.3.16/dist/themes/default/style.min.css">
				    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/github-markdown-css@5.5.0/github-markdown.min.css">
				    <link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/highlightjs/cdn-release@11.9.0/build/styles/default.min.css">
				    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-vue@2.23.0/dist/bootstrap-vue.css">
				    <script src="https://cdn.jsdelivr.net/gh/Nasdanika/html@master/model/app.gen/web-resources/js/common.js"></script>
				    <script src="https://cdn.jsdelivr.net/gh/Nasdanika/html@master/model/app.gen/web-resources/js/dark-head.js"></script>
				    <script src="https://cdn.jsdelivr.net/npm/jstree@3.3.16/dist/jstree.min.js"></script>
				    <script src="https://cdn.jsdelivr.net/gh/highlightjs/cdn-release@11.9.0/build/highlight.min.js"></script>
				    <script src="https://cdn.jsdelivr.net/npm/vue@2.7.16/dist/vue.min.js"></script>
				    <script src="https://cdn.jsdelivr.net/npm/bootstrap-vue@2.23.0/dist/bootstrap-vue.min.js"></script>
				    <script src="https://cdn.jsdelivr.net/gh/Nasdanika/html@master/model/app.gen/web-resources/js/components/table.js"></script>
				    <script src="https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js"></script>
				    <script src="https://cdn.jsdelivr.net/gh/Nasdanika-Models/ecore@master/graph/web-resources/components/table.js"></script>
				    <script src="https://cdnjs.cloudflare.com/ajax/libs/echarts/5.4.3/echarts.min.js"></script><!-- Global site tag (gtag.js) - Google Analytics -->
				</head>			
				<body>
					<div class="container-fluid">
						<div id="graph-container-${graphContainerId}" class="row" style="height:80vh;width:100%">
						</div>
					</div>
					<script type="text/javascript">
						$(document).ready(function() {
							var dom = document.getElementById("graph-container-${graphContainerId}");
							var myChart = echarts.init(dom, null, {
								render: "canvas",
								useDirtyRect: false
							});		
							var option = ${chart};
							option.tooltip = {};
							option.series[0].tooltip = {
								formatter: function(arg) { 
									return arg.value ? arg.value.description : null; 
								}
							};
							myChart.setOption(option);
							myChart.on("dblclick", function(params) {
								if (params.value) {
									if (params.value.link) {
										window.open(params.value.link, "_self");
									} else if (params.value.externalLink) {
										window.open(params.value.externalLink);
									}
								}
							});
							window.addEventListener("resize", myChart.resize);
						});		
					</script>
				</body>
			</html>
			""";	
			
	private Node moduleToNode(
			Module module, 
			ModuleLayer layer, 
			Graph graph,
			Item nsdCategory,
			Item eclipseCategory,
			Item javaCategory,
			Item otherCategory) {
		ModuleDescriptor moduleDescriptor = module.getDescriptor();		
		Node moduleNode = getModuleNode(module, layer, graph, nsdCategory, eclipseCategory, javaCategory, otherCategory);
		if (moduleNode != null) {
			for (Requires req: moduleDescriptor.requires()) {
				Optional<Module> rmo = layer.findModule(req.name());
				if (rmo.isPresent()) {
					Node reqNode = moduleToNode(rmo.get(), layer, graph, nsdCategory, eclipseCategory, javaCategory, otherCategory);
					if (reqNode != null) {
						org.nasdanika.models.echarts.graph.Link reqLink = GraphFactory.eINSTANCE.createLink();				
						reqLink.setTarget(reqNode);
						moduleNode.getOutgoingLinks().add(reqLink);
					}
				}
			}
		}
		return moduleNode;
	}
	
	private Node getModuleNode(
			Module module, 
			ModuleLayer layer, 
			Graph graph, 
			Item nsdCategory,
			Item eclipseCategory,
			Item javaCategory,
			Item otherCategory) {
		
		if (includeModules != null && !matchModule(module.getName(), includeModules)) {
			return null;
		}
		
		if (excludeModules != null && matchModule(module.getName(), excludeModules)) {
			return null;
		}
		
		for (Node n: graph.getNodes()) {
			if (n.getName().equals(module.getName())) {
				return n;
			}
		}
		Node ret = GraphFactory.eINSTANCE.createNode();
		ret.setName(module.getName());
		
		if (ret.getName().startsWith("org.nasdanika.")) {
			ret.setCategory(nsdCategory);
		} else if (ret.getName().startsWith("org.eclipse.")) {
			ret.setCategory(eclipseCategory);
		} else if (ret.getName().startsWith("java.")) {
			ret.setCategory(javaCategory);
		} else {
			// TODO - categories from options
			ret.setCategory(otherCategory);
		}
		
		ret.getSymbolSize().add(10.0 + Math.log(1 + module.getDescriptor().exports().size()));
		
		graph.getNodes().add(ret);
		return ret;
	}
		
	/**
	 * Uses JGraphT {@link FRLayoutAlgorithm2D} to force layout the graph.
	 * @param graph
	 */
	protected void forceLayout(Graph graph) {
		GraphUtil.forceLayout(graph, 2000, 1500);		
	}
		
	protected boolean matchModule(String moduleName, String[] patterns) {
		if (patterns != null) {
			for (String pattern: patterns) {
				if (moduleName.equals(pattern)) {
					return true;
				}
				
				if (pattern.endsWith(SINGLE_SUFFIX)) {
					String prefix = pattern.substring(0, pattern.length() - 1);
					if (moduleName.startsWith(prefix) && moduleName.indexOf('.', prefix.length()) == -1) {
						return true;
					}
				}
							
				if (pattern.endsWith(DOUBLE_SUFFIX)) {
					String prefix = pattern.substring(0, pattern.length() - DOUBLE_SUFFIX.length());
					if (moduleName.equals(prefix) || moduleName.startsWith(prefix + ".")) {
						return true;
					}
				}				
			}
		}
		
		return false;
	}

	@Override
	public Integer call() throws Exception {
		Module thisModule = getClass().getModule();
		ModuleLayer moduleLayer = thisModule.getLayer();
		
		Graph graph = GraphFactory.eINSTANCE.createGraph();
		
		Item nsdCategory = GraphFactory.eINSTANCE.createItem();
		nsdCategory.setName("Nasdanika");
		graph.getCategories().add(nsdCategory);
		
		Item eclipseCategory = GraphFactory.eINSTANCE.createItem();
		eclipseCategory.setName("Eclipse");
		graph.getCategories().add(eclipseCategory);
		
		Item javaCategory = GraphFactory.eINSTANCE.createItem();
		javaCategory.setName("Java");
		graph.getCategories().add(javaCategory);
		
		Item otherCategory = GraphFactory.eINSTANCE.createItem();
		otherCategory.setName("Other");
		graph.getCategories().add(otherCategory);
		
		moduleToNode(
				thisModule, 
				moduleLayer, 
				graph, 
				nsdCategory, 
				eclipseCategory, 
				javaCategory, 
				otherCategory);
		
		GraphUtil.forceLayout(graph, width, height);
		GraphSeries graphSeries = new org.icepear.echarts.charts.graph.GraphSeries()
			.setSymbolSize(24)
			.setDraggable(true)				
			.setLayout("none")
	        .setLabel(new SeriesLabel().setShow(true).setPosition("right"))
	        .setLineStyle(new GraphEdgeLineStyle().setColor("source").setCurveness(0))
	        .setRoam(true)
	        .setEdgeSymbol(new String[] { "none", "arrow" })
	        .setEmphasis(new GraphEmphasis().setFocus("adjacency")); // Line style width 10?
				
		graph.configureGraphSeries(graphSeries);
		
    	org.icepear.echarts.Graph echartsGraph = new org.icepear.echarts.Graph()
                .setTitle("Module Dependencies")
                .setLegend()
                .addSeries(graphSeries);
    	
	    Engine engine = new Engine();
	    String chartJSON = engine.renderJsonOption(echartsGraph);
	    
	    String templateStr;
	    if (template == null) {
	    	templateStr = GRAPH_TEMPLATE;
	    } else {
	    	templateStr = Files.readString(template.toPath());
	    }	    
	    
		String chartHTML = Context
				.singleton("chart", chartJSON)
				.compose(Context.singleton("graphContainerId", "graph-container"))
				.interpolateToString(templateStr);
	    
	    Files.writeString(output.toPath(), chartHTML);
	    return 0;
	}

}
