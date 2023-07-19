import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingDate {
    private String checkin;
    private String checkout;
}
