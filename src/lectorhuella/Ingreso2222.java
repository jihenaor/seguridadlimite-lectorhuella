package lectorhuella;

import DB.ConexionBD;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.digitalpersona.uareu.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import pojo.Huella;

public class Ingreso2222 
	extends JPanel
	implements ActionListener
{
	private static final long serialVersionUID = 6;
	private static final String ACT_BACK = "back";

	private CaptureThread m_capture;
	private Reader  m_reader;
	private Fmd[]   m_fmds;
	private JDialog m_dlgParent;
  	
  public List<Huella> m_listOfRecords = new ArrayList<>();
	public Fmd[] m_fmdArray = null; // Will hold final array of FMDs to identify

	private JTextArea m_text;
	
	private final String m_strPrompt1 = "Verificacion iniciada\n    Pon un dedo en el lector\n\n";
	private final String m_strPrompt2 = "    pon el mismo u otro dedo en el lector\n\n";
  private int cont = 0;
  
	private Ingreso2222(Reader reader){
		m_reader = reader;
		m_fmds = new Fmd[2]; //two FMDs to perform comparison

		final int vgap = 5;
		final int width = 380;
		
		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(layout);
		
		m_text = new JTextArea(22, 1);
		m_text.setEditable(false);
		JScrollPane paneReader = new JScrollPane(m_text);
		add(paneReader);
		Dimension dm = paneReader.getPreferredSize();
		dm.width = width;
		paneReader.setPreferredSize(dm);
		
		add(Box.createVerticalStrut(vgap));
		
		JButton btnBack = new JButton("Back");
		btnBack.setActionCommand(ACT_BACK);
		btnBack.addActionListener(this);
		add(btnBack);
		add(Box.createVerticalStrut(vgap));

		setOpaque(true);
    try {
      selectDB();
    } catch (UareUException ex) {
      Logger.getLogger(Ingreso2222.class.getName()).log(Level.SEVERE, null, ex);
    }
	}

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
				m_dlgParent.setVisible(false);
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
        m_listOfRecords.add(new Huella(rs.getLong("idusuario"), rs.getLong("idtrabajador"), ""));
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
                  } else {
                    System.out.println("No match: " + i + " Intentos: " + cont);
                    cont++;
                  }
                }
                m_fmds[0] = null;
/*
                Engine.Candidate[] matches = engine.Identify(m_fmds[0], 0,
                    m_fmdArray, target_falsematch_rate, 1);
                if (matches.length == 1){
                  m_text.append("Fingerprints matched.\n");
                  JOptionPane
                      .showMessageDialog(
                          null,
                          "Match found:"
                              + this.m_listOfRecords
                                  .get(matches[0].fmd_index).getIdtrabajador());
                } else {
                  JOptionPane.showMessageDialog(null,
                      "Not Identified!!!");
                }
                */
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
		
		//start capture thread
		StartCaptureThread();

		//put initial prompt on the screen
		m_text.append(m_strPrompt1);
		
		//bring up modal dialog
		m_dlgParent = dlgParent;
		m_dlgParent.setContentPane(this);
		m_dlgParent.pack();
		m_dlgParent.setLocationRelativeTo(null);
		m_dlgParent.toFront();
		m_dlgParent.setVisible(true);
		m_dlgParent.dispose();
		
		//cancel capture
		StopCaptureThread();
		
		//wait for capture thread to finish
		WaitForCaptureThread();
		
		//close reader
		try{
			m_reader.Close();
		}
		catch(UareUException e){ MessageBox.DpError("Reader.Close()", e); }
	}
	
	public static void Run(Reader reader){
    	JDialog dlg = new JDialog((JDialog)null, "Verification", true);
    	Ingreso2222 verification = new Ingreso2222(reader);
    	verification.doModal(dlg);
	}
}
