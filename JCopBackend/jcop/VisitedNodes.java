package jcop;

import java.util.Hashtable;

import AST.ASTNode;
/**
 * Documented by wander
 * 
 * a data structure about counting visiting times of every ASTNode
 * a hash table here is used to store mapping from ASTNode to VisitCounter
 * 
 * @see jcop.generation.jcopaspect.PartialFieldAdviceGenerator#createFieldAccessorAdviceOnce(AST.FieldDeclaration, String, String)
 * @see jcop.transformation.PartialMethodSourceTransformer#transform()
 */
public class VisitedNodes {
	// private static HashSet<MemberDecl> visitedBaseMemberForCodeGeneration =
	// new HashSet<MemberDecl>();
	// private static HashSet<MemberDecl>
	// visitedBaseMemberForDelegationMethodGeneration = new
	// HashSet<MemberDecl>();
	private static Hashtable<ASTNode, VisitCounter> visited = new Hashtable<ASTNode, VisitCounter>();

	public static void add(ASTNode<ASTNode> decl) {
		visited.put(decl, new VisitCounter());
	}

	private static VisitCounter get(ASTNode<ASTNode> decl) {
		if (!visited.containsKey(decl))
			add(decl);
		return visited.get(decl);
	}

	public static boolean firstVisit(ASTNode<ASTNode> decl) {
		return (decl == null) || numberOfVisits(decl, 0);
	}

	public static boolean secondVisit(ASTNode<ASTNode> decl) {
		return numberOfVisits(decl, 1);
	}
	
	/**
	 * check whether ASTNode is visited less than| equal to nr times
	 * if true, visit ASTNode, and do nothing otherwise
	 * @param decl
	 * @param nr
	 * @return
	 */
	private static boolean numberOfVisits(ASTNode<ASTNode> decl, int nr) {
		VisitCounter v = get(decl);
		if (v.getVisits() <= nr) {
			v.visit();
			return true;
		} else
			return false;
	}
	
	/**
	 * maintain visited times
	 *
	 */
	static class VisitCounter {
		private int numVisits;

		VisitCounter() {
			numVisits = 0;
		}

		int getVisits() {
			return numVisits;
		}

		void visit() {
			numVisits++;
		}
	}
}
