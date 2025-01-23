package tekarchFlights.TafBookingService.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingResponse {
    private Long id;
  //  @JsonProperty("user")
    private UserResponse user;
 //   @JsonProperty("flight")
    private FlightResponse flight;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}

