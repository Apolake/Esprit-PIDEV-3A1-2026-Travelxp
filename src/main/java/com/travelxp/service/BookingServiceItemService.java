package com.travelxp.service;

import com.travelxp.model.Booking;
import com.travelxp.model.BookingServiceItem;
import com.travelxp.model.ServiceOffering;
import com.travelxp.repository.BookingServiceItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BookingServiceItemService {

    @Autowired
    private BookingServiceItemRepository bookingServiceItemRepository;

    public List<BookingServiceItem> getAll() {
        return bookingServiceItemRepository.findAll();
    }

    public List<BookingServiceItem> getByBooking(Booking booking) {
        return bookingServiceItemRepository.findByBooking(booking);
    }

    @Transactional
    public BookingServiceItem save(BookingServiceItem item) {
        validate(item);
        if (item.getPriceAtBooking() == null && item.getService() != null) {
            item.setPriceAtBooking(item.getService().getPrice());
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            item.setQuantity(1);
        }
        return bookingServiceItemRepository.save(item);
    }

    @Transactional
    public void delete(Long id) {
        bookingServiceItemRepository.deleteById(id);
    }

    private void validate(BookingServiceItem item) {
        if (item.getBooking() == null) {
            throw new IllegalArgumentException("Booking is required.");
        }
        if (item.getService() == null) {
            throw new IllegalArgumentException("Service is required.");
        }
        if (item.getQuantity() != null && item.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        BigDecimal price = item.getPriceAtBooking();
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
    }
}
