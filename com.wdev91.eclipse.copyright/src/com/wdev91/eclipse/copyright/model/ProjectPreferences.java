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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Project copyright preferences.
 */
public class ProjectPreferences {
  public static final ProjectPreferences NO_PREFS = new ProjectPreferences();

  private String headerText;
  private Map<String, HeaderFormat> formats;

  private ProjectPreferences() {
    headerText = null;
    formats = null;
  }

  public ProjectPreferences(String headerText, Collection<HeaderFormat> formats) {
    this.headerText = headerText;
    this.formats = new HashMap<String, HeaderFormat>(formats.size());
    for (HeaderFormat f : formats) {
      this.formats.put(f.getContentId(), f);
    }
  }

  public String getHeaderText() {
    return headerText;
  }

  public Map<String, HeaderFormat> getFormats() {
    return formats;
  }
}
