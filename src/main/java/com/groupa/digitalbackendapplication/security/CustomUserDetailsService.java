package com.groupa.digitalbackendapplication.security;

import com.groupa.digitalbackendapplication.exceptions.ResourceNotFoundException;
import com.groupa.digitalbackendapplication.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.lang.module.ResolutionException;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.groupa.digitalbackendapplication.domain.entities.Customer customer = customerRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        return AuthUser.builder()
                .customer(customer)
                .build();
    }
}
