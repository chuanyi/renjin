package org.renjin.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.renjin.packaging.PackageBuilder;
import org.renjin.packaging.PackageDescription;
import org.renjin.packaging.PackageSource;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.io.Files;

import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Compiles R sources into a serialized blob
 */
@ThreadSafe
@Mojo(name = "namespace-compile",
      defaultPhase = LifecyclePhase.COMPILE, 
      requiresDependencyResolution = ResolutionScope.COMPILE)
public class NamespaceMojo extends AbstractMojo {

  /**
   * Directory containing R sources
   *
   */
  @Parameter(defaultValue = "src/main/R", required = true)
  private File sourceDirectory;

  /**
   * Directory containing data files
   */
  @Parameter(defaultValue = "src/main/data")
  private File dataDirectory;

  @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
  private File outputDirectory;

  @Parameter(defaultValue = "${plugin.artifacts}", readonly = true)
  private List<Artifact> pluginDependencies;


  /**
   * The enclosing project.
   */
  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;


  @Parameter(defaultValue = "${project.artifactId}", required = true)
  private String packageName;

  @Parameter(defaultValue = "${project.groupId}", required = true, readonly = true)
  private String groupId;

  @Parameter(defaultValue = "${project.artifactId}", required = true)
  private String namespaceName;

  @Parameter(defaultValue = "${project.basedir}/NAMESPACE")
  private File namespaceFile;

  @Parameter(defaultValue = "${project.basedir}/DESCRIPTION")
  private File descriptionFile;
  
  @Parameter
  private List<String> sourceFiles;

  @Parameter
  private List<String> defaultPackages;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    try {
      PackageSource source = new PackageSource.Builder(project.getBasedir())
          .setGroupId(groupId)
          .setDescriptionFile(descriptionFile)
          .setNamespaceFile(namespaceFile)
          .setSourceDir(sourceDirectory)
          .setSourceFiles(sourceFiles)
          .setDataDir(dataDirectory)
          .build();

      MavenBuildContext buildContext = new MavenBuildContext(project, pluginDependencies);
      buildContext.setDefaultPackages(defaultPackages);

      PackageBuilder builder = new PackageBuilder(source, buildContext);
      builder.copyRootFiles();
      builder.compileNamespace();
      builder.compileDatasets();

    } catch (IOException e) {
      throw new MojoExecutionException("IOException: " + e.getMessage(), e);
    }
  }

  private void compileNamespaceEnvironment(MavenBuildContext buildContext) throws MojoExecutionException {

    PackageSource source;
    try {
      source = new PackageSource.Builder(project.getBasedir())
          .setGroupId(groupId)
          .setNamespaceFile(namespaceFile)
          .setDescriptionFile(descriptionFile)
          .setSourceDir(sourceDirectory)
          .setSourceFiles(sourceFiles)
          .build();
    } catch (IOException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }

    
    ClassLoader classLoader = buildContext.getClassLoader();
    ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();

    try {
      Thread.currentThread().setContextClassLoader(classLoader);

      Object builder = classLoader.loadClass("org.renjin.packaging.NamespaceBuilder").newInstance();
      builder.getClass()
          .getMethod("build", String.class, String.class, File.class, List.class, File.class, List.class)
          .invoke(builder, groupId, namespaceName, namespaceFile, source.getSourceFiles(), getEnvironmentFile(),
              defaultPackages);

    } catch(Exception e) {
      throw new MojoExecutionException("exception", e);
    } finally {
      Thread.currentThread().setContextClassLoader(contextLoader);
    }
  }

  private void compileDatasets(MavenBuildContext buildContext) throws MojoExecutionException {

    
    ClassLoader classLoader = buildContext.getClassLoader();
    ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(classLoader);

      Constructor ctor = classLoader.loadClass("org.renjin.packaging.DatasetsBuilder")
          .getConstructor(File.class, File.class);
      Object builder = ctor.newInstance(dataDirectory, getPackageRoot());
      builder.getClass().getMethod("build").invoke(builder);
    } catch(Exception e) {
      throw new MojoExecutionException("exception", e);
    } finally {
      Thread.currentThread().setContextClassLoader(contextLoader);
    }

  }

  private File getEnvironmentFile() {
    return new File(getPackageRoot(), "environment");
  }

  private File getPackageRoot() {
    File packageRoot = new File(outputDirectory.getAbsoluteFile() + File.separator +
        groupId.replace(".", File.separator) + File.separator + packageName);
    packageRoot.mkdirs();
    return packageRoot;
  }

  private void copyResources() {
    try {
      if(!namespaceFile.exists()) {
        System.err.println("NAMESPACE file is missing. (looked in " + namespaceFile.getAbsolutePath() + ")");
        throw new RuntimeException("Missing NAMESPACE file");
      }
      Files.copy(namespaceFile, new File(getPackageRoot(), "NAMESPACE"));

      if(descriptionFile.exists()) {
        Files.copy(descriptionFile, new File(getPackageRoot(), "DESCRIPTION"));
      }

    } catch (IOException e) {
      throw new RuntimeException("Exception copying NAMESPACE file", e);
    }

  }


  private void writeRequires() {
    // save a list of packages that are to be loaded onto the
    // global search path when this package is loaded

    if(descriptionFile.exists()) {
      PackageDescription description;
      try {
        description = PackageDescription.fromFile(descriptionFile);
      } catch(IOException e) {
        throw new RuntimeException("Exception reading DESCRIPTION file");
      }
      try {
        PrintWriter requireWriter = new PrintWriter(new File(getPackageRoot(), "requires"));
        for(PackageDescription.PackageDependency dep : description.getDepends()) {
          if(!dep.getName().equals("R") && !Strings.isNullOrEmpty(dep.getName())) {
            requireWriter.println(dep.getName());
          }
        }
        requireWriter.close();
      } catch (IOException e) {
        throw new RuntimeException("Exception writing requires file", e);
      }
    }
  }
}
