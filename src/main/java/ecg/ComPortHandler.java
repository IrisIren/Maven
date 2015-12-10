package ecg;

import jssc.*;

import java.util.concurrent.BlockingQueue;


public class ComPortHandler {

    private volatile boolean running = true;
    public void terminate() {
        running = false;
    }

    public static void setSerialPort(String  comPortName) {
        ComPortHandler.serialPort = new SerialPort(comPortName);
    }

    private static SerialPort serialPort;
    BlockingQueue<Double> mQueue;

    private static final byte[] FRAME_END = {'\r', '\n'};
    private static final int SIZE = 32 / 8;

    public ComPortHandler(BlockingQueue<Double> queue) {
        mQueue = queue;
    }

    public static String[] getPortNames() {
        String[] portNames = SerialPortList.getPortNames();
//        String[] portNames = {"COM1", "COM2", "COM3", "COM4", "COM5"};
//        String[] portNames = {};
        for (String portName : portNames) {
            System.out.println(portName);
        }
        return portNames;
    }

    public void start() {
        try {
            if ((serialPort!=null && serialPort.isOpened ())) {
                serialPort.closePort();
            }
            serialPort.openPort();
            serialPort.setParams(
                    SerialPort.BAUDRATE_115200,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE
            );
            int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
            serialPort.setEventsMask(mask);//Set mask
            serialPort.addEventListener(new SerialPortReader());//Add SerialPortEventListener
        }
        catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public void finish() {
        if ((serialPort!=null && serialPort.isOpened ())) {
            try {
                serialPort.closePort();
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
        }
    }






    //    TODO: USE LATER
//    StringBuilder frameData = new StringBuilder(SIZE);
//    for (int i = 0; i < SIZE; i++) {
//        byte b = получить очередную цифру;
//        if (b == FRAME_END[0]) {
//            break;
//        }
//        frameData.append((char) b);
//    }
//    try {
//        list.add(Integer.parseInt(frameData.toString(), 16));
//    }
//    catch (NumberFormatException ex) {
//        incrementErrors();
//    }

    private class SerialPortReader implements SerialPortEventListener {

        private CircularBuffer buf = new CircularBuffer(2048);
        private int size;
        private StringBuilder frameData;

        @Override
        public void serialEvent(SerialPortEvent serialPortEvent) {
            byte[] receiveBuffer;
            int size = 0;
            switch (serialPortEvent.getEventType()) {
                case SerialPortEvent.RXCHAR:
                    try {
                        if (serialPortEvent.getEventValue() > 0) {
                            receiveBuffer = serialPort.readBytes();
                            size = receiveBuffer.length;

                            for (byte b : receiveBuffer) {
                                if ((b == FRAME_END[0]) || (b == FRAME_END[1])){
                                    frameData = new StringBuilder(size);
                                    for (int i = 0; i < size; i++) {
                                        frameData.append((char) (byte) buf.read());
                                    }
                                    Double d;
                                    try {
                                        d = Double.parseDouble(frameData.toString());
                                        System.out.println("value: " + String.valueOf(d));
                                        mQueue.put(d);
                                    }
                                    catch (NumberFormatException ex) {
                                        System.out.println("unknown string: " + frameData.toString());
                                    }

                                    size = 0;
                                } else {
                                    buf.store(b);
                                    size++;
                                }
                            }
                        }
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                    break;

                default:
                    break;
            }
        }
    }

}
