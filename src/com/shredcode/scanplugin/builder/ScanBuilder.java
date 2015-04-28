package com.shredcode.scanplugin.builder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.shredcode.scanplugin.utils.ScanPluginUtils;


public class ScanBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = "ScanPlugin.scanBuilder";

	private static final String MARKER_TYPE = "ScanPlugin.unusedProblem";
	private final static Logger LOGGER = Logger.getLogger(ScanBuilder.class
			.getName());

	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	void checkForChanges(IResource resource) {

		LOGGER.log(Level.INFO, "starting checkForChanges");

		if (resource instanceof IFile && resource.getName().endsWith(".java")) {

			final List<String> annotationsToScanForUsed = ScanPluginUtils.findAnnotationToScanForUnused();
			if (annotationsToScanForUsed.isEmpty()) {
				return;
			}

			IFile file = (IFile) resource;
			deleteMarkers(file);
			ICompilationUnit cunit = (ICompilationUnit) JavaCore.create(file);
			final CompilationUnit parsedUnit = ScanPluginUtils.parse(cunit);
			final Set names = new HashSet();
			final Set binds = new HashSet();
			final Map<IVariableBinding, VariableDeclarationFragment> bindstoNode = new HashMap<IVariableBinding, VariableDeclarationFragment>();
			final Map<IVariableBinding, String> bindstoAnnotation = new HashMap<IVariableBinding, String>();
			try {
				parsedUnit.accept(new ASTVisitor() {

					public boolean visit(FieldDeclaration node) {
						List<ASTNode> mods = node.modifiers();
						for (ASTNode mod : mods) {
							if (mod instanceof MarkerAnnotation) {
								SimpleName name = (SimpleName) ((MarkerAnnotation) mod)
										.getTypeName();
								for (String annotationToSearchFor : annotationsToScanForUsed) {
									if (annotationToSearchFor.equalsIgnoreCase(name
											.getIdentifier())) {
										for (Iterator iter = node.fragments()
												.iterator(); iter.hasNext();) {
											VariableDeclarationFragment fragment = (VariableDeclarationFragment) iter
													.next();
											IVariableBinding binding = fragment
													.resolveBinding();
											binds.add(binding);
											bindstoNode.put(binding, fragment);
											bindstoAnnotation.put(binding, annotationToSearchFor);
										}
									}
								}

							}
						}
						return true;
					}

					public boolean visit(VariableDeclarationFragment node) {
						SimpleName name = node.getName();
						names.add(name.getIdentifier());
						System.out.println("Declaration of '"
								+ name
								+ "' at line"
								+ parsedUnit.getLineNumber(name
										.getStartPosition()));
						return false; // do not continue to avoid usage info
					}

					public boolean visit(SimpleName node) {
						if (names.contains(node.getIdentifier())) {
							System.out.println("Usage of '"
									+ node
									+ "' at line "
									+ parsedUnit.getLineNumber(node
											.getStartPosition()));
						}
						if (binds.contains(node.resolveBinding())) {
							binds.remove(node.resolveBinding());
							System.out.println("variable used");
						}
						return true;
					}

				});
				if (!binds.isEmpty()) {
					for (Object object : binds) {
						IVariableBinding binding = (IVariableBinding) object;
						addMarker(
								file,
								"Unused variable with Annnotation @" + bindstoAnnotation.get(
										binding),
								parsedUnit.getLineNumber(bindstoNode.get(
										binding).getStartPosition()),bindstoNode.get(
												binding),
								IMarker.SEVERITY_ERROR, bindstoAnnotation.get(
										binding));
					}
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "exception in checkForChanges", e);
			}
		}
	}


	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
		try {
			getProject().accept(new ScanResourceVisitor());
		} catch (CoreException e) {
		}
	}

	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		delta.accept(new ScanDeltaVisitor());
	}

	private void addMarker(IFile file, String message, int lineNumber,
			VariableDeclarationFragment node, int severity, String annotation) {
		try {
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute("annotationType", annotation);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			marker.setAttribute ( IMarker.CHAR_START, node.getStartPosition() ) ;
			marker.setAttribute ( IMarker.CHAR_END, node.getStartPosition() + node.getLength() ) ; 
		} catch (CoreException e) {
		}
	}

	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}

	class ScanDeltaVisitor implements IResourceDeltaVisitor {

		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				checkForChanges(resource);
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				checkForChanges(resource);
				break;
			}
			// return true to continue visiting children.
			return true;
		}
	}

	class ScanResourceVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) {
			checkForChanges(resource);
			// return true to continue visiting children.
			return true;
		}
	}
}
