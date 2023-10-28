package eBookPurchaseMicroService;

import org.springframework.data.repository.CrudRepository;
import eBookPurchaseMicroService.PurchaseInformation;

public interface PurchaseRepository extends CrudRepository<PurchaseInformation, Long> {

}
