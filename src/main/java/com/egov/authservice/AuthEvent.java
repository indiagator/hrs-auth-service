package com.egov.authservice;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthEvent
{
    String type;
    String principal;
}
