package net.mate.comm.applet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TooManyListenersException;

import javax.comm.CommPort;
import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEventListener;


/**
 * 串口通讯类
 * 方法初始化顺序
 * 1.connect
 * 2.initIOStream
 * 3.initListener
 * @author Nick
 *
 */
public class Communicator 
{

	//扫描端口列表
    //for containing the ports that will be found
    private Enumeration ports = null;
    
    //保存端口
    //map the port names to CommPortIdentifiers
    private HashMap<String,CommPortIdentifier> portMap = new HashMap<String,CommPortIdentifier>();

	public HashMap<String, CommPortIdentifier> getPortMap() {
		return portMap;
	}

	public void setPortMap(HashMap<String, CommPortIdentifier> portMap) {
		this.portMap = portMap;
	}

	//保存当前打开
    //this is the object that contains the opened port
    private CommPortIdentifier selectedPortIdentifier = null;
    private SerialPort serialPort = null;

    public SerialPort getSerialPort() {
		return serialPort;
	}

	public void setSerialPort(SerialPort serialPort) {
		this.serialPort = serialPort;
	}

	//输入输出
    //input and output streams for sending and receiving data
    private InputStream input = null;
	private OutputStream output = null;
    
    public InputStream getInput() {
		return input;
	}

	public void setInput(InputStream input) {
		this.input = input;
	}

	public OutputStream getOutput() {
		return output;
	}

	public void setOutput(OutputStream output) {
		this.output = output;
	}


    //当前是否在连接状态
    //just a boolean flag that i use for enabling
    //and disabling buttons depending on whether the program
    //is connected to a serial port or not
    private boolean bConnected = false;

    //连接超时时间
    //the timeout value for connecting with the port
    final static int TIMEOUT = 2000;

    //some ascii values for for certain things
    final static int SPACE_ASCII = 32;
    final static int DASH_ASCII = 45;
    final static int NEW_LINE_ASCII = 10;

    //a string for recording what goes on in the program
    //this string is written to the GUI
    String logText = "";

    public Communicator()
    {

    }

