package com.factglobal.delivery.services;

import com.factglobal.delivery.dto.security.RegistrationAdminDTO;
import com.factglobal.delivery.dto.security.RegistrationCourierDto;
import com.factglobal.delivery.dto.security.RegistrationCustomerDto;
import com.factglobal.delivery.models.Courier;
import com.factglobal.delivery.models.Customer;
import com.factglobal.delivery.models.User;
import com.factglobal.delivery.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final CourierService courierService;
    private final CustomerService customerService;
    private final RoleService roleService;
    private final ModelMapper modelMapper;
    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    public User findById(int userId) {
        return userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(
                "User with id: " + userId + " was not found"
        ));
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
        User user = findByPhoneNumber(phoneNumber).orElseThrow(() -> new UsernameNotFoundException(
                String.format("User with phone number:'%s' not found", phoneNumber)
        ));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getRoles().stream().
                        map(role -> new SimpleGrantedAuthority(role.getName())).
                        collect(Collectors.toList())
        );
    }

    private User createAndSaveUser(String phoneNumber, String password, String role) {
        User user = new User();
        user.setPhoneNumber(phoneNumber);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Set.of(roleService.getUserRole(role)));
        user.setBlock(true);

        return userRepository.saveAndFlush(user);
    }

    public User createNewCourier(RegistrationCourierDto registrationCourierDto) {
        User savedUser = createAndSaveUser(registrationCourierDto.getPhoneNumber(), registrationCourierDto.getPassword(), "ROLE_COURIER");

        Courier courier = converterToCourier(registrationCourierDto);
        courier.setUser(savedUser);
        courierService.enrichCourier(courier);
        courierService.saveAndFlush(courier);

        savedUser.setCourier(courier);

        return userRepository.save(savedUser);
    }

    public User createNewAdmin(RegistrationAdminDTO registrationAdminDTO) {
        return createAndSaveUser(
                registrationAdminDTO.getPhoneNumber(),
                registrationAdminDTO.getPassword(),
                "ROLE_ADMIN"
        );
    }

    public User createNewCustomer(RegistrationCustomerDto registrationCustomerDto) {
        User savedUser = createAndSaveUser(registrationCustomerDto.getPhoneNumber(), registrationCustomerDto.getPassword(), "ROLE_CUSTOMER");

        Customer customer = converterToCustomer(registrationCustomerDto);
        customer.setUser(savedUser);
        customerService.saveAndFlush(customer);

        savedUser.setCustomer(customer);

        return userRepository.save(savedUser);
    }

    public void blockUser(int id) {
        User user = findById(id);
        user.setBlock(false);
        userRepository.saveAndFlush(user);
    }

    public void unblockUser(int id) {
        User user = findById(id);
        user.setBlock(true);
        userRepository.saveAndFlush(user);
    }

    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    public ResponseEntity<?> deleteUser(int id) {
        String phoneNumber = findById(id).getPhoneNumber();
        userRepository.deleteById(id);

        return ResponseEntity.ok().body("User with phone number:" + phoneNumber + " is delete");
    }

    private Courier converterToCourier(RegistrationCourierDto registrationCourierDto) {
        return modelMapper.map(registrationCourierDto, Courier.class);
    }

    private Customer converterToCustomer(RegistrationCustomerDto registrationCustomerDto) {
        return modelMapper.map(registrationCustomerDto, Customer.class);
    }
}
