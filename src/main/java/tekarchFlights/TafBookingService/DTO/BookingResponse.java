package tekarchFlights.TafBookingService.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingResponse {
    private Long id;
    private UserResponse user;
    private FlightResponse flight;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

