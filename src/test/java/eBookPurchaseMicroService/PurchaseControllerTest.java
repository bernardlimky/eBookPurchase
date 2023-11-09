package eBookPurchaseMicroService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class PurchaseControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private PurchaseController purchaseController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(purchaseController).build();
    }

    @Test
    public void testAddNewPurchase() throws Exception {
        mockMvc.perform(post("/purchase/details")
                        .param("cardName", "John Doe")
                        .param("cardNumber", "1234567890123456")
                        .param("cardExpiryDate", "1224")
                        .param("cvv", "123"))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment added"));

        verify(purchaseRepository, times(1)).save(any(PurchaseInformation.class));
    }

    @Test
    public void testEncryptionAndDecryption() {
        // Test encryption and decryption
        String originalData = "1234567890123456";
        String encryptedData = purchaseController.encrypt(originalData);
        String decryptedData = purchaseController.decrypt(encryptedData);

        assertEquals(originalData, decryptedData);
    }
}
