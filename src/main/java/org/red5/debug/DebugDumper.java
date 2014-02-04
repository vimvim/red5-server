package org.red5.debug;

import org.apache.mina.core.buffer.IoBuffer;
import org.red5.server.net.rtmp.message.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class used for dump rtmp session data.
 * This data will be used in tests.
 *
 */
public class DebugDumper {

    private static Map<String,Integer> inSeqNum = new HashMap<String,Integer>();
    private static Map<String,Integer> outSeqNum = new HashMap<String,Integer>();

    public static synchronized void dumpIncoming(String sessionId, IoBuffer data) {
        dumpPacket(sessionId, inSeqNum, "in", data);
    }

    public static synchronized void dumpOutgoing(String sessionId, IoBuffer data) {
        dumpPacket(sessionId, outSeqNum, "out", data);
    }

    /**
     * Random bytes generated for fillup handshake response from 8 - Constants.HANDSHAKE_SIZE bytes
     *
     * @param randBytes
     */
    public static synchronized void dumpHandshakeRand1(byte[] randBytes) {
        dumpData("hrand1", randBytes, 0 , Constants.HANDSHAKE_SIZE-8);
    }

    /**
     * Random bytes used for fill up/generate digest in the handshake response
     * Size = Constants.HANDSHAKE_SIZE - DIGEST_LENGTH
     *
     * @param randBytes
     */
    public static synchronized void dumpHandshakeRand2(byte[] randBytes) {
        dumpData("hrand2", randBytes, 0 , Constants.HANDSHAKE_SIZE - 32);
    }

    private static void dumpPacket(String sessionId, Map<String,Integer> seqNums, String direction, IoBuffer data) {

        int seqNum = 0;

        if (seqNums.containsKey(sessionId)) {
            seqNum = seqNums.get(sessionId);
        }

        seqNum++;
        seqNums.put(sessionId, seqNum);

        int arrayOffset = data.arrayOffset();
        int limit = data.limit();

        dumpData(direction+"_"+seqNum, data.array(), arrayOffset, limit);
    }

    private static void dumpData(String filename, byte[] data, int offset, int limit) {

        FileOutputStream output = null;
        try {
            output = new FileOutputStream(new File("dump/"+filename+".rtmp"));
            output.write(data, offset, limit);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
