import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

class Colors {
    public static final Color
        BIN         = new Color(115, 6, 7),
        ADD         = new Color(30, 82, 0),
        FINISH      = new Color(120, 85, 28),
        CONTROLS    = new Color(75, 63, 59),
        SLIDERS     = CONTROLS,

        BINH        = new Color(158, 35, 35),
        ADDH        = new Color(60, 117, 28),
        FINISHH     = new Color(160, 110, 28),
        CONTROLSH   = new Color(95, 72, 64),
        SLIDERSH    = CONTROLSH,

        BINC        = new Color(178, 55, 55),
        ADDC        = new Color(90, 147, 58),
        FINISHC     = new Color(190, 140, 58),
        CONTROLSC   = new Color(125, 102, 94),
        SLIDERSC    = CONTROLSC,

        TEXT        = new Color(241, 226, 197);

}
public class Window {


    public static JFrame frame;
    public  static EmbeddedMediaPlayerComponent vpanel;

    private static float playbackRate = 1;

    private static JButton bin, add, finish;
    public  static JSlider playback, editor_start, editor_end;

    private static boolean allow_editor_end_skip = false;

    // unmute/mute?
    private static final JButton[] controls = {
        new JButton("slower"),
        new JButton("⏪"),
        new JButton("⏯"),
        new JButton("⏩"),
        new JButton("faster")
    };

    private static int vwidth = 0, vheight = 0;


    // Keybinds for everything! like WASD, ' ', Q, E for bad good, R -- unmute

    public static void init() {

        frame = new JFrame("Clip Editor v5");
        frame.setSize(1280, 720);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        frame.setMinimumSize(new Dimension(1280 / 2 + 40, 720 / 2 + 40));
        frame.getContentPane().setBackground(new Color(20, 20, 20));
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                vpanel.release();
                System.exit(0);
            }
        });

        JLayeredPane pane = frame.getLayeredPane();

        playback = new JSlider();

        bin = new JButton("bin");
        add = new JButton("add");
        finish = new JButton("should finish (after this)");

        bin.setForeground(Colors.TEXT);
        add.setForeground(Colors.TEXT);
        finish.setForeground(Colors.TEXT);

        bin.setFont(new Font("Lucon", Font.PLAIN, 36));
        add.setFont(new Font("Lucon", Font.PLAIN, 36));
        finish.setFont(new Font("Lucon", Font.PLAIN, 36));

        editor_start = new JSlider();
        editor_end   = new JSlider();

        editor_start.setBackground(Colors.SLIDERS);
        editor_end.setBackground(Colors.SLIDERS);

        editor_start.setForeground(Colors.TEXT);
        editor_end.setForeground(Colors.TEXT);

        editor_start.putClientProperty("JSlider.isFilled", Boolean.TRUE);

        bin.setBackground(Colors.BIN);
        add.setBackground(Colors.ADD);
        finish.setBackground(Colors.FINISH);

        bin.setBorderPainted(false);
        add.setBorderPainted(false);
        finish.setBorderPainted(false);

        for(JButton ctrl : controls){
            ctrl.setBackground(Colors.CONTROLS);
            ctrl.setForeground(Colors.TEXT);
            ctrl.setFont(new Font("Lucon", Font.PLAIN, 36));
            ctrl.setBorderPainted(false);
        }


        vpanel = new EmbeddedMediaPlayerComponent();
        pane.add(vpanel, Integer.valueOf(0));

