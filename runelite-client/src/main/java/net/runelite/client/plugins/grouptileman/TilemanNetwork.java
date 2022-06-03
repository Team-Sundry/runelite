package net.runelite.client.plugins.grouptileman;

import lombok.Getter;
import lombok.Value;

import java.net.Socket;
import java.io.*;
import java.net.UnknownHostException;

public class TilemanNetwork {
    public static final int PORT = 2532;
    public static final byte VERSION = 0;
    public static final byte RESPONSE_PACKET = 0;
    public static final byte HANDSHAKE_PACKET = 1;
    public static final byte PLACE_TILE_PACKET = 2;
    public static final byte DISCONNECT_PACKET = 3;
    private Socket sock;

    @Getter
    private boolean connected = false;

    public void connect(String addr, long hash)
    {
        try {
            sock = new Socket(addr, PORT);
            OutputStream out = sock.getOutputStream();
            byte[] hsPacket = objectToBytes(new HandshakePacket(hash));
            out.write(hsPacket);
            out.flush();
        }
        catch(UnknownHostException e) {
            System.err.println("Unable to find host on " + addr);
        }
        catch (IOException e) {
            System.err.println("IO Exception connecting to host on " + addr);
        }
    }

    private static byte[] objectToBytes(Object o) throws IOException {
        try(ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            ObjectOutputStream oOut = new ObjectOutputStream(bOut)) {
            oOut.writeObject(o);
            return bOut.toByteArray();
        }
    }

    private static class ResponsePacket
    {
        public byte command;
        public byte status;

        public ResponsePacket(byte status)
        {
            command = RESPONSE_PACKET;
            this.status = status;
        }
    }

    private static class HandshakePacket
    {
        public byte command;
        public byte version;
        public long hash;

        public HandshakePacket(long hash){
            command = HANDSHAKE_PACKET;
            version = VERSION;
            this.hash = hash;
        }
    }

    private static class PlaceTilePacket
    {
        public byte command;
        public int regionId;
        public int regionX;
        public int regionY;
        public int z;

        public PlaceTilePacket(int regionId, int regionX, int regionY, int z)
        {
            command = PLACE_TILE_PACKET;
            this.regionId = regionId;
            this.regionX = regionX;
            this.regionY = regionY;
            this.z = z;
        }
    }

    private static class DisconnectPacket
    {
        public byte command;

        public DisconnectPacket()
        {
            command = DISCONNECT_PACKET;
        }
    }
}
