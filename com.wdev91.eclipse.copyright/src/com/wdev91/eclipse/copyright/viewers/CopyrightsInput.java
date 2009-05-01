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
package com.wdev91.eclipse.copyright.viewers;

import java.util.List;

import com.wdev91.eclipse.copyright.model.Copyright;
import com.wdev91.eclipse.copyright.model.CopyrightManager;

public class CopyrightsInput {
  protected List<Copyright> copyrights;

  public CopyrightsInput(boolean addCustom) {
    copyrights = CopyrightManager.listCopyrights(addCustom);
  }

  public boolean addCopyright(Copyright copyright) {
    for (Copyright c : copyrights) {
      if ( c.getLabel().equalsIgnoreCase(copyright.getLabel()) ) {
        return false;
      }
    }
    copyrights.add(copyright);
    return true;
  }

  public boolean deleteCopyright(Copyright copyright) {
    return copyrights.remove(copyright);
  }

  public Copyright[] getCopyrights() {
    return copyrights.toArray(new Copyright[] {});
  }

  public void save() {
    CopyrightManager.saveCopyrights(copyrights);
  }
}
