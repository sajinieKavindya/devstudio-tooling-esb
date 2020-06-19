package org.wso2.developerstudio.eclipse.gmf.esb.presentation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.codehaus.jettison.json.JSONException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.FileEditorInput;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.wso2.developerstudio.eclipse.esb.project.artifact.ESBArtifact;
import org.wso2.developerstudio.eclipse.esb.project.artifact.ESBProjectArtifact;
import org.wso2.developerstudio.eclipse.gmf.esb.impl.CloudConnectorOperationImpl;
import org.wso2.developerstudio.eclipse.gmf.esb.persistence.Activator;
import org.wso2.developerstudio.eclipse.gmf.esb.presentation.desc.parser.ConnectorConnectionRoot;
import org.wso2.developerstudio.eclipse.gmf.esb.presentation.desc.parser.ConnectorDescriptorParser;
import org.wso2.developerstudio.eclipse.gmf.esb.presentation.desc.parser.ConnectorOperationRoot;
import org.wso2.developerstudio.eclipse.logging.core.IDeveloperStudioLog;
import org.wso2.developerstudio.eclipse.logging.core.Logger;
import org.wso2.developerstudio.eclipse.maven.util.MavenUtils;
import org.xml.sax.SAXException;
import org.eclipse.swt.layout.GridLayout;

public class EEFPropertyViewUtil {

    Composite view;
    static Properties properties = null;
    private static final String TYPE_TEMPLATE_SEQ = "synapse/sequenceTemplate";
    private static final String TYPE_TEMPLATE_EPT = "synapse/endpointTemplate";
    private static final String LOCAL_ENTRIES_DIR_NAME = "local-entries";
    private static final String AVAILABLE_TEMPLATE_LIST_DEFAULT_VALUE = "Select From Templates";
    private static IDeveloperStudioLog log = Logger.getLog(Activator.PLUGIN_ID);
    public static final String PLUGIN_ID = "org.wso2.developerstudio.eclipse.gmf.esb.edit";
    public static final String WSO2_ESB_LOCAL_ENTRY_VERSION="2.1.0";
    private static final String LOCAL_ENTRY_LOCATION = File.separator + "src" + File.separator + "main" + File.separator
            + "synapse-config" + File.separator + "local-entries";
    private static final String XML_EXTENSION = ".xml";
    private static final String CONNECTION_LOCAL_ENTRY_TYPE_XPATH = "//*[local-name()='connectionType']/text()";

