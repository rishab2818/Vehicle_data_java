import org.json.JSONObject;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageHandler {
    private static final Logger LOGGER = Logger.getLogger(MessageHandler.class.getName());
    private static final String AES_ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int CHECKSUM_INDEX = 26;

    public static void handleMessage(byte[] message, KafkaManager kafkaManager) {
        try {
            LOGGER.info("Received message: " + message);
            String decryptedData = decryptData(message, "Sixteen byte key");
            String jsonObjectString = parseDecryptedData(decryptedData);
            if (jsonObjectString != null) {
                kafkaManager.sendMessage("abc", jsonObjectString);
            } else {
                LOGGER.warning("Failed to parse decrypted data: " + decryptedData);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to handle message", e);
        }
    }

    private static String decryptData(byte[] encryptedData, String key) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            System.out.println(encryptedData);
            byte[] iv = new byte[16];
            System.arraycopy(encryptedData, 0, iv, 0, iv.length);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] decrypted = cipher.doFinal(encryptedData, 16, encryptedData.length - 16);
            System.out.println(decrypted);
            return new String(decrypted, "UTF-8").trim();
        } catch (Exception e) {
            //logger.error("Error decrypting data: {}", e.getMessage());
            return null;
        }
    }
    private static String parseDecryptedData(String decryptedData) {
        try {
            String[] fields = decryptedData.split(",");
            String incomingChecksum = fields[CHECKSUM_INDEX].trim().substring(1);
            String checksum = calculateChecksum(decryptedData);

            if (!checksum.equals(incomingChecksum)) {
                //logger.error("Checksum does not match! Corrupted data: {}", decryptedData);
                return null;
            } else {
                JSONObject jsonObject = new JSONObject();
                for (int i = 0; i < fields.length; i++) {
                    jsonObject.put(getFieldName(i), fields[i].trim());
                }
                jsonObject.put("Checksum", checksum);
                return jsonObject.toString();
            }
        } catch (Exception e) {
            //logger.error("Failed to parse decrypted data: {}", decryptedData, e);
            return null;
        }
    }
    private static String getFieldName(int index) {
        switch (index) {
            case 0:
                return "StartCharacter";
            case 1:
                return "Header";
            case 2:
                return "FirmwareVersion";
            case 3:
                return "ConfigVersion";
            case 4:
                return "PacketType";
            case 5:
                return "PacketStatus";
            case 6:
                return "IMEI";
            case 7:
                return "GPSFix";
            case 8:
                return "Date";
            case 9:
                return "Time";
            case 10:
                return "Latitude";
            case 11:
                return "LatitudeDirection";
            case 12:
                return "Longitude";
            case 13:
                return "LongitudeDirection";
            case 14:
                return "Speed";
            case 15:
                return "Heading";
            case 16:
                return "NoofSatellites";
            case 17:
                return "Altitude";
            case 18:
                return "PDOP";
            case 19:
                return "HDOP";
            case 20:
                return "NetworkOperatorName";
            case 21:
                return "IgnitionStatus";
            case 22:
                return "MainInputVoltage";
            case 23:
                return "GSMSignalStrength";
            case 24:
                return "GPRSStatus";
            case 25:
                return "FrameNumber";
            case 26:
                return "EndCharacter";
            // Add more fields here if needed
            default:
                return "Field" + index;
        }
    }
    private static String calculateChecksum(String data) {
        String[] fields = data.split(",");
        int sum = 0;
        for (int i = 1; i < fields.length - 1; i++) {
            sum += fields[i].chars().sum();
        }
        return String.valueOf(sum);
    }
}
