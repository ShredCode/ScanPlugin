package com.shredcode.scanplugin.resolutions;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;

import com.shredcode.scanplugin.utils.ScanPluginUtils;



public class AnnotationNotUsedResolutionGenerator implements IMarkerResolutionGenerator2  {

	private final static Logger LOGGER = Logger.getLogger(AnnotationNotUsedResolutionGenerator.class.getName());
	
	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		final List<AnnotationNotUsedResolution> markerResolutionList = new ArrayList<AnnotationNotUsedResolution>();
		markerResolutionList.add(new AnnotationNotUsedResolution());
		return markerResolutionList.toArray(new IMarkerResolution[markerResolutionList.size()]);
	}

	@Override
	public boolean hasResolutions(IMarker marker) {
		String customType = marker.getAttribute("annotationType", "");
		List<String> annotationsToScanForUsed = ScanPluginUtils.findAnnotationToScanForUnused();
		for (String annScan : annotationsToScanForUsed) {
			if (annScan.equalsIgnoreCase(customType)) {
				return true;
			}
		}
		return false;
	}

}
