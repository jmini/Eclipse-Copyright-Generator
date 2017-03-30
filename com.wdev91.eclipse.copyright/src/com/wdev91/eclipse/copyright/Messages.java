/*******************************************************************************
 * Copyright (c) 2008-2012 Eric Wuillai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Eric Wuillai - initial API and implementation
 ******************************************************************************/
package com.wdev91.eclipse.copyright;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "com.wdev91.eclipse.copyright.messages"; //$NON-NLS-1$
  public static String ApplyCopyrightOnSelectionHandler_confirmMessage;
  public static String ApplyCopyrightOnSelectionHandler_messageTitle;
  public static String ApplyCopyrightWizard_error;
  public static String ApplyCopyrightWizard_selectionTaskMessage;
  public static String ApplyCopyrightWizard_title;
  public static String CopyrightManager_customLabel;
  public static String CopyrightManager_err_licenseCreate;
  public static String CopyrightManager_err_readContent;
  public static String CopyrightManager_err_savingFileNameMissing;
  public static String CopyrightManager_err_savingHeaderTextMissing;
  public static String CopyrightManager_err_savingHeaderTextMissingForProject;
  public static String CopyrightManager_err_savingLicenseTextMissing;
  public static String CopyrightManager_err_savingXmlFile;
  public static String CopyrightManager_err_selection;
  public static String CopyrightManager_err_validation;
  public static String CopyrightManager_jobName;
  public static String CopyrightManager_taskName;
  public static String CopyrightPreferencePage_buttonAdd;
  public static String CopyrightPreferencePage_buttonDelete;
  public static String CopyrightPreferencePage_buttonModify;
  public static String CopyrightPreferencePage_err_labelAlreadyExists;
  public static String CopyrightPreferencePage_err_noLabelProvided;
  public static String CopyrightPreferencePage_inputLabel;
  public static String CopyrightPreferencePage_inputTitle;
  public static String CopyrightPreferencePage_labelOwner;
  public static String CopyrightPreferencePage_labelLicenses;
  public static String CopyrightPreferencePage_labelFilecontent;
  public static String CopyrightPreferencePage_labelFilename;
  public static String CopyrightPreferencePage_labelHeader;
  public static String CopyrightPreferencePage_labelLicenseFile;
  public static String CopyrightPreferencePage_msgConfirmDelete;
  public static String CopyrightPreferencePage_titleCopyrights;
  public static String CopyrightPreferencePage_titleDelete;
  public static String CopyrightPreferencePage_titleModify;
  public static String CopyrightSettingsPage_checkboxAddLicense;
  public static String CopyrightSettingsPage_checkboxReplaceHeaders;
  public static String CopyrightSettingsPage_description;
  public static String CopyrightSettingsPage_excludePattern;
  public static String CopyrightSettingsPage_excludePatternTooltip;
  public static String CopyrightSettingsPage_labelHeader;
  public static String CopyrightSettingsPage_labelLicenseFile;
  public static String CopyrightSettingsPage_includePattern;
  public static String CopyrightSettingsPage_includePatternTooltip;
  public static String CopyrightSettingsPage_labelTypes;
  public static String CopyrightSettingsPage_msgPatternsDescr;
  public static String CopyrightSettingsPage_title;
  public static String FormatsPanel_labelExcludeType;
  public static String FormatsPanel_labelFirstLinePattern;
  public static String HeadersPreferencePage_buttonClear;
  public static String HeadersPreferencePage_checkLineFormat;
  public static String HeadersPreferencePage_checkPreserveFirstLine;
  public static String HeadersPreferencePage_errorInvalidHeaderFormat;
  public static String HeadersPreferencePage_errorTitle;
  public static String HeadersPreferencePage_labelBlankLines;
  public static String HeadersPreferencePage_labelFirstLine;
  public static String HeadersPreferencePage_labelLastLine;
  public static String HeadersPreferencePage_labelLinePrefix;
  public static String ProjectCopyrightPreferencePage_checkboxEnable;
  public static String ProjectCopyrightPreferencePage_errmsgOnSave;
  public static String ProjectCopyrightPreferencePage_errTitle;
  public static String ProjectCopyrightPreferencePage_msgInitialize;
  public static String ProjectCopyrightPreferencePage_tabFormats;
  public static String ProjectSelectionWizardPage_checkboxOverrideFormatsOnly;
  public static String ProjectSelectionWizardPage_checkboxOverrideSettings;
  public static String ProjectSelectionWizardPage_description;
  public static String ProjectSelectionWizardPage_title;
  public static String ResourcesSelectionPage_description;
  public static String ResourcesSelectionPage_noResourcesInfo;
  public static String ResourcesSelectionPage_selectedFileInfo;
  public static String ResourcesSelectionPage_title;
  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  private Messages() {}
}
