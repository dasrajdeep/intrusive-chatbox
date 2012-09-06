package ugrid;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.awt.*;

public class GridTerminal extends javax.swing.JFrame {
    
    private String remotesite="ugrid.php";
    private static Peer peer;
    private javax.swing.table.DefaultTableModel model;
    private javax.swing.DefaultComboBoxModel combo;
    private javax.swing.JFileChooser chooser;
    private NewUser newuser;
    
    public GridTerminal() {
        initComponents();
        this.setTray();
        chooser=new javax.swing.JFileChooser();
        newuser=new NewUser(this,true);
        model=(javax.swing.table.DefaultTableModel)peer_table.getModel();
        combo=(javax.swing.DefaultComboBoxModel)peerbox.getModel();
        combo.removeAllElements();
        peer=new Peer(remotesite,this);
        menu_sync.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                connect();
            }
        });
        menu_manual.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                String ip=javax.swing.JOptionPane.showInputDialog(rootPane, "", "Enter Remote IP Address", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                peer.manualConnect(ip);
            }
        });
        menu_exit.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                System.exit(1);
            }
        });
        menu_loopback.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                String local[]={"127.0.0.1","self"};
                if(!peer.connectPeer(local)) peer.dump("Unable to bind to localhost.");
            }
        });
        input.addKeyListener(new KeyAdapter(){
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_ENTER) {
                    getInput();
                }
            }
        });
        peerbox.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                String peername=(String)peerbox.getSelectedItem();
                if(peername==null) return;
                status.setText(peername);
                String addr=peername.substring(peername.lastIndexOf(':'));
                peer.setCurrentNode(addr);
            }
        });
    }
    
    private void setTray() {
        URL url=this.getClass().getResource("/images/main.png");
        Image img=Toolkit.getDefaultToolkit().getImage(url);
        this.setIconImage(img);
        if(!SystemTray.isSupported()) return;
        try {
            SystemTray tray=SystemTray.getSystemTray();
            TrayIcon icon=new TrayIcon(img,"uGrid terminal");
            icon.setImageAutoSize(true);
            tray.add(icon);
        } catch(Exception e) {}
    }
    
    public java.io.File getFile() {
        chooser.showOpenDialog(this);
        chooser.setMultiSelectionEnabled(false);
        java.io.File file=chooser.getSelectedFile();
        return file;
    }
    
    public void showRegistration() {
        this.newuser.setVisible(true);
    }
    
    public boolean registered() {
        return this.newuser.valid;
    }
    
    public String getRegName() {
        return this.newuser.username;
    }
    
    public void setPeer(String peername) {
        this.status.setText(peername);
    }
    
    public void enlistPeer(String peername) {
        String row[]={peername};
        this.model.addRow(row);
        this.combo.addElement(peername);
    }
    
    public void delistPeer(String peername) {
        int index=-1;
        for(int i=0;i<model.getRowCount();i++) {
            if(this.model.getValueAt(i, 0).equals(peername)) index=i;
        }
        if(index!=-1) this.model.removeRow(index);
        this.combo.removeElement(peername);
        if(this.status.getText().equals(peername)) this.status.setText(peer.getCurrentNode());
    }
    
    public void connect() {
        new Thread(new Runnable() {
            public void run() {
                peer.plugIn();
            }
        }).start();
    }
    
    public void getInput() {
        String text=input.getText().trim();
        peer.dispatcher.handleOutgoing(text);
        input.setText("");
    }
    
    public void setProgress(int val) {
        this.progress.setValue(val);
    }
    
    public javax.swing.JTextArea getTerminal() {
        return this.terminal;
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuItem3 = new javax.swing.JMenuItem();
        jDialog1 = new javax.swing.JDialog();
        panel_bottom = new javax.swing.JPanel();
        progress = new javax.swing.JProgressBar();
        jScrollPane1 = new javax.swing.JScrollPane();
        peer_table = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        terminal = new javax.swing.JTextArea();
        input = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        status = new javax.swing.JTextField();
        peerbox = new javax.swing.JComboBox();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        menu_exit = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        menu_manual = new javax.swing.JMenuItem();
        menu_sync = new javax.swing.JMenuItem();
        menu_loopback = new javax.swing.JMenuItem();

        jMenuItem3.setText("jMenuItem3");

        jDialog1.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        jDialog1.setTitle("New uGrid User");
        jDialog1.setAlwaysOnTop(true);
        jDialog1.setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jDialog1Layout.setVerticalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("inChat");
        setBounds(new java.awt.Rectangle(0, 0, 400, 300));
        setLocationByPlatform(true);
        setPreferredSize(new java.awt.Dimension(500, 400));

        panel_bottom.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout panel_bottomLayout = new javax.swing.GroupLayout(panel_bottom);
        panel_bottom.setLayout(panel_bottomLayout);
        panel_bottomLayout.setHorizontalGroup(
            panel_bottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel_bottomLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(progress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panel_bottomLayout.setVerticalGroup(
            panel_bottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(progress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        peer_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Peer "
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(peer_table);

        terminal.setBackground(new java.awt.Color(0, 0, 51));
        terminal.setColumns(20);
        terminal.setForeground(new java.awt.Color(255, 255, 255));
        terminal.setLineWrap(true);
        terminal.setRows(5);
        terminal.setWrapStyleWord(true);
        jScrollPane2.setViewportView(terminal);

        input.setMinimumSize(new java.awt.Dimension(10, 20));
        input.setName("");
        input.setPreferredSize(new java.awt.Dimension(10, 20));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        status.setBackground(new java.awt.Color(0, 0, 51));
        status.setEditable(false);
        status.setForeground(new java.awt.Color(255, 255, 255));

        peerbox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(status, javax.swing.GroupLayout.PREFERRED_SIZE, 387, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(peerbox, 0, 62, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(status, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(peerbox, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jMenu1.setText("Terminal");

        menu_exit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        menu_exit.setText("Exit");
        jMenu1.add(menu_exit);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Connect");

        menu_manual.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.CTRL_MASK));
        menu_manual.setText("Manual Connect");
        jMenu2.add(menu_manual);

        menu_sync.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        menu_sync.setText("Remote Sync");
        jMenu2.add(menu_sync);

        menu_loopback.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        menu_loopback.setText("Loopback");
        menu_loopback.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menu_loopbackActionPerformed(evt);
            }
        });
        jMenu2.add(menu_loopback);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel_bottom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(input, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2)))
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(input, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panel_bottom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void menu_loopbackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menu_loopbackActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_menu_loopbackActionPerformed

    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GridTerminal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GridTerminal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GridTerminal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GridTerminal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new GridTerminal().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField input;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JMenuItem menu_exit;
    private javax.swing.JMenuItem menu_loopback;
    private javax.swing.JMenuItem menu_manual;
    private javax.swing.JMenuItem menu_sync;
    private javax.swing.JPanel panel_bottom;
    private javax.swing.JTable peer_table;
    private javax.swing.JComboBox peerbox;
    private javax.swing.JProgressBar progress;
    private javax.swing.JTextField status;
    private javax.swing.JTextArea terminal;
    // End of variables declaration//GEN-END:variables
}
