/*
 * Copyright © 2001-2008 Wouter van Oortmerssen
 * Copyright © 2008-2015 Jonathan Dowland <jon@dow.land>
 *
 * Distributed under the terms of the GNU GPL Version 2
 * See file LICENSE.txt
 */

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class MainFrame extends Frame implements WadCMainFrame {
  TextArea textArea1 = new TextArea("",15,30);
  GroupLayout borderLayout1 = new GroupLayout(true);
  Panel panel1 = new Panel();
  TextArea textArea2 = new TextArea("",5,20);
  MenuBar menuBar1 = new MenuBar();
  Menu menu1 = new Menu();
  MenuItem menuItem1 = new MenuItem();
  MenuItem menuItem2 = new MenuItem();
  MenuItem menuItem3 = new MenuItem();
  MenuItem menuItem4 = new MenuItem();
  MenuItem menuItem5 = new MenuItem();
  Menu menu2 = new Menu();
  MenuItem menuItem6 = new MenuItem();
  MenuItem menuItem7 = new MenuItem();
  MenuItem menuItem8 = new MenuItem();
  Canvas cv;

  WadParse lastwp = null;
  boolean changed = false;

  public MainFrame() {
    try  {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    MainFrame mainFrame1 = new MainFrame();
  }

  private void jbInit() throws Exception {
    setTitle("wadc");
    textArea1.setFont(new Font("Monospaced",0,12));
    textArea1.addTextListener(new java.awt.event.TextListener() {

      public void textValueChanged(TextEvent e) {
        textArea1_textValueChanged(e);
      }
    });
    panel1.setLayout(new GroupLayout(false));
    textArea1.setBackground(Color.white);
    this.setLayout(borderLayout1);
    setBackground(Color.lightGray);
    setEnabled(true);
    setMenuBar(menuBar1);
    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        quit(null);
      }
    });
    menu1.setLabel("File");
    menuItem1.setLabel("New");
    menuItem1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        newfile(e);
      }
    });
    menuItem2.setLabel("Open");
    menuItem2.setShortcut(new MenuShortcut(79));
    menuItem2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        open(e);
      }
    });
    menuItem3.setLabel("Save As");
    menuItem3.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveas(e);
      }
    });
    menuItem4.setLabel("Save");
    menuItem4.setShortcut(new MenuShortcut(83));
    menuItem4.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        save(e);
      }
    });
    menuItem5.setLabel("Quit");
    menuItem5.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        quit(e);
      }
    });
    menu2.setLabel("Program");
    menuItem6.setLabel("Run");
    menuItem6.setShortcut(new MenuShortcut(82));
    menuItem6.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        run(e);
      }
    });
    menuItem7.setLabel("Run / Save / Save Wad");
    menuItem7.setShortcut(new MenuShortcut(87));
    menuItem7.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        savewad(e);
      }
    });
    menuItem8.setLabel("Run / Save / Save Wad / BSP / DOOM");
    menuItem8.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        bspdoom(savewad(e));
      }
    });
    add(textArea1, "b");
    menuBar1.add(menu1);
    menuBar1.add(menu2);
    menu1.add(menuItem1);
    menu1.add(menuItem2);
    menu1.add(menuItem3);
    menu1.add(menuItem4);
    menu1.add(menuItem5);
    menu2.add(menuItem6);
    menu2.add(menuItem7);
    menu2.add(menuItem8);
    cv = new MyCanvas(this);
    //textArea2.setBackground(Color.lightGray);
    textArea2.setEditable(false);
    panel1.add(cv,"b3");
    panel1.add(textArea2, "b1");
    add(panel1, "b3");
    //setSize(600,400);
    this.setLocation(50,50);
    pack();
    setVisible(true);
    newfile(null);

    String cfg = getPrefs();
    if("".equals(cfg)) {
        // if we couldn't read wadc.cfg, write out a default
        savecfg();
    } else {
        WadParse prefs = new WadParse(cfg, this);
        if(prefs.err==null) prefs.run();
    }

    String lf = loadtextfile(prefs.basename);
    if(lf.length()>0) { textArea1.setText(lf); } else { newfile(null); };
  }

  public void msg(String s) {
    textArea2.append(s+"\n");
  }

  void savetextfile(String name, String contents) {
    try {
      FileOutputStream prj = new FileOutputStream(name);
      DataOutputStream dos = new DataOutputStream(prj);
      dos.writeBytes(contents);
      dos.flush();
      dos.close();
      prj.close();
      msg("wrote file "+name);
    } catch(IOException i) {
      msg("saving file "+name+" unsuccessful");
    };
  }

  String loadtextfile(String name) {
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(name)));
      String c = "";
      String t;
      while((t = in.readLine())!=null) c+=t+"\n";
      in.close();
      msg("file "+name+" read");
      return c;
    } catch(IOException i) {
      msg("couldn't load file "+name);
    };
    return "";
  }

  String getPrefs() {
      String prefDir = System.getProperty("user.home") + File.separator + ".wadc";
      return loadtextfile(prefDir + File.separator + "wadc.cfg");
  }

  void setPrefs(String prefs) {
      String prefDir = System.getProperty("user.home") + File.separator + ".wadc";
      File f = new File(prefDir);

      if(!f.exists() && !f.mkdir()) {
          msg("couldn't create " + prefDir + ", saving configuration unsuccessful");
      }
      if(f.exists() && !f.isDirectory()) {
          msg(prefDir + " exists but is not a directory, saving configuration unsuccessful");
      }

      savetextfile(prefDir + File.separator + "wadc.cfg", prefs);
  }

  void newfile(ActionEvent e) {
    textArea1.setText("#\"standard.h\"\n\nmain {\n  straight(64)\n}\n");
    // XXX: nasty hack to ensure basename is always a FQ path
    prefs.basename = new File(System.getProperty("user.home"), "untitled.wl").toString();
  }

  void open(ActionEvent e) {
    FileDialog fd = new FileDialog(this,"select a .wl file to load",FileDialog.LOAD);
    fd.setDirectory((new File(prefs.basename)).getParent());
    fd.setVisible(true);
    String name = fd.getFile();
    if(name==null) return;
    prefs.basename = (new File(fd.getDirectory(),name)).toString();
    textArea1.setText(loadtextfile(prefs.basename));
  }

  void saveas(ActionEvent e) {
    FileDialog fd = new FileDialog(this,"save program (.wl)",FileDialog.SAVE);
    fd.setDirectory((new File(prefs.basename)).getParent());
    fd.setFile((new File(prefs.basename)).getName()); //File f = new File(); f.
    fd.setVisible(true);
    String name = fd.getFile();
    if(name==null) return;
    prefs.basename = (new File(fd.getDirectory(),name)).toString();
    save(e);
  }

  void save(ActionEvent e) {
    savetextfile(prefs.basename,textArea1.getText());
    changed = false;
  }

  void savecfg() {
    setPrefs(prefs.toString());
  }

  void quit(ActionEvent e) {
    if(changed) saveas(e);
    savecfg();
    System.exit(1);
  }

  void run(ActionEvent e) {
    textArea2.setText("");
    WadParse wp = new WadParse(textArea1.getText(),this);
    if(wp.err!=null) {
      textArea1.setCaretPosition(wp.pos);
    } else {
      msg("parsed successfully, evaluating...");
      wp.run();
      msg("done.");
      if(lastwp!=null && lastwp.wr.zoomed && (lastwp.wr.basescale<0.99f || lastwp.wr.basescale>1.01f)) {
        wp.wr.zoomed = true;
        wp.wr.basescale = lastwp.wr.basescale;
        wp.wr.scale = lastwp.wr.scale;
        wp.wr.xmid = lastwp.wr.xmid;
        wp.wr.ymid = lastwp.wr.ymid;
      };
      lastwp = wp;
      cv.repaint();
    };
  }

  String savewad(ActionEvent e) {
    run(e);
    String wadfile = null;
    if(lastwp==null || lastwp.err!=null) {
      //msg("wadsave: can only save wad after program has been run successfully");
    } else {
      save(e);
      wadfile = prefs.basename.substring(0,prefs.basename.lastIndexOf('.'))+".wad";
      new Wad(lastwp,this,wadfile);
    };
    return wadfile;
  }

  void textArea1_textValueChanged(TextEvent e) {
    changed = true;
    if(lastwp!=null) {
      if(lastwp.editchanged==0) lastwp.editchanged = 2;
      if(lastwp.editchanged==1) lastwp.editchanged = 0;
    };
  }

  // helper routine for invoking bsp, doom, etc.
  void subcmd(List<String> cmd) {
    ProcessBuilder pb = new ProcessBuilder(cmd);
    pb.inheritIO();
    msg("launching: "+ String.join(" ", cmd));

    try {
      if(pb.start().waitFor() != 0) msg(cmd.get(0) +" cmd failed?");
    } catch(Exception e) {
      msg(cmd.get(0) + " command interrupted!");
    };
  }

  void bspdoom(String wadfile) {
    if(wadfile==null) return;

    subcmd(Arrays.asList(prefs.bspcmd, wadfile, "-o", wadfile));

    ArrayList<String> cmd = new ArrayList<String>();
    cmd.add(prefs.doomexe);
    cmd.addAll(Arrays.asList(prefs.doomargs.split("\\s+")));
    cmd.addAll(Arrays.asList(prefs.twad1, prefs.twad2, prefs.twad3, wadfile).stream()
        .filter(s -> !"".equals(s))
        .collect(Collectors.toList()));

    subcmd(cmd);
  }

  public String getText() {
      return textArea1.getText();
  }

  public void insert(String s, int pos) {
      textArea1.insert(s, pos);
  }
}

