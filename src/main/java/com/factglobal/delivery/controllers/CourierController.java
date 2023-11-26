package com.factglobal.delivery.controllers;

import com.factglobal.delivery.dto.CourierDTO;
import com.factglobal.delivery.models.Courier;
import com.factglobal.delivery.services.CourierService;
import com.factglobal.delivery.services.UserService;
import com.factglobal.delivery.util.common.Mapper;
import com.factglobal.delivery.util.exception_handling.ErrorValidation;
import com.factglobal.delivery.util.validation.CourierValidator;
import com.factglobal.delivery.util.validation.PasswordsMatching;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/couriers")
public class CourierController {
    private final CourierService courierService;
    private final UserService userService;
    private final CourierValidator courierValidator;
    private final Mapper mapper;

    @GetMapping
    public CourierDTO getCourier(Principal principal) {
        Courier courier = courierService.findCourierByPhoneNumber(principal.getName());

        return mapper.convertToCourierDTO(courier);
    }

    @PutMapping
    public ResponseEntity<?> editCourier(@RequestBody @Valid CourierDTO courierDTO,
                                         BindingResult bindingResult,
                                         Principal principal) {
        var courier = mapper.convertToCourier(courierDTO);
        int userId = userService.findByPhoneNumber(principal.getName()).orElse(null).getId();
        courier.setId(courierService.findCourierIdByUserId(userId));
        courierValidator.validate(courier, bindingResult);
        ErrorValidation.message(bindingResult);

        ResponseEntity<?> response = userService.editCourier(courier, userId);
        SecurityContextHolder.clearContext();

        return response;
    }

    @DeleteMapping
    public ResponseEntity<?> deleteCourier(Principal principal) {
        ResponseEntity<?> response = userService.deleteUser(userService.findByPhoneNumber(principal.getName()).orElse(null).getId());
        SecurityContextHolder.clearContext();

        return response;
    }
}
