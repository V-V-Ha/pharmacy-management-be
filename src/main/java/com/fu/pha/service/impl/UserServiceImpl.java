package com.fu.pha.service.impl;

import com.fu.pha.dto.request.LoginDtoRequest;
import com.fu.pha.dto.response.JwtResponse;
import com.fu.pha.dto.response.ServiceResponse;
import com.fu.pha.entity.User;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.ResponseMessage;
import com.fu.pha.repository.RoleRepository;
import com.fu.pha.repository.UserRepository;
import com.fu.pha.security.impl.UserDetailsImpl;
import com.fu.pha.security.jwt.JwtUtils;
import com.fu.pha.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Override
    public User findByUsername(String username) {
        return null;
    }

    @Override
    public ServiceResponse<JwtResponse> login(LoginDtoRequest loginDtoRequest) {
        try {
            // Xác thực thông tin đăng nhập
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDtoRequest.getUsername(), loginDtoRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            JwtResponse jwtResponse = new JwtResponse(
                    jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles);

            return ServiceResponse.succeed(HttpStatus.OK, jwtResponse);

//        } catch (BadCredentialsException e) {
//            throw new ErrorMessage(HttpStatus.UNAUTHORIZED, ResponseMessage.INVALID_USERNAME);
//        } catch (LockedException e) {
//            throw new ErrorMessage(HttpStatus.LOCKED, ResponseMessage.ACCOUNT_LOCKED);
//        } catch (DisabledException e) {
//            throw new ErrorMessage(HttpStatus.FORBIDDEN, ResponseMessage.ACCOUNT_DISABLED);
//        } catch (Exception e) {
//            throw new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessage.LOGIN_ERROR);
//        }
            // FIXME: fix code at here
        } catch (Exception e) {
            throw new BadRequestException(ResponseMessage.LOGIN_ERROR);
        }
    }


//    @Override
//    public ServiceResponse<Object> register(UserDto request, String token) {
//        return null;
//    }
}
