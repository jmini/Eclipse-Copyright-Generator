/*******************************************************************************
 * Copyright (c) 2008 Eric Wuillai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Eric Wuillai - initial API and implementation
 ******************************************************************************/
package com.wdev91.eclipse.copyright.model;

public class Copyright {
  protected String label;
  protected String headerText;
  protected String licenseFilename;
  protected String licenseText;

  public Copyright(String label) {
    this.label = label;
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
    this.label = label;
  }

  public void setLicenseFilename(String licenseFilename) {
    this.licenseFilename = licenseFilename;
  }

  public void setLicenseText(String licenseText) {
    this.licenseText = licenseText;
  }
}
