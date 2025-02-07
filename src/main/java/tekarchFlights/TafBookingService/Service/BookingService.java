package tekarchFlights.TafBookingService.Service;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tekarchFlights.TafBookingService.DTO.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class BookingService {

    @Autowired
    private RestTemplate restTemplate;

    private static final Logger logger = LogManager.getLogger(BookingService.class);

    @Value("${datasource.flight.service.url}")
    private String DATASTORE_FLIGHT_URL;

    @Value("${datasource.booking.service.url}")
    private String DATASTORE_BOOKING_URL;

    @Value("${datasource.user.service.url}")
    private String DATASTORE_USER_URL;


    //  private static final String DATASTORE_BOOKING_URL = "http://localhost:8081/api/bookings";
  //  private static final String DATASTORE_FLIGHT_URL = "http://localhost:8081/api/flights";
  //  private static final String DATASTORE_USER_URL = "http://localhost:8081/api/users";


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


    public List<BookingResponse> getAllBookings() {
        BookingResponse[] BookingArray = restTemplate.getForObject(DATASTORE_BOOKING_URL, BookingResponse[].class);
        assert BookingArray != null;
        return Arrays.asList(BookingArray);
    }

  /*  public List<Map<String, Object>> getBookingsForUser(Long userId) {
        String url = DATASTORE_BOOKING_URL + "/user/" + userId;
        ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
        return response.getBody();
    }   */

    public List<Map<String, Object>> getBookingsForUser(Long userId) {
        String url = DATASTORE_BOOKING_URL + "/user/" + userId;

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {
                }
        );

        return response.getBody();
    }


    public List<Map<String, Object>> getBookingsByFlightId(Long flightId) throws IOException {
        try {
            //    String url = DATASTORE_FLIGHT_URL + "/" + flightId;
            String url = DATASTORE_BOOKING_URL + "/flight/" + flightId;
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    }
            );
//            ResponseEntity<List<BookingResponse>> response = restTemplate.exchange(
//                    url, HttpMethod.GET,
//                    null,
//                    new ParameterizedTypeReference<List<BookingResponse>>() {
//                    }
//            );
            return response.getBody();
        } catch (Exception ex) {
            logger.error("Error fetching bookings for flightId {}: {}", flightId, ex.getMessage(), ex);
            throw new RuntimeException("Failed to fetch bookings", ex);
        }
    }


    public String cancelBooking(Long bookingId) {

        // Step 1: Check if the bookingId is valid
        if (bookingId == null) {
            throw new IllegalArgumentException("Booking ID must not be null");
        }

        // Step 2: Retrieve the booking based on bookingId
        BookingResponse booking = restTemplate.getForObject(DATASTORE_BOOKING_URL + "/" + bookingId, BookingResponse.class);

        if (booking == null) {
            throw new RuntimeException("Booking with ID " + bookingId + " not found.");
        }

        // Check if the booking is already canceled
        if ("Cancelled".equalsIgnoreCase(booking.getStatus())) {
            return "Booking is already cancelled for bookingId :"  + bookingId ;
        }

        // Step 3: Retrieve the associated flight based on the booking's flight ID
        FlightResponse flight = restTemplate.getForObject(DATASTORE_FLIGHT_URL + "/" + booking.getFlight().getId(), FlightResponse.class);

        if (flight == null) {
            throw new RuntimeException("Flight with ID " + booking.getFlight().getId() + " not found.");
        }

        // Step 4: Update the available seats in the flight (increase by 1)
        flight.setAvailableSeats(flight.getAvailableSeats() + 1);

        // Step 5: Update the flight in the data store
        String flightUrl = DATASTORE_FLIGHT_URL + "/" + flight.getId() + "/update";
        try {
            System.out.println("Sending PUT request to URL: " + flightUrl + " with payload: " + flight);
            restTemplate.put(flightUrl, flight);  // Update the flight with the new available seat count
            System.out.println("Successfully updated flight available seats after booking cancellation.");
        } catch (Exception e) {
            System.err.println("Error during PUT request to update flight: " + e.getMessage());
            throw new RuntimeException("Error during PUT request to update flight: " + e.getMessage(), e);
        }

        // Step 6: Cancel the booking itself by updating its status to "Cancelled"
        BookingUpdateRequest request = new BookingUpdateRequest();
        request.setId(bookingId);
        request.setStatus("Cancelled");

        String bookingUrl = DATASTORE_BOOKING_URL + "/" + bookingId + "/update";
        try {
            System.out.println("Sending PUT request to URL: " + bookingUrl + " with payload: " + request);
            restTemplate.put(bookingUrl, request); // Cancel the booking
            System.out.println("Successfully cancelled booking with ID: " + bookingId);
        } catch (Exception e) {
            System.err.println("Error during PUT request to cancel booking: " + e.getMessage());
            throw new RuntimeException("Error during PUT request to cancel booking: " + e.getMessage(), e);
        }

        // Return success message
        return "Booking with ID " + bookingId + " has been cancelled successfully and available seats updated.";
    }


  /*  public String cancelBooking(Long bookingId) {

        BookingUpdateRequest request = new BookingUpdateRequest();
        request.setId(bookingId); // Ensure this is populated
        request.setStatus("Cancelled");

        if (bookingId == null) {
            throw new IllegalArgumentException("Booking ID must not be null");
        }

        String url = DATASTORE_BOOKING_URL + "/" + bookingId + "/update";
        try {
            System.out.println("Sending PUT request to URL: " + url + " with payload: " + request);
            restTemplate.put(url, request);
            System.out.println("Successfully cancelled booking with ID: " + bookingId);
        } catch (Exception e) {
            System.err.println("Error during PUT request to cancel booking: " + e.getMessage());
            throw new RuntimeException("Error during PUT request to cancel booking: " + e.getMessage(), e);
        }
        return "Booking with ID " + bookingId + " has been cancelled successfully.";
    }

   */
}









