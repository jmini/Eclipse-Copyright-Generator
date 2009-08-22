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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
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
import java.util.Collection;
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
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wdev91.eclipse.copyright.Activator;
import com.wdev91.eclipse.copyright.Constants;
import com.wdev91.eclipse.copyright.Messages;

/**
 * Central class managing all operations around the copyrights.
 */
public class CopyrightManager {
  /**
   * Class of job used to apply copyright on given settings.
   */
  static class CopyrightJob extends WorkspaceJob {
    CopyrightSettings settings;

    public CopyrightJob(CopyrightSettings settings) {
      super(Messages.CopyrightManager_jobName);
      this.settings = settings;
    }

    @Override
    public IStatus runInWorkspace(IProgressMonitor monitor) {
      try {
        return applyCopyright(settings, monitor);
      } catch (CopyrightException e) {
        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
      }
    }
  }

  public static final Copyright CUSTOM;
  private static final String REPOSITORY_FILENAME = "copyrights.xml"; //$NON-NLS-1$
  private static final String HEADERS_FILENAME = "headers.xml"; //$NON-NLS-1$
  private static final String TAG_CROOT = "copyrights"; //$NON-NLS-1$
  private static final String TAG_COPYRIGHT = "copyright"; //$NON-NLS-1$
  private static final String TAG_HEADER = "header"; //$NON-NLS-1$
  private static final String TAG_LICENSE = "license"; //$NON-NLS-1$
  private static final String TAG_PROOT = "project"; //$NON-NLS-1$
  private static final String ATT_FILENAME = "filename"; //$NON-NLS-1$
  private static final String ATT_LABEL = "label"; //$NON-NLS-1$
  private static final String TAG_HROOT = "headers"; //$NON-NLS-1$
  private static final String ATT_CONTENTID = "contentId"; //$NON-NLS-1$
  private static final String TAG_BEGIN = "beginLine"; //$NON-NLS-1$
  private static final String TAG_PREFIX = "linePrefix"; //$NON-NLS-1$
  private static final String TAG_END = "endLine"; //$NON-NLS-1$
  private static final String ATT_POSTBLANKLINES = "postBlankLines"; //$NON-NLS-1$
  private static final String ATT_LINEFORMAT = "lineFormat"; //$NON-NLS-1$
  private static final String ATT_PRESERVEFIRST = "preserveFirstLine"; //$NON-NLS-1$
  private static final String XML_ENCODING = "UTF-8"; //$NON-NLS-1$
  private static final String ATT_EXCLUDED = "excluded"; //$NON-NLS-1$

  /** File containing the headers definitions. */
  private static File headersFile;
  /** Eclipse content types manager */
  private static IContentTypeManager contentTypeManager;
  /** The default text content type */
  private static IContentType textContentType;

  /** Cache of the headers formats */
  private static Map<String, HeaderFormat> headerFormats = null;
  /** Cache of the projects preferences */
  private static Map<String, ProjectPreferences> projectsPreferences;

  static {
    projectsPreferences = new HashMap<String, ProjectPreferences>();

    CUSTOM = new Copyright(Messages.CopyrightManager_customLabel);
    CUSTOM.setHeaderText(Constants.EMPTY_STRING);
    CUSTOM.setLicenseFilename(Constants.EMPTY_STRING);
    CUSTOM.setLicenseText(Constants.EMPTY_STRING);

    // Gets the root Text content type
    contentTypeManager = Platform.getContentTypeManager();
    textContentType = contentTypeManager.getContentType(IContentTypeManager.CT_TEXT);
  }

