package JSAnalyzer

object GraphvizDrawer {
	var tab = "\t"
	def export(graph: Graph ,export:  java.io.PrintStream = System.out, clusters : Int = 0) = {
 		export.println("digraph "+graph.name()+" {")

 		drawGraph( graph, export,clusters )

 		export.println("}")
	}

	def drawGraph( graph : Graph, export:  java.io.PrintStream = System.out,clusters : Int = 0) : Int = {
		var newCluster = drawSubgraphs( graph.subgraphs(), export, clusters )

		drawNodes( graph.nodes(), export )

 		var ranks = getRanks( graph.nodes )

 		drawEdges( graph.edges(), export )

 		drawRanks( ranks, export )

 		newCluster
	}

	def drawSubgraphs( graphs: List[Graph], export:  java.io.PrintStream = System.out,clusters : Int = 0 ) : Int = {
		graphs.foldLeft(clusters)((clusters,graph) => {
			export.println("subgraph cluster_"+clusters+" {")
			export.println("label = \""+graph.name()+"\";")
			var res = drawGraph( graph, export, clusters + 1 )
			export.println("}")
			res
		})
	}

	def drawNodes(nodes : List[Node], export:  java.io.PrintStream = System.out) = {
		nodes.foreach((node) => export.println( tab + "\"%s\" [shape=%s label=\"%s\"];".format(node.id, node.shape.getOrElse(Record()), node.label)))
	}

	def getRanks(nodes: List[Node]) : Map[Int, List[Node]] = {
		nodes.foldLeft(Map() : Map[Int, List[Node]])((ranks,node) => node.rank match {
 			case Some(rank) => ranks + ((rank,node :: ranks.getOrElse(rank,List())))
 			case None => ranks
 		})
	} 

	def drawEdges( edges: List[Edge], export:  java.io.PrintStream = System.out) = {
		edges.foreach((edge) => edge.label match {
 			case Some(l) => export.println(tab + "\"%s\" -> \"%s\" [label=\"%s\"];".format(edge.from,edge.to, l))
 			case None => export.println(tab + "\"%s\" -> \"%s\"".format(edge.from, edge.to))
 		})
	}

	def drawRanks(ranks : Map[Int, List[Node]], export:  java.io.PrintStream = System.out) = {
		ranks.foreach{
	      case (key, value) => export.println(tab + "{ rank=same; %s }".format(value.map(_.id).reduceLeft(_ + " " + _)))
	    }
	}

	def escape(s :String) : String = {
		s.map(_ match { 
			  case '\n' => "\\n"
	          case '\'' => "&apos;"
	          case '"' => "&quot;"
	          case '<' => "&lt;"
	          case '>' => "&gt;"
	          case '{' => "\\{"
	          case '}' => "\\}"
	          case ']' => "\\]"
	          case '[' => "\\["
	          case other => other toString
        }) mkString;
	}


	abstract class Shape() {
		override def toString() : String = {
			var n = this 
			n match {
				case n : Diamond => "Mdiamond"
				case n : Square => "Msquare"
				case n : Record => "record"
			}
		}
	}
	case class Diamond() extends Shape
	case class Square() extends Shape
	case class Record() extends Shape

	case class Node(id : String, label: String, shape: Option[Shape] = None, rank: Option[Int] = None)

	case class Edge(from : String, to: String, label: Option[String] = None)

	abstract class Graph() {
		def name() : String
		def edges() : List[Edge]
		def nodes() : List[Node]
		def subgraphs() : List[Graph]
	}
}