//        setLayout(); // Possible fix for something at some point in the future, since I had it before...

        frame.add(bin);
        frame.add(add);
        frame.add(finish);

        pane.add(playback, Integer.valueOf(1));

        pane.add(editor_start, Integer.valueOf(1));
        pane.add(editor_end, Integer.valueOf(1));

        for (JButton control : controls) frame.add(control);

        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                setLayout();
                frame.revalidate();
            }
        });

        addListeners();

        frame.setVisible(true);

        setLayout();

        frame.revalidate();

    }

    public static void setLayout(){

        int w = frame.getWidth(), h = frame.getHeight();

        int a = Math.min((w - 200) / 320, (h - 200) / 180);
        vwidth = a * 320; // v(ideo) width
        vheight= a * 180;

        int sbwidth = (w - vwidth) / 2; // side button width
        bin.setBounds(0,                0, sbwidth, vheight);
        vpanel.setBounds(sbwidth, 0, vwidth, vheight);
        playback.setBounds(sbwidth + 20, vheight - 40, vwidth - 40, 20);
        add.setBounds(sbwidth + vwidth, 0, sbwidth, vheight);

        int b = w / 5;
        for(int i = 0; i < controls.length; i ++)
            controls[i].setBounds(b * i, vheight + 4, b, 60);

        finish.setBounds(0, vheight + 60 + 8, w, 64);

        int c = 48*2;

        editor_start.setBounds(0, h - c - 48, w - 16, 48);
        editor_end  .setBounds(0, h - c,      w - 16, 48);

    }

    private static void addListeners(){
        AtomicBoolean yes = new AtomicBoolean(false);
        Executors.newScheduledThreadPool(1)
            .scheduleAtFixedRate(
                () -> {
                    playback.setValue((int) vpanel.mediaPlayer().status().time());
                    yes.set(true);
                },
                0, 250, TimeUnit.MILLISECONDS
            );
        bin.addActionListener(e -> Output.bin());
        add.addActionListener(e -> Output.add());
        finish.addActionListener(e -> {
            Main.shouldFinish = !Main.shouldFinish;
            if(Main.shouldFinish)
                finish.setFont(new Font("Lucon", Font.BOLD, 36));
            else
                finish.setFont(new Font("Lucon", Font.PLAIN, 36));
        });
        controls[0].addActionListener(e -> vpanel.mediaPlayer().controls().setRate(playbackRate -= 0.1f));
        controls[1].addActionListener(e -> vpanel.mediaPlayer().controls().skipTime(-2000));
        controls[2].addActionListener(e -> {
            if(!vpanel.mediaPlayer().status().isSeekable()) {
                vpanel.mediaPlayer().controls().play();
                vpanel.mediaPlayer().controls().setTime(editor_start.getValue());
            } else
                vpanel.mediaPlayer().controls().pause();

        });
        controls[3].addActionListener(e -> vpanel.mediaPlayer().controls().skipTime(2000));
        controls[4].addActionListener(e -> vpanel.mediaPlayer().controls().setRate(playbackRate += 0.1f));


        // Slider event handling
        // ##################################################################################
        {
            playback.addChangeListener(e -> {
                if(!yes.get()) vpanel.mediaPlayer().controls().setTime(playback.getValue());
                yes.set(false);
            });

            playback.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    super.mousePressed(e);
                    double percent = e.getPoint().getX() / playback.getWidth();
                    playback.setValue((int) (percent * playback.getMaximum()));
                }
            });

            editor_start.addChangeListener(e -> {
                if(!yes.get()) vpanel.mediaPlayer().controls().setTime(editor_start.getValue());
                yes.set(false);
            });

            editor_start.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    super.mousePressed(e);
                    double percent = e.getPoint().getX() / editor_start.getWidth();
                    editor_start.setValue((int) (percent * editor_start.getMaximum()));
                }
            });

            editor_end.addChangeListener(e -> {
                if(allow_editor_end_skip && editor_end.getValue() != vpanel.mediaPlayer().status().length()) {
                    if (!yes.get()) vpanel.mediaPlayer().controls().setTime(editor_end.getValue());
                    yes.set(false);
                }
                allow_editor_end_skip = true;
            });

            editor_end.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    super.mousePressed(e);
                    double percent = e.getPoint().getX() / editor_end.getWidth();
                    editor_end.setValue((int) (percent * editor_end.getMaximum()));
                }
            });
        }

        // Colors
        // ##################################################################################

        bin.addMouseListener(new CustomMouseListener(Colors.BIN, Colors.BINH, Colors.BINC));
        add.addMouseListener(new CustomMouseListener(Colors.ADD, Colors.ADDH, Colors.ADDC));
        finish.addMouseListener(new CustomMouseListener(Colors.FINISH, Colors.FINISHH, Colors.FINISHC));

        playback.addMouseListener(new CustomMouseListener(Colors.SLIDERS, Colors.SLIDERSH, Colors.SLIDERSC));
        editor_start.addMouseListener(new CustomMouseListener(Colors.SLIDERS, Colors.SLIDERSH, Colors.SLIDERSC));
        editor_end.addMouseListener(new CustomMouseListener(Colors.SLIDERS, Colors.SLIDERSH, Colors.SLIDERSC));

        for(JButton ctrl : controls){
            ctrl.addMouseListener(new CustomMouseListener(Colors.CONTROLS, Colors.CONTROLSH, Colors.CONTROLSC));
        }
    }

    public static void update(String video_path){
        allow_editor_end_skip = false;

        editor_start.setSnapToTicks(false);
        editor_end.setSnapToTicks(false);

        if(video_path == null || video_path.isEmpty()) {
            vpanel.mediaPlayer().media().startPaused("");
            return;
        }
        vpanel.mediaPlayer().media().startPaused(video_path.replaceAll("/", "\\\\"));
        vpanel.mediaPlayer().controls().play();

//        playbackRate = 1;
//        vpanel.mediaPlayer().controls().setRate(playbackRate); // maybe I don't want this?

        SwingUtilities.invokeLater(() -> {

            while(vpanel.mediaPlayer().video().videoDimension() == null);

            if(vpanel.mediaPlayer().video().videoDimension() != null) {
                Dimension original = vpanel.mediaPlayer().video().videoDimension();
                vpanel.mediaPlayer().video().setScale((float) Math.min(vwidth / original.getWidth(), vheight / original.getHeight())); // 0.5 is for 1280x720
                playback.setMaximum((int) vpanel.mediaPlayer().status().length());

                editor_start.setMinimum(0);
                editor_start.setMaximum((int) vpanel.mediaPlayer().status().length());
                editor_start.setValue(0);

                editor_end.setMinimum(0);
                editor_end.setMaximum((int) vpanel.mediaPlayer().status().length());
                editor_end.setValue(editor_end.getMaximum());

                editor_start.setMinorTickSpacing(1000);
                editor_start.setMajorTickSpacing(1000);
                editor_start.setSnapToTicks(true);

                editor_end.setMinorTickSpacing(1000);
                editor_end.setMajorTickSpacing(1000);
                editor_end.setSnapToTicks(true);

            }
        });

    }
}

class CustomMouseListener extends MouseAdapter {
    Color def,  highlighted, clicked;
    public CustomMouseListener(Color def, Color highlighted, Color clicked){
        this.def = def;
        this.highlighted = highlighted;
        this.clicked = clicked;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseReleased(e);
        e.getComponent().setBackground(clicked); // practically useless, since Java overrides it. I don't really care.
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        e.getComponent().setBackground(def);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        super.mouseEntered(e);
        e.getComponent().setBackground(highlighted);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        super.mouseExited(e);
        e.getComponent().setBackground(def);
    }
}


/*
* Todo:
*  Design
*  Events
*
* Can't restart video after it stops!
*
* */


