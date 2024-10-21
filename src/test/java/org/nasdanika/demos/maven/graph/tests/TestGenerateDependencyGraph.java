package org.nasdanika.demos.maven.graph.tests;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.icepear.echarts.charts.graph.GraphEdgeLineStyle;
import org.icepear.echarts.charts.graph.GraphEmphasis;
import org.icepear.echarts.charts.graph.GraphSeries;
import org.icepear.echarts.components.series.SeriesLabel;
import org.icepear.echarts.render.Engine;
import org.jgrapht.alg.drawing.FRLayoutAlgorithm2D;
import org.junit.jupiter.api.Test;
import org.nasdanika.common.Context;
import org.nasdanika.common.NasdanikaException;
import org.nasdanika.models.echarts.graph.Graph;
import org.nasdanika.models.echarts.graph.GraphFactory;
import org.nasdanika.models.echarts.graph.Item;
import org.nasdanika.models.echarts.graph.Node;
import org.nasdanika.models.echarts.graph.util.GraphUtil;
import org.nasdanika.models.maven.Coordinates;
import org.nasdanika.models.maven.Dependency;
import org.nasdanika.models.maven.MavenFactory;
import org.nasdanika.models.maven.Model;

public class TestGenerateDependencyGraph {
	
	enum Metric {
		
		REPO,
		MODULE,
		MAVEN,
		FILE,
		LINE_OF_CODE,
		SITE,
		HTML_PAGE
		
	}
	
	private record CoordinatesRecord(String groupId, String artifactId, String version) {
		
		public CoordinatesRecord(Coordinates coordinates) {
			this(coordinates.getGroupId(), coordinates.getArtifactId(), coordinates.getVersion());
		}
		
	}
		
	private final String[] GIT_REPOS = { "core", "html", "cli", "nasdanika.github.io", "retrieval-augmented-generation" };	
	private final String[] GIT_MODEL_REPOS = { 
		"echarts",
		"ecore",
		"ncore",
		"drawio",
		"exec",
		"graph",
		"git",
		"gitLab",
		"html",
		"bootstrap",
		"html-app",
		"excel",
		"party",
		"architecture",
		"family",
		"bank",
		"pdf",
		"coverage",
		"source-engineering",
		"java",
		"maven",
		"enterprise",
		"function-flow",
		"nature",
		"rules"
	};
	
	private final static String GRAPH_TEMPLATE = 
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
	
	/**
	 * Computes code stats - modules, source files, lines of code.
	 */
	@Test
	public void testGenerateDependencyGraph() {		
		Map<Metric, int[]> measurements = new TreeMap<>();
		BiConsumer<Metric, Integer> measurementConsumer = (metric, measurement) -> measurements.computeIfAbsent(metric, m -> new int[] { 0 })[0] += measurement;
		Map<CoordinatesRecord, Entry<File, Model>> models = new HashMap<>();
		BiConsumer<File, Model> mavenModelConsumer = (file, model) -> {
			models.put(new CoordinatesRecord(model), Map.entry(file, model));
		};
		for (String gitRepo: GIT_REPOS) {
			repoStats(new File("../../git/" + gitRepo), measurementConsumer, mavenModelConsumer);
		}
		for (String gitModelRepo: GIT_MODEL_REPOS) {
			repoStats(new File("../../git-models/" + gitModelRepo), measurementConsumer, mavenModelConsumer);
		}
			
		measurements.entrySet().forEach(e -> System.out.println(e.getKey() + " = " + e.getValue()[0]));
		
		Graph graph = GraphFactory.eINSTANCE.createGraph();
		
		Item coreCategory = GraphFactory.eINSTANCE.createItem();
		coreCategory.setName("Core");
		graph.getCategories().add(coreCategory);
		
		Item htmlCategory = GraphFactory.eINSTANCE.createItem();
		htmlCategory.setName("HTML");
		graph.getCategories().add(htmlCategory);
		
		Item modelsCategory = GraphFactory.eINSTANCE.createItem();
		modelsCategory.setName("Models");
		graph.getCategories().add(modelsCategory);
		
		Item otherCategory = GraphFactory.eINSTANCE.createItem();
		otherCategory.setName("Other");
		graph.getCategories().add(otherCategory);
		
		Function<Model, Integer> sizeComputer = model -> 0; 		
		
		Map<CoordinatesRecord, Node> nodeMap = new HashMap<>();
		Function<Model, Node> resolver	= model -> {
			return nodeMap.computeIfAbsent(new CoordinatesRecord(model), c -> createModelNode(model, sizeComputer, graph, coreCategory, htmlCategory, modelsCategory, otherCategory));	
		};
		
		for (Entry<CoordinatesRecord, Entry<File, Model>> me: models.entrySet()) {
			createAndLink(
					me.getValue().getValue(), 
					resolver,
					graph, 
					coreCategory, 
					htmlCategory, 
					modelsCategory, 
					otherCategory);
		}
		
		GraphUtil.forceLayout(graph, 2000, 1600);
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
	    
		String chartHTML = Context
				.singleton("chart", chartJSON)
				.compose(Context.singleton("graphContainerId", "graph-container"))
				.interpolateToString(GRAPH_TEMPLATE);
	    
	    Files.writeString(new File("docs/index.html").toPath(), chartHTML);
	}
	
