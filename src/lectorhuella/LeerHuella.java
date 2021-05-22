package lectorhuella;

import DB.ConexionBD;
import Decoder.BASE64Decoder;
import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import pojo.Trabajador;


public class LeerHuella extends CapturarHuella implements ActionListener{
    
    public class EnrollmentThread extends Thread implements Engine.EnrollmentCallback {
            public static final String ACT_PROMPT   = "enrollment_prompt";
            public static final String ACT_CAPTURE  = "enrollment_capture";
            public static final String ACT_FEATURES = "enrollment_features";
            public static final String ACT_DONE     = "enrollment_done";
            public static final String ACT_CANCELED = "enrollment_canceled";
                        
            public class EnrollmentEvent extends ActionEvent{
                private static final long serialVersionUID = 102;

                public Reader.CaptureResult capture_result;
                public Reader.Status        reader_status;
                public UareUException       exception;
                public Fmd                  enrollment_fmd;
			
                public EnrollmentEvent(Object source, String action, Fmd fmd, Reader.CaptureResult cr, Reader.Status st, UareUException ex){
                    super(source, ActionEvent.ACTION_PERFORMED, action);
                        capture_result = cr;
                        reader_status = st;
                        exception = ex;
                        enrollment_fmd = fmd;
                }
            }
		
            private final Reader   m_reader;
            private CaptureThread  m_capture;
            private final ActionListener m_listener;
            private boolean m_bCancel;
		
		protected EnrollmentThread(Reader reader, ActionListener listener){
			m_reader = reader;
			m_listener = listener;
		}
		
		public Engine.PreEnrollmentFmd GetFmd(Fmd.Format format){
			Engine.PreEnrollmentFmd prefmd = null;

			while(null == prefmd && !m_bCancel){
				//start capture thread
				m_capture = new CaptureThread(m_reader, false, 
						Fid.Format.ISO_19794_4_2005,
                Reader.ImageProcessing.IMG_PROC_DEFAULT);
				m_capture.start(null);

				//prompt for finger
				SendToListener(ACT_PROMPT, null, null, null, null);

				//wait till done
				m_capture.join(0);
				
				//check result
				CaptureThread.CaptureEvent evt = m_capture
                .getLastCaptureEvent();
				if(null != evt.capture_result){
                                    //Captura una huella
					if(Reader.CaptureQuality.CANCELED == evt.capture_result.quality){
						//capture canceled, return null
						break;
					}
					else if(null != evt.capture_result.image && Reader.CaptureQuality.GOOD == evt.capture_result.quality){
						//acquire engine
                                                // Captura una huella
						Engine engine = UareUGlobal.GetEngine();
							
						try{
							//extract features
							Fmd fmd = engine.CreateFmd(
                      evt.capture_result.image, 
									Fmd.Format.DP_PRE_REG_FEATURES);

							//return prefmd 
							prefmd = new Engine.PreEnrollmentFmd();
							prefmd.fmd = fmd;
							prefmd.view_index = 0;

              if (evt != null) {
                if (evt.capture_result != null) {
                  Fid.Fiv view = evt.capture_result.image.getViews()[0];
                  BufferedImage m_image;
                  int w;
                  int h;

                  m_image = new BufferedImage(view.getWidth(), view.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
                  m_image.getRaster().setDataElements(0, 0, view.getWidth(), view.getHeight(), view.getImageData());
                  w = view.getWidth();
                  h = view.getHeight();
                  
                  BufferedImage bufferedImage = new BufferedImage(m_image.getWidth(null), m_image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                  Graphics2D bGr = bufferedImage.createGraphics();
                  bGr.drawImage(m_image, 0, 0, null);
                  bGr.dispose();

                  AffineTransform tx = new AffineTransform();
                  tx.rotate(Math.toRadians(180), bufferedImage.getWidth() / 2, bufferedImage.getHeight() / 2);
                  AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
                  bufferedImage = op.filter(bufferedImage, null);
                  lblHuella.setIcon(new ImageIcon(m_image.getScaledInstance(lblHuella.getWidth(), lblHuella.getHeight(), Image.SCALE_DEFAULT)));
	              }
              }
            //send success
							SendToListener(ACT_FEATURES, null, null, null, null);
						}	catch(UareUException e){ 
							//send extraction error
							SendToListener(ACT_FEATURES, null, null, null, e);
						}
					}	else{
						//send quality result
						SendToListener(ACT_CAPTURE, null, evt.capture_result, 
                    evt.reader_status, evt.exception);
					}
				}	else{
					//send capture error
					SendToListener(ACT_CAPTURE, null, evt.capture_result, 
                  evt.reader_status, evt.exception);
				}
			}
			
			return prefmd;
		}
		
		public void cancel(){
      m_bCancel = true;
      if (null != m_capture){
        m_capture.cancel();
      }
		}
		
		private void SendToListener(String action, Fmd fmd, 
            Reader.CaptureResult cr, Reader.Status st, UareUException ex){
			if(null == m_listener || null == action || action.equals("")) 
        return;

			final EnrollmentEvent evt = new EnrollmentEvent(this, action, fmd, 
              cr, st, ex);

			//invoke listener on EDT thread
	        try {
            javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
              public void run() {
                m_listener.actionPerformed(evt);
              }
            });
          } 
          catch (InvocationTargetException e) { 
            e.printStackTrace(); 
          } 
            catch (InterruptedException e) { 
                e.printStackTrace(); 
            }
        }
		
