package com.shop.onlineshopping.dto;

import com.shop.onlineshopping.entity.Address;

/** A delivery address as sent to the client. Flattened, no entity leaks. */
public record AddressResponse(
        Long id,
        String label,
        String recipientName,
        String phone,
        String line1,
        String line2,
        String city,
        String state,
        String pincode,
        boolean isDefault
) {
    public static AddressResponse from(Address a) {
        return new AddressResponse(
                a.getId(),
                a.getLabel(),
                a.getRecipientName(),
                a.getPhone(),
                a.getLine1(),
                a.getLine2(),
                a.getCity(),
                a.getState(),
                a.getPincode(),
                a.isDefault());
    }
}
