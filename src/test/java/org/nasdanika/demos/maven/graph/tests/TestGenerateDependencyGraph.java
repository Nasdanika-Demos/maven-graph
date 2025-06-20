package org.nasdanika.demos.maven.graph.tests;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
import org.jgrapht.alg.drawing.model.Point2D;
import org.jgrapht.alg.drawing.model.Points;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.nasdanika.common.Context;
import org.nasdanika.common.NasdanikaException;
import org.nasdanika.html.HTMLFactory;
import org.nasdanika.html.HTMLPage;
import org.nasdanika.html.Tag;
import org.nasdanika.html.TagName;
import org.nasdanika.html.forcegraph3d.ForceGraph3D;
import org.nasdanika.html.forcegraph3d.ForceGraph3DFactory;
import org.nasdanika.models.echarts.graph.Graph;
import org.nasdanika.models.echarts.graph.GraphFactory;
import org.nasdanika.models.echarts.graph.Item;
import org.nasdanika.models.echarts.graph.Link;
import org.nasdanika.models.echarts.graph.Node;
import org.nasdanika.models.echarts.graph.util.GraphUtil;
import org.nasdanika.models.maven.Coordinates;
import org.nasdanika.models.maven.Dependency;
import org.nasdanika.models.maven.MavenFactory;
import org.nasdanika.models.maven.Model;

public class TestGenerateDependencyGraph {
	
	private static final String MODELS_GROUP_PREFIX = "org.nasdanika.models.";
	private static final String DEMOS_GROUP_PREFIX = "org.nasdanika.demos";
	private static final String TEMPLATES_GROUP_PREFIX = "org.nasdanika.templates.";

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
		