		public void run(){
			//acquire engine
			Engine engine = UareUGlobal.GetEngine();
			
			try{
				m_bCancel = false;
				while(!m_bCancel){
					//run enrollment
					Fmd fmd = engine.CreateEnrollmentFmd(
							Fmd.Format.DP_REG_FEATURES, this);
					
					//send result
					if(null != fmd){
						SendToListener(ACT_DONE, fmd, null, null, null);
					}
					else{
						SendToListener(ACT_CANCELED, null, null, null, null);
						break;
					}
				}
			} catch(UareUException e){ 
				SendToListener(ACT_DONE, null, null, null, e);
			}
		}
	}
    
  private static final String ACT_BACK = "back";
  private static final String FIND_TRABAJADOR = "find_trabajador";
  private boolean BUSCAR = false;
  private EnrollmentThread m_enrollment;
  private Reader  m_reader;
  public Trabajador trabajador;
  private ImagePanel    m_image;
  private int paso = 0;

    private boolean m_bJustStarted;
    
    public LeerHuella(java.awt.Dialog parent, boolean modal, Reader reader) {
        super(parent, modal);
        m_reader = reader;
        m_bJustStarted = true;
        
        jbBuscar.setActionCommand(FIND_TRABAJADOR);
        jbBuscar.addActionListener(this);
        
        m_image = new ImagePanel();
        
        m_enrollment = new LeerHuella.EnrollmentThread(m_reader, this);
    }

