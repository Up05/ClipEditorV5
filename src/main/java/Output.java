import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class Output {

    public static File to_be_deletedf = null;
    public static File output_listf = null;

    public static ArrayList<String> left_overf = new ArrayList<>(64);

    public static void bin(){
        if(to_be_deletedf == null)
            to_be_deletedf = new File(Main.DATA_PATH, "to_be_deleted.txt");

        boolean done = Main.nextVideo();
        if(done) System.exit(0);

        int i = Main.index - 1;

        boolean delete_success = false;
        try {
            delete_success = new File(Main.directory + "/" + Main.paths[i]).delete();
        } catch (SecurityException e){
            e.printStackTrace();
        }

        if(!delete_success) {
            try {
                FileWriter writer = new FileWriter(to_be_deletedf, true);
                writer.write(Main.directory + "/" + Main.paths[i] + "\n");
                writer.close(); // non-pessimized code, what's that?
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void add() {
        if(output_listf == null) {
            output_listf = new File(Main.DATA_PATH, "output.txt");
            output_listf.delete();
        }
        if(to_be_deletedf == null)
            to_be_deletedf = new File(Main.DATA_PATH, "to_be_deleted.txt");

        boolean done = Main.nextVideo();

        int i = Main.index - 1;

        try {
            FileWriter writer = new FileWriter(output_listf, true);
            writer.write("file '" + Main.directory + "/" + Main.paths[i] + "'\n");
            writer.write("inpoint "  + formatTime(Window.editor_start.getValue()));
            writer.write("outpoint " + formatTime(Window.editor_end.getValue()));
            writer.close();

            left_overf.add(Main.directory + "/" + Main.paths[i]);

        } catch (Exception e) {
            e.printStackTrace();
        }

        String outf_path = Main.DATA_PATH + "/output#" + Objects.requireNonNull(new File(Main.DATA_PATH).list((dir, name) -> name.contains(".mp4"))).length + ".mp4";

        if(done) {
            try { // I should really just make a proper way to do this... \/
                Process proc = new ProcessBuilder("C:/ProgramData/chocolatey/bin/ffmpeg.exe", "-f", "concat", "-safe", "0", "-i", "output.txt", "-an", "-c", "copy", outf_path)
                    .directory(new File(Main.DATA_PATH))
                    .inheritIO()
                    .start();
                proc.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }

            for(String f : left_overf) {
                boolean delete_success = false;
                try {
                    delete_success = new File(f).delete();
                } catch (SecurityException e){
                    e.printStackTrace();
                }
                if(!delete_success) {
                    try {
                        FileWriter writer = new FileWriter(to_be_deletedf, true);
                        writer.write(Main.directory + "/" + f + "\n");
                        writer.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            System.exit(0);
        }
    }

    /** adds a new line to end of format! */
    public static String formatTime(long millis){

        long sec  = millis / 1000,
             min  = sec / 60,
             hour = min / 60;

        millis %= 1000;
        sec %= 60;
        min %= 60;
        hour %= 24;


        return String.format("%02d:%02d:%02d.%03d\n", hour, min, sec, millis);
    }
}
