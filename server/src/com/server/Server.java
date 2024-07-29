package com.tonybeltramelli.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Tony Beltramelli www.tonybeltramelli.com - created 13/08/15
 */
public class Server
{
    private ServerSocket _listener;

    public static int PORT = 25500;

    public Server(int port) throws IOException
    {
        _listener = new ServerSocket(port);
        _listener.setReuseAddress(true);
    }

    public void listen() throws IOException
    {
        Socket socket = _listener.accept();

        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String data = input.readLine();
            System.out.println(data);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("message received");
        } finally {
            socket.close();
        }
    }

    public void close() throws IOException
    {
        _listener.close();
    }

    public static void main(String[] args) throws IOException
    {
        Server server = new Server(PORT);

        System.out.println(InetAddress.getLocalHost().getHostAddress());

        try {
            while(true)
            {
                server.listen();
            }
        } finally {
            server.close();
        }
    }
}

