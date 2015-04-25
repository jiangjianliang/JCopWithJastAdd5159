package jcop.transformation.lookup;

import java.util.Hashtable;

import jcop.Globals.Msg;
import AST.BodyDecl;
import AST.ClassDecl;
import AST.CompositeMethodDecl;
import AST.ImportDecl;
import AST.LayerDecl;
import AST.LayerDeclaration;
import AST.List;
import AST.MethodDecl;
import AST.SimpleSet;
import AST.TypeDecl;

/**
 * Documented by wander,
 * 
 * helper class for looking up layer related problems, such as base method of
 * partial method
 * 
 */
public class Lookup {

	private static Hashtable<MethodDecl, MethodDecl> baseMethods;

	//for composite method
	private static Hashtable<CompositeMethodDecl, MethodDecl> compToBaseMethods;
	
	static {
		baseMethods = new Hashtable<MethodDecl, MethodDecl>();
		compToBaseMethods = new Hashtable<CompositeMethodDecl, MethodDecl>();
	}

	public static void setBaseForPartialMethod(MethodDecl originalMethod,
			MethodDecl partialMethod) {
		baseMethods.put(partialMethod, originalMethod);
		baseMethods.put(originalMethod, originalMethod);
	}

	/**
	 * look for corresponding base method for partial method
	 * 
	 * @param cmd
	 * @return
	 */
	public static MethodDecl lookupMethodCorrespondingTo(CompositeMethodDecl cmd) {
		try {
			ClassDecl host = (ClassDecl) cmd.hostType();
			return lookupMethodCorrespondingTo(host, cmd);
		} catch (Exception e) {
			System.err.println("Error: cannot lookup method corresponding to "
					+ cmd.getNamePattern().toString());
			return null;
		}
	}
	
	/**
	 * find and store mapping from partial method to base method into
	 * baseMethods
	 * 
	 * @param host
	 * @param cmd
	 * @return
	 */
	private static MethodDecl lookupMethodCorrespondingTo(ClassDecl host,
			CompositeMethodDecl cmd) {
		MethodDecl baseMethod = findBaseMethod(host, cmd);
		
		if (!compToBaseMethods.contains(cmd) && baseMethod != null)
			compToBaseMethods.put(cmd, baseMethod);
		return compToBaseMethods.get(cmd);
	}
	
	/**
	 * look for base method of partial method in the host class first, then in
	 * supper class and so on
	 * 
	 * @param host
	 * @param cmd
	 * @return
	 */
	private static MethodDecl findBaseMethod(ClassDecl host, CompositeMethodDecl cmd) {

		String signatureOfPartialMethod = cmd.signature();
		for (BodyDecl bodyDecl : host.getBodyDeclListNoTransform()) {
			if (bodyDecl instanceof MethodDecl) {
				String signature = ((MethodDecl) bodyDecl).signature();
				if (signature.equals(signatureOfPartialMethod)) {
					return (MethodDecl) bodyDecl;
				}
			}
		}
		if (host.superclass() != null)
			return lookupMethodCorrespondingTo(host.superclass(), cmd);
		return null;
	}
	
	/**
	 * look for corresponding base method for partial method
	 * 
	 * @param pmd
	 * @return
	 */
	public static MethodDecl lookupMethodCorrespondingTo(MethodDecl pmd) {
		try {
			ClassDecl host = (ClassDecl) pmd.hostType();
			return lookupMethodCorrespondingTo(host, pmd);
		} catch (Exception e) {
			System.err.println("Error: cannot lookup method corresponding to "
					+ pmd.getFullQualifiedName());
			return pmd;
		}
	}

	/**
	 * find and store mapping from partial method to base method into
	 * baseMethods
	 * 
	 * @param host
	 * @param pmd
	 * @return
	 */
	private static MethodDecl lookupMethodCorrespondingTo(ClassDecl host,
			MethodDecl pmd) {
		MethodDecl baseMethod = findBaseMethod(host, pmd);
		/*
		 * if baseMethod == null, it is a layer local class and null should be
		 * returned
		 */
		if (!baseMethods.contains(pmd) && baseMethod != null)
			baseMethods.put(pmd, baseMethod);
		return baseMethods.get(pmd);
	}

	/**
	 * look for base method of partial method in the host class first, then in
	 * supper class and so on
	 * 
	 * @param host
	 * @param pmd
	 * @return
	 */
	private static MethodDecl findBaseMethod(ClassDecl host, MethodDecl pmd) {
		// System.out.println("find base method, host: " + host.getID() +
		// ", method:" + pmd);
		;

		String signatureOfPartialMethod = pmd.signature();
		for (BodyDecl bodyDecl : host.getBodyDeclListNoTransform()) {
			if (bodyDecl instanceof MethodDecl) {
				String signature = ((MethodDecl) bodyDecl).signature();
				if (signature.equals(signatureOfPartialMethod)) {
					return (MethodDecl) bodyDecl;
				}
			}
		}
		if (host.superclass() != null)
			return lookupMethodCorrespondingTo(host.superclass(), pmd);
		return null;
	}

	/**
	 * look for corresponding {@link AST.ClassDecl ClassDecl} for layerDecl
	 * 
	 * @param layerDecl
	 * @return
	 */
	public static ClassDecl lookupLayerClassDecl(LayerDeclaration layerDecl) {
		if (layerDecl.hostType() == layerDecl)
			return (LayerDecl) layerDecl.hostType();

		SimpleSet s = layerDecl.hostType().lookupType(layerDecl.getID());
		if (s.isEmpty())
			throw new RuntimeException(Msg.LayerDeclarationNotFound
					+ layerDecl.getID());
		return (ClassDecl) s.iterator().next();

		// //layer.topLevelType().lookupType(name)
		// ClassDecl classDecl = null ;
		// List<ImportDecl> list = getImportsOfHostType(layerDecl);
		// classDecl = findClassDeclInImports(list, layerDecl);
		// return classDecl;
	}

	/**
	 * FIXME waiting
	 * @param list
	 * @param decl
	 * @return
	 */
	private static ClassDecl findClassDeclInImports(List<ImportDecl> list,
			LayerDeclaration decl) {
		for (ImportDecl imp : list) {
			TypeDecl importDecl = imp.getAccess().type();
			if (importDecl.name().equals(decl.getID()))
				return (ClassDecl) importDecl;
		}
		throw new RuntimeException(Msg.LayerDeclarationNotFound + decl.getID());
	}

	private static List<ImportDecl> getImportsOfHostType(LayerDeclaration decl) {
		return decl.hostType().topLevelType().compilationUnit()
				.getImportDeclList();
	}
}
