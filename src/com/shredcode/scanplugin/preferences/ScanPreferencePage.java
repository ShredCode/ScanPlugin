package com.shredcode.scanplugin.preferences;

import java.util.logging.Logger;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

import com.shredcode.scanplugin.Activator;

public class ScanPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {
	
	private final static Logger LOGGER = Logger.getLogger(ScanPreferencePage.class.getName());

	public ScanPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Choose options to scan for below");
	}
	
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(
				"AUTOWIRED",
			"@Autowired but not used",
			getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				"MOCK",
			"@Mock but not used",
			getFieldEditorParent()));
		addField(new StringFieldEditor(
				"@USERENTERED",
			"&Annotated variables not used (comma seperated for mutliple)\n",
			getFieldEditorParent()));
		
	}

	public void init(IWorkbench workbench) {
	}
	
}