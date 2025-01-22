package tekarchFlights.TafBookingService.DTO;

import lombok.Data;

import java.time.LocalDateTime;

/*@Data
public class FlightResponse {
    private Long id;
    private String flightNumber;
    private String departure;
    private String arrival;
    private String departureTime;
    private String arrivalTime;
    private Double price;
    private Integer availableSeats;
}   */

@Data
public class FlightResponse {
    private Long id;
    private String flightNumber;
    private String departure;
    private String arrival;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Double price;
    private Integer availableSeats;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
