package net.runelite.client.plugins.grouptileman;

import com.google.common.primitives.Longs;
import lombok.Getter;
import lombok.Value;
import net.runelite.api.coords.WorldPoint;

import java.net.Socket;
import java.io.*;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class TilemanNetwork {
    public static final int PORT = 2532;
    public static final byte VERSION = 0;
    public static final byte RESPONSE_PACKET = 0;
    public static final byte HANDSHAKE_PACKET = 1;
    public static final byte PLACE_TILE_PACKET = 2;
    public static final byte DISCONNECT_PACKET = 3;
    private Socket sock;
    private DataOutputStream out;
    private DataInputStream in;
    private Thread streamConsumer;
    @Getter
    private boolean connected = false;
    @Getter
    private String address = "";

    private TilemanModePlugin plugin;

    public TilemanNetwork(TilemanModePlugin plugin)
    {
        this.plugin = plugin;
    }

    public boolean connect(String addr, long hash)
    {
        try {
            sock = new Socket(addr, PORT);
            sock.setSoTimeout(2000);
            out = new DataOutputStream(sock.getOutputStream());
            in = new DataInputStream(sock.getInputStream());
            byte[] hsPacket = (new HandshakePacket(hash)).payload();
            out.write(hsPacket);
            out.flush();

            byte[] hsResponse = new byte[2];
            in.readFully(hsResponse);
            if(hsResponse[0] != 0 || hsResponse[1] != 0)
                return false;

            address = addr;
            connected = true;

            //TODO Should probably make a proper function or even a class instead of using an anonymous one
            streamConsumer = new Thread(() -> {
                while(connected)
                {
                    try {
                        byte command = in.readByte();

                        switch (command)
                        {
                            case 0:
                                byte response = in.readByte();
                                plugin.handleNetworkResponse(response == 0);
                                break;
                            case 2:
                                int regionId = in.readInt();
                                int regionX = in.readInt();
                                int regionY = in.readInt();
                                int z = in.readInt();

                                WorldPoint point = WorldPoint.fromRegion(regionId, regionX, regionY, z);
                                plugin.updateTileMark(point, true, TilemanModeTile.TILE_REMOTE);

                                break;
                            case 3:
                                disconnect();
                                break;
                        }
                    }catch (SocketTimeoutException ignored) {}
                    catch (IOException e) {
                        System.err.println("Error reading command type from " + addr);
                        System.err.println(e.toString());
                    }
                }
                System.out.println("Shut down");
            });

            streamConsumer.start();

            return true;
        }
        catch(UnknownHostException e) {
            System.err.println("Unable to find host on " + addr);
            System.err.println(e.getMessage());
        }
        catch (IOException e) {
            System.err.println("IO Exception connecting to host on " + addr);
            System.err.println(e.getMessage());
        }

        return false;
    }

    public void disconnect()
    {
        try {
            out.write(new DisconnectPacket().payload());
            out.flush();
            sock.close();
            connected = false;
            address = "";
        } catch (IOException e) {
            System.err.println("IO Exception disconnecting from " + address);
        }
    }

    public void sendTileUnlock(TilemanModeTile tile)
    {
        if(connected)
        {
            try
            {
                PlaceTilePacket packet = new PlaceTilePacket(tile);
                out.write(packet.payload());
                out.flush();
            } catch (IOException e) {
                System.err.println("IO Exception sending tile update to " + address);
            }
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

        public byte[] payload()
        {
            return new byte[]{command, status};
        }
    }

    private static class HandshakePacket
    {
        public byte command;
        public byte version;
        public long hash;

        public HandshakePacket(long hash) {
            command = HANDSHAKE_PACKET;
            version = VERSION;
            this.hash = hash;
        }

        public byte[] payload()
        {
            return ByteBuffer.allocate(10).put(command).put(version).putLong(hash).array();
        }
    }

    private static class PlaceTilePacket
    {
        public byte command;
        public int regionId;
        public int regionX;
        public int regionY;
        public int z;

        public PlaceTilePacket(TilemanModeTile tile)
        {
            command = PLACE_TILE_PACKET;
            regionId = tile.getRegionId();
            regionX = tile.getRegionX();
            regionY = tile.getRegionY();
            z = tile.getZ();
        }

        public PlaceTilePacket(int regionId, int regionX, int regionY, int z)
        {
            command = PLACE_TILE_PACKET;
            this.regionId = regionId;
            this.regionX = regionX;
            this.regionY = regionY;
            this.z = z;
        }

        public byte[] payload()
        {
            ByteBuffer buf = ByteBuffer.allocate(17).put(command).putInt(regionId).putInt(regionX).putInt(regionY).putInt(z);
            return buf.array();
        }
    }

    private static class DisconnectPacket implements Serializable
    {
        public byte command;

        public DisconnectPacket()
        {
            command = DISCONNECT_PACKET;
        }

        public byte[] payload()
        {
            return new byte[]{command};
        }
    }
}
