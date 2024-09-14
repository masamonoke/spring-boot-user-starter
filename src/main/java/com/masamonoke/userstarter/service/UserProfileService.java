package com.masamonoke.userstarter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.masamonoke.userstarter.exception.InvalidUserException;
import com.masamonoke.userstarter.exception.UserDataNotMatchException;
import com.masamonoke.userstarter.exception.UserEmailIsNotConfirmed;
import com.masamonoke.userstarter.exception.UserNotFoundException;
import com.masamonoke.userstarter.model.User;

public interface UserProfileService {
    User getUserById(Long id, String token) throws JsonProcessingException, UserNotFoundException, InvalidUserException;

    User updateUsername(Long id, String username, String token) throws JsonProcessingException, UserNotFoundException, InvalidUserException;

    User updatePassword(User user, String token) throws JsonProcessingException, UserNotFoundException, InvalidUserException;

    User updateEmail(Long id, String email, String token) throws JsonProcessingException, UserNotFoundException, InvalidUserException, UserEmailIsNotConfirmed;

    void deleteAccount(Long id, String token) throws JsonProcessingException, UserNotFoundException, InvalidUserException, UserEmailIsNotConfirmed;

    void restoreAccount(User user) throws UserNotFoundException, UserDataNotMatchException;
}
