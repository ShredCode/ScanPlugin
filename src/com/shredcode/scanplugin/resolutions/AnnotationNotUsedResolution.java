package com.shredcode.scanplugin.resolutions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.views.markers.WorkbenchMarkerResolution;

import com.shredcode.scanplugin.utils.ScanPluginUtils;


public class AnnotationNotUsedResolution extends WorkbenchMarkerResolution {
	
	private final static Logger LOGGER = Logger.getLogger(AnnotationNotUsedResolution.class.getName());

	@Override
	public String getLabel() {
		return "Delete annotated variable never used";
	}
	
	@Override
	public IMarker[] findOtherMarkers(IMarker[] paramArrayOfIMarker) {
		List<IMarker> others = new ArrayList<IMarker>();
		for (IMarker marker : paramArrayOfIMarker) {
			try {
				if ("ScanPlugin.unusedProblem".equalsIgnoreCase(marker
						.getType())) {
					others.add(marker);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return others.toArray(new IMarker[0]);
	}

	@Override
	public void run(IMarker marker) {
        IResource resource = marker.getResource();
        final int lineNumber = marker.getAttribute(IMarker.LINE_NUMBER, 0);
        if (resource instanceof IFile) {
            IFile file = (IFile) resource;
			ICompilationUnit cunit =(ICompilationUnit)JavaCore.create(file);
			final CompilationUnit unit = ScanPluginUtils.parse(cunit);
            final ASTRewrite rewrite = ASTRewrite.create(unit.getAST());
			final List<String> annotationsToScanForUsed = ScanPluginUtils.findAnnotationToScanForUnused();
			if (annotationsToScanForUsed.isEmpty()) {
				return;
			}
			
			try {
				unit.accept(new ASTVisitor() {
					 
					public boolean visit(FieldDeclaration node) {
						List<ASTNode> mods = node.modifiers();
						for (ASTNode mod:mods) {
							if (mod instanceof MarkerAnnotation) {
							SimpleName name =	(SimpleName)((MarkerAnnotation)mod).getTypeName();
							for (String annotationToSearchFor : annotationsToScanForUsed) {
								if (annotationToSearchFor.equalsIgnoreCase(name
										.getIdentifier())) {
		
									for (Iterator iter = node.fragments().iterator(); iter.hasNext();) {
										VariableDeclarationFragment fragment = (VariableDeclarationFragment) iter.next();
										if (unit.getLineNumber(fragment.getStartPosition()) == lineNumber) {
											rewrite.remove(node,null);
											//rewrite.remove(mod, null);
										}
									}
								}
							}
								
							}
						}
						
						return true;
					}
		 
					public boolean visit(VariableDeclarationFragment node) {
						SimpleName name = node.getName();
						System.out.println("Declaration of '"+name+"' at line"+ unit.getLineNumber(name.getStartPosition()));
						return false; // do not continue to avoid usage info
					}
		 
				});
				
				ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager(); // get the buffer manager
				IPath path = unit.getJavaElement().getPath(); // unit: instance of CompilationUnit
				try {
					bufferManager.connect(path, LocationKind.IFILE, null); // (1)
					ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path, LocationKind.IFILE);
					// retrieve the buffer
					IDocument document = textFileBuffer.getDocument();
					// ... edit the document here ... 
					TextEdit edit = rewrite.rewriteAST();
					edit.apply(document);
				  // commit changes to underlying file
					textFileBuffer
						.commit(null /* ProgressMonitor */, false /* Overwrite */); // (3)

				} finally {
					bufferManager.disconnect(path, LocationKind.IFILE, null); // (4)
				}
				
				//((ICompilationUnit) unit).applyTextEdit(rewrite.rewriteAST(), new NullProgressMonitor());
			} catch (Exception e) {
				e.printStackTrace();
			}
            
        }
		
	}

	@Override
	public String getDescription() {
		return "Delete annotated variable never used";
	}

	@Override
	public Image getImage() {
		return null;
	}

}
