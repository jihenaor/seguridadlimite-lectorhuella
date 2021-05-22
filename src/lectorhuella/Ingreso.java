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
import java.awt.Dimension;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import pojo.Huella;
import pojo.Trabajador;

public class Ingreso extends IngresoView implements ActionListener{
  private static final long serialVersionUID = 6;
	private static final String ACT_BACK = "back";

	private CaptureThread m_capture;
	private Reader  m_reader;
	private Fmd[]   m_fmds;
  
  public List<Huella> m_listOfRecords = new ArrayList<>();
	public Fmd[] m_fmdArray = null; // Will hold final array of FMDs to identify

  
  private final String m_strPrompt1 = "Verificacion iniciada\n    Pon un dedo en el lector\n\n";
	private final String m_strPrompt2 = "    pon el mismo u otro dedo en el lector\n\n";
  private int cont = 0;

  public Ingreso(java.awt.Dialog parent, boolean modal, Reader reader) {
    super(parent, modal);
    
    m_reader = reader;
		m_fmds = new Fmd[2]; //two FMDs to perform comparison

		final int vgap = 5;
		final int width = 380;
				
		m_text.setEditable(false);

		btnSalir.setActionCommand(ACT_BACK);
		btnSalir.addActionListener(this);
    
//		setOpaque(true);
    try {
      selectDB();
    } catch (UareUException ex) {
      Logger.getLogger(Ingreso2222.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public void actionPerformed(ActionEvent e){
		if(e.getActionCommand().equals(ACT_BACK)){
			//cancel capture
			StopCaptureThread();
		}
		else if(e.getActionCommand().equals(CaptureThread.ACT_CAPTURE)){
			//process result
			CaptureThread.CaptureEvent evt = (CaptureThread.CaptureEvent)e;
			if(ProcessCaptureResult(evt)){
				//restart capture thread
				WaitForCaptureThread();
				StartCaptureThread();
			}
			else{
				//destroy dialog
				//m_dlgParent.setVisible(false);
			}
		}
  }

  public void selectDB() throws UareUException {
    String sql = "SELECT idusuario, idtrabajador, nombre, huella, documento FROM usuario WHERE huella is not null";
    ConexionBD con = new ConexionBD();
    con.conectar();
    ResultSet rs;
    List<Fmd> m_fmdList = new ArrayList<>();
      
    try {
      rs = con.CONSULTAR(sql);
      while(rs.next()){
        Fmd fmd = UareUGlobal
            .GetImporter()
            .ImportFmd(
                rs.getBytes("huella"),
                com.digitalpersona.uareu.Fmd.Format.DP_REG_FEATURES,
                com.digitalpersona.uareu.Fmd.Format.DP_REG_FEATURES);
        m_fmdList.add(fmd);
        m_listOfRecords.add(new Huella(rs.getLong("idusuario"), rs.getLong("idtrabajador"), rs.getString("documento")));
      }
      m_fmdArray = new Fmd[m_fmdList.size()];
      m_fmdList.toArray(m_fmdArray);
      System.out.println("Huellas en la base de datos " + m_fmdArray.length);
    } catch (SQLException ex) {
      Logger.getLogger(Ingreso2222.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
	  
  private void StartCaptureThread() {
		m_capture = new CaptureThread(m_reader, false,
        Fid.Format.ISO_19794_4_2005,
				Reader.ImageProcessing.IMG_PROC_DEFAULT);
		m_capture.start(this);
	}

	
	private void StopCaptureThread(){
		if(null != m_capture) m_capture.cancel();
	}
	
	private void WaitForCaptureThread(){
		if(null != m_capture) m_capture.join(1000);
	}
	
	private boolean ProcessCaptureResult(CaptureThread.CaptureEvent evt){
		boolean bCanceled = false;
    
		if(null != evt.capture_result){
			if(null != evt.capture_result.image && Reader.CaptureQuality.GOOD == evt.capture_result.quality){
				//extract features
				Engine engine = UareUGlobal.GetEngine();

				try{
					Fmd fmd = engine.CreateFmd(evt.capture_result.image, 
                  Fmd.Format.ANSI_378_2004);
					if(null == m_fmds[0]) {
              m_fmds[0] = fmd;
            
              try{
                int target_falsematch_rate = Engine.PROBABILITY_ONE / 100000; // target
																						// rate
																						// is
																						// 0.00001
                                            
                for(int i = 0; i < m_fmdArray.length; i++) {
            
                  int falsematch_rate = engine.Compare(m_fmds[0], 0, m_fmdArray[i], 0);
                    if(falsematch_rate < target_falsematch_rate){
                        System.out.println("match: " + i + " Intentos: " + cont);
                        cont = 0;
                        m_text.append("Fingerprints matched.\n");
                        String str = String.format("dissimilarity score: 0x%x.\n", falsematch_rate);
                        m_text.append(str);
                        str = String.format("false match rate: %e.\n\n\n", (double)(falsematch_rate / Engine.PROBABILITY_ONE));
                        m_text.append(str);
                        Trabajador trabajador;
                        
                        try {
                            Gson gson = new Gson();
                            String r = util.ClienteRest.getText("trabajadorIdentification/" + m_listOfRecords.get(i).getDocumento() + "/S");

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
                  } else {
                    lblFoto.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("/img/lector_desconectado.png")).getImage().getScaledInstance(lblFoto.getWidth(), lblFoto.getHeight(), Image.SCALE_DEFAULT)));
                    System.out.println("No match: " + i + " Intentos: " + cont);
                    cont++;
                  }
                }
                m_fmds[0] = null;

              } catch(UareUException e){ 
                 m_text.append("Fingerprints did not match. \n\n\n");
                e.printStackTrace();
//                        MessageBox.DpError("Engine.CreateFmd()", e); 
              }

          }
				}
				catch(UareUException e){
          MessageBox.DpError("Engine.CreateFmd()", e);
          e.printStackTrace();
        }
					
				if(null != m_fmds[0] &&  null != m_fmds[1]){
				}
				else{
					//the loop continues
					m_text.append(m_strPrompt2);
				}
			}
			else if(Reader.CaptureQuality.CANCELED == evt.capture_result.quality){
				//capture or streaming was canceled, just quit
				bCanceled = true;
			}
			else{
				//bad quality
				MessageBox.BadQuality(evt.capture_result.quality);
			}
		}
		else if(null != evt.exception){
			//exception during capture
			MessageBox.DpError("Capture", evt.exception);
			bCanceled = true;
		}
		else if(null != evt.reader_status){
			//reader failure
			MessageBox.BadStatus(evt.reader_status);
			bCanceled = true;
		}

		return !bCanceled;
	}

	private void doModal(JDialog dlgParent){
		//open reader
		try{
			m_reader.Open(Reader.Priority.COOPERATIVE);
		}
		catch(UareUException e){ MessageBox.DpError("Reader.Open()", e); }
		
		StartCaptureThread();

		m_text.append(m_strPrompt1);
		
    this.pack();
    this.setLocationRelativeTo(null);
    this.setVisible(true);
    this.dispose();

		StopCaptureThread();
		
		WaitForCaptureThread();
		
		try{
			m_reader.Close();
		}
		catch(UareUException e){ MessageBox.DpError("Reader.Close()", e); }
	}
	
 	public static void Run(Reader reader){
    	JDialog dlg = new JDialog((JDialog)null, "Verification", true);
    	Ingreso verification = new Ingreso(null, true, reader);
    	verification.doModal(dlg);
	}
}
