package ugrid;

import java.net.*;
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

public class Peer {
    
    private int port;
    private boolean silent=false;
    private PeerNode currentNode=null;
    private String remotelogger;
    private ServerSocket server;
    private ArrayList<PeerNode> sockets;
    private ArrayList<String> adapters;
    private javax.swing.JTextArea terminal;
    private int maxTrials=5;
    private boolean registered=false;
    private String username=null;
    
    public GridTerminal ugrid;
    public Dispatcher dispatcher;
    public boolean remoteLogging=false;
    
    Peer(String remotesite, GridTerminal ugrid) {
        this.port=9791;
        this.ugrid=ugrid;
        this.terminal=ugrid.getTerminal();
        this.remotelogger=remotesite;
        this.dispatcher=new Dispatcher(this);
        this.sockets=new ArrayList<PeerNode>();
        this.adapters=new ArrayList<String>();
        try {
            this.getInterfaces();
            new Thread(new Runnable() {
                public void run() {plugIn();}
            }).start();//plugIn thread
            this.server=new ServerSocket(this.port);
            Thread t=new Thread(new Runnable(){
                public void run() {
                    while(true) {
                        try {
                            Socket sock=server.accept();
                            sockets.add(new PeerNode(sock,null));
                        } catch(IOException e) {}
                    }
                }
            });//server thread
            t.setDaemon(true);
            t.start();
        } catch(IOException e) {
            dump("Unable to host a server on this machine!");
        }
    }
    
    public void setMaxTrials(int n) {
        this.maxTrials=n;
    }
    
    public void dump(String line) {
        terminal.append(line+"\n");
        terminal.setCaretPosition(terminal.getText().length());
    }
    
    public boolean connectPeer(String node[]) {
        try {
            Socket sock=new Socket(node[0],this.port);
            sockets.add(new PeerNode(sock,node[1]));
            return true;
        } catch(IOException e) {return false;}
    }
    
    public boolean manualConnect(String remote) {
        if(remote.isEmpty()) return false;
        try {
            Socket sock=new Socket(remote,this.port);
            sockets.add(new PeerNode(sock,null));
            return true;
        } catch(IOException e) {return false;}
    }
    
    public void unicast(String message) {
        if(sockets.isEmpty()) dump("No peers connected.");
        PeerNode peer=this.currentNode;
        if(peer==null) return;
        peer.out.println(message);
        peer.out.flush();
    }
    
