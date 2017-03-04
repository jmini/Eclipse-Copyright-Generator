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
package com.wdev91.eclipse.copyright.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Project copyright preferences.
 */
public class ProjectPreferences {
  public static final ProjectPreferences NO_PREFS = new ProjectPreferences();

  private String owner = null;
  private String headerText = null;
  private Map<String, HeaderFormat> formats = null;

  public ProjectPreferences() {}

  public Map<String, HeaderFormat> getFormats() {
    return formats;
  }

  public String getHeaderText() {
    return headerText;
  }

  public String getOwner() {
    return owner;
  }

  public boolean isEmpty() {
    return owner == null && headerText == null && formats == null;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public void setHeaderText(String headerText) {
    this.headerText = headerText;
  }

  public void setFormats(Collection<HeaderFormat> formats) {
    this.formats = new HashMap<String, HeaderFormat>(formats.size());
    for (HeaderFormat f : formats) {
      this.formats.put(f.getContentId(), f);
    }
  }
}
