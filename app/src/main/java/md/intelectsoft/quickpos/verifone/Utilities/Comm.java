package md.intelectsoft.quickpos.verifone.Utilities;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by Simon on 2018/8/23.
 */

public class Comm {

    private static final String TAG = "EMVDemo-Comm";

    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private int status;
    String ip;
    int port;

    public Comm(){
        status = 0;
        ip = "";
        port = 0;
        outputStream = null;
        inputStream = null;
    }

    public Comm(String ip, int port ){
        status = 0;
        this.ip = ip;
        this.port = port;
        outputStream = null;
        inputStream = null;
    }


    public boolean connect(String ip, int port) {
        this.ip = ip;
        this.port = port;

        return connect();

    }
    public boolean connect( ) {

        if( status > 0 ) {
            if( (this.ip == ip) && this.port==port  ) {
                return true;
            } else {
                disconnect();
            }
        }
        try {
            socket = new Socket(ip, port);
            if( null == socket ){
                return false;
            }
            this.ip = ip;
            this.port = port;
            status = 1;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public int send( byte[] data ) {
        if( status <= 0 ) {
            return 0;
        }

        Log.d(TAG, "SEND:");
        Log.d(TAG, Utility.byte2HexStr(data));

        try {
            outputStream = socket.getOutputStream();
            if( null == outputStream ){
                return 0;
            }

            outputStream.write( data );
            outputStream.flush();
            return data.length;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public byte[] receive( int wantLength, int timeoutSecond ){
        if( status <= 0 ){
            return null;
        }
        try {
            socket.setSoTimeout( timeoutSecond*1000 );
            inputStream = socket.getInputStream();
            if( null == inputStream ) {
                return null;
            }
            byte[] tmp = new byte[wantLength];
            int recvLen = inputStream.read(tmp);
            if( recvLen > 0  ) {
                byte[] ret = new byte[recvLen];
                System.arraycopy(tmp,0, ret, 0, recvLen);
                return ret;
            } else if( recvLen == 0 ){
                return null;
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void disconnect(){
        status = 0;
        try {
            if( null != inputStream ) {
                inputStream.close();
                inputStream = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if( null != outputStream ){
                outputStream.close();
                outputStream = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if( null != socket ){
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        status = 0;
    }
}
