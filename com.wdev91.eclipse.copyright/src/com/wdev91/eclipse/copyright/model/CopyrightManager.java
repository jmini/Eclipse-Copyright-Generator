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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.osgi.util.NLS;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wdev91.eclipse.copyright.Activator;
import com.wdev91.eclipse.copyright.Constants;
import com.wdev91.eclipse.copyright.Messages;

public class CopyrightManager {
  public static final Copyright CUSTOM;

  private static final String REPOSITORY_FILENAME = "copyrights.xml"; //$NON-NLS-1$
  private static final String HEADERS_FILENAME = "headers.xml"; //$NON-NLS-1$
  private static final String TAG_CROOT = "copyrights"; //$NON-NLS-1$
  private static final String TAG_COPYRIGHT = "copyright"; //$NON-NLS-1$
  private static final String TAG_HEADER = "header"; //$NON-NLS-1$
  private static final String TAG_LICENSE = "license"; //$NON-NLS-1$
  private static final String ATT_FILENAME = "filename"; //$NON-NLS-1$
  private static final String ATT_LABEL = "label"; //$NON-NLS-1$
  private static final String TAG_HROOT = "headers"; //$NON-NLS-1$
  private static final String ATT_CONTENTID = "contentId"; //$NON-NLS-1$
  private static final String TAG_BEGIN = "beginLine"; //$NON-NLS-1$
  private static final String TAG_PREFIX = "linePrefix"; //$NON-NLS-1$
  private static final String TAG_END = "endLine"; //$NON-NLS-1$
  private static final String ATT_LINEFORMAT = "lineFormat"; //$NON-NLS-1$
  private static final String ATT_PRESERVEFIRST = "preserveFirstLine"; //$NON-NLS-1$

  private static File repository;
  private static File headersFile;
  private static IContentTypeManager contentTypeManager;
  private static IContentType textContentType;
  private static Map<String, HeaderFormat> headerFormats = null;

  static {
    CUSTOM = new Copyright(Messages.CopyrightManager_customLabel);
    CUSTOM.setHeaderText(Constants.EMPTY_STRING);
    CUSTOM.setLicenseFilename(Constants.EMPTY_STRING);
    CUSTOM.setLicenseText(Constants.EMPTY_STRING);

    // Gets the root Text content type
    contentTypeManager = Platform.getContentTypeManager();
    textContentType = contentTypeManager.getContentType(IContentTypeManager.CT_TEXT);
  }

