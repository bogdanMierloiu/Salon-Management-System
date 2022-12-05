package ro.musiclover.manicureappointments.model.appointment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AppointmentRequest {

    private Integer id;

    @NotNull
    @Future(message = "Please check the date")
    private Date appointmentDate;

    @NotNull
    private LocalTime appointmentTime;

    private Integer manicuristId;

    private Integer customerId;

    private int[] nailsServicesIds;

}