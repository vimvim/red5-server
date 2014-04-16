package org.red5.debug;

import org.apache.mina.core.buffer.IoBuffer;
import org.red5.server.net.rtmp.message.Constants;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
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


    private static int videoStreamSqNum = 0;

    public static synchronized int dumpIncoming(String sessionId, IoBuffer data) {
        return dumpPacket(sessionId, inSeqNum, "in", data);
    }

    public static synchronized int dumpOutgoing(String sessionId, IoBuffer data) {
        return dumpPacket(sessionId, outSeqNum, "out", data);
    }

    /**
     * Random bytes generated for fill up handshake response from 8 - Constants.HANDSHAKE_SIZE bytes
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

    /**
     * Dump keys pair created by te server during handshake
     *
     * @param keys
     */
    public static synchronized void dumpKeyPair(KeyPair keys) {
        try {
            saveKeyPair("dump/", keys);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save generated key pair to the file.
     *
     * @param path
     * @param keyPair
     * @throws IOException
     */
    public static synchronized void saveKeyPair(String path, KeyPair keyPair) throws IOException {
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        // Store Public Key.
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
                publicKey.getEncoded());
        FileOutputStream fos = new FileOutputStream(path + "/public.key");
        fos.write(x509EncodedKeySpec.getEncoded());
        fos.close();

        // Store Private Key.
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());

        fos = new FileOutputStream(path + "/private.key");
        fos.write(pkcs8EncodedKeySpec.getEncoded());
        fos.close();
    }

    /**
     * Load key pair from file.
     *
     * @param path
     * @param algorithm             Needs to be "DH" for handshake keys.
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static synchronized KeyPair loadKeyPair(String path, String algorithm) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

        // Read Public Key.
        File filePublicKey = new File(path + "/public.key");
        FileInputStream fis = new FileInputStream(path + "/public.key");
        byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
        fis.read(encodedPublicKey);
        fis.close();

        // Read Private Key.
        File filePrivateKey = new File(path + "/private.key");
        fis = new FileInputStream(path + "/private.key");
        byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
        fis.read(encodedPrivateKey);
        fis.close();

        // Generate KeyPair.
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

        return new KeyPair(publicKey, privateKey);
    }

    /**
     * Dump binary invoke packet
     *
     * @param action            Invoke action name
     * @param transactId        Transaction id
     * @param in                Binary data
     */
    public static void dumpInvokePacket(String action, int transactId, IoBuffer in) {
        dumpPacket("packet_invoke_"+action+"_"+transactId, in);
    }

    /**
     * Dump video stream packet
     *
     * @param in
     */
    public static synchronized void dumpVideoStream(IoBuffer in) {
        dumpPacket("video_"+videoStreamSqNum+".bin", in);
        videoStreamSqNum = videoStreamSqNum + 1;
    }    
    
    public static void dumpPacket(String filename, IoBuffer data) {

        int arrayOffset = data.arrayOffset();
        int limit = data.limit();

        dumpData(filename, data.array(), arrayOffset, limit);
    }

    private static int dumpPacket(String sessionId, Map<String,Integer> seqNums, String direction, IoBuffer data) {

        int seqNum = 0;

        if (seqNums.containsKey(sessionId)) {
            seqNum = seqNums.get(sessionId);
        }

        seqNum++;
        seqNums.put(sessionId, seqNum);

        dumpPacket(direction+"_"+seqNum, data);

        return seqNum;
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