class MyCanvas extends Canvas {
  MainFrame mf;
  boolean dragged;
  int startx, starty;
  MyCanvas c = this;

  public void paint(Graphics g) {
    if(mf.lastwp!=null) mf.lastwp.wr.render(g);
  }

  MyCanvas(MainFrame m) {
    mf = m;
    setBackground(Color.black);
    enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        dragged = false;
        startx = e.getX();
        starty = e.getY();
      }
      public void mouseReleased(MouseEvent e) {
        Graphics g = c.getGraphics();
        if(mf.lastwp!=null) {
          if(e.getButton() != MouseEvent.BUTTON1) {
            mf.lastwp.wr.zoom(e.getX(),e.getY(),2.0f);
          } else if((e.getModifiers()&MouseEvent.CTRL_MASK)!=0) {
             mf.lastwp.wr.addstep(e.getX(),e.getY(),'L');
          } else if((e.getModifiers()&MouseEvent.ALT_MASK)!=0) {
             mf.lastwp.wr.addstep(e.getX(),e.getY(),'C');
          } else if((e.getModifiers()&MouseEvent.SHIFT_MASK)!=0) {
             mf.lastwp.wr.addstep(e.getX(),e.getY(),'J');
          } else if(dragged && Math.abs(startx-e.getX())+Math.abs(starty-e.getY())>10) {
            mf.lastwp.wr.pan(startx,starty,e.getX(),e.getY());
          } else {
            mf.lastwp.wr.zoom(e.getX(),e.getY(),0.5f);
          };
          repaint();
        };
      }
      public void mouseClicked(MouseEvent e) {
      };
    });
    addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent e) {
        dragged = true;
        if(mf.lastwp!=null) mf.lastwp.wr.crosshair(c.getGraphics(),(e.getModifiers()&MouseEvent.CTRL_MASK)!=0);
      };
    });
  }
}