	private final String[] GIT_REPOS = { "core", "html", "cli", "ai" /* "cli" , "nasdanika.github.io", "retrieval-augmented-generation" */};	
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
		"rules",
		"crew-ai",
		"python",
		"jira",
		"education"
	};
	private final String[] GIT_DEMO_REPOS = { 
//		"aws-diagram-doc",
//		"bob-the-builder",		
		"cli",
		"compute-graph",
		"concurrent-executable-diagrams",
//		"connecting-the-dots",
//		"declarative-command-pipelines",
//		"diagram-processor-activation",
		"executable-diagram-dynamic-proxy",
//		"executable-graphs-and-diagrams",
//		"executable-uris-story",
//		"family-semantic-mapping",
//		"general-purpose-executable-diagrams-story",
//		"gitlab-junit-assistant",
//		"internet-banking-system",
		"internet-banking-system-c4",
		"jira",
//		"living-beings",
		"maven-graph",
		"mcp-server",
		"semantic-mapping",
//		"visual-communication-continuum"
	};
	
	private final String[] GIT_TEMPLATE_REPOS = { "drawio-site" };	
	
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
				    <link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/Nasdanika-Models/html-app@master/gen/web-resources/css/app.css">
				    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.1/css/all.min.css">
				    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/jstree@3.3.16/dist/themes/default/style.min.css">
				    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/github-markdown-css@5.5.0/github-markdown.min.css">
				    <link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/highlightjs/cdn-release@11.9.0/build/styles/default.min.css">
				    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-vue@2.23.0/dist/bootstrap-vue.css">
				    <script src="https://cdn.jsdelivr.net/gh/Nasdanika-Models/html-app@master/gen/web-resources/js/common.js"></script>
				    <script src="https://cdn.jsdelivr.net/gh/Nasdanika-Models/html-app@master/gen/web-resources/js/dark-head.js"></script>
				    <script src="https://cdn.jsdelivr.net/npm/jstree@3.3.16/dist/jstree.min.js"></script>
				    <script src="https://cdn.jsdelivr.net/gh/highlightjs/cdn-release@11.9.0/build/highlight.min.js"></script>
				    <script src="https://cdn.jsdelivr.net/npm/vue@2.7.16/dist/vue.min.js"></script>
				    <script src="https://cdn.jsdelivr.net/npm/bootstrap-vue@2.23.0/dist/bootstrap-vue.min.js"></script>
				    <script src="https://cdn.jsdelivr.net/gh/Nasdanika-Models/html-app@master/gen/web-resources/js/components/table.js"></script>
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
	
	@Test
	public void testInitJavadocIo() throws IOException {		
		Map<CoordinatesRecord, Entry<File, Model>> models = load();
		for (CoordinatesRecord cr: models.keySet()) {
			String urlStr = "https://javadoc.io/doc/" + cr.groupId() + "/" + cr.artifactId() + "/" + cr.version() + "/";
			System.out.print(urlStr);
			try {
				URL url = new URL(urlStr);
				try (InputStream in = url.openStream()) {
					in.read();
				}
				System.out.println(": OK!");
			} catch (Exception e) {
				System.out.println(": " + e);
			}
		}
	}		
	
	/**
	 * Computes code stats - modules, source files, lines of code.
	 * @throws IOException 
	 */
	@Test
	public void testGenerateDependencyGraph() throws IOException {		
		Map<CoordinatesRecord, Entry<File, Model>> models = load();		
		
		Graph graph = GraphFactory.eINSTANCE.createGraph();
		
		Item coreCategory = GraphFactory.eINSTANCE.createItem();
		coreCategory.setName("Core");
		graph.getCategories().add(coreCategory);
		
		Item aiCategory = GraphFactory.eINSTANCE.createItem();
		aiCategory.setName("AI");
		graph.getCategories().add(aiCategory);
		
		Item htmlCategory = GraphFactory.eINSTANCE.createItem();
		htmlCategory.setName("HTML");
		graph.getCategories().add(htmlCategory);
		
		Item modelsCategory = GraphFactory.eINSTANCE.createItem();
		modelsCategory.setName("Models");
		graph.getCategories().add(modelsCategory);
				
		Item demosCategory = GraphFactory.eINSTANCE.createItem();
		demosCategory.setName("Demos");
		graph.getCategories().add(demosCategory);
				
		Item templatesCategory = GraphFactory.eINSTANCE.createItem();
		templatesCategory.setName("Templates");
//		graph.getCategories().add(templatesCategory);
		
		Item otherCategory = GraphFactory.eINSTANCE.createItem();
		otherCategory.setName("Other");
		graph.getCategories().add(otherCategory);
		
		Map<CoordinatesRecord, Integer> sizeMap = new HashMap<>();
		for (Entry<CoordinatesRecord, Entry<File, Model>> me: models.entrySet()) {
			Map<Metric, int[]> modelMeasurements = new TreeMap<>();
			BiConsumer<Metric, Integer> modelMeasurementConsumer = (metric, measurement) -> modelMeasurements.computeIfAbsent(metric, m -> new int[] { 0 })[0] += measurement;
			repoStats(me.getValue().getKey().getParentFile(), modelMeasurementConsumer, (file, model) -> {});
			int[] loc = modelMeasurements.get(Metric.LINE_OF_CODE);
			if (loc != null) {
				sizeMap.put(new CoordinatesRecord(me.getValue().getValue()), loc[0]);
			}
		}				
		
		Function<Model, Integer> sizeComputer = model -> {
			Integer result = sizeMap.get(new CoordinatesRecord(model));
			return result == null ? 0 : result;
		};
		
		Map<CoordinatesRecord, Node> nodeMap = new HashMap<>();
		for (Entry<CoordinatesRecord, Entry<File, Model>> me: models.entrySet()) {			
			Model model = me.getValue().getValue();
			if ("jar".equals(model.getPackaging())) {
				nodeMap.put(me.getKey(),  createModelNode(
						model, 
						sizeComputer, 
						graph, 
						coreCategory, 
						htmlCategory, 
						aiCategory,
						modelsCategory, 
						templatesCategory,
						demosCategory,
						otherCategory));
			}
		}
		
		Function<Model, Node> resolver	= model -> nodeMap.get(new CoordinatesRecord(model));	
		
		for (Entry<CoordinatesRecord, Entry<File, Model>> me: models.entrySet()) {
			link(
					me.getValue().getValue(), 
					resolver,
					graph, 
					coreCategory, 
					htmlCategory, 
					modelsCategory, 
					templatesCategory,
					demosCategory,
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
                .setTitle("Maven Dependencies")
                .setLegend()
                .addSeries(graphSeries);
    	
	    Engine engine = new Engine();
	    String chartJSON = engine.renderJsonOption(echartsGraph);
	    
	    System.out.println(chartJSON);
	    
		String chartHTML = Context
				.singleton("chart", chartJSON)
				.compose(Context.singleton("graphContainerId", "graph-container"))
				.interpolateToString(GRAPH_TEMPLATE);
	    
	    Files.writeString(new File("docs/index.html").toPath(), chartHTML);
	    
	    // 3D force graph Nasdanika fluent Java API
		ForceGraph3DFactory forceGraph3DFactory = ForceGraph3DFactory.INSTANCE;
		ForceGraph3D forceGraph3D = forceGraph3DFactory.create();
		forceGraph3D.name("graph");
		String forceGraphContainerId = "force-graph";
		forceGraph3D
			.elementId(forceGraphContainerId)
			.nodeAutoColorBy("'group'")
			.nodeVal("'size'")
			.linkDirectionalArrowLength(3.5)
			.linkDirectionalArrowRelPos(1)
			.addExtraRederer("new CSS2DRenderer()")
			.nodeThreeObject(
					"""
					node => {
					        const nodeEl = document.createElement('div');
					        nodeEl.textContent = node.name;
					        nodeEl.style.color = node.color;
					        nodeEl.className = 'node-label';
					        return new CSS2DObject(nodeEl);
					      }					
					""")
			.nodeThreeObjectExtend(true)
			.onNodeDragEnd(
					"""
					node => {
					          node.fx = node.x;
					          node.fy = node.y;
					          node.fz = node.z;
					        }					
					""");
	    
	    // 3D force graph - https://github.com/vasturiano/3d-force-graph?tab=readme-ov-file
	    JSONObject force3DGraph = new JSONObject();
	    JSONArray jNodes = new JSONArray();
	    force3DGraph.put("nodes", jNodes);
	    JSONArray jLinks = new JSONArray();
	    force3DGraph.put("links", jLinks);
	    
	    for (Node node: graph.getNodes()) {
	    	JSONObject jNode = new JSONObject();
	    	jNodes.put(jNode);
	    	jNode.put("id", node.getId());
	    	jNode.put("name", node.getName());
	    	jNode.put("group", node.getCategory().getName());
	    	for (Double symbolSize: node.getSymbolSize()) {
	    		jNode.put("size", symbolSize / 2);
	    	}
	    	Double x = node.getX();
	    	Double y = node.getY();
	    	Point2D point = x != null && y != null ? Point2D.of(x, y) : null;
	    	
	    	forceGraph3D.addNode(jNode);
	    	
	    	for (Link link: node.getOutgoingLinks()) {
	    		JSONObject jLink = new JSONObject();
	    		jLink.put("source", node.getId());
	    		jLink.put("target", link.getTarget().getId());

	    		if (point != null) {
			    	Double tx = link.getTarget().getX();
			    	Double ty = link.getTarget().getY();
			    	Point2D tPoint = tx != null && ty != null ? Point2D.of(tx, ty) : null;
			    	if (tPoint != null) {
			    		Point2D diff = Points.subtract(tPoint, point);
			    		double linkValue = Points.length(diff);
			    		jLink.put("value", linkValue);
			    	}
	    		}
	    		jLinks.put(jLink);
		    	forceGraph3D.addLink(jLink);
	    	}
	    }	    
	    
	    System.out.println("Graph nodes: " + jNodes.length());
	    System.out.println("Graph links: " + jLinks.length());
	    
	    Files.writeString(new File("docs/graph-3d.html").toPath(), Context.singleton("graph-data", force3DGraph.toString(2)).interpolateToString(GRAPH_3D));	
	    
	    // The same thing but with the 3d-force-graph module
	    
		HTMLPage page = HTMLFactory.INSTANCE.page();
		forceGraph3DFactory.cdn(page);
		page.body(HTMLFactory.INSTANCE.div().id(forceGraphContainerId));				
		Tag scriptTag = TagName.script.create(
				System.lineSeparator(),
				"import { CSS2DRenderer, CSS2DObject } from 'https://esm.sh/three/examples/jsm/renderers/CSS2DRenderer.js';",
				System.lineSeparator(),
				forceGraph3D).attribute("type", "module");
		page.body(scriptTag);
		page.head(TagName.style.create(
				"""
				.node-label {
				      font-size: 12px;
				      padding: 1px 4px;
				      border-radius: 4px;
				      background-color: rgba(0,0,0,0.5);
				      user-select: none;
				    }				
				"""));
	    Files.writeString(new File("docs/force-graph-3d.html").toPath(), page.toString());
	   
	    generateWithGraphUtil(graph);
	}
	
	private void generateWithGraphUtil(Graph graph) throws IOException {
		HTMLPage page = HTMLFactory.INSTANCE.page();
		ForceGraph3D forceGraph3D = GraphUtil.asForceGraph3D(graph);
		forceGraph3D.getFactory().cdn(page);
		String forceGraphContainerId = "force-graph";
		forceGraph3D
			.elementId(forceGraphContainerId)
			.nodeAutoColorBy("'group'")
			.nodeVal("'size'")
			.linkDirectionalArrowLength(3.5)
			.linkDirectionalArrowRelPos(1)
			.addExtraRederer("new CSS2DRenderer()")
			.nodeThreeObject(
					"""
					node => {
					        const nodeEl = document.createElement('div');
					        nodeEl.textContent = node.name;
					        nodeEl.style.color = node.color;
					        nodeEl.className = 'node-label';
					        return new CSS2DObject(nodeEl);
					      }					
					""")
			.nodeThreeObjectExtend(true)
			.onNodeDragEnd(
					"""
					node => {
					          node.fx = node.x;
					          node.fy = node.y;
					          node.fz = node.z;
					        }					
					""");
		
		page.body(HTMLFactory.INSTANCE.div().id(forceGraphContainerId));				
		Tag scriptTag = TagName.script.create(
				System.lineSeparator(),
				"import { CSS2DRenderer, CSS2DObject } from 'https://esm.sh/three/examples/jsm/renderers/CSS2DRenderer.js';",
				System.lineSeparator(),
				forceGraph3D).attribute("type", "module");
		page.body(scriptTag);
		page.head(TagName.style.create(
				"""
				.node-label {
				      font-size: 12px;
				      padding: 1px 4px;
				      border-radius: 4px;
				      background-color: rgba(0,0,0,0.5);
				      user-select: none;
				    }				
				"""));
	    Files.writeString(new File("docs/force-graph-3d-util.html").toPath(), page.toString());		    	    		
	}
	
	private static final String GRAPH_3D = 
		"""
		<head>
		  <style> body { margin: 0; } </style>
		
		  <script src="//cdn.jsdelivr.net/npm/3d-force-graph"></script>
		</head>
		
		<body>
		  <div id="3d-graph"></div>
		
		  <script>
		    const gData = ${graph-data};
		
//		    // cross-link node objects
//		    gData.links.forEach(link => {
//		      const a = gData.nodes[link.source];
//		      const b = gData.nodes[link.target];
//		      !a.neighbors && (a.neighbors = []);
//		      !b.neighbors && (b.neighbors = []);
//		      a.neighbors.push(b);
//		      b.neighbors.push(a);
//		
//		      !a.links && (a.links = []);
//		      !b.links && (b.links = []);
//		      a.links.push(link);
//		      b.links.push(link);
//		    });
		
		    const highlightNodes = new Set();
		    const highlightLinks = new Set();
		    let hoverNode = null;
		    const Graph = new ForceGraph3D(document.getElementById('3d-graph'))
		        .graphData(gData)
		        .nodeLabel('id')
		        .nodeAutoColorBy('group')
			.nodeVal('size')
			.linkDirectionalArrowLength(3.5)
			.linkDirectionalArrowRelPos(1)
//		        .nodeColor(node => highlightNodes.has(node) ? node === hoverNode ? 'rgb(255,0,0,1)' : 'rgba(255,160,0,0.8)' : 'rgba(0,255,255,0.6)')
//		        .linkWidth(link => highlightLinks.has(link) ? 4 : 1)
//		        .linkDirectionalParticles(link => highlightLinks.has(link) ? 4 : 0)
//		        .linkDirectionalParticleWidth(4)
		        .onNodeDragEnd(node => {
		          node.fx = node.x;
		          node.fy = node.y;
		          node.fz = node.z;
		        })
		        .onNodeHover(node => {
//		          // no state change
//		          if ((!node && !highlightNodes.size) || (node && hoverNode === node)) return;
//		
//		          highlightNodes.clear();
//		          highlightLinks.clear();
//		          if (node) {
//		            highlightNodes.add(node);
//		            node.neighbors.forEach(neighbor => highlightNodes.add(neighbor));
//		            node.links.forEach(link => highlightLinks.add(link));
//		          }
//		
//		          hoverNode = node || null;
//		
//		          updateHighlight();
		        })
		        .onLinkHover(link => {
//		          highlightNodes.clear();
//		          highlightLinks.clear();
//		
//		          if (link) {
//		            highlightLinks.add(link);
//		            highlightNodes.add(link.source);
//		            highlightNodes.add(link.target);
//		          }
//		
//		          updateHighlight();
		        });
		
		    function updateHighlight() {
		      // trigger update of highlighted objects in scene
		      Graph
		        .nodeColor(Graph.nodeColor())
		        .linkWidth(Graph.linkWidth())
		        .linkDirectionalParticles(Graph.linkDirectionalParticles());
		    };
		  </script>
		</body>			
		""";
	
	protected Map<CoordinatesRecord, Entry<File, Model>> load() {
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
		for (String gitModelRepo: GIT_DEMO_REPOS) {
			repoStats(new File("../../git-demos/" + gitModelRepo), measurementConsumer, mavenModelConsumer);
		}
		for (String gitModelRepo: GIT_TEMPLATE_REPOS) {
			repoStats(new File("../../git-templates/" + gitModelRepo), measurementConsumer, mavenModelConsumer);
		}
			
		measurements.entrySet().forEach(e -> System.out.println(e.getKey() + " = " + e.getValue()[0]));
		
		for (Entry<CoordinatesRecord, Entry<File, Model>> me: models.entrySet()) {
			me.getValue().getValue().resolve(c -> {
				Entry<File, Model> e = models.get(new CoordinatesRecord(c));
				return e == null ? null : e.getValue();
			});
		}
		return models;
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
	
	private void link(
			Model model, 
			Function<Model, Node> resolver,
			Graph graph,
			Item coreCategory,
			Item htmlCategory,
			Item modelsCategory,
			Item templatesCategory,
			Item demosCategory,
			Item otherCategory) {
		Node modelNode = resolver.apply(model);				
		if (modelNode != null) {			
			System.out.println(modelNode.getId());
			for (Dependency dependency: model.getDependencies()) {
				Model target = dependency.getTarget();
				System.out.println("\t" + dependency.getGroupId() + ":" + dependency.getArtifactId() + " -> " + target);
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
			Item aiCategory,
			Item modelsCategory,
			Item templatesCategory,
			Item demosCategory,
			Item otherCategory) {
		
		Node ret = GraphFactory.eINSTANCE.createNode();
		String nodeName = model.getId();
		ret.setId(model.getId());
		
		if (ret.getId().startsWith("org.nasdanika.html:")) {
			ret.setCategory(htmlCategory);
			ret.setName(model.getArtifactId());
		} else if (ret.getId().startsWith(MODELS_GROUP_PREFIX)) {
			ret.setCategory(modelsCategory);
			ret.setName(model.getGroupId().substring(MODELS_GROUP_PREFIX.length()) + ":" + model.getArtifactId());
		} else if (ret.getId().startsWith(DEMOS_GROUP_PREFIX + ".")) {
			ret.setCategory(demosCategory);
			ret.setName(model.getGroupId().substring(DEMOS_GROUP_PREFIX.length() + 1) + ":" + model.getArtifactId());
		} else if (ret.getId().startsWith(DEMOS_GROUP_PREFIX + ":")) {
			ret.setCategory(demosCategory);
			ret.setName(model.getArtifactId());
		} else if (ret.getId().startsWith(TEMPLATES_GROUP_PREFIX)) {
			ret.setCategory(templatesCategory);
			ret.setName(model.getGroupId().substring(TEMPLATES_GROUP_PREFIX.length()) + ":" + model.getArtifactId());
		} else if (ret.getId().startsWith("org.nasdanika.core:")) {
			ret.setCategory(coreCategory);
			ret.setName(model.getArtifactId());
		} else if (ret.getId().startsWith("org.nasdanika.ai:")) {
			ret.setCategory(aiCategory);
			ret.setName(model.getArtifactId());
		} else {
			ret.setCategory(otherCategory);
			ret.setName(nodeName);
		}
		
		ret.getSymbolSize().add(5.0 + Math.log(1 + sizeComputer.apply(model)));
		
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
