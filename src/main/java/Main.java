
import java.io.File;

public class Main {

    // btw: gsudo .\vlc-cache-gen.exe "C:\Program Files\VideoLAN\VLC\plugins"

    /*
    * Usage:
    * Don't use the Artifact, it does not feel like functioning.
    * With IntelliJ, I use the "Application" Build & Run ¯\_(ツ)_/¯
    *
    * *Don't forget to replace "C:/ProgramData/chocolatey/bin/ffmpeg.exe" in Output#add with your own path, if needed
    *
    */

    public static int index = 0;
    public static String directory;
    public static String[] paths;

    public static String DATA_PATH;

    public static boolean shouldFinish = false;

    public static void main(String[] args) throws Exception {
        if(args.length == 0) {
            System.err.println("clip_editor_v5 needs 1 argument -- folder path! clip_editor_v5 <folder_path>");
            System.exit(1);
        }

//        String _pwd = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
//        DATA_PATH = new File(URLDecoder.decode(_pwd, "UTF-8")).getParent();
        DATA_PATH = new File(System.getenv("LocalAppData"), "Ult1/ClipEditor").getCanonicalPath();
        new File(DATA_PATH).mkdirs();

        Window.init(); // "multithreading?"

        directory = new File(args[0]).getCanonicalPath().replaceAll("\\\\", "/");
        System.out.println("Video directory: " + directory);
        paths = new File(directory).list((dir, name) -> name.contains(".mp4"));
        if(paths == null || paths.length == 0){
            System.err.println("Given directory is either empty or does not exist!");
            System.exit(1);
        }
        System.out.println("Data stored at: " + DATA_PATH);


        Window.update(directory + '/' + paths[index]);
    }

    public static boolean nextVideo(){

        index ++;
        if( shouldFinish || index >= paths.length ){
            System.out.println("Done");
            Window.update(null);
            return true;
        }
        Window.update(directory + '/' + paths[index]);
        return false;
    }

}