    static {
        URL url;
        try {
            url = new URL("platform:/plugin/org.wso2.developerstudio.eclipse.gmf.esb.edit/src-gen/org/wso2/developerstudio/eclipse/gmf/esb/presentation/helpcontent.properties");
            InputStream inputStream = url.openConnection().getInputStream();
            properties = new Properties();
            properties.load(inputStream);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public EEFPropertyViewUtil(Composite view) {
        this.view = view;
    }

    public void clearElements(Composite[] propertiesGroups) {
        for (Composite propertiesGroup : propertiesGroups) {
            hideEntry(propertiesGroup.getChildren(), false);
        }
    }

    public void clearTableButtons(Control[] controls) {
        for (Control control : ((Composite)controls[0]).getChildren()) {
          if (control instanceof org.eclipse.swt.widgets.Button) {
              control.setVisible(false);
          }
        }
    }

    public void clearElement(Control control) {
        if (control.getLayoutData() != null && control.getLayoutData() instanceof GridData) {
            ((GridData) control.getLayoutData()).exclude = true;
            control.setVisible(false);
        }
    }

    public void hideEntry(Control controls[], boolean layout) {
        // view.getChildren();
        for (Control control : controls) {
            // null check and type check
            // control.setLayoutData( new GridData(GridData.FILL_HORIZONTAL));
            control.setEnabled(true);
            if (control.getLayoutData() != null && control.getLayoutData() instanceof GridData) {
                ((GridData) control.getLayoutData()).exclude = true;
                control.setVisible(false);
            } else if (control.getLayoutData() != null && control.getLayoutData() instanceof FormData) {
                // ((FormData) control.getLayoutData()). = true;
                control.setVisible(false);
            } else {
                control.setLayoutData(new GridData());
                ((GridData) control.getLayoutData()).exclude = true;
                control.setVisible(false);
            }
        }
        if (layout) {
            view.layout(true, true);
        }
    }

    public void showEntry(Control controls[], boolean layout) {
        for (Control control : controls) {
            // null check and type check
            // control.setLayoutData( new GridData(GridData.FILL_HORIZONTAL));
            control.setEnabled(true);
            if (control.getLayoutData() != null && control.getLayoutData() instanceof GridData) {
                ((GridData) control.getLayoutData()).exclude = false;
                control.setVisible(true);
            } else if (control.getLayoutData() != null && control.getLayoutData() instanceof FormData) {
                control.setVisible(true);
            } else {
                control.setLayoutData(new GridData());
                ((GridData) control.getLayoutData()).exclude = false;
                control.setVisible(true);
            }
        }
        if (layout) {
            view.layout(true, true);
        }
    }
    
    public void showEntry(ArrayList<Control> controls, boolean layout) {
        for (Control control : controls) {
            // null check and type check
            // control.setLayoutData( new GridData(GridData.FILL_HORIZONTAL));
            control.setEnabled(true);
            if (control.getLayoutData() != null && control.getLayoutData() instanceof GridData) {
                ((GridData) control.getLayoutData()).exclude = false;
                control.setVisible(true);
            } else if (control.getLayoutData() != null && control.getLayoutData() instanceof FormData) {
                control.setVisible(true);
            } else {
                control.setLayoutData(new GridData());
                ((GridData) control.getLayoutData()).exclude = false;
                control.setVisible(true);
            }
        }
        if (layout) {
            view.layout(true, true);
        }
    }

    public static Control[] getTableElements(Control[] previousControls, Control[] newControls) {
        Control[] tableElements = new Control[newControls.length - previousControls.length];
        for (int i = previousControls.length; i < newControls.length; i++) {
            tableElements[i - previousControls.length] = newControls[i];
        }
        return tableElements;
    }
    
    public static void addTableElementsAsList(ArrayList<Control> arrayList, Control[] previousControls, Control[] newControls) {
/*        Control [] controlArray = getTableElements(previousControls, newControls);
        for(Control control : controlArray) {
            arrayList.add(control);
        }*/
        
        //Control[] tableElements = new Control[newControls.length - previousControls.length];
        for (int i = previousControls.length; i < newControls.length; i++) {
            //tableElements[i - previousControls.length] = newControls[i];
            arrayList.add(newControls[i]);
        }
    }
    
    // This method will return a subSection group that can be use to group section inside a eef form
    public static Composite createSubsectionGroup(FormToolkit widgetFactory, final Composite parent, String name, boolean expanded) {
        int style = Section.TITLE_BAR | Section.TWISTIE;
        if(expanded) {
            style = Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED;
        }
        Section propertiesSection = widgetFactory.createSection(parent, style);
        propertiesSection.setText(name);
        GridData propertiesSectionData = new GridData(GridData.FILL_HORIZONTAL);
        propertiesSectionData.horizontalSpan = 3;
        propertiesSection.setLayoutData(propertiesSectionData);
        Composite subsectionGroup = widgetFactory.createComposite(propertiesSection);
        GridLayout propertiesGroupLayout = new GridLayout();
        propertiesGroupLayout.numColumns = 3;
        subsectionGroup.setLayout(propertiesGroupLayout);
        propertiesSection.setClient(subsectionGroup);
        return subsectionGroup;
    }
    
    
    public static Composite createNewGroup(FormToolkit widgetFactory, final Composite parent, String name) {
        Group propertiesSection = new Group(parent, SWT.FILL);
        propertiesSection.setText(name);
        GridLayout propertiesGroupLayout = new GridLayout();
        propertiesGroupLayout.numColumns = 3;
        propertiesGroupLayout.marginLeft = 15;
        propertiesGroupLayout.horizontalSpacing = 20;
        propertiesGroupLayout.verticalSpacing = 10;
        propertiesSection.setLayout(propertiesGroupLayout);
        GridData propertiesSectionData = new GridData(GridData.FILL_HORIZONTAL);
        propertiesSectionData.horizontalSpan = 3;
        propertiesSection.setLayoutData(propertiesSectionData);
        return propertiesSection;
    }
    
    /**
     * This method will check if a specific key combination matches a defined
     * set of key combinations.
     * 
     * @param e KeyEvent instance.
     * @return 'True' if matches, 'False' otherwise.
     */
    public static boolean isReservedKeyCombination(KeyEvent e) {
        
        if (e.keyCode == SWT.CR) {
            return true;
        } else if (e.keyCode == SWT.COMMAND) {
            return true;
        } else if (e.keyCode == SWT.CTRL) {
            return true;
        } else if (e.keyCode == SWT.ALT) {
            return true;
        } else if (e.keyCode == SWT.SHIFT) {
            return true;
        } else if (e.keyCode == SWT.ESC) {
            return true;
        } else if (e.stateMask == SWT.COMMAND) {
            return true;
        } else if (e.stateMask == SWT.ALT && e.keyCode != SWT.TAB) {
        	return true;
        } else if (e.stateMask == SWT.CTRL && e.keyCode != 118) {//not returning true for Ctrl+V
            return true;
        }
        
        return false;
    }

    /**
     * This returns help content assigned for each property (in helpContent.properties file)
     * @param key property key
     * @return property help content
     */
    public static String getHelpContent(Object key) {
        String helpContent = "";
        if(key instanceof String) {
            //replacing :: to - since colon is a reserved char
            helpContent = ((String)key).replaceAll("::", "-");
        }
        return properties.getProperty(helpContent);
    }

    public static ArrayList<String> getAvailableTemplateList() {
        ArrayList<String> definedTemplates = new ArrayList<String>();
        definedTemplates.add(AVAILABLE_TEMPLATE_LIST_DEFAULT_VALUE);
        File projectPath = null;
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (IProject activeProject : projects) {
            if (activeProject != null) {
                try {
                    if (activeProject.hasNature("org.wso2.developerstudio.eclipse.esb.project.nature")) {
                        ESBProjectArtifact esbProjectArtifact = new ESBProjectArtifact();
                        projectPath = activeProject.getLocation().toFile();
                        try {
                            esbProjectArtifact.fromFile(activeProject.getFile("artifact.xml").getLocation().toFile());
                            List<ESBArtifact> allESBArtifacts = esbProjectArtifact.getAllESBArtifacts();
                            for (ESBArtifact esbArtifact : allESBArtifacts) {
                                if (TYPE_TEMPLATE_SEQ.equals(esbArtifact.getType())) {
                                    File artifact = new File(projectPath, esbArtifact.getFile());
                                    definedTemplates.add(artifact.getName().replaceAll("[.]xml$", ""));
                                } else if (TYPE_TEMPLATE_EPT.equals(esbArtifact.getType())) {
                                    File artifact = new File(projectPath, esbArtifact.getFile());
                                    definedTemplates.add(artifact.getName().replaceAll("[.]xml$", ""));
                                }
                            }
                        } catch (Exception e) {
                            log.error("Error occured while scanning the project for artifacts", e);
                        }
                    }
                } catch (CoreException e) {
                    log.error("Error occured while scanning the project", e);
                }
            }
        }
        return definedTemplates;
    }

    public static String spaceFormat(String str) {
        int maxLength = 100;
        int tabSpace = (maxLength - str.length()) / 4;
        for (int i = 0; i < tabSpace; i++) {
            str = str.concat("\t");
        }
        return str;
    }
    
    public static String getIconPath(String iconName) throws URISyntaxException, IOException {
        Bundle bundle = Platform.getBundle(PLUGIN_ID);
        URL webAppURL = bundle.getEntry(iconName);
        URL resolvedFolderURL = FileLocator.toFileURL(webAppURL);
        URI resolvedFolderURI = new URI(resolvedFolderURL.getProtocol(), resolvedFolderURL.getPath(), null);
        File resolvedWebAppFolder = new File(resolvedFolderURI);
        return resolvedWebAppFolder.getAbsolutePath();
    }

    /**
     * This method removes connector palettes from editor if the connector is not in the workspace.
     * 
     * @param editorPart editor from which palettes should be removed
     * @param esbPaletteFactory PaletteFactory of the editor
     */
    public static void loadConnectorSchemas() {
        String connectorDirectory = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + File.separator
                + ".metadata" + File.separator + ".Connectors";
        File directory = new File(connectorDirectory);
        if (directory.isDirectory()) {
            File[] children = directory.listFiles();
            for (int childIndex = 0; childIndex < children.length; ++childIndex) {
                if (children[childIndex].isDirectory()) {
                    String jsonSchemaDirPath = children[childIndex].getAbsolutePath() + File.separator + "uischema";
                    File jsonSchemaDir = new File(jsonSchemaDirPath);
                    if (jsonSchemaDir.isDirectory()) {
                        File[] jsonSchemaChildren = jsonSchemaDir.listFiles();
                        for (int jsonSchemaIndex = 0; jsonSchemaIndex < jsonSchemaChildren.length; ++jsonSchemaIndex) {
                            String jsonSchemaName = jsonSchemaChildren[jsonSchemaIndex].getName();
                            if (jsonSchemaName.endsWith(".json")) {
                                addConnectorRoot(jsonSchemaChildren[jsonSchemaIndex]);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void addConnectorRoot(File jsonSchemafile) {
        String content = "";
        ConnectorSchemaHolder schemaHolder = ConnectorSchemaHolder.getInstance();

        try {
            content = new String(Files.readAllBytes(Paths.get(jsonSchemafile.getAbsolutePath())));

            if (ConnectorDescriptorParser.isConnectorConnection(content)) {
                ConnectorConnectionRoot connectorConnectionRoot = ConnectorDescriptorParser
                        .parseConnectionRoot(content);
                String schemaName = connectorConnectionRoot.getConnectorName() + '-'
                        + connectorConnectionRoot.getConnectionName();
                schemaHolder.putConnectorConnectionSchema(schemaName, connectorConnectionRoot);
            } else {
                ConnectorOperationRoot connectorOperationRoot = ConnectorDescriptorParser.parseOperationRoot(content);
                String schemaName = connectorOperationRoot.getConnectorName() + '-'
                        + connectorOperationRoot.getOperationName();
                schemaHolder.putConnectorOperationSchema(schemaName, connectorOperationRoot);
            }
        } catch (IOException | JSONException e) {
            // log.error("Unable to parse the Connector UI descriptor file", e);
            e.printStackTrace();
        }
    }
    public static String generateSchemaName(IPropertiesEditionComponent propertiesEditionComponent) {
        CloudConnectorOperationImpl connectorObject = (CloudConnectorOperationImpl)propertiesEditionComponent.getEditingContext().getEObject();
        String schemaName = connectorObject.getConnectorName().split("connector")[0] + "-" + connectorObject.getOperationName();
        return schemaName;
    }

    public static ArrayList<String> getAvailableConnectionEntriesList(List<String> allowedConnectionTypes) throws CoreException {
        ArrayList<String> definedTemplates = new ArrayList<String>();
        IFolder localEntriesDir = getLocalEntriesDir();
        for(IResource resource:localEntriesDir.members()) {
            if (resource instanceof IFile && ((IFile)resource).getFileExtension().equals("xml")) {
                try {
                    if(isAllowedConnection(((IFile)resource), allowedConnectionTypes)) {
                        definedTemplates.add(((IFile)resource).getName().split(XML_EXTENSION)[0]);
                    }
                } catch (XPathExpressionException e) {
                    log.error("Connection Local Entry file is not well structured", e);
                } catch (SAXException | ParserConfigurationException e) {
                    log.error("Cannot parse Connection Local Entry file", e);
                } catch (IOException e) {
                    log.error("Cannot read Connection Local Entry file", e);
                }
            }
        }
        return definedTemplates;
    }

    public static boolean isAllowedConnection(IFile file, List<String> allowedTypes) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException, CoreException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file.getContents());
        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
        XPathExpression expr = xpath.compile(CONNECTION_LOCAL_ENTRY_TYPE_XPATH);
        Object result = expr.evaluate(doc, XPathConstants.STRING);
        boolean valid = false;
        if(allowedTypes.contains(result)) {
            valid = true;
        }
        return valid;
    }

    public static IFolder getLocalEntriesDir() {
        IEditorPart editorPart = null;
        IFolder localEntriesDir = null;
        IEditorReference editorReferences[] = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .getEditorReferences();
        for (int i = 0; i < editorReferences.length; i++) {
            IEditorPart editor = editorReferences[i].getEditor(false);

            if (editor != null) {
                editorPart = editor.getSite().getWorkbenchWindow().getActivePage().getActiveEditor();
            }

            if (editorPart != null) {
                IEditorInput input =
                        editorPart == null ? null : editorPart.getEditorInput();
                IPath path = input instanceof FileEditorInput
                        ? ((FileEditorInput)input).getPath()
                        : null;
                        //Do nature validation
                //String localEntriesPath = ((FileEditorInput)input).getFile().getParent().getParent().getFolder(new Path(LOCAL_ENTRIES_DIR_NAME)).getLocation().toOSString();
                localEntriesDir = ((FileEditorInput)input).getFile().getParent().getParent().getFolder(new Path(LOCAL_ENTRIES_DIR_NAME));
            }
        }
        return localEntriesDir;
    }

    public static void updateArtifact(HashMap<String, Control> generatedElements, IProject currentProject) throws FactoryConfigurationError, Exception {
        Text connectionNameText = (Text)generatedElements.get("connectionName");
        String localEntryName =  connectionNameText.getText();
        MavenProject mvp = EEFPropertyViewUtil.updatePom(currentProject);
        ESBProjectArtifact esbProjectArtifact = new ESBProjectArtifact();
        esbProjectArtifact.fromFile(currentProject.getFile("artifact.xml").getLocation().toFile());
        ESBArtifact artifact = new ESBArtifact();
        artifact.setName(localEntryName);
        artifact.setVersion(mvp.getVersion());
        artifact.setType("synapse/local-entry");
        artifact.setServerRole("EnterpriseServiceBus");
        artifact.setGroupId(mvp.getGroupId());
        artifact.setFile(LOCAL_ENTRY_LOCATION
                + File.separator + localEntryName + XML_EXTENSION);
        esbProjectArtifact.addESBArtifact(artifact);
        esbProjectArtifact.toFile();
        currentProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
    }

    public static MavenProject updatePom(IProject esbProject) throws IOException, XmlPullParserException {
        File mavenProjectPomLocation = esbProject.getFile("pom.xml").getLocation().toFile();
        MavenProject mavenProject = MavenUtils.getMavenProject(mavenProjectPomLocation);
        String version = mavenProject.getVersion();

        // Skip changing the pom file if group ID and artifact ID are matched
        if (MavenUtils.checkOldPluginEntry(mavenProject, "org.wso2.maven", "wso2-esb-localentry-plugin")) {
            return mavenProject;
        }

        Plugin plugin = MavenUtils.createPluginEntry(mavenProject, "org.wso2.maven", "wso2-esb-localentry-plugin",
                WSO2_ESB_LOCAL_ENTRY_VERSION, true);
        PluginExecution pluginExecution = new PluginExecution();
        pluginExecution.addGoal("pom-gen");
        pluginExecution.setPhase("process-resources");
        pluginExecution.setId("localentry");

        Xpp3Dom configurationNode = MavenUtils.createMainConfigurationNode();
        Xpp3Dom artifactLocationNode = MavenUtils.createXpp3Node(configurationNode, "artifactLocation");
        artifactLocationNode.setValue(".");
        Xpp3Dom typeListNode = MavenUtils.createXpp3Node(configurationNode, "typeList");
        typeListNode.setValue("${artifact.types}");
        pluginExecution.setConfiguration(configurationNode);
        plugin.addExecution(pluginExecution);
        MavenUtils.saveMavenProject(mavenProject, mavenProjectPomLocation);
        return mavenProject;
    }
}
