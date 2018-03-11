package com.example.dez.devlpart_socket_simplechatroom;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TCPClientActivity extends Activity implements View.OnClickListener {

    private static final int MESSAGE_RECEIVE_NEW_MSG = 1;
    private static final int MESSAGE_SOCKET_CONNECTED = 2;

    private Button mSendButton;
    private TextView mMessageTextView;
    private EditText mMessageEditText;

    private PrintWriter mPrintWriter;
    private Socket mClientSocket;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what)
            {
                case MESSAGE_RECEIVE_NEW_MSG:
                    mMessageTextView.setText(mMessageTextView.getText() + (String)msg.obj);
                    break;
                case MESSAGE_SOCKET_CONNECTED:
                    mSendButton.setEnabled(true);
                    break;
                default:
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcpclient);

        mMessageTextView = (TextView)findViewById(R.id.msg_container);
        mMessageEditText = (EditText)findViewById(R.id.msg);
        mSendButton = (Button)findViewById(R.id.send);
        mSendButton.setOnClickListener(this);

        Intent service = new Intent(this,TCPServerService.class);
        startService(service);

        new Thread(){
            @Override
            public void run() {
                connectTCPServer();
            }
        }.start();

    }

    @Override
    protected void onDestroy() {
        if(mClientSocket != null)
        {
            try{
                mClientSocket.shutdownInput();
                mClientSocket.close();
            }catch(IOException e)
            {
                e.printStackTrace();
            }
        }

        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

        if(v == mSendButton)
        {
            final String msg = mMessageEditText.getText().toString();
            if(!TextUtils.isEmpty(msg) && mPrintWriter != null)
            {
                mPrintWriter.println(msg);
                mPrintWriter.flush();
                mMessageEditText.setText("");
                String time = formatDateTime(System.currentTimeMillis());
                final String showedMsg = "self " + time + ":" + msg + "\n";
                mMessageTextView.setText(mMessageTextView.getText() + showedMsg);
            }


        }

    }

    @SuppressLint("SimpleDateFormat")
    private String formatDateTime(Long time)
    {
        return new SimpleDateFormat("(hh:mm:ss)").format(new Date(time));
    }

    private void connectTCPServer(){

        Socket socket = null;
        while(socket == null){
            try {
                socket = new Socket("localhost", 8688);
                mClientSocket = socket;
                mPrintWriter = new PrintWriter( new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
                mHandler.sendEmptyMessage(MESSAGE_SOCKET_CONNECTED);
                System.out.println("connect server success");
            }catch(IOException e)
            {
                SystemClock.sleep(1000);
                System.out.println("connect tcp server failed, retry...");
            }
        }

        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while(!TCPClientActivity.this.isFinishing() &&  !socket.isClosed() && socket.isConnected())
            {
                String msg = br.readLine();
                System.out.println("receive :" + msg);
                if(msg != null) {
                    String time = formatDateTime(SystemClock.currentThreadTimeMillis());
                    final String showedMsg = "server " + time + ":" + msg + "\n";
                    mHandler.obtainMessage(MESSAGE_RECEIVE_NEW_MSG,showedMsg).sendToTarget();

                }
            }

            System.out.println("quit...");
            MyUtils.close(mPrintWriter);
            MyUtils.close(br);
            socket.close();
        }catch(IOException e)
        {
            e.printStackTrace();
        }

    }

}






