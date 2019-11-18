package eclipse.importprojects;

import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IOverwriteQuery;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;



import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;



public class Application implements IApplication {
	
	
	private static final String ARG_IMPORT = "-import";

	private String[] getImportPaths() {
        BundleContext context = Activator.getContext();
        ServiceReference<?> ser = context.getServiceReference(IApplicationContext.class.getName());
        IApplicationContext iac = (IApplicationContext) context.getService(ser);
        String[] args = (String[]) iac.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        List<String> importPath = new ArrayList<String>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.compareToIgnoreCase(ARG_IMPORT) == 0) {
                i++;
                if (i < args.length) {
                    importPath.add(args[i]);
                }
            }
        }

        return importPath.toArray(new String[importPath.size()]);
    }

    private List<File> findFilesRecursively(String path, String pattern, List<File> returnedList) {
        File root = new File(path);
        File[] list = root.listFiles();
        
         
        if (list == null)
            return returnedList;

        for (File f : list) {
            if (f.isDirectory()) {
                this.findFilesRecursively(f.getAbsolutePath(), pattern, returnedList);
            }
            else {
                if (Pattern.matches(pattern, f.getName()) == true) {
                    returnedList.add(f);
                }
            }
        }

        return returnedList;
    }
    
    public void importThem() {
    	String[] importPaths = this.getImportPaths();
    	
        for (String importPath : importPaths) {
        	
            List<File> projectFiles = this.findFilesRecursively(importPath, "\\.project", new ArrayList<File>());
            if(!projectFiles.isEmpty()) {
            for (File  projectFile : projectFiles) {
                try {    	
                    IWorkspace workspace = ResourcesPlugin.getWorkspace();
                    IProjectDescription description = workspace.loadProjectDescription(new Path(projectFile.toString()));
                    IProject project =ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
            		if (project.isOpen() == false) {  
            			
            			project.create(description, null);
            			project.open(null);               
                        System.out.println("Importing project  " +description.getName());
                    } else {
                    	
                    	System.out.println("Refreshing project "+description.getName());                    	
                    }
        		
                } catch (CoreException e) {
                	System.out.println("Error" +e.getMessage());
                } 
            }   
        }
        
        else {
        	projectFiles = this.findFilesRecursively(importPath, "pom.xml", new ArrayList<File>());
        	if(!projectFiles.isEmpty()) {
        	    System.out.println("");
        	    System.out.println("Found Maven Projects!");
        	    System.out.println("Please go to "+importPath+" and execute mvn eclipse:eclipse");
        	    System.out.println("and run application again.");
        	}
        	else {
        		System.out.println("");
        		System.out.println("Sorry, unknown project type.");
        	}
        	
        }	
        }
    }
	

	@Override
	public Object start(IApplicationContext context) throws Exception {
		this.importThem();
		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
		// nothing to do
	}
}