  public static void applyCopyright(CopyrightSettings settings) throws CopyrightException {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(Constants.P_YEAR, Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
    parameters.put(Constants.P_USER, System.getProperty("user.name")); //$NON-NLS-1$
    parameters.put(Constants.P_OWNER,
                   Activator.getDefault()
                            .getPreferenceStore()
                            .getString(Constants.PREFERENCES_OWNER));

    // Apply the header comment on the selected files
    for (IFile file : settings.getFiles()) {
      parameters.put(Constants.P_FILE_NAME, file.getName());
      parameters.put(Constants.P_FILE_ABSPATH, file.getLocation().toString());
      parameters.put(Constants.P_FILE_PATH, file.getProjectRelativePath().toString());
      parameters.put(Constants.P_PROJECT_NAME, file.getProject().getName());
      applyCopyright(file, settings.getCopyright(), parameters);
    }

    // Creates the license files in the selected projects
    try {
      String filename = settings.getLicenseFile();
      if ( filename != null ) {
        for (IProject project : settings.getProjects()) {
          IPath path = project.getLocation();
          if ( path == null ) continue;
          File licenseFile = new File(path.toFile(), filename);
          if ( ! licenseFile.exists() ) {
            writeLicense(licenseFile, settings.getCopyright());
          }
        }
      }
    } catch (Exception e) {
      throw new CopyrightException(Messages.CopyrightManager_err_licenseCreate, e);
    }

    // Refreshes the workspace
    try {
      ResourcesPlugin.getWorkspace()
                     .getRoot()
                     .refreshLocal(IResource.DEPTH_INFINITE, null);
    } catch (CoreException e) {
      e.printStackTrace();
    }
  }

  private static void applyCopyright(IFile file, Copyright copyright,
        Map<String, String> parameters) throws CopyrightException {
    File f = file.getLocation().toFile();
    BufferedReader reader = null;
    StringWriter buffer = null;
    PrintWriter writer = null;
    FileWriter fw = null;
    String line;

    try {
      // Gets the header format for the file's content type
      IContentType ct = getContentType(file);
      if ( ct == null ) return;
      HeaderFormat format = getHeaderFormat(ct);
      String charset = file.getCharset(true);

      reader = new BufferedReader(new InputStreamReader(file.getContents(), charset));
      buffer = new StringWriter(new Long(f.length()).intValue());
      writer = new PrintWriter(buffer);

      // Preserves the first line if defined in the header format
      if ( format.preserveFirstLine ) {
        line = reader.readLine();
        if ( line == null ) return;
        writer.println(line);
      }

      // Writes the copyright header
      BufferedReader header = new BufferedReader(new StringReader(copyright.headerText));
      writer.println(format.beginLine);
      while ( (line = header.readLine()) != null ) {
        writer.println(format.linePrefix + substitute(line, parameters));
      }
      writer.println(format.endLine);

      // Writes the file content, except an optionnaly existing header
      int headerStatus = 0;
      while ( (line = reader.readLine()) != null ) {
        switch ( headerStatus ) {
          case 0 :
//            if ( line.startsWith(format.beginLine.substring(0, Math.min(format.beginLine.length(), 5))) ) {
            if ( line.trim().length() > 0
                 && format.beginLine.startsWith(line.substring(0, Math.min(line.length(), 5)).trim()) ) {
              headerStatus = 1;
            } else {
              writer.println(line);
              headerStatus = 2;
            }
            break;
          case 1 :
            if ( format.lineCommentFormat ) {
//              if ( line.startsWith(format.endLine.substring(0, Math.min(format.beginLine.length(), 5))) ) {
              if ( line.trim().length() > 0
                   && format.endLine.startsWith(line.substring(0, Math.min(line.length(), 5)).trim()) ) {
                headerStatus = 2;
              }
//            } else if ( line.endsWith(format.endLine.substring(format.endLine.length() - 5)) ) {
            } else if ( line.trim().length() > 0
                        && format.endLine.endsWith(line.substring(Math.max(line.length() - 5, 0)).trim()) ) {
              headerStatus = 2;
            }
            break;
          case 2 :
            writer.println(line);
            break;
        }
      }

      // Updates the file content
      file.setContents(new ByteArrayInputStream(buffer.toString().getBytes()),
                       true, true, null);
      if ( ! charset.equals(file.getParent().getDefaultCharset()) ) {
        file.setCharset(charset, null);
      }
      file.refreshLocal(0, null);
    } catch (Exception e) {
      throw new CopyrightException(NLS.bind(Messages.CopyrightManager_err_readContent, file.getName()), e);
    } finally {
      if ( reader != null ) {
        try { reader.close(); } catch (Exception e) {}
      }
      if ( fw != null ) {
        try { fw.close(); } catch (Exception e) {}
      }
    }
  }

  public static Map<String, HeaderFormat> getAllHeadersFormats() {
    if ( headerFormats == null ) {
      loadHeadersFormats();
    }
    Map<String, HeaderFormat> formats = new HashMap<String, HeaderFormat>();
    for (String id : headerFormats.keySet()) {
      formats.put(id, (HeaderFormat) (headerFormats.get(id).clone()));
    }
    return formats;
  }

  public static IContentType getContentType(IFile file) throws CoreException {
    IContentDescription description = file.getContentDescription();
    return description != null
                          ? description.getContentType()
                          : contentTypeManager.findContentTypeFor(file.getName());
  }

  private static File getHeaderFile() throws IOException {
    if ( headersFile == null ) {
      headersFile = Activator.getDefault()
                             .getStateLocation()
                             .append(HEADERS_FILENAME)
                             .toFile();
    }
    return headersFile;
  }

  public static HeaderFormat getHeaderFormat(IContentType contentType) {
    if ( headerFormats == null ) {
      loadHeadersFormats();
    }
    HeaderFormat format = headerFormats.get(contentType.getId());
    if ( format == null ) {
      IContentType parent = contentType.getBaseType();
      while ( parent != null ) {
        format = headerFormats.get(parent.getId());
        if ( format != null ) {
          headerFormats.put(contentType.getId(), format);
          break;
        }
      }
    }
    return format;
  }

  private static File getRepositoryFile() throws IOException {
    if ( repository == null ) {
      repository = Activator.getDefault()
                            .getStateLocation()
                            .append(REPOSITORY_FILENAME)
                            .toFile();
      if ( ! repository.exists() ) {
        loadDefaultFile();
      }
    }
    return repository;
  }

  /**
   * Checks if a given file is valid for selection to apply the copyright.
   * 
   * @param file The file to check
   * @param matchers Extensions patterns matchers array.
   * @param forceApply <code>true</code> if existing header must be overrided.
   * @return <code>true<code> if valid for selection, else <code>false</code>
   * @throws CopyrightException 
   */
  private static boolean isValidFile(IFile file, StringMatcher[] matchers, boolean forceApply) throws CopyrightException {
    BufferedReader reader = null;
    try {
      // Gets the content type of the file
      IContentType ct = getContentType(file);
      if ( ct == null || ! ct.isKindOf(textContentType) ) {
        return false;  // Non text files are rejected
      }

      // Filter by patterns
      boolean patternOk = false;
      for (StringMatcher matcher : matchers) {
        if ( matcher.match(file.getName()) ) {
          patternOk = true;
          break;
        }
      }
      if ( ! patternOk ) return false;

      // Checks if the file already have a header
      if ( ! forceApply ) {
        HeaderFormat format = getHeaderFormat(ct);
        reader = new BufferedReader(new InputStreamReader(file.getContents()));
        String line = reader.readLine();
        if ( line != null && format.preserveFirstLine ) {
          line = reader.readLine();   // Read the 2d line if first line must be preserved
        }
        if ( line == null ) {
          return false;  // Empty file
        }
//        if ( line.startsWith(format.beginLine.substring(0, Math.min(format.beginLine.length(), 5))) ) {
        if ( line.trim().length() > 0
             && format.beginLine.startsWith(line.substring(0, Math.min(line.length(), 5)).trim()) ) {
          return false;  // The file already contains a header
        }
      }
    } catch (Exception e) {
      throw new CopyrightException(NLS.bind(Messages.CopyrightManager_err_validation, file.getName()), e);
    } finally {
      if ( reader != null ) {
        try { reader.close(); } catch (Exception e) {}
      }
    }
    return true;
  }

  public static List<Copyright> listCopyrights(boolean custom) {
    List<Copyright> copyrights = new ArrayList<Copyright>();
    try {
      File xmlFile = getRepositoryFile();
      if ( xmlFile.exists() ) {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(xmlFile);
        Element elt = doc.getDocumentElement();
        NodeList nodes = elt.getElementsByTagName(TAG_COPYRIGHT);
        for (int i = 0; i < nodes.getLength(); i++) {
          elt = (Element) nodes.item(i);
          Copyright c = new Copyright(elt.getAttribute(ATT_LABEL));
          Node n = elt.getElementsByTagName(TAG_HEADER).item(0).getFirstChild();
          if ( n != null && n instanceof CDATASection ) {
            c.setHeaderText(((CDATASection) n).getTextContent());
          }
          n = elt.getElementsByTagName(TAG_LICENSE).item(0);
          c.setLicenseFilename(((Element) n).getAttribute(ATT_FILENAME));
          n = n.getFirstChild();
          if ( n != null && n instanceof CDATASection ) {
            c.setLicenseText(((CDATASection) n).getTextContent());
          }
          copyrights.add(c);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    if ( custom ) {
      copyrights.add(0, CUSTOM);
    }
    return copyrights;
  }

  private static void loadDefaultFile() {
    try {
      byte[] buffer = new byte[2048];
      URL url = Activator.getDefault()
                         .getBundle()
                         .getResource(REPOSITORY_FILENAME);
      InputStream input = url.openStream();
      FileOutputStream output = new FileOutputStream(repository);
      while (true) {
        int read = input.read(buffer);
        if ( read == -1 ) {
          break;
        }
        output.write(buffer, 0, read);
      }
      output.close();
      input.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void loadHeadersFormats() {
    headerFormats = new HashMap<String, HeaderFormat>();
    try {
      File xmlFile = getHeaderFile();
      if ( xmlFile.exists() ) {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(xmlFile);
        Element elt = doc.getDocumentElement();
        NodeList nodes = elt.getElementsByTagName(TAG_HEADER);
        for (int i = 0; i < nodes.getLength(); i++) {
          elt = (Element) nodes.item(i);
          HeaderFormat format = new HeaderFormat(elt.getAttribute(ATT_CONTENTID));
          format.setLineCommentFormat(Boolean.parseBoolean(elt.getAttribute(ATT_LINEFORMAT)));
          format.setPreserveFirstLine(Boolean.parseBoolean(elt.getAttribute(ATT_PRESERVEFIRST)));
          Node n = elt.getElementsByTagName(TAG_BEGIN).item(0).getFirstChild();
          if ( n != null && n instanceof CDATASection ) {
            format.setBeginLine(((CDATASection) n).getTextContent());
          }
          n = elt.getElementsByTagName(TAG_PREFIX).item(0).getFirstChild();
          if ( n != null && n instanceof CDATASection ) {
            format.setLinePrefix(((CDATASection) n).getTextContent());
          }
          n = elt.getElementsByTagName(TAG_END).item(0).getFirstChild();
          if ( n != null && n instanceof CDATASection ) {
            format.setEndLine(((CDATASection) n).getTextContent());
          }
          headerFormats.put(format.getContentId(), format);
        }
        return;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    // If here, no file found. Then load the defaults.
    headerFormats.put(IContentTypeManager.CT_TEXT, HeaderFormat.TEXT_HEADER);
    headerFormats.put(HeaderFormat.CT_XML, HeaderFormat.XML_HEADER);
    if ( contentTypeManager.getContentType(HeaderFormat.CT_JAVA) != null ) {
      headerFormats.put(HeaderFormat.CT_JAVA, HeaderFormat.JAVA_HEADER);
    }
  }

  public static void save(List<Copyright> copyrights) {
    try {
      PrintWriter writer = new PrintWriter(getRepositoryFile());
      writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"); //$NON-NLS-1$
      writer.println('<' + TAG_CROOT + '>');
      for (Copyright cp : copyrights) {
        writer.println("\t<" + TAG_COPYRIGHT + " " + ATT_LABEL + "=\"" + cp.getLabel() + "\">");
        writer.println("\t\t<" + TAG_HEADER + "><![CDATA[" + cp.getHeaderText() + "]]></" + TAG_HEADER + '>');
        writer.println("\t\t<" + TAG_LICENSE + " " + ATT_FILENAME + "=\"" + cp.getLicenseFilename() + "\"><![CDATA[" + cp.getLicenseText() + "]]></" + TAG_LICENSE + '>');
        writer.println("\t</" + TAG_COPYRIGHT + '>');
      }
      writer.println("</" + TAG_CROOT + '>');
      writer.flush();
      writer.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void save(Map<String, HeaderFormat> formats) {
    try {
      PrintWriter writer = new PrintWriter(getHeaderFile());
      writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"); //$NON-NLS-1$
      writer.println('<' + TAG_HROOT + '>');
      for (HeaderFormat format : formats.values()) {
        writer.println("\t<" + TAG_HEADER
                       + " " + ATT_CONTENTID + "=\"" + format.getContentId() + "\" "
                       + ATT_LINEFORMAT + "=\"" + format.isLineCommentFormat() + "\" "
                       + ATT_PRESERVEFIRST + "=\"" + format.isPreserveFirstLine() + "\">");
        writer.println("\t\t<" + TAG_BEGIN + "><![CDATA[" + format.getBeginLine() + "]]></" + TAG_BEGIN + '>');
        writer.println("\t\t<" + TAG_PREFIX + "><![CDATA[" + format.getLinePrefix() + "]]></" + TAG_PREFIX + '>');
        writer.println("\t\t<" + TAG_END + "><![CDATA[" + format.getEndLine() + "]]></" + TAG_END + '>');
        writer.println("\t</" + TAG_HEADER + '>');
      }
      writer.println("</" + TAG_HROOT + '>');
      writer.flush();
      writer.close();
      headerFormats = formats;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static CopyrightSelectionItem[] selectResources(CopyrightSettings settings) throws CopyrightException {
    String[] patterns = settings.getPattern().split(","); //$NON-NLS-1$
    StringMatcher[] matchers = new StringMatcher[patterns.length];
    for (int i = 0; i < patterns.length; i++) {
      matchers[i] = new StringMatcher(patterns[i].trim());
    }

    List<CopyrightSelectionItem> projectsSelection = new ArrayList<CopyrightSelectionItem>(settings.getProjects().length);
    for (IProject project : settings.getProjects()) {
      try {
        CopyrightSelectionItem[] selection = selectResources(project, matchers, settings.forceApply);
        if ( selection.length > 0 ) {
          projectsSelection.add(new CopyrightSelectionItem(project, selection));
        }
      } catch (CoreException e) {
        throw new CopyrightException(Messages.CopyrightManager_err_selection, e);
      }
    }
    return projectsSelection.toArray(new CopyrightSelectionItem[] {});
  }

  private static CopyrightSelectionItem[] selectResources(IContainer parent,
        StringMatcher[] matchers, boolean forceApply) throws CoreException, CopyrightException {
    List<CopyrightSelectionItem> membersSelection = new ArrayList<CopyrightSelectionItem>();
    IResource[] members = parent.members();
    for (IResource member : members) {
      if ( member.getName().startsWith(".") ) continue; //$NON-NLS-1$
      if ( member instanceof IFile && isValidFile((IFile) member, matchers, forceApply) ) {
        membersSelection.add(new CopyrightSelectionItem(member, null));
      } else if ( member instanceof IContainer ) {
        CopyrightSelectionItem[] selection = selectResources((IContainer) member, matchers, forceApply);
        if ( selection.length > 0 ) {
          membersSelection.add(new CopyrightSelectionItem(member, selection));
        }
      }
    }
    return membersSelection.toArray(new CopyrightSelectionItem[] {});
  }

  private static String substitute(String line, Map<String, String> parameters) {
    if ( line == null ) return null;

    StringBuilder buffer = new StringBuilder(line);
    int i = 0, j;
    while ((i = buffer.indexOf("${", i)) >= 0) { //$NON-NLS-1$
      j = buffer.indexOf("}", i + 2); //$NON-NLS-1$
      if ( j > 0 ) {
        String key = buffer.substring(i + 2, j);
        String value = parameters.get(key);
        buffer.replace(i++, j + 1, value != null ? value.toString() : Constants.EMPTY_STRING);
      }
    }
    return buffer.toString();
  }

  private static void writeLicense(File licenseFile, Copyright copyright) throws IOException {
    PrintWriter writer = null;
    try {
      licenseFile.getParentFile().mkdirs();
      writer = new PrintWriter(licenseFile);
      writer.write(copyright.licenseText);
    } finally {
      if ( writer != null ) {
        writer.close();
      }
    }
  }
}
