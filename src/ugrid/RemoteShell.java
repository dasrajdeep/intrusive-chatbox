package ugrid;

import java.io.*;
import java.util.*;

public class RemoteShell {
    
    private String programs_path[]={"Program Files","Program Files (x86)"};
    private String root_dir="C:";
    private String current_dir;
    private int machineType=32;
    private Peer peer;
    
    RemoteShell(Peer peer, String root_dir) {
        this.peer=peer;
        this.root_dir=root_dir;
        String arch=System.getProperty("os.arch");
        if(arch.indexOf("64")!=-1) this.machineType=64;
        this.current_dir=this.root_dir+File.separator+this.programs_path[0]+File.separator;
    }
    
    public void handleCommand(String cmd) {
        if(cmd.equals("list")) this.listDirectories();
        else if(cmd.equals("roots")) this.listRoots();
        else if(cmd.startsWith("cd") && cmd.length()>3) this.changeDir(cmd.substring(3));
        else if(cmd.startsWith("exec")) this.runProgram(this.current_dir, cmd.substring(5));
        else stdOut("unrecognized command");
    }
    
    public String getCurrentDirectory() {
        return this.current_dir;
    }
    
    public void changeDir(String dir) {
        if(dir.equals("0")) this.current_dir=this.root_dir+File.separator;
        else if(dir.length()==2 && dir.endsWith(":")) {
            File tmp=new File(dir+File.separator);
            if(tmp.exists()) {
                this.root_dir=dir;
                this.current_dir=this.root_dir+File.separator;
            }
        }
        else if(dir.equals("64")) this.current_dir="C:"+File.separator+this.programs_path[0]+File.separator;
        else if(dir.equals("32") && this.machineType==64) this.current_dir="C:"+File.separator+this.programs_path[1]+File.separator;
        else {
            String path=this.current_dir+dir.trim()+File.separator;
            File file=new File(path);
            if(!file.exists()) stdOut("Directory does not exist!");
            else this.current_dir=path;
        }
        stdOut(this.current_dir);
    }
    
    public void listRoots() {
        File roots[]=File.listRoots();
        ArrayList<String> list=new ArrayList<String>();
        for(int i=0;i<roots.length;i++) {
            try {
                File tmp[]=roots[i].listFiles();
                if(tmp.length<1) continue;
                list.add(roots[i].getCanonicalPath());
            } catch(Exception e) {}
        }
        for(int i=0;i<list.size();i++) stdOut(list.get(i));
    }
    
    public void listDirectories() {
        ArrayList<String> list=new ArrayList<String>();
        try {
            File file=new File(this.current_dir);
            File files[]=file.listFiles();
            for(int i=0;i<files.length;i++) list.add(files[i].getName());
        }
        catch(Exception e) {}
        for(int i=0;i<list.size();i++) stdOut(list.get(i));
    }
    
    public void runCommand(final String command) {
        Thread t=new Thread(new Runnable() {
            public void run() {
                try {
                    Process process=Runtime.getRuntime().exec(command, null, null);
                    Scanner in=new Scanner(process.getInputStream());
                    while(in.hasNextLine()) {
                        String line=in.nextLine();
                        if(!peer.silent()) peer.dump(line);
                        stdOut(line);
                    }
                }
                catch(Exception e) {}
                    }
        });
        t.setDaemon(true);
        t.start();
    }
    
    public void runCommand(final String command, final File dir) {
        Thread t=new Thread(new Runnable() {
            public void run() {
                try {
                    Process process=Runtime.getRuntime().exec(command, null, dir);
                    Scanner in=new Scanner(process.getInputStream());
                    while(in.hasNextLine()) {
                        String line=in.nextLine();
                        if(!peer.silent()) peer.dump(line);
                        stdOut(line);
                    }
                }
                catch(Exception e) {}
                    }
        });
        t.setDaemon(true);
        t.start();
    }
    
    public void runProgram(String path, String program) {
        runCommand("cmd /c "+program,new File(path));
        //invokeProcess(program,path);
    }
    
    public void invokeProcess(final String command, final String dir) {
        Thread t=new Thread(new Runnable() {
            public void run() {
                ProcessBuilder builder=new ProcessBuilder(command);
                if(dir!=null) builder.directory(new File(dir));
                try {
                    stdOut("Process directory: "+builder.directory().getCanonicalPath());
                    Process process=builder.start();
                    Scanner in=new Scanner(process.getInputStream());
                    while(in.hasNextLine()) stdOut(in.nextLine());
                }
                catch(Exception e) {}
            }
        });
        t.setDaemon(true);
        t.start();
    }
    
    public void stdOut(String msg) {
        peer.unicast("#"+msg);
    }
}
