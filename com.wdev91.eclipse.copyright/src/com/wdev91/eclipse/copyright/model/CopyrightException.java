/*******************************************************************************
 * Copyright (c) 2008-2010 Eric Wuillai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Eric Wuillai - initial API and implementation
 ******************************************************************************/
package com.wdev91.eclipse.copyright.model;

/**
 * Exceptions thrown by the copyright operations.
 */
public class CopyrightException extends Exception {
  private static final long serialVersionUID = -6823695001180785001L;

  public CopyrightException(String message) {
    super(message);
  }

  public CopyrightException(String message, Throwable cause) {
    super(message, cause);
  }
}
