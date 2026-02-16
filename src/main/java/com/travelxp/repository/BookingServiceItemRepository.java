package com.travelxp.repository;

import com.travelxp.model.Booking;
import com.travelxp.model.BookingServiceItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingServiceItemRepository extends JpaRepository<BookingServiceItem, Long> {
    @Override
    @EntityGraph(attributePaths = {"booking", "booking.property", "booking.guest", "booking.guest.level", "service"})
    List<BookingServiceItem> findAll();

    @EntityGraph(attributePaths = {"booking", "booking.property", "booking.guest", "booking.guest.level", "service"})
    List<BookingServiceItem> findByBooking(Booking booking);
}
