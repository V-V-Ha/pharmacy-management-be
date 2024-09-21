package com.fu.pha.service;

import com.fu.pha.dto.request.AuthDto;
import com.fu.pha.dto.response.MessageResponse;
import com.fu.pha.entity.User;

public interface UserService {
    User findByUsername(String username);

    MessageResponse login(AuthDto authDto);
}
