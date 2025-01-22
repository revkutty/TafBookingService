package tekarchFlights.TafBookingService.Service;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tekarchFlights.TafBookingService.DTO.*;

import java.util.List;
import java.util.Map;

@Service
public class BookingService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String DATASTORE_BOOKING_URL = "http://localhost:8081/api/bookings";
    private static final String DATASTORE_FLIGHT_URL = "http://localhost:8081/api/flights";
    private static final String DATASTORE_USER_URL = "http://localhost:8081/api/users";


    public BookingResponse createBooking(BookingRequest bookingRequest) {
        try {
            // Fetch flight details
            FlightResponse flight = restTemplate.getForObject(DATASTORE_FLIGHT_URL + "/" + bookingRequest.getFlight(), FlightResponse.class);
            UserResponse user = restTemplate.getForObject(DATASTORE_USER_URL + "/" + bookingRequest.getUser(), UserResponse.class);

            if (flight == null || flight.getAvailableSeats() <= 0) {
                throw new RuntimeException("Flight is fully booked or unavailable.");
            }

            // Reduce available seats
            flight.setAvailableSeats(flight.getAvailableSeats() - 1);
            restTemplate.put(DATASTORE_FLIGHT_URL + "/" + flight.getId(), flight);

            // Create booking in the database
            return restTemplate.postForObject(DATASTORE_BOOKING_URL, bookingRequest, BookingResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("Flight not found with ID: " + bookingRequest.getFlight());
        }
    }

    public BookingResponse getBookingById(Long bookingId) {
        return restTemplate.getForObject(DATASTORE_BOOKING_URL + "/" + bookingId, BookingResponse.class);
    }


    public List<Map<String, Object>> getBookingsForUser(Long userId) {
        String url = DATASTORE_BOOKING_URL + "/user/" + userId;
        ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
        return response.getBody();
    }

    public String cancelBooking(Long bookingId) {
        BookingUpdateRequest updateRequest = new BookingUpdateRequest();
        updateRequest.setStatus("Cancelled");

        String url = DATASTORE_BOOKING_URL + "/" + bookingId;
        try {
            System.out.println("Sending PUT request to URL: " + url + " with payload: " + updateRequest);
            restTemplate.put(url, updateRequest);
            System.out.println("Successfully cancelled booking with ID: " + bookingId);
        } catch (Exception e) {
            System.err.println("Error during PUT request to cancel booking: " + e.getMessage());
            throw new RuntimeException("Error during PUT request to cancel booking: " + e.getMessage(), e);
        }
        return "Booking with ID " + bookingId + " has been cancelled successfully.";
    }

}




