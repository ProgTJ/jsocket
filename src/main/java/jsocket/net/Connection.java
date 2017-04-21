package jsocket.net;

import jsocket.exceptions.InstantiationException;
import jsocket.exceptions.SocketTimeoutException;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * @author Will Czifro
 */
public abstract class Connection implements Closeable {

    protected Closeable conn;

    protected Connection(Closeable conn) {
        if (!(conn instanceof java.net.Socket ||
                conn instanceof java.net.DatagramSocket)) {
            throw new InstantiationException("conn needs to be a Socket or DatagramSocket");
        }
        this.conn = conn;
        init();
    }

    public void setSoTimeout(int timeout) {
        try {
            if (conn instanceof java.net.Socket) {
                ((java.net.Socket)conn).setSoTimeout(timeout);
            }
            else {
                ((java.net.DatagramSocket)conn).setSoTimeout(timeout);
            }
        } catch (SocketException e) {
            throw new ConnectionException("Failed to set SocketOptions timeout", e);
        }
    }

    protected abstract void init();

    public abstract byte[] receive();

    public abstract void send(byte[] data);

    /**
     * Sets the buffer size for underlying implementation.
     * Note: {@link UDPConnection} forces buffer size of 512, which is size of packet
     * @param bufferSize number of bytes to allocate for buffer.
     */
    public abstract void setBufferSize(int bufferSize);

    protected void throwIfSocketTimedOut(Exception e) {
        Class<java.net.SocketTimeoutException> steClass = java.net.SocketTimeoutException.class;
        if (e.getClass() == steClass || e.getCause().getClass() == steClass) {
            throw new SocketTimeoutException(e);
        }
    }

    public void close() {
        try {
            conn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Connection getInstance(Closeable conn) {
        if (conn instanceof java.net.Socket)
          return new TCPConnection((java.net.Socket)conn);
        return new UDPConnection((DatagramSocket)conn);
    }
}
