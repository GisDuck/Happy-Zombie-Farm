package game.Happy_Zombie_Farm.idempotency;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IdempotencyEntry {
    private IdempotencyStatus status;
    private String requestHash;
    private String responseBody;
    private Integer httpStatus;
    private String contentType;
}
