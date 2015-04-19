package com.shredcode.scanplugin.preferences;

import java.util.logging.Logger;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.shredcode.scanplugin.Activator;


/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
	
	private final static Logger LOGGER = Logger.getLogger(PreferenceInitializer.class.getName());

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_AUTOWIRED, true);
		store.setDefault(PreferenceConstants.P_MOCK, true);
		store.setDefault(PreferenceConstants.P_USERENTERED, "");
	}

}
