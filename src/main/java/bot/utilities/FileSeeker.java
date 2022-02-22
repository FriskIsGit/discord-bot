package bot.utilities;

import java.io.File;
import java.net.URISyntaxException;

public class FileSeeker{

    private boolean isRunFromJar = false;
    private String targetName = "config.json";
    private String targetPath = "";
    private String basePath;
    private File baseFile;

    //target is an absolute name
    public FileSeeker(String exactTargetFileName){
        this();
        this.targetName = exactTargetFileName;
    }
    public FileSeeker(){
        try{
            basePath = FileSeeker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        }catch (URISyntaxException uriExc){
            basePath = System.getProperty("user.dir");
        }
        baseFile = new File(basePath);
        if(baseFile.isFile()){
            isRunFromJar = true;
            baseFile = baseFile.getParentFile();
        }
    }
    //target name is contained
    public FileSeeker(String containedFileName, String baseDirectory){
        this.targetName = containedFileName;
        basePath = baseDirectory;
        baseFile = new File(basePath);
    }

    public String findTargetPath(){
        stepDown(baseFile);
        return targetPath;
    }

    public String findContainingPath(){
        stepDownContains(baseFile);
        return targetPath;
    }

    private boolean stepDown(File currentDir){
        File [] files = currentDir.listFiles();
        for(int i = 0; i<files.length; i++){
            if (files[i].isDirectory()){
                if(stepDown(files[i])){
                    return true;
                }
            }else if(files[i].getName().startsWith(targetName)){
                targetPath = files[i].getPath();
                return true;
            }
        }
        return false;
    }
    private boolean stepDownContains(File currentDir){
        File [] files = currentDir.listFiles();
        for(int i = 0; i<files.length; i++){
            if (files[i].isDirectory()){
                if(stepDownContains(files[i])){
                    return true;
                }
            }else if(getNameWithoutExtension(files[i].getName()).contains(targetName)){
                targetPath = files[i].getPath();
                return true;
            }
        }
        return false;
    }

    /**
     * method should account for hidden files prepended with a dot
     * @param absoluteName - name potentially with extension
     * @return nameWithoutExtension or empty string if name doesn't contain a dot
     */
    public static String getNameWithoutExtension(String absoluteName){
        int index = absoluteName.indexOf('.');
        if(index <= 0){
            return "";
        }
        return absoluteName.substring(0,index);
    }
    /**
     * method should account for hidden files prepended with a dot
     * @param absoluteName - name potentially with extension
     * @return extension or empty string if name doesn't contain extension
     */
    public static String getExtension(String absoluteName){
        int index = absoluteName.indexOf('.');
        if(index <= 0 || absoluteName.length()==index+1){
            return "";
        }
        return absoluteName.substring(index+1);
    }
    public String getBasePath(){
        return basePath;
    }
    public boolean isRunFromJar(){
        return isRunFromJar;
    }

    //Files.walk(Paths.get(System.getProperty("user.dir")));

}
