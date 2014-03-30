package net.mate.comm.applet;


import java.awt.BorderLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.comm.CommDriver;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * 
 * @author Nick
 *
 */
public class AppletWeightTextInput extends JApplet implements SerialPortEventListener {
	
	static {
	    System.setSecurityManager(null); //���ð�ȫ������(����д)
	}
	
	private static final String DLL_FILE = "win32com.dll";
	private String driverName = "com.sun.comm.Win32Driver";	
	private Boolean iserror=false;	
	private Communicator communicator = null;
	
	public AppletWeightTextInput() {
		
		txtWeight = new JTextField();
		getContentPane().add(txtWeight, BorderLayout.CENTER);
		txtWeight.setColumns(10);
		
		btnStatus = new JButton("AUTO");
		getContentPane().add(btnStatus, BorderLayout.EAST);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txtWeight;
	private JButton btnStatus;
	
	//�����û���HTML�������
	public String commPort;
	public String commAppname;
	public String commBaud;
	public String commDatabits;
	public String commStopbits;
	public String commParity;
	
	
	/**
	 * 1.Applet��ʼ��
	 */
	public void init() {
		System.out.println("-------init-----");

		commPort=super.getParameter("commPort");
		commAppname=super.getParameter("commAppname");
		commBaud=super.getParameter("commBaud");
		commDatabits=super.getParameter("commDatabits");
		commStopbits=super.getParameter("commStopbits");
		commParity=super.getParameter("commParity");
		
		if(commPort==null || commPort.equals("") ||
				commAppname==null || commAppname.equals("") ||
						commBaud==null || commBaud.equals("") ||
								commDatabits==null || commDatabits.equals("") ||
										commStopbits==null || commStopbits.equals("") ||
												commParity==null || commParity.equals("")
				){
			JOptionPane.showMessageDialog(null,"Applet��ǩ��������������ѡ����!");
			iserror=true;
			return;
		}		
		
		initCommPlus();
		
		LoadCommDrive();	
		
	}
	
	/**
	 * 2.start������init����ִ�����֮��ִ�У���Applet���������������У�start�������Ա�ִ�ж�Ρ�
	 * ����start������������ʵ��Ѱ�ҿ��ö˿ڣ��򿪶˿ڣ����ö˿ڲ������ȴ����ݵ����Լ����ݴ���ȴ���
	 */
	public void start() {	
		
		if(!iserror)createObjects();
				
		if(!iserror)ConnectionAction();//���Ӵ���
	}
	
    private void createObjects()
    {
        communicator = new Communicator();
        communicator.searchForPorts();
    }	
    
	/**
	 * ��������
	 */
	private void LoadCommDrive(){
		if(!iserror){
			try {
				System.loadLibrary("win32com");
			    CommDriver driver = (CommDriver)Class.forName(driverName).newInstance();
			    driver.initialize();
			} catch (Exception e) {
				e.printStackTrace(System.out);
				JOptionPane.showMessageDialog(null,"���ش�������:"+e.getMessage());
			}
		}
	}
	
	/**
	 * ��ʼ�����ڳ���
	 */
	private void initCommPlus() {

		try {
			
			// ��ȡ���ؿ�ʱ������·���б�
			String dirs = System.getProperty("java.library.path");
			String[] libs = dirs.split(";");
			String libPath = "";
			for (String lib : libs) {
				//if (lib.toLowerCase().endsWith(LIB_PATH_SUFFIX)) {
				if ((lib.toLowerCase().indexOf("jre")>0 && lib.toLowerCase().indexOf("bin")>0) || (lib.toLowerCase().indexOf("jdk")>0 && lib.toLowerCase().indexOf("bin")>0)) {
					libPath = lib;
					break;
				}
			}
			
			System.out.println("libPath======="+libPath);
			
			System.out.println("----------1---------");
			try{
				getCodeBase();
			}catch(Exception e){
				JOptionPane.showMessageDialog(null,"��ҳApplet��ǩ��������CodeBase����!"+e.toString());	
				return;
			}

			System.out.println("----------2---------");
						
			File dll = new File(libPath, DLL_FILE);
			
			if (!dll.exists()) {
				URL url = new URL(getCodeBase() + DLL_FILE);
				InputStream is = url.openConnection().getInputStream();
				FileOutputStream fos = new FileOutputStream(dll);
				byte[] buf = new byte[256]; // ��ȡ����
				int len = 0;
				while ((len = is.read(buf)) != -1) {
					fos.write(buf, 0, len);
				}
				fos.flush();
				fos.close();
				is.close();
				System.out.println("�����ļ����[" + dll + "].");
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace(System.out);
			JOptionPane.showMessageDialog(this,e.toString());
			iserror=true;
		} catch (IOException e) {
			e.printStackTrace(System.out);
			JOptionPane.showMessageDialog(this,e.toString());
			iserror=true;
		}catch(Exception e){
			e.printStackTrace(System.out);
			JOptionPane.showMessageDialog(this,e.toString());
			iserror=true;
		}
	}

    //����COM��
    private void ConnectionAction() {
    	//String selectedPort,String appname,int baud,int databits,int stopbits,String parity
    	int baudrate=9600;
    	int databits=8;
    	int stopbits=2;
    	try{
    		baudrate=Integer.parseInt(commBaud);
    		databits=Integer.parseInt(commDatabits);
    		stopbits=Integer.parseInt(commStopbits);
    	}catch(Exception e){
    		System.out.println(e.toString());
    	}
    	
        try {
			communicator.connect(commPort,commAppname,baudrate,databits,stopbits,commParity);
	        if (communicator.getConnected() == true)
	        {
	            if (communicator.initIOStream() == true)
	            {
	                communicator.initListener(this);
	            }
	        }			
		} catch (CommunicatorException e) {
			JOptionPane.showMessageDialog(null,e.toString());
			return;
		}
        
    }
    
    public void destroy() {
    	try {
			communicator.disconnect();
		} catch (CommunicatorException e) {
			JOptionPane.showMessageDialog(null,e.toString());
			return;
		}
    }
    
    
	
    final static int NEW_LINE_ASCII_ENTER = 13;//�س�
    final static int NEW_LINE_ASCII_NL = 0;//����
    final static int NEW_LINE_ASCII_EQ = (int)"=".charAt(0);//�Ⱥ�
    
    private String contentBuff="";
	@Override
    //�����¼�
    //what happens when data is received
    //pre: serial event is triggered
    //post: processing on the data it reads
    public void serialEvent(SerialPortEvent evt) {
    	System.out.println("=====evt.getEventType()======="+evt.getEventType());
        if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE)
        {
            try {
                while (true) {
                    int b = communicator.getInput().read(); // �����ȡ���������������
                   if (b == NEW_LINE_ASCII_ENTER || b == NEW_LINE_ASCII_NL || b == NEW_LINE_ASCII_EQ) { // ��������س��������ʾ��ȡ���
                	   txtWeight.setText(contentBuff);
                	   contentBuff="";
                       break;
                   } else {
                       //txtWeight.setText(txtWeight.getText()+new String(new byte[] { (byte) b }));
                	   contentBuff+=new String(new byte[] { (byte) b });
                   }
                }
            } catch (IOException e) {
            	System.out.println("Failed to read data. (" + e.toString() + ")");
            	JOptionPane.showMessageDialog(null,"Failed to read data. (" + e.toString() + ")");
            }
        	
//        	
//            try
//            {
//            	System.out.println("=====communicator.getInput().read()=======");
//            	
//            	byte singleData = (byte)communicator.getInput().read();
//            	
//                if (singleData != NEW_LINE_ASCII)
//                {
//                	contentBuff+=new String(new byte[] {singleData});
//                }
//                else
//                {
//                	this.txtWeight.setText(contentBuff);
//                	contentBuff="";
//                }
//            
//            }catch (Exception e){
//                System.out.println("Failed to read data. (" + e.toString() + ")");
//            }
        }
    }   

}
