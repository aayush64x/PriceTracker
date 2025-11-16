package com.project.PriceTracker.service;

import com.project.PriceTracker.dto.UserTemporaryDTO;
import com.project.PriceTracker.model.UserTemporary;
import com.project.PriceTracker.repository.UserTemporaryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserTemporaryService {
    private UserTemporaryRepository userTemporaryRepository;

    public UserTemporaryService(UserTemporaryRepository userTemporaryRepository){
        this.userTemporaryRepository = userTemporaryRepository;
    }

    public UserTemporary addUserTemporary(UserTemporaryDTO userTemporaryDTO){
        UserTemporary user = new UserTemporary();
        user.setEmail(userTemporaryDTO.getEmail());
        return userTemporaryRepository.save(user);
    }
}
