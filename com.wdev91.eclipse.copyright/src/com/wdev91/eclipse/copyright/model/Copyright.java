/*******************************************************************************
 * Copyright (c) 2008-2011 Eric Wuillai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Eric Wuillai - initial API and implementation
 ******************************************************************************/
package com.wdev91.eclipse.copyright.model;

import com.wdev91.eclipse.copyright.Constants;

/**
 * Copyright definition.
 */
public class Copyright {
  /** Name of the copyright */
  protected String label;
  /** Text to insert as header of files */
  protected String headerText;
  /** File name of the license file */
  protected String licenseFilename = Constants.EMPTY_STRING;
  /** Text content of the license file */
  protected String licenseText = Constants.EMPTY_STRING;

  /**
   * Constructor. Creates a copyright with the given label.
   */
  public Copyright(String label) {
    this.label = label.trim();
  }

  public String getHeaderText() {
    return headerText;
  }

  public String getLabel() {
    return label;
  }

  public String getLicenseFilename() {
    return licenseFilename;
  }

  public String getLicenseText() {
    return licenseText;
  }

  public void setHeaderText(String headerText) {
    this.headerText = headerText;
  }

  public void setLabel(String label) {
    this.label = label.trim();
  }

  public void setLicenseFilename(String licenseFilename) {
    this.licenseFilename = licenseFilename.trim();
  }

  public void setLicenseText(String licenseText) {
    this.licenseText = licenseText;
  }
}
