package me.divisiion.omg;

import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Main {

    // Reference to the dir of where all the Forge files are held ( and where the malicious files are )
    public static File forgeFilesDir = new File(System.getenv("APPDATA") + "/.minecraft/libraries/net/minecraftforge");
    public static File versionsDir = new File(System.getenv("APPDATA") + "/.minecraft/versions");
    // Names of all the SAFE Forge files to make sure we keep
    public static String[] safeForgeFileNames = {
            "accesstransformers",
            "binarypatcher",
            "coremod",
            "coremods",
            "eventbus",
            "forge",
            "forgespi",
            "installertools",
            "jarsplitter",
            "unsafe"
    };

    public static void main(String[] args) throws Exception {
        // If the net/minecraftforge directory doesn't exist don't do anything
        if(!forgeFilesDir.exists() || !versionsDir.exists()) {
            System.out.println("Crazy! You're safe!");
        } else {
            // Loop over every dir in forgeFilesDir
            for(File file : forgeFilesDir.listFiles()) {
                /*
                  Check if the names of any of the files DOESN'T match to safeForgeFileNames
                  and if so print the path and call cleanForgeFiles method
                 */
                if(Arrays.stream(safeForgeFileNames).noneMatch(s -> s.equals(file.getName()))) {
                    File infectedFile = file;
                    System.out.println("Found backdoor at " + infectedFile.getAbsolutePath() + ", Removing it now!");
                    cleanForgeFiles(infectedFile);
                }
            }
            // Method of checking versionDir for any permanent lib in there from qqAntiVirus
            for(File version : versionsDir.listFiles()) {
                if(version.isDirectory()) {
                    File[] versionShit = version.listFiles();
                    for (File versionFile : versionShit) {
                        if (versionFile.getName().endsWith(".json") && versionFile.getName().contains("1.12.2") && versionFile.getName().contains("forge")) {
                            String json = new String(Files.readAllBytes(Paths.get(versionFile.getAbsolutePath())), StandardCharsets.UTF_8);
                            if (json.contains("--tweakClass net.minecraftforge.modloader.Tweaker")) {
                                System.out.println("backmeme found!!! cleaning.");
                                clean(json, Paths.get(versionFile.getAbsolutePath()));
                            }
                        }
                    }
                }
            }
        }
    }

    public static boolean cleanForgeFiles(File file) {
        // If the file is somehow removed during the process, tell the user and stop to prevent null.
        if(!file.exists()) { System.out.println("Something went wrong in the process of locating and deleting the malicious file");
        }
        // If the file is a dir then make an array of all of it's children ( preparation for deleting )
        if(file.isDirectory()) {
            String[] children = file.list();
            // Loop over every malicious file to delete them and store if the file has properly been deleted
            for (int i = 0; i < children.length; i++) {
                boolean success = cleanForgeFiles (new File(file, children[i]));
                // If success == false then return false to let the user know that deletion of the malicious file was not successful
                if (!success) {
                    return false;
                }
            }
        }
        // Return the boolean of File.delete() to check if the file was deleted again
        return file.delete();
    }

    // Method from qqAntiVirus
    public static void clean(String jsonString, Path path) throws Exception {
        JSONObject versionJson = new JSONObject(jsonString);
        String args = (String) versionJson.get("minecraftArguments");
        String newArgs = args.replace("--tweakClass net.minecraftforge.modloader.Tweaker", "");
        versionJson.remove("minecraftArguments");
        versionJson.put("minecraftArguments", newArgs);
        Files.write(path, versionJson.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("cleaned " + path.toString());
    }
}