    public void broadcast(String message) {
        if(sockets.isEmpty()) dump("No peers connected.");
        for(int i=0;i<sockets.size();i++) {
            sockets.get(i).out.println(message);
            sockets.get(i).out.flush();
        }
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public InetAddress getCurrentNodeAddress() {
        return this.currentNode.socket.getInetAddress();
    }
    
    public String getCurrentNode() {
        if(this.currentNode==null) return "no peers available";
        return this.currentNode.name+"::"+this.currentNode.address;
    }
    
    public void setCurrentNode(String nodename) {
        PeerNode node=null;
        for(int i=0;i<sockets.size();i++) {
            if(sockets.get(i).address.equals(nodename)) node=sockets.get(i);
        }
        if(node!=null) this.setCurrentNode(node);
    }
    
    public void setCurrentNode(PeerNode node) {
        this.currentNode=node;
    }
    
    public boolean silent() {
        return this.silent;
    }
    
    public void silence() {
        this.silent=true;
    }
    
    public void unsilence() {
        this.silent=false;
    }
    
    private void getInterfaces() throws IOException {
        Enumeration interfaces=NetworkInterface.getNetworkInterfaces();
        while(interfaces.hasMoreElements()) {
            NetworkInterface net=(NetworkInterface)interfaces.nextElement();
            byte addr[]=net.getHardwareAddress();
            if(addr==null || addr.length!=6) continue;
            String mac=getByte(addr[0]);
            for(int i=1;i<addr.length;i++) mac+=":"+getByte(addr[i]);
            adapters.add(mac);
        }
    }
    
    private String getByte(byte b) {
        int addrByte=b;
        if(addrByte<0) addrByte&=255;
        return Integer.toHexString(addrByte);
    }
    
    public void plugIn() {
        if(!this.remoteLogging) {
            dump("No Remote Logger Registered. Only Manual Lookups are Possible.");
            return;
        }
        dump("Connecting to remote logger...");
        int max=this.maxTrials;
        boolean success=false;
        for(int trial=0;trial<max && !success;trial++) {
            if(trial>0) dump("Retrying (attempt "+trial+")...");
            try {
                String xml=getXML();
                URL u=new URL(this.remotelogger+"?xml="+URLEncoder.encode(xml, "UTF-8"));
                URLConnection con=u.openConnection();
                Scanner in=new Scanner(con.getInputStream());
                String xmldata="";
                while(in.hasNextLine()) {
                    String line=in.nextLine();
                    xmldata+=line;
                }
                dump("Connected to remote logger.");
                success=true;
                String peers[][]=getPeerList(xmldata);
                int count=0;
                dump("Connecting to remote peers...");
                for(int i=0;i<peers.length;i++) {if(connectPeer(peers[i])) count++;}
                if(count==0) dump("Unable to connect to any remote peer(s).");
            } catch(Exception e) {
                dump("Unable to connect to server.");
                try{Thread.sleep(5000);} catch(Exception ex){}
            }
        }
        if(!success) dump("Connection to remote logger failed! You will be offline.");
    }
    
    private String getXML() {
        String xml="<adapters>";
        String adpt[]=new String[adapters.size()];
        adpt=adapters.toArray(adpt);
        for(int i=0;i<adpt.length;i++) xml+="<mac>"+adpt[i]+"</mac>";
        xml+="</adapters>";
        return xml;
    }
    
    public String[][] getPeerList(String xml) {
        ArrayList<String[]> machines=new ArrayList<String[]>();
        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
        Document doc=null;
        try {
            DocumentBuilder dom=factory.newDocumentBuilder();
            InputSource is=new InputSource();
            is.setCharacterStream(new StringReader(xml));
            doc=dom.parse(is);
        } catch(Exception e) {}
        if(doc==null) {return new String[0][0];}
        for(Node mac=doc.getFirstChild().getFirstChild();mac!=null;mac=mac.getNextSibling()) {
            if(mac.getNodeName().equals("registered")) {
                String val=mac.getFirstChild().getNodeValue();
                if(val.equals("yes")) registered=true;
            }
            else if(mac.getNodeName().equals("username")) {
                Node child=mac.getFirstChild();
                if(child!=null) username=child.getNodeValue();
                else username="";
                if(username.trim().equals("")) username=null;
            }
            else {
                String machine[]=new String[2];
                for(Node c=mac.getFirstChild();c!=null;c=c.getNextSibling()) {
                    if(c.getNodeType()==Node.ELEMENT_NODE) {
                        if(c.getNodeName().equals("ip")) machine[0]=c.getFirstChild().getNodeValue();
                        else if(c.getNodeName().equals("username")) {
                            Node child=c.getFirstChild();
                            if(child!=null) machine[1]=child.getNodeValue();
                        }
                    }
                }
                machines.add(machine);
            }
        }
        String macs[][]=new String[machines.size()][2];
        macs=machines.toArray(macs);
        if(!registered) {
            ugrid.showRegistration();
            if(ugrid.registered()) {
                this.username=ugrid.getRegName();
                register();
            }
        }
        return macs;
    }
    
    public void register() {
        String name=this.username;
        dump("Registering user \""+name+"\" ...");
        int max=this.maxTrials;
        boolean success=false;
        for(int trial=0;trial<max && !success;trial++) {
            if(trial>0) dump("Retrying connection (attempt "+trial+")...");
            try {
                String xml=getXML();
                URL u=new URL("http://pingshot.x10.mx/ugrid_register.php?xml="+URLEncoder.encode(xml, "UTF-8")+"&username="+URLEncoder.encode(name, "UTF-8"));
                URLConnection con=u.openConnection();
                success=true;
                Scanner in=new Scanner(con.getInputStream());
                while(in.hasNextLine()) dump(in.nextLine());
            } catch(IOException e) {
                dump("Unable to connect to server.");
                try{Thread.sleep(5000);} catch(Exception ex){}
            }
        }
        if(!success) dump("Unable to register this user!");
    }
    
    public class PeerNode {
        
        PeerNode self;
        String name=null;
        String address=null;
        Socket socket;
        PrintWriter out;
        Scanner in;
        
        PeerNode(Socket sock, String username) throws IOException {
            this.self=this;
            if(username==null) username="unknown";
            if(sock.getInetAddress().getHostAddress().equals("127.0.0.1")) username="self";
            this.name=username;
            this.socket=sock;
            if(sock!=null) {
                address=socket.getInetAddress().getHostAddress();
                dump("Connected to peer ("+name+"::"+address+")");
                ugrid.enlistPeer(name+"::"+address);
                in=new Scanner(socket.getInputStream());
                out=new PrintWriter(socket.getOutputStream());
                currentNode=self;
                ugrid.setPeer(currentNode.name+"::"+currentNode.address);
                Thread t=new Thread(new Runnable(){
                    public void run() {
                        while(in.hasNextLine()) {
                            String line=in.nextLine().trim();
                            interpret(line);
                        }
                        dump("Disconnected from peer ("+name+"::"+address+")");
                        sockets.remove(self);
                        if(currentNode==self) {
                            if(sockets.isEmpty()) currentNode=null;
                            else currentNode=sockets.get(sockets.size()-1);
                        }
                        ugrid.delistPeer(name+"::"+address);
                    }
                });
                t.setDaemon(true);
                t.start();
            }
        }
        public void interpret(String line) {
            if(line.startsWith("$")) {
                line=line.substring(1);
                dispatch(line);
            }
            else dispatcher.handleIncoming(line,name,address);
        }
        public void dispatch(String cmd) {
            if(cmd.startsWith("username")) {
                cmd=cmd.substring(10);
                this.name=cmd;
            }
        }
        
    }
}
