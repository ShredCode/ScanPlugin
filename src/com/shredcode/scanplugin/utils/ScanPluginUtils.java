package com.shredcode.scanplugin.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.shredcode.scanplugin.preferences.PreferenceConstants;


public class ScanPluginUtils {

	private static final String PLUGIN_ID = "ScanPlugin";

	public static CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit); // set source
		parser.setResolveBindings(true); // we need bindings later on
		return (CompilationUnit) parser.createAST(null /* IProgressMonitor */); // parse
	}
	
	public static List<String> findAnnotationToScanForUnused() {

		IPreferencesService service = Platform.getPreferencesService();
		boolean autowiredFlag = service.getBoolean(PLUGIN_ID,
				PreferenceConstants.P_AUTOWIRED, true, null);
		boolean mockFlag = service.getBoolean(PLUGIN_ID,
				PreferenceConstants.P_MOCK, true, null);
		List<String> results = new ArrayList<String>(2);
		if (autowiredFlag) {
			results.add("Autowired");
		}
		if (mockFlag) {
			results.add("Mock");
		}
		String userEnteredAnnotations = service.getString(PLUGIN_ID,
				PreferenceConstants.P_USERENTERED, "", null);
		if (userEnteredAnnotations != null && !userEnteredAnnotations.isEmpty()) {
			String[] userArr = userEnteredAnnotations.split(",");
			for (int i = 0; i < userArr.length; i++) {
				String ann = userArr[i];
				if (ann != null && !ann.isEmpty()) {
					results.add(ann);
				}
			}
		}

		// LOGGER.info("anser " + autowiredFlag + ":" + mockFlag);
		return results;
	}
}
