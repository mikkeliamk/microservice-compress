package fi.mamk.osa.microservices;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

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
        String output = "";
        String filename = "";
        String uploaddirectory = "";
        
        if (options != null) {
            if (options.containsKey("filename")) {
                filename = options.get("filename").toString();
            }
            if (options.containsKey("uploaddirectory")) {
                uploaddirectory = options.get("uploaddirectory").toString();
            }
        }

        if (input != null && !input.isEmpty()) {
            //Handle input from previous microservice here
        }
        
        Process p;
        try {
            
            p = Runtime.getRuntime().exec(super.getExec().replace("{name}", filename).replace("{dir}", uploaddirectory));
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = reader.readLine();
            if(line != null){
                 output += line+"\n";
            }
            
            while (line != null) {
                line = reader.readLine();
                if(line != null){
                    output += line+"\n";
                }
            }
            
            /*
             * to really know if the command did what you wanted it to do you need to analyze output here and determine if
             * it is correct. Otherwise UI may display that execution succeeded for example if ping command was successful
             * but there was no answer.
             */
            
            output += "Zip generated for "+filename+"\n";
            
            success = true;
            super.setState("completed");
            super.setOutput(output);
            super.setCompleted(true);
            
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
            super.setOutput(e.toString());
            super.setState("error");
            super.setCompleted(true);
            
        }
        
        log();
        return success;
    }
    
}