  /**
   * Applies copyright based on the given settings, reporting operations to
   * the given monitor.
   * 
   * @param settings settings defining the copyright to apply
   * @param monitor monitor for reporting operations
   * @return Status of the execution
   * @throws CopyrightException
   */
  public static IStatus applyCopyright(final CopyrightSettings settings, IProgressMonitor monitor) throws CopyrightException {
    IStatus retval = Status.OK_STATUS;

    monitor.beginTask(Messages.CopyrightManager_taskName, settings.getFiles().length);
    try {
    	String user = System.getProperty("user.name"); //$NON-NLS-1$
    	String owner = Activator.getDefault()
      												 .getPreferenceStore()
      												 .getString(Constants.PREFERENCES_OWNER);
    	Map<String, String> parameters = new HashMap<String, String>();
      parameters.put(Constants.P_YEAR, Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
      parameters.put(Constants.P_USER, System.getProperty("user.name")); //$NON-NLS-1$
      parameters.put(Constants.P_OWNER,
                     owner.length() > 0 ? owner : user);

      // Creates the license files in the selected projects
      String filename = settings.getLicenseFile();
      if ( filename != null ) {
        try {
          for (IProject project : settings.getProjects()) {
            IPath path = project.getLocation();
            if ( path == null ) continue;
            File licenseFile = new File(path.toFile(), filename);
            if ( ! licenseFile.exists() ) {
              writeLicense(licenseFile, settings.getCopyright());
            }
          }
        } catch (IOException e) {
          throw new CopyrightException(e.getMessage(), e);
        }
      }

      // Apply the header comment on the selected files
      for (IFile file : settings.getFiles()) {
        parameters.put(Constants.P_FILE_NAME, file.getName());
        parameters.put(Constants.P_FILE_ABSPATH, file.getLocation().toString());
        parameters.put(Constants.P_FILE_PATH, file.getProjectRelativePath().toString());
        parameters.put(Constants.P_PROJECT_NAME, file.getProject().getName());
        applyCopyright(file, settings, parameters);
        if ( monitor.isCanceled() ) {
          retval = Status.CANCEL_STATUS;
          break;
        }
        monitor.worked(1);
      }

      // Refreshes the workspace
      try {
        ResourcesPlugin.getWorkspace()
                       .getRoot()
                       .refreshLocal(IResource.DEPTH_INFINITE, null);
      } catch (CoreException e) {
        throw new CopyrightException(e.getMessage(), e);
      }
    } finally {
      monitor.done();
    }

    return retval;
  }

  /**
   * Applies a copyright on the given file. The variables present in the header
   * text are replaced by the given parameters.
   * 
   * @param file
   * @param copyright
   * @param parameters
   * @throws CopyrightException
   */
  private static void applyCopyright(IFile file, final CopyrightSettings settings,
        Map<String, String> parameters) throws CopyrightException {
    File f = file.getLocation().toFile();
    BufferedReader reader = null;
    StringWriter buffer = null;
    PrintWriter writer = null;
    FileWriter fw = null;
    String line;
    boolean firstInstructionFinded = false;

    try {
      // Gets the header format for the file's content type
      IContentType ct = getContentType(file);
      if ( ct == null ) return;
      HeaderFormat format = (settings.getOverride() != CopyrightSettings.OVERRIDE_ALL)
                            ? getHeaderFormat(file.getProject(), ct)
                            : getHeaderFormat(ct);
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

      // Gets the copyright header
      String headerText = null;
      if ( settings.getOverride() == CopyrightSettings.OVERRIDE_NONE ) {
        ProjectPreferences preferences = getProjectPreferences(file.getProject());
        if ( preferences != ProjectPreferences.NO_PREFS ) {
          headerText = preferences.getHeaderText();
        }
      }
      if ( headerText == null ) {
        headerText = settings.getCopyright().headerText;
      }

      // Writes the copyright header
      BufferedReader header = new BufferedReader(new StringReader(headerText));
      writer.println(format.beginLine);
      while ( (line = header.readLine()) != null ) {
        writer.println(format.linePrefix + substitute(line, parameters));
      }
      writer.println(format.endLine);

      // Add the optionnal blank lines
      int bl = format.getPostBlankLines();
      for (int i = 0; i < bl; i++) {
        writer.println(Constants.EMPTY_STRING);
      }

      // Writes the file content, except an optionnaly existing header
      int headerStatus = 0;
      while ( (line = reader.readLine()) != null ) {
        switch ( headerStatus ) {
          case 0 :
            if ( line.trim().length() > 0 ) {
              if ( line.startsWith(format.beginLine.substring(0, Math.min(format.beginLine.length(), 10))) ) {
                headerStatus = 1;
                break;
              } else {
                headerStatus = 2;
                firstInstructionFinded = true;
              }
            } else {
              if ( ! firstInstructionFinded ) {
                break;
              }
            }
            writer.println(line);

            break;
          case 1 :
            if ( format.lineCommentFormat ) {
              if ( line.startsWith(format.endLine.substring(0, Math.min(format.endLine.length(), 10)))
                   || ! line.startsWith(format.linePrefix) ) {
                headerStatus = 2;
              }
            } else if ( line.trim().length() > 0
                        && format.endLine.endsWith(line.substring(Math.max(line.length() - 5, 0)).trim()) ) {
              headerStatus = 2;
            }
            break;
          case 2 :
            if ( ! firstInstructionFinded ) {
              if ( line.trim().length() == 0 ) {
                break;
              } else {
                firstInstructionFinded = true;
              }
            }
            writer.println(line);
            break;
        }
      }

      // Updates the file content
      file.setContents(new ByteArrayInputStream(buffer.toString().getBytes(charset)),
                       true, true, null);
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

  /**
   * Creates a job to apply copyright based on the given settings.
   * 
   * @param settings settings defining the copyright to apply
   */
  public static void applyCopyrightJob(CopyrightSettings settings) {
    Job job = new CopyrightJob(settings);
    job.setRule(ResourcesPlugin.getWorkspace().getRoot());
    job.schedule();
  }

  /**
   * Returns the Eclipse content type for the given file.
   * 
   * @param file
   * @return
   */
  public static IContentType getContentType(IFile file) {
    try {
      IContentDescription description = file.getContentDescription();
      return description != null
             ? description.getContentType()
             : contentTypeManager.findContentTypeFor(file.getName());
    } catch (CoreException e) {
      return null;
    }
  }

  /**
   * Returns the list of default copyrights stored in the plugin, bases on
   * standard open source licenses.
   * 
   * @return List of default copyrights
   */
  public static List<Copyright> getDefaultCopyrights() {
    URL url = Activator.getDefault()
    									 .getBundle()
    									 .getResource(REPOSITORY_FILENAME);
    try {
			return readCopyrights(url.openStream());
		} catch (IOException e) {
			e.printStackTrace();
			return new ArrayList<Copyright>();
		}
  }

  /**
   * Returns the default headers formats. Defaults are used to initialize the
   * Eclipse preferences the first time the plugin is used. They can be
   * restored if needed from the preference page.
   * 
   * @return List of default formats
   */
  public static List<HeaderFormat> getDefaultHeadersFormats() {
    List<HeaderFormat> formats = new ArrayList<HeaderFormat>();

    // If here, no file found. Then load the defaults.
    formats.add((HeaderFormat) HeaderFormat.TEXT_HEADER.clone());
    formats.add((HeaderFormat) HeaderFormat.XML_HEADER.clone());
    if ( contentTypeManager.getContentType(HeaderFormat.CT_JAVA) != null ) {
    	formats.add((HeaderFormat) HeaderFormat.JAVA_HEADER.clone());
    }

    String[] excludedContentIds = new String[] {
    	"org.eclipse.ant.core.antBuildFile",
    	"org.eclipse.jdt.core.JARManifest",
    	"org.eclipse.ui.views.log.log"
    };
    for (String contentId : excludedContentIds) {
    	IContentType contentType = contentTypeManager.getContentType(contentId);
    	if ( contentType != null ) {
    		formats.add(HeaderFormat.createExcluded(contentId));
    	}
    }
    String[] excludedPlugins = new String[] {
      	"org.eclipse.jst.j2ee",
      	"org.eclipse.pde"
    };
    for (String pluginId : excludedPlugins) {
      for (IContentType contentType : contentTypeManager.getAllContentTypes()) {
      	String contentId = contentType.getId();
      	int i = contentId.lastIndexOf('.');
      	if ( contentId.substring(0, i).equals(pluginId) ) {
      		formats.add(HeaderFormat.createExcluded(contentId));
      	}
      }
    }

    return formats;
  }

  /**
   * Returns the file containing the headers definitions. This file is
   * located in the workspace in the state directory of the plugin.
   * 
   * @return
   * @throws IOException
   */
  private static File getHeaderFile() throws IOException {
    if ( headersFile == null ) {
      headersFile = Activator.getDefault()
                             .getStateLocation()
                             .append(HEADERS_FILENAME)
                             .toFile();
    }
    return headersFile;
  }

  /**
   * Returns the header format for the given Eclipse content type.
   * 
   * @param contentType
   * @return
   */
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

  /**
   * Returns the header format defined on a project for a given content type.
   * If no header format can be found on the project, then the format defined
   * at the workspace level is returned.
   * 
   * @param project project
   * @param contentType content type
   * @return format for the content type
   */
  private static HeaderFormat getHeaderFormat(IProject project, IContentType contentType) {
    ProjectPreferences preferences = getProjectPreferences(project);
    HeaderFormat format = null;
    if ( preferences != ProjectPreferences.NO_PREFS ) {
      Map<String, HeaderFormat> projectFormats = preferences.getFormats();
      format = projectFormats.get(contentType.getId());
      if ( format == null ) {
        IContentType parent = contentType.getBaseType();
        while ( parent != null ) {
          format = projectFormats.get(parent.getId());
          if ( format != null ) {
//            projectFormats.put(contentType.getId(), format);
            break;
          }
          parent = parent.getBaseType();
        }
      }
    }
    return format != null ? format : getHeaderFormat(contentType);
  }

  /**
   * Returns a copy of all the headers formats registered in the workspace
   * preferences.
   * All the formats returned are a copy (clone) of the saved ones.
   * 
   * @return A copy of the current list of formats.
   */
  public static Collection<HeaderFormat> getHeadersFormats() {
    if ( headerFormats == null ) {
      loadHeadersFormats();
    }
    List<HeaderFormat> formats = new ArrayList<HeaderFormat>(headerFormats.size());
    for (HeaderFormat f : headerFormats.values()) {
      formats.add((HeaderFormat) f.clone());
    }
    return formats;
  }

  /**
   * Returns the copyright preferences for a project.
   * 
   * @param project project
   * @return Copyright project preferences
   */
  public static ProjectPreferences getProjectPreferences(IProject project) {
    ProjectPreferences preferences = projectsPreferences.get(project.getName());
    if ( preferences == null ) {
      File projectFile = getProjectPreferencesFile(project);
      if ( projectFile.exists() ) {
        try {
          DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
          Document doc = builder.parse(projectFile);
          Element elt = doc.getDocumentElement();
          Node n = elt.getElementsByTagName(TAG_COPYRIGHT).item(0);
          Collection<HeaderFormat> formats = loadHeadersFormats(elt.getElementsByTagName(TAG_HEADER));
          preferences = new ProjectPreferences(n.getTextContent(), formats);
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else {
        preferences = ProjectPreferences.NO_PREFS;
      }
      projectsPreferences.put(project.getName(), preferences);
    }
    return preferences;
  }

  /**
   * Returns project settings file that is (or will be) located in the .settings
   * directory of the project.
   * 
   * @param project
   * @return
   */
  private static File getProjectPreferencesFile(IProject project) {
    File projectSettings = new File(project.getLocation().toFile(), ".settings"); //$NON-NLS-1$
    return new File(projectSettings, Activator.PLUGIN_ID + ".xml"); //$NON-NLS-1$
  }

  /**
   * Returns the File used to store copyrights definitions in the workspace.
   * 
   * @return File
   * @throws IOException
   */
  private static File getRepositoryFile() throws IOException {
  	return Activator.getDefault()
                    .getStateLocation()
                    .append(REPOSITORY_FILENAME)
                    .toFile();
  }

  /**
   * Checks if a given file is valid for selection to apply the copyright.
   * 
   * @param file The file to check
   * @param includeMatchers Extensions patterns matchers array.
   * @param forceApply <code>true</code> if existing header must be overrided.
   * @param override flag indicating if the project copyright settings must be overrided.
   * @return <code>true<code> if valid for selection, else <code>false</code>
   * @throws CopyrightException 
   */
  private static boolean isValidFile(IFile file, StringMatcher[] includeMatchers,
			StringMatcher[] excludeMatchers, CopyrightSettings settings)
			throws CopyrightException {
  	BufferedReader reader = null;
    try {
      // Checks if file is writable
      if ( file.isPhantom() || file.isReadOnly() ) {
        return false;
      }

      // Gets the content type of the file
      IContentType ct = getContentType(file);
      if ( ct == null || ! ct.isKindOf(textContentType) ) {
        return false;  // Non text files are rejected
      }

      // Filters by patterns
      boolean patternOk = false;
      for (StringMatcher incMatcher : includeMatchers) {
        if ( incMatcher.match(file.getName()) ) {
          patternOk = true;
          break;
        }
      }
      if ( ! patternOk ) return false;

      if ( excludeMatchers != null ) {
        for (StringMatcher excMatcher : excludeMatchers) {
          if ( excMatcher.match(file.getName()) ) {
            return false;
          }
        }
      }

      // Checks if the file already have a header
      if ( ! settings.isForceApply() ) {
        HeaderFormat format = (settings.getOverride() != CopyrightSettings.OVERRIDE_ALL)
                              ? getHeaderFormat(file.getProject(), ct)
                              : getHeaderFormat(ct);
        if ( format == null ) {
        	// No format defined for this content type or its parents
        	return false;
        } else if ( format.isExcluded() ) {
        	// Content type excluded from copyright
        	return false;
        }
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
    } catch (CoreException e) {
      return false;
    } catch (IOException e) {
      throw new CopyrightException(NLS.bind(Messages.CopyrightManager_err_validation, file.getName()), e);
    } finally {
      if ( reader != null ) {
        try { reader.close(); } catch (Exception e) {}
      }
    }
    return true;
  }

  /**
   * Returns list of copyrights readed from the XML store file, if it is present
   * in the workspace. If no store file can be found, an empty list is returned.
   * 
   * If the custom parameter is set to true, a CUSTOM copyright entry is added
   * at the beginning of the returned list.
   * 
   * @param custom true to add CUSTOM copyright in the returned list.
   * @return List of copyrights (can be empty).
   */
  public static List<Copyright> listCopyrights(boolean custom) {
    try {
      List<Copyright> copyrights;
      File xmlFile = getRepositoryFile();
      if ( xmlFile.exists() ) {
      	copyrights = readCopyrights(new FileInputStream(xmlFile));
      } else {
      	copyrights = getDefaultCopyrights();
      }
      if ( custom ) {
        copyrights.add(0, CUSTOM);
      }
      return copyrights;
    } catch (Exception e) {
      e.printStackTrace();
      return new ArrayList<Copyright>(0);
    }
  }

  /**
   * Loads in cache the headers formats definitions stored in the headers
   * file of the workspace.
   */
  private static void loadHeadersFormats() {
    headerFormats = new HashMap<String, HeaderFormat>();
    try {
      File xmlFile = getHeaderFile();
      if ( xmlFile.exists() ) {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(xmlFile);
        Collection<HeaderFormat> formats = loadHeadersFormats(doc.getDocumentElement()
                                                                 .getElementsByTagName(TAG_HEADER));
        for (HeaderFormat format : formats) {
          headerFormats.put(format.getContentId(), format);
        }
        return;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    // If here, no file found. Then load the defaults.
    Collection<HeaderFormat> formats = getDefaultHeadersFormats();
    saveFormats(formats);
    for (HeaderFormat format : formats) {
    	headerFormats.put(format.getContentId(), format);
    }
  }

  /**
   * Converts a NodeList of header tags into a collection of HeaderFormat
   * elements.
   * 
   * @param nodes list of XML nodes to read
   * @return Collection of HeaderFormat
   */
  private static Collection<HeaderFormat> loadHeadersFormats(NodeList nodes) {
    ArrayList<HeaderFormat> formats = new ArrayList<HeaderFormat>();
    for (int i = 0; i < nodes.getLength(); i++) {
      Element elt = (Element) nodes.item(i);
      HeaderFormat format = new HeaderFormat(elt.getAttribute(ATT_CONTENTID));
      String excluded = elt.getAttribute(ATT_EXCLUDED);
      if ( "true".equalsIgnoreCase(excluded) ) { //$NON-NLS-1$
      	format.setExcluded(true);
      } else {
        String pbl = elt.getAttribute(ATT_POSTBLANKLINES);
        format.setPostBlankLines(Constants.EMPTY_STRING.equals(pbl) ? 0 : Integer.parseInt(pbl));
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
      }
      formats.add(format);
    }
    return formats;
  }

  /**
   * Reads all the copyrights saved in a given XML stream and returns as a List.
   * 
   * @param source XML source file
   * @return List of copyrights
   */
  private static List<Copyright> readCopyrights(InputStream source) {
    List<Copyright> copyrights = new ArrayList<Copyright>();
    try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(source);
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
		} catch (Exception e) {
			e.printStackTrace();
		}
    return copyrights;
  }

  /**
   * Saves a collection of copyright definitions in the workspace preferences.
   * The definitions are stored in the repository file. All definitions
   * present in the repository are replaced.
   * 
   * @param copyrights
   * @throws CopyrightException 
   */
  public static void saveCopyrights(Collection<Copyright> copyrights) throws CopyrightException {
  	// Verification of the copyrights
  	for (Copyright cp : copyrights) {
    	if ( cp.getHeaderText().length() == 0 ) {
    		throw new CopyrightException(NLS.bind(Messages.CopyrightManager_err_savingHeaderTextMissing, cp.getLabel()));
    	}
    	if ( cp.getLicenseText().length() != 0 && cp.getLicenseFilename().length() == 0 ) {
    		throw new CopyrightException(NLS.bind(Messages.CopyrightManager_err_savingFileNameMissing, cp.getLabel()));
    	} else if ( cp.getLicenseText().length() == 0 && cp.getLicenseFilename().length() != 0 ) {
    		throw new CopyrightException(NLS.bind(Messages.CopyrightManager_err_savingLicenseTextMissing, cp.getLabel()));
    	}
    }

  	// Save of the copyrights in the XML store file
  	try {
  		File xmlFile = getRepositoryFile();
  		if ( ! xmlFile.exists() ) {
  			xmlFile.getParentFile().mkdirs();
  		}

  		PrintWriter writer = new PrintWriter(xmlFile, XML_ENCODING);
      writer.println("<?xml version=\"1.0\" encoding=\"" + XML_ENCODING + "\" ?>");
      writer.println('<' + TAG_CROOT + '>');
      for (Copyright cp : copyrights) {
        writer.println("\t<" + TAG_COPYRIGHT + " " + ATT_LABEL + "=\""
        							 + cp.getLabel() + "\">");
        writer.println("\t\t<" + TAG_HEADER + "><![CDATA[" + stripNonValidXMLCharacters(cp.getHeaderText())
        							 + "]]></" + TAG_HEADER + '>');
        writer.println("\t\t<" + TAG_LICENSE + " " + ATT_FILENAME + "=\""
        							 + cp.getLicenseFilename() + "\"><![CDATA["
        							 + stripNonValidXMLCharacters(cp.getLicenseText()) + "]]></" + TAG_LICENSE + '>');
        writer.println("\t</" + TAG_COPYRIGHT + '>');
      }
      writer.println("</" + TAG_CROOT + '>');
      writer.flush();
      writer.close();
    } catch (Exception e) {
      throw new CopyrightException(Messages.CopyrightManager_err_savingXmlFile + e.getMessage());
    }
  }

  /**
   * Saves a collection of header format definitions in the workspace
   * preferences.
   * Headers definitions are stored in the file headers.xml, placed in the
   * workspace .metadata directory.
   * 
   * @param formats the formats to save
   */
  public static void saveFormats(Collection<HeaderFormat> formats) {
    try {
      PrintWriter writer = new PrintWriter(getHeaderFile(), XML_ENCODING);
      writer.println("<?xml version=\"1.0\" encoding=\"" + XML_ENCODING + "\" ?>");
      writer.println('<' + TAG_HROOT + '>');
      for (HeaderFormat format : formats) {
      	if ( format.isExcluded() ) {
          writer.println("\t<" + TAG_HEADER
          							 + " " + ATT_CONTENTID + "=\"" + format.getContentId() + "\" "
          							 + ATT_EXCLUDED + "=\"true\" />");
      	} else {
          writer.println("\t<" + TAG_HEADER
              					 + " " + ATT_CONTENTID + "=\"" + format.getContentId() + "\" "
              					 + ATT_POSTBLANKLINES + "=\"" + format.getPostBlankLines() + "\" "
              					 + ATT_LINEFORMAT + "=\"" + format.isLineCommentFormat() + "\" "
              					 + ATT_PRESERVEFIRST + "=\"" + format.isPreserveFirstLine() + "\">");
          writer.println("\t\t<" + TAG_BEGIN + "><![CDATA[" + format.getBeginLine()
   											 + "]]></" + TAG_BEGIN + '>');
          writer.println("\t\t<" + TAG_PREFIX + "><![CDATA[" + format.getLinePrefix()
												 + "]]></" + TAG_PREFIX + '>');
          writer.println("\t\t<" + TAG_END + "><![CDATA[" + format.getEndLine()
												 + "]]></" + TAG_END + '>');
          writer.println("\t</" + TAG_HEADER + '>');
      	}
      }
      writer.println("</" + TAG_HROOT + '>');
      writer.flush();
      writer.close();
      if ( headerFormats == null ) {
        headerFormats = new HashMap<String, HeaderFormat>();
      } else {
        headerFormats.clear();
      }
      for (HeaderFormat f : formats) {
        headerFormats.put(f.getContentId(), f);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Saves the copyright preferences for a project. To disable project
   * preferences, the preference parameter must be null. Then if a project
   * preferences file exists, it will be deleted.
   * 
   * @param project project
   * @param preferences project copyright preferences, or null to disable
   * @throws IOException
   * @throws CopyrightException 
   */
  public static void saveProjectPreferences(IProject project, ProjectPreferences preferences)
  throws IOException, CopyrightException {
    File projectFile = getProjectPreferencesFile(project);
    if ( preferences != null ) {
    	if ( preferences.getHeaderText().length() == 0 ) {
    		throw new CopyrightException(Messages.CopyrightManager_err_savingHeaderTextMissingForProject);
    	}

    	if ( ! projectFile.exists() ) {
    		projectFile.getParentFile().mkdirs();
    	}

    	PrintWriter writer = new PrintWriter(projectFile, XML_ENCODING);
      writer.println("<?xml version=\"1.0\" encoding=\"" + XML_ENCODING + "\" ?>");
      writer.println('<' + TAG_PROOT + '>');
      writer.println("\t<" + TAG_COPYRIGHT + "><![CDATA[" + stripNonValidXMLCharacters(preferences.getHeaderText())
      							 + "]]></" + TAG_COPYRIGHT + '>');
      for (HeaderFormat format : preferences.getFormats().values()) {
      	if ( format.isExcluded() ) {
        	writer.println("\t<" + TAG_HEADER
        								 + " " + ATT_CONTENTID + "=\"" + format.getContentId() + "\" "
              					 + ATT_EXCLUDED + "=\"true\" />");
      	} else {
        	writer.println("\t<" + TAG_HEADER
              					 + " " + ATT_CONTENTID + "=\"" + format.getContentId() + "\" "
              					 + ATT_POSTBLANKLINES + "=\"" + format.getPostBlankLines() + "\" "
              					 + ATT_LINEFORMAT + "=\"" + format.isLineCommentFormat() + "\" "
              					 + ATT_PRESERVEFIRST + "=\"" + format.isPreserveFirstLine() + "\">");
        	writer.println("\t\t<" + TAG_BEGIN + "><![CDATA[" + format.getBeginLine()
												 + "]]></" + TAG_BEGIN + '>');
        	writer.println("\t\t<" + TAG_PREFIX + "><![CDATA[" + format.getLinePrefix()
        								 + "]]></" + TAG_PREFIX + '>');
        	writer.println("\t\t<" + TAG_END + "><![CDATA[" + format.getEndLine()
        								 + "]]></" + TAG_END + '>');
        	writer.println("\t</" + TAG_HEADER + '>');
      	}
      }
      writer.println("</" + TAG_PROOT + '>');
      writer.flush();
      writer.close();
    } else if ( projectFile.exists() ) {
      projectFile.delete();
    }
    projectsPreferences.remove(project.getName());
  }

  /**
   * Returns a selection of resources (IFolder and IFile) based on the given
   * copyright settings.
   * 
   * @param settings Copyright settings
   * @return Array of selection items
   * @throws CopyrightException
   */
  public static CopyrightSelectionItem[] selectResources(CopyrightSettings settings,
  		IProgressMonitor monitor) throws CopyrightException {
    String[] includePatterns = settings.getIncludePattern().split(","); //$NON-NLS-1$
    StringMatcher[] includeMatchers = new StringMatcher[includePatterns.length];
    for (int i = 0; i < includePatterns.length; i++) {
      includeMatchers[i] = new StringMatcher(includePatterns[i].trim());
    }

    StringMatcher[] excludeMatchers = null;
    String patterns = settings.getExcludePattern();
    if ( patterns != null ) {
      String[] excludePatterns = patterns.split(","); //$NON-NLS-1$
      excludeMatchers = new StringMatcher[excludePatterns.length];
      for (int i = 0; i < excludePatterns.length; i++) {
        excludeMatchers[i] = new StringMatcher(excludePatterns[i].trim());
      }
    }

    List<CopyrightSelectionItem> projectsSelection = new ArrayList<CopyrightSelectionItem>(settings.getProjects().length);
    for (IProject project : settings.getProjects()) {
      try {
        CopyrightSelectionItem[] selection = selectResources(project, includeMatchers,
        		excludeMatchers, settings, monitor);
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
      StringMatcher[] includeMatchers, StringMatcher[] excludeMatchers,
      CopyrightSettings settings, IProgressMonitor monitor)
  		throws CoreException, CopyrightException {
    List<CopyrightSelectionItem> membersSelection = new ArrayList<CopyrightSelectionItem>();
    IResource[] members = parent.members();
    for (IResource member : members) {
    	monitor.subTask(parent.getName() + " - " + member.getFullPath().toPortableString()); //$NON-NLS-1$
      if ( member.getName().startsWith(".") ) continue; //$NON-NLS-1$
      if ( member instanceof IFile && isValidFile((IFile) member, includeMatchers,
      																						excludeMatchers, settings) ) {
        membersSelection.add(new CopyrightSelectionItem(member, null));
      } else if ( member instanceof IContainer ) {
        CopyrightSelectionItem[] selection = selectResources((IContainer) member,
        		includeMatchers, excludeMatchers, settings, monitor);
        if ( selection.length > 0 ) {
          membersSelection.add(new CopyrightSelectionItem(member, selection));
        }
      }
    }
    return membersSelection.toArray(new CopyrightSelectionItem[] {});
  }

  /**
   * This method ensures that the output String has only valid XML unicode
   * characters as specified by the XML 1.0 standard. For reference, please
   * see
   * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the
   * standard</a>. This method will return an empty String if the input is
   * null or empty.
   *
   * @param in The String whose non-valid characters we want to remove.
   * @return The in String, stripped of non-valid characters.
   */
  private static String stripNonValidXMLCharacters(String in) {
  	StringBuffer out = new StringBuffer(); // Used to hold the output.
  	char current; // Used to reference the current character.

  	if ( in == null || ("".equals(in)) ) return "";
  	for (int i = 0; i < in.length(); i++) {
  		current = in.charAt(i);
  		if ( (current == 0x9) ||
  				 (current == 0xA) ||
  				 (current == 0xD) ||
  				 ((current >= 0x20) && (current <= 0xD7FF)) ||
  				 ((current >= 0xE000) && (current <= 0xFFFD)) ||
  				 ((current >= 0x10000) && (current <= 0x10FFFF)) ) {
  			out.append(current);
  		}
  	}
  	return out.toString();
  }

  /**
   * Makes the variables substitution in a given string.
   * 
   * @param line the string to operate on
   * @param parameters map of variables definitions
   * @return the result string
   */
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

  /**
   * Creates a license file containing the text of the license defined in a
   * copyright.
   * 
   * @param licenseFile the license file to create
   * @param copyright the copyright containing the license text
   * @throws IOException
   */
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
