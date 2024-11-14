package projectis3;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class ProjectIS3 {

    public static void main(String[] args) {
        Scanner inputReader = new Scanner(System.in); 
        String option;
        boolean running = true;

        while (running) {    
            showOptions();
            option = inputReader.nextLine();  

            switch (option) {      
                case "1":
                    executeEncryption(inputReader);
                    break;
                case "2":
                    executeDecryption(inputReader); 
                    break;
                case "3":
                    System.out.println("Closing application... Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid selection, please choose again.");
                    break;
            }
        }
        inputReader.close();
    }

     //Encrypt code
    public static String[] performEncryption(String text, int shiftFactor) {
        String binaryText = convertTextToBinary(text);                                // Convert text to binary.
        String shiftedBinary = applyShift(binaryText, shiftFactor);          // Apply shift to binary text based on key (Even)right,(odd)left.
        String binaryKey = convertTextToBinary(String.valueOf(shiftFactor));   // Convert shift factor to binary.
        String encryptedBinary = xorOperation(shiftedBinary, binaryKey);     // Apply XOR operation between shifted text and binary key.
        String hashValue = computeSHA256(encryptedBinary);                      // Calculate hash of encrypted text.
        return new String[]{encryptedBinary, hashValue};                             // Return encrypted text and hash.
    }

     //Decryption code 
    public static String performDecryption(String encryptedText, int shiftFactor, String hashCheck) {
        //Verification code
        String calculatedHash = computeSHA256(encryptedText); // Calculate hash of the encrypted text for verification.

        if (!calculatedHash.equals(hashCheck)) {   // Check if hash matches the provided hash.
            System.out.println("Hash verification failed. Incorrect cipher."); // If not matching, return null to indicate decryption failure.
            return null;
        }

        String binaryKey = convertTextToBinary(String.valueOf(shiftFactor));  // Convert key to binary.
        String xorDecrypted = xorOperation(encryptedText, binaryKey);       // Apply XOR operation between encryted text and binary key.
        String originalBinary = reverseShift(xorDecrypted, shiftFactor);   // Reverses the previous shift applied to xorDecrypted based on the key(Even)left,(Odd)right.
        return binaryToText(originalBinary);                               // return Convert originalBinary to text.
    }

    // Convert text to binary
    public static String convertTextToBinary(String text) {   
        StringBuilder binaryBuilder = new StringBuilder();
        for (char character : text.toCharArray()) {
            String binaryChar = Integer.toBinaryString(character);
            binaryBuilder.append("0".repeat(8 - binaryChar.length())).append(binaryChar);
        }
        return binaryBuilder.toString();
    }

     // Applies a shift based on key.
     // (Even keys) rotate right by 3; (odd keys) rotate left by 3.
    public static String applyShift(String binaryData, int shiftFactor) {
        return (shiftFactor % 2 == 0) ? rotateBinary(binaryData, 3,false) : rotateBinary(binaryData, 3,true);
    }

     // Reverses the shift for decryption.
    public static String reverseShift(String binaryData, int shiftFactor) {
        return (shiftFactor % 2 == 0) ? rotateBinary(binaryData, 3,true) : rotateBinary(binaryData, 3,false);
    }

     // Rotates the binary data (left) if true, (right) if false.
    public static String rotateBinary(String data, int positions, boolean left) {
    if (data.length() <= positions) return "00";   // If data length is too short for rotation, returns "00".


    if (left) {
        
        return data.substring(positions) + data.substring(0, positions);  // Left rotationز
    } else {
        
        return data.substring(data.length() - positions) + data.substring(0, data.length() - positions); // Right rotationز
    }
}

     // XOR operation
    public static String xorOperation(String data, String key) { 
        StringBuilder xorResult = new StringBuilder();
        StringBuilder expandedKey = new StringBuilder(key);

        // Repeat the key if it's shorter than the binary text to match their lengths.
        while (expandedKey.length() < data.length()) {
            expandedKey.append(key);
        }

        // Apply XOR between each bit in data and the corresponding bit in the key.
        for (int i = 0; i < data.length(); i++) {
            // If the bits(same)append '0' ,(Different)append '1'
            xorResult.append(data.charAt(i) == expandedKey.charAt(i) ? '0' : '1');
        }

        return xorResult.toString(); // Return the XOR result as binary text.
    }
    
     // Converts binary text back to regular text.
    public static String binaryToText(String binaryData) {  
        StringBuilder textResult = new StringBuilder();
        for (int i = 0; i < binaryData.length(); i += 8) {
            String byteString = binaryData.substring(i, Math.min(i + 8, binaryData.length()));
            textResult.append((char) Integer.parseInt(byteString, 2));
        }
        return textResult.toString();
    }

    // Hashing code using SHA-256
    public static String computeSHA256(String input) {  
        try {
            MessageDigest shaDigest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = shaDigest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexHash = new StringBuilder();

            for (byte byteVal : hashBytes) {
                String hex = Integer.toHexString(0xff & byteVal);
                hexHash.append((hex.length() == 1 ? "0" : "")).append(hex);
            }

            return hexHash.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm is not available.", e);
        }
    }

    
    private static void showOptions() {
        System.out.println("Please select an option:");
        System.out.println("[1] Encrypt a message");
        System.out.println("[2] Decrypt a message");
        System.out.println("[3] Exit application");
        System.out.print("Your choice: ");
    }

    
    private static void executeEncryption(Scanner scanner) {
        System.out.print("Enter message to encrypt: ");
        String message = scanner.nextLine();
       
        int key = getValidInteger(scanner,"Enter encryption key (integer):"); 
     

        String[] encryptionResult = performEncryption(message, key);
        System.out.println("Encrypted Text: " + encryptionResult[0]);
        System.out.println("Hash of Encrypted Text: " + encryptionResult[1]);
    }

    
    private static void executeDecryption(Scanner scanner) {
        System.out.print("Enter the encrypted text: ");
        String encryptedText = scanner.nextLine();
        
         int key = getValidInteger(scanner,"Enter decryption key (integer):"); 
        
       
        System.out.print("Enter hash for verification: ");
        String hash = scanner.nextLine();

        String decryptedMessage = performDecryption(encryptedText, key, hash);
        System.out.println("Original Message: " + (decryptedMessage != null ? decryptedMessage : "Decryption failed"));
    }
    
    
    private static int getValidInteger(Scanner scanner,String prompt){
        
        int key=0;
        boolean valid=false;
        
        // loop until valid integer value is provided 
        while(!valid){
            System.out.println(prompt);
            
            // check if the next input is integer 
            if (scanner.hasNextInt()) {
                key=scanner.nextInt();//read integer value
                valid=true;// to exit loop
            }else{
                System.out.println("Invalid input, please enter an integer key:");
                scanner.next();//clear the invalid input key
            }
            
        }
        scanner.nextLine();// consume the new line after the integer input
        return key;// return integer key
        
    }
}