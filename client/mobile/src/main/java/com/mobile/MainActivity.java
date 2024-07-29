package com.tonybeltramelli.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Tony Beltramelli www.tonybeltramelli.com on 13/08/15.
 */
public class MainActivity extends Activity implements View.OnClickListener
{
    public final String address = "192.168.0.20";
    public final int port = 25500;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Button for user triggered socket communication
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);

        // Thread triggered socket communication
        Runnable sendDataToServer = new Runnable() {
            public void run() {
                _send("Send from thread scheduler.");
            }
        };
        Executors.newSingleThreadScheduledExecutor().schedule(sendDataToServer, 1000, TimeUnit.MILLISECONDS);

        // Scan through all permissions and access which ones are granted
        PermissionScanner _scanner = new PermissionScanner(getApplicationContext());
        System.out.println(_scanner.scan());
    }

    @Override
    public void onClick(View v)
    {
        _send("Send after user interaction.");
    }

    private void _send(final String data)
    {
        Log.d(this.getClass().getName(), "Attempt to send data to remote server");

        Executors.newCachedThreadPool().submit(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Socket socket = new Socket(address, port);
                    try
                    {
                        // Outgoing data to server
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        out.println(data);
                        Log.d(this.getClass().getName(), "Send data to " + address + ":" + port);

                        // Incoming message from server
                        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String message = input.readLine();
                        Log.d(this.getClass().getName(), "Message from server: " + message);
                    } finally
                    {
                        socket.close();
                    }
                } catch (IOException e)
                {
                    Log.e(this.getClass().getName(), e.getMessage());
                }
            }
        });
    }
}
