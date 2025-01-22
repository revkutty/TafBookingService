package tekarchFlights.TafBookingService.DTO;

import lombok.Data;

@Data
public class BookingUpdateRequest {
    private Long id;
    private String status;
}