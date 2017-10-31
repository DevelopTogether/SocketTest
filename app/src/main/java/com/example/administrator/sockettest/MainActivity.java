package com.example.administrator.sockettest;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Socket socket;
    private BufferedReader br = null;
    OutputStream ops = null;//socket对应的输入流


    private EditText mInputContent;
    /**
     * 发送
     */
    private Button mSendMsg;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0://展示内容
                    String obj0 = (String) msg.obj;
                    mDisplayContent.setText(obj0);
                    break;
                case 8://按钮被点击了
                    mDisplayContent.setText("");
                    String obj = (String) msg.obj;
                    if (ops != null) {
                        try {
                            ops.write((obj + "\r\n").getBytes("utf-8"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private TextView mDisplayContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_test);
        initView();
        startThread();
    }

    private void startThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //建议连接到远程服务器的socket
                    socket = new Socket("192.168.1.102", 30000);
                    //将socket对应的输入流包装成BufferReader
                    br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    ops = socket.getOutputStream();
                    //启动一个线程处理服务器响应的数据
                    new Thread() {
                        @Override
                        public void run() {
                            String content = "";
                            try {
                                //采用循环，不断从socket中读取服务端发送过来的数据
                                while ((content = br.readLine()) != null) {
                                    sendMsg(content, 0);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void initView() {
        mInputContent = (EditText) findViewById(R.id.input_content);
        mSendMsg = (Button) findViewById(R.id.send_msg);
        mSendMsg.setOnClickListener(this);
        mDisplayContent = (TextView) findViewById(R.id.display_content);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_msg:

                String obj = mInputContent.getText().toString().trim();
                sendMsg(obj, 8);
                break;
        }
    }

    private void sendMsg(String obj, int what) {
        Message msg = new Message();
        msg.obj = obj;
        msg.what = what;
        mHandler.sendMessage(msg);
    }
}