	private void repoStats(
			File repo, 
			BiConsumer<Metric, Integer> measurementConsumer,
			BiConsumer<File, Model> mavenModelConsumer) {
		if (!repo.isDirectory()) {
			fail("Not a directory:" + repo.getAbsolutePath());
		}
		measurementConsumer.accept(Metric.REPO, 1);
		walk(
				null, 
				(file, path) -> repoFileStats(
						file, 
						path, 
						measurementConsumer,
						mavenModelConsumer), 
				repo.listFiles());
	}
	
	protected void repoFileStats(
			File file, 
			String path, 
			BiConsumer<Metric, Integer> measurementConsumer,
			BiConsumer<File, Model> mavenModelConsumer) {
		if (path.equals("src/main/java/module-info.java") || path.endsWith("/src/main/java/module-info.java")) {
			measurementConsumer.accept(Metric.MODULE, 1);
		}		
		if ((path.equals("pom.xml") || path.endsWith("/pom.xml")) && !path.contains("target/classes/META-INF/maven/")) {
			measurementConsumer.accept(Metric.MAVEN, 1);
			Model model = MavenFactory.eINSTANCE.createModel();
			try {
				model.load(file);
			} catch (Exception e) {
				throw new NasdanikaException(e);
			}
			mavenModelConsumer.accept(file, model);
		}		
		if ((path.startsWith("src/") || path.contains("/src/")) && file.isFile() && file.getName().endsWith(".java")) {
			measurementConsumer.accept(Metric.FILE, 1);
			try {
				measurementConsumer.accept(Metric.LINE_OF_CODE, Files.lines(file.toPath(), StandardCharsets.UTF_8).toList().size());
			} catch (Exception e) {
				fail("Exception: " + e, e);
			}
		}
		if (path.equals("docs") && file.isDirectory()) {
			measurementConsumer.accept(Metric.SITE, 1);			
		}
		if (path.startsWith("docs/") && file.isFile() && path.endsWith(".html")) {
			measurementConsumer.accept(Metric.HTML_PAGE, 1);			
		}
	}
	
	/**
	 * Walks the directory passing files and their paths to the listener.
	 * @param source
	 * @param target
	 * @param cleanTarget
	 * @param cleanPredicate
	 * @param listener
	 * @throws IOException
	 */
	public static void walk(String path, BiConsumer<File,String> listener, File... files) {
		if (files != null) {
			for (File file: files) {
				String filePath = path == null ? file.getName() : path + "/" + file.getName();
				listener.accept(file, filePath);
				if (file.isDirectory()) {
					walk(filePath, listener, file.listFiles());
				}
			}
		}
	}
	
	private void createAndLink(
			Model model, 
			Function<Model, Node> resolver,
			Graph graph,
			Item coreCategory,
			Item htmlCategory,
			Item modelsCategory,
			Item otherCategory) {
		Node modelNode = resolver.apply(model);
		if (modelNode != null) {			
			for (Dependency dependency: model.getDependencies()) {
				Model target = dependency.getTarget();
				if (target != null) {
					Node depNode = resolver.apply(target);
					if (depNode != null) {
						org.nasdanika.models.echarts.graph.Link depLink = GraphFactory.eINSTANCE.createLink();				
						depLink.setTarget(depNode);
						modelNode.getOutgoingLinks().add(depLink);
					}
				}
			}
		}
	}
	
	private Node createModelNode(
			Model model,		
			Function<Model, Integer> sizeComputer, 
			Graph graph,
			Item coreCategory,
			Item htmlCategory,
			Item modelsCategory,
			Item otherCategory) {
		
		Node ret = GraphFactory.eINSTANCE.createNode();
		String nodeName = model.getArtifactId();
		ret.setName(nodeName);
		
		if (ret.getName().startsWith("org.nasdanika.html.")) {
			ret.setCategory(htmlCategory);
		} else if (ret.getName().startsWith("org.nasdanika.models")) {
			ret.setCategory(modelsCategory);
		} else if (ret.getName().startsWith("org.nasdanika.")) {
			ret.setCategory(coreCategory);
		} else {
			ret.setCategory(otherCategory);
		}
		
		ret.getSymbolSize().add(10.0 + Math.log(1 + sizeComputer.apply(model)));
		
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

}
