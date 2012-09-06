package ugrid;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

public class Dispatcher {
    
    private Peer peer;
    private RemoteShell remote;
    private FileTransfer transfer;
    
    public Dispatcher(Peer peer) {
        this.peer=peer;
        remote=new RemoteShell(this.peer,"C:");
        transfer=new FileTransfer(this.peer);
    }
    
    public void handleIncoming(String line, String name, String addr) {
        if(line.startsWith("@")) peer.dump("["+name+"::"+addr+"]: "+line.substring(1));
        else if(line.startsWith("?")) remote.handleCommand(line.substring(1));
        else if(line.startsWith("#")) peer.dump(line.substring(1));
        else if(line.startsWith("!")) console(line.substring(1),addr);
        else if(line.startsWith(":")) remote.runCommand(line.substring(1));
    }
    
    public void handleOutgoing(String cmd) {
        String user=peer.getUsername();
        if(user==null) user="me";
        
        if(cmd.startsWith("?")) peer.dump(">"+cmd.substring(1));
        else if(cmd.startsWith(":")) peer.dump(">"+cmd.substring(1));
        else if(cmd.startsWith("!")) peer.dump(cmd);
        else if(cmd.startsWith("+")) console(cmd.substring(1),null);
        else {
            peer.dump(user+": "+cmd);
            cmd="@"+cmd;
        }
        
        if(!cmd.startsWith("+")) peer.unicast(cmd);
    }
    
    public void console(String cmd, String addr) {
        if(cmd.equals("silent")) peer.silence();
        else if(cmd.equals("loud")) peer.unsilence();
        else if(cmd.equals("sendfile")) transmitFile();
        else if(cmd.startsWith("getfile")) receiveFile(cmd.substring(8),addr);
        else if(cmd.startsWith("filename")) transfer.setFilename(cmd.substring(9));
        else if(cmd.startsWith("filesize")) transfer.setFilesize(Long.parseLong(cmd.substring(9)));
    }
    
    public void receiveFile(String filename, final String addr) {
        String dir=remote.getCurrentDirectory();
        final File file=new File(dir+filename);
        peer.unicast(dir+filename);
        if(!file.exists()) {
            peer.unicast("#File does not exist!");
            return;
        }
        Thread t=new Thread(new Runnable(){
            public void run() {
                peer.unicast("!filename "+file.getName());
                peer.unicast("!filesize "+file.length());
                transfer.setFilesize(file.length());
                try {
                    InetAddress a=InetAddress.getByName(addr);
                    transfer.transmit(file, a);
                } catch(IOException e){}
            }
        });
        t.setDaemon(true);
        t.start();
    }
    
    public void transmitFile() {
        final File file=peer.ugrid.getFile();
        if(file==null) peer.dump("No file chosen!");
        else {
            Thread t=new Thread(new Runnable(){
                public void run() {
                    handleOutgoing("!filename "+file.getName());
                    handleOutgoing("!filesize "+file.length());
                    transfer.setFilesize(file.length());
                    boolean success=transfer.transmit(file, peer.getCurrentNodeAddress());
                    if(success) peer.dump("Transfer complete.");
                    else peer.dump("Transfer failed!");
                }
            });
            t.setDaemon(true);
            t.start();
        }
    }
    
}