    //获取端口
    //search for all the serial ports
    //pre: none
    //post: adds all the found ports to a combo box on the GUI
    public void searchForPorts()
    {
        ports = CommPortIdentifier.getPortIdentifiers();

        while (ports.hasMoreElements())
        {
            CommPortIdentifier curPort = (CommPortIdentifier)ports.nextElement();
            //get only serial ports
            if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL)
            {
                portMap.put(curPort.getName(), curPort);
            }
        }
    }
    
    //连接端口
    //connect to the selected port in the combo box
    //pre: ports are already found by using the searchForPorts method
    //post: the connected comm port is stored in commPort, otherwise,
    //an exception is generated
    //DEFAULT: 9600 baud, 8 data bits, 1 stop bit, no parity
    public void connect(String selectedPort,String appname,int baud,int databits,int stopbits,String parity) throws CommunicatorException
    {
        selectedPortIdentifier = (CommPortIdentifier)portMap.get(selectedPort);

        CommPort commPort = null;

        try
        {
            //the method below returns an object of type CommPort
            commPort = selectedPortIdentifier.open(appname, TIMEOUT);
            
            //the CommPort object can be casted to a SerialPort object
            serialPort = (SerialPort)commPort;
            
            //设置串行端口通讯参数。
            int curParity=SerialPort.PARITY_NONE;
            if(parity.equals("ODD")){
            	curParity=SerialPort.PARITY_ODD;
            }else if(parity.equals("EVEN")){
            	curParity=SerialPort.PARITY_EVEN;
            }else if(parity.equals("MARK")){
            	curParity=SerialPort.PARITY_MARK;
            }else if(parity.equals("SPACE")){
            	curParity=SerialPort.PARITY_SPACE;
            }         
            
            serialPort.setSerialPortParams(baud,databits,stopbits,curParity);
            //serialPort.setSerialPortParams(2400,SerialPort.DATABITS_8,SerialPort.STOPBITS_2,SerialPort.PARITY_NONE); 

            //for controlling GUI elements
            setConnected(true);

        }catch (PortInUseException e)
        {
            logText = selectedPort + " is in use. (" + e.toString() + ")";
            System.out.println(logText);
            throw new CommunicatorException(logText);
        }
        catch (Exception e)
        {
            logText = "Failed to open " + selectedPort + "(" + e.toString() + ")";
            System.out.println(logText);
            throw new CommunicatorException(logText);

        }
    }

    //初始货IO流(当连接成功后，初始货IO流）
    //open the input and output streams
    //pre: an open port
    //post: initialized intput and output streams for use to communicate data
    public boolean initIOStream()throws CommunicatorException
    {
        //return value for whather opening the streams is successful or not
        boolean successful = false;

        try {
            //
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();
            writeData(0);
            successful = true;
            return successful;
        }
        catch (IOException e) {
            logText = "I/O Streams failed to open. (" + e.toString() + ")";
            System.out.println(logText);
            //return successful;
            throw new CommunicatorException(logText);
        }
    }

    //初始化监听器
    //starts the event listener that knows whenever data is available to be read
    //pre: an open serial port
    //post: an event listener for the serial port that knows when data is recieved
    public void initListener(SerialPortEventListener event)throws CommunicatorException
    {
        try
        {
            serialPort.addEventListener(event);
            serialPort.notifyOnDataAvailable(true);
        }
        catch (TooManyListenersException e)
        {
            logText = "Too many listeners. (" + e.toString() + ")";
            System.out.println(logText);
            throw new CommunicatorException(logText);
        }
    }

    //断开连接
    //disconnect the serial port
    //pre: an open serial port
    //post: clsoed serial port
    public void disconnect()throws CommunicatorException
    {
        //close the serial port
        try
        {
            //writeData(0);

            serialPort.removeEventListener();
            serialPort.close();
            input.close();
            output.close();
            setConnected(false);

            logText = "Disconnected.";
            System.out.println(logText);
        }
        catch (Exception e)
        {
            logText = "Failed to close " + serialPort.getName() + "(" + e.toString() + ")";
            System.out.println(logText);
            throw new CommunicatorException(logText);
        }
    }

    final public boolean getConnected()
    {
        return bConnected;
    }

    public void setConnected(boolean bConnected)
    {
        this.bConnected = bConnected;
    }

   

    //向串口写入数据
    //method that can be called to send data
    //pre: open serial port
    //post: data sent to the other device
    public void writeData(int ascii)throws CommunicatorException
    {
        try
        {
        	//写入ASCII码
            output.write(ascii);
            output.flush();
            //this is a delimiter for the data
           // output.write(DASH_ASCII);//使用_为分隔结速
           // output.flush();
            /*
            output.write(ascii);
            output.flush();
            //will be read as a byte so it is a space key
            output.write(SPACE_ASCII);
            output.flush();
            */
        }
        catch (Exception e)
        {
            logText = "Failed to write data. (" + e.toString() + ")";
            throw new CommunicatorException(logText);
        }
    }
    
    public void writeData(String content)throws CommunicatorException{
    	if(content!=null && !content.equals(""))
    	writeData(content.getBytes());
    }
    
    public void writeData(byte[] content)throws CommunicatorException
    {
        try
        {
        	//写入ASCII码
            output.write(content);
            output.flush();
            //this is a delimiter for the data
            //output.write(DASH_ASCII);//使用_为分隔结速
           // output.flush();
            
            /*
            output.write(ascii);
            output.flush();
            //will be read as a byte so it is a space key
            output.write(SPACE_ASCII);
            output.flush();
            */
        }
        catch (Exception e)
        {
            logText = "Failed to write data. (" + e.toString() + ")";
            throw new CommunicatorException(logText);
        }
    }    
}
