package eBookPurchaseMicroService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Controller
@RequestMapping(path = "/purchase")
public class PurchaseController {

    @Autowired
    private PurchaseRepository purchaseRepository;

    // Secret key for encryption and decryption
    private SecretKey secretKey;

    // Constructor to initialize the secret key
    public PurchaseController() {
        try {
            // Read the secret key from a file
            byte[] keyBytes = Files.readAllBytes(Paths.get("src/main/java/eBookPurchaseMicroService/secret.key"));
            secretKey = new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize secret key");
        }
    }

    @PostMapping(path = "/details")
    public @ResponseBody String addNewPurchase (@RequestParam String cardName, @RequestParam String cardNumber,
                                                @RequestParam String cardExpiryDate, @RequestParam String cvv) {

        // Encrypt the cardNumber and cvv
        String encryptedCardNumber = encrypt(cardNumber);
        String encryptedCvv = encrypt(cvv);

        // Set the date & time for purchase
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm");
        String formattedDate = dateFormat.format(currentDate);

        PurchaseInformation details = new PurchaseInformation();
        details.setCardName(cardName);
        details.setCardNumber(encryptedCardNumber);
        details.setCardExpiryDate(cardExpiryDate);
        details.setCvv(encryptedCvv);
        details.setPurchaseDate(formattedDate);
        purchaseRepository.save(details);
        return "Saved";
    }

    @GetMapping(path = "/all")
    public @ResponseBody Iterable<PurchaseInformation> getAllPurchaseDetails() {
        Iterable<PurchaseInformation> purchaseDetails = purchaseRepository.findAll();

        for (PurchaseInformation purchase : purchaseDetails) {
            // Decrypt the cardNumber and cvv
            String decryptedCardNumber = decrypt(purchase.getCardNumber());
            String decryptedCvv = decrypt(purchase.getCvv());

            purchase.setCardNumber(decryptedCardNumber);
            purchase.setCvv(decryptedCvv);
        }

        return purchaseDetails;
    }

    // Testing
    @GetMapping(path = "/id")
    public @ResponseBody PurchaseInformation getPurchaseDetailsById(@PathVariable Long id) {
        Optional<PurchaseInformation> purchaseOptional = purchaseRepository.findById(id);
        if (purchaseOptional.isPresent()) {
            PurchaseInformation purchase = purchaseOptional.get();

            // Create a PurchaseDetails object with cardName and purchaseDate
            PurchaseInformation purchaseDetails = new PurchaseInformation();
            purchaseDetails.setCardName(purchase.getCardName());
            purchaseDetails.setPurchaseDate(purchase.getPurchaseDate());

            return purchaseDetails;
        } else {
            throw new RuntimeException("Purchase not found with ID: " + id);
        }
    }


    // Method to encrypt data
    private String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedData = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt data");
        }
    }

    // Method to decrypt data
    private String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt data");
        }
    }
}
