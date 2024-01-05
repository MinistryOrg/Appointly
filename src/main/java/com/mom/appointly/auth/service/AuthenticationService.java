package com.mom.appointly.auth.service;

import com.mom.appointly.auth.model.AuthenticationRequest;
import com.mom.appointly.auth.model.AuthenticationResponse;
import com.mom.appointly.auth.model.ChangePasswordRequest;
import com.mom.appointly.auth.model.RegisterRequest;
import com.mom.appointly.model.Role;
import com.mom.appointly.model.UserEntity;
import com.mom.appointly.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.security.Principal;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        var userExist = userRepo.findByEmail(request.getEmail());
        if(userExist.isEmpty()){
            var user = UserEntity.builder()
                    .firstname(request.getFirstname())
                    .lastname(request.getLastname())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(Role.ADMIN) // add the user you want the user to have when sign n
                    .build();
            userRepo.save(user);
            var jwtToken = jwtService.generateToken(user,user);
            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .build();
        }
        throw new RuntimeException("Fail to register the user, user already exist");
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepo.findByEmail(request.getEmail()).orElseThrow();
        var jwtToken = jwtService.generateToken(user,user);
        return AuthenticationResponse
                .builder()
                .token(jwtToken)
                .build();
    }

    public void changePassword(ChangePasswordRequest changePasswordRequest, Principal connectedUser) {
        var user = (UserEntity) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        // check if the users type the correct password
        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }
        // check if the password match
        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmationPassword())) {
            throw new RuntimeException("Passwords not the same");
        }
        // old password is the same with the new one
        if (changePasswordRequest.getOldPassword().equals(changePasswordRequest.getNewPassword())) {
            throw new RuntimeException("New password can't be the same with old");
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepo.save(user);
    }


}
