package org.example.DTO.response;

import org.example.enums.User_Type;

public record UserResponse(
   int id,
   String email,
   String fullName,
   User_Type role
) {}
