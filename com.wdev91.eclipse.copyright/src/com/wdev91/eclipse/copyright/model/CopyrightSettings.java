/*******************************************************************************
 * Copyright (c) 2008-2009 Eric Wuillai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Eric Wuillai - initial API and implementation
 ******************************************************************************/
package com.wdev91.eclipse.copyright.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

public class CopyrightSettings {
  public static final int OVERRIDE_NONE = 0;
  public static final int OVERRIDE_TEXT = 1;
  public static final int OVERRIDE_ALL = 2;

  public static final String DEFAULT_INCLUDE_PATTERN = "*";

  /** Selected projects */
  protected IProject[] projects;
  /** Pattern for resources filtering */
  protected String pattern;
  /** Flag indicating settings having impact on resources selection have changed */
  protected boolean changed;
  /** Files on which to apply the copyright */
  protected IFile[] files;
  /** Flag indicating if existing header comments must be replaced (true) or not (false). */
  protected boolean forceApply;
  /** The copyright definition to apply */
  protected Copyright copyright;
  /** License file name to add in selected projects. */
  protected String licenseFile;
  /** Override project settings flag */
  protected int override;

  public Copyright getCopyright() {
    return copyright;
  }

  public String getLicenseFile() {
    return licenseFile;
  }

  public int getOverride() {
    return override;
  }

  public String getPattern() {
    return pattern != null && pattern.length() > 0 ? pattern : DEFAULT_INCLUDE_PATTERN;
  }

  public IProject[] getProjects() {
    return projects != null ? projects : new IProject[0];
  }

  public IFile[] getFiles() {
    return files;
  }

  public boolean isChanged() {
    return changed;
  }

  public boolean isForceApply() {
    return forceApply;
  }

  public void setChanged(boolean changed) {
    this.changed = changed;
  }

  public void setCopyright(Copyright copyright) {
    this.copyright = copyright;
  }

  public void setForceApply(boolean forceApply) {
    this.forceApply = forceApply;
    this.changed    = true;
  }

  public void setLicenseFile(String licenseFile) {
    this.licenseFile = licenseFile;
  }

  public void setOverride(int override) {
    this.override = override;
    this.changed  = true;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern.trim();
    this.changed = true;
  }

  public void setProjects(IProject[] projects) {
    this.projects = projects;
    this.changed  = true;
  }

  public void setFiles(IFile[] files) {
    this.files = files;
  }
}
