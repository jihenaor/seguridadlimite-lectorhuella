package lectorhuella;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

public class IngresoView extends javax.swing.JDialog {
    
    public IngresoView(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                init();
                start();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                
            }

        });
    }
    
    protected void init(){
        /*
        lector.addDataListener((DPFPDataEvent dpfpde) -> {
            procesarHuella(dpfpde.getSample());
        });
        
        lector.addReaderStatusListener(new DPFPReaderStatusListener() {
            @Override
            public void readerConnected(DPFPReaderStatusEvent dpfprs) {
                lblHuella.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("/img/lector_conectado_2.png")).getImage().getScaledInstance(lblHuella.getWidth(), lblHuella.getHeight(), Image.SCALE_DEFAULT)));
            }

            @Override
            public void readerDisconnected(DPFPReaderStatusEvent dpfprs) {
                lblHuella.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("/img/lector_desconectado.png")).getImage().getScaledInstance(lblHuella.getWidth(), lblHuella.getHeight(), Image.SCALE_DEFAULT)));
            }
        });
*/
    }
    
//    protected void procesarHuella(DPFPSample sample) {
//        Image image = DPFPGlobal.getSampleConversionFactory().createImage(sample);
//        drawPicture(image);
//    }
    
    public void drawPicture(Image image) {
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bufferedImage.createGraphics();
        bGr.drawImage(image, 0, 0, null);
        bGr.dispose();

        AffineTransform tx = new AffineTransform();
        tx.rotate(Math.toRadians(180), bufferedImage.getWidth() / 2, bufferedImage.getHeight() / 2);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
        bufferedImage = op.filter(bufferedImage, null);
        lblHuella.setIcon(new ImageIcon(image.getScaledInstance(lblHuella.getWidth(), lblHuella.getHeight(), Image.SCALE_DEFAULT)));
    }
    
    protected void start(){
//        lector.startCapture();
    }
    
  /*  
    protected DPFPFeatureSet extractFeatures(DPFPSample sample, DPFPDataPurpose purpose){
        DPFPFeatureExtraction extractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
        try {
            return extractor.createFeatureSet(sample, purpose);
        } catch (DPFPImageQualityException e) {
            return null;
        }
    }
    */
    @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    lblHuella = new javax.swing.JLabel();
    jScrollPane1 = new javax.swing.JScrollPane();
    m_text = new javax.swing.JTextArea();
    jLabel1 = new javax.swing.JLabel();
    txtNumerodocumento = new javax.swing.JTextField();
    jbBuscar = new javax.swing.JButton();
    lblNombre = new javax.swing.JLabel();
    lblFoto = new javax.swing.JLabel();
    btnSalir = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

    lblHuella.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    lblHuella.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/lector_conectado.png"))); // NOI18N
    lblHuella.setBorder(javax.swing.BorderFactory.createEtchedBorder());

    jScrollPane1.setName("m_text"); // NOI18N

    m_text.setEditable(false);
    m_text.setColumns(20);
    m_text.setRows(5);
    m_text.setName(""); // NOI18N
    jScrollPane1.setViewportView(m_text);

    jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
    jLabel1.setText("Numero de documento del aprendiz");

    txtNumerodocumento.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

    jbBuscar.setText("Buscar");
    jbBuscar.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jbBuscarActionPerformed(evt);
      }
    });

    lblNombre.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N

    lblFoto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    lblFoto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/lector_conectado.png"))); // NOI18N
    lblFoto.setBorder(javax.swing.BorderFactory.createEtchedBorder());

    btnSalir.setText("Salir");
    btnSalir.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnSalirActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addGroup(layout.createSequentialGroup()
            .addComponent(lblHuella, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jScrollPane1))
          .addComponent(jLabel1)
          .addGroup(layout.createSequentialGroup()
            .addComponent(txtNumerodocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jbBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addComponent(lblNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 537, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(lblFoto, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
        .addGap(2, 2, 2))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(jLabel1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jbBuscar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addGroup(layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))))
          .addGroup(layout.createSequentialGroup()
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(txtNumerodocumento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(lblNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
          .addComponent(lblFoto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(lblHuella, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

    private void jbBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbBuscarActionPerformed

    }//GEN-LAST:event_jbBuscarActionPerformed

  private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalirActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_btnSalirActionPerformed

//    public static void main(String args[]) {
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Windows".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(CapturarHuella.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        java.awt.EventQueue.invokeLater(() -> {
//            CapturarHuella dialog = new CapturarHuella(new javax.swing.JFrame(), true);
//            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
//                @Override
//                public void windowClosing(java.awt.event.WindowEvent e) {
//                    System.exit(0);
//                }
//            });
//            dialog.setVisible(true);
//        });
//    }
    
  // Variables declaration - do not modify//GEN-BEGIN:variables
  protected javax.swing.JButton btnSalir;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JScrollPane jScrollPane1;
  protected javax.swing.JButton jbBuscar;
  protected javax.swing.JLabel lblFoto;
  protected javax.swing.JLabel lblHuella;
  protected javax.swing.JLabel lblNombre;
  public static javax.swing.JTextArea m_text;
  protected javax.swing.JTextField txtNumerodocumento;
  // End of variables declaration//GEN-END:variables
}
