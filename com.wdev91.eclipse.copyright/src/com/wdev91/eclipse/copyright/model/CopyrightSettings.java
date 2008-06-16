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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

public class CopyrightSettings {
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

  public Copyright getCopyright() {
    return copyright;
  }

  public String getLicenseFile() {
    return licenseFile;
  }

  public String getPattern() {
    return pattern;
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
    this.changed  = true;
  }

  public void setLicenseFile(String licenseFile) {
    this.licenseFile = licenseFile;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
    this.changed  = true;
  }

  public void setProjects(IProject[] projects) {
    this.projects = projects;
    this.changed  = true;
  }

  public void setFiles(IFile[] files) {
    this.files = files;
  }
}
