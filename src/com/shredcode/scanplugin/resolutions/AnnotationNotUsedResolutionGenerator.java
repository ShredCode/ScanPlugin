package com.shredcode.scanplugin.resolutions;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.eclipse.ui.views.markers.WorkbenchMarkerResolution;

import com.shredcode.scanplugin.utils.ScanPluginUtils;



public class AnnotationNotUsedResolutionGenerator extends WorkbenchMarkerResolution implements IMarkerResolutionGenerator2  {

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

	@Override
	public String getDescription() {
		return "Delete the field that is autowired and not used.";
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void run(IMarker paramIMarker) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IMarker[] findOtherMarkers(IMarker[] paramArrayOfIMarker) {
		// TODO Auto-generated method stub
		return null;
	}

}
