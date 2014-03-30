package net.mate.comm.applet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.comm.CommDriver;
import javax.comm.CommPortIdentifier;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 * COM口设置程序
 * @author Nick
 *
 */
@SuppressWarnings("serial")
public class AppletSerialPortSetting extends JApplet implements SerialPortEventListener {
	
	static {
	    System.setSecurityManager(null); //禁用安全管理器(必须写)
	}
	
	private static final String DLL_FILE = "win32com.dll";
	private String driverName = "com.sun.comm.Win32Driver";	
	private Boolean iserror=false;
	
	public JComboBox cbProlist;
	public JComboBox cbBaudrate;
	public JComboBox cbDatabits;
	public JComboBox cbStopbits;
	public JComboBox cbParity;
	public JButton btnConnection;
	public JButton btnDisConnection;
	public JButton btnSenddata;
	public JTextArea txtSendData;		
	public JTextArea txtRecData;
	Communicator communicator = null;
	
	
	/**
	 * 初始化组件
	 */
	private void initComponents(){
		getContentPane().setLayout(null);
		
		JLabel label = new JLabel("串口:");
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		label.setBounds(0, 6, 48, 16);
		getContentPane().add(label);
		
		cbProlist = new JComboBox();
		cbProlist.setBounds(47, 2, 72, 27);
		getContentPane().add(cbProlist);
		
		JLabel label_1 = new JLabel("波特率:");
		label_1.setHorizontalAlignment(SwingConstants.RIGHT);
		label_1.setBounds(118, 6, 48, 16);
		getContentPane().add(label_1);
		
		cbBaudrate = new JComboBox();
		cbBaudrate.setEditable(true);
		cbBaudrate.setBounds(167, 2, 85, 27);
		cbBaudrate.addItem("150");
		cbBaudrate.addItem("300");
		cbBaudrate.addItem("600");
		cbBaudrate.addItem("1200");
		cbBaudrate.addItem("2400");
		cbBaudrate.addItem("4800");
		cbBaudrate.addItem("9600");
		
		getContentPane().add(cbBaudrate);
		
		JLabel label_2 = new JLabel("数据位:");
		label_2.setHorizontalAlignment(SwingConstants.RIGHT);
		label_2.setBounds(250, 6, 48, 16);
		getContentPane().add(label_2);

		
		cbDatabits = new JComboBox();
		cbDatabits.setEditable(true);
		cbDatabits.setBounds(295, 2, 85, 27);
		cbDatabits.addItem("5");
		cbDatabits.addItem("6");
		cbDatabits.addItem("7");
		cbDatabits.addItem("8");		
		
		cbDatabits.setSelectedIndex(3);
		getContentPane().add(cbDatabits);
		
		JLabel label_3 = new JLabel("停止位:");
		label_3.setHorizontalAlignment(SwingConstants.RIGHT);
		label_3.setBounds(378, 6, 48, 16);
		getContentPane().add(label_3);
		
		cbStopbits = new JComboBox();
		cbStopbits.addItem("1");
		cbStopbits.addItem("2");		
		cbStopbits.setEditable(true);
		cbStopbits.setBounds(424, 2, 85, 27);
		getContentPane().add(cbStopbits);
		
		JLabel label_4 = new JLabel("效检位:");
		label_4.setHorizontalAlignment(SwingConstants.RIGHT);
		label_4.setBounds(513, 6, 48, 16);
		getContentPane().add(label_4);
		
		cbParity = new JComboBox();
		cbParity.setBounds(559, 2, 85, 27);
	
		cbParity.addItem("NONE");
		cbParity.addItem("ODD");
		cbParity.addItem("EVEN");
		cbParity.addItem("MARK");
		cbParity.addItem("SPACE");
		
		getContentPane().add(cbParity);
				
		btnConnection = new JButton("连接");
		btnConnection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnConnectionActionPerformed(e);
			}
		});
		btnConnection.setBounds(10, 34, 117, 29);
		getContentPane().add(btnConnection);
		
		btnDisConnection = new JButton("断开连接");
		btnDisConnection.setEnabled(false);
		btnDisConnection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnDisConnectionActionPerformed(e);
			}
		});
		btnDisConnection.setBounds(128, 34, 117, 29);
		getContentPane().add(btnDisConnection);
		
		txtRecData = new JTextArea();
		txtRecData.setEditable(true);
		txtRecData.setBounds(20, 100, 624, 82);
		txtRecData.setLineWrap(true);
		getContentPane().add(txtRecData);
		
		JLabel label_5 = new JLabel("接收到数据:");
		label_5.setBounds(20, 72, 99, 16);
		getContentPane().add(label_5);
		
		JLabel label_6 = new JLabel("发送数据:");
		label_6.setBounds(20, 202, 99, 16);
		getContentPane().add(label_6);
		
		txtSendData = new JTextArea();
		txtSendData.setBounds(20, 230, 624, 82);
		txtSendData.setLineWrap(true);
		getContentPane().add(txtSendData);
		
		btnSenddata = new JButton("发送数据");
		btnSenddata.setEnabled(false);
		
		btnSenddata.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnSenddataActionPerformed(e);
			}
		});
		btnSenddata.setBounds(527, 324, 117, 29);
		getContentPane().add(btnSenddata);
	}
	
	/**
	 * 1.Applet初始化
	 */
	public void init() {
		System.out.println("-------init-----");

		System.out.println(super.getParameter("test"));//获取html参数
		
		initCommPlus();
		
		LoadCommDrive();		
		
	}
	
	/**
	 * start方法在init方法执行完毕之后执行，在Applet的整个生命周期中，start方法可以被执行多次。
	 * 所以start方法可以用来实现寻找可用端口，打开端口，设置端口参数，等待数据到达以及数据处理等代码
	 */
	public void start() {	
		createObjects();
		searchPorts();
	}
	
    private void createObjects()
    {
        communicator = new Communicator();
    }
    
    //构造函数	
	public AppletSerialPortSetting() {
		initComponents();
	}	
	
	/**
	 * 加载驱动
	 */
	private void LoadCommDrive(){
		if(!iserror){
			try {
				System.loadLibrary("win32com");
			    CommDriver driver = (CommDriver)Class.forName(driverName).newInstance();
			    driver.initialize();
			} catch (Exception e) {
				e.printStackTrace(System.out);
				JOptionPane.showMessageDialog(null,"加载串口驱动:"+e.getMessage());
			}
		}
	}
	
	/**
	 * 初始化串口程序
	 */
	private void initCommPlus() {

		try {
			
			// 获取加载库时搜索的路径列表
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
				JOptionPane.showMessageDialog(null,"网页Applet标签中请设置CodeBase属性!"+e.toString());	
				return;
			}

			System.out.println("----------2---------");
						
			File dll = new File(libPath, DLL_FILE);
			
			if (!dll.exists()) {
				URL url = new URL(getCodeBase() + DLL_FILE);
				InputStream is = url.openConnection().getInputStream();
				FileOutputStream fos = new FileOutputStream(dll);
				byte[] buf = new byte[256]; // 读取缓存
				int len = 0;
				while ((len = is.read(buf)) != -1) {
					fos.write(buf, 0, len);
				}
				fos.flush();
				fos.close();
				is.close();
				System.out.println("创建文件完成[" + dll + "].");
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
	
    /**
     * 指定组件中加入所有端口列表
     * @param cbProlist
     */
    private void searchPorts(){
    	communicator.searchForPorts();
    	HashMap<String,CommPortIdentifier> portMap=communicator.getPortMap();
    	for(String proname:portMap.keySet()){
    		cbProlist.addItem(proname);
    	}
    }	
	
    //连接COM口
    private void btnConnectionActionPerformed(java.awt.event.ActionEvent evt) {
    	
    	//String selectedPort,String appname,int baud,int databits,int stopbits,String parity

    	String port=this.cbProlist.getSelectedItem().toString();
    	String baudrateStr=this.cbBaudrate.getSelectedItem().toString();
    	String databitsStr=this.cbDatabits.getSelectedItem().toString();
    	String stopbitsStr=this.cbStopbits.getSelectedItem().toString();
    	String parity=this.cbParity.getSelectedObjects().toString();
    	
    	int baudrate=9600;
    	int databits=8;
    	int stopbits=2;
    	
    	try{
    		System.out.println("baudrateStr="+baudrateStr);
    		System.out.println("databitsStr="+databitsStr);
    		System.out.println("stopbitsStr="+stopbitsStr);
    		baudrate=Integer.parseInt(baudrateStr);
    		databits=Integer.parseInt(databitsStr);
    		stopbits=Integer.parseInt(stopbitsStr);
    	}catch(Exception e){
    		System.out.println(e.toString());
    	}
    	
        try {
			communicator.connect(port,"SerialPortSetting",baudrate,databits,stopbits,parity);
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
        

        
        btnDisConnection.setEnabled(true);
        this.btnSenddata.setEnabled(true);
        this.btnConnection.setEnabled(false);
        this.btnConnection.setText("已连接"+this.cbProlist.getSelectedItem().toString());
    }
	
    private void btnDisConnectionActionPerformed(java.awt.event.ActionEvent evt) {
    	try {
			communicator.disconnect();
		} catch (CommunicatorException e) {
			JOptionPane.showMessageDialog(null,e.toString());
			return;
		}
    	this.btnDisConnection.setEnabled(false);
    	btnSenddata.setEnabled(false);
    	
        this.btnConnection.setEnabled(true);
        this.btnConnection.setText("连接");
    }
    
    private void btnSenddataActionPerformed(java.awt.event.ActionEvent evt) {
    	 if (communicator.getConnected() == true){
    		 try {
				communicator.writeData(this.txtSendData.getText());
			} catch (CommunicatorException e) {
				JOptionPane.showMessageDialog(null,e.toString());
				return;
			}
    	 }
    }

    final static int NEW_LINE_ASCII = 13;//回车
    
	@Override
    //监听事件
    //what happens when data is received
    //pre: serial event is triggered
    //post: processing on the data it reads
    public void serialEvent(SerialPortEvent evt) {
		String txt;
    	System.out.println("=====evt.getEventType()======="+evt.getEventType());
        if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE)
        {
            try
            {
            	System.out.println("=====communicator.getInput().read()=======");
            	
            	byte singleData = (byte)communicator.getInput().read();
            	
                if (singleData != NEW_LINE_ASCII)
                {
                	txt = new String(new byte[] {singleData});
                	txtRecData.append(txt);
                }
                else
                {
                	txtRecData.append("\n");
                }

            
            }catch (Exception e){
                System.out.println("Failed to read data. (" + e.toString() + ")");
            }
        }
    }

}

