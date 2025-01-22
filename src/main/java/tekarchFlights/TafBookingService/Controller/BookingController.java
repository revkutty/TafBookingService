package tekarchFlights.TafBookingService.Controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tekarchFlights.TafBookingService.DTO.BookingRequest;
import tekarchFlights.TafBookingService.DTO.BookingResponse;
import tekarchFlights.TafBookingService.Service.BookingService;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Validated @RequestBody BookingRequest bookingRequest) {
        BookingResponse bookingResponse = bookingService.createBooking(bookingRequest);
        return ResponseEntity.ok(bookingResponse);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long bookingId) {
        BookingResponse bookingResponse = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(bookingResponse);
    }

 /*   @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByUserId(@PathVariable Long userId) {
        List<BookingResponse> bookings = bookingService.getBookingsByUserId(userId);
        return ResponseEntity.ok(bookings);
    }
      */

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getBookingsByUserId(@PathVariable Long userId) {
        List<Map<String, Object>> bookings = bookingService.getBookingsForUser(userId);
        if (bookings == null || bookings.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bookings);
    }


    @PutMapping("/{id}/cancel")
    public ResponseEntity<String> cancelBooking(@PathVariable Long id) {
        try {
            String response = bookingService.cancelBooking(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }
}

   /* @DeleteMapping("/{bookingId}")
    public ResponseEntity<String> cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok("Booking with ID " + bookingId + " successfully cancelled.");
    }   */

  /*  @DeleteMapping("/{bookingId}")
    public ResponseEntity<Map<String, Object>> cancelBooking(@PathVariable Long bookingId) {
        Map<String, Object> updatedBooking = bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok(updatedBooking);
    }   */