    @Override
    public void actionPerformed(ActionEvent e){
        
      if(e.getActionCommand().equals(FIND_TRABAJADOR)){
        try {
            Gson gson = new Gson();
            if (txtNumerodocumento.getText() == null || txtNumerodocumento.getText().length() == 0) {
              txtNumerodocumento.setText("1097407340");
            }
            String r = util.ClienteRest.getText("trabajadorIdentification/" + txtNumerodocumento.getText()+"/N");

            trabajador = gson.fromJson(r, Trabajador.class);
            lblNombre.setText(trabajador.getNombrecompleto());
            if (trabajador.getBase64() != null) {
              byte[] imageByte;
              
              if (trabajador.getIdaprendiz() == null) {
                JOptionPane.showMessageDialog(null, "El trabajador no se encuentra en un grupo activo", "Error", JOptionPane.ERROR_MESSAGE);
              }

              BASE64Decoder decoder = new BASE64Decoder();
              imageByte = decoder.decodeBuffer(trabajador.getBase64());
              
              ImageIcon imageIcon = new ImageIcon(imageByte);
              imageIcon.getImage();
              
              lblFoto.setIcon(imageIcon);

            }
        } catch (JsonSyntaxException | IOException ex) {
          trabajador = null;
          JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return;
      }
      
        if(e.getActionCommand().equals(ACT_BACK)){
                //destroy dialog to cancel enrollment
            this.setVisible(false);
        }
        else{
            LeerHuella.EnrollmentThread.EnrollmentEvent evt = (LeerHuella.EnrollmentThread.EnrollmentEvent)e;

            if(e.getActionCommand().equals(LeerHuella.EnrollmentThread.ACT_PROMPT)){
                if(m_bJustStarted){
                  m_text.append("Enrrolamiento Iniciado\n");
                  m_text.append("    Pon un dedo en el lector\n");
                  paso = 0;
                  lblHuella.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("/img/lector_conectado_2.png")).getImage().getScaledInstance(lblHuella.getWidth(), lblHuella.getHeight(), Image.SCALE_DEFAULT)));
          }
                else{
                  m_text.append("    Pon el mismo dedo en el lector\n");
                  paso++;
                  switch (paso) {
                    case 1: 
                      lblPasos.setIcon(new ImageIcon(getClass().getResource("/img/paso1.png")));
                      break;
                    case 2: 
                      lblPasos.setIcon(new ImageIcon(getClass().getResource("/img/paso2.png")));
                      break;
                    case 3: 
                      lblPasos.setIcon(new ImageIcon(getClass().getResource("/img/paso3.png")));
                      break;
                    case 4: 
                      lblPasos.setIcon(new ImageIcon(getClass().getResource("/img/paso4.png")));
                      break;
                  }
                }

                m_bJustStarted = false;
            }
            else if(e.getActionCommand().equals(LeerHuella.EnrollmentThread.ACT_CAPTURE)){
              if (trabajador == null) {
                JOptionPane.showMessageDialog(null, "Digita el documento de un trabajador en un curso activo", "Error", JOptionPane.ERROR_MESSAGE);
                return;
              }
                    if(null != evt.capture_result){
                            MessageBox.BadQuality(evt.capture_result.quality);
                    }
                    else if(null != evt.exception){
                      lblHuella.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("/img/lector_desconectado.png")).getImage().getScaledInstance(lblHuella.getWidth(), lblHuella.getHeight(), Image.SCALE_DEFAULT)));
                      MessageBox.DpError("Capture", evt.exception);
                }
                    else if(null != evt.reader_status){
                            MessageBox.BadStatus(evt.reader_status);
                    }
                    m_bJustStarted = false;
            } else if(e.getActionCommand().equals(LeerHuella.EnrollmentThread.ACT_FEATURES)){
                if(null == evt.exception){
                  m_text.append("    Imagen capturada, caracteristicas extraidas\n\n");                 
                }
                else{
                        MessageBox.DpError("Feature extraction", evt.exception);
                }
                m_bJustStarted = false;
            }
            else if(e.getActionCommand().equals(LeerHuella.EnrollmentThread.ACT_DONE)){
                if(null == evt.exception){
                    try {
                        String huella = new String(evt.enrollment_fmd.getData(), Charset.forName("ISO-8859-1"));
                        //String huella = Base64.getMimeEncoder().encodeToString(evt.enrollment_fmd.getData());
                       
                        String str = String.format("    enrollment template creado, size: %d\n\n\n", evt.enrollment_fmd.getData().length);
                        m_text.append(str);
                        
                        byte[] huella2;
                        String sql;

                        huella2 = evt.enrollment_fmd.getData();
                        ConexionBD con = new ConexionBD();
                        con.conectar();                        
                        sql = "select idusuario"
                                + " from usuario"
                                + " where documento = '" + trabajador.getNumerodocumento() + "'";
                        
                        Statement st = con.getConexion().createStatement();

                        ResultSet rs = st.executeQuery(sql);

                        if (rs.next()) {
                          sql = "UPDATE usuario "
                                  + "SET huella = ?"
                                  + " where idusuario = " + rs.getLong("idusuario");
                        } else{
                          sql = "INSERT INTO usuario "
                                  + "(nombre,documento, huella) "
                                  + "VALUES("
                                  + "'"+ (trabajador == null ? "Sin id" : trabajador.getNombrecompleto()) + "',"
                                  + "'"+ (trabajador == null ? "Sin doc" : trabajador.getNumerodocumento()) + "',"
                                  + " ?)";
                        }
                        try {
                            PreparedStatement pst = con.getConexion().prepareStatement(sql);
                            pst.setBytes(1, huella2);
                            if(pst.executeUpdate()>0){
                                // cjnombre.setText(null);
                            }
                        } catch (SQLException ex) {
                            Logger.getLogger(LeerHuella.class.getName()).log(Level.SEVERE, null, ex);
                        }        
                        String data = "{\"idtrabajador\":" + (trabajador == null ? 0 : trabajador.getId()) + ","
                                + "\"huella\": \"" + huella + "\""
                                + "}";
                        System.out.println("Data:" + data);
//                        util.ClienteRest.sendPost(data, 
//                            "savehuella");

                        MessageBox.Warning("Actualizacion exitosa");
                    } catch (Exception ex) {
                        MessageBox.Warning("Error almacenando la huella " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
                else{
                        MessageBox.DpError("Enrollment template creation", evt.exception);
                }
                m_bJustStarted = true;
            }
            else if(e.getActionCommand().equals(LeerHuella.EnrollmentThread.ACT_CANCELED)){
                    //canceled, destroy dialogmacdeveloper
                this.setVisible(false);
            }

            //cancel enrollment if any exception or bad reader status
            if(null != evt.exception){
                this.setVisible(false);
            }
            else if(null != evt.reader_status && Reader.ReaderStatus.READY != evt.reader_status.status && Reader.ReaderStatus.NEED_CALIBRATION != evt.reader_status.status){
                this.setVisible(false);
            }
        }
    }
    /*
    private void saveDataToFile(byte[] data) {
		System.out.println(new String(data));

		// TODO Auto-generated method stub
		JFileChooser fc = new JFileChooser(new File("test"));

		fc.showSaveDialog(this);
		if (fc.getSelectedFile() != null) {
			OutputStream output = null;
			try {
				output = new BufferedOutputStream(new FileOutputStream(
						fc.getSelectedFile()));
				output.write(data);
				output.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(null, "Error saving file.");
			}
		}
	}
    */
//    @Override
    protected void procesarHuella() {
//        super.procesarHuella(sample);
  /*      
        if(caracteristicas != null){
            try {
                enrollador.addFeatures(caracteristicas);
            } catch (Exception e) {
            }finally{
                switch(enrollador.getFeaturesNeeded()){
                    case 4:
                        lblPasos.setIcon(new ImageIcon(getClass().getResource("/img/paso0.png")));
                        break;
                    case 3:
                        lblPasos.setIcon(new ImageIcon(getClass().getResource("/img/paso1.png")));                               
                        break;
                    case 2:
                        lblPasos.setIcon(new ImageIcon(getClass().getResource("/img/paso2.png")));
                        break;
                    case 1:
                        lblPasos.setIcon(new ImageIcon(getClass().getResource("/img/paso3.png")));
                        break;
                    case 0:
                        lblPasos.setIcon(new ImageIcon(getClass().getResource("/img/paso4.png")));
                        break;
                }
                
                switch(enrollador.getTemplateStatus()){
                    case TEMPLATE_STATUS_READY:
                        stop();
                        plantillaHuella = enrollador.getTemplate();
                        setVisible(false);
                        break;

                    case TEMPLATE_STATUS_FAILED:
                        enrollador.clear();
                        stop();
                        plantillaHuella = null;
                        lblPasos.setIcon(new ImageIcon(getClass().getResource("/img/paso0.png")));                        
                        start();
                        break;                                
                    default: break;
                }
            }
        }
*/
    }

    public boolean isBUSCAR() {
        return BUSCAR;
    }

    public void setBUSCAR(boolean buscar) {
        this.BUSCAR = buscar;
    }
    
    private void doModal(JDialog dlgParent){
        //open reader
        try{
            m_reader.Open(Reader.Priority.COOPERATIVE);
        }
        catch(UareUException e){ 
            MessageBox.DpError("Reader.Open()", e); 
        }

        m_enrollment.start();

        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.dispose();

        m_enrollment.cancel();

        try{
            m_reader.Close();
        }
        catch(UareUException e){ 
            MessageBox.DpError("Reader.Close()", e); 
        }
    }
        
    public static void Run(Reader reader){
        JDialog dlg = new JDialog((JDialog)null, "Enrollment", true);
        LeerHuella leerHuella = new LeerHuella(null, true, reader);
        leerHuella.doModal(dlg);
    }
}
