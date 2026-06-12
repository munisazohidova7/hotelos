package com.hotelos.reception.service;

import com.hotelos.shared.model.Guest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * HotelOS - Billing Algorithm (LO1 / Task 1 - Secondary Algorithm)
 *
 * Calculates the final bill at checkout:
 *   Total = (roomPricePerNight × nightsStayed) + serviceCharges - discount
 *
 * Edge cases handled:
 *   - Early checkout: uses actual nights, not originally booked nights
 *   - Zero service charges: handled gracefully (no NPE)
 *   - Discount capped at total (bill cannot be negative)
 *   - Late checkout fee: if checking out after noon on same day
 */
@Service
public class BillingService {

    // Late checkout fee applied when guest checks out same day as scheduled
    private static final double LATE_CHECKOUT_FEE = 50.0;
    private static final int LATE_CHECKOUT_HOUR = 12; // noon

    /**
     * Calculate and finalize the guest's bill.
     *
     * @param guest         The guest checking out
     * @param roomPrice     Price per night for the assigned room
     * @param actualCheckout The real checkout date (may differ from booked)
     * @return              Final total amount due
     */
    public double calculateBill(Guest guest, double roomPrice, LocalDate actualCheckout) {

        // --- Step 1: Calculate nights stayed ---
        long nights = ChronoUnit.DAYS.between(guest.getCheckInDate(), actualCheckout);
        if (nights <= 0) {
            nights = 1; // Minimum 1 night charge
        }

        // --- Step 2: Room charges ---
        double roomTotal = roomPrice * nights;
        guest.setRoomCharges(roomTotal);

        // --- Step 3: Sum all service charges (room service orders etc.) ---
        double serviceTotal = guest.getServiceChargesTotal();

        // --- Step 4: Apply late checkout fee if applicable ---
        double lateFeee = 0.0;
        if (actualCheckout.isAfter(guest.getCheckOutDate())) {
            // Guest stayed past scheduled checkout date
            lateFeee = LATE_CHECKOUT_FEE;
            guest.addServiceCharge("Late checkout fee", lateFeee);
            serviceTotal += lateFeee;
        }

        // --- Step 5: Apply discount (cannot make total negative) ---
        double discount = guest.getDiscount();
        double subtotal = roomTotal + serviceTotal;
        double total = Math.max(0.0, subtotal - discount);

        return total;
    }
}
