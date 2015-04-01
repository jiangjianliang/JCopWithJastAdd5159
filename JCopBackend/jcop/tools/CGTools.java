package jcop.tools;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import AST.CGCallee;
import AST.CGNode;
import AST.CGRoot;

/**
 * 3-4
 * @author wander
 *
 */
public class CGTools {
	
	public static boolean canReach(CGNode from, CGNode to){
		Queue<CGNode> queue = new LinkedList<CGNode>();
		queue.add(from);
		
		Set<CGNode> closure = new HashSet<CGNode>();
		closure.add(from);
		
		while(!queue.isEmpty()){
			CGNode node = queue.remove();
			for(Iterator<CGCallee> iter = node.CGCallees().iterator(); iter.hasNext();){
				CGCallee callee = iter.next();
				for(Iterator<CGNode> innerIter = callee.CGEdges().iterator(); innerIter.hasNext();){
					CGNode toBeCheck = innerIter.next();
					if(!closure.contains(toBeCheck)){
						queue.add(toBeCheck);
						closure.add(toBeCheck);
					}
				}
			}
			if(closure.contains(to)){//test to end quickly
				return true;
			}
		}
		
		return false;
		
	}
	
	public static boolean canReach(CGRoot root, CGNode to){
		//one starter can reach CGNode(to) is ok!
		for(Iterator<CGNode> iter = root.CGNodes().iterator(); iter.hasNext();){
			if(canReach(iter.next(), to)){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	}

}
