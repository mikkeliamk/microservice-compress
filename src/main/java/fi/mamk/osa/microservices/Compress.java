package fi.mamk.osa.microservices;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.belvain.soswe.workflow.Microservice;

import net.xeoh.plugins.base.annotations.Capabilities;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class Compress extends Microservice {
    
    @Capabilities
    public String[] caps() {
        return new String[] {"name:Compress"};
    }
    
    @Override
    public boolean execute(String input, HashMap<String, Object> options)
            throws Exception {
        
        boolean success = false;
        String state = "error";
        String output = "";
        String filename = "";
        String fileextension = ".tar.gz";
        String uploaddirectory = "";
        String organization = "defaultorganization";
        String user = "defaultuser";
        
        if (options != null) {
            if (options.containsKey("filename")) {
                filename = options.get("filename").toString();
            }
            if (options.containsKey("fileextension")) {
                fileextension = options.get("fileextension").toString();
            }
            if (options.containsKey("uploaddirectory")) {
                uploaddirectory = options.get("uploaddirectory").toString();
            }
            if (options.containsKey("organization")) {
                organization = options.get("organization").toString();
            }
            if (options.containsKey("username")) {
                user = options.get("username").toString();
            }
        }

        if (input != null && !input.isEmpty()) {
            //Handle input from previous microservice here
        }
        
        Process p;
        try {
            
            if (fileextension.equals(".zip")) {
                
                String zipFile = uploaddirectory+filename+fileextension;
                String srcDir = uploaddirectory+filename;
                List<String> fileList = new ArrayList<String>();
                
                generateFileList(new File(srcDir), fileList, srcDir);
                createZip(zipFile, fileList, srcDir, output);
                
            } else {
                
                p = Runtime.getRuntime().exec(super.getExec().replace("{name}", filename).replace("{dir}", uploaddirectory));
            
                p.waitFor();
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = reader.readLine();
                if (line != null) {
                    output += line+"\n";
                }
                
                while (line != null) {
                    line = reader.readLine();
                    if (line != null) {
                        output += line+"\n";
                    }
                }
            }
            
            /*
             * to really know if the command did what you wanted it to do you need to analyze output here and determine if
             * it is correct. Otherwise UI may display that execution succeeded for example if ping command was successful
             * but there was no answer.
             */
            output += fileextension+" generated for "+filename+"\n";
            
            success = true;
            state = "completed";
            
        } catch (Exception e) {
            output += "Zip generation for "+filename+" failed.\n"+e.toString()+"\n";
        }
        
        super.setState(state);
        super.setOutput(output);
        super.setCompleted(true);
        
        String log = super.getLog().replace("{organization}", organization)
                                   .replace("{user}", user);
        super.setLog(log);
        log();
        
        return success;
    }
          
    public void generateFileList(File node, List<String> fileList, String srcDir) {
        // check if file
        if (node.isFile()){
            fileList.add(generateZipEntry(node.getAbsoluteFile().toString(), srcDir));
        }
           
        if (node.isDirectory()) {
            String[] subNote = node.list();
            for (String filename : subNote) {
                generateFileList(new File(node, filename), fileList, srcDir);
            }
        }
    }
    
    private String generateZipEntry(String file, String srcDir){
        return file.substring(srcDir.length()+1, file.length());
    }

    public void createZip(String zipFile, List<String> fileList, String srcDir, String output) throws IOException {
        
        byte[] buffer = new byte[1024];
        
        FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream zos = new ZipOutputStream(fos);
        output += "Output to Zip : "+zipFile+"\n";
               
        for (String file : fileList) {
            output += file+"\n";
            
            ZipEntry zipEntry = new ZipEntry(file);
            zos.putNextEntry(zipEntry);
                  
            FileInputStream fis = new FileInputStream(srcDir + File.separator + file);
              
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            zos.closeEntry();
            fis.close();
        }
        
        zos.close();
        fos.close();
    }
    
}