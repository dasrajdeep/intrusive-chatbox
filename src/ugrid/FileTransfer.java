package ugrid;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.nio.*;

public class FileTransfer {
    
    private int port;
    private ServerSocket server;
    private final ByteBuffer buffer;
    private String lastfile;
    private long filesize=0;
    private int filecount=0;
    private Peer peer;
    
    public FileTransfer(Peer peer) {
        this.port=9792;
        this.peer=peer;
        this.lastfile="transfer";
        buffer=ByteBuffer.allocateDirect(1024*1024);
        try {
            server=new ServerSocket(this.port);
            Thread th=new Thread(new Runnable(){
                public void run() {
                    try {
                        Socket sock=server.accept();
                        File file=new File(lastfile);
                        while(file.exists()) file=new File("duplicate"+(++filecount)+"_"+lastfile);
                        receive(sock,file);
                    } catch(IOException ex){}
                }
            });
            th.setDaemon(true);
            th.start();
        } catch(IOException e){}
    }
    
    public void setFilesize(long size) {
        this.filesize=size;
    }
    
    public void setFilename(String name) {
        this.lastfile=name;
    }
    
    public String getLastFilename() {
        return this.lastfile;
    }
    
    public void receive(Socket socket, File file) {
        peer.dump("File transfer initiated.");
        try {
            if(!file.exists()) file.createNewFile();
            peer.dump("File: "+file.getCanonicalPath());
            OutputStream output=new FileOutputStream(file);
            InputStream input=socket.getInputStream();
            
            boolean success=transfer(input,output);
            if(success) peer.dump("Transfer complete.");
            else peer.dump("Transfer failed.");
        } catch(Exception e) {
            peer.dump("Transfer failed.");
        }
    }
    
    public boolean transmit(File file, InetAddress destination) {
        try {
            Socket socket=new Socket(destination,port);
            InputStream input=new FileInputStream(file);
            OutputStream output=socket.getOutputStream();
            
            boolean success=transfer(input,output);
            socket.close();
            
            if(success) return true;
            else return false;
        } catch(Exception e) {
            peer.dump(e.getMessage());
            return false;
        }
    }
    
    public boolean transfer(InputStream input, OutputStream output) {
        try {
            ReadableByteChannel inputChannel=Channels.newChannel(input);
            WritableByteChannel outputChannel=Channels.newChannel(output);
            
            long sent=0;
            while(inputChannel.read(buffer)!=-1) {
                buffer.flip();
                sent+=buffer.remaining();
                getPercent(sent);
                outputChannel.write(buffer);
                buffer.compact();
            }
            buffer.flip();
            sent+=buffer.remaining();
            getPercent(sent);
            while(buffer.hasRemaining()) outputChannel.write(buffer);
            
            inputChannel.close();
            outputChannel.close();
            getPercent(0);
            return true;
        } catch(IOException e) {
            return false;
        }
    }
    
    private long getPercent(long sent) {
        //if(peer.silent()) return 0;
        if(this.filesize==0) return -1;
        long percent=(sent*100)/this.filesize;
        peer.ugrid.setProgress(Integer.valueOf(""+percent));
        return percent;
    }
}
