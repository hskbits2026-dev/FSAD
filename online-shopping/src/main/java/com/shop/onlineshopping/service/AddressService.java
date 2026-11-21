package com.shop.onlineshopping.service;

import com.shop.onlineshopping.dto.AddressRequest;
import com.shop.onlineshopping.entity.Address;
import com.shop.onlineshopping.entity.User;
import com.shop.onlineshopping.exception.ResourceNotFoundException;
import com.shop.onlineshopping.repository.AddressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business rules for a customer's delivery addresses. A {@code @Service} is just a class that
 * holds logic (here: the "only one default" rule) so controllers stay thin.
 *
 * <p>Key rules enforced here, not in the entity:
 * <ul>
 *   <li>The first address a customer adds is automatically their default.</li>
 *   <li>Marking an address default clears the flag on all their other addresses.</li>
 *   <li>Deleting the default promotes the next remaining address to default.</li>
 *   <li>Every lookup is scoped to the owning user, so no customer can read/change another's address.</li>
 * </ul>
 */
@Service
public class AddressService {

    private static final Logger log = LoggerFactory.getLogger(AddressService.class);

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Transactional(readOnly = true)
    public List<Address> listForUser(User user) {
        return addressRepository.findByUserOrderByIsDefaultDescIdAsc(user);
    }

    @Transactional
    public Address create(User user, AddressRequest req) {
        Address a = new Address();
        a.setUser(user);
        apply(a, req);
        // First address for this customer becomes the default automatically.
        boolean isFirst = addressRepository.findByUserOrderByIsDefaultDescIdAsc(user).isEmpty();
        a.setDefault(isFirst);
        a = addressRepository.save(a);
        log.info("Customer {} added address {} (default={})", user.getId(), a.getId(), a.isDefault());
        return a;
    }

    @Transactional
    public Address update(User user, Long addressId, AddressRequest req) {
        Address a = requireOwned(user, addressId);
        apply(a, req);
        return addressRepository.save(a);
    }

    @Transactional
    public void delete(User user, Long addressId) {
        Address a = requireOwned(user, addressId);
        boolean wasDefault = a.isDefault();
        addressRepository.delete(a);
        if (wasDefault) {
            // Promote the next remaining address (if any) so the customer always has a default.
            List<Address> remaining = addressRepository.findByUserOrderByIsDefaultDescIdAsc(user);
            if (!remaining.isEmpty()) {
                Address promoted = remaining.get(0);
                promoted.setDefault(true);
                addressRepository.save(promoted);
                log.info("Promoted address {} to default for customer {}", promoted.getId(), user.getId());
            }
        }
    }

    @Transactional
    public Address setDefault(User user, Long addressId) {
        Address target = requireOwned(user, addressId);
        for (Address other : addressRepository.findByUserOrderByIsDefaultDescIdAsc(user)) {
            if (other.isDefault() && !other.getId().equals(target.getId())) {
                other.setDefault(false);
                addressRepository.save(other);
            }
        }
        target.setDefault(true);
        return addressRepository.save(target);
    }

    /** Load an address only if it belongs to this user; otherwise 404 (ownership guard). */
    private Address requireOwned(User user, Long addressId) {
        return addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + addressId));
    }

    private void apply(Address a, AddressRequest req) {
        a.setLabel(req.label());
        a.setRecipientName(req.recipientName());
        a.setPhone(req.phone());
        a.setLine1(req.line1());
        a.setLine2(req.line2());
        a.setCity(req.city());
        a.setState(req.state());
        a.setPincode(req.pincode());
    }
}
