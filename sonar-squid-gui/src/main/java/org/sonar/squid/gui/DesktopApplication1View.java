/*
 * DesktopApplication1View.java
 */
package org.sonar.squid.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.jdesktop.application.Action;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;
import org.sonar.squid.Squid;
import org.sonar.squid.api.SourceClass;
import org.sonar.squid.api.SquidConfiguration;
import org.sonar.squid.api.SourceFile;
import org.sonar.squid.api.SourceMethod;
import org.sonar.squid.api.SourcePackage;
import org.sonar.squid.api.SourceProject;
import org.sonar.squid.api.SourceCode;
import org.sonar.squid.ast.JavaAstScanner;
import org.sonar.squid.measures.Metric;

/**
 * The application's main frame.
 */
public class DesktopApplication1View extends FrameView {

  public DesktopApplication1View(SingleFrameApplication app) {
    super(app);

    initComponents();

    initDefaultjTree();

    // status bar initialization - message timeout, idle icon and busy animation, etc
    ResourceMap resourceMap = getResourceMap();
    int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
    messageTimer = new Timer(messageTimeout, new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        statusMessageLabel.setText("");
      }
    });
    messageTimer.setRepeats(false);
    int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
    for (int i = 0; i < busyIcons.length; i++) {
      busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
    }
    busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
        statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
      }
    });
    idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
    statusAnimationLabel.setIcon(idleIcon);
    progressBar.setVisible(false);

    // connecting action tasks to status bar via TaskMonitor
    TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
    taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if ("started".equals(propertyName)) {
          if (!busyIconTimer.isRunning()) {
            statusAnimationLabel.setIcon(busyIcons[0]);
            busyIconIndex = 0;
            busyIconTimer.start();
          }
          progressBar.setVisible(true);
          progressBar.setIndeterminate(true);
        } else if ("done".equals(propertyName)) {
          busyIconTimer.stop();
          statusAnimationLabel.setIcon(idleIcon);
          progressBar.setVisible(false);
          progressBar.setValue(0);
        } else if ("message".equals(propertyName)) {
          String text = (String) (evt.getNewValue());
          statusMessageLabel.setText((text == null) ? "" : text);
          messageTimer.restart();
        } else if ("progress".equals(propertyName)) {
          int value = (Integer) (evt.getNewValue());
          progressBar.setVisible(true);
          progressBar.setIndeterminate(false);
          progressBar.setValue(value);
        }
      }
    });
  }

  @Action
  public void showAboutBox() {
    if (aboutBox == null) {
      JFrame mainFrame = SonarSquidApplication.getApplication().getMainFrame();
      aboutBox = new DesktopApplication1AboutBox(mainFrame);
      aboutBox.setLocationRelativeTo(mainFrame);
    }
    SonarSquidApplication.getApplication().show(aboutBox);
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method
   * is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    mainPanel = new javax.swing.JPanel();
    jSplitPane1 = new javax.swing.JSplitPane();
    jScrollPane1 = new javax.swing.JScrollPane();
    jTree1 = new javax.swing.JTree();
    jTabbedPane1 = new javax.swing.JTabbedPane();
    jScrollPane2 = new javax.swing.JScrollPane();
    jDashboardTextArea = new javax.swing.JTextArea();
    jScrollPane3 = new javax.swing.JScrollPane();
    jTextAreaMostAndLess = new javax.swing.JTextArea();
    menuBar = new javax.swing.JMenuBar();
    javax.swing.JMenu fileMenu = new javax.swing.JMenu();
    jMenuItem1 = new javax.swing.JMenuItem();
    javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
    javax.swing.JMenu helpMenu = new javax.swing.JMenu();
    javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
    statusPanel = new javax.swing.JPanel();
    javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
    statusMessageLabel = new javax.swing.JLabel();
    statusAnimationLabel = new javax.swing.JLabel();
    progressBar = new javax.swing.JProgressBar();

    mainPanel.setName("mainPanel"); // NOI18N

    jSplitPane1.setName("jSplitPane1"); // NOI18N

    jScrollPane1.setName("jScrollPane1"); // NOI18N

    jTree1.setName("jTree1"); // NOI18N
    jTree1.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
      public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
        jTree1ValueChanged(evt);
        jTree1ValueChangedMostAndLess(evt);
      }
    });
    jScrollPane1.setViewportView(jTree1);

    jSplitPane1.setLeftComponent(jScrollPane1);

    jTabbedPane1.setName("jTabbedPane1"); // NOI18N

    jScrollPane2.setName("jScrollPane2"); // NOI18N

    jDashboardTextArea.setColumns(20);
    jDashboardTextArea.setRows(5);
    jDashboardTextArea.setName("jDashboardTextArea"); // NOI18N
    jScrollPane2.setViewportView(jDashboardTextArea);

    org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.sonar.squid.gui.SonarSquidApplication.class).getContext().getResourceMap(DesktopApplication1View.class);
    jTabbedPane1.addTab(resourceMap.getString("jScrollPane2.TabConstraints.tabTitle"), jScrollPane2); // NOI18N

    jScrollPane3.setName("jScrollPane3"); // NOI18N

    jTextAreaMostAndLess.setColumns(20);
    jTextAreaMostAndLess.setRows(5);
    jTextAreaMostAndLess.setName("jTextAreaMostAndLess"); // NOI18N
    jScrollPane3.setViewportView(jTextAreaMostAndLess);

    jTabbedPane1.addTab(resourceMap.getString("jScrollPane3.TabConstraints.tabTitle"), jScrollPane3); // NOI18N

    jSplitPane1.setRightComponent(jTabbedPane1);

    org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
    mainPanel.setLayout(mainPanelLayout);
    mainPanelLayout.setHorizontalGroup(
      mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
      .add(mainPanelLayout.createSequentialGroup()
        .addContainerGap()
        .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 596, Short.MAX_VALUE)
        .addContainerGap())
    );
    mainPanelLayout.setVerticalGroup(
      mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
      .add(mainPanelLayout.createSequentialGroup()
        .addContainerGap()
        .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE)
        .addContainerGap())
    );

    menuBar.setName("menuBar"); // NOI18N

    fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
    fileMenu.setName("fileMenu"); // NOI18N

    javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(org.sonar.squid.gui.SonarSquidApplication.class).getContext().getActionMap(DesktopApplication1View.class, this);
    jMenuItem1.setAction(actionMap.get("load")); // NOI18N
    jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
    jMenuItem1.setName("jMenuItem1"); // NOI18N
    fileMenu.add(jMenuItem1);

    exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
    exitMenuItem.setName("exitMenuItem"); // NOI18N
    fileMenu.add(exitMenuItem);

    menuBar.add(fileMenu);

    helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
    helpMenu.setName("helpMenu"); // NOI18N

    aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
    aboutMenuItem.setName("aboutMenuItem"); // NOI18N
    helpMenu.add(aboutMenuItem);

    menuBar.add(helpMenu);

    statusPanel.setName("statusPanel"); // NOI18N

    statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

    statusMessageLabel.setName("statusMessageLabel"); // NOI18N

    statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

    progressBar.setName("progressBar"); // NOI18N

    org.jdesktop.layout.GroupLayout statusPanelLayout = new org.jdesktop.layout.GroupLayout(statusPanel);
    statusPanel.setLayout(statusPanelLayout);
    statusPanelLayout.setHorizontalGroup(
      statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
      .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 616, Short.MAX_VALUE)
      .add(statusPanelLayout.createSequentialGroup()
        .addContainerGap()
        .add(statusMessageLabel)
        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 446, Short.MAX_VALUE)
        .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
        .add(statusAnimationLabel)
        .addContainerGap())
    );
    statusPanelLayout.setVerticalGroup(
      statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
      .add(statusPanelLayout.createSequentialGroup()
        .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
          .add(statusMessageLabel)
          .add(statusAnimationLabel)
          .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        .add(3, 3, 3))
    );

    setComponent(mainPanel);
    setMenuBar(menuBar);
    setStatusBar(statusPanel);
  }// </editor-fold>//GEN-END:initComponents

  private void initDefaultjTree() {
    DefaultMutableTreeNode project = new DefaultMutableTreeNode("Projetc");
    DefaultMutableTreeNode packages = new DefaultMutableTreeNode("Packages");
    DefaultMutableTreeNode files = new DefaultMutableTreeNode("Files");
    DefaultMutableTreeNode classes = new DefaultMutableTreeNode("Classes");
    DefaultMutableTreeNode methods = new DefaultMutableTreeNode("Methods");
    classes.add(methods);
    files.add(classes);
    packages.add(files);
    project.add(packages);
    jTree1.setModel(new DefaultTreeModel(project));
  }

  private void jTree1ValueChanged(javax.swing.event.TreeSelectionEvent evt) {// GEN-FIRST:event_jTree1ValueChanged
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) evt.getNewLeadSelectionPath().getLastPathComponent();
    if (node.getUserObject() instanceof SourceCode) {
      SourceCode squidUnit = (SourceCode) node.getUserObject();
      jDashboardTextArea.setText(squidUnitToString(squidUnit));
    }
  }// GEN-LAST:event_jTree1ValueChanged

  private void jTree1ValueChangedMostAndLess(javax.swing.event.TreeSelectionEvent evt) {// GEN-FIRST:event_jTree1ValueChangedMostAndLess
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) evt.getNewLeadSelectionPath().getLastPathComponent();
    if (node.getUserObject() instanceof SourceCode) {
      SourceCode squidUnit = (SourceCode) node.getUserObject();
      jTextAreaMostAndLess.setText(squidUnitToString(squidUnit));
    }
  }// GEN-LAST:event_jTree1ValueChangedMostAndLess

  public String squidUnitToString(SourceCode squidUnit) {
    StringBuilder sb = new StringBuilder();

    if (squidUnit instanceof SourceProject) {
      SourceProject squidProject = (SourceProject) squidUnit;
      sb.append("Project ").append(squidProject.getKey()).append("\n");
      sb.append("Packages ").append(squidUnit.getInt(Metric.PACKAGES)).append("\n");
      sb.append("Files :").append(squidUnit.getInt(Metric.FILES)).append("\n");
      sb.append("Classes :").append(squidUnit.getInt(Metric.CLASSES)).append("\n");
      sb.append("Methods :").append(squidUnit.getInt(Metric.METHODS)).append("\n");
      sb.append("Loc :").append(squidUnit.getInt(Metric.LINES)).append("\n");
      sb.append("Ncloc :").append(squidUnit.getInt(Metric.LINES_OF_CODE)).append("\n");
      sb.append("Complexity :").append(squidUnit.getInt(Metric.COMPLEXITY)).append("\n");
    } else if (squidUnit instanceof SourcePackage) {
      sb.append("Package ").append(squidUnit.getKey()).append("\n");
      sb.append("Files :").append(squidUnit.getInt(Metric.FILES)).append("\n");
      sb.append("Classes :").append(squidUnit.getInt(Metric.CLASSES)).append("\n");
      sb.append("Methods :").append(squidUnit.getInt(Metric.METHODS)).append("\n");
      sb.append("Loc :").append(squidUnit.getInt(Metric.LINES)).append("\n");
      sb.append("Ncloc :").append(squidUnit.getInt(Metric.LINES_OF_CODE)).append("\n");
      sb.append("Complexity :").append(squidUnit.getInt(Metric.COMPLEXITY)).append("\n");
    } else if (squidUnit instanceof SourceFile) {
      sb.append("File ").append(squidUnit.getKey()).append("\n");
      sb.append("Classes :").append(squidUnit.getInt(Metric.CLASSES)).append("\n");
      sb.append("Methods :").append(squidUnit.getInt(Metric.METHODS)).append("\n");
      sb.append("Loc :").append(squidUnit.getInt(Metric.LINES)).append("\n");
      sb.append("Ncloc :").append(squidUnit.getInt(Metric.LINES_OF_CODE)).append("\n");
      sb.append("Complexity :").append(squidUnit.getInt(Metric.COMPLEXITY)).append("\n");
    } else if (squidUnit instanceof SourceClass) {
      sb.append("Classe :").append(squidUnit.getKey()).append("\n");
      sb.append("Methods :").append(squidUnit.getInt(Metric.METHODS)).append("\n");
      sb.append("Loc :").append(squidUnit.getInt(Metric.LINES)).append("\n");
      sb.append("Ncloc :").append(squidUnit.getInt(Metric.LINES_OF_CODE)).append("\n");
      sb.append("Complexity :").append(squidUnit.getInt(Metric.COMPLEXITY)).append("\n");
      for (SourceCode child : squidUnit.getChildren()) {
        sb.append(child.getKey()).append("\n");
      }
    } else if (squidUnit instanceof SourceMethod) {
      sb.append("Method :").append(squidUnit.getKey()).append("\n");
      sb.append("Loc :").append(squidUnit.getInt(Metric.LINES)).append("\n");
      sb.append("Ncloc :").append(squidUnit.getInt(Metric.LINES_OF_CODE)).append("\n");
      sb.append("Complexity :").append(squidUnit.getInt(Metric.COMPLEXITY)).append("\n");
    }

    return sb.toString();
  }

  @Action(block = Task.BlockingScope.COMPONENT)
  public Task load() {
    return new LoadTask(getApplication());
  }

  private class LoadTask extends org.jdesktop.application.Task<Object, Void> {

    File selectedFile = null;

    LoadTask(org.jdesktop.application.Application app) {
      // Runs on the EDT. Copy GUI state that
      // doInBackground() depends on from parameters
      // to LoadTask fields, here.
      super(app);
      JFileChooser fc = new JFileChooser();
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      int result = fc.showDialog(getComponent(), "Select a source directory");

      // Determine which button was clicked to close the dialog
      switch (result) {
        case JFileChooser.APPROVE_OPTION:
          // Approve (Open or Save) was clicked
          selectedFile = fc.getSelectedFile();
          break;
        case JFileChooser.CANCEL_OPTION:
          // Cancel or the close-dialog icon was clicked
          break;
        case JFileChooser.ERROR_OPTION:
          // The selection process did not complete successfully
          break;
      }
    }

    @Override
    protected Object doInBackground() {
      // Your Task's code here. This method runs
      // on a background thread, so don't reference
      // the Swing GUI from here.
      Squid squid = new org.sonar.squid.Squid(new SquidConfiguration());
      SourceCode squidUnit = null;
      if (selectedFile != null) {
        squid.scanDir(JavaAstScanner.class, selectedFile);
        squidUnit = squid.computeMeasures();
      }
      return squidUnit; // return your result
    }

    @Override
    protected void succeeded(Object result) {
      // Runs on the EDT. Update the GUI based on
      // the result computed by doInBackground().
      if (result != null) {
        SourceCode squidUnit = (SourceCode) result;
        DefaultMutableTreeNode rootNode = getNode(squidUnit);
        jTree1.setModel(new DefaultTreeModel(rootNode));
      }
    }

    private DefaultMutableTreeNode getNode(SourceCode squidUnit) {
      // DefaultMutableTreeNode node = new DefaultMutableTreeNode(toStringFirstLevel(squidUnit));
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(squidUnit);
      for (SourceCode child : squidUnit.getChildren()) {
        node.add(getNode(child));
      }
      return node;
    }
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JTextArea jDashboardTextArea;
  private javax.swing.JMenuItem jMenuItem1;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JScrollPane jScrollPane3;
  private javax.swing.JSplitPane jSplitPane1;
  private javax.swing.JTabbedPane jTabbedPane1;
  private javax.swing.JTextArea jTextAreaMostAndLess;
  private javax.swing.JTree jTree1;
  private javax.swing.JPanel mainPanel;
  private javax.swing.JMenuBar menuBar;
  private javax.swing.JProgressBar progressBar;
  private javax.swing.JLabel statusAnimationLabel;
  private javax.swing.JLabel statusMessageLabel;
  private javax.swing.JPanel statusPanel;
  // End of variables declaration//GEN-END:variables
  private final Timer              messageTimer;
  private final Timer              busyIconTimer;
  private final Icon               idleIcon;
  private final Icon[]             busyIcons     = new Icon[15];
  private int                      busyIconIndex = 0;
  private JDialog                  aboutBox;
}
