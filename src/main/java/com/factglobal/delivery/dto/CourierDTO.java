package com.factglobal.delivery.dto;

import com.factglobal.delivery.models.Courier;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CourierDTO {
    @NotBlank(message = "Name should not be empty")
    @Size(min = 2, max = 50, message = "Name should be between 2 and 50 characters")
    private String name;

    @NotBlank(message = "INN should not be empty")
    @Length(min = 12, max = 12, message = "INN should have 12 characters.")
    private String inn;

    @NotBlank(message = "Phone number should not be empty")
    @Pattern(regexp = "^\\+7\\d{3}\\d{7}$"
            , message = "Phone number should consist of 14 digits and match the format +7XXXХХХХХХХ")
    private String phoneNumber;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email number should not be empty")
    private String email;

    private Courier.Status courierStatus;
}
