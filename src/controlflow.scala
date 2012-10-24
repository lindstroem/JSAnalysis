abstract class ControlFlowNode

case class ControlFlowASTNode(ASTel:Any = null) extends ControlFlowNode
case class MergeNode(label:String) extends ControlFlowNode
case class ContinueNode(i:Identifier) extends ControlFlowNode
case class ReturnNode() extends ControlFlowNode
case class ThrowNode(e:Expression) extends ControlFlowNode

case class ControlFlowGraph(
	start : Option[ControlFlowNode] = None, 
	end: Option[ControlFlowNode] = None, 
	nodes: List[ControlFlowNode] = List(), 
	edges: Map[ControlFlowNode, ControlFlowNode] = Map(), 
	labels:Map[(ControlFlowNode, ControlFlowNode), String] = Map()
) {
	def +(el: ControlFlowNode ) = ControlFlow.append( this, el )
	def ::(cfg: ControlFlowGraph ) = ControlFlow.concat( this, cfg )
}

object ControlFlow {
	/******************************/
	/******** HELPER METHODS ******/
	/******************************/
	def append( cfg : ControlFlowGraph, el : ControlFlowNode,label: String = null ) : ControlFlowGraph = {
		cfg.start match {
			case None => { //Assume everything is empty
				ControlFlowGraph(Some(el),Some(el),el :: cfg.nodes,cfg.edges,cfg.labels)
			}
			case Some(n) => {
				if (label != null){
					ControlFlowGraph(Some(el),cfg.end,el :: cfg.nodes,cfg.edges ++ Map((el,n)) ,cfg.labels ++ Map(((el,n),label)))
				} else {
					ControlFlowGraph(Some(el),cfg.end,el :: cfg.nodes,cfg.edges ++ Map((el,n)),cfg.labels)
				}
			}
		}
	}

	def prepend( cfg : ControlFlowGraph, el: ControlFlowNode, label:String = null ) : ControlFlowGraph = {
		cfg.end match {
			case None => { //Assume everything is empty
				ControlFlowGraph(Some(el),Some(el),el :: cfg.nodes,cfg.edges,cfg.labels)
			}
			case Some(n) => {
				if (label != null)
					ControlFlowGraph(cfg.start,Some(el),el :: cfg.nodes,cfg.edges ++ Map((n,el)),cfg.labels ++ Map(((n,el),label)))
				else
					ControlFlowGraph(cfg.start,Some(el),el :: cfg.nodes,cfg.edges ++ Map((n,el)),cfg.labels)
			}
		}
	}

	def concat(cfg1 : ControlFlowGraph, cfg2: ControlFlowGraph, label: String = null ) : ControlFlowGraph = {
		(cfg1.start,cfg2.start,cfg1.end) match {
			case (None,None,_) => cfg1//Both empty so just reply one of them.
			case (None,Some(n),_) => cfg2 //cfg1 is empty
			case (Some(n),None,_) => cfg1 //cfg2 is empty
			case (Some(n),Some(m),Some(k)) => {
				if (label != null)
					ControlFlowGraph(cfg1.start,cfg2.end,cfg1.nodes ::: cfg2.nodes, cfg1.edges ++ cfg2.edges ++ Map((k,m)), cfg1.labels ++ cfg2.labels ++ Map(((k,m),label)))
				else 
					ControlFlowGraph(cfg1.start,cfg2.end,cfg1.nodes ::: cfg2.nodes, cfg1.edges ++ cfg2.edges ++ Map((k,m)), cfg1.labels ++ cfg2.labels)
			}
		}
	}

	/******************************/
	/*** Recursivly walkthrough ***/
	/******************************/
	def statement( s:AST.Statement ) : ControlFlowGraph = s match {
		case AST.Block( sl ) => statements( sl )
		case AST.VariableStatement(vds) => ControlFlowGraph() //TODO
		case AST.EmptyStatement() => ControlFlowGraph()
		case AST.ExpressionStatement(e) => expression(e)
 		case AST.IfStatement(e,s1,os2) => {
			var cfg1 = expression( e ) :: statement( s1 )
			var cfg2 = os2 match {
				case Some(s2) => expression( e ) :: statement( s2 )
				case None => expression( e )
			}
			cfg1
/*
			var cfg2 = cfg1.branch( statement( s1 ) )
			var cfg3 = cfg1.branch( statement( s2 ) )*/
		}
		case AST.WhileStatement(e:Expression, s:Statement) => ControlFlowGraph() //TODO
		case AST.DoWhileStatement() => ControlFlowGraph() //TODO
		case AST.ForStatement(_,_,_,_) => ControlFlowGraph()//TODO
		case AST.ForInStatement() => ControlFlowGraph()//TODO
		case AST.ContinueStatement(oi) => ControlFlowGraph()//TODO
		case AST.BreakStatement(i) => ControlFlowGraph() //TODO
		case AST.ReturnStatement(oe) => ControlFlowGraph() //TODO
		case AST.WithStatement(e,s) => ControlFlowGraph() //TODO
		case AST.LabelledStatement(i,s) => ControlFlowGraph() //TODO
		case AST.SwitchStatement(e,cb) => ControlFlowGraph() //TODO
		case AST.ThrowStatement(e) => ControlFlowGraph() //TODO
		case AST.TryStatement(b,oc,of) => ControlFlowGraph() //TODO
	}

	def statements(ss : List[AST.Statement] ) : ControlFlowGraph = ss match {
		case s :: ss => statement( s ) :: statements( ss )
		case Nil => ControlFlowGraph()
	}

	def expression( e:AST.Expression ) : ControlFlowGraph = ControlFlowGraph()
}